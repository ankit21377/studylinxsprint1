package com.example.studylinx

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studylinx.model.NotificationItem

@Composable
fun NotificationItemCard(data: NotificationItem, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Placeholder profile image
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(data.userName, fontWeight = FontWeight.Bold)
            Text("${data.action}  •  ${data.likeCount} likes", color = Color.Gray)
            Text(data.timeAgo, color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize)
        }

        if (!data.isRead) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Green)
            )
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (selected) Color(0xFFE5FFF1) else Color(0xFFF3F3F3),
                shape = CircleShape
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            color = if (selected) Color(0xFF00C853) else Color.DarkGray,
            fontWeight = FontWeight.SemiBold
        )
    }
}
