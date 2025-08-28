1.Rode o servidor:

python server.py


2.Gerando o APK do App Android:

Abra o projeto Android no Android Studio.

Configure o IP do servidor no app (private val SERVER_HOST = "Seu IP").

Vá em Build → Build Bundle(s) / APK(s) → Build APK(s).

Encontre o APK em:

<projeto>/app/build/outputs/apk/debug/app-debug.apk

Transfira o APK para o celular ou instale direto com Android Studio.


3.Usar app

No app, tocar “Tirar e Enviar” → tirar a foto e confirmar.

O servidor recebe, salva (em received/foto_YYYYMMDD_HHMMSS.jpg) e mostra a foto.

Tirar outra foto → a janela atualiza com a nova imagem (e salva novamente).
