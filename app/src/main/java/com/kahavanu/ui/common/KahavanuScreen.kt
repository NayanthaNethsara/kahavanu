package com.kahavanu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kahavanu.ui.theme.AmbientGlowPrimary
import com.kahavanu.ui.theme.AmbientGlowSecondary
import com.kahavanu.ui.theme.AmbientGlowTertiary
import com.kahavanu.ui.theme.ScreenBackground
import com.kahavanu.ui.theme.Spacing

/**
 * Shared screen scaffold that matches the HomeScreen pattern: ambient glow
 * background, top-aligned ScreenHeader, and a LazyColumn with consistent
 * content padding and section spacing.
 *
 * Use [screenSection] inside [content] to get the standard horizontal padding
 * applied to each section card.
 */
@Composable
fun KahavanuScreen(
    headerLabel: String,
    headerTitle: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(top = 140.dp, bottom = 120.dp),
    content: LazyListScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        AmbientGlow(
            color = AmbientGlowPrimary,
            size = 360.dp,
            modifier = Modifier.offset(x = (-96).dp, y = (-128).dp),
        )
        AmbientGlow(
            color = AmbientGlowSecondary,
            size = 320.dp,
            modifier = Modifier.offset(x = 170.dp, y = 284.dp),
        )
        AmbientGlow(
            color = AmbientGlowTertiary,
            size = 300.dp,
            modifier = Modifier.offset(x = 98.dp, y = 648.dp),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.extraLarge),
                ) {
                    ScreenHeader(label = headerLabel, title = headerTitle)
                }
            }
            content()
        }
    }
}

/**
 * Adds a section to a [KahavanuScreen] with the standard horizontal padding.
 */
fun LazyListScope.screenSection(
    content: @Composable () -> Unit,
) {
    item {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.extraLarge),
        ) {
            content()
        }
    }
}
