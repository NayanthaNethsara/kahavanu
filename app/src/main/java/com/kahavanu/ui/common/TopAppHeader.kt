package com.kahavanu.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kahavanu.R
import com.kahavanu.domain.model.UserSession
import com.kahavanu.ui.theme.Elevation
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextSecondary

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
                    CircularIconButton(
                        icon = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        onClick = onNotificationClick,
                        size = 42.dp,
                        iconSize = 22.dp,
                        shadowElevation = 10.dp,
                        backgroundColor = Color.White.copy(alpha = 0.85f),
                        borderColor = Color.White.copy(alpha = 0.5f),
                        borderWidth = 0.5.dp,
                        tint = TextSecondary,
                    )
                }
            }
        }
    }
}