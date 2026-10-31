package org.ffortuna.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MetadataRulesTest {
    @Test fun descriptionMatchingIsExactAfterWhitespaceCaseAndUnicodeNormalization() {
        assertEquals(MetadataRules.matchKey(" Caf\u00E9\t  Loja "), MetadataRules.matchKey("cafe\u0301\u2003loja"))
        assertNotEquals(MetadataRules.matchKey("Loja A"), MetadataRules.matchKey("Loja-A"))
        assertThrows(FinanceFailure::class.java) { MetadataRules.matchKey(" \t\u00A0 ") }
        assertThrows(FinanceFailure::class.java) { MetadataRules.matchKey("Movimento\u0000inválido") }
    }

    @Test fun merchantRulesMatchOnlyExplicitNamesAndAliasesAtWordBoundaries() {
        assertEquals(true, MetadataRules.matchesMerchantTerm("Compra BLUMENAUENSE Loja · Pix", "blumenauense"))
        assertEquals(true, MetadataRules.matchesMerchantTerm("Pagamento MERCADO DO BAIRRO", "mercado do bairro"))
        assertEquals(true, MetadataRules.matchesMerchantTerm("Compra BLOOM · Pix", "Bloom"))
        assertEquals(false, MetadataRules.matchesMerchantTerm("Compra XBLUMENAUENSEY", "BLUMENAUENSE"))
        assertEquals(false, MetadataRules.matchesMerchantTerm("Compra sem identificação", "blumenauense"))
    }

    @Test fun merchantRuleConflictsStayAmbiguousAndExactRuleWins() {
        val first = mapOf<String, Any?>("matchMode" to "merchant", "id" to "merchant-1")
        val second = mapOf<String, Any?>("matchMode" to "merchant", "id" to "merchant-2")
        val exact = mapOf<String, Any?>("matchMode" to "exact", "id" to "exact")
        assertEquals(null, MetadataRules.selectSuggestionCandidate(emptyList()))
        assertEquals(null, MetadataRules.selectSuggestionCandidate(listOf(first, second)))
        assertEquals(first, MetadataRules.selectSuggestionCandidate(listOf(first)))
        assertEquals(exact, MetadataRules.selectSuggestionCandidate(listOf(first, exact)))
        assertEquals(null, MetadataRules.selectSuggestionCandidate(listOf(first, second, exact)))
    }
}
