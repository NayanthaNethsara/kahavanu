package com.kahavanu.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.kahavanu.domain.model.GoalCategory
import com.kahavanu.ui.theme.BrandAccent
import com.kahavanu.ui.theme.IconMuted
import com.kahavanu.ui.theme.Info
import com.kahavanu.ui.theme.Primary
import com.kahavanu.ui.theme.Romance
import com.kahavanu.ui.theme.UtilityAccent
import com.kahavanu.ui.theme.Warning

/** Canonical color for a goal category — used in active and completed goal cards. */
fun goalCategoryColor(category: GoalCategory): Color = when (category) {
    GoalCategory.SAVINGS -> BrandAccent
    GoalCategory.TRAVEL -> Info
    GoalCategory.EMERGENCY -> Warning
    GoalCategory.EDUCATION -> UtilityAccent
    GoalCategory.PURCHASE -> Romance
    GoalCategory.INVESTMENT -> Primary
    GoalCategory.OTHER -> IconMuted
}

/** Canonical icon for a goal category — used in active and completed goal cards. */
fun goalCategoryIcon(category: GoalCategory): ImageVector = when (category) {
    GoalCategory.SAVINGS -> Icons.Outlined.Savings
    GoalCategory.TRAVEL -> Icons.Outlined.FlightTakeoff
    GoalCategory.EMERGENCY -> Icons.Outlined.Shield
    GoalCategory.EDUCATION -> Icons.Outlined.School
    GoalCategory.PURCHASE -> Icons.Outlined.ShoppingBag
    GoalCategory.INVESTMENT -> Icons.Outlined.BarChart
    GoalCategory.OTHER -> Icons.Outlined.Flag
}
