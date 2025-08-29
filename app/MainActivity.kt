package com.example.tcpphoto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var imageView: ImageView
    private lateinit var editIp: EditText
    private lateinit var editPort: EditText
    private var currentBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        editIp = findViewById(R.id.editIp)
        editPort = findViewById(R.id.editPort)

        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        btnTakePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            currentBitmap = imageBitmap

            // mostra no ImageView
            imageView.setImageBitmap(imageBitmap)

            // envia para o servidor
            sendPhoto(imageBitmap)
        }
    }

    private fun sendPhoto(bitmap: Bitmap) {
        Thread {
            try {
                val stream = ByteArrayOutputStream()
                // Reduz qualidade/tamanho
                val resized = Bitmap.createScaledBitmap(bitmap, 187,
                    (bitmap.height * 187.0 / bitmap.width).toInt(), true)
                resized.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val bytes = stream.toByteArray()

                val ip = editIp.text.toString()
                val port = editPort.text.toString().toInt()

                val socket = Socket(ip, port)
                val output: OutputStream = socket.getOutputStream()

                // envia tamanho (4 bytes) + foto
                val sizeBuffer = ByteBuffer.allocate(4).putInt(bytes.size).array()
                output.write(sizeBuffer)
                output.write(bytes)
                output.flush()

                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
