package com.example.tcpphoto

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.net.Socket
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var serverIp: String = "0.0.0.0"  // valor padrão
    private var serverPort: Int = 5000             // valor padrão

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editIp = findViewById<EditText>(R.id.editIp)
        val editPort = findViewById<EditText>(R.id.editPort)
        val btn = findViewById<Button>(R.id.btnCaptureSend)

        // Registrar launcher da câmera
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val extras = result.data?.extras
                val bmp = extras?.get("data") as? Bitmap
                if (bmp != null) {
                    // Redimensionar imagem (se quiser)
                    val resized = resizeToMaxWidth(bmp, 800)
                    // Enviar para servidor
                    sendImageToServer(resized, serverIp, serverPort)
                }
            }
        }

        // Botão captura e envia
        btn.setOnClickListener {
            val ipText = editIp.text.toString().trim()
            val portText = editPort.text.toString().trim()

            if (ipText.isNotEmpty()) serverIp = ipText
            if (portText.isNotEmpty()) serverPort = portText.toIntOrNull() ?: 5000

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }

    // Função para redimensionar imagem antes de enviar
    private fun resizeToMaxWidth(bmp: Bitmap, maxWidth: Int): Bitmap {
        if (bmp.width <= maxWidth) return bmp
        val scale = maxWidth.toFloat() / bmp.width.toFloat()
        val newH = (bmp.height * scale).toInt()
        val matrix = Matrix().apply { postScale(scale, scale) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }

    // Função que envia a imagem via socket TCP
    private fun sendImageToServer(bmp: Bitmap, ip: String, port: Int) {
        Thread {
            try {
                val socket = Socket(ip, port)
                val output = socket.getOutputStream()

                val baos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val data = baos.toByteArray()

                // Envia tamanho (4 bytes) + dados
                val sizeBuf = ByteBuffer.allocate(4).putInt(data.size).array()
                output.write(sizeBuf)
                output.write(data)
                output.flush()

                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
