# World Tower Wars

Primeiro protótipo Android do jogo de live interativa.

## O que já está preparado

- Tela de configuração para salvar o @ do TikTok.
- Botões separados para conectar à LIVE e iniciar o jogo.
- Arena 2D horizontal com tema natalino.
- Dez torres retangulares, uma por país.
- Cada presente reconhecido adiciona blocos à torre correspondente.
- A cada 100 pontos, a torre zera e o país recebe +1 WIN.
- Pontuação e WINS salvos localmente no Android.
- Imagens reais dos dez presentes carregadas pelos arquivos de imagem do catálogo BeetGames.

## Países e presentes

| País | Presente |
| --- | --- |
| Brasil | Rose |
| Estados Unidos | GG |
| Japão | Ice Cream Cone |
| Alemanha | Cake Slice |
| França | Freestyle |
| Reino Unido | Glow Stick |
| México | Pop |
| Canadá | Starfish |
| Itália | TikTok |
| Espanha | Wink wink |

Os ícones são carregados de `https://beetgames.com/images/gifts/`. O catálogo brasileiro consultado lista esses presentes por 1 moeda. A disponibilidade e os preços podem mudar, então confirme no próprio TikTok antes de usar em uma LIVE.

## Estado da integração TikTok

Os arquivos extraídos do detector permanecem em `android/` e `native/`. O app inclui a ponte Kotlin para receber os callbacks JNI, mas o APK ainda não contém a biblioteca nativa `liblivecraft_tiktok.so` nem o crate `livecraft-tiktok-core` em `native/live-rs`. Portanto, a interface e o jogo podem ser abertos, mas a conexão real com a LIVE ainda depende de completar essa integração.

## Build

O workflow GitHub Actions `.github/workflows/android-build.yml` gera o APK debug em cada push para esta branch. Também é possível compilar localmente com JDK 17 e Gradle 8.9 usando:

```bash
gradle assembleDebug
```
