package com.example.mesrapp.pages

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mesrapp.ChatViewModel
import com.example.mesrapp.R
import com.example.mesrapp.clients.Client
import com.example.mesrapp.models.User
import com.example.mesrapp.services.FirebaseService
import com.example.mesrapp.services.Message
import com.example.mesrapp.ui.theme.MesrappTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID


class ChatActivity : ComponentActivity() {
    var userId2: String = ""
    val apiService = Client.apiService
    private val user: MutableState<User> = mutableStateOf(User("user", "1", "1", "Erkek", "May 18, 2024"))
    @OptIn(DelicateCoroutinesApi::class)

    private fun getUser() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getUser(userId2).execute()
                user.value = response.body()!!
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(context: Context, image: String, message: Message, imageUri: Uri, viewModel: ChatViewModel, chatId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                inputStream.use { input ->
                    val file = File(context.cacheDir, "temp_image.jpg")
                    FileOutputStream(file).use { output ->
                        input?.copyTo(output)
                    }
                    val imagePart = MultipartBody.Part.createFormData("file", "$image.jpg", file.asRequestBody())
                    val response = apiService.uploadImage(image, imagePart).execute()
                    if (response.isSuccessful) {
                        viewModel.sendImageMessage(chatId, message.senderId, message.receiverId, image)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        val userId1 = intent.getStringExtra("userId1")
        userId2 = intent.getStringExtra("userId2")!!
        getUser()
        super.onCreate(savedInstanceState)
        setContent {
            MesrappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    ChatScreen(
                        userId1!!,
                        userId2!!,
                        user,
                        uploadImage = { image, message, imageUri, viewModel, chatId ->
                            uploadImage(this, image, message, imageUri, viewModel, chatId)
                        })
                }
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

@Composable
fun BottomBorder(
    color: Color,
    height: Dp = 1.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(color)
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = height.toPx()
                )
            }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(currentUserId: String, otherUserId: String, user: MutableState<User>, uploadImage: (String, Message, Uri, ChatViewModel, String) -> Unit) {
    val viewModel = remember { ChatViewModel() }
    val chatId = FirebaseService.getChatId(currentUserId, otherUserId)
    val messages by viewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var previousDate: LocalDate? = null


    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                        val uuid = UUID.randomUUID().toString()
                        uploadImage(uuid, Message(currentUserId, otherUserId, uuid, System.currentTimeMillis(), true), it, viewModel, chatId)
                    }
            }
    )

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
    }
    val userImg = Client.BASE_URL + "images/" + otherUserId + ".jpg"
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0,0,0,10))
                .clickable(
                    onClick = {
                        val intent = Intent(ctx, OtherProfile::class.java).apply {
                            putExtra("spotiId", otherUserId)
                        }
                        ctx.startActivity(intent)
                    }
                )
        ) {
            Spacer(modifier = Modifier.width(20.dp))
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .align(Alignment.CenterVertically)
                    .border(2.dp, Color(0,0,0,30) ,shape = CircleShape)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(userImg),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.width(20.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = user.value.username,
                    color = Color.Black,
                    fontFamily = customFontFamily,
                    fontSize = 24.sp
                )
            }

        }

        BottomBorder(color = Color(0,0,0,20), height = 1.dp)

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Column {
                messages.forEach { message ->
                    val isCurrentUser = message.senderId == currentUserId
                    val backgroundColor = if (isCurrentUser) Color(221, 119, 207, 255) else Color(233,233,235)
                    val alignment = if (isCurrentUser) Alignment.TopEnd else Alignment.TopStart
                    val shape = RoundedCornerShape(12.dp)
                    val color = if (isCurrentUser) Color.White else Color.Black
                    val timestamp = message.timestamp
                    val localDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    val currentDate = localDateTime.toLocalDate()
                    val formattedTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(localDateTime)

                    if (currentDate != previousDate) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        previousDate = currentDate
                    }

                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        contentAlignment = alignment

                    ) {
                        Box(
                            modifier = Modifier
                                .shadow(2.dp, shape = shape)
                                .wrapContentWidth()
                        ) {
                            if (message.isImage) {
                                Image(
                                    painter = rememberAsyncImagePainter(Client.BASE_URL + "chatimages/" + message.message + ".jpg"),
                                    contentDescription = "Image",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .fillMaxSize()
                                        .clickable(
                                            onClick = {
                                                val intent =
                                                    Intent(context, ImageDetailActivity::class.java)
                                                intent.putExtra(
                                                    "imageUrl",
                                                    Client.BASE_URL + "chatimages/" + message.message + ".jpg"
                                                )
                                                context.startActivity(intent)
                                            }
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(
                                            start = 8.dp,
                                            top = 4.dp,
                                            end = 8.dp,
                                            bottom = 8.dp
                                        )
                                        .background(Color(0,0,0,150), shape = RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = formattedTime,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(color = backgroundColor, shape = shape)
                                        .padding(8.dp, 8.dp, 8.dp, 0.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = message.message,
                                            color = color,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                            Text(
                                                text = formattedTime,
                                                color = color,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 8.dp)
                                                    .align(Alignment.End)
                                            )

                                    }
                                }
                            }
                        }
                    }
                }
            }
            LaunchedEffect(messages) {
                scrollState.scrollTo(scrollState.maxValue)
            }
        }


        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0,0,0,20)))
                .background(Color(0,0,0,10))
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .border(1.dp, Color.Black, shape = RoundedCornerShape(16.dp))
                    .height(40.dp)
                    .align(Alignment.CenterVertically)
                    .padding(start = 8.dp),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Start
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                }
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(width = 1.dp, color = Color.Black, shape = CircleShape)
                    .padding(8.dp)
                    .clickable(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                            messageText = ""
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.picture),
                    contentDescription = "Send Image",
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .border(width = 1.dp, color = Color.Black, shape = CircleShape)
                    .padding(4.dp)
                    .clickable(
                        onClick = {
                            viewModel.sendMessage(
                                chatId,
                                currentUserId,
                                otherUserId,
                                messageText
                            )
                            messageText = ""
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        }
    }
}
