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
    return categoryIcon(category.iconCode, category.name)
}

/** Icon for an icon code; [AUTO_ICON_CODE] (or an unknown code) guesses it from the name. */
fun categoryIcon(iconCode: String, name: String): ImageVector =
    iconForCode(iconCode) ?: iconForName(name) ?: LedgerIcons.Tag

/** Icon code meaning "pick the icon from the category name". */
const val AUTO_ICON_CODE = "other"

class CategoryIconOption(val code: String, val icon: ImageVector)

/**
 * Icons a category can be given, in picker order. Codes are stored in Category.iconCode;
 * the legacy built-in codes (food, cafe, transport, ...) keep their meaning.
 */
val CATEGORY_ICON_OPTIONS: List<CategoryIconOption> by lazy {
    listOf(
        // Everyday spending
        CategoryIconOption("food", LedgerIcons.Cart),
        CategoryIconOption("cafe", LedgerIcons.Coffee),
        CategoryIconOption("restaurant", LedgerIcons.Utensils),
        CategoryIconOption("fruit", LedgerIcons.Apple),
        CategoryIconOption("drinks", LedgerIcons.Wine),
        CategoryIconOption("shopping", LedgerIcons.ShoppingBag),
        CategoryIconOption("delivery", LedgerIcons.Package),
        CategoryIconOption("clothes", LedgerIcons.Shirt),
        CategoryIconOption("beauty", LedgerIcons.Scissors),
        CategoryIconOption("care", LedgerIcons.Sparkles),
        CategoryIconOption("jewelry", LedgerIcons.Gem),
        // Transport & travel
        CategoryIconOption("transport", LedgerIcons.Bus),
        CategoryIconOption("car", LedgerIcons.Car),
        CategoryIconOption("fuel", LedgerIcons.Fuel),
        CategoryIconOption("parking", LedgerIcons.Parking),
        CategoryIconOption("train", LedgerIcons.Train),
        CategoryIconOption("bike", LedgerIcons.Bike),
        CategoryIconOption("travel", LedgerIcons.Plane),
        CategoryIconOption("hotel", LedgerIcons.Bed),
        CategoryIconOption("outdoors", LedgerIcons.Mountain),
        CategoryIconOption("vacation", LedgerIcons.Umbrella),
        // Home & bills
        CategoryIconOption("home", LedgerIcons.Building),
        CategoryIconOption("rent", LedgerIcons.Key),
        CategoryIconOption("utilities", LedgerIcons.Zap),
        CategoryIconOption("water", LedgerIcons.Droplet),
        CategoryIconOption("heating", LedgerIcons.Flame),
        CategoryIconOption("repair", LedgerIcons.Wrench),
        CategoryIconOption("furniture", LedgerIcons.Sofa),
        CategoryIconOption("garden", LedgerIcons.Leaf),
        CategoryIconOption("mobile", LedgerIcons.Smartphone),
        CategoryIconOption("phone", LedgerIcons.Phone),
        CategoryIconOption("internet", LedgerIcons.Wifi),
        CategoryIconOption("subs", LedgerIcons.Repeat),
        CategoryIconOption("cloud", LedgerIcons.Cloud),
        CategoryIconOption("software", LedgerIcons.Code),
        CategoryIconOption("taxes", LedgerIcons.Receipt),
        CategoryIconOption("insurance", LedgerIcons.Shield),
        // Leisure, health, family
        CategoryIconOption("fun", LedgerIcons.Gamepad),
        CategoryIconOption("tv", LedgerIcons.Tv),
        CategoryIconOption("movies", LedgerIcons.Film),
        CategoryIconOption("music", LedgerIcons.Music),
        CategoryIconOption("events", LedgerIcons.Ticket),
        CategoryIconOption("photo", LedgerIcons.Camera),
        CategoryIconOption("books", LedgerIcons.Book),
        CategoryIconOption("education", LedgerIcons.Study),
        CategoryIconOption("sport", LedgerIcons.Dumbbell),
        CategoryIconOption("health", LedgerIcons.Health),
        CategoryIconOption("pharmacy", LedgerIcons.Pill),
        CategoryIconOption("kids", LedgerIcons.Baby),
        CategoryIconOption("pets", LedgerIcons.PawPrint),
        CategoryIconOption("family", LedgerIcons.Users),
        CategoryIconOption("charity", LedgerIcons.Heart),
        CategoryIconOption("celebration", LedgerIcons.Cake),
        CategoryIconOption("gift", LedgerIcons.Gift),
        // Money
        CategoryIconOption("salary", LedgerIcons.Briefcase),
        CategoryIconOption("freelance", LedgerIcons.Laptop),
        CategoryIconOption("bonus", LedgerIcons.Trophy),
        CategoryIconOption("investments", LedgerIcons.TrendingUp),
        CategoryIconOption("savings", LedgerIcons.PiggyBank),
        CategoryIconOption("interest", LedgerIcons.Percent),
        CategoryIconOption("coins", LedgerIcons.Coins),
        CategoryIconOption("cash", LedgerIcons.Cash),
        CategoryIconOption("card", LedgerIcons.Card),
        CategoryIconOption("wallet", LedgerIcons.Wallet),
        CategoryIconOption("transfer", LedgerIcons.Transfer),
        CategoryIconOption("star", LedgerIcons.Star),
        CategoryIconOption("tag", LedgerIcons.Tag)
    )
}

private val ICON_BY_CODE: Map<String, ImageVector> by lazy {
    CATEGORY_ICON_OPTIONS.associate { it.code to it.icon }
}

