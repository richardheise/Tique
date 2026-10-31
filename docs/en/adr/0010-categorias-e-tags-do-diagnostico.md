[Português](../../adr/0010-categorias-e-tags-do-diagnostico.md)

# Managed categories and tags

Decision accepted on 30/10/2026: one optional main category and several optional tags per notification review. The user confirmed this model after functionally approving 0.6.1. Financial nature (income, expense, transfer, or other) remains independent of thematic classification.

The catalog has stable UUIDs and editable names. It starts with eight local categories: Comida (Food), Lazer (Leisure), Contas (Bills), Transporte (Transport), Saúde (Health), Educação (Education), Compras (Shopping), and Outros (Other). With no selection, the preview shows “A classificar” (Unclassified). Tags start empty. Create, rename, archive, and reactivate are explicit operations; archiving prevents new selections while preserving old ones. There is no permanent deletion and no automatic rule created from a correction. Names may have up to 80 characters, use NFC normalization, and are unique per type case-insensitively, including archived options. Limits: 200 total options and 20 tags per review.

`CaptureRuntime` remains the owner of the serial executor. `ClassificationCatalog` opens a separate SQLCipher database, `ffortuna-classifications-v1.db`, with a random key wrapped by Keystore and a transactional schema 0→1 migration. Missing key or a database with existing material fails without recreation. Existing SQLCipher policies and disabled Android backup apply. This is a configuration separation, not a second financial ledger.

The catalog survives the seven-day retention period and “Delete temporary records.” Reviews remain in the temporary database; optional JSON fields `categoryId` and `tagIds` are a compatible extension of existing schema 2. Earlier free-text labels remain readable and are not automatically associated with a category merely because the names match. When an option is selected, it uses the ID. The saved category name is retained for compatibility; reads resolve the current name from the catalog. Tags are resolved through the catalog and contain no financial inference.

Before saving, the same executor validates that options exist and have the right type and state. Review editing uses an atomic write in the diagnostics database. There is no distributed write across databases: creating an option and saving a review are independent actions, so interruption may leave an unused option but never a partially saved review. Archiving or renaming does not change IDs. Reprocessing replaces only the parsing result and preserves reviewed classification.

Alternatives: text-only labels would make reuse and renaming harder; several tags without a main category would make monthly grouping ambiguous; keeping the catalog with temporary records would require changing disposal policy and the vault-removal guarantee. A separate configuration database adds a key and small storage footprint but keeps lifecycles explicit.

Consequences: classification still does not confirm transactions, change balance, or report real spending. Selections expire with the observation; the catalog remains. At this stage, neither the catalog nor real reviews are included in synthetic local/portable copies. The interface says uninstalling removes the catalog; integrating this data with recovery policy and the real ledger requires its own increment. No new dependency, permission, or data upload.

Twelve new scenarios share callbacks between Android JUnit and the local interface suite. Scope becomes `isolated-native-checks/v4`, 60 checks in total. Compiling scenarios is not the same as running them on a phone.
