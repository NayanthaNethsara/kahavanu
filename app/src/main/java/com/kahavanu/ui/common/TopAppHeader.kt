package com.kahavanu.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kahavanu.R
import com.kahavanu.domain.model.UserSession
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun TopAppHeader(
    currentSession: UserSession?,
    onNotificationClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { /* Intercept and consume touches */ }
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color.White,
                        Color.White,
                        Color.White.copy(alpha = 0.9f),
                        Color.Transparent
                    )
                )
            ),
        color = Color.Transparent,
        tonalElevation = Elevation.level0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Spacing.large,
                        end = Spacing.large,
                        top = Spacing.extraLarge,
                        bottom = Spacing.extraLarge + Spacing.medium
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kahavanu_logo),
                        contentDescription = "Kahavanu Logo",
                        modifier = Modifier.width(90.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    val glassModifier = Modifier
                        .size(42.dp)
                        .shadow(
                            elevation = 10.dp,
                            spotColor = RawColors.Gray.Gray400,
                            ambientColor = RawColors.Gray.Gray400,
                            shape = CircleShape
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.85f),
                            shape = CircleShape
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clip(CircleShape)

                    // Notification Icon
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = glassModifier
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = RawColors.Emerald.Emerald700,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}