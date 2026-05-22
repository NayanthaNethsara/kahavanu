package com.kahavanu.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.kahavanu.ui.theme.CategoryFood
import com.kahavanu.ui.theme.CategoryFun
import com.kahavanu.ui.theme.CategoryHealth
import com.kahavanu.ui.theme.CategoryLifestyle
import com.kahavanu.ui.theme.CategoryOther
import com.kahavanu.ui.theme.CategoryShopping
import com.kahavanu.ui.theme.CategorySubscriptions
import com.kahavanu.ui.theme.CategoryTransport
import com.kahavanu.ui.theme.CategoryUtilities

fun categoryColor(category: String): Color =
    when (category.trim().lowercase()) {
        "food", "essentials" -> CategoryFood
        "transport" -> CategoryTransport
        "utilities" -> CategoryUtilities
        "shopping" -> CategoryShopping
        "lifestyle" -> CategoryLifestyle
        "health" -> CategoryHealth
        "fun" -> CategoryFun
        "subscriptions" -> CategorySubscriptions
        else -> CategoryOther
    }

fun categoryIcon(category: String): ImageVector =
    when (category.trim().lowercase()) {
        "food", "essentials" -> Icons.Outlined.LocalPizza
        "transport" -> Icons.Outlined.LocalTaxi
        "utilities" -> Icons.Outlined.Bolt
        "shopping", "lifestyle" -> Icons.Outlined.ShoppingBag
        "health" -> Icons.Outlined.HealthAndSafety
        "fun", "subscriptions" -> Icons.Outlined.SportsEsports
        else -> Icons.Outlined.TipsAndUpdates
    }
