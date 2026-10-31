package org.ffortuna.capture

import java.time.LocalDate

data class NotificationEvidence(
    val value: String,
    val sourceField: String,
    val observedText: String,
    val start: Int,
    val end: Int
) {
    fun toMap(): Map<String, Any> = mapOf(
        "value" to value, "sourceField" to sourceField, "observedText" to observedText, "start" to start, "end" to end
    )
}

data class NotificationFinanceHints(
    val kind: String?,
    val natureConfidence: String,
    val effectiveDate: String?,
    val dateConfidence: String,
    val paymentMethod: String?,
    val fieldEvidence: Map<String, Map<String, Any>>
)

/** Conservative hints only. This parser never confirms or posts a movement. */
object NotificationFinanceHintParser {
    const val VERSION = "notification-finance-hints/1"

    private data class Signal(val value: String, val evidence: NotificationEvidence)
    private data class DateSignal(val date: String?, val evidence: NotificationEvidence)

    private val transfer = Regex("(?i)\\btransfer[eê]ncia\\b|\\btransferid[oa]s?\\b|\\btransferir\\b")
    private val cardOrBill = Regex("(?i)\\bfatura\\b|\\b(?:compra|pagamento)\\b.{0,48}\\b(?:cart[ãa]o|cr[eé]dito)\\b|\\bcart[ãa]o\\b.{0,48}\\b(?:compra|pagamento)\\b")
    private val specialHandling = Regex("(?i)\\bestorn[oa]\\b|\\breembols[oa]\\b|\\bdevolu[cç][aã]o\\b|\\baplica[cç][aã]o\\b|\\binvestiment[oa]\\b|\\baport[eoa]\\b|\\bresgat[aeiou]\\b")
    private val income = Regex("(?i)\\bsal[aá]rio\\b|\\bvenda\\s+(?:realizada|conclu[ií]da|confirmada)\\b|\\brendimento\\s+(?:recebido|creditado)\\b|\\breceita\\s+(?:recebida|confirmada)\\b")
    private val expense = Regex("(?i)\\bcompra\\s+(?:aprovada|realizada|efetuada|conclu[ií]da)\\b|\\bcompra(?:\\s+(?:de|por|no\\s+valor\\s+de))?\\s*R\\$|\\bpagamento\\s+(?:realizado|efetuado|aprovado)\\b|\\b(?:boleto|conta)\\s+pago\\b")
    private val weakExpense = Regex("(?i)\\bcompra\\b|\\bpagamento\\b")
    private val pix = Regex("(?i)\\bpix\\b")
    private val labeledDate = Regex(
        "(?i)(data\\s+(?:do|da)\\s+(?:movimento|transa[cç][aã]o|compra|pagamento)|(?:compra|movimento|transa[cç][aã]o)\\s+(?:realizada|efetuada|ocorrida)\\s+em)\\s*[:\\-]?\\s*(\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4})"
    )

    fun parse(fields: List<Pair<String, String>>, truncated: Boolean = false): NotificationFinanceHints {
        val signals = mutableListOf<Signal>()
        val dates = mutableListOf<DateSignal>()
        var pixEvidence: NotificationEvidence? = null
        for ((field, text) in fields) {
            collect(text, field, transfer, "transfer", signals)
            collect(text, field, cardOrBill, "other", signals)
            collect(text, field, specialHandling, "other", signals)
            collect(text, field, income, "income", signals)
            collect(text, field, expense, "expense", signals)
            if (pixEvidence == null) pix.find(text)?.let { match ->
                pixEvidence = evidence("pix", field, text, match.range.first, match.range.last + 1)
            }
            for (match in labeledDate.findAll(text)) {
                val raw = match.groups[2]?.value ?: continue
                val parsed = parseDate(raw)
                val labelStart = match.range.first
                dates += DateSignal(parsed, evidence(parsed ?: "invalid", field, text, labelStart, match.range.last + 1))
            }
        }

        val unsupported = signals.filter { it.value == "other" }
        val incoming = signals.filter { it.value == "income" }
        val outgoing = signals.filter { it.value == "expense" }
        val transfers = signals.filter { it.value == "transfer" }
        val allSupportedKinds = (incoming + outgoing + transfers).map { it.value }.distinct()
        val (kind, natureConfidence, natureEvidence) = when {
            unsupported.isNotEmpty() && transfers.isEmpty() -> Triple("other", "certain", unsupported.first().evidence)
            unsupported.isNotEmpty() -> Triple(null, "uncertain", unsupported.first().evidence)
            allSupportedKinds.size > 1 -> Triple(null, "uncertain", (incoming + outgoing + transfers).first().evidence)
            transfers.isNotEmpty() -> Triple("transfer", "certain", transfers.first().evidence)
            incoming.isNotEmpty() -> Triple("income", "certain", incoming.first().evidence)
            outgoing.isNotEmpty() -> Triple("expense", "certain", outgoing.first().evidence)
            pixEvidence != null -> Triple(null, "missing", null)
            weakExpense.containsMatchIn(fields.joinToString(" ") { it.second }) -> {
                val fieldMatch = fields.firstNotNullOfOrNull { (field, text) -> weakExpense.find(text)?.let { field to (text to it) } }
                if (fieldMatch == null) Triple(null, "missing", null)
                else Triple("expense", "uncertain", evidence("expense", fieldMatch.first, fieldMatch.second.first, fieldMatch.second.second.range.first, fieldMatch.second.second.range.last + 1))
            }
            else -> Triple(null, "missing", null)
        }
        // Pix describes the payment rail, not whether money left/entered personal spending.
        // Even explicit surrounding wording remains a review hint, never a confident nature.
        val finalKind = kind
        // Notifications are hints, not proof of what the movement means. Keep every
        // non-empty nature suggestion explicitly uncertain, including Pix wording.
        val finalNatureConfidence = if (kind != null) "uncertain" else natureConfidence

        val distinctDates = dates.map { it.date }.distinct()
        val (effectiveDate, dateConfidence, dateEvidence) = when {
            dates.isEmpty() -> Triple(null, "missing", null)
            distinctDates.size != 1 || distinctDates.single() == null -> Triple(null, "uncertain", dates.first().evidence)
            else -> Triple(distinctDates.single(), "certain", dates.first().evidence)
        }
        val finalEffectiveDate = if (truncated && effectiveDate != null) null else effectiveDate
        val finalDateConfidence = if (truncated && effectiveDate != null) "uncertain" else dateConfidence
        val evidence = linkedMapOf<String, Map<String, Any>>()
        natureEvidence?.let { evidence["kind"] = it.toMap() }
        dateEvidence?.let { evidence["effectiveDate"] = it.toMap() }
        pixEvidence?.let { evidence["paymentMethod"] = it.toMap() }
        return NotificationFinanceHints(
            kind = finalKind,
            natureConfidence = finalNatureConfidence,
            effectiveDate = finalEffectiveDate,
            dateConfidence = finalDateConfidence,
            paymentMethod = if (pixEvidence == null) null else "pix",
            fieldEvidence = evidence
        )
    }

    private fun collect(text: String, field: String, pattern: Regex, value: String, into: MutableList<Signal>) {
        pattern.find(text)?.let { match -> into += Signal(value, evidence(value, field, text, match.range.first, match.range.last + 1)) }
    }

    private fun evidence(value: String, field: String, text: String, start: Int, end: Int) =
        NotificationEvidence(value, field, text.substring(start, end), start, end)

    private fun parseDate(value: String): String? {
        return try {
            val date = if (value.contains('/')) {
                val parts = value.split('/')
                if (parts.size != 3 || parts[2].length != 4) null
                else LocalDate.of(parts[2].toInt(), parts[1].toInt(), parts[0].toInt())
            } else LocalDate.parse(value)
            date?.toString()
        } catch (_: Exception) { null }
    }
}
