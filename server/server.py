import socket
import struct
import os
import datetime
import cv2
import numpy as np

# 0.0.0.0 = escuta em todas as interfaces de rede
HOST = '0.0.0.0'
PORT = 5000
SAVE_DIR = 'received'
WINDOW_NAME = 'Servidor - Aguardando foto...'

os.makedirs(SAVE_DIR, exist_ok=True)

def recv_exact(conn, n):
    data = b''
    while len(data) < n:
        chunk = conn.recv(n - len(data))
        if not chunk:
            raise ConnectionError("Conexão encerrada antes de receber tudo.")
        data += chunk
    return data

def show_waiting():
    # Imagem preta com "Aguardando foto..."
    img = np.zeros((360, 480, 3), dtype=np.uint8)
    cv2.putText(img, 'Aguardando foto...', (20, 190),
                cv2.FONT_HERSHEY_SIMPLEX, 0.9, (255, 255, 255), 2, cv2.LINE_AA)
    cv2.imshow(WINDOW_NAME, img)
    cv2.waitKey(1)

def main():
    # Mostra IPs do PC para facilitar a conexão do celular
    import socket as sk
    hostname = sk.gethostname()
    try:
        ip_address = sk.gethostbyname(hostname)
        print(f"IP do PC na rede: {ip_address}")
    except:
        print("Não foi possível determinar o IP automaticamente.")

    cv2.namedWindow(WINDOW_NAME, cv2.WINDOW_AUTOSIZE)
    show_waiting()

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))
        s.listen(1)
        print(f"Servidor ouvindo na porta {PORT} (todas interfaces)")

        while True:
            try:
                conn, addr = s.accept()
                with conn:
                    print('Conexão de', addr)
                    # 1) lê 4 bytes (tamanho) em big-endian
                    header = recv_exact(conn, 4)
                    (size,) = struct.unpack('!I', header)
                    # 2) lê a imagem
                    img_bytes = recv_exact(conn, size)

                    # Decodifica com OpenCV
                    arr = np.frombuffer(img_bytes, dtype=np.uint8)
                    img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
                    if img is None:
                        print("Falha ao decodificar JPEG.")
                        show_waiting()
                        continue

                    # Salva com timestamp
                    ts = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
                    path = os.path.join(SAVE_DIR, f'foto_{ts}.jpg')
                    cv2.imwrite(path, img)
                    print(f"Salvo: {path}")

                    # Mostra a foto
                    cv2.imshow(WINDOW_NAME, img)
                    cv2.waitKey(1)
            except KeyboardInterrupt:
                print("\nEncerrando servidor...")
                break
            except Exception as e:
                print("Erro:", e)
                show_waiting()

    cv2.destroyAllWindows()

if __name__ == '__main__':
    main()
