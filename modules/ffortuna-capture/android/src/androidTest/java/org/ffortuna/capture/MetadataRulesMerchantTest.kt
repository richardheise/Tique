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
}
