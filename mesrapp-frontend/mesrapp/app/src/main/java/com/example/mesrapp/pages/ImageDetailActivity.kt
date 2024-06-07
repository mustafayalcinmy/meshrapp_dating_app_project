package com.example.mesrapp.pages

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.compose.rememberAsyncImagePainter
import com.example.mesrapp.ui.theme.MesrappTheme

class ImageDetailActivity : ComponentActivity() {
    var imageUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        imageUrl = intent.getStringExtra("imageUrl")!!
        super.onCreate(savedInstanceState)
        setContent {
            MesrappTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                                .clickable(
                                    onClick = {
                                        downloadImage(this@ImageDetailActivity, imageUrl)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


    fun downloadImage(context: Context, imageUrl: String) {
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Resmi indirmek istiyor musunuz?")
            .setDescription("Resim indiriliyor...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, imageUrl.split("/").last())

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "Resim indiriliyor...", Toast.LENGTH_SHORT).show()
    }

}