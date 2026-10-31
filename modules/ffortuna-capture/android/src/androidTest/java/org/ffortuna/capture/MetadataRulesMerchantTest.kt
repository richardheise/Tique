package org.ffortuna.capture

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID

/** Invented place names and descriptions; these are not captured bank templates. */
@RunWith(AndroidJUnit4::class)
class MetadataRulesMerchantTest {
    private fun merchant(name: String, aliases: List<String> = emptyList()) = JSONObject()
        .put("matchMode", "merchant")
        .put("description", name)
        .put("aliases", JSONArray(aliases))
        .put("categoryId", JSONObject.NULL)
        .put("category", "A classificar")
        .put("tagIds", JSONArray())

    @Test fun placeRuleAlwaysSuggestsItsTagAndAmbiguousPlacesDoNotChooseOne() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { catalog ->
                val ledger = catalog.ledger()
                val place = ledger.saveMetadataRule(merchant("BLUMENAUENSE", listOf("BNU Loja")).toString())
                val placeTagId = place["merchantTagId"] as String
                assertEquals("BLUMENAUENSE", place["merchantTagName"])
                val repeated = ledger.saveMetadataRule(merchant("BLUMENAUENSE", listOf("BNU Loja"))
                    .put("id", place["id"]).toString())
                assertEquals(placeTagId, repeated["merchantTagId"])

                val byName = ledger.suggestMetadata("Compra · Blumenauense · R$ 12,34")!!
                val byAlias = ledger.suggestMetadata("Compra · BNU Loja · R$ 12,34")!!
                assertEquals(listOf(placeTagId), byName["tagIds"])
                assertEquals(listOf(placeTagId), byAlias["tagIds"])
                assertNull(byName["categoryId"])

                ledger.saveMetadataRule(merchant("BNU Mercado", listOf("BLUMENAUENSE")).toString())
                assertNull(ledger.suggestMetadata("Compra · Blumenauense · R$ 12,34"))
            }
        }
    }

    @Test fun exactRuleKeepsItsCategoryAndIncludesOneUnambiguousPlaceTag() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { catalog ->
                val ledger = catalog.ledger()
                val place = ledger.saveMetadataRule(merchant("BLUMENAUENSE").toString())
                val category = catalog.snapshot().first { it["kind"] == "category" }
                val exactDescription = "Compra · Blumenauense · R$ 12,34"
                ledger.saveMetadataRule(JSONObject()
                    .put("description", exactDescription)
                    .put("categoryId", category["id"])
                    .put("category", category["name"])
                    .put("tagIds", JSONArray())
                    .toString())

                val suggestion = ledger.suggestMetadata(exactDescription)!!
                assertEquals(category["id"], suggestion["categoryId"])
                assertEquals(listOf(place["merchantTagId"]), suggestion["tagIds"])
                assertNotNull(suggestion["merchantTagName"])
            }
        }
    }

    @Test fun historicalManualApplyAndNewPostingKeepRequestAndAuditThePlaceTag() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { catalog ->
                val ledger = catalog.ledger()
                val date = LocalDate.now().toString()
                ledger.setup(JSONObject().put("openingDate", date).put("openingBalanceCents", 0).toString())
                val description = "Compra fictícia BLUMENAUENSE"
                val oldCommand = UUID.randomUUID().toString()
                val original = JSONObject().put("commandId", oldCommand).put("amountCents", 1234).put("kind", "expense")
                    .put("effectiveDate", date).put("description", description).put("category", "A classificar")
                    .put("categoryId", JSONObject.NULL).put("tagIds", JSONArray())
                val oldId = ledger.post(original.toString())
                val originalRequest = ledger.rows("finance_transactions").single { it["id"] == oldId }["request_json"]

                val rule = ledger.saveMetadataRule(merchant("BLUMENAUENSE").toString())
                val placeTagId = rule["merchantTagId"] as String
                assertEquals(1, ledger.previewMetadataRule(rule["id"] as String).size)
                val reclass = JSONObject().put("ruleId", rule["id"]).put("items", JSONArray().put(JSONObject()
                    .put("transactionId", oldId).put("scope", "finance").put("categoryId", JSONObject.NULL)
                    .put("category", "A classificar").put("tagIds", JSONArray().put(placeTagId))))
                assertEquals(1, ledger.reclassifyMetadata(reclass.toString())["updated"])
                assertEquals(0, ledger.reclassifyMetadata(reclass.toString())["updated"])
                val oldRow = ledger.rows("finance_transactions").single { it["id"] == oldId }
                assertEquals(originalRequest, oldRow["request_json"])
                assertEquals(1, ledger.rows("finance_metadata_audit").size)

                val newCommand = UUID.randomUUID().toString()
                val newPost = JSONObject(original.toString()).put("commandId", newCommand)
                val newId = ledger.post(newPost.toString())
                val newRow = ledger.rows("finance_transactions").single { it["id"] == newId }
                assertEquals(emptySet<String>(), JSONObject(newRow["request_json"] as String).getJSONArray("tagIds")
                    .let { (0 until it.length()).map { index -> it.getString(index) }.toSet() })
                assertEquals(listOf(placeTagId), ledger.rows("finance_tags").filter { it["transaction_id"] == newId }.map { it["tag_id"] })
                assertEquals(newId, ledger.post(newPost.toString()))
                assertEquals(2, ledger.rows("finance_metadata_audit").size)
            }
        }
    }
}
