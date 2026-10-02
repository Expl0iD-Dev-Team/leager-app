package com.ledger.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// Line icons from the Figma redesign (Lucide-style: 24x24 viewport, 2px round stroke).
// Generated from SVG; the tint is applied by Icon(tint = ...).
private fun lineIcon(name: String, vararg paths: String): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    )
    paths.forEach { d ->
        builder.addPath(
            pathData = addPathNodes(d),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
    return builder.build()
}

object LedgerIcons {
    val Home: ImageVector by lazy {
        lineIcon(
            "Home",
            "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8",
            "M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
        )
    }

    val List: ImageVector by lazy {
        lineIcon(
            "List",
            "M3 6h.01M3 12h.01M3 18h.01M8 6h13M8 12h13M8 18h13"
        )
    }

    val Plus: ImageVector by lazy {
        lineIcon(
            "Plus",
            "M5 12h14M12 5v14"
        )
    }

    val PieChart: ImageVector by lazy {
        lineIcon(
            "PieChart",
            "M21 12c.552 0 1.005-.449.95-.998a10 10 0 0 0-8.953-8.951c-.55-.055-.998.398-.998.95v8a1 1 0 0 0 1 1z",
            "M21.21 15.89A10 10 0 1 1 8 2.83"
        )
    }

    val Grid: ImageVector by lazy {
        lineIcon(
            "Grid",
            "M4 3h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1z",
            "M15 3h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1z",
            "M15 14h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1z",
            "M4 14h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1z"
        )
    }

    val Cart: ImageVector by lazy {
        lineIcon(
            "Cart",
            "M7 21a1 1 0 1 0 2 0a1 1 0 1 0 -2 0",
            "M18 21a1 1 0 1 0 2 0a1 1 0 1 0 -2 0",
            "M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12"
        )
    }

    val Coffee: ImageVector by lazy {
        lineIcon(
            "Coffee",
            "M10 2v2M14 2v2M6 2v2M16 8a1 1 0 0 1 1 1v8a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4V9a1 1 0 0 1 1-1h14a4 4 0 1 1 0 8h-1"
        )
    }

    val Bus: ImageVector by lazy {
        lineIcon(
            "Bus",
            "M8 6v6M15 6v6M2 12h19.6M18 18h3s.5-1.7.8-2.8c.1-.4.2-.8.2-1.2 0-.4-.1-.8-.2-1.2l-1.4-5C20.1 6.8 19.1 6 18 6H4a2 2 0 0 0-2 2v10h3",
            "M5 18a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
            "M9 18h5",
            "M14 18a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"
        )
    }

    val Health: ImageVector by lazy {
        lineIcon(
            "Health",
            "M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z",
            "M3.22 12H9.5l.5-1 2 4.5 2-7 1.5 3.5h5.27"
        )
    }

    val Gamepad: ImageVector by lazy {
        lineIcon(
            "Gamepad",
            "M6 11h4M8 9v4M15 12h.01M18 10h.01",
            "M17.32 5H6.68a4 4 0 0 0-3.978 3.59C2.604 9.416 2 14.456 2 16a3 3 0 0 0 3 3c1 0 1.5-.5 2-1l1.414-1.414A2 2 0 0 1 9.828 16h4.344a2 2 0 0 1 1.414.586L17 18c.5.5 1 1 2 1a3 3 0 0 0 3-3c0-1.545-.604-6.584-.685-7.258A4 4 0 0 0 17.32 5z"
        )
    }

    val Shirt: ImageVector by lazy {
        lineIcon(
            "Shirt",
            "M20.38 3.46 16 2a4 4 0 0 1-8 0L3.62 3.46a2 2 0 0 0-1.34 2.23l.58 3.47a1 1 0 0 0 .99.84H6v10c0 1.1.9 2 2 2h8a2 2 0 0 0 2-2V10h2.15a1 1 0 0 0 .99-.84l.58-3.47a2 2 0 0 0-1.34-2.23z"
        )
    }

    val Repeat: ImageVector by lazy {
        lineIcon(
            "Repeat",
            "m17 2 4 4-4 4",
            "M3 11v-1a4 4 0 0 1 4-4h14",
            "m7 22-4-4 4-4",
            "M21 13v1a4 4 0 0 1-4 4H3"
        )
    }

    val Zap: ImageVector by lazy {
        lineIcon(
            "Zap",
            "M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z"
        )
    }

    val Fuel: ImageVector by lazy {
        lineIcon(
            "Fuel",
            "M3 22h12M4 9h10M14 22V4a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v18",
            "M14 13h2a2 2 0 0 1 2 2v2a2 2 0 0 0 4 0V9.83a2 2 0 0 0-.59-1.42L18 5"
        )
    }

    val Car: ImageVector by lazy {
        lineIcon(
            "Car",
            "M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2",
            "M5 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
            "M9 17h6",
            "M15 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"
        )
    }

    val Study: ImageVector by lazy {
        lineIcon(
            "Study",
            "M21.42 10.922a1 1 0 0 0-.019-1.838L12.83 5.18a2 2 0 0 0-1.66 0L2.6 9.08a1 1 0 0 0 0 1.832l8.57 3.908a2 2 0 0 0 1.66 0z",
            "M22 10v6M6 12.5V16a6 3 0 0 0 12 0v-3.5"
        )
    }

    val Building: ImageVector by lazy {
        lineIcon(
            "Building",
            "M10 18v-7M14 18v-7M18 18v-7M6 18v-7M3 22h18M12 2l8 5H4z"
        )
    }

    val Briefcase: ImageVector by lazy {
        lineIcon(
            "Briefcase",
            "M16 20V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16",
            "M4 6h16a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2z"
        )
    }

    val Laptop: ImageVector by lazy {
        lineIcon(
            "Laptop",
            "M20 16V7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v9m16 0H4m16 0 1.28 2.55a1 1 0 0 1-.9 1.45H3.62a1 1 0 0 1-.9-1.45L4 16"
        )
    }

    val Gift: ImageVector by lazy {
        lineIcon(
            "Gift",
            "M4 8h16a1 1 0 0 1 1 1v2a1 1 0 0 1 -1 1h-16a1 1 0 0 1 -1 -1v-2a1 1 0 0 1 1 -1z",
            "M12 8v13M19 12v7a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-7",
            "M7.5 8a2.5 2.5 0 0 1 0-5A4.8 8 0 0 1 12 8a4.8 8 0 0 1 4.5-5 2.5 2.5 0 0 1 0 5"
        )
    }

    val Transfer: ImageVector by lazy {
        lineIcon(
            "Transfer",
            "M8 3 4 7l4 4M4 7h16M16 21l4-4-4-4M20 17H4"
        )
    }

    val Search: ImageVector by lazy {
        lineIcon(
            "Search",
            "M3 11a8 8 0 1 0 16 0a8 8 0 1 0 -16 0",
            "m21 21-4.3-4.3"
        )
    }

    val Calendar: ImageVector by lazy {
        lineIcon(
            "Calendar",
            "M8 2v4M16 2v4",
            "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2z",
            "M3 10h18"
        )
    }

    val ChevronLeft: ImageVector by lazy {
        lineIcon(
            "ChevronLeft",
            "m15 18-6-6 6-6"
        )
    }

    val ChevronRight: ImageVector by lazy {
        lineIcon(
            "ChevronRight",
            "m9 18 6-6-6-6"
        )
    }

    val ChevronDown: ImageVector by lazy {
        lineIcon(
            "ChevronDown",
            "m6 9 6 6 6-6"
        )
    }

    val Eye: ImageVector by lazy {
        lineIcon(
            "Eye",
            "M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0",
            "M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0"
        )
    }

    val Card: ImageVector by lazy {
        lineIcon(
            "Card",
            "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2z",
            "M2 10h20"
        )
    }

    val Wallet: ImageVector by lazy {
        lineIcon(
            "Wallet",
            "M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1",
            "M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4"
        )
    }

    val Cash: ImageVector by lazy {
        lineIcon(
            "Cash",
            "M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-8a2 2 0 0 1 2 -2z",
            "M10 12a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
            "M6 12h.01M18 12h.01"
        )
    }

    val Sliders: ImageVector by lazy {
        lineIcon(
            "Sliders",
            "M21 4h-7M10 4H3M21 12h-9M8 12H3M21 20h-5M12 20H3M14 2v4M8 10v4M16 18v4"
        )
    }

    val Lock: ImageVector by lazy {
        lineIcon(
            "Lock",
            "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-7a2 2 0 0 1 2 -2z",
            "M7 11V7a5 5 0 0 1 10 0v4"
        )
    }

    val Fingerprint: ImageVector by lazy {
        lineIcon(
            "Fingerprint",
            "M12 10a2 2 0 0 0-2 2c0 1.02-.1 2.51-.26 4M14 13.12c0 2.38 0 6.38-1 8.88M17.29 21.02c.12-.6.43-2.3.5-3.02M2 12a10 10 0 0 1 18-6M2 16h.01M21.8 16c.2-2 .131-5.354 0-6M5 19.5C5.5 18 6 15 6 12a6 6 0 0 1 .34-2M8.65 22c.21-.66.45-1.32.57-2M9 6.8a6 6 0 0 1 9 5.2v2"
        )
    }

    val Upload: ImageVector by lazy {
        lineIcon(
            "Upload",
            "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"
        )
    }

    val Download: ImageVector by lazy {
        lineIcon(
            "Download",
            "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"
        )
    }

    val FileText: ImageVector by lazy {
        lineIcon(
            "FileText",
            "M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z",
            "M14 2v4a2 2 0 0 0 2 2h4M10 9H8M16 13H8M16 17H8"
        )
    }

    val Trash: ImageVector by lazy {
        lineIcon(
            "Trash",
            "M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"
        )
    }

    val Sun: ImageVector by lazy {
        lineIcon(
            "Sun",
            "M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0",
            "M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41"
        )
    }

    val Moon: ImageVector by lazy {
        lineIcon(
            "Moon",
            "M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"
        )
    }

    val Monitor: ImageVector by lazy {
        lineIcon(
            "Monitor",
            "M4 3h16a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2z",
            "M8 21h8M12 17v4"
        )
    }

    val Tag: ImageVector by lazy {
        lineIcon(
            "Tag",
            "M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z",
            "M6.5 7.5a1 1 0 1 0 2 0a1 1 0 1 0 -2 0"
        )
    }

    val Close: ImageVector by lazy {
        lineIcon(
            "Close",
            "M18 6 6 18M6 6l12 12"
        )
    }

    val Check: ImageVector by lazy {
        lineIcon(
            "Check",
            "M20 6 9 17l-5-5"
        )
    }

    val Pencil: ImageVector by lazy {
        lineIcon(
            "Pencil",
            "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"
        )
    }

    val Copy: ImageVector by lazy {
        lineIcon(
            "Copy",
            "M10 8h10a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2z",
            "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"
        )
    }

    val ArrowIn: ImageVector by lazy {
        lineIcon(
            "ArrowIn",
            "M17 7 7 17M17 17H7V7"
        )
    }

    val ArrowOut: ImageVector by lazy {
        lineIcon(
            "ArrowOut",
            "M7 7h10v10M7 17 17 7"
        )
    }

    val ArrowLeft: ImageVector by lazy {
        lineIcon(
            "ArrowLeft",
            "m12 19-7-7 7-7M19 12H5"
        )
    }

    val Backspace: ImageVector by lazy {
        lineIcon(
            "Backspace",
            "M10 5a2 2 0 0 0-1.344.519l-6.328 5.74a1 1 0 0 0 0 1.481l6.328 5.741A2 2 0 0 0 10 19h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2z",
            "m12 9 6 6M18 9l-6 6"
        )
    }

    val Globe: ImageVector by lazy {
        lineIcon(
            "Globe",
            "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0",
            "M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20M2 12h20"
        )
    }

    val Coins: ImageVector by lazy {
        lineIcon(
            "Coins",
            "M2 8a6 6 0 1 0 12 0a6 6 0 1 0 -12 0",
            "M18.09 10.37A6 6 0 1 1 10.34 18M7 6h1v4M16.71 13.88l.7.71-2.82 2.82"
        )
    }

    val Info: ImageVector by lazy {
        lineIcon(
            "Info",
            "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0",
            "M12 16v-4M12 8h.01"
        )
    }

    val User: ImageVector by lazy {
        lineIcon(
            "User",
            "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2",
            "M8 7a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"
        )
    }

    val Inbox: ImageVector by lazy {
        lineIcon(
            "Inbox",
            "M22 12h-6l-2 3h-4l-2-3H2",
            "M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"
        )
    }
}
