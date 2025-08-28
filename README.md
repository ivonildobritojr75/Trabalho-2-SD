1.Rode o servidor:

python server.py

2.Gerando o APK do App Android:

Abra o projeto Android no Android Studio.

Configure o IP do servidor no App (private val SERVER_HOST = "Seu IP").

Vá em Build → Build Bundle(s) / APK(s) → Build APK(s).

Encontre o APK em:

<projeto>/app/build/outputs/apk/debug/app-debug.apk

Transfira o APK para o celular ou instale direto com Android Studio.

3.Usar App

No App, tocar “Tirar e Enviar” → tirar a foto e confirmar.

O servidor recebe, salva (em received/foto_YYYYMMDD_HHMMSS.jpg) e mostra a foto.

Tirar outra foto → a janela atualiza com a nova imagem (e salva novamente).

Server Print:
<img width="1920" height="1080" alt="Image" src="https://github.com/user-attachments/assets/28f9979c-1d2a-4dc8-a5c4-633586c5148a" />

App Print:

![Image](https://github.com/user-attachments/assets/b0c1da83-2600-4f13-9831-a3eddefe505d)
