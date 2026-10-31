# Contribuir com o Tique

**Português** · [English](CONTRIBUTING.en.md)

Contribuições, dúvidas e relatos de erro são bem-vindos em português ou inglês.

## Relatar um problema

Pesquise [issues existentes](https://github.com/richardheise/Tique/issues) e consulte o [README](README.md) e as notas da sua versão. Abra o [formulário](https://github.com/richardheise/Tique/issues/new/choose), informe versão do Tique, modelo/Android e descreva o resultado esperado e o observado. Atualizar é opcional se a atualização for justamente o problema.

Use exemplos inventados. **Não envie notificações reais, extratos, bancos, backups, nomes de pessoas, números de conta, chaves, senhas ou tokens.** Capturas de tela devem esconder dados pessoais e financeiros. Logs são opcionais: envie apenas trechos técnicos revisados, sem conteúdo financeiro. Nunca apague seus dados para tentar reproduzir um erro.

Os checkboxes seguem a ideia de leitura atenta do [template do end-4](https://github.com/end-4/dots-hyprland/blob/main/.github/ISSUE_TEMPLATE/1-issue.yml), com texto próprio adaptado ao Tique. A opção “marquei sem ler” é opcional; não há bot fechando issues automaticamente por ela.

## Preparar o ambiente

Leia o [README](README.md), as [decisões](docs/adr/) e a [continuidade](docs/continuidade.md). Use Node 24, respeite o lockfile e preserve alterações existentes.

```sh
nvm use
npm ci
npm run check
```

Expo Go é uma demonstração com dados exclusivamente sintéticos. O modo real requer APK próprio, Kotlin/SQLCipher e toolchain local conforme [captura nativa](docs/captura-nativa.md). Não exige EAS, conta Expo ou serviço pago. Não rode builds nativos em paralelo; o script limita recursos. Não substitua chaves de assinatura existentes.

## Propor uma alteração

Para alterações grandes, explique o problema e o comportamento desejado numa issue antes de ampliar o escopo. Faça mudanças pequenas, com validação adequada ao comportamento. Um pull request deve explicar o problema, o resultado, verificações executadas e limitações conhecidas. Diferencie testes escritos, compilados e executados; não declare validação física a partir do emulador.

Preserve estas invariantes:

- Um livro financeiro real, local e offline; sem backend obrigatório, telemetria, credenciais bancárias ou APIs privadas.
- Centavos inteiros seguros, operações atômicas, IDs e comandos idempotentes, evidências e correções humanas preservadas.
- Transferências, aportes/resgates e pagamento de fatura não inflam renda ou consumo. Compra no cartão é despesa uma vez.
- Notificação não é extrato completo. Data de publicação não comprova data efetiva. Valor/data iguais não comprovam duplicidade.
- Dados reais só no armazenamento protegido do app, nunca em fixtures, logs, temporários em claro ou Git.
- APK release sem INTERNET; acesso de ferramentas de desenvolvimento é separado.

Não use `npm audit fix --force` ou atualize SDK por conveniência. Confira compatibilidade oficial, lockfile e inventário de licenças quando mudar dependências.

## Documentação e imagens

Mantenha as versões em português e inglês equivalentes. Originais ficam na raiz e em `docs/`; versões inglesas usam `.en.md` na raiz e `docs/en/`. Preserve comandos, identificadores, hashes, nomes de versões e fatos históricos. Notas antigas descrevem sua própria versão; não transforme teste compilado em executado ao traduzir.

O símbolo é o mesmo do cabeçalho. Para recriar os SVGs e PNGs com a geometria do símbolo, instale `rsvg-convert` como ferramenta local de desenvolvimento e execute:

```sh
node scripts/generate-app-icons.mjs
```

Não adicione marcas, assets ou código de terceiros sem verificar licença. Os textos originais de licenças e avisos devem ser preservados; traduções explicativas não os substituem.

## Licença e releases

Contribuições são distribuídas sob [AGPL-3.0-only](LICENSE); mantenha avisos de terceiros. Não é exigido CLA adicional. APK, fonte correspondente, avisos e checksums devem acompanhar releases. Tags e anexos publicados são imutáveis; correções usam nova versão/code, preservando pacote, certificado e migrações. Nunca envie material privado de assinatura ao repositório.
