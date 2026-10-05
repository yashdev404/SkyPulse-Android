package com.yash.skypulse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen() {
    var showPasskeyDialog by remember { mutableStateOf(false) }
    var showJoinRoomSheet by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<ChannelItem?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)) // Deep space blue
    ) {
        // 1. Community Sidebar (The "Bases")
        CommunitySidebar()

        // 2. Main Content Area (The "Mission Control")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            RoomHeader()
            
            Box(modifier = Modifier.weight(1f)) {
                ChannelList(
                    onChannelClick = { item ->
                        selectedRoom = item
                        if (item.isPrivate) {
                            showPasskeyDialog = true
                        } else {
                            showJoinRoomSheet = true
                        }
                    }
                )
            }
            
            UserStatusCard()
            
            // Padding for the bottom navigation pill
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // 3. Interactive Modals
    if (showPasskeyDialog && selectedRoom != null) {
        PasskeyDialog(
            roomName = selectedRoom!!.name,
            onDismiss = { showPasskeyDialog = false },
            onConfirm = { passkey ->
                // Future backend logic goes here
                showPasskeyDialog = false
                showJoinRoomSheet = true // Proceed to join sheet if valid (mocked)
            }
        )
    }

    if (showJoinRoomSheet && selectedRoom != null) {
        ModalBottomSheet(
            onDismissRequest = { showJoinRoomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1B2A47) // Space theme sheet
        ) {
            JoinRoomSheetContent(room = selectedRoom!!)
        }
    }
}

@Composable
fun CommunitySidebar() {
    Column(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Global Comms Icon
        SidebarIcon("💬", isSelected = true)
        
        HorizontalDivider(modifier = Modifier.width(32.dp), color = Color.White.copy(alpha = 0.1f))
        
        // Mock Communities
        SidebarIcon("🚀")
        SidebarIcon("🛸")
        SidebarIcon("🔭")
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Base", tint = Color.White)
        }
    }
}

@Composable
fun SidebarIcon(emoji: String, isSelected: Boolean = false) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(if (isSelected) RoundedCornerShape(16.dp) else CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 20.sp)
    }
}

@Composable
fun RoomHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Global Comms",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "23,882 Explorers Online",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
        IconButton(onClick = { /* Search */ }) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
        }
    }
}

@Composable
fun ChannelList(onChannelClick: (ChannelItem) -> Unit) {
    val categories = listOf(
        ChannelCategory("MISSION BRIEFING", listOf(
            ChannelItem("announcements", Icons.Default.Campaign),
            ChannelItem("rules-of-engagement", Icons.Default.Description)
        )),
        ChannelCategory("GLOBAL LOUNGES", listOf(
            ChannelItem("general-chat", Icons.Default.ChatBubbleOutline),
            ChannelItem("mission-memes", Icons.Default.SentimentVerySatisfied),
            ChannelItem("observatory", Icons.Default.Podcasts, isLive = true)
        )),
        ChannelCategory("VOICE COMMS", listOf(
            ChannelItem("Main Hangar", Icons.AutoMirrored.Filled.VolumeUp),
            ChannelItem("Research Lab", Icons.Default.Lock, isPrivate = true)
        ))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(categories) { category ->
            Column {
                Text(
                    text = category.name,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                category.items.forEach { item ->
                    ChannelRow(item, onClick = { onChannelClick(item) })
                }
            }
        }
    }
}

@Composable
fun ChannelRow(item: ChannelItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (item.isLive) Color.Red else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = item.name,
            color = if (item.isLive) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 12.dp).weight(1f)
        )
        if (item.isLive) {
            Surface(
                color = Color.Red,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "LIVE",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun UserStatusCard() {
    Surface(
        color = Color.Black.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨‍🚀", fontSize = 18.sp)
                }
                // Online Status Dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF0D1B2A), CircleShape) // Border
                        .padding(2.dp)
                        .background(Color.Green, CircleShape)
                )
            }
            
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text("stargazer_yash", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Online", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            
            IconButton(onClick = { /* Settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun PasskeyDialog(roomName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var passkey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1B2A), // Space theme dialog
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Restricted Access")
            }
        },
        text = {
            Column {
                Text("Room: #$roomName\nEnter clearance code to proceed.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = passkey,
                    onValueChange = { passkey = it },
                    label = { Text("Passkey", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(passkey) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Authenticate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abort", color = Color.Gray)
            }
        }
    )
}

@Composable
fun JoinRoomSheetContent(room: ChannelItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = room.icon,
                contentDescription = null,
                tint = if (room.isLive) Color.Red else Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Join #${room.name}",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = if (room.isLive) "Event is currently live!" else "Voice and text comms open.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { /* Handle actual join logic in future */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (room.isLive) Color.Red else MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Connect", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp)) // Extra padding for safe area
    }
}

data class ChannelCategory(val name: String, val items: List<ChannelItem>)
data class ChannelItem(
    val name: String, 
    val icon: ImageVector, 
    val isLive: Boolean = false,
    val isPrivate: Boolean = false
)
