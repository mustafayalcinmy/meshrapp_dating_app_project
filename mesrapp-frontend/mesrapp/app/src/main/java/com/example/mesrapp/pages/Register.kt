package com.example.mesrapp.pages

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.mesrapp.MainActivity
import com.example.mesrapp.R
import com.example.mesrapp.auth.SpotifyAuth
import com.example.mesrapp.auth.accessToken
import com.example.mesrapp.clients.Client
import com.example.mesrapp.clients.SpotifyApiClient
import com.example.mesrapp.models.UpdateImageUserDto
import com.example.mesrapp.models.User
import com.example.mesrapp.services.SpotifyApiService
import com.example.mesrapp.ui.theme.MesrappTheme
import com.example.mesrapp.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.gson.Gson
import com.spotify.sdk.android.auth.LoginActivity.REQUEST_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.CountDownLatch


val customFontFamily = FontFamily(
    Font(R.font.circular)
)

var check : Boolean = false

class Register : ComponentActivity() {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val spotifyAuth = SpotifyAuth(this)
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var spotifyApiService: SpotifyApiService
    private lateinit var spotifyUserProfile: Map<String, Any>
    private val apiService = Client.apiService

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        okHttpClient = OkHttpClient()
        check = false

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(REQUEST_CODE, result.resultCode, result.data)
        }

        setContent {
            MesrappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterPage() { spotifyAuth.login() }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        spotifyAuth.onActivityResult(requestCode, resultCode, data)
        println(accessToken)
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
                        MotionToast.createToast(this@Register,
                            "Hata! 🚫",
                            "Kullanıcı zaten kayıtlı!",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this@Register,R.font.circular))
                    } else {
                        check = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}



@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun RegisterPage(login: () -> Unit){
    var currentStep by remember { mutableIntStateOf(1) }
    var username by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var location by remember {
        mutableStateOf("")
    }

    fun nextStep() {
        if (currentStep < 3) {
            currentStep++
        }
    }
    val current = LocalContext.current
    fun previousStep() {
        if (currentStep == 1) {
            val intent = Intent(current, MainActivity::class.java)
            current.startActivity(intent)
        }
        if (currentStep > 1) {
            currentStep--
            if (currentStep == 1) {
                check = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepIndicatorWithLine(step = 1, currentStep)
            StepIndicatorWithLine(step = 2, currentStep)
            StepIndicatorWithLine(step = 3, currentStep)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when (currentStep) {
                1 -> Step1Content( {nextStep()}, {login()} )
                2 -> Step2Content(
                    username = username,
                    onUsernameChange = { username = it },
                    birthDate = birthDate,
                    onBirthDateChange = { birthDate = it },
                    gender = gender,
                    onGenderChange = { gender = it },
                    onNext = { nextStep() },
                    location = location,
                    onLocationChange = { location = it}
                )
                3 -> Step3Content(username, birthDate, gender, location)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Start)
                .clickable { previousStep() }
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
fun StepIndicatorWithLine(step: Int, currentStep: Int) {
    val lineColor = remember { getLineColor(step, currentStep) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        StepIndicator(step = step, currentStep, step < currentStep)
        // Sadece 3. adım için çizgi ekleniyor
        if (step < 3) {
            val lineColor = when (step) {
                1 -> if (currentStep >= 2) Color(0xFFFCBAC3) else Color.Gray
                2 -> if (currentStep >= 3) Color(0xFFFCBAC3) else Color.Gray
                else -> Color.Gray
            }
            LineBetweenSteps(lineColor)
        }
    }
}
fun getLineColor(step: Int, currentStep: Int): Color {
    return when (step) {
        1 -> if (currentStep >= 2) Color(0xFFFCBAC3) else Color.Gray
        2 -> if (currentStep >= 3) Color(0xFFFCBAC3) else Color.Gray
        else -> Color.Gray
    }
}

@Composable
fun StepIndicator(step: Int, currentStep: Int, stepCompleted: Boolean) {
    val targetColor = if (step <= currentStep) Color(0xFFFCBAC3) else Color(0xFFD1CFC0)
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1400)
    )
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(animatedColor, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = stepCompleted,
            enter = fadeIn() + scaleIn(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.heart),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        }

        AnimatedVisibility(
            visible = !stepCompleted,
            enter = fadeIn() + scaleIn(),
        ) {
            Text(text = step.toString(), color = Color.White, fontSize = 24.sp)
        }
    }
}


@Composable
fun LineBetweenSteps(lineColor: Color) {
    val animatedLineColor by animateColorAsState(
        targetValue = lineColor,
        animationSpec = tween(durationMillis = 1000)
    )
    DottedLine(
        modifier = Modifier
            .height(2.25.dp)
            .width(95.dp),
        lineColor = animatedLineColor
    )
}


@Composable
fun DottedLine(
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Gray,
    lineWidth: Dp = 3.dp,
    dotRadius: Float = 3f,
    dotGap: Float = 28.4f
) {
    Canvas(modifier = modifier) {
        val startY = size.height / 2
        var startX = 22f
        while (startX < size.width) {
            val endX = (startX + dotRadius).coerceAtMost(size.width)
            drawLine(
                color = lineColor,
                start = Offset(startX, startY),
                end = Offset(endX, startY),
                strokeWidth = lineWidth.toPx(),
                cap = StrokeCap.Round
            )
            startX += (dotRadius * 2) + dotGap
        }
    }
}

@Composable
fun Step1Content(onNext: () -> Unit, login: () -> Unit){
    val ctx = LocalContext.current

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
    }
    Spacer(modifier = Modifier.height(50.dp))


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val coroutineScope = rememberCoroutineScope()
        var canContinue = false
        fun startTokenCheck() {
            coroutineScope.launch {
                val latch = CountDownLatch(60 * 20)
                println(check)
                println(latch.count)
                while (!check && latch.count > 0) {
                    delay(1000)
                    if (check) {
                        canContinue = true
                        onNext()
                        coroutineScope.cancel()
                        break
                    }
                    latch.countDown()
                }
                if (!canContinue) {
                    navigateWithMsg(ctx)
                }
            }

        }
        Button(
            onClick = {
                login()
                startTokenCheck()

            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff22d05d)),
            modifier = Modifier
                .width(300.dp)
                .height(50.dp)

        ) {
            Text(
                text = "Spotify ile Kayıt Ol",
                fontSize = 18.sp,
                color = Color.White,
                fontFamily = customFontFamily
            )
        }
    }
}

