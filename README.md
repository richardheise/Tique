# Tique

**Português** · [English](README.en.md) · [Contribuir](CONTRIBUTING.md) · [Documentação](docs/README.md)

Aplicativo Android livre para acompanhar suas movimentações financeiras no Brasil. **Tique** (Τύχη, *Tyche*) é a deusa grega da fortuna. O antigo nome FFortuna permanece em identificadores internos para preservar atualizações e dados.

<img src="assets/branding/icon.png" alt="Símbolo do Tique: dois quadrados dourados e um losango central sobre preto" width="96" />

**[Baixar Tique 1.1.2](https://github.com/richardheise/Tique/releases/download/v1.1.2/tique-arm64.apk)** · [Novidades e limites](docs/releases/v1.1.2.md). Atualize por cima, sem desinstalar. O app acompanha registros conhecidos; não garante reproduzir seu saldo bancário completo.

## O que funciona no APK

- Análises como tela inicial e Movimentos como segunda aba; gráficos por categoria e comparação mensal.
- Entradas/despesas manuais e revisão individual ou em lote de notificações e extratos CSV/OFX de conta.
- Categorias, tags e regras locais por descrição ou estabelecimento. Um lugar pode ter nomes alternativos e uma tag própria, com categoria opcional.
- Caixinhas com modalidade, posição informada, aportes, resgates, transferências entre caixinhas e ajustes explícitos.
- Cartões, compras, fechamento/vencimento e pagamentos parciais de faturas. Compra é despesa uma vez; pagamento não repete a despesa.
- Anulação auditada, preservando original e motivo; dependências inconsistentes bloqueiam a operação sem gravação parcial.
- Livro nativo SQLCipher, recuperação local automática e backup portátil com senha. Backups financeiros incluem confirmados e configurações, sem pendências ou notificações brutas.
- Captura diária consentida pelo Android, persistida antes do processamento e independente de JavaScript. Sugestões de campos preservam origem e correções humanas.

Pix é meio de pagamento, não receita ou despesa. Notificações podem faltar e os textos podem ser insuficientes: **não há parser Nubank certificado por amostras reais nem lançamento automático novo**. Não há backend, conta externa, telemetria do app, credenciais bancárias ou INTERNET no APK release. Abrir o Nubank é apenas um atalho; não lê telas ou operações privadas.

Parcelamentos, estornos financeiros, despesas compartilhadas e projeções completas permanecem futuros. Caixinhas são posições informadas, sem rentabilidade automática. Ciclos de cartão usam dias 1–28; confira sua fatura. Veja [as notas da versão](docs/releases/v1.1.2.md) e [o glossário](GLOSSARY.md).

## Desenvolver e testar

Use Node 24 (`.nvmrc`). Expo 57, React Native 0.86 e React 19.2 são as versões fixadas. Confira compatibilidade antes de atualizar dependências.

```sh
nvm use
npm ci
npm run check
```

### Demonstração — Expo Go

```sh
npm start -- --lan
```

Computador e celular devem estar na mesma rede; abra o QR no Expo Go compatível com SDK 57. Em SSH, `npm start -- --tunnel` usa um intermediário de desenvolvimento. A demonstração aceita **somente dados fictícios**, em SQLite local sem criptografia. Não captura notificações reais ou importa seus arquivos financeiros. Expo Go não executa o módulo Kotlin personalizado.

### APK Android local

Sem EAS, conta Expo ou serviço pago. Configure JDK/SDK e a assinatura conforme [captura nativa](docs/captura-nativa.md). Preserve chaves e bancos existentes.

```sh
npm run native:build -- --compile-device-tests
```

Saída: `artifacts/tique-arm64.apk`. O script limita a compilação a 12 GiB/16 CPUs. Não execute builds nativos em paralelo.

Com emulador Android x86_64 conectado, os testes instrumentados usam:

```sh
npm run native:test -- --run-device-tests
```

O modo reserva até 8 GiB/12 CPUs ao Gradle e até 4 GiB/4 CPUs ao emulador. Desligue o emulador antes do build ARM64. Expo Go, Metro, downloads e ferramentas de desenvolvimento podem usar rede; isso é separado do funcionamento offline do APK final.

Na 1.1.1, **46 testes TypeScript, 72 JVM e 87 testes Android** passaram. Os testes Android foram executados no emulador Android 16 x86_64; não são validação física no Samsung A36 nem de notificações reais do Nubank. Resultados específicos de cada versão estão nas notas de release. Não confunda testes compilados com executados. Não é exigida uma nova bateria manual do usuário.

## Arquitetura e privacidade

Monólito modular. No APK real, Kotlin possui o livro, as migrações e o executor serial; a interface TypeScript usa Expo Modules API. Uma caixa cifrada de evidências alimenta candidatos revisáveis e um único livro financeiro com lançamentos equilibrados, centavos inteiros seguros, vínculos e comandos idempotentes. Publicação, captura e data efetiva são informações distintas. Valor e dia iguais não provam duplicidade.

| Local | Responsabilidade |
| --- | --- |
| `modules/ffortuna-capture/` | Captura Android, parsing, livro SQLCipher, regras, migrações e recuperação. |
| `src/capture/` | Interface real, revisão, classificações, recursos e análises. |
| `src/core/`, `src/application/`, `src/processing/`, `src/persistence/`, `src/demo/` | Domínio e serviço da demonstração sintética; não sincronizam outro livro real. |
| `app/`, `src/ui/`, `src/legal/` | Navegação, tipografia, componentes e avisos. |

Originais diários têm retenção limitada a 30 dias, limpa quando o núcleo executar. Observações estruturadas e registros confirmados persistem. O diagnóstico antigo é separado, opcional e temporário. Force-stop, reinício, revogação e políticas do Android podem interromper a captura. Bloqueio biométrico não substitui criptografia.

Cópias internas são perdidas ao desinstalar. Para trocar de aparelho, exporte um backup com senha e guarde o arquivo e a senha. **Nunca envie ao GitHub notificações reais, extratos, bancos, backups, chaves ou credenciais.** Use fixtures inventadas e remova informações pessoais de relatos de erro.

## Contribuir e documentação

Issues e contribuições são bem-vindas em português ou inglês. Leia [CONTRIBUTING.md](CONTRIBUTING.md), consulte [a documentação](docs/README.md) e use os [formulários de issue](https://github.com/richardheise/Tique/issues/new/choose). Português é o idioma atual da interface; documentação bilíngue não implica tradução do app.

[Continuidade](docs/continuidade.md) · [Decisões de arquitetura](docs/adr/) · [Dependências](docs/dependencias.md) · [Verificação de licenças](docs/revisao-licencas-apk.md)

## Licença

Código original sob [AGPL-3.0-only](LICENSE). Dependências e fontes preservam suas licenças: [avisos](THIRD_PARTY_NOTICES.md), [inventário do APK](legal/apk-components.json) e [revisão](docs/revisao-licencas-apk.md). Releases incluem APK, fonte correspondente, avisos e checksums. Space Grotesk usa OFL-1.1.

Finan+ e PennyWise são referências conceituais, sem código ou assets incorporados. Não há afiliação com esses projetos ou com o Nubank. Nomes de código e termos de licença originais não são traduzidos.
