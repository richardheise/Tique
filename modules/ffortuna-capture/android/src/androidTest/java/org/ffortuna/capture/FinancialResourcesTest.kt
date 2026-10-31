package org.ffortuna.capture

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID

/** Invented values exercise durable reserve/card journals and their accounting rules. */
@RunWith(AndroidJUnit4::class)
class FinancialResourcesTest {
    private fun uuid() = UUID.randomUUID().toString()
    private fun json(command: String, fields: JSONObject) = fields.put("commandId", command).toString()

    @Test fun reserveMovesAndCardInvoiceDoNotDoubleCountExpense() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { catalog ->
                val ledger = catalog.ledger()
                val date = LocalDate.now().toString()
                val month = date.take(7)
                ledger.setup(JSONObject().put("openingDate", date).put("openingBalanceCents", 20_000).toString())

                val createCommand = uuid()
                val create = JSONObject().put("name", "Viagem fictícia").put("modality", "goal").put("date", date).put("amountCents", 5_000)
                val reserveId = ledger.createReserve(json(createCommand, JSONObject(create.toString())))
                assertEquals(reserveId, ledger.createReserve(json(createCommand, JSONObject(create.toString()))))
                val depositCommand = uuid()
                val deposit = JSONObject().put("reserveId", reserveId).put("direction", "deposit").put("amountCents", 3_000).put("date", date)
                val moveId = ledger.moveReserve(json(depositCommand, JSONObject(deposit.toString())))
                assertEquals(moveId, ledger.moveReserve(json(depositCommand, JSONObject(deposit.toString()))))
                assertEquals(17_000L, ledger.snapshot(month)["balanceCents"])
                @Suppress("UNCHECKED_CAST")
                var reserves = ledger.snapshot(month)["reserves"] as List<Map<String, Any?>>
                assertEquals(8_000L, reserves.single()["balanceCents"])

                val adjustmentCommand = uuid()
                val adjustment = JSONObject().put("reserveId", reserveId).put("amountCents", 7_000).put("date", date).put("note", "Conferência fictícia")
                val adjustmentId = ledger.updateReserve(json(adjustmentCommand, JSONObject(adjustment.toString())))
                assertEquals(adjustmentId, ledger.updateReserve(json(adjustmentCommand, JSONObject(adjustment.toString()))))
                assertEquals(17_000L, ledger.snapshot(month)["balanceCents"])
                reserves = ledger.snapshot(month)["reserves"] as List<Map<String, Any?>>
                assertEquals(7_000L, reserves.single()["balanceCents"])

                val secondReserve = ledger.createReserve(json(uuid(), JSONObject().put("name", "Emergência fictícia")
                    .put("modality", "goal").put("date", date).put("amountCents", 1_000)))
                val transfer = JSONObject().put("fromReserveId", reserveId).put("toReserveId", secondReserve).put("amountCents", 2_000).put("date", date)
                val transferCommand = uuid()
                val transferId = ledger.transferReserve(json(transferCommand, JSONObject(transfer.toString())))
                assertEquals(transferId, ledger.transferReserve(json(transferCommand, JSONObject(transfer.toString()))))
                assertEquals(17_000L, ledger.snapshot(month)["balanceCents"])
                @Suppress("UNCHECKED_CAST")
                reserves = ledger.snapshot(month)["reserves"] as List<Map<String, Any?>>
                assertEquals(5_000L, reserves.single { it["id"] == reserveId }["balanceCents"])
                assertEquals(3_000L, reserves.single { it["id"] == secondReserve }["balanceCents"])
                assertEquals(0L, ledger.snapshot(month)["incomeCents"])
                assertEquals(0L, ledger.snapshot(month)["expenseCents"])
            }
        }
    }

    @Test fun cardAndReserveResourcesStartWithInternalZeroWithoutManualOpeningBalance() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { catalog ->
                val ledger = catalog.ledger()
                val date = LocalDate.now().toString()
                val reserveId = ledger.createReserve(json(uuid(), JSONObject().put("name", "Reserva inicial fictícia")
                    .put("modality", "goal").put("date", date).put("amountCents", 0)))
                ledger.moveReserve(json(uuid(), JSONObject().put("reserveId", reserveId).put("direction", "deposit")
                    .put("amountCents", 50).put("date", date)))
                val cardId = ledger.createCard(json(uuid(), JSONObject().put("name", "Cartão sem abertura")
                    .put("closingDay", 18).put("dueDay", 8)))
                val purchase = JSONObject().put("cardId", cardId).put("amountCents", 250).put("effectiveDate", date)
                    .put("description", "Compra fictícia sem caixa").put("category", "Compras").put("categoryId", JSONObject.NULL)
                    .put("tagIds", JSONArray()).put("notes", "")
                ledger.cardPurchase(json(uuid(), purchase))
                val snapshot = ledger.snapshot(date.take(7))
                assertEquals(true, snapshot["configured"])
                assertEquals(-50L, snapshot["balanceCents"])
                @Suppress("UNCHECKED_CAST")
                val reserves = snapshot["reserves"] as List<Map<String, Any?>>
                assertEquals(50L, reserves.single()["balanceCents"])
                assertEquals(250L, snapshot["expenseCents"])
            }
        }
    }

    @Test fun historicalTransactionsBeforeOpeningDateRemainVisibleAndOpeningAppliesOnItsDate() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { catalog ->
                val ledger = catalog.ledger()
                val openingDate = LocalDate.now().toString()
                ledger.setup(JSONObject().put("openingDate", openingDate).put("openingBalanceCents", 1_000).toString())
                val historical = JSONObject().put("commandId", uuid()).put("amountCents", 250).put("kind", "expense")
                    .put("effectiveDate", "2020-01-02").put("description", "Compra histórica fictícia")
                    .put("category", "Compras").put("categoryId", JSONObject.NULL).put("tagIds", JSONArray())
                ledger.post(historical.toString())
                val historic = ledger.snapshot("2020-01")
                assertEquals(250L, historic["expenseCents"])
                assertEquals(750L, historic["balanceCents"])
                assertEquals(750L, ledger.snapshot(openingDate.take(7))["balanceCents"])
            }
        }
    }

    @Test fun cardPurchaseAndPaymentSurviveBackupAndMetadataReclassification() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            val date = LocalDate.now().toString()
            val month = date.take(7)
            val book = sandbox.newName()
            var backup = ByteArray(0)
            var purchaseId = ""
            var cardId = ""
            var commitmentId = ""
            var observedEventId = ""
            ClassificationCatalog(sandbox.context, book, true).use { catalog ->
                val ledger = catalog.ledger()
                ledger.setup(JSONObject().put("openingDate", date).put("openingBalanceCents", 20_000).toString())
                cardId = ledger.createCard(json(uuid(), JSONObject().put("name", "Cartão fictício").put("closingDay", 20).put("dueDay", 5)))
                val purchase = JSONObject().put("cardId", cardId).put("amountCents", 4_500).put("effectiveDate", date)
                    .put("description", "Compra teste mercado").put("category", "Compras").put("categoryId", JSONObject.NULL)
                    .put("tagIds", JSONArray()).put("notes", "")
                val command = uuid()
                purchaseId = ledger.cardPurchase(json(command, JSONObject(purchase.toString())))
                assertEquals(purchaseId, ledger.cardPurchase(json(command, JSONObject(purchase.toString()))))
                observedEventId = uuid()
                val observedAt = java.time.Instant.now().toString()
                val source = mapOf("eventId" to observedEventId, "packageName" to DiagnosticPolicy.PACKAGE,
                    "sourceKey" to "invented-card-observation", "revision" to 1, "publishedAt" to observedAt,
                    "capturedAt" to observedAt, "parserVersion" to BrlNotificationParser.VERSION,
                    "fieldEvidence" to emptyMap<String, Any?>(), "confidence" to mapOf("movement" to "certain",
                        "amount" to "certain", "nature" to "certain", "account" to "missing", "date" to "certain", "category" to "missing"))
                ledger.linkObservation(observedEventId, purchaseId, source)
                ledger.linkObservation(observedEventId, purchaseId, source)
                assertEquals(purchaseId, ledger.transactionForEvent(observedEventId)?.get("id"))
                assertEquals("extended", ledger.transactionForEvent(observedEventId)?.get("scope"))
                val beforePay = ledger.snapshot(month)
                assertEquals(4_500L, beforePay["expenseCents"])
                assertEquals(20_000L, beforePay["balanceCents"])
                @Suppress("UNCHECKED_CAST")
                val pending = (beforePay["commitments"] as List<Map<String, Any?>>).single()
                commitmentId = pending["id"] as String
                assertEquals(4_500L, pending["remainingCents"])
                val expectedInvoice = (if (LocalDate.parse(date).dayOfMonth > 20) LocalDate.parse(date).plusMonths(1) else LocalDate.parse(date)).toString().take(7)
                assertEquals(expectedInvoice, pending["invoiceMonth"])
                assertEquals(java.time.YearMonth.parse(expectedInvoice).plusMonths(1).atDay(5).toString(), pending["dueDate"])

                val paymentCommand = uuid()
                val payment = JSONObject().put("commitmentId", commitmentId).put("amountCents", 1_000).put("date", date)
                val paymentId = ledger.payCommitment(json(paymentCommand, JSONObject(payment.toString())))
                assertEquals(paymentId, ledger.payCommitment(json(paymentCommand, JSONObject(payment.toString()))))
                val afterPay = ledger.snapshot(month)
                assertEquals(4_500L, afterPay["expenseCents"])
                assertEquals(19_000L, afterPay["balanceCents"])
                @Suppress("UNCHECKED_CAST")
                val partlyPaid = (afterPay["commitments"] as List<Map<String, Any?>>).single()
                assertEquals(1_000L, partlyPaid["paidCents"])
                assertEquals(3_500L, partlyPaid["remainingCents"])

                val categoryId = catalog.snapshot().first { it["kind"] == "category" }["id"] as String
                val tagId = catalog.create("tag", "Fictícia").first { it["kind"] == "tag" }["id"] as String
                val rule = ledger.saveMetadataRule(JSONObject().put("description", "Compra teste mercado")
                    .put("categoryId", categoryId).put("category", catalog.snapshot().first { it["id"] == categoryId }["name"])
                    .put("tagIds", JSONArray().put(tagId)).toString())
                assertEquals(1, ledger.previewMetadataRule(rule["id"] as String).size)
                val categoryName = catalog.snapshot().first { it["id"] == categoryId }["name"] as String
                val updated = ledger.reclassifyMetadata(JSONObject().put("items", JSONArray().put(JSONObject()
                    .put("transactionId", purchaseId).put("scope", "extended").put("categoryId", categoryId)
                    .put("category", categoryName).put("tagIds", JSONArray().put(tagId)))).put("ruleId", rule["id"]).toString())
                assertEquals(1, updated["updated"])
                assertEquals(1, ledger.rows("finance_metadata_audit").size)
                assertEquals("extended", ledger.rows("finance_metadata_audit").single()["transaction_scope"])
                val renamedCategory = "Categoria fictícia renomeada"
                catalog.rename(categoryId, renamedCategory)
                assertEquals(renamedCategory, ledger.listMetadataRules().single()["category"])
                assertEquals(renamedCategory, ledger.suggestMetadata("Compra teste mercado")?.get("category"))
                assertEquals(categoryName, ledger.rows("finance_metadata_audit").single()["next_category"])
                val currentFields=ledger.transactionForEvent(observedEventId)?.get("fields") as Map<*, *>
                assertEquals(categoryId,currentFields["categoryId"])
                assertEquals(renamedCategory,currentFields["category"])
                assertEquals(listOf(tagId),currentFields["tagIds"])
                catalog.setArchived(categoryId, true)
                assertEquals(null, ledger.suggestMetadata("Compra teste mercado"))
                backup = catalog.exportFinance()
                assertEquals(FinancialBackup.CURRENT_VERSION, JSONObject(backup.toString(Charsets.UTF_8)).getInt("version"))
            }
            try {
                ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { restored ->
                    restored.importFinance(backup)
                    val snapshot = restored.ledger().snapshot(month)
                    assertEquals(4_500L, snapshot["expenseCents"])
                    assertEquals(19_000L, snapshot["balanceCents"])
                    @Suppress("UNCHECKED_CAST")
                    val transactions = snapshot["transactions"] as List<Map<String, Any?>>
                    val restoredPurchase = transactions.single { it["id"] == purchaseId }
                    assertEquals("extended", restoredPurchase["scope"])
                    assertEquals("card_purchase", restoredPurchase["kind"])
                    assertEquals(cardId, restoredPurchase["resourceId"])
                    assertNotNull(restored.ledger().rows("finance_metadata_audit").singleOrNull())
                    @Suppress("UNCHECKED_CAST")
                    val cards = snapshot["cards"] as List<Map<String, Any?>>
                    assertEquals(3_500L, cards.single()["debtCents"])
                    assertTrue(snapshot["commitments"] is List<*>)
                    assertEquals(purchaseId, restored.ledger().transactionForEvent(observedEventId)?.get("id"))
                    assertEquals(1, restored.ledger().rows("finance_extended_observation_links").size)
                }
                val v2 = JSONObject(backup.toString(Charsets.UTF_8)).put("version", 2)
                val oldTables = setOf("finance_reserves", "finance_cards", "finance_commitments", "finance_extended_transactions", "finance_extended_entries",
                    "finance_extended_tags", "finance_extended_cancellations", "finance_extended_observation_links", "finance_metadata_rules", "finance_metadata_audit")
                oldTables.forEach(v2.getJSONObject("data")::remove)
                val legacy = v2.toString().toByteArray(Charsets.UTF_8)
                try {
                    ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { oldRestore ->
                        oldRestore.importFinance(legacy)
                        val oldSnapshot = oldRestore.ledger().snapshot(month)
                        assertEquals(0, (oldSnapshot["cards"] as List<*>).size)
                        assertTrue(oldSnapshot["configured"] == true)
                    }
                } finally { legacy.fill(0) }
            } finally { backup.fill(0) }
        }
    }

    @Test fun extendedCancellationIsIdempotentKeepsEvidenceAndProtectsReserveAndInvoiceBalances() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            val book = sandbox.newName()
            val date = LocalDate.now().toString()
            val month = date.take(7)
            var backup = ByteArray(0)
            var purchaseId = ""
            var eventId = ""
            ClassificationCatalog(sandbox.context, book, true).use { catalog ->
                val ledger = catalog.ledger() // Resource workflows start with an internal zero opening.
                fun cancel(id: String, reason: String = "Correção fictícia") = ledger.cancel(JSONObject().put("id", id).put("reason", reason).toString())
                fun failure(id: String, reason: String, code: String) {
                    val error = assertThrows(FinanceFailure::class.java) { cancel(id, reason) }
                    assertEquals(code, error.code)
                }

                val reserveId = ledger.createReserve(json(uuid(), JSONObject().put("name", "Reserva cancelamento")
                    .put("modality", "goal").put("date", date).put("amountCents", 1_000)))
                val withdrawal = ledger.moveReserve(json(uuid(), JSONObject().put("reserveId", reserveId).put("direction", "withdraw")
                    .put("amountCents", 700).put("date", date)))
                val reserveOpening = ledger.rows("finance_extended_transactions").single { it["kind"] == "reserve_opening" }["id"] as String
                failure(reserveOpening, "Não anular ainda", "INSUFFICIENT_FUNDS")
                cancel(withdrawal)
                assertEquals(1_000L, (ledger.snapshot(month)["reserves"] as List<Map<String, Any?>>).single()["balanceCents"])
                cancel(reserveOpening)
                assertEquals(0L, (ledger.snapshot(month)["reserves"] as List<Map<String, Any?>>).single()["balanceCents"])
                assertEquals(1, ledger.rows("finance_reserves").size) // Cancellation never deletes the resource.
                cancel(reserveOpening)
                failure(reserveOpening, "Outro motivo", "CONFIRMATION_CONFLICT")

                val sourceReserve = ledger.createReserve(json(uuid(), JSONObject().put("name", "Origem transferência")
                    .put("modality", "goal").put("date", date).put("amountCents", 0)))
                val targetReserve = ledger.createReserve(json(uuid(), JSONObject().put("name", "Destino transferência")
                    .put("modality", "goal").put("date", date).put("amountCents", 0)))
                val deposit = ledger.moveReserve(json(uuid(), JSONObject().put("reserveId", sourceReserve).put("direction", "deposit")
                    .put("amountCents", 500).put("date", date)))
                val adjustment = ledger.updateReserve(json(uuid(), JSONObject().put("reserveId", sourceReserve).put("amountCents", 800)
                    .put("date", date).put("note", "Ajuste fictício")))
                cancel(adjustment, "Ajuste removido")
                assertEquals(500L, (ledger.snapshot(month)["reserves"] as List<Map<String, Any?>>).single { it["id"] == sourceReserve }["balanceCents"])
                val transfer = ledger.transferReserve(json(uuid(), JSONObject().put("fromReserveId", sourceReserve).put("toReserveId", targetReserve)
                    .put("amountCents", 200).put("date", date)))
                cancel(transfer, "Transferência removida")
                val reservePositions = ledger.snapshot(month)["reserves"] as List<Map<String, Any?>>
                assertEquals(500L, reservePositions.single { it["id"] == sourceReserve }["balanceCents"])
                assertEquals(0L, reservePositions.single { it["id"] == targetReserve }["balanceCents"])
                cancel(deposit, "Aporte removido")
                assertEquals(0L, (ledger.snapshot(month)["reserves"] as List<Map<String, Any?>>).single { it["id"] == sourceReserve }["balanceCents"])

                val cardId = ledger.createCard(json(uuid(), JSONObject().put("name", "Cartão cancelamento")
                    .put("closingDay", 20).put("dueDay", 5)))
                val purchase = JSONObject().put("cardId", cardId).put("amountCents", 1_000).put("effectiveDate", date)
                    .put("description", "Compra para cancelamento").put("category", "Compras").put("categoryId", JSONObject.NULL)
                    .put("tagIds", JSONArray()).put("notes", "")
                purchaseId = ledger.cardPurchase(json(uuid(), JSONObject(purchase.toString())))
                eventId = uuid()
                val captured = java.time.Instant.now().toString()
                ledger.linkObservation(eventId, purchaseId, mapOf("eventId" to eventId,"packageName" to DiagnosticPolicy.PACKAGE,
                    "sourceKey" to "invented-cancellation-evidence","revision" to 1,"publishedAt" to captured,"capturedAt" to captured,
                    "parserVersion" to BrlNotificationParser.VERSION,"fieldEvidence" to emptyMap<String, Any?>(),"confidence" to mapOf(
                        "movement" to "certain","amount" to "certain","nature" to "certain","account" to "missing","date" to "certain","category" to "missing")))
                val commitment = (ledger.snapshot(month)["commitments"] as List<Map<String, Any?>>).single()
                val payment = ledger.payCommitment(json(uuid(), JSONObject().put("commitmentId", commitment["id"])
                    .put("amountCents", 250).put("date", date)))
                failure(purchaseId, "Compra paga", "CANCELLATION_CONFLICT")
                cancel(payment, "Pagamento anulado") // Invoice debt reopens without counting another expense.
                assertEquals(1_000L, (ledger.snapshot(month)["commitments"] as List<Map<String, Any?>>).single()["remainingCents"])
                cancel(purchaseId, "Compra anulada")
                assertEquals(0L, ledger.snapshot(month)["expenseCents"])
                assertTrue((ledger.snapshot(month)["commitments"] as List<*>).isEmpty())
                assertEquals(purchaseId, ledger.transactionForEvent(eventId)?.get("id"))
                assertEquals(1, ledger.rows("finance_extended_observation_links").size)
                assertEquals(true, (ledger.snapshot(month)["transactions"] as List<Map<String, Any?>>).single { it["id"] == purchaseId }["voided"])
                cancel(purchaseId, "Compra anulada")
                failure(purchaseId, "Motivo divergente", "CONFIRMATION_CONFLICT")

                val debtId = ledger.createCommitment(json(uuid(), JSONObject().put("description", "Dívida fictícia")
                    .put("dueDate", LocalDate.parse(date).plusDays(2).toString()).put("amountCents", 900).put("cardId", JSONObject.NULL)))
                val debtPayment = ledger.payCommitment(json(uuid(), JSONObject().put("commitmentId", debtId).put("amountCents", 300).put("date", date)))
                val openingDebt = ledger.rows("finance_extended_transactions").single { it["kind"] == "commitment_opening" }["id"] as String
                failure(openingDebt, "Dívida parcialmente paga", "CANCELLATION_CONFLICT")
                cancel(debtPayment, "Pagamento de dívida anulado")
                cancel(openingDebt, "Dívida removida")
                assertEquals(0L, ledger.rows("finance_commitments").single { it["id"] == debtId }["amount"])
                assertEquals(0L, ledger.snapshot(month)["debtCents"])
                backup = catalog.exportFinance()
            }
            try {
                ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { restored ->
                    restored.importFinance(backup)
                    val ledger = restored.ledger()
                    assertEquals(purchaseId, ledger.transactionForEvent(eventId)?.get("id"))
                    assertEquals(0L, ledger.snapshot(month)["expenseCents"])
                    assertEquals(1, ledger.rows("finance_extended_observation_links").size)
                    assertTrue(ledger.rows("finance_extended_cancellations").size >= 6)
                }
            } finally { backup.fill(0) }
        }
    }

    @Test fun notificationEvidenceWithNatureDateAndPixFieldsValidatesAndSurvivesBackup() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            val eventId = uuid()
            val date = LocalDate.now().toString()
            val dateText = "Data da compra: $date"
            val captured = java.time.Instant.now().toString()
            val source = mapOf(
                "eventId" to eventId, "packageName" to DiagnosticPolicy.PACKAGE,
                "sourceKey" to "invented-new-evidence-shape", "revision" to 1,
                "publishedAt" to captured, "capturedAt" to captured,
                "parserVersion" to "notification-candidate/2+invented",
                "fieldEvidence" to mapOf(
                    "amountCents" to mapOf("value" to "1234", "sourceField" to "text", "observedText" to "R$ 12,34", "start" to 0, "end" to 8),
                    "kind" to mapOf("value" to "expense", "sourceField" to "text", "observedText" to "Compra aprovada", "start" to 0, "end" to 15),
                    "effectiveDate" to mapOf("value" to date, "sourceField" to "text", "observedText" to dateText, "start" to 0, "end" to dateText.length),
                    "paymentMethod" to mapOf("value" to "pix", "sourceField" to "text", "observedText" to "Pix", "start" to 0, "end" to 3)
                ),
                "confidence" to mapOf("movement" to "uncertain", "amount" to "certain", "nature" to "uncertain",
                    "account" to "missing", "date" to "certain", "category" to "uncertain", "paymentMethod" to "certain")
            )
            var backup = ByteArray(0)
            var transactionId = ""
            ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { catalog ->
                val ledger = catalog.ledger()
                val result = ledger.confirmObservedBatch(listOf(ObservedBatchPost(eventId, 1234, "expense", date,
                    "Compra fictícia", "A classificar", null, emptyList(), source)))
                assertEquals(listOf(date.take(7)), result["months"])
                transactionId = ledger.transactionForEvent(eventId)?.get("id") as String
                ledger.linkObservation(eventId, transactionId, source)
                ledger.validate()
                backup = catalog.exportFinance()
            }
            try {
                ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { restored ->
                    restored.importFinance(backup)
                    val ledger = restored.ledger()
                    ledger.validate()
                    ledger.linkObservation(eventId, transactionId, source)
                    assertEquals(transactionId, ledger.transactionForEvent(eventId)?.get("id"))
                    val savedTransaction = ledger.rows("finance_transactions").single { it["id"] == transactionId }
                    assertEquals(eventId, savedTransaction["source_event_id"])
                    val savedSource = JSONObject(savedTransaction["source_json"] as String)
                    assertEquals("certain", savedSource.getJSONObject("confidence").getString("paymentMethod"))
                    assertEquals(date, savedSource.getJSONObject("fieldEvidence").getJSONObject("effectiveDate").getString("value"))
                }
            } finally { backup.fill(0) }
        }
    }

    @Test fun merchantPlaceTagIsAppliedToNewBatchAndCardPurchaseWithoutChangingReplayIntent() {
        SecuritySandbox(InstrumentationRegistry.getInstrumentation().targetContext).use { sandbox ->
            ClassificationCatalog(sandbox.context, sandbox.newName(), true).use { catalog ->
                val ledger = catalog.ledger()
                val date = LocalDate.now().toString()
                val categoryId = catalog.snapshot().first { it["kind"] == "category" && it["name"] == "Compras" }["id"] as String
                val rule = ledger.saveMetadataRule(JSONObject().put("description", "Loja Fictícia")
                    .put("matchMode", "merchant").put("aliases", JSONArray().put("Loja Fictícia Online"))
                    .put("categoryId", categoryId).put("category", "Compras").put("tagIds", JSONArray()).toString())
                val placeTagId = rule["merchantTagId"] as String
                val description = "Compra na Loja Fictícia"
                assertEquals(placeTagId, ledger.suggestMetadata(description)?.get("merchantTagId"))

                val eventId = uuid()
                val captured = java.time.Instant.now().toString()
                val source = mapOf("eventId" to eventId, "packageName" to DiagnosticPolicy.PACKAGE,
                    "sourceKey" to "invented-merchant-batch", "revision" to 1, "publishedAt" to captured,
                    "capturedAt" to captured, "parserVersion" to BrlNotificationParser.VERSION,
                    "fieldEvidence" to emptyMap<String, Any?>(), "confidence" to mapOf("movement" to "certain",
                        "amount" to "certain", "nature" to "certain", "account" to "missing", "date" to "certain", "category" to "missing"))
                val item = ObservedBatchPost(eventId, 500, "expense", date, description, "A classificar", null, emptyList(), source)
                val first = ledger.confirmObservedBatch(listOf(item))
                val batchTransactionId = ledger.transactionForEvent(eventId)?.get("id") as String
                val batchFields = ledger.transactionForEvent(eventId)?.get("fields") as Map<String, Any?>
                assertEquals(listOf(placeTagId), batchFields["tagIds"])
                assertEquals(null, batchFields["categoryId"]) // The place rule adds its tag; it does not replace the user's category choice.
                assertEquals(batchTransactionId, ledger.confirmObservedBatch(listOf(item)).let { ledger.transactionForEvent(eventId)?.get("id") })
                assertEquals(1, first["registered"])
                val batchRequest = JSONObject(ledger.rows("finance_transactions").single { it["id"] == batchTransactionId }["request_json"] as String)
                assertEquals(0, batchRequest.getJSONArray("tagIds").length())
                assertEquals(1, ledger.rows("finance_metadata_audit").size)

                val cardId = ledger.createCard(json(uuid(), JSONObject().put("name", "Cartão de teste merchant")
                    .put("closingDay", 20).put("dueDay", 5)))
                val command = uuid()
                val purchase = JSONObject().put("cardId", cardId).put("amountCents", 700).put("effectiveDate", date)
                    .put("description", description).put("category", "A classificar").put("categoryId", JSONObject.NULL)
                    .put("tagIds", JSONArray()).put("notes", "")
                val cardTransactionId = ledger.cardPurchase(json(command, JSONObject(purchase.toString())))
                assertEquals(cardTransactionId, ledger.cardPurchase(json(command, JSONObject(purchase.toString()))))
                val cardFields = ledger.snapshot(date.take(7))["transactions"] as List<Map<String, Any?>>
                val cardPurchase = cardFields.single { it["id"] == cardTransactionId }
                assertEquals(listOf(placeTagId), cardPurchase["tagIds"])
                assertEquals(null, cardPurchase["categoryId"])
                assertEquals(2, ledger.rows("finance_metadata_audit").size)
                val canonicalRequest = JSONObject(ledger.rows("finance_extended_transactions").single { it["id"] == cardTransactionId }["request_json"] as String)
                assertEquals(0, canonicalRequest.getJSONArray("tagIds").length())
            }
        }
    }
}
