package com.yash.skypulse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoomsViewModel : ViewModel() {
    private val _rooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val rooms: StateFlow<List<ChatRoom>> = _rooms

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _activeRoom = MutableStateFlow<ChatRoom?>(null)
    val activeRoom: StateFlow<ChatRoom?> = _activeRoom

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    init {
        // MOCK DATA: Simulating /api/v1/rooms
        _rooms.value = listOf(
            ChatRoom("1", "Mars Colonists", "Discussions about the upcoming Mars missions.", 142, "🔴"),
            ChatRoom("2", "Astrophotography", "Share your telescope setups and sky captures.", 56, "🌌"),
            ChatRoom("3", "Webb Telescope Fans", "Latest data and images from JWST.", 89, "🛰️"),
            ChatRoom("4", "Secret Base", "Classified clearance only.", 12, "👽", isPrivate = true)
        )
    }

    fun joinRoom(room: ChatRoom) {
        _activeRoom.value = room
        _messages.value = emptyList() // Clear old messages
        
        // 1. Fetch persistent chat history from MongoDB via REST API!
        viewModelScope.launch {
            try {
                val response = BackendRetrofitInstance.api.getRoomHistory(room.id)
                if (response.isSuccessful && response.body() != null) {
                    val history = response.body()!!.map { msg ->
                        if (msg.senderName != "Yash") msg.copy(isMe = false) else msg.copy(isMe = true)
                    }
                    _messages.value = history
                }
            } catch (e: Exception) {
                println("Failed to fetch chat history: ${e.message}")
            }
        }
        
        // 2. Connect to Spring Boot WebSocket for live updates!
        val request = Request.Builder().url("ws://10.0.2.2:8080/chat").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // We received a message from Spring Boot!
                val incomingMsg = gson.fromJson(text, ChatMessage::class.java)
                
                // If it's from someone else, make sure isMe is false
                val processedMsg = if (incomingMsg.senderName != "Yash") {
                    incomingMsg.copy(isMe = false)
                } else {
                    incomingMsg
                }

                // Add to our list
                _messages.value = _messages.value + processedMsg
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket Error: ${t.message}")
            }
        })
    }

    fun leaveRoom() {
        webSocket?.close(1000, "User left")
        webSocket = null
        _activeRoom.value = null
        _messages.value = emptyList()
    }

    fun sendMessage(text: String) {
        val room = _activeRoom.value ?: return
        if (text.isBlank()) return

        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newMessage = ChatMessage(
            roomId = room.id,
            senderName = "Yash", // Hardcoded user
            message = text,
            timestamp = currentTime,
            isMe = true
        )
        
        // Convert to JSON and send to Spring Boot!
        val jsonString = gson.toJson(newMessage)
        webSocket?.send(jsonString)
    }
}

@Composable
fun RoomsScreen(viewModel: RoomsViewModel = viewModel()) {
    val rooms by viewModel.rooms.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()

    var showPasskeyDialog by remember { mutableStateOf<ChatRoom?>(null) }

    // Intercept back button to leave the active room instead of closing the app
    BackHandler(enabled = activeRoom != null) {
        viewModel.leaveRoom()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1B2A))) {
        if (activeRoom == null) {
            // STATE 1: List of Rooms
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Comms Channels", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Join a discussion room", color = Color(0xFF4A90E2), fontSize = 14.sp, modifier = Modifier.padding(bottom = 24.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(rooms) { room ->
                        RoomCard(room, onClick = {
                            if (room.isPrivate) {
                                showPasskeyDialog = room
                            } else {
                                viewModel.joinRoom(room)
                            }
                        })
                    }
                }
            }
        } else {
            // STATE 2: Inside a Chat Room
            ChatRoomView(
                room = activeRoom!!,
                messages = messages,
                onBack = { viewModel.leaveRoom() },
                onSendMessage = { viewModel.sendMessage(it) }
            )
        }

        if (showPasskeyDialog != null) {
            PasskeyDialog(
                roomName = showPasskeyDialog!!.name,
                onDismiss = { showPasskeyDialog = null },
                onConfirm = { _ ->
                    // Mock validation
                    viewModel.joinRoom(showPasskeyDialog!!)
                    showPasskeyDialog = null
                }
            )
        }
    }
}

@Composable
fun RoomCard(room: ChatRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A47)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(room.emojiIcon, fontSize = 24.sp)
            }
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (room.isPrivate) {
                        Icon(Icons.Default.Lock, contentDescription = "Private", tint = Color.Yellow, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    }
                    Text(room.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(room.description, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, maxLines = 1)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${room.activeUsers}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChatRoomView(
    room: ChatRoom,
    messages: List<ChatMessage>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Surface(
            color = Color(0xFF1B2A47),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(36.dp)) {
                    Text("←", color = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(room.emojiIcon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(room.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${room.activeUsers} online", color = Color.Green, fontSize = 12.sp)
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // Input Area (Padded at the bottom for floating nav bar)
        Surface(
            color = Color(0xFF1B2A47),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp), // Extra bottom padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Transmit message...", color = Color.White.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A90E2),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        onSendMessage(messageText)
                        messageText = ""
                    },
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF4A90E2))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isSystem = message.senderName == "System"
    
    if (isSystem) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(message.message, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
    ) {
        if (!message.isMe) {
            Text(message.senderName, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp, start = 8.dp))
        }
        
        Surface(
            color = if (message.isMe) Color(0xFF4A90E2) else Color.White.copy(alpha = 0.1f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isMe) 16.dp else 4.dp,
                bottomEnd = if (message.isMe) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.message,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
        Text(message.timestamp, color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp, end = 8.dp, start = 8.dp))
    }
}

@Composable
fun PasskeyDialog(roomName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var passkey by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B2A47),
        title = { Text("Security Clearance Required", color = Color.White) },
        text = {
            Column {
                Text("Enter passkey for $roomName", color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = passkey,
                    onValueChange = { passkey = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(passkey) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))) {
                Text("Authorize")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abort", color = Color.Red) }
        }
    )
}
