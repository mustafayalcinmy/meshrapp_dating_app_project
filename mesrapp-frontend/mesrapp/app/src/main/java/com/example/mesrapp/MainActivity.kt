package com.example.mesrapp

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.mesrapp.observers.AppLifecycleObserver
import com.example.mesrapp.observers.ConnectivityObserver
import com.example.mesrapp.pages.Login
import com.example.mesrapp.pages.Register
import com.example.mesrapp.ui.theme.MesrappTheme
import com.example.mesrapp.utils.Utils
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class MainActivity : ComponentActivity() {
    private val appLifecycleObserver = AppLifecycleObserver()

    private lateinit var connectivityObserver: ConnectivityObserver


    private fun checkLocationPermission() {
        val fineLocationPermissionState =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermissionState =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        val permissionsToRequest = mutableListOf<String>()

        if (fineLocationPermissionState == PackageManager.PERMISSION_DENIED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (coarseLocationPermissionState == PackageManager.PERMISSION_DENIED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this as Activity,
                permissionsToRequest.toTypedArray(),
                codes.LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    object codes {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun createNotificationChannel() {
        val channelId = "MATCHING_SERVICE_CHANNEL"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Matching Service Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for Matching Service notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        createNotificationChannel()
        super.onCreate(savedInstanceState)

        connectivityObserver = ConnectivityObserver(applicationContext)
        connectivityObserver.checkInitialConnection()

        if (intent.hasExtra("msg")) {
            MotionToast.createToast(
                this as Activity,
                "Zaman Aşımı! 🕒",
                "Tekrar kayıt olunuz.",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this as Activity, R.font.circular)
            )
        }
        setContent {
            MesrappTheme {
                val isConnected by connectivityObserver.isConnected.collectAsState()

                if (!isConnected) {
                    ShowNoConnectionDialog()
                } else {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Main(
                            navigateRegister = { navigateToRegister() },
                            checkLocationPermission = { checkLocationPermission() },
                            navigateLogin = { navigateToLogin() })
                    }
                }
            }
        }
    }

    @Composable
    fun ShowNoConnectionDialog() {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Bağlantı Hatası") },
            text = { Text("İnternet bağlantısı yok. Uygulama kapatılacak.") },
            confirmButton = {
                Button(onClick = { (context as MainActivity).finish() }) {
                    Text("Tamam")
                }
            }
        )
    }

    private fun navigateToRegister() {
        Log.d("MainActivity", "Navigate to Login called")
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        Log.d("MainActivity", "Navigate to Login called")
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
}

@Composable
fun Main(navigateRegister: () -> Unit, navigateLogin: () -> Unit, checkLocationPermission: () -> Unit) {
    LaunchedEffect(Unit) {
        checkLocationPermission()
    }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .width(300.dp)
                .padding(bottom = 20.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = { navigateRegister() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22D05D)
            ),
            modifier = Modifier
                .width(300.dp)
                .padding(bottom = 20.dp)
        ) {
            Text("Kayıt Ol", fontFamily = Utils.customFontFamily, fontSize = 20.sp, color = Color.White)
        }
        Button(
            onClick = { navigateLogin() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22D05D)
            ),
            modifier = Modifier
                .width(300.dp)
        ) {
            Text("Giriş Yap", fontFamily = Utils.customFontFamily, fontSize = 20.sp, color = Color.White)
        }
    }
}




