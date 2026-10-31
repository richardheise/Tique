package org.ffortuna.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Invented phrases for grammar coverage; these are not bank templates or captured samples. */
class NotificationFinanceHintParserTest {
    private fun parse(text: String) = NotificationFinanceHintParser.parse(listOf("text" to text))

    @Test fun pixIsRecordedAsPaymentMethodAndNeverSelectsNature() {
        val result = parse("Pix recebido · R$ 12,34")
        assertNull(result.kind)
        assertEquals("missing", result.natureConfidence)
        assertEquals("pix", result.paymentMethod)
        assertEquals("pix", result.fieldEvidence["paymentMethod"]?.get("value"))
        assertNull(parse("Pagamento recebido via Pix · R$ 12,34").kind)
        assertNull(parse("Compra no Pix · R$ 12,34").kind)
        val received = parse("Pix recebido de Maria · R$ 12,34")
        assertNull(received.kind)
        val outgoing = parse("Pix enviado · R$ 12,34")
        assertNull(outgoing.kind)
        val explicitPurchase = parse("Compra aprovada via Pix · R$ 12,34")
        assertEquals("expense", explicitPurchase.kind)
        assertEquals("uncertain", explicitPurchase.natureConfidence)
    }

    @Test fun onlyExplicitTransferLanguageSuggestsTransfer() {
        val result = parse("Transferência recebida · R$ 12,34")
        assertEquals("transfer", result.kind)
        assertEquals("uncertain", result.natureConfidence)
        assertEquals("transfer", result.fieldEvidence["kind"]?.get("value"))
        assertNull(parse("Pix enviado · R$ 12,34").kind)
    }

    @Test fun clearIncomeAndExpenseSignalsAreSuggestionsWithSourceEvidence() {
        val income = parse("Salário creditado · R$ 1.200,00")
        assertEquals("income", income.kind)
        assertEquals("uncertain", income.natureConfidence)
        assertEquals("Salário", income.fieldEvidence["kind"]?.get("observedText"))

        val expense = parse("Compra aprovada · R$ 35,90")
        assertEquals("expense", expense.kind)
        assertEquals("uncertain", expense.natureConfidence)
    }

    @Test fun cardRefundInvestmentAndReserveSignalsRequireSpecialReview() {
        for (text in listOf(
            "Compra aprovada no cartão · R$ 35,90",
            "Pagamento de fatura · R$ 35,90",
            "Estorno da compra · R$ 35,90",
            "Resgate da caixinha · R$ 35,90",
            "Aporte no investimento · R$ 35,90"
        )) {
            val result = parse(text)
            assertEquals(text, "other", result.kind)
            assertEquals(text, "uncertain", result.natureConfidence)
        }
    }

    @Test fun conflictingKindsAndWeakPurchaseMentionsStayUncertain() {
        val conflict = parse("Compra aprovada e salário creditado · R$ 35,90")
        assertNull(conflict.kind)
        assertEquals("uncertain", conflict.natureConfidence)

        val weak = parse("Confira sua próxima compra · R$ 35,90")
        assertEquals("expense", weak.kind)
        assertEquals("uncertain", weak.natureConfidence)
    }

    @Test fun effectiveDateNeedsAnExplicitMovementDateLabel() {
        val result = parse("Compra realizada em 28/10/2026 · R$ 35,90")
        assertEquals("2026-10-28", result.effectiveDate)
        assertEquals("certain", result.dateConfidence)
        val evidence = result.fieldEvidence.getValue("effectiveDate")
        assertEquals("2026-10-28", evidence["value"])
        assertEquals("Compra realizada em 28/10/2026", evidence["observedText"])

        val unlabeled = parse("Notificação recebida em 28/10/2026 · R$ 35,90")
        assertNull(unlabeled.effectiveDate)
        assertEquals("missing", unlabeled.dateConfidence)
    }

    @Test fun conflictingOrInvalidExplicitDatesRemainReviewOnly() {
        val conflicting = parse("Data da compra: 28/10/2026; data do pagamento: 29/10/2026")
        assertNull(conflicting.effectiveDate)
        assertEquals("uncertain", conflicting.dateConfidence)

        val invalid = parse("Data da compra: 31/02/2026")
        assertNull(invalid.effectiveDate)
        assertEquals("uncertain", invalid.dateConfidence)
        assertTrue(invalid.fieldEvidence.getValue("effectiveDate")["observedText"].toString().contains("31/02/2026"))
    }

    @Test fun truncatedTextDoesNotProduceConfidentNatureOrEffectiveDate() {
        val result = NotificationFinanceHintParser.parse(
            listOf("text" to "Compra realizada em 28/10/2026"), truncated = true
        )
        assertEquals("expense", result.kind)
        assertEquals("uncertain", result.natureConfidence)
        assertNull(result.effectiveDate)
        assertEquals("uncertain", result.dateConfidence)
    }
}