fun navigate(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    context.startActivity(intent)
}

fun navigateWithMsg(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("msg", "Spotify ile giriş yapılırken bir hata oluştu. Lütfen tekrar deneyin.")
    context.startActivity(intent)
}

fun getCityAndCountry(context: Context, latitude: Double, longitude: Double): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses?.isNotEmpty() == true) {
            val city = addresses[0].locality ?: ""
            val country = addresses[0].countryName ?: ""
            "$city, $country"
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun Step2Content(
    username: String,
    onUsernameChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    onNext: () -> Unit,
    location: String,
    onLocationChange: (String) -> Unit
) {



    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var formattedDate by remember { mutableStateOf("Doğum Tarihi Seç") }

    var isUsernameValid by remember { mutableStateOf(true) }
    var isBirthDateValid by remember { mutableStateOf(true) }
    var isGenderValid by remember { mutableStateOf(true) }
    var locationText by remember { mutableStateOf("Şehir ve ülke bilgisi alınıyor...") }
    val context = LocalContext.current
    var fusedLocationClient: FusedLocationProviderClient
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var ctx = LocalContext.current




    LaunchedEffect(Unit) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@LaunchedEffect
        }
        fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, object : CancellationToken() {
           override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

           override fun isCancellationRequested() = false
       })
           .addOnSuccessListener { location: Location? ->
               if (location == null)
                   Toast.makeText(context, "Location alınamıyor", Toast.LENGTH_SHORT).show()
               else {
                   val lat = location.latitude
                   val lon = location.longitude
                   println(lat)
                   println(lon)
                   val loc = getCityAndCountry(context, lat, lon)
                   if (loc != null) {
                       onLocationChange(loc)
                       locationText = loc
                   }
               }

           }

    }



    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = locationText, fontSize = 14.sp, color = Color.Gray)

        Card(
        ) {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        if (it.length <= 16)
                            onUsernameChange(it)
                                    },
                    label = { Text("Kullanıcı Adı", fontFamily = customFontFamily) },
                    modifier = Modifier.padding(bottom = 8.dp),
                    colors =  OutlinedTextFieldDefaults.colors(
                        cursorColor = Color.Black,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    ),
                    textStyle = TextStyle(fontFamily = customFontFamily)
                )

            }
        }
        if (username.length > 15) {
            isUsernameValid = false
            Text(
                text = "Kullanıcı adı en fazla 15 karakter olmalıdır",
                color = Color.Red,
                fontFamily = customFontFamily,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            isUsernameValid = true
            Text(
                text = "",
                color = Color.Transparent,
                fontFamily = customFontFamily,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Card(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .height(70.dp)
                .width(280.dp)
                .align(Alignment.CenterHorizontally)
                .clickable {
                    val calendar = selectedDate ?: Calendar.getInstance()
                    DatePickerDialog(
                        ctx,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth)
                            selectedDate = calendar
                            formattedDate = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.US).format(calendar.time)
                            onBirthDateChange(formattedDate)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (formattedDate == "Doğum Tarihi Seç") {
                        Text(
                            text = formattedDate,
                            modifier = Modifier.padding(start = 8.dp),
                            fontFamily = customFontFamily
                        )
                    } else {
                        Text(
                            text = "Doğum Tarihi: $formattedDate",
                            modifier = Modifier.padding(start = 8.dp),
                            fontFamily = customFontFamily
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Card (
                modifier = Modifier
                    .width(280.dp)
                    .height(70.dp)
                    .align(Alignment.CenterHorizontally)

            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 30.dp)
                        .padding(top = 12.dp)
                ) {
                    RadioButton(
                        selected = gender == "Erkek",
                        onClick = { onGenderChange("Erkek") }
                    )
                    Text("Erkek", fontFamily = customFontFamily)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = gender == "Kadin",
                        onClick = { onGenderChange("Kadin") }
                    )
                    Text("Kadin", fontFamily = customFontFamily)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (gender == "") {
                    isGenderValid = false
                } else {
                    isGenderValid = true
                }

                if(birthDate == "Doğum Tarihi Seç" || birthDate == "") {
                    isBirthDateValid = false
                } else {
                    isBirthDateValid = true
                }
                if (username.length > 15 || username.isEmpty()) {
                    isUsernameValid = false
                } else {
                    isUsernameValid = true
                }

                if (!isUsernameValid || !isBirthDateValid || !isGenderValid) {
                    // toast message
                    if (!isUsernameValid) {
                        MotionToast.createToast(ctx as ComponentActivity,
                            "Hata! 🚫",
                            "Kullanıcı adı en fazla 15 karakter olmalıdır ve boş olamaz.",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ctx,R.font.circular))
                    }
                    if (!isBirthDateValid) {
                        MotionToast.createToast(ctx as ComponentActivity,
                            "Hata! 🚫",
                            "Doğum tarihi seçilmelidir.",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ctx,R.font.circular))
                    }
                    if (!isGenderValid) {
                        MotionToast.createToast(ctx as ComponentActivity,
                            "Hata! 🚫",
                            "Cinsiyet seçilmelidir.",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ctx,R.font.circular))
                    }


                    isUsernameValid = true
                    return@Button
                }
                onNext()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff22d05d)),
            modifier = Modifier
                .width(300.dp)
                .height(50.dp)
        ) {
            Text(text = "İleri", fontSize = 18.sp, color = Color.White, fontFamily = customFontFamily)
        }

        Spacer(modifier = Modifier.height(50.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(180.dp),
            contentScale = ContentScale.Crop,
        )
    }
}



@Composable
fun Step3Content(
    username: String,
    birthDate: String,
    gender: String,
    location: String,
) {
    var spotifyUserProfile by remember { mutableStateOf<Map<String, Any>?>(null) }
    var ctx = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }
    val image: ImageBitmap = ImageBitmap.imageResource(R.drawable.logo)
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    val cropImage = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            uri = result.uriContent
            selectedImageUri = uri.toString()
        }
    }

    val okHttpClient = remember { SpotifyApiClient.createOkHttpClient(accessToken!!) }
    val spotifyApiService = remember { SpotifyApiService(okHttpClient) }

    LaunchedEffect(Unit) {
        try {
            val profile = withContext(Dispatchers.IO) {
                spotifyApiService.getCurrentUserProfile("Bearer $accessToken")
            }
            spotifyUserProfile = profile as Map<String, Any>?
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(250.dp)
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .border(2.dp, Color.Black)
                )
            } else {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .border(2.dp, Color.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                uri = null
                cropImage.launch(CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        outputCompressFormat = Bitmap.CompressFormat.JPEG
                    )
                ))
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff22d05d)),
            modifier = Modifier
                .width(300.dp)
                .height(60.dp)
                .padding(top = 16.dp)
        ) {
            Text(text = "Resmini Değiştir", fontSize = 18.sp, color = Color.White, fontFamily = customFontFamily)
        }
        val coroutineScope = rememberCoroutineScope()

        Button(
            onClick = {
                val img = selectedImageUri
                val apiService = Client.apiService
                if (img == null) {
                    MotionToast.createToast(ctx as ComponentActivity,
                        "Hata! 🚫",
                        "Lütfen bir resim seçin.",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(ctx as ComponentActivity,R.font.circular))
                    return@Button
                }
                val imageFile = img?.let { Utils.createTempFileFromUri(Uri.parse(it), ctx) }
                val requestFile = imageFile?.asRequestBody("image/*".toMediaTypeOrNull())

                val fileExtension = ".jpg"
                val imagePart = requestFile?.let {
                    MultipartBody.Part.createFormData("file", imageFile.name.substringBeforeLast(".") + fileExtension, it)
                }


                val image = spotifyUserProfile?.get("id").toString() + fileExtension
                val gson = Gson()


                var user = User(username, image, spotifyUserProfile?.get("id").toString(), spotifyUserProfile?.get("email").toString(), gender = gender, dateTime = birthDate, location = location)
                val userJson = gson.toJson(user)

                val updateImageUserDto = UpdateImageUserDto(image, spotifyUserProfile?.get("id").toString())
                val updateImageUserDtoJson = gson.toJson(updateImageUserDto)
                println(updateImageUserDtoJson)
                coroutineScope.launch {
                    try {
                        val addUserResponse = withContext(Dispatchers.IO) {
                            apiService.addUser(user).execute()
                        }
                        val registrationSuccessful = addUserResponse.isSuccessful
                        println(registrationSuccessful)
                        if (registrationSuccessful) {
                            println(user.image)
                            val updateImageResponse = withContext(Dispatchers.IO) {
                                apiService.updateImage(
                                    user = updateImageUserDto,
                                    file = imagePart!!
                                ).execute()
                            }
                            val imageUploadSuccessful = updateImageResponse.isSuccessful
                            if (imageUploadSuccessful) {
                                MotionToast.createToast(ctx as ComponentActivity,
                                    "Kayıt Başarılı! 🎉",
                                    "MesrApp'e Hoşgeldiniz!",
                                    MotionToastStyle.SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(ctx as ComponentActivity,R.font.circular))
                                navigate(ctx)
                            } else {
                                MotionToast.createToast(ctx as ComponentActivity,
                                    "Hata! 🚫",
                                    "Kayıt başarısız oldu. Lütfen tekrar deneyin.",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(ctx as ComponentActivity,R.font.circular))
                            }
                        } else {
                            MotionToast.createToast(ctx as ComponentActivity,
                                "Hata! 🚫",
                                "Kayıt başarısız oldu. Lütfen tekrar deneyin.",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(ctx as ComponentActivity,R.font.circular))
                        }
                    } catch (e: Exception) {
                        MotionToast.createToast(ctx as ComponentActivity,
                            "Hata! 🚫",
                            "Kayıt başarısız oldu. Lütfen tekrar deneyin.",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(ctx as ComponentActivity,R.font.circular))
                    }
                }



            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff22d05d)),
            modifier = Modifier
                .width(300.dp)
                .height(60.dp)
                .padding(top = 16.dp)
        ) {
            Text(text = "Kaydını Bitir!", fontSize = 18.sp, color = Color.White, fontFamily = customFontFamily)
        }
    }
}
