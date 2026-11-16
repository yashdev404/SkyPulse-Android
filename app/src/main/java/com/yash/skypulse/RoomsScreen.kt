package com.yash.skypulse

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Futuristic HUD Telemetry Color System
private val DarkObsidian = Color(0xFF080C14)
private val CardBgStart = Color(0xFF0C121D)
private val CardBgEnd = Color(0xFF080C14)
private val CosmicCyan = Color(0xFF38BDF8)
private val CosmicIce = Color(0xFF7DD3FC)
private val CosmicStarlight = Color(0xFFBAE6FD)
private val AmberGold = Color(0xFFF59E0B)
private val AmberGoldBorder = Color(0xFFFBBF24)
private val MutedText = Color(0xFF94A3B8)
private val BorderCyan = Color(0xFF38BDF8).copy(alpha = 0.25f)

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
        _rooms.value = listOf(
            ChatRoom("1", "RF-01", "Mars Colonists", "Discussions about the upcoming orbital transfer & habitat", 142, "🔴", isPrivate = false, avatarType = "mars"),
            ChatRoom("2", "RF-02", "Astrophotography", "Share your telescope setups and deep sky captures", 56, "🌌", isPrivate = false, avatarType = "nebula"),
            ChatRoom("3", "RF-03", "Webb Telescope Fans", "Latest data, infrared discoveries and calibration", 89, "🛰️", isPrivate = false, avatarType = "jwst"),
            ChatRoom("4", "RF-04", "Secret Base", "Classified clearance only. Encrypted channel.", 12, "👽", isPrivate = true, avatarType = "alien"),
            ChatRoom("5", "RF-05", "Meteorite Hunters", "Tracking atmospheric fireballs & impact sites", 74, "☄️", isPrivate = false, avatarType = "meteorite"),
            ChatRoom("6", "RF-06", "Orbital Mechanics", "Trajectory calculations, slingshots & vectors", 31, "🛰️", isPrivate = false, avatarType = "orbit")
        )
    }

    fun joinRoom(room: ChatRoom) {
        _activeRoom.value = room
        _messages.value = emptyList()
        
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
        
        val request = Request.Builder().url("ws://10.0.2.2:8080/chat").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val incomingMsg = gson.fromJson(text, ChatMessage::class.java)
                val processedMsg = if (incomingMsg.senderName != "Yash") {
                    incomingMsg.copy(isMe = false)
                } else {
                    incomingMsg
                }
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

    fun deleteRoom(roomId: String) {
        if (_activeRoom.value?.id == roomId) {
            leaveRoom()
        }
        _rooms.value = _rooms.value.filterNot { it.id == roomId }
    }

    fun sendMessage(text: String) {
        val room = _activeRoom.value ?: return
        if (text.isBlank()) return

        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val newMessage = ChatMessage(
            roomId = room.id,
            senderName = "Yash",
            message = text,
            timestamp = currentTime,
            isMe = true
        )
        
        val jsonString = gson.toJson(newMessage)
        webSocket?.send(jsonString)
    }

    fun createRoom(name: String, description: String, isPrivate: Boolean) {
        val newId = (System.currentTimeMillis()).toString()
        val nextRfNumber = String.format(Locale.US, "RF-%02d", _rooms.value.size + 1)
        val newRoom = ChatRoom(
            id = newId,
            rfCode = nextRfNumber,
            name = name,
            description = description,
            activeUsers = 1,
            emojiIcon = if (isPrivate) "🔒" else "📡",
            isPrivate = isPrivate,
            avatarType = if (isPrivate) "alien" else "orbit"
        )
        _rooms.value = listOf(newRoom) + _rooms.value
    }
}

