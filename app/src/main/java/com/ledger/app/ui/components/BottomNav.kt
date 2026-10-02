package com.ledger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ledger.app.ui.navigation.Routes
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger

sealed class NavTab(val route: String, val label: String) {
    object Home  : NavTab(Routes.HOME,         "Home")
    object Ops   : NavTab(Routes.TRANSACTIONS, "Activity")
    object Add   : NavTab("add",               "Add")
    object Stats : NavTab(Routes.STATS,        "Stats")
    object More  : NavTab(Routes.SETTINGS,     "More")
}

val NavTabs = listOf(NavTab.Home, NavTab.Ops, NavTab.Add, NavTab.Stats, NavTab.More)

private fun NavTab.icon(): ImageVector = when (this) {
    NavTab.Home  -> LedgerIcons.Home
    NavTab.Ops   -> LedgerIcons.List
    NavTab.Add   -> LedgerIcons.Plus
    NavTab.Stats -> LedgerIcons.PieChart
    NavTab.More  -> LedgerIcons.Grid
}

/** Floating pill navigation with a central "add" button. */
@Composable
fun LedgerBottomNav(
    currentRoute: String,
    onTabSelected: (NavTab) -> Unit
) {
    val c = MaterialTheme.ledger
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = if (c.isDark) 18.dp else 10.dp,
                    shape = shape,
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .clip(shape)
                .background(c.surface)
                .border(1.dp, c.border, shape)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabs.forEach { tab ->
                if (tab is NavTab.Add) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(c.accent)
                            .clickable { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(tab.icon(), contentDescription = "Add operation", tint = c.onAccent, modifier = Modifier.size(26.dp))
                    }
                } else {
                    val isActive = currentRoute == tab.route
                    val tint = if (isActive) c.text else c.muted
                    Column(
                        modifier = Modifier
                            .width(64.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isActive) c.accentSoft else Color.Transparent)
                            .clickable { onTabSelected(tab) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(tab.icon(), contentDescription = tab.label, tint = tint, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tab.label,
                            fontFamily = AppFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = tint
                        )
                    }
                }
            }
        }
    }
}