private fun iconForCode(code: String): ImageVector? = ICON_BY_CODE[code]

private fun iconForName(name: String): ImageVector? {
    val n = name.lowercase()
    fun has(vararg keys: String) = keys.any { n.contains(it) }
    // More specific rules first
    return when {
        has("перевод", "transfer") -> LedgerIcons.Transfer
        has("аптек", "лекарств", "pharm", "medicine") -> LedgerIcons.Pill
        has("ресторан", "restaurant", "обед", "lunch", "dinner") -> LedgerIcons.Utensils
        has("алкогол", "вино", "пиво", "alcohol", "wine", "beer") -> LedgerIcons.Wine
        has("фрукт", "овощ", "fruit", "vegetable") -> LedgerIcons.Apple
        has("продукт", "супермаркет", "еда", "grocer", "food") -> LedgerIcons.Cart
        has("кафе", "кофе", "cafe", "coffee") -> LedgerIcons.Coffee
        has("доставк", "маркетплейс", "озон", "ozon", "wildberries", "delivery") -> LedgerIcons.Package
        has("покупк", "шопинг", "shopping") -> LedgerIcons.ShoppingBag
        has("самолет", "самолёт", "авиа", "путешеств", "отпуск", "travel", "flight", "trip") -> LedgerIcons.Plane
        has("отел", "гостиниц", "hotel") -> LedgerIcons.Bed
        has("поезд", "ржд", "электрич", "train") -> LedgerIcons.Train
        has("велос", "самокат", "bike", "scooter") -> LedgerIcons.Bike
        has("парков", "parking") -> LedgerIcons.Parking
        has("транспорт", "такси", "метро", "автобус", "transport", "taxi") -> LedgerIcons.Bus
        has("топлив", "бенз", "азс", "fuel", "petrol") -> LedgerIcons.Fuel
        has("авто", "машин", "car") -> LedgerIcons.Car
        has("спорт", "фитнес", "трениров", "sport", "fitness", "gym") -> LedgerIcons.Dumbbell
        has("здоров", "медиц", "врач", "стомат", "health", "doctor") -> LedgerIcons.Health
        has("интернет", "internet") -> LedgerIcons.Wifi
        has("связь", "мобил", "телефон", "mobile", "phone") -> LedgerIcons.Smartphone
        has("жкх", "коммун", "электр", "utilit") -> LedgerIcons.Zap
        has("отоплен", "heating") -> LedgerIcons.Flame
        has("облак", "cloud") -> LedgerIcons.Cloud
        has("подписк", "subscr") -> LedgerIcons.Repeat
        has("одежд", "обув", "cloth") -> LedgerIcons.Shirt
        has("красот", "салон", "парикмах", "beauty", "hair") -> LedgerIcons.Scissors
        has("украшен", "ювелир", "jewel") -> LedgerIcons.Gem
        has("книг", "book") -> LedgerIcons.Book
        has("образов", "учеб", "курс", "educat", "study", "school") -> LedgerIcons.Study
        has("кино", "фильм", "movie", "cinema") -> LedgerIcons.Film
        has("музык", "music") -> LedgerIcons.Music
        has("концерт", "билет", "театр", "ticket", "concert", "theatre") -> LedgerIcons.Ticket
        has("фото", "photo") -> LedgerIcons.Camera
        has("досуг", "развлеч", "игр", "хобби", "leisure", "entertain", "game", "hobby") -> LedgerIcons.Gamepad
        has("ребен", "ребён", "детск", "дети", "kid", "child", "baby") -> LedgerIcons.Baby
        has("питом", "животн", "кошк", "собак", "pet") -> LedgerIcons.PawPrint
        has("семь", "family") -> LedgerIcons.Users
        has("благотвор", "пожертв", "charity", "donat") -> LedgerIcons.Heart
        has("день рожд", "праздн", "birthday", "party") -> LedgerIcons.Cake
        has("страхов", "insurance") -> LedgerIcons.Shield
        has("налог", "штраф", "tax") -> LedgerIcons.Receipt
        has("ремонт", "repair") -> LedgerIcons.Wrench
        has("мебел", "furnit") -> LedgerIcons.Sofa
        has("растен", "садов", "garden", "plant") -> LedgerIcons.Leaf
        has("аренд", "rent") -> LedgerIcons.Key
        has("дом", "кварт", "ипотек", "home", "hous", "mortgage") -> LedgerIcons.Building
        has("зарплат", "оклад", "salary") -> LedgerIcons.Briefcase
        has("фриланс", "подработ", "freelance") -> LedgerIcons.Laptop
        has("бонус", "преми", "bonus") -> LedgerIcons.Trophy
        has("инвест", "брокер", "invest", "stock") -> LedgerIcons.TrendingUp
        has("накоплен", "сбереж", "копил", "saving") -> LedgerIcons.PiggyBank
        has("кэшбэк", "процент", "cashback", "interest") -> LedgerIcons.Percent
        has("дивиден", "dividend") -> LedgerIcons.Coins
        has("подар", "gift") -> LedgerIcons.Gift
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
        // Title and buttons stay fixed; only the body scrolls, and the card never
        // goes under the status / gesture bars or the keyboard.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clip(shape)
                .background(c.surface)
                .border(1.dp, c.border, shape)
                .padding(vertical = 22.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = c.text,
                modifier = Modifier.padding(horizontal = 22.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
            if (dismissText != null || (confirmText != null && onConfirm != null)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
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