@Composable
fun RoomsScreen(viewModel: RoomsViewModel = viewModel()) {
    val rooms by viewModel.rooms.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showPasskeyDialog by remember { mutableStateOf<ChatRoom?>(null) }
    var showCreateChannelDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ChatRoom?>(null) }

    val filteredRooms = remember(rooms, searchQuery) {
        if (searchQuery.isBlank()) rooms
        else rooms.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.rfCode.contains(searchQuery, ignoreCase = true)
        }
    }

    BackHandler(enabled = activeRoom != null) {
        viewModel.leaveRoom()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkObsidian)
    ) {
        if (activeRoom == null) {
            // STATE 1: HUD Comms Directory View
            Column(modifier = Modifier.fillMaxSize()) {
                // Header & Frequency Radar Deck
                CommsHeaderDeck(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )

                // Channels Scroll List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    items(filteredRooms) { room ->
                        RoomCard(
                            room = room,
                            onClick = {
                                if (room.isPrivate) {
                                    showPasskeyDialog = room
                                } else {
                                    viewModel.joinRoom(room)
                                }
                            },
                            onDeleteClick = {
                                showDeleteConfirmDialog = room
                            }
                        )
                    }
                }
            }

            // Hovering Oval FAB ("Create Channel")
            Surface(
                color = CosmicCyan,
                shape = CircleShape,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 96.dp)
                    .shadow(12.dp, CircleShape, spotColor = CosmicCyan)
                    .clickable { showCreateChannelDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Channel",
                        tint = DarkObsidian,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create Channel",
                        color = DarkObsidian,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        } else {
            // STATE 2: Inside Active Chat Channel
            ChatRoomView(
                room = activeRoom!!,
                messages = messages,
                onBack = { viewModel.leaveRoom() },
                onSendMessage = { viewModel.sendMessage(it) },
                onDeleteRoom = {
                    showDeleteConfirmDialog = activeRoom
                }
            )
        }

        if (showPasskeyDialog != null) {
            PasskeyDialog(
                roomName = showPasskeyDialog!!.name,
                onDismiss = { showPasskeyDialog = null },
                onConfirm = { _ ->
                    viewModel.joinRoom(showPasskeyDialog!!)
                    showPasskeyDialog = null
                }
            )
        }

        if (showCreateChannelDialog) {
            CreateChannelDialog(
                onDismiss = { showCreateChannelDialog = false },
                onCreate = { name, desc, isPrivate ->
                    viewModel.createRoom(name, desc, isPrivate)
                    showCreateChannelDialog = false
                }
            )
        }

        if (showDeleteConfirmDialog != null) {
            DeleteChannelDialog(
                roomName = showDeleteConfirmDialog!!.name,
                onDismiss = { showDeleteConfirmDialog = null },
                onDelete = {
                    viewModel.deleteRoom(showDeleteConfirmDialog!!.id)
                    showDeleteConfirmDialog = null
                }
            )
        }
    }
}

