# Correção auditada, pistas de captura e estabelecimentos

Decisão de 31/10/2026. O usuário priorizou anulação segura, execução dos testes Android e melhoria da captura, com foco em compras e Pix. Pediu que o lugar fosse uma tag, exemplificando BLUMENAUENSE → Comida, e autorizou pedir o preenchimento ao usuário dentro do app.

## Decisões

Anular movimentos estendidos mantém linhas, motivo e vínculo de evidência. Os efeitos são recalculados em uma transação: anular pagamento reabre dívida; anular compra reduz a fatura. Dependências que deixariam caixinha negativa ou dívida inconsistente bloqueiam a operação, sem alteração parcial. Recursos não são apagados e repetir o mesmo comando/motivo é idempotente. A correção financeira consiste em anular e registrar novamente, preservando o histórico; não é estorno no banco.

Catálogo passa ao esquema 6 (4→5 muda a semântica admitida de cancelamentos; 5→6 acrescenta campos de regras por lugar). Backup financeiro v5 inclui esses campos; versões antigas são lidas com modo exato, aliases vazios e tag de lugar nula. Downgrade não deve tentar reinterpretar dados novos como versão antiga.

Uma regra de estabelecimento tem nome, nomes alternativos explícitos, tag de lugar obrigatória e categoria opcional. Correspondência usa palavras normalizadas, não substrings soltas. A tag é criada/reutilizada por ID. Regras por descrição exata antigas continuam válidas; quando há um único estabelecimento reconhecido, sua tag também acompanha uma regra exata. Estabelecimentos ambíguos exigem revisão. Aplicação ao histórico exige prévia e confirmação; regras sugerem classificação para novos candidatos não revisados e não substituem correções humanas. A revisão permite informar o lugar sem lançar a transação.

Um parser complementar sugere natureza apenas com palavras explícitas e mantém sua confiança incerta. Pix é meio de pagamento, não receita/despesa por si. Data efetiva só vem de rótulo explícito e calendário válido, nunca da data de publicação. Estorno, fatura, cartão, aporte e resgate pedem tratamento próprio. Evidências registram campo, texto e posições; validade da fonte financeira foi estendida para esses campos opcionais sem rejeitar fontes antigas. Reprocessar mantém edições humanas e confirmações; snippets derivados não escapam da política de retenção.

Não há lançamento automático novo nem template Nubank declarado validado. Fixtures são sintéticas. Uma cadeia de Markov de ordem 3 não oferece a mesma explicação ou garantia de identidade que um vínculo explícito de estabelecimento. Trigramas podem futuramente ajudar na sugestão de nomes parecidos, sem classificar automaticamente; não foram implementados aqui.

## Testes e consequências

A suíte instrumentada é executada em emulador Android 16 x86_64 com KVM; não equivale ao Samsung A36 ou a notificações bancárias reais. Emulador limitado a 4 GiB/4 CPUs e Gradle de testes a 8 GiB/12 CPUs, totalizando até 12 GiB/16 CPUs. O emulador é encerrado antes do build ARM64, que mantém seu limite próprio de 12 GiB/16 CPUs.

A primeira execução expôs testes com expectativas antigas e a falha real de restauração causada por `direction` não anulável no decoder. Também foi corrigida a integração da nova evidência com o validador financeiro, que antes só admitia valor. A suíte final é registrada nas notas de release. Não solicitar ao usuário outra bateria manual.

Referências: [aceleração do emulador](https://developer.android.com/studio/run/emulator-acceleration) e [limites do NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService). As APIs Android não garantem entrega completa de notificações e não fornecem extratos bancários.
