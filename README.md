# Detector de presentes do TikTok LIVE

Extração do módulo de detecção de presentes do LiveCraft, branch `feature/gift-combo-per-item`.

## Arquivos

- `android/GiftRuleEngine.kt`: catálogo, armazenamento das regras e associação do presente recebido à ação configurada.
- `android/TikTokNative.kt`: declara a ponte JNI para iniciar/parar o conector.
- `native/jni/src/lib.rs`: conecta à LIVE, recebe eventos TikTok Gift e encaminha nome, imagem, moedas, combo e quantidade para Kotlin.
- `native/jni/Cargo.toml`: dependências do adaptador JNI.

## Combo de presentes

O conector envia `repeat_count` quando o presente é de combo. O callback Kotlin recebe `giftCount`. O motor de regras só libera o combo quando `isStreakOver` é verdadeiro, evitando executar a ação em cada atualização intermediária.

## Integração

Esta é a camada de detecção e identificação de presentes, não o aplicativo completo. O callback JNI original chama `GameBridgeService.onNativeTikTokGift(...)`; ao integrar em outro app, implemente esse callback (mesma assinatura) e conecte-o ao seu executor de ações.

O adaptador Rust depende do crate local `livecraft-tiktok-core` em `../live-rs`. O manifesto do crate está documentado em `native/live-rs-Cargo.toml`; o código-fonte desse cliente TikTok precisa estar disponível nesse caminho para compilar o JNI. Android SDK/NDK e Rust também são necessários para gerar a biblioteca nativa.

Origem: `LUFFYBWX/LiveCraft`, branch `feature/gift-combo-per-item`.