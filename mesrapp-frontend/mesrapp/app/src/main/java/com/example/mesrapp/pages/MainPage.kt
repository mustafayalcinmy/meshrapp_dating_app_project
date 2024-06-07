package com.example.mesrapp.pages

import android.Manifest
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import coil.compose.rememberAsyncImagePainter
import com.example.mesrapp.MainActivity
import com.example.mesrapp.R
import com.example.mesrapp.appDatabase.AppDatabase
import com.example.mesrapp.auth.accessToken
import com.example.mesrapp.clients.Client
import com.example.mesrapp.clients.SpotifyApiClient
import com.example.mesrapp.models.AcceptedUser
import com.example.mesrapp.models.IdMatch
import com.example.mesrapp.models.Match
import com.example.mesrapp.models.User
import com.example.mesrapp.services.ApiService
import com.example.mesrapp.services.GeminiApi
import com.example.mesrapp.services.MatchingService
import com.example.mesrapp.services.SpotifyApiService
import com.example.mesrapp.services.SpotifyListeningService
import com.example.mesrapp.ui.theme.MesrappTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainPage : ComponentActivity() {

    private val currentlyPlayingTrackState: MutableState<String?> = mutableStateOf(null)
    private var mostListenedSongs: MutableState<List<Song>> = mutableStateOf(emptyList<Song>())

    private val user: MutableState<User> = mutableStateOf(User("", "", "", "", ""))
    private val matchesBy: MutableState<List<IdMatch>> = mutableStateOf(emptyList())
    private val matchesByTracks: MutableState<List<TrackInfo>> = mutableStateOf(emptyList())
    private val acceptBy: MutableState<List<AcceptedUser>> = mutableStateOf(emptyList())
    private val matchesByUsers: MutableState<List<User>> = mutableStateOf(emptyList())
    private val acceptedByUsers: MutableState<List<User>> = mutableStateOf(emptyList())
    private var apiService = Client.apiService
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var spotifyApiService: SpotifyApiService
    val geminiApi = GeminiApi()
    private val createdAboutMe: MutableState<String> = mutableStateOf("")


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "CURRENTLY_PLAYING_TRACK") {
                val responseBody = intent.getStringExtra("response_body")
                currentlyPlayingTrackState.value = responseBody
                onCurrentlyPlayingTrackStateChange()
            }
            else if (intent?.action == "ACCEPTING_SERVICE_RESPONSE") {
                val responseBody = intent.getStringExtra("response_body_accepting")!!
                println(responseBody)
                val regex = Regex("""AcceptedUser\(id=(\d+), spotiId=([^,]+), acceptedSpotiIds=([^,]*), acceptedMusicId=([^,]*), acceptedArtistId=([^,]*)\)""")

                val idMatchStrings = regex.findAll(responseBody).map {
                    val (id, spotiId, acceptedSpotiIds, acceptedMusicId, acceptedArtistId) = it.destructured

                    // Create AcceptedUser instance, allowing empty values
                    AcceptedUser(
                        spotiId = spotiId,
                        acceptedSpotiIds = if (acceptedSpotiIds.isEmpty()) "" else acceptedSpotiIds,
                        acceptedMusicId = if (acceptedMusicId.isEmpty()) "" else acceptedMusicId,
                        acceptedArtistId = if (acceptedArtistId.isEmpty()) "" else acceptedArtistId,
                        id = id
                    )
                }.toList()
                if (acceptBy.value != idMatchStrings) {
                    acceptBy.value = idMatchStrings
                    getAcceptedUsers(idMatchStrings)
                }
            }

            else if (intent?.action == "MATCHING_SERVICE_RESPONSE") {
                val responseBody = intent.getStringExtra("response_body_matching")!!
                println(responseBody)
                val regex = Regex("""IdMatch\(id=(\w+),\s*spotiId=([^,]+),\s*matchedSpotiId=([^,]+),\s*matchedMusicId=([^,]+),\s*matchedArtistId=([^,]+)\)""")
                val idMatchStrings = regex.findAll(responseBody).map {
                    val (id, spotiId, matchedSpotiId, matchedMusicId, matchedArtistId) = it.destructured
                    IdMatch(id, spotiId, matchedSpotiId, matchedMusicId, matchedArtistId)
                }.toList()
                matchesBy.value = idMatchStrings
                val spotiIds = matchesBy.value.map { it.matchedSpotiId }
                getMatchedUsers(spotiIds)

                println(matchesBy.value)

            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun getUser(isUpdated: Boolean = false) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(this@MainPage)
                val userDao = database.userDao()
                if (isUpdated) {
                    val response = apiService.getUser(spotifyUserProfile["id"].toString()).execute()
                    user.value = response.body()!!
                    val localUser: User = userDao.getUserById(spotifyUserProfile["id"].toString())
                    if (localUser == null || localUser.toString() == "null") {
                        userDao.insert(user.value)
                    } else {
                        userDao.update(user.value)
                    }
                } else {
                    val localUser = userDao.getUserById(spotifyUserProfile["id"].toString())
                    if (localUser != null && localUser.toString() != "null") {
                        user.value = localUser
                    } else {
                        val response = apiService.getUser(spotifyUserProfile["id"].toString()).execute()
                        user.value = response.body()!!
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onClickAccepteds() {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isAcceptedUser = apiService.getAcceptedMatches(spotifyUserProfile["id"].toString())
                    val res =  isAcceptedUser.execute().body().toString()
                    val responseBody = res
                    val regex = Regex("""AcceptedUser\(id=(\d+), spotiId=([^,]+), acceptedSpotiIds=([^,]+), acceptedMusicId=([^,]+), acceptedArtistId=([^,]+)\)""")
                    val idMatchStrings = regex.findAll(responseBody).map {
                        val (id, spotiId, acceptedSpotiIds,acceptedMusicId, acceptedArtistId) = it.destructured
                        AcceptedUser(id, spotiId, acceptedSpotiIds, acceptedMusicId, acceptedArtistId)
                    }.toList()
                    acceptBy.value = idMatchStrings
                    getAcceptedUsers(acceptBy.value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getUsers(users: List<User>) {
        var usersList: MutableList<User> = mutableListOf()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                for (user in users) {
                    val response = apiService.getUser(user.spotiId).execute()
                    if (response.isSuccessful) {
                        usersList.add(response.body()!!)
                    }
                    matchesByUsers.value = usersList
                }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getAcceptedUsers(users: List<AcceptedUser>) {
        var usersList: MutableList<User> = mutableListOf()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                for (user in users) {
                    val response = apiService.getUser(user.acceptedSpotiIds).execute()
                    if (response.isSuccessful) {
                        usersList.add(response.body()!!)
                    }
                    acceptedByUsers.value = usersList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun generateBio(aboutMe: String) {
        GlobalScope.launch {
            try {
                val generatedText = geminiApi.generateText(aboutMe)
                println(generatedText)
                withContext(Dispatchers.Main) {
                    if (generatedText != null) {
                        createdAboutMe.value = generatedText
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun acceptUser(spotiId: String, acceptedSpotiId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val accept = apiService.acceptUser(spotiId, acceptedSpotiId).execute()
                if (accept.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        MotionToast.createToast(
                            this@MainPage,
                            "Kullanıcı kabul edildi! 🎉",
                            "Artık takılabilirsiniz!",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this@MainPage, R.font.circular)
                        )
                    }
                    val deleteUser = apiService.deleteMatchedUsers(acceptedSpotiId, spotiId).execute()

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteUser(spotiId: String, acceptedSpotiId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val deleteUser = apiService.deleteMatchedUsers(acceptedSpotiId, spotiId).execute()
                println(deleteUser.isSuccessful)
                println(deleteUser.body())

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun deleteUserDb(spotiId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val deleteUser = apiService.deleteUser(spotiId).execute()
                println(deleteUser.isSuccessful)
                println(deleteUser.body())
                if (deleteUser.isSuccessful) {
                    val database = AppDatabase.getDatabase(this@MainPage)
                    val userDao = database.userDao()
                    userDao.deleteUserById(spotiId)

                    val intent = Intent(this@MainPage, MainActivity::class.java)
                    startActivity(intent)

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val serviceIntent = Intent(this, SpotifyListeningService::class.java)
        serviceIntent.putExtra("access_token", accessToken)
        this.startService(serviceIntent)

        val filter = IntentFilter()
        filter.addAction("MATCHING_SERVICE_RESPONSE")
        filter.addAction("ACCEPTING_SERVICE_RESPONSE")
        filter.addAction("CURRENTLY_PLAYING_TRACK")
        this.registerReceiver(receiver, filter, RECEIVER_EXPORTED)




        okHttpClient = SpotifyApiClient.createOkHttpClient(accessToken!!)
        spotifyApiService = SpotifyApiService(okHttpClient)

        getUserTopSongs()
        getUser()


        val matchingService = Intent(this, MatchingService::class.java)
        this.startService(matchingService)

        super.onCreate(savedInstanceState)
        checkNotificationPermission()
        MotionToast.createToast(
            this@MainPage,
            "Giriş başarılı! 🎉",
            "MesrApp'e Hoşgeldiniz!",
            MotionToastStyle.SUCCESS,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this@MainPage, R.font.circular)
        )

        setContent {
            MesrappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background Image
                        Image(
                            painter = painterResource(id = R.drawable.background_image),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Main Content
                        MainContent(currentlyPlayingTrackState, apiService, mostListenedSongs, user, acceptBy= acceptBy, matchesBy =  matchesBy, generateBio = { aboutMe -> generateBio(aboutMe) } , createdBio = createdAboutMe,  matchesByUsers = matchesByUsers,  updateUser = updateUser, deleteUser ={ spotiId1, spotiId2 -> deleteUser(spotiId1, spotiId2) } , acceptUser = { spotiId1, spotiId2 -> acceptUser(spotiId1, spotiId2) } , deleteUserDb = { id -> deleteUserDb(id) }, getUserTopTracks = { getUserTopSongs() }
                        , matchesByTracks = matchesByTracks, acceptedByUsers = acceptedByUsers, onClickAccepteds = { onClickAccepteds() }
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        // Geri tuşuna basıldığında uygulamadan çıkış yapılmasını engellemek için boş bırakıldı.
    }

    override fun onDestroy() {
        this.unregisterReceiver(receiver)
        val spotifyServiceIntent = Intent(this, SpotifyListeningService::class.java)
        val matchingServiceIntent = Intent(this, MatchingService::class.java)
        this.stopService(spotifyServiceIntent)
        this.stopService(matchingServiceIntent)

        super.onDestroy()
    }

    fun onCurrentlyPlayingTrackStateChange() {
        val detailedTrackInfo = getDetailedTrackInfo(currentlyPlayingTrackState.value!!)
        sendListeningData(detailedTrackInfo, apiService)
    }

    private fun checkNotificationPermission() {
        val permissionState =
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        if (permissionState == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }



    val updateUser: (String, User) -> Unit = { spotiId, user ->
        GlobalScope.launch {
            try {
                val response = apiService.updateUser(spotiId, user).execute()
                if (response.isSuccessful) {
                    println("User updated successfully!")
                    getUser()
                    withContext(Dispatchers.Main) {
                        MotionToast.createToast(
                            this@MainPage,
                            "Güncelleme Başarılı! 🎉",
                            "Bilgileriniz Güncellendi!!",
                            MotionToastStyle.SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this@MainPage, R.font.circular)
                        )
                    }
                } else {
                    println("Failed to update user: ${response.errorBody()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getMatchedUsers(spotiIds: List<String>) {
        GlobalScope.launch {
            try {
                val users = mutableListOf<User>()
                for (spotiId in spotiIds) {
                    val response = apiService.getUser(spotiId).execute()
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            users.add(user)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    matchesByUsers.value = users
                    getTracks(matchesBy.value.map { it.matchedMusicId })
                    getUsers(users)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getUserTopSongs() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val songs = accessToken?.let { spotifyApiService.getCurrentUserTopTracks(it) }
                mostListenedSongs.value = songs?.let {
                    val songList = it["items"] as List<Map<*, *>>
                    songList.map { song ->
                        val songName = song["name"] as String
                        val artistName = (song["artists"] as List<Map<*, *>>)[0]["name"] as String
                        val imageUrl = (song["album"] as Map<*, *>)["images"] as List<Map<*, *>>
                        Song(songName, artistName, imageUrl[0]["url"] as String)
                    }
                } ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getTracks(trackIds: List<String>) {
        var tracks = mutableListOf<TrackInfo>()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                for (trackId in trackIds) {
                    val track = accessToken?.let { spotifyApiService.getTrack(trackId, it) }
                    println(track.toString())
                    val trackInfo = track?.let { getTrackInfoById(it) }
                    if (trackInfo != null) {
                        tracks.add(trackInfo)
                    }
                }
                matchesByTracks.value = tracks
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}





enum class ContentType {
    MAIN, OTHER1, OTHER2, PROFILE, EDIT
}


private data class DetailedTrackInfo(val songId : String, val artistId: String)

private fun sendListeningData(detailedTrackInfo: DetailedTrackInfo, apiService: ApiService) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val match = Match(
                spotifyUserProfile?.get("id").toString(),
                detailedTrackInfo.songId,
                detailedTrackInfo.artistId
            )
            apiService.addMatch(match).execute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}




private fun parseTrackInfo(trackInfo: String): TrackInfo {
    if (trackInfo.isEmpty() || trackInfo == "null") {
        return TrackInfo("", "", "", "")
    }
    val gson = Gson()
    val trackMap = gson.fromJson(trackInfo, Map::class.java)
    val isPlaying = trackMap["is_playing"] as? Boolean ?: false
    if (!isPlaying) {
        return TrackInfo("", "", "", "")
    }
    val songName = trackMap["item"]?.let { (it as Map<*, *>)["name"] as? String } ?: ""
    val artistName = trackMap["item"]?.let { ((it as Map<*, *>)["artists"] as? List<*>)?.get(0)?.let { artist -> (artist as Map<*, *>)["name"] as? String } } ?: ""
    val imageUrl = trackMap["item"]?.let { ((it as Map<*, *>)["album"] as? Map<*, *>)?.let { album -> (album["images"] as? List<*>)?.get(0)?.let { image -> (image as Map<*, *>)["url"] as? String } } } ?: ""
    val spotiSongUrlToGo = trackMap["item"]?.let { (it as Map<*, *>)["external_urls"]?.let { externalUrls -> (externalUrls as Map<*, *>)["spotify"] as? String } } ?: ""
    return TrackInfo(songName, artistName, imageUrl, spotiSongUrlToGo)
}

data class TrackInfo(
    val songName: String,
    val artistName: String,
    val imageUrl: String,
    val spotiSongUrlToGo: String
)

data class Song(val name: String, val artist: String, val imageUrl: String)

private fun getDetailedTrackInfo(trackInfo: String): DetailedTrackInfo {
    if (trackInfo.isEmpty() || trackInfo == "null") {
        return DetailedTrackInfo("", "")
    }
    val gson = Gson()
    val trackMap = gson.fromJson(trackInfo, Map::class.java)
    val isPlaying = trackMap["is_playing"] as? Boolean ?: false
    if (!isPlaying) {
        return DetailedTrackInfo("", "")
    }
    val songId = trackMap["item"]?.let { (it as Map<*, *>)["id"] as? String } ?: ""
    val artistId = trackMap["item"]?.let { ((it as Map<*, *>)["artists"] as? List<*>)?.get(0)?.let { artist -> (artist as Map<*, *>)["id"] as? String } } ?: ""
    println(songId)
    return DetailedTrackInfo(songId, artistId)
}

fun getTrackInfoById(trackData: Map<*,*>): TrackInfo {
    val albumData = trackData["album"] as Map<*, *>
    val artistsData = albumData["artists"] as List<Map<*, *>>
    val imagesData = albumData["images"] as List<Map<*, *>>
    val songName = trackData["name"] as String
    val artistName = (artistsData.first()["name"] as String)
    val imageUrl = (imagesData.first()["url"] as String)
    val spotiSongUrlToGo = (trackData["external_urls"] as Map<*, *>)["spotify"] as String
    return TrackInfo(songName, artistName, imageUrl, spotiSongUrlToGo)
}



@Composable
fun MainContent(
    currentlyPlayingTrackState: MutableState<String?>,
    apiService: ApiService,
    mostListenedSongs: MutableState<List<Song>>,
    user: MutableState<User>,
    matchesBy: MutableState<List<IdMatch>>,
    acceptBy: MutableState<List<AcceptedUser>>,
    generateBio: (String) -> Unit,
    createdBio: MutableState<String>,
    matchesByUsers: MutableState<List<User>>,
    getUserTopTracks: () -> Unit,
    updateUser: (String, User) -> Unit,
    acceptUser: (String, String) -> Unit,
    deleteUser: (String, String) -> Unit,
    deleteUserDb: (String) -> Unit,
    matchesByTracks: MutableState<List<TrackInfo>>,
    acceptedByUsers: MutableState<List<User>>,
    onClickAccepteds: () -> Unit
    ){
    var contentType by remember { mutableStateOf(ContentType.MAIN) }
    var previousContentType by remember { mutableStateOf(ContentType.MAIN) }

    var username by remember { mutableStateOf(user.value.username) }
    var gender by remember { mutableStateOf(user.value.gender!!) }
    var birthDate by remember { mutableStateOf(user.value.dateTime!!) }
    var emptyOrInstagram: String
    var emptyOrBio: String
    if (user.value.bio == null) {
        emptyOrBio = ""
    } else {
        emptyOrBio = user.value.bio!!
    }
    var bio by remember { mutableStateOf(emptyOrBio) }
    if (user.value.instagram == null) {
        emptyOrInstagram = ""
    } else {
        emptyOrInstagram = user.value.instagram!!
    }
    var instagram by remember { mutableStateOf(emptyOrInstagram) }






    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp),
        ) {
            AnimatedVisibility(
                visible = contentType == ContentType.MAIN,
                enter = if (contentType.ordinal > previousContentType.ordinal) {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween())
                } else {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween())
                },
                exit = if (contentType.ordinal > previousContentType.ordinal) {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween())
                } else {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween())
                }
            ) {
                MainScreenContent(currentlyPlayingTrackState, apiService)
            }

            AnimatedVisibility(
                visible = contentType == ContentType.OTHER1,
                enter = if (contentType.ordinal > previousContentType.ordinal) {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween())
                } else {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween())
                },
                exit = if (contentType.ordinal > previousContentType.ordinal) {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween())
                } else {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween())
                }
            ) {
                Other1Content(matchesBy, acceptUser, matchesByUsers, deleteUser, matchesByTracks)
            }

            AnimatedVisibility(
                visible = contentType == ContentType.OTHER2,
                enter = if (contentType.ordinal > previousContentType.ordinal) {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween())
                } else {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween())
                },
                exit = if (contentType.ordinal > previousContentType.ordinal) {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween())
                } else {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween())
                }
            ) {
                Other2Content(acceptBy, acceptedByUsers, onClickAccepteds)
            }

            AnimatedVisibility(
                visible = contentType == ContentType.PROFILE,
                enter = if (contentType.ordinal > previousContentType.ordinal) {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween())
                } else {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween())
                },
                exit = if (contentType.ordinal > previousContentType.ordinal) {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween())
                } else {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween())
                }
            ) {
                ProfileContent(apiService, mostListenedSongs, user, getUserTopTracks)
            }

            AnimatedVisibility(
                visible = contentType == ContentType.EDIT,
                enter = if (contentType.ordinal > previousContentType.ordinal) {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween())
                } else {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween())
                },
                exit = if (contentType.ordinal > previousContentType.ordinal) {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween())
                } else {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween())
                }
            ) {
                EditContent(
                    username = username,
                    onUsernameChange = { username = it },
                    birthDate = birthDate,
                    onBirthDateChange = { birthDate = it },
                    gender = gender,
                    onGenderChange = { gender = it },
                    bio = bio,
                    onBioChange = { bio = it },
                    instagram = instagram,
                    onInstagramChange = { instagram = it },
                    updateUser = updateUser,
                    user = user,
                    generateBio = generateBio,
                    createdAboutMe = createdBio,
                    deleteUser = deleteUserDb,
                )
            }
        }


        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(10.dp)
                .align(Alignment.BottomCenter),
            shape = RectangleShape,
            color = Color(255, 255, 255, 255),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dividerModifier = Modifier
                    .height(53.dp)
                    .width(1.dp)
                    .background(Color(0, 0, 0, 20))

                val buttonModifier = Modifier
                    .weight(1f)
                    .height(44.dp)

                Button(
                    onClick = {
                        previousContentType = contentType
                        contentType = ContentType.MAIN
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(255, 255, 255, 255)
                    ),
                    modifier = buttonModifier
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Button Icon",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = dividerModifier)
                Button(
                    onClick = {
                        previousContentType = contentType
                        contentType = ContentType.OTHER1
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(255, 255, 255, 255)
                    ),
                    modifier = buttonModifier
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.search2),
                        contentDescription = "Button Icon",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = dividerModifier)
                Button(
                    onClick = {
                        previousContentType = contentType
                        contentType = ContentType.OTHER2
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(255, 255, 255, 255)
                    ),
                    modifier = buttonModifier
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = "Button Icon",
                        modifier = Modifier.size(46.dp)
                    )
                }
                Spacer(modifier = dividerModifier)
                Button(
                    onClick = {
                        previousContentType = contentType
                        contentType = ContentType.PROFILE
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(255, 255, 255, 255)
                    ),
                    modifier = buttonModifier
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Button Icon",
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = dividerModifier)
                Button(
                    onClick = {
                        previousContentType = contentType
                        contentType = ContentType.EDIT
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(255, 255, 255, 255)
                    ),
                    modifier = buttonModifier
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = "Button Icon",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreenContent(currentlyPlayingTrackState: MutableState<String?>, apiService: ApiService) {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            currentlyPlayingTrackState.value?.let { trackInfo ->
                val (songName, artistName, imageUrl, spotiUrl) = parseTrackInfo(trackInfo)
                if (songName.isEmpty() || artistName.isEmpty() || imageUrl.isEmpty()) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Song Image",
                        modifier = Modifier.size(300.dp)
                    )
                    Text(
                        text = "Şu anda bir şarkı çalmıyor.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bir şarkı çalmaya başladığınızda burada gösterilecektir.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontFamily = ResourcesCompat.getFont(LocalContext.current, R.font.circular)
                            ?.let { FontFamily(it) },
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Song Image",
                        modifier = Modifier
                            .size(320.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotiUrl))
                                ctx.startActivity(intent)
                            }
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        text = "$songName",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
                        fontFamily = ResourcesCompat.getFont(LocalContext.current, R.font.circular)
                            ?.let { FontFamily(it) },
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "$artistName",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontFamily = ResourcesCompat.getFont(LocalContext.current, R.font.circular)
                            ?.let { FontFamily(it) },
                    )
                    Spacer(modifier = Modifier.height(55.dp))

            }
        }

    }
}


@Composable
fun ProfileContent(apiService: ApiService, mostListenedSongs: MutableState<List<Song>>, user: MutableState<User>, getUserTopTracks: () -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Profil", "En Çok Dinlenen Şarkılar")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(255,255,255, 255))
            .padding(0.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = Color.Black
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> ProfileScreen(user)
            1 -> TopSongsTabContent(mostListenedSongs, getUserTopTracks)
        }
    }
}


@Composable
fun ProfileScreen(user: MutableState<User>) {

    LaunchedEffect (user) {

    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        ProfileImage(user)
        Spacer(modifier = Modifier.height(16.dp))
        ProfileInfo(user)
        Spacer(modifier = Modifier.height(16.dp))
        InterestsSection(user)
    }
}

@Composable
fun ProfileImage(user: MutableState<User>) {
    val imageUrl = Client.BASE_URL + "images/" + user.value.spotiId + ".jpg"
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(320.dp)
            .height(320.dp)
            .background(shape = RectangleShape, color = MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .clickable(onClick =
                {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://open.spotify.com/user/" + user.value.spotiId)
                    )
                    context.startActivity(intent)
                }
                )
        )

        }
}

@Composable
fun ProfileInfo(user: MutableState<User>) {
    var age = user.value.dateTime?.substringAfter(", ")
    println("0000")
    println(age)
    println(user.value)
    age = (2024 - age.toString().toInt()).toString()
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = user.value.username, fontWeight = FontWeight.Bold, fontSize = 28.sp)

        Text(text = age + ", " + user.value.gender.toString(),  fontSize = 16.sp, color = Color.Black)

        user.value.location?.let { location ->
            Text(text = "$location", fontSize = 16.sp, color = Color.Black)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val icons = listOf(
                R.drawable.inst_logo,
            )
            val urls = listOf(
                "https://www.instagram.com/"
            )
            icons.forEachIndexed { index, icon ->
                val url = urls[index] + user.value.instagram
                if (user.value.instagram != null || user.value.instagram != "") {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(248,248,248, 0))
                        ,onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    ) {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }


            }
        }
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsSection(user: MutableState<User>) {
    val bio = user.value.bio ?: ""
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Hakkımda", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = bio,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}


@Composable
fun TopSongsTabContent(mostListenedSongs: MutableState<List<Song>>, getUserTopTracks: () -> Unit) {
    // Fetch user top tracks if the list is empty
    if (mostListenedSongs.value.isEmpty()) {
        getUserTopTracks()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(mostListenedSongs.value.size) { index ->
            val song = mostListenedSongs.value[index]
            SongItemCo(song)
        }
    }
}
@Composable
fun SongItemCo(song: Song) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(255, 255, 255, 255))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(song.imageUrl),
                contentDescription = "Song Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = ResourcesCompat.getFont(LocalContext.current, R.font.circular)
                        ?.let { FontFamily(it) }
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}










@Composable
fun Other1Content(
    matchesBy: MutableState<List<IdMatch>>,
    acceptUser: (String, String) -> Unit,
    matchesByUsers: MutableState<List<User>>,
    deleteUser: (String, String) -> Unit,
    matchesByTracks: MutableState<List<TrackInfo>>
) {
    val leftSwipedCards = remember { mutableStateListOf<String>() }
    val rightSwipedCards = remember { mutableStateListOf<String>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (matchesBy.value.isNotEmpty()) {
            val profile = matchesBy.value.first()
            val spotiId = profile.matchedSpotiId
            val track = matchesByTracks.value.first()
            val username = matchesByUsers.value.first().username
            val img = Client.BASE_URL + "images/" + profile.matchedSpotiId + ".jpg"
            println(img)
            val offsetX = remember { Animatable(0f) }
            val coroutineScope = rememberCoroutineScope()

            val modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(700.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                val targetValue = when {
                                    offsetX.value < -200f -> {
                                        leftSwipedCards.add(spotiId)
                                        deleteUser(spotifyUserProfile["id"].toString(), spotiId)
                                        -1000f
                                    }

                                    offsetX.value > 200f -> {
                                        rightSwipedCards.add(spotiId)
                                        acceptUser(spotifyUserProfile["id"].toString(), spotiId)
                                        1000f
                                    }

                                    else -> 0f
                                }
                                offsetX.animateTo(
                                    targetValue = targetValue,
                                    animationSpec = tween(durationMillis = 300)
                                )
                                if (targetValue != 0f) {
                                    delay(300)
                                    matchesBy.value = matchesBy.value.drop(1)
                                    matchesByTracks.value = matchesByTracks.value.drop(1)
                                    matchesByUsers.value = matchesByUsers.value.drop(1)

                                    offsetX.snapTo(0f)
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                        }
                    }
                }

            Box(
                modifier = modifier
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(700.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(255, 255, 255, 255))
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val painter = rememberAsyncImagePainter(img)
                        Image(
                            painter = painter,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .width(350.dp)
                                .height(420.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = username,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = ResourcesCompat.getFont(LocalContext.current, R.font.circular)
                                ?.let { FontFamily(it) },
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(track.imageUrl),
                                contentDescription = "Song Image",
                                modifier = Modifier
                                    .size(128.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Column (
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = track.songName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = ResourcesCompat.getFont(LocalContext.current, R.font.circular)
                                        ?.let { FontFamily(it) },
                                    fontSize = 22.sp
                                )
                                Text(
                                    text = track.artistName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = ResourcesCompat.getFont(LocalContext.current, R.font.circular)
                                        ?.let { FontFamily(it) },
                                    fontSize = 14.sp
                                )
                            }

                        }

                    }
                }
            }
        } else {
            Text("Şuanlık bir eşleşme bulunamadı" , style = MaterialTheme.typography.bodyLarge, fontFamily = customFontFamily, fontSize = 20.sp)
        }
    }
}








@Composable
fun Other2Content(acceptBy: MutableState<List<AcceptedUser>>, acceptedByUsers: MutableState<List<User>>, onClickAccepteds: () -> Unit ){

    val uniqueAcceptByUsers = remember(acceptBy.value) {
        acceptBy.value.distinctBy { it.acceptedSpotiIds }
    }
    val uniqueAcceptedByUsers = remember(acceptedByUsers.value) {
        acceptedByUsers.value.distinctBy { it.spotiId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Eşleşmelerim",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 24.sp,
            fontFamily = customFontFamily
        )
        Spacer(modifier = Modifier.height(16.dp))
        // onClickAccepteds()


        LazyColumn {
            items(uniqueAcceptByUsers.zip(uniqueAcceptedByUsers)) { (user1, user2) ->
                UserCard(user1, user2)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun UserCard(user: AcceptedUser, user2: User) {
    val ctx = LocalContext.current
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(255, 255, 255, 255)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable(
                onClick = {
                    val intent = Intent(ctx, ChatActivity::class.java).apply {
                        putExtra("userId1", user.spotiId)
                        putExtra("userId2", user.acceptedSpotiIds)
                    }
                    ctx.startActivity(intent)
                }
            )

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)

        ) {
            val userImg = Client.BASE_URL + "images/" + user.acceptedSpotiIds + ".jpg"
            Image(
                painter = rememberAsyncImagePainter(userImg),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = user2.username,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun EditContent(
    username: String,
    onUsernameChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    instagram: String,
    onInstagramChange: (String) -> Unit,
    updateUser: (String, User) -> Unit,
    user: MutableState<User>,
    generateBio: (String) -> Unit,
    createdAboutMe: MutableState<String>,
    deleteUser: (String) -> Unit
) {

    LaunchedEffect(createdAboutMe.value) {
        if (createdAboutMe.value.isNotEmpty()) {
            onBioChange(createdAboutMe.value.trim())
        }
    }
    LaunchedEffect(user.value) {
        onUsernameChange(user.value.username)
        onGenderChange(user.value.gender!!)
        onBirthDateChange(user.value.dateTime!!)
        onBioChange(user.value.bio ?: "")
        onInstagramChange(user.value.instagram ?: "")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(255, 255, 255, 255))
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    if (it.length <= 16)
                        onUsernameChange(it)
                },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.White)
            )
            if (username.length > 15) {
                Text(
                    text = "Kullanıcı adı en fazla 15 karakter olmalıdır",
                    color = Color.Red,
                    fontFamily = customFontFamily,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(70.dp)
                    .align(Alignment.CenterHorizontally)

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,

                ) {
                    Text(text = "Cinsiyetin: ")
                    RadioButton(
                        selected = gender == "Erkek",
                        onClick = {
                            onGenderChange("Erkek")
                        }
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


            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val initialDate = Calendar.getInstance().apply {
                time = dateFormat.parse(user.value.dateTime!!) ?: Date()
            }
            val formattedStartDate =
                DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.US).format(initialDate.time)

            var selectedDate by remember { mutableStateOf<Calendar?>(initialDate) }
            var formattedDate by remember {
                mutableStateOf(formattedStartDate)
            }
            val ctx = LocalContext.current
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(70.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        val calendar = selectedDate ?: Calendar.getInstance()
                        DatePickerDialog(
                            ctx,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                selectedDate = calendar
                                formattedDate = DateFormat
                                    .getDateInstance(DateFormat.DEFAULT, Locale.US)
                                    .format(calendar.time)
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
                                fontFamily = customFontFamily
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = bio,
                onValueChange = {
                    if (it.length <= 501)
                        onBioChange(it)
                },
                label = { Text("Bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.White)
            )
            if (bio.length > 500) {
                Text(
                    text = "Kullanıcı adı en fazla 500 karakter olmalıdır",
                    color = Color.Red,
                    fontFamily = customFontFamily,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Button(onClick = {
                generateBio(bio)
            },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(192, 114, 181, 255))
            ) {
                Text("Hakkımda Yazısını Yapay Zeka ile Doldur")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = instagram,
                onValueChange = { onInstagramChange(it) },
                label = { Text("Instagram") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val spotiId = spotifyUserProfile["id"].toString()
                val updatedUser = user.value.copy()

                Button(
                    onClick = {
                        if (username.length > 15) {
                            return@Button
                        }

                        if (bio.length > 500) {
                            return@Button
                        }

                        updatedUser.gender = gender
                        updatedUser.username = username
                        updatedUser.dateTime = formattedDate
                        updatedUser.bio = bio
                        updatedUser.instagram = instagram
                        updateUser(spotiId, updatedUser)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(192, 114, 181, 255))
                    ,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(text = "Kaydet")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val spotiId = spotifyUserProfile["id"].toString()

                Button(
                    onClick = {
                        deleteUser(spotiId)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(172, 54, 54, 255)
                    )
                ) {
                    Text(text = "Hesabı Sil")
                }
            }
            Spacer(modifier = Modifier.height(500.dp))
        }
    }
}