package com.ledger.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ledger.app.domain.model.AccountType
import com.ledger.app.domain.model.Category
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger

// ── Category & account visuals ───────────────────────────────────────────────

/** Icon for a category: by its built-in icon code first, then guessed from the name. */
fun categoryIcon(category: Category?): ImageVector {
    if (category == null) return LedgerIcons.Tag
    return iconForCode(category.iconCode) ?: iconForName(category.name) ?: LedgerIcons.Tag
}

private fun iconForCode(code: String): ImageVector? = when (code) {
    "food"      -> LedgerIcons.Cart
    "cafe"      -> LedgerIcons.Coffee
    "transport" -> LedgerIcons.Bus
    "subs"      -> LedgerIcons.Repeat
    "health"    -> LedgerIcons.Health
    "fun"       -> LedgerIcons.Gamepad
    "home"      -> LedgerIcons.Building
    "clothes"   -> LedgerIcons.Shirt
    "salary"    -> LedgerIcons.Briefcase
    "freelance" -> LedgerIcons.Laptop
    "gift"      -> LedgerIcons.Gift
    "transfer"  -> LedgerIcons.Transfer
    else        -> null // "other" and user-created categories fall back to the name
}

private fun iconForName(name: String): ImageVector? {
    val n = name.lowercase()
    fun has(vararg keys: String) = keys.any { n.contains(it) }
    return when {
        has("перевод", "transfer")                                        -> LedgerIcons.Transfer
        has("продукт", "супермаркет", "еда", "grocer", "food")            -> LedgerIcons.Cart
        has("кафе", "ресторан", "кофе", "cafe", "coffee", "restaurant")   -> LedgerIcons.Coffee
        has("транспорт", "такси", "метро", "автобус", "transport", "taxi") -> LedgerIcons.Bus
        has("топлив", "бенз", "азс", "fuel", "petrol")                    -> LedgerIcons.Fuel
        has("авто", "машин", "car")                                       -> LedgerIcons.Car
        has("здоров", "аптек", "медиц", "врач", "стомат", "health", "pharm") -> LedgerIcons.Health
        has("жкх", "коммун", "электр", "utilit")                          -> LedgerIcons.Zap
        has("подписк", "subscr")                                          -> LedgerIcons.Repeat
        has("одежд", "обув", "cloth")                                     -> LedgerIcons.Shirt
        has("образов", "учеб", "курс", "educat", "study")                 -> LedgerIcons.Study
        has("досуг", "развлеч", "кино", "игр", "leisure", "entertain")    -> LedgerIcons.Gamepad
        has("дом", "аренд", "кварт", "ипотек", "home", "rent", "hous")    -> LedgerIcons.Building
        has("зарплат", "оклад", "salary")                                 -> LedgerIcons.Briefcase
        has("фриланс", "подработ", "freelance")                           -> LedgerIcons.Laptop
        has("подар", "gift")                                              -> LedgerIcons.Gift
        has("кэшбэк", "процент", "дивиден", "cashback", "interest")       -> LedgerIcons.Coins
        else -> null
    }
}

fun accountIcon(type: AccountType): ImageVector = when (type) {
    AccountType.CARD    -> LedgerIcons.Card
    AccountType.CASH    -> LedgerIcons.Cash
    AccountType.DEPOSIT -> LedgerIcons.Building
    AccountType.SAVINGS -> LedgerIcons.Wallet
}

/** Rounded square with a tinted background and a line icon in [color]. */
@Composable
fun IconBubble(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    corner: Dp = 14.dp,
    background: Color = color.copy(alpha = 0.16f)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(size * 0.46f))
    }
}

@Composable
fun CategoryBubble(
    category: Category?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    corner: Dp = 14.dp
) {
    val color = category?.let { parseHexColor(it.color) } ?: MaterialTheme.ledger.muted
    IconBubble(categoryIcon(category), color, modifier, size, corner)
}

// ── Surfaces ─────────────────────────────────────────────────────────────────

/** Card surface: rounded, surface fill, hairline border. */
@Composable
fun Modifier.ledgerCard(radius: Dp = 22.dp): Modifier {
    val c = MaterialTheme.ledger
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .background(c.surface)
        .border(1.dp, c.border, shape)
}

@Composable
fun LedgerCard(
    modifier: Modifier = Modifier,
    radius: Dp = 22.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    verticalSpacing: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .ledgerCard(radius)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content
    )
}

// ── Headers ──────────────────────────────────────────────────────────────────

/** Big title for top-level tabs (Activity, Statistics, Settings). */
@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = MaterialTheme.ledger
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.displayMedium, color = c.text)
        trailing?.invoke()
    }
}

/** Back button + centered title + optional trailing action, for secondary screens. */
@Composable
fun TopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backIcon: ImageVector = LedgerIcons.ArrowLeft,
    trailing: (@Composable () -> Unit)? = null
) {
    val c = MaterialTheme.ledger
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIconButton(backIcon, onClick = onBack, contentDescription = "Back")
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = c.text,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        )
        if (trailing != null) trailing() else Spacer(Modifier.size(44.dp))
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    actionColor: Color? = null,
    onAction: (() -> Unit)? = null
) {
    val c = MaterialTheme.ledger
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = c.text)
        if (action != null) {
            Text(
                action,
                fontFamily = AppFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = actionColor ?: c.muted,
                modifier = if (onAction != null) {
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAction)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                } else Modifier
            )
        }
    }
}

/** Small uppercase caption above a group of settings rows. */
@Composable
fun GroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.ledger.faint,
        modifier = modifier
    )
}

// ── Buttons & controls ───────────────────────────────────────────────────────

@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color? = null,
    contentDescription: String? = null
) {
    val c = MaterialTheme.ledger
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(c.surface)
            .border(1.dp, c.border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = tint ?: c.text, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val c = MaterialTheme.ledger
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (enabled) c.accent else c.surface2)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontFamily = AppFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (enabled) c.onAccent else c.faint
        )
    }
}

enum class ButtonKind { PRIMARY, SECONDARY, DANGER }

@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: ButtonKind = ButtonKind.PRIMARY
) {
    val c = MaterialTheme.ledger
    val (bg, fg) = when (kind) {
        ButtonKind.PRIMARY   -> c.accent to c.onAccent
        ButtonKind.SECONDARY -> c.surface2 to c.text
        ButtonKind.DANGER    -> c.danger to Color.White
    }
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontFamily = AppFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = fg)
    }
}

/**
 * Pill-shaped segmented control. The selected segment is inverted (text color fill)
 * or, with [accentSelected], filled with the accent.
 */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector?)? = null,
    height: Dp = 44.dp,
    accentSelected: Boolean = false,
    containerColor: Color? = null,
    bordered: Boolean = true
) {
    val c = MaterialTheme.ledger
    val outer = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(outer)
            .background(containerColor ?: c.surface)
            .then(if (bordered) Modifier.border(1.dp, c.border, outer) else Modifier)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { opt ->
            val on = opt == selected
            val bg = when {
                !on            -> Color.Transparent
                accentSelected -> c.accent
                else           -> c.text
            }
            val fg = when {
                !on            -> c.muted
                accentSelected -> c.onAccent
                else           -> c.bg
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .clickable { onSelect(opt) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val ic = icon?.invoke(opt)
                if (ic != null) {
                    Icon(ic, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    label(opt),
                    fontFamily = AppFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = fg,
                    maxLines = 1
                )
            }
        }
    }
}

/** Filter / choice chip. Selected chips are inverted. */
@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.ledger
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) c.text else c.surface)
            .then(if (selected) Modifier else Modifier.border(1.dp, c.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            fontFamily = AppFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (selected) c.bg else c.muted
        )
    }
}

@Composable
fun LedgerSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val c = MaterialTheme.ledger
    val knobOffset by animateDpAsState(if (checked) 23.dp else 3.dp, label = "switchKnob")
    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 30.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(15.dp))
            .background(if (checked) c.accent else c.surface2)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset, y = 3.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) c.onAccent else c.muted)
        )
    }
}

@Composable
fun ProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    track: Color? = null
) {
    val shape = RoundedCornerShape(height / 2)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(track ?: MaterialTheme.ledger.surface2)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(shape)
                .background(color)
        )
    }
}

// ── Fields ───────────────────────────────────────────────────────────────────

/** Tappable card showing a label and value, e.g. account / date pickers. */
@Composable
fun FieldCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    leading: (@Composable () -> Unit)? = null,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val c = MaterialTheme.ledger
    Row(
        modifier = modifier
            .ledgerCard(20.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(12.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = c.muted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
            Text(
                value,
                fontFamily = AppFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = c.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailingText != null) {
            Text(trailingText, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = c.muted)
            Spacer(Modifier.width(8.dp))
        }
        if (trailingIcon != null) {
            Icon(trailingIcon, contentDescription = null, tint = c.faint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun LedgerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    containerColor: Color? = null,
    singleLine: Boolean = true
) {
    val c = MaterialTheme.ledger
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                label,
                fontFamily = AppFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = c.muted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(containerColor ?: c.surface2)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = c.muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(
                    fontFamily = AppFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = c.text
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                cursorBrush = SolidColor(c.text),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                fontFamily = AppFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = c.faint
                            )
                        }
                        inner()
                    }
                }
            )
        }
    }
}

// ── Dialogs & states ─────────────────────────────────────────────────────────

@Composable
fun LedgerDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = "Cancel",
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = MaterialTheme.ledger
    val shape = RoundedCornerShape(28.dp)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(shape)
                .background(c.surface)
                .border(1.dp, c.border, shape)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = c.text)
            content()
            if (dismissText != null || (confirmText != null && onConfirm != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (dismissText != null) {
                        DialogButton(dismissText, onDismiss, Modifier.weight(1f), ButtonKind.SECONDARY)
                    }
                    if (confirmText != null && onConfirm != null) {
                        DialogButton(
                            confirmText,
                            onConfirm,
                            Modifier.weight(1f),
                            if (destructive) ButtonKind.DANGER else ButtonKind.PRIMARY
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.ledger
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBubble(icon, c.muted, size = 56.dp, corner = 20.dp, background = c.surface2)
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.text)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.muted, textAlign = TextAlign.Center)
    }
}

/** Small colored dot used in chart legends. */
@Composable
fun LegendDot(color: Color, label: String) {
    val c = MaterialTheme.ledger
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = c.muted)
    }
}
