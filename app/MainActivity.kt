package com.example.tcpphoto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private val SERVER_HOST = "0.0.0.0"
    private val SERVER_PORT = 5000

    private lateinit var preview: ImageView
    private lateinit var statusText: TextView

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bmp = result.data?.extras?.get("data") as? Bitmap
            if (bmp != null) {
                val resized = resizeToMaxWidth(bmp, 1280)
                val bos = ByteArrayOutputStream()
                resized.compress(Bitmap.CompressFormat.JPEG, 80, bos)
                val jpegBytes = bos.toByteArray()

                preview.setImageBitmap(resized)
                statusText.text = "Enviando (${jpegBytes.size} bytes)..."

                Thread {
                    try {
                        Socket(SERVER_HOST, SERVER_PORT).use { sock ->
                            val out = sock.getOutputStream()
                            val header = ByteBuffer.allocate(4)
                                .order(ByteOrder.BIG_ENDIAN)
                                .putInt(jpegBytes.size)
                                .array()
                            out.write(header)
                            out.write(jpegBytes)
                            out.flush()
                        }
                        runOnUiThread { statusText.text = "Enviado!" }
                    } catch (e: Exception) {
                        runOnUiThread { statusText.text = "Erro: ${e.message}" }
                    }
                }.start()
            }
        } else {
            statusText.text = "Captura cancelada."
        }
    }

    private fun resizeToMaxWidth(bmp: Bitmap, maxWidth: Int): Bitmap {
        if (bmp.width <= maxWidth) return bmp
        val scale = maxWidth.toFloat() / bmp.width.toFloat()
        val newH = (bmp.height * scale).toInt()
        val matrix = Matrix().apply { postScale(scale, scale) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.previewImage)
        statusText = findViewById(R.id.statusText)

        val btn = findViewById<Button>(R.id.btnCaptureSend)
        btn.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }
}