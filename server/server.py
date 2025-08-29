import socket
import select
import struct
import os
import datetime
import cv2
import numpy as np

HOST = '0.0.0.0'
PORT = 5000
SAVE_DIR = 'received'
WINDOW_NAME = 'Servidor - Fotos Recebidas'

os.makedirs(SAVE_DIR, exist_ok=True)

def recv_exact(conn, n):
    data = b''
    while len(data) < n:
        chunk = conn.recv(n - len(data))
        if not chunk:
            raise ConnectionError("Conexão encerrada antes de receber tudo.")
        data += chunk
    return data

def make_waiting_image():
    img = np.zeros((360, 480, 3), dtype=np.uint8)
    cv2.putText(img, 'Aguardando foto...', (20, 190),
                cv2.FONT_HERSHEY_SIMPLEX, 0.9, (255, 255, 255), 2, cv2.LINE_AA)
    return img

def main():
    import socket as sk
    hostname = sk.gethostname()
    try:
        ip_address = sk.gethostbyname(hostname)
        print(f"IP do PC na rede: {ip_address}")
    except:
        print("Não foi possível determinar o IP automaticamente.")

    cv2.namedWindow(WINDOW_NAME, cv2.WINDOW_AUTOSIZE)

    # começa com tela de espera
    last_img = make_waiting_image()

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))
        s.listen(1)
        print(f"Servidor ouvindo na porta {PORT} (todas interfaces)")

        while True:
            try:
                # atualiza a tela SEMPRE (última imagem conhecida)
                cv2.imshow(WINDOW_NAME, last_img)
                if cv2.waitKey(30) & 0xFF == 27:  # ESC sai
                    print("Encerrando servidor...")
                    break

                # espera conexões, mas com timeout
                ready, _, _ = select.select([s], [], [], 0.1)
                if not ready:
                    continue

                conn, addr = s.accept()
                with conn:
                    print('Conexão de', addr)

                    header = recv_exact(conn, 4)
                    (size,) = struct.unpack('!I', header)
                    img_bytes = recv_exact(conn, size)

                    arr = np.frombuffer(img_bytes, dtype=np.uint8)
                    img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
                    if img is None:
                        print("Falha ao decodificar JPEG.")
                        continue

                    ts = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
                    path = os.path.join(SAVE_DIR, f'foto_{ts}.jpg')
                    cv2.imwrite(path, img)
                    print(f"Salvo: {path}")

                    # atualiza última imagem recebida
                    last_img = img

            except KeyboardInterrupt:
                print("\nEncerrando servidor (Ctrl+C)...")
                break
            except Exception as e:
                print("Erro:", e)

    cv2.destroyAllWindows()

if __name__ == '__main__':
    main()