@Composable
fun CommsHeaderDeck(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Comms Channels",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Live Cyan Pulse Dot
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CosmicCyan.copy(alpha = alpha))
                            .shadow(6.dp, CircleShape, spotColor = CosmicCyan)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Join a discussion room",
                        color = CosmicIce.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Animated Frequency Scanner Line
                    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
                    val progress by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "scannerProgress"
                    )

                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(0.5.dp, BorderCyan, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(24.dp)
                                .offset { IntOffset((progress * 56.dp.toPx()).roundToInt(), 0) }
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, CosmicCyan, Color.Transparent)
                                    )
                                )
                        )
                    }
                }
            }

            // Frequency Signal Beacon Tuning Button
            Surface(
                color = CardBgStart,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = "Tuning Signal",
                        tint = CosmicCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Precision Telemetry Radar Search Bar (Unclipped Custom Height & Alignment)
        Surface(
            color = CardBgStart,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "Radar",
                    tint = CosmicCyan,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Find frequencies & comms...",
                            color = CosmicIce.copy(alpha = 0.45f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(CosmicCyan),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan)
                ) {
                    Text(
                        text = "RADAR",
                        color = CosmicIce,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RoomCard(
    room: ChatRoom,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isAmberTheme = room.isPrivate
    val activeBorderColor = if (isAmberTheme) AmberGoldBorder.copy(alpha = 0.4f) else BorderCyan
    val accentColor = if (isAmberTheme) AmberGold else CosmicCyan
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgStart)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(68.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoomAvatarIcon(avatarType = room.avatarType, emojiIcon = room.emojiIcon)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, activeBorderColor)
                        ) {
                            Text(
                                text = room.rfCode,
                                color = if (isAmberTheme) AmberGold else CosmicIce,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        if (room.isPrivate) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Private",
                                tint = AmberGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = room.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = room.description,
                        color = MutedText,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Presence Badge Pill
                Surface(
                    color = CardBgEnd,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, activeBorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${room.activeUsers}",
                            color = if (isAmberTheme) AmberGold else CosmicStarlight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Options Context Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(CardBgStart)
                            .border(1.dp, BorderCyan)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Delete / Leave Channel", color = Color(0xFFF43F5E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoomAvatarIcon(avatarType: String, emojiIcon: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF0A101B))
            .border(1.dp, BorderCyan, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (avatarType) {
            "mars" -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFBE123C), Color(0xFFEF4444), Color(0xFFF59E0B))
                            )
                        )
                )
            }
            "nebula" -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4338CA), Color(0xFF9333EA), Color(0xFF38BDF8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
                }
            }
            "jwst" -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFD97706), Color(0xFF0F172A), Color(0xFF1E3A8A))
                            )
                        )
                        .border(1.dp, AmberGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                }
            }
            "alien" -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(1.dp, MutedText, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                }
            }
            "meteorite" -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF92400E), Color(0xFFEA580C), Color(0xFF38BDF8))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(1.dp, CosmicCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(14.dp))
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
    onSendMessage: (String) -> Unit,
    onDeleteRoom: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkObsidian)
    ) {
        Surface(
            color = CardBgStart,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderCyan)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, top = 40.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CosmicIce)
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    RoomAvatarIcon(avatarType = room.avatarType, emojiIcon = room.emojiIcon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan)
                            ) {
                                Text(
                                    text = room.rfCode,
                                    color = CosmicIce,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                room.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text("${room.activeUsers} Crew Members Live", color = CosmicCyan, fontSize = 11.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = CosmicIce)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(CardBgStart)
                                .border(1.dp, BorderCyan)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Delete / Exit Channel", color = Color(0xFFF43F5E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteRoom()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // Message Input Dock
        Surface(
            color = CardBgStart,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderCyan)
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Transmit message...", color = CosmicIce.copy(alpha = 0.4f), fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkObsidian,
                        unfocusedContainerColor = DarkObsidian,
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = BorderCyan,
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
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CosmicCyan)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = DarkObsidian, modifier = Modifier.size(18.dp))
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
            Surface(
                color = CardBgStart,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan)
            ) {
                Text(message.message, color = CosmicIce, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
    ) {
        if (!message.isMe) {
            Text(message.senderName, color = CosmicIce, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp, start = 8.dp))
        }
        
        Surface(
            color = if (message.isMe) CosmicCyan else CardBgStart,
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (message.isMe) 14.dp else 2.dp,
                bottomEnd = if (message.isMe) 2.dp else 14.dp
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (message.isMe) CosmicCyan else BorderCyan)
        ) {
            Text(
                text = message.message,
                color = if (message.isMe) DarkObsidian else Color.White,
                fontSize = 13.sp,
                fontWeight = if (message.isMe) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
        Text(message.timestamp, color = MutedText, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp, end = 6.dp, start = 6.dp))
    }
}

@Composable
fun PasskeyDialog(roomName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var passkey by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBgStart,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Security Clearance Required", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("Enter clearance passkey for $roomName", color = MutedText, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = passkey,
                    onValueChange = { passkey = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkObsidian,
                        unfocusedContainerColor = DarkObsidian,
                        focusedBorderColor = AmberGold,
                        unfocusedBorderColor = BorderCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(passkey) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Authorize", color = DarkObsidian, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abort", color = MutedText) }
        }
    )
}

@Composable
fun CreateChannelDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, isPrivate: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBgStart,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Radio, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transmit New Comms Channel", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Channel Name", color = MutedText) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkObsidian,
                        unfocusedContainerColor = DarkObsidian,
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = BorderCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Orbital Frequency Topic", color = MutedText) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkObsidian,
                        unfocusedContainerColor = DarkObsidian,
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = BorderCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Encrypted Private Channel", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AmberGold, checkedTrackColor = CardBgEnd)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, description.ifBlank { "Orbital telemetry discussion channel" }, isPrivate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Broadcast Channel", color = DarkObsidian, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedText) }
        }
    )
}

@Composable
fun DeleteChannelDialog(
    roomName: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBgStart,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete / Leave Channel", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                text = "Are you sure you want to delete or leave $roomName? This action will disconnect your frequency connection.",
                color = MutedText,
                fontSize = 12.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete Channel", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedText) }
        }
    )
}
