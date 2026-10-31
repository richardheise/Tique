package org.ffortuna.capture

import net.zetetic.database.sqlcipher.SQLiteDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.time.Instant
import java.util.Locale
import java.util.UUID

/** Deterministic description rules and metadata-only correction helpers for the existing finance DB. */
object MetadataRules {
    data class Change(
        val transactionId: String,
        val categoryId: String?,
        val category: String,
        val tagIds: List<String>,
        val scope: String = "finance"
    )

    fun createSchema(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE finance_metadata_rules(
            id TEXT PRIMARY KEY NOT NULL,
            description TEXT NOT NULL,
            match_key TEXT NOT NULL UNIQUE,
            category_id TEXT REFERENCES classifications(id),
            category_legacy TEXT NOT NULL,
            tag_ids_json TEXT NOT NULL,
            enabled INTEGER NOT NULL CHECK(enabled IN (0,1)),
            created_at TEXT NOT NULL,
            updated_at TEXT NOT NULL
        )""")
        db.execSQL("""CREATE TABLE finance_metadata_audit(
            id TEXT PRIMARY KEY NOT NULL,
            transaction_scope TEXT NOT NULL,
            transaction_id TEXT NOT NULL,
            rule_id TEXT,
            previous_category_id TEXT,
            previous_category TEXT NOT NULL,
            previous_tag_ids_json TEXT NOT NULL,
            next_category_id TEXT,
            next_category TEXT NOT NULL,
            next_tag_ids_json TEXT NOT NULL,
            changed_at TEXT NOT NULL
        )""")
        db.execSQL("CREATE INDEX finance_metadata_audit_transaction ON finance_metadata_audit(transaction_scope,transaction_id,changed_at)")
    }

    /** Schema 5→6: tolerate development databases that already have some new columns. */
    fun migrateMerchantSchema(db: SQLiteDatabase) {
        val columns = db.rawQuery("PRAGMA table_info(finance_metadata_rules)", null).use { cursor ->
            buildSet { while (cursor.moveToNext()) add(cursor.getString(1)) }
        }
        if ("match_mode" !in columns) db.execSQL("ALTER TABLE finance_metadata_rules ADD COLUMN match_mode TEXT NOT NULL DEFAULT 'exact'")
        if ("aliases_json" !in columns) db.execSQL("ALTER TABLE finance_metadata_rules ADD COLUMN aliases_json TEXT NOT NULL DEFAULT '[]'")
        if ("merchant_tag_id" !in columns) db.execSQL("ALTER TABLE finance_metadata_rules ADD COLUMN merchant_tag_id TEXT")
    }

    fun validate(db: SQLiteDatabase) {
        db.rawQuery("SELECT id,description,match_key,category_id,category_legacy,tag_ids_json,enabled,created_at,updated_at,match_mode,aliases_json,merchant_tag_id FROM finance_metadata_rules", null).use { rows ->
            while (rows.moveToNext()) {
                val id = rows.getString(0)
                val description = rows.getString(1)
                if (runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false).not() || matchKey(description) != rows.getString(2)) throw FinanceFailure("INVALID_STATE")
                val mode = rows.getString(9)
                if (mode !in setOf("exact", "merchant") || rows.getInt(6) !in setOf(0, 1)) throw FinanceFailure("INVALID_STATE")
                val aliases = parseAliasesJson(rows.getString(10))
                aliases.forEach { cleanLabel(cleanDescription(it)) }
                val aliasKeys = aliases.map(::matchKey)
                if (aliasKeys.distinct().size != aliases.size || (mode == "exact" && aliases.isNotEmpty())) throw FinanceFailure("INVALID_STATE")
                val tagIds = parseTagsJson(rows.getString(5))
                val maxTags = if (mode == "merchant") 19 else 20
                if (tagIds.distinct().size != tagIds.size || tagIds.size > maxTags) throw FinanceFailure("INVALID_STATE")
                val categoryId = rows.getString(3).takeUnless { rows.isNull(3) }
                if (categoryId != null) ensureClassification(db, categoryId, "category")
                for (tagId in tagIds) ensureClassification(db, tagId, "tag")
                val merchantTagId = rows.getString(11).takeUnless { rows.isNull(11) }
                if (mode == "merchant") {
                    cleanLabel(description)
                    if (aliasKeys.any { it == matchKey(description) } || merchantTagId == null || merchantTagId in tagIds) throw FinanceFailure("INVALID_STATE")
                    ensureClassification(db, merchantTagId, "tag")
                } else if (merchantTagId != null) throw FinanceFailure("INVALID_STATE")
                cleanLabel(rows.getString(4))
                Instant.parse(rows.getString(7)); Instant.parse(rows.getString(8))
            }
        }
    }

    private fun ensureClassification(db: SQLiteDatabase, id: String, kind: String) {
        if (runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false).not()) throw FinanceFailure("INVALID_STATE")
        db.rawQuery("SELECT id FROM classifications WHERE id=? AND kind=?", arrayOf(id, kind)).use {
            if (!it.moveToFirst()) throw FinanceFailure("INVALID_STATE")
        }
    }

    /** Caller owns the DB transaction and backup boundary for every mutation. */
    fun listRules(db: SQLiteDatabase): List<Map<String, Any?>> = db.rawQuery(
        "SELECT id,description,category_id,category_legacy,tag_ids_json,enabled,created_at,updated_at,match_key,match_mode,aliases_json,merchant_tag_id FROM finance_metadata_rules ORDER BY updated_at DESC,id",
        null
    ).use { cursor -> buildList {
        while (cursor.moveToNext()) add(ruleMap(cursor, db))
    } }

    fun saveRule(db: SQLiteDatabase, json: String): Map<String, Any?> {
        val root = parseObject(json, 8192)
        val required = setOf("description", "categoryId", "category", "tagIds")
        val keys = root.keys().asSequence().toSet()
        if (!keys.containsAll(required) || !required.plus(setOf("enabled", "id", "matchMode", "aliases")).containsAll(keys)) throw FinanceFailure("INVALID_FIELDS")
        val id = if (root.has("id") && !root.isNull("id")) uuid(root.opt("id")) else UUID.randomUUID().toString()
        val description = cleanDescription(root.opt("description"))
        val matchMode = if (root.has("matchMode")) root.optString("matchMode") else "exact"
        if (matchMode !in setOf("exact", "merchant")) throw FinanceFailure("INVALID_FIELDS")
        val aliases = if (root.has("aliases")) parseAliases(root.opt("aliases")) else emptyList()
        if (matchMode == "exact" && aliases.isNotEmpty()) throw FinanceFailure("INVALID_FIELDS")
        if (matchMode == "merchant") {
            cleanLabel(description)
            if (aliases.any { matchKey(it) == matchKey(description) }) throw FinanceFailure("INVALID_FIELDS")
        }
        val categoryId = optionalString(root, "categoryId")
        val category = if (matchMode == "merchant" && categoryId == null && (root.isNull("category") || root.optString("category").isBlank()))
            "A classificar" else cleanLabel(root.opt("category"))
        val tagIds = parseTags(root.opt("tagIds"))
        if (matchMode == "merchant" && tagIds.size > 19) throw FinanceFailure("INVALID_CLASSIFICATION")
        val enabled = if (root.has("enabled")) root.opt("enabled") as? Boolean ?: throw FinanceFailure("INVALID_FIELDS") else null
        val key = matchKey(description)
        val previous = findRule(db, id)
        val now = Instant.now().toString()
        val oldCategoryId = previous?.get("categoryId") as? String
        val oldTagIds = previous?.get("tagIds") as? List<String>
        validateMetadata(db, categoryId, category, tagIds, oldCategoryId, oldTagIds)
        val duplicate = db.rawQuery("SELECT id FROM finance_metadata_rules WHERE match_key=? AND id!=? LIMIT 1", arrayOf(key, id)).use { it.moveToFirst() }
        if (duplicate) throw FinanceFailure("RULE_DUPLICATE")
        val selectedEnabled = enabled ?: (previous?.get("enabled") as? Boolean ?: true)
        val merchantTagId = if (matchMode == "merchant") ensureMerchantTag(db, description) else null
        val usedByAudit = previous != null && db.rawQuery("SELECT id FROM finance_metadata_audit WHERE rule_id=? LIMIT 1", arrayOf(id)).use { it.moveToFirst() }
        if (usedByAudit && previous?.get("merchantTagId") != null &&
            (matchMode != "merchant" || previous["merchantTagId"] != merchantTagId)) throw FinanceFailure("RULE_IN_USE")
        if (previous == null) {
            db.execSQL("INSERT INTO finance_metadata_rules(id,description,match_key,category_id,category_legacy,tag_ids_json,enabled,created_at,updated_at,match_mode,aliases_json,merchant_tag_id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                arrayOf(id, description, key, categoryId, category, JSONArray(tagIds).toString(), if (selectedEnabled) 1 else 0, now, now, matchMode, JSONArray(aliases).toString(), merchantTagId))
        } else {
            db.execSQL("UPDATE finance_metadata_rules SET description=?,match_key=?,category_id=?,category_legacy=?,tag_ids_json=?,enabled=?,updated_at=?,match_mode=?,aliases_json=?,merchant_tag_id=? WHERE id=?",
                arrayOf(description, key, categoryId, category, JSONArray(tagIds).toString(), if (selectedEnabled) 1 else 0, now, matchMode, JSONArray(aliases).toString(), merchantTagId, id))
        }
        return findRule(db, id) ?: throw FinanceFailure("INVALID_STATE")
    }

    fun setRuleEnabled(db: SQLiteDatabase, id: String, enabled: Boolean): Map<String, Any?> {
        uuid(id)
        val current = findRule(db, id) ?: throw FinanceFailure("RULE_MISSING")
        if (current["enabled"] != enabled) {
            db.execSQL("UPDATE finance_metadata_rules SET enabled=?,updated_at=? WHERE id=?", arrayOf<Any>(if (enabled) 1 else 0, Instant.now().toString(), id))
        }
        return findRule(db, id) ?: throw FinanceFailure("INVALID_STATE")
    }

    /** Deterministic exact or explicitly named merchant suggestion for an unreviewed candidate. */
    fun suggestion(db: SQLiteDatabase, description: String): Map<String, Any?>? {
        val key = try { matchKey(description) } catch (_: Exception) { return null }
        val candidates = matchingRules(db, key)
        val rule = selectSuggestionCandidate(candidates) ?: return null
        val merchantMatches = candidates.filter { it["matchMode"] == "merchant" }
        val categoryId = rule["categoryId"] as? String
        val matchMode = rule["matchMode"] as? String ?: "exact"
        val category = if (categoryId == null) {
            if (matchMode == "exact") return null
            "A classificar"
        } else db.rawQuery("SELECT name,kind,archived FROM classifications WHERE id=?", arrayOf(categoryId)).use {
            if (it.moveToFirst() && it.getString(1) == "category" && it.getInt(2) == 0) it.getString(0) else null
        } ?: return null
        val tags = (rule["tagIds"] as? List<String> ?: return null).toMutableList()
        val merchantRule = if (matchMode == "merchant") rule else merchantMatches.singleOrNull()
        val merchantTagId = merchantRule?.get("merchantTagId") as? String
        if (merchantRule != null) tags += merchantTagId ?: return null
        val distinctTags = tags.distinct().sorted()
        for (tag in distinctTags) {
            val active = db.rawQuery("SELECT kind,archived FROM classifications WHERE id=?", arrayOf(tag)).use {
                it.moveToFirst() && it.getString(0) == "tag" && it.getInt(1) == 0
            }
            if (!active) return null
        }
        return mapOf("ruleId" to (rule["id"] as String), "matchMode" to matchMode, "description" to (rule["description"] as String),
            "categoryId" to categoryId, "category" to category, "tagIds" to distinctTags,
            "merchantTagId" to merchantTagId, "merchantTagName" to merchantRule?.get("merchantTagName"))
    }

    /** Exact normalized description match; only live, confirmed income/expense records are returned. */
    fun previewRule(db: SQLiteDatabase, id: String): List<Map<String, Any?>> {
        uuid(id)
        val rule = findRule(db, id) ?: throw FinanceFailure("RULE_MISSING")
        val rows = mutableListOf<Map<String, Any?>>()
        val sources = listOf(
            Triple("finance", "finance_transactions", "finance_cancellations"),
            Triple("extended", "finance_extended_transactions", "finance_extended_cancellations")
        )
        for ((scope, table, cancellations) in sources) db.rawQuery("""SELECT t.id,t.kind,t.amount,t.effective_date,t.description,t.category_id,t.category_legacy,t.source_event_id,t.source_json
            FROM $table t WHERE t.kind IN (${if (scope == "finance") "'income','expense'" else "'card_purchase'"})
            AND NOT EXISTS(SELECT 1 FROM $cancellations c WHERE c.transaction_id=t.id)
            ORDER BY t.effective_date DESC,t.created_at DESC,t.id""", null).use { cursor ->
            while (cursor.moveToNext()) {
                val description = cursor.getString(4)
                if (!matches(rule, matchKey(description))) continue
                val transactionId = cursor.getString(0)
                val sourceType = if (cursor.isNull(8)) "manual" else try {
                    if (JSONObject(cursor.getString(8)).optString("packageName") == "org.tique.statement") "statement" else "notification"
                } catch (_: Exception) { throw FinanceFailure("INVALID_STATE") }
                val tagTable = if (scope == "finance") "finance_tags" else "finance_extended_tags"
                val tags = db.rawQuery("SELECT tag_id FROM $tagTable WHERE transaction_id=? ORDER BY tag_id", arrayOf(transactionId)).use { tagCursor ->
                    buildList { while (tagCursor.moveToNext()) add(tagCursor.getString(0)) }
                }
                val tagNames = tags.map { tagId ->
                    db.rawQuery("SELECT name FROM classifications WHERE id=?", arrayOf(tagId)).use { tagCursor ->
                        if (!tagCursor.moveToFirst()) throw FinanceFailure("INVALID_STATE")
                        tagCursor.getString(0)
                    }
                }
                val categoryId = cursor.getString(5).takeUnless { cursor.isNull(5) }
                val category = if (categoryId == null) cursor.getString(6) else db.rawQuery("SELECT name FROM classifications WHERE id=?", arrayOf(categoryId)).use { categoryCursor ->
                    if (!categoryCursor.moveToFirst()) cursor.getString(6) else categoryCursor.getString(0)
                }
                val merchantMatches = matchingRules(db, matchKey(description)).filter { it["matchMode"] == "merchant" }
                val merchantRule = if (merchantMatches.size == 1) merchantMatches.single() else null
                rows += mapOf(
                    "id" to transactionId, "kind" to cursor.getString(1), "amountCents" to cursor.getLong(2),
                    "effectiveDate" to cursor.getString(3), "description" to description,
                    "categoryId" to categoryId, "category" to category,
                    "tagIds" to tags, "tags" to tagNames, "sourceEventId" to cursor.getString(7).takeUnless { cursor.isNull(7) },
                    "sourceType" to if (scope == "extended") "card" else sourceType,
                    "scope" to scope, "voided" to false, "cancellationReason" to null,
                    "merchantTagId" to merchantRule?.get("merchantTagId"), "merchantTagName" to merchantRule?.get("merchantTagName"),
                    "merchantAmbiguous" to (merchantMatches.size > 1)
                )
            }
        }
        return rows
    }

    /** Add only the known place tag after posting; the canonical request and ledger entries stay unchanged. */
    fun applyMerchantTagAfterInsert(db: SQLiteDatabase, transactionId: String, scope: String): Boolean {
        uuid(transactionId)
        if (scope !in setOf("finance", "extended")) throw FinanceFailure("INVALID_FIELDS")
        val table = if (scope == "finance") "finance_transactions" else "finance_extended_transactions"
        val tagTable = if (scope == "finance") "finance_tags" else "finance_extended_tags"
        val kind: String
        val description: String
        val categoryId: String?
        val category: String
        db.rawQuery("SELECT kind,description,category_id,category_legacy FROM $table WHERE id=?", arrayOf(transactionId)).use { cursor ->
            if (!cursor.moveToFirst()) throw FinanceFailure("NOT_FOUND")
            kind = cursor.getString(0)
            if ((scope == "finance" && kind !in setOf("income", "expense")) || (scope == "extended" && kind != "card_purchase")) return false
            description = cursor.getString(1)
            categoryId = cursor.getString(2).takeUnless { cursor.isNull(2) }
            category = cursor.getString(3)
        }
        val placeRules = matchingRules(db, matchKey(description)).filter { it["matchMode"] == "merchant" }
        val rule = placeRules.singleOrNull() ?: return false
        val tagId = rule["merchantTagId"] as? String ?: return false
        val tagIsActive = db.rawQuery("SELECT archived,kind FROM classifications WHERE id=?", arrayOf(tagId)).use {
            it.moveToFirst() && it.getString(1) == "tag" && it.getInt(0) == 0
        }
        if (!tagIsActive) return false
        val oldTags = db.rawQuery("SELECT tag_id FROM $tagTable WHERE transaction_id=? ORDER BY tag_id", arrayOf(transactionId)).use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
        }
        if (tagId in oldTags) return false
        if (oldTags.size >= 20) throw FinanceFailure("INVALID_CLASSIFICATION")
        val nextTags = (oldTags + tagId).distinct().sorted()
        db.execSQL("INSERT INTO $tagTable(transaction_id,tag_id) VALUES(?,?)", arrayOf(transactionId, tagId))
        db.execSQL("INSERT INTO finance_metadata_audit VALUES(?,?,?,?,?,?,?,?,?,?,?)", arrayOf(
            UUID.randomUUID().toString(), scope, transactionId, rule["id"], categoryId, category, JSONArray(oldTags.sorted()).toString(),
            categoryId, category, JSONArray(nextTags).toString(), Instant.now().toString()
        ))
        return true
    }

    fun replayOriginalRequest(db: SQLiteDatabase, scope: String, transactionId: String, requestJson: String): JSONObject {
        val chain = auditChain(db, scope, transactionId)
        if (chain.isEmpty()) return JSONObject(requestJson)
        val original = chain.first().previous
        return JSONObject(requestJson)
            .put("categoryId", original.categoryId ?: JSONObject.NULL)
            .put("category", if (scope == "finance" && original.categoryId != null) "" else original.category)
            .put("tagIds", JSONArray(original.tagIds))
    }

    fun validateMetadataHistory(
        db: SQLiteDatabase, scope: String, transactionId: String,
        requestCategoryId: String?, requestCategory: String, requestTagIds: Set<String>,
        currentCategoryId: String?, currentCategory: String, currentTagIds: Set<String>
    ): Boolean {
        val requestTags = requestTagIds.sorted()
        fun matchesRequest(state: AuditState): Boolean =
            requestCategoryId == state.categoryId && requestTags == state.tagIds.sorted() &&
                (requestCategory == state.category || (scope == "finance" && state.categoryId != null && requestCategory.isEmpty()))
        val chain = auditChain(db, scope, transactionId)
        if (chain.isEmpty()) return matchesRequest(AuditState(currentCategoryId, currentCategory, currentTagIds.toList()))
        for (index in 1 until chain.size) {
            if (chain[index - 1].next != chain[index].previous) return false
        }
        val initial = chain.first().previous
        val latest = chain.last().next
        if (latest.categoryId != currentCategoryId || latest.category != currentCategory || latest.tagIds.toSet() != currentTagIds) return false
        // The original 1.0 reclassifier rewrote request_json. Accept its latest audit state
        // as a legacy base while new rows keep the original request unchanged.
        return chain.any { matchesRequest(it.previous) || matchesRequest(it.next) } || matchesRequest(initial) || matchesRequest(latest)
    }

    private data class AuditState(val categoryId: String?, val category: String, val tagIds: List<String>)
    private data class AuditStep(val previous: AuditState, val next: AuditState)

    private fun auditChain(db: SQLiteDatabase, scope: String, transactionId: String): List<AuditStep> = db.rawQuery(
        "SELECT previous_category_id,previous_category,previous_tag_ids_json,next_category_id,next_category,next_tag_ids_json FROM finance_metadata_audit WHERE transaction_scope=? AND transaction_id=? ORDER BY rowid",
        arrayOf(scope, transactionId)
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(AuditStep(
                AuditState(cursor.getString(0).takeUnless { cursor.isNull(0) }, cursor.getString(1), parseTagsJson(cursor.getString(2)).sorted()),
                AuditState(cursor.getString(3).takeUnless { cursor.isNull(3) }, cursor.getString(4), parseTagsJson(cursor.getString(5)).sorted())
            ))
        }
    }

    /** Caller must be inside FinancialLedger.atomic; writes only category/tags and an audit row. */
    fun reclassify(db: SQLiteDatabase, changes: List<Change>, ruleId: String? = null): Int {
        if (changes.isEmpty() || changes.size > 1000 || changes.map { it.transactionId }.distinct().size != changes.size) throw FinanceFailure("INVALID_FIELDS")
        val rule = ruleId?.let { uuid(it); findRule(db, it) ?: throw FinanceFailure("RULE_MISSING") }
        if (rule != null && rule["enabled"] != true) throw FinanceFailure("RULE_DISABLED")
        val now = Instant.now().toString()
        var updated = 0
        for (change in changes) {
            uuid(change.transactionId)
            if (change.scope !in setOf("finance", "extended")) throw FinanceFailure("INVALID_FIELDS")
            val description: String
            val oldCategoryId: String?
            val oldCategory: String
            val oldTags: List<String>
            val kind: String
            val table = if (change.scope == "finance") "finance_transactions" else "finance_extended_transactions"
            val tagTable = if (change.scope == "finance") "finance_tags" else "finance_extended_tags"
            val cancellationTable = if (change.scope == "finance") "finance_cancellations" else "finance_extended_cancellations"
            db.rawQuery("SELECT kind,description,category_id,category_legacy FROM $table WHERE id=?", arrayOf(change.transactionId)).use { cursor ->
                if (!cursor.moveToFirst()) throw FinanceFailure("NOT_FOUND")
                kind = cursor.getString(0)
                val allowedKinds = if (change.scope == "finance") setOf("income", "expense") else setOf("card_purchase")
                if (kind !in allowedKinds) throw FinanceFailure("INVALID_FIELDS")
                description = cursor.getString(1)
                oldCategoryId = cursor.getString(2).takeUnless { cursor.isNull(2) }
                oldCategory = cursor.getString(3)
            }
            val voided = db.rawQuery("SELECT transaction_id FROM $cancellationTable WHERE transaction_id=?", arrayOf(change.transactionId)).use { it.moveToFirst() }
            if (voided) throw FinanceFailure("TRANSACTION_VOIDED")
            oldTags = db.rawQuery("SELECT tag_id FROM $tagTable WHERE transaction_id=? ORDER BY tag_id", arrayOf(change.transactionId)).use { cursor ->
                buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
            }
            val category = cleanLabel(change.category)
            val tags = change.tagIds.distinct().sorted()
            if (tags.size != change.tagIds.size || tags.size > 20) throw FinanceFailure("INVALID_CLASSIFICATION")
            validateMetadata(db, change.categoryId, category, tags, oldCategoryId, oldTags)
            if (rule != null) {
                val mode = rule["matchMode"] as? String ?: "exact"
                if (!matches(rule, matchKey(description))) throw FinanceFailure("RULE_MISMATCH")
                if (mode == "exact") {
                    val placeRules = matchingRules(db, matchKey(description)).filter { it["matchMode"] == "merchant" }
                    if (placeRules.size > 1) throw FinanceFailure("AMBIGUOUS_RULE_MATCH")
                    val expectedTags = ((rule["tagIds"] as? List<String> ?: emptyList()) +
                        (placeRules.singleOrNull()?.get("merchantTagId") as? String ?: "")).filter { it.isNotEmpty() }.distinct().sorted()
                    if (change.categoryId != rule["categoryId"] || category != rule["category"] || tags != expectedTags) throw FinanceFailure("RULE_MISMATCH")
                } else {
                    val requiredTags = (oldTags + (rule["tagIds"] as? List<String> ?: emptyList()) +
                        (rule["merchantTagId"] as? String ?: throw FinanceFailure("INVALID_STATE"))).distinct().sorted()
                    val ruleCategoryId = rule["categoryId"] as? String
                    if ((ruleCategoryId != null && (change.categoryId != ruleCategoryId || category != rule["category"])) || tags != requiredTags) throw FinanceFailure("RULE_MISMATCH")
                }
            }
            if (change.categoryId == oldCategoryId && category == oldCategory && tags == oldTags.sorted()) continue

            db.execSQL("UPDATE $table SET category_id=?,category_legacy=? WHERE id=?",
                arrayOf(change.categoryId, category, change.transactionId))
            db.execSQL("DELETE FROM $tagTable WHERE transaction_id=?", arrayOf(change.transactionId))
            for (tag in tags) db.execSQL("INSERT INTO $tagTable(transaction_id,tag_id) VALUES(?,?)", arrayOf(change.transactionId, tag))
            db.execSQL("INSERT INTO finance_metadata_audit VALUES(?,?,?,?,?,?,?,?,?,?,?)", arrayOf(
                UUID.randomUUID().toString(), change.scope, change.transactionId, ruleId, oldCategoryId, oldCategory, JSONArray(oldTags.sorted()).toString(),
                change.categoryId, category, JSONArray(tags).toString(), now
            ))
            updated++
        }
        return updated
    }

    fun matchKey(value: String): String {
        val normalized = Normalizer.normalize(collapseWhitespace(value).lowercase(Locale.ROOT), Normalizer.Form.NFC)
        if (normalized.isBlank() || normalized.length > 500 || normalized.any { Character.isISOControl(it) }) throw FinanceFailure("INVALID_RULE_DESCRIPTION")
        return normalized
    }

    private fun findRule(db: SQLiteDatabase, id: String): Map<String, Any?>? = db.rawQuery(
        "SELECT id,description,category_id,category_legacy,tag_ids_json,enabled,created_at,updated_at,match_key,match_mode,aliases_json,merchant_tag_id FROM finance_metadata_rules WHERE id=?", arrayOf(id)
    ).use { if (it.moveToFirst()) ruleMap(it, db) else null }

    private fun ruleMap(cursor: android.database.Cursor, db: SQLiteDatabase): Map<String, Any?> {
        val id = cursor.getString(0)
        val description = cursor.getString(1)
        val categoryId = cursor.getString(2).takeUnless { cursor.isNull(2) }
        val savedCategory = cursor.getString(3)
        val category = if(categoryId==null) savedCategory else db.rawQuery("SELECT name FROM classifications WHERE id=? AND kind='category'",arrayOf(categoryId)).use { if(it.moveToFirst()) it.getString(0) else savedCategory }
        val tags = parseTagsJson(cursor.getString(4))
        val enabled = cursor.getInt(5) == 1
        val createdAt = cursor.getString(6)
        val updatedAt = cursor.getString(7)
        val key = if (cursor.columnCount > 8) cursor.getString(8) else matchKey(description)
        val mode = if (cursor.columnCount > 9) cursor.getString(9) else "exact"
        val aliases = if (cursor.columnCount > 10) parseAliasesJson(cursor.getString(10)) else emptyList()
        val merchantTagId = if (cursor.columnCount > 11 && !cursor.isNull(11)) cursor.getString(11) else null
        val merchantTagName = merchantTagId?.let { tagName(db, it) }
        return mapOf("id" to id, "description" to description, "categoryId" to categoryId, "category" to category,
            "tagIds" to tags, "enabled" to enabled, "createdAt" to createdAt, "updatedAt" to updatedAt, "matchKey" to key,
            "matchMode" to mode, "aliases" to aliases, "merchantTagId" to merchantTagId, "merchantTagName" to merchantTagName)
    }

    private fun ensureMerchantTag(db: SQLiteDatabase, name: String): String {
        val normalized = cleanLabel(name)
        val key = Normalizer.normalize(normalized.uppercase(Locale.ROOT).lowercase(Locale.ROOT), Normalizer.Form.NFC)
        val existing = db.rawQuery("SELECT id,archived FROM classifications WHERE kind='tag' AND name_key=? LIMIT 1", arrayOf(key)).use {
            if (it.moveToFirst()) it.getString(0) to (it.getInt(1) == 1) else null
        }
        if (existing != null) {
            if (existing.second) db.execSQL("UPDATE classifications SET archived=0 WHERE id=?", arrayOf(existing.first))
            return existing.first
        }
        val count = db.rawQuery("SELECT COUNT(*) FROM classifications", null).use { it.moveToFirst(); it.getInt(0) }
        if (count >= 200) throw FinanceFailure("CLASSIFICATION_LIMIT")
        val id = UUID.randomUUID().toString()
        db.execSQL("INSERT INTO classifications VALUES(?,?,?,?,0)", arrayOf(id, "tag", normalized, key))
        return id
    }

    private fun tagName(db: SQLiteDatabase, id: String): String? = db.rawQuery(
        "SELECT name FROM classifications WHERE id=? AND kind='tag'", arrayOf(id)
    ).use { if (it.moveToFirst()) it.getString(0) else null }

    private fun matches(rule: Map<String, Any?>, key: String): Boolean =
        when (rule["matchMode"] as? String ?: "exact") {
            "exact" -> key == rule["matchKey"]
            "merchant" -> (listOf(rule["description"] as? String ?: "") + (rule["aliases"] as? List<String> ?: emptyList()))
                .any { matchesMerchantTerm(key, it) }
            else -> false
        }

    internal fun selectSuggestionCandidate(candidates: List<Map<String, Any?>>): Map<String, Any?>? {
        val exact = candidates.filter { it["matchMode"] == "exact" }
        val merchants = candidates.filter { it["matchMode"] == "merchant" }
        // An exact full-description choice is more specific than a merchant alias.
        // Multiple merchant matches stay ambiguous and are never applied silently.
        return when {
            exact.size == 1 && merchants.size <= 1 -> exact.single()
            exact.size == 1 -> null
            exact.size > 1 -> null
            else -> merchants.singleOrNull()
        }
    }

    private fun matchingRules(db: SQLiteDatabase, normalizedDescription: String): List<Map<String, Any?>> =
        db.rawQuery("SELECT id FROM finance_metadata_rules WHERE enabled=1 ORDER BY id", null).use { cursor ->
            buildList { while (cursor.moveToNext()) findRule(db, cursor.getString(0))?.let { rule -> if (matches(rule, normalizedDescription)) add(rule) } }
        }

    internal fun matchesMerchantTerm(description: String, term: String): Boolean {
        val pattern = runCatching { matchKey(term) }.getOrNull() ?: return false
        val text = runCatching { matchKey(description) }.getOrNull() ?: return false
        return merchantContains(text, pattern)
    }

    private fun merchantContains(text: String, pattern: String): Boolean {
        if (pattern.isBlank()) return false
        var from = 0
        while (from <= text.length - pattern.length) {
            val start = text.indexOf(pattern, from)
            if (start < 0) return false
            val end = start + pattern.length
            if ((start == 0 || !isWord(text[start - 1])) && (end == text.length || !isWord(text[end]))) return true
            from = start + 1
        }
        return false
    }

    private fun isWord(value: Char): Boolean {
        val type = Character.getType(value)
        return Character.isLetterOrDigit(value) || type == Character.NON_SPACING_MARK.toInt() || type == Character.COMBINING_SPACING_MARK.toInt()
    }

    private fun validateMetadata(db: SQLiteDatabase, categoryId: String?, category: String, tags: List<String>, oldCategoryId: String?, oldTags: List<String>?) {
        if (categoryId != null) {
            uuid(categoryId)
            db.rawQuery("SELECT name,archived,kind FROM classifications WHERE id=?", arrayOf(categoryId)).use {
                if (!it.moveToFirst() || it.getString(2) != "category" || it.getString(0) != category || (it.getInt(1) != 0 && categoryId != oldCategoryId)) throw FinanceFailure("INVALID_CLASSIFICATION")
            }
        } else cleanLabel(category)
        if (tags.distinct().size != tags.size || tags.size > 20) throw FinanceFailure("INVALID_CLASSIFICATION")
        for (tagId in tags) {
            uuid(tagId)
            db.rawQuery("SELECT archived,kind FROM classifications WHERE id=?", arrayOf(tagId)).use {
                if (!it.moveToFirst() || it.getString(1) != "tag" || (it.getInt(0) != 0 && tagId !in oldTags.orEmpty())) throw FinanceFailure("INVALID_CLASSIFICATION")
            }
        }
    }

    private fun parseTags(value: Any?): List<String> {
        val array = value as? JSONArray ?: throw FinanceFailure("INVALID_CLASSIFICATION")
        if (array.length() > 20) throw FinanceFailure("INVALID_CLASSIFICATION")
        val values = (0 until array.length()).map { uuid(array.opt(it)) }
        if (values.distinct().size != values.size) throw FinanceFailure("INVALID_CLASSIFICATION")
        return values.sorted()
    }

    private fun parseAliases(value: Any?): List<String> {
        val array = value as? JSONArray ?: throw FinanceFailure("INVALID_FIELDS")
        if (array.length() > 20) throw FinanceFailure("INVALID_FIELDS")
        val aliases = (0 until array.length()).map { index ->
            val alias = cleanDescription(array.opt(index))
            if (alias.length > 80) throw FinanceFailure("INVALID_RULE_DESCRIPTION")
            alias
        }
        val keys = aliases.map(::matchKey)
        if (keys.distinct().size != keys.size) throw FinanceFailure("INVALID_FIELDS")
        return aliases
    }

    private fun parseAliasesJson(value: String): List<String> {
        val array = try { JSONArray(value) } catch (_: Exception) { throw FinanceFailure("INVALID_STATE") }
        if (array.length() > 20) throw FinanceFailure("INVALID_STATE")
        return (0 until array.length()).map { index ->
            val alias = array.getString(index)
            cleanLabel(cleanDescription(alias))
        }
    }

    private fun parseTagsJson(value: String): List<String> {
        val array = try { JSONArray(value) } catch (_: Exception) { throw FinanceFailure("INVALID_STATE") }
        return (0 until array.length()).map { array.getString(it) }
    }

    private fun parseObject(json: String, max: Int): JSONObject {
        if (json.length > max) throw FinanceFailure("INVALID_FIELDS")
        return try { JSONObject(json) } catch (_: Exception) { throw FinanceFailure("INVALID_FIELDS") }
    }

    private fun optionalString(root: JSONObject, key: String): String? {
        val value = root.opt(key)
        if (value == null || value === JSONObject.NULL) return null
        return value as? String ?: throw FinanceFailure("INVALID_CLASSIFICATION")
    }

    private fun cleanDescription(raw: Any?): String {
        val value = raw as? String ?: throw FinanceFailure("INVALID_RULE_DESCRIPTION")
        val cleaned = collapseWhitespace(value)
        if (cleaned.isBlank() || cleaned.length > 500 || cleaned.any { Character.isISOControl(it) }) throw FinanceFailure("INVALID_RULE_DESCRIPTION")
        return Normalizer.normalize(cleaned, Normalizer.Form.NFC)
    }

    private fun collapseWhitespace(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFC)
        val result = StringBuilder(normalized.length)
        var pendingSpace = false
        for (character in normalized) {
            if (Character.isWhitespace(character) || Character.isSpaceChar(character)) {
                pendingSpace = result.isNotEmpty()
            } else {
                if (pendingSpace) result.append(' ')
                result.append(character)
                pendingSpace = false
            }
        }
        return result.toString()
    }

    private fun cleanLabel(raw: Any?): String {
        val value = raw as? String ?: throw FinanceFailure("INVALID_CLASSIFICATION")
        if (value.isBlank() || value.length > 80 || value.any { Character.isISOControl(it) }) throw FinanceFailure("INVALID_CLASSIFICATION")
        return value
    }

    private fun uuid(value: Any?): String {
        val id = value as? String ?: throw FinanceFailure("INVALID_FIELDS")
        if (runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false) != true) throw FinanceFailure("INVALID_FIELDS")
        return id
    }
}
