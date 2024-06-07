package com.example.mesrapp.pages


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.mesrapp.R
import com.example.mesrapp.auth.SpotifyAuth
import com.example.mesrapp.auth.accessToken
import com.example.mesrapp.clients.Client
import com.example.mesrapp.clients.SpotifyApiClient
import com.example.mesrapp.services.ApiService
import com.example.mesrapp.services.SpotifyApiService
import com.example.mesrapp.ui.theme.MesrappTheme
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

lateinit var spotifyUserProfile: Map<String, Any>
fun isSpotifyUserInitialized() = ::spotifyUserProfile.isInitialized

class Login : ComponentActivity() {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val spotifyAuth = SpotifyAuth(this)
    private lateinit var apiService: ApiService
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var spotifyApiService: SpotifyApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        okHttpClient = OkHttpClient()
        apiService = Client.apiService
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(REQUEST_CODE, result.resultCode, result.data)
        }

        setContent {
            MesrappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginPage({ spotifyAuth.login() }, { navigateToMainPage(this) }, apiService)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        spotifyAuth.onActivityResult(requestCode, resultCode, data)
        if (!accessToken.isNullOrEmpty()) {
            okHttpClient = SpotifyApiClient.createOkHttpClient(accessToken!!)
            spotifyApiService = SpotifyApiService(okHttpClient)
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val profile = withContext(Dispatchers.IO) {
                        spotifyApiService.getCurrentUserProfile("Bearer $accessToken")
                    }
                    spotifyUserProfile = (profile as Map<String, Any>?)!!
                    val response = withContext(Dispatchers.IO) {
                        apiService.checkSpotifyId(spotifyUserProfile["id"].toString()).execute()
                    }
                    val checkId = response.body()?.string()
                    if (checkId.toString() == "true") {
                        navigateToMainPage(this@Login)
                    } else {
                        MotionToast.createToast(this@Login,
                            "Hata! 🚫",
                            "Kullanıcı bulunamadı!",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this@Login,R.font.circular))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    fun navigateToMainPage(context: Context) {
        val intent = Intent(context, MainPage::class.java)
        context.startActivity(intent)
    }

}



@Composable
fun LoginPage(login: () -> Unit, navigateToMainPage: () -> Unit, apiService: ApiService) {
    val current = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Step content
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            // Spacer for consistent top spacing
            Spacer(modifier = Modifier.height(16.dp))

            Step4Content (
                login = login,
                navigateToMainPage = { navigateToMainPage() },
                apiService = apiService,
                ctx = current
            )

        }

        Box(
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { navigate(current) }
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color = Color(0xff22d05d), shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back2),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(60.dp)
                )
            }
        }
    }
}



@Composable
fun Step4Content(login: () -> Unit, navigateToMainPage: () -> Unit, apiService: ApiService, ctx: Context) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(180.dp),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(50.dp))

        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    login()

                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff22d05d)),
                modifier = Modifier
                    .width(300.dp)
            ) {
                Text(
                    text = "Spotify ile Giriş Yap",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = customFontFamily
                )
            }
        }
    }
