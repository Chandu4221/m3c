package dev.chandradsl.m3c.core.domain.model

enum class IconCategory(val displayName: String) {
    All("All"),
    Navigation("Navigation"),
    Action("Action"),
    Communication("Communication"),
    Content("Content"),
    Alert("Alert"),
    Media("Media"),
    Social("Social"),
    Places("Places")
}

data class MaterialIconEntry(
    val name: String,
    val category: IconCategory,
    val isAutoMirrored: Boolean = false,
    val keywords: List<String> = emptyList()
)

object MaterialIconCatalog {

    val icons: List<MaterialIconEntry> = listOf(
        // ==========================================
        // 1. Navigation
        // ==========================================
        MaterialIconEntry("Home", IconCategory.Navigation, keywords = listOf("house", "main", "start", "dashboard")),
        MaterialIconEntry("Menu", IconCategory.Navigation, keywords = listOf("hamburger", "navigation", "drawer", "bars", "options")),
        MaterialIconEntry("ArrowBack", IconCategory.Navigation, isAutoMirrored = true, keywords = listOf("previous", "left", "return", "backwards")),
        MaterialIconEntry("ArrowForward", IconCategory.Navigation, isAutoMirrored = true, keywords = listOf("next", "right", "continue", "forward")),
        MaterialIconEntry("Close", IconCategory.Navigation, keywords = listOf("cancel", "dismiss", "x", "exit", "remove")),
        MaterialIconEntry("MoreVert", IconCategory.Navigation, keywords = listOf("overflow", "dots", "menu", "kebab", "more")),
        MaterialIconEntry("Refresh", IconCategory.Navigation, keywords = listOf("reload", "sync", "update", "restart")),
        MaterialIconEntry("ArrowDropDown", IconCategory.Navigation, keywords = listOf("expand", "down", "chevron", "triangle")),
        MaterialIconEntry("ArrowDropUp", IconCategory.Navigation, keywords = listOf("collapse", "up", "chevron")),
        MaterialIconEntry("ChevronLeft", IconCategory.Navigation, isAutoMirrored = true, keywords = listOf("back", "caret")),
        MaterialIconEntry("ChevronRight", IconCategory.Navigation, isAutoMirrored = true, keywords = listOf("forward", "caret", "next")),
        MaterialIconEntry("ExpandMore", IconCategory.Navigation, keywords = listOf("down", "accordion", "open")),
        MaterialIconEntry("ExpandLess", IconCategory.Navigation, keywords = listOf("up", "accordion", "close")),
        MaterialIconEntry("Fullscreen", IconCategory.Navigation, keywords = listOf("expand", "maximize", "zoom")),
        MaterialIconEntry("FullscreenExit", IconCategory.Navigation, keywords = listOf("shrink", "minimize")),
        MaterialIconEntry("Apps", IconCategory.Navigation, keywords = listOf("grid", "launcher", "dots")),
        MaterialIconEntry("UnfoldMore", IconCategory.Navigation, keywords = listOf("sort", "expand", "arrows")),
        MaterialIconEntry("UnfoldLess", IconCategory.Navigation, keywords = listOf("collapse", "shrink")),

        // ==========================================
        // 2. Action
        // ==========================================
        MaterialIconEntry("Search", IconCategory.Action, keywords = listOf("find", "magnifier", "lookup", "explore")),
        MaterialIconEntry("Settings", IconCategory.Action, keywords = listOf("gear", "cog", "preferences", "config", "options")),
        MaterialIconEntry("Favorite", IconCategory.Action, keywords = listOf("heart", "like", "love", "bookmark")),
        MaterialIconEntry("Check", IconCategory.Action, keywords = listOf("done", "tick", "confirm", "ok", "success")),
        MaterialIconEntry("Done", IconCategory.Action, keywords = listOf("complete", "finished", "check", "tick")),
        MaterialIconEntry("Delete", IconCategory.Action, keywords = listOf("trash", "bin", "remove", "garbage")),
        MaterialIconEntry("Edit", IconCategory.Action, keywords = listOf("pencil", "write", "modify", "rename")),
        MaterialIconEntry("Star", IconCategory.Action, keywords = listOf("rating", "favorite", "bookmark", "score")),
        MaterialIconEntry("Lock", IconCategory.Action, keywords = listOf("secure", "password", "private", "padlock")),
        MaterialIconEntry("LockOpen", IconCategory.Action, keywords = listOf("unlock", "open", "insecure")),
        MaterialIconEntry("AccountCircle", IconCategory.Action, keywords = listOf("user", "profile", "avatar", "person")),
        MaterialIconEntry("Build", IconCategory.Action, keywords = listOf("wrench", "tool", "repair", "fix")),
        MaterialIconEntry("ShoppingCart", IconCategory.Action, keywords = listOf("cart", "buy", "ecommerce", "store", "shop")),
        MaterialIconEntry("ThumbUp", IconCategory.Action, keywords = listOf("like", "upvote", "approve", "yes")),
        MaterialIconEntry("ThumbDown", IconCategory.Action, keywords = listOf("dislike", "downvote", "reject", "no")),
        MaterialIconEntry("Info", IconCategory.Action, keywords = listOf("about", "help", "detail", "information")),
        MaterialIconEntry("Help", IconCategory.Action, isAutoMirrored = true, keywords = listOf("question", "faq", "support")),
        MaterialIconEntry("Visibility", IconCategory.Action, keywords = listOf("eye", "view", "show", "preview")),
        MaterialIconEntry("VisibilityOff", IconCategory.Action, keywords = listOf("eye", "hide", "password", "hidden")),
        MaterialIconEntry("CheckCircle", IconCategory.Action, keywords = listOf("success", "verified", "ok", "done")),
        MaterialIconEntry("ExitToApp", IconCategory.Action, isAutoMirrored = true, keywords = listOf("logout", "leave", "signout")),
        MaterialIconEntry("OpenInNew", IconCategory.Action, isAutoMirrored = true, keywords = listOf("external", "link", "window", "tab")),
        MaterialIconEntry("Fingerprint", IconCategory.Action, keywords = listOf("biometric", "auth", "security")),
        MaterialIconEntry("Schedule", IconCategory.Action, keywords = listOf("clock", "time", "history", "recent")),
        MaterialIconEntry("Bookmark", IconCategory.Action, keywords = listOf("save", "read later", "favorite")),
        MaterialIconEntry("BookmarkBorder", IconCategory.Action, keywords = listOf("save", "unsaved", "outline")),
        MaterialIconEntry("Lightbulb", IconCategory.Action, keywords = listOf("idea", "hint", "tip", "insight")),
        MaterialIconEntry("PowerSettingsNew", IconCategory.Action, keywords = listOf("power", "shutdown", "on", "off")),

        // ==========================================
        // 3. Communication
        // ==========================================
        MaterialIconEntry("Email", IconCategory.Communication, keywords = listOf("mail", "envelope", "inbox", "letter", "message")),
        MaterialIconEntry("Phone", IconCategory.Communication, keywords = listOf("call", "telephone", "mobile", "contact")),
        MaterialIconEntry("Share", IconCategory.Communication, keywords = listOf("social", "export", "send to", "link")),
        MaterialIconEntry("Send", IconCategory.Communication, isAutoMirrored = true, keywords = listOf("paper plane", "submit", "deliver", "chat")),
        MaterialIconEntry("Chat", IconCategory.Communication, isAutoMirrored = true, keywords = listOf("message", "bubble", "talk", "sms")),
        MaterialIconEntry("Call", IconCategory.Communication, keywords = listOf("phone", "dial", "ring")),
        MaterialIconEntry("MailOutline", IconCategory.Communication, keywords = listOf("email", "unread")),
        MaterialIconEntry("Forum", IconCategory.Communication, keywords = listOf("discussion", "community", "comments")),
        MaterialIconEntry("ContactMail", IconCategory.Communication, keywords = listOf("address", "contact card", "vcard")),
        MaterialIconEntry("ContactPhone", IconCategory.Communication, keywords = listOf("phonebook", "contacts")),
        MaterialIconEntry("VpnKey", IconCategory.Communication, keywords = listOf("key", "secret", "access")),
        MaterialIconEntry("LocationOn", IconCategory.Communication, keywords = listOf("pin", "map", "gps", "coordinates")),

        // ==========================================
        // 4. Content
        // ==========================================
        MaterialIconEntry("Add", IconCategory.Content, keywords = listOf("plus", "new", "create", "insert")),
        MaterialIconEntry("AddCircle", IconCategory.Content, keywords = listOf("plus", "badge", "create")),
        MaterialIconEntry("Create", IconCategory.Content, keywords = listOf("pencil", "edit", "compose", "write")),
        MaterialIconEntry("ContentCopy", IconCategory.Content, keywords = listOf("copy", "duplicate", "clone", "clipboard")),
        MaterialIconEntry("ContentCut", IconCategory.Content, keywords = listOf("cut", "scissors", "snip")),
        MaterialIconEntry("ContentPaste", IconCategory.Content, keywords = listOf("paste", "clipboard", "insert")),
        MaterialIconEntry("Drafts", IconCategory.Content, keywords = listOf("envelope", "unsent", "mail")),
        MaterialIconEntry("Save", IconCategory.Content, keywords = listOf("floppy", "disk", "store", "record")),
        MaterialIconEntry("FilterList", IconCategory.Content, keywords = listOf("filter", "funnel", "refine")),
        MaterialIconEntry("Sort", IconCategory.Content, isAutoMirrored = true, keywords = listOf("order", "arrange", "alphabetical")),
        MaterialIconEntry("Clear", IconCategory.Content, keywords = listOf("remove", "cross", "delete", "erase")),
        MaterialIconEntry("Archive", IconCategory.Content, keywords = listOf("box", "store", "compress")),
        MaterialIconEntry("Unarchive", IconCategory.Content, keywords = listOf("restore", "unpack")),
        MaterialIconEntry("Inventory", IconCategory.Content, keywords = listOf("stock", "warehouse", "items")),

        // ==========================================
        // 5. Alert & Feedback
        // ==========================================
        MaterialIconEntry("Notifications", IconCategory.Alert, keywords = listOf("bell", "alarm", "alert", "reminder")),
        MaterialIconEntry("NotificationsNone", IconCategory.Alert, keywords = listOf("bell", "silent", "muted")),
        MaterialIconEntry("Warning", IconCategory.Alert, keywords = listOf("caution", "triangle", "danger", "alert", "problem")),
        MaterialIconEntry("Error", IconCategory.Alert, keywords = listOf("problem", "danger", "stop", "bad", "failure")),
        MaterialIconEntry("ErrorOutline", IconCategory.Alert, keywords = listOf("circle", "alert", "exclamation")),
        MaterialIconEntry("PriorityHigh", IconCategory.Alert, keywords = listOf("important", "urgent", "exclamation")),
        MaterialIconEntry("Feedback", IconCategory.Alert, keywords = listOf("comment", "report", "bug", "opinion")),
        MaterialIconEntry("NotificationImportant", IconCategory.Alert, keywords = listOf("urgent", "badge", "bell")),

        // ==========================================
        // 6. Media
        // ==========================================
        MaterialIconEntry("PlayArrow", IconCategory.Media, keywords = listOf("start", "run", "video", "music", "audio")),
        MaterialIconEntry("Pause", IconCategory.Media, keywords = listOf("halt", "hold", "break")),
        MaterialIconEntry("Stop", IconCategory.Media, keywords = listOf("end", "square")),
        MaterialIconEntry("FastForward", IconCategory.Media, keywords = listOf("skip", "ahead")),
        MaterialIconEntry("FastRewind", IconCategory.Media, keywords = listOf("back", "rewind")),
        MaterialIconEntry("VolumeUp", IconCategory.Media, isAutoMirrored = true, keywords = listOf("speaker", "sound", "audio", "loud")),
        MaterialIconEntry("VolumeDown", IconCategory.Media, isAutoMirrored = true, keywords = listOf("speaker", "quiet", "lower")),
        MaterialIconEntry("VolumeMute", IconCategory.Media, isAutoMirrored = true, keywords = listOf("silent", "speaker")),
        MaterialIconEntry("VolumeOff", IconCategory.Media, isAutoMirrored = true, keywords = listOf("no sound", "mute")),
        MaterialIconEntry("Mic", IconCategory.Media, keywords = listOf("microphone", "voice", "record", "audio")),
        MaterialIconEntry("MicOff", IconCategory.Media, keywords = listOf("mute", "microphone")),
        MaterialIconEntry("Videocam", IconCategory.Media, keywords = listOf("camera", "video", "recording", "call")),
        MaterialIconEntry("MusicNote", IconCategory.Media, keywords = listOf("song", "audio", "tune")),
        MaterialIconEntry("CameraAlt", IconCategory.Media, keywords = listOf("photo", "picture", "snapshot")),
        MaterialIconEntry("Image", IconCategory.Media, keywords = listOf("photo", "gallery", "picture", "wallpaper")),

        // ==========================================
        // 7. Social
        // ==========================================
        MaterialIconEntry("Person", IconCategory.Social, keywords = listOf("user", "account", "profile", "individual", "human")),
        MaterialIconEntry("PersonAdd", IconCategory.Social, keywords = listOf("user", "invite", "add friend")),
        MaterialIconEntry("Group", IconCategory.Social, keywords = listOf("team", "people", "users", "community")),
        MaterialIconEntry("GroupAdd", IconCategory.Social, keywords = listOf("invite", "team")),
        MaterialIconEntry("Public", IconCategory.Social, keywords = listOf("globe", "world", "earth", "internet", "web")),
        MaterialIconEntry("Mood", IconCategory.Social, keywords = listOf("smile", "happy", "face", "emoji")),
        MaterialIconEntry("MoodBad", IconCategory.Social, keywords = listOf("frown", "sad", "unhappy")),
        MaterialIconEntry("Share", IconCategory.Social, keywords = listOf("send to", "forward", "network")),
        MaterialIconEntry("ThumbUp", IconCategory.Social, keywords = listOf("like", "praise", "agree")),

        // ==========================================
        // 8. Places & Hardware
        // ==========================================
        MaterialIconEntry("Place", IconCategory.Places, keywords = listOf("location", "pin", "destination", "geo")),
        MaterialIconEntry("Navigation", IconCategory.Places, keywords = listOf("compass", "arrow", "direction")),
        MaterialIconEntry("Map", IconCategory.Places, keywords = listOf("directions", "gps", "travel")),
        MaterialIconEntry("Computer", IconCategory.Places, keywords = listOf("laptop", "pc", "desktop", "monitor")),
        MaterialIconEntry("Smartphone", IconCategory.Places, keywords = listOf("phone", "mobile", "device", "android")),
        MaterialIconEntry("Tablet", IconCategory.Places, keywords = listOf("ipad", "device", "screen")),
        MaterialIconEntry("Keyboard", IconCategory.Places, keywords = listOf("typing", "input", "keys"))
    )

    private val nameIndex: Map<String, MaterialIconEntry> = icons
        .associateBy { it.name.lowercase() }

    fun find(name: String): MaterialIconEntry? {
        val clean = name.trim().lowercase()
        return nameIndex[clean]
    }

    fun search(query: String, category: IconCategory? = null): List<MaterialIconEntry> {
        val cleanQuery = query.trim().lowercase()
        return icons.filter { entry ->
            val matchesCategory = category == null || category == IconCategory.All || entry.category == category
            if (!matchesCategory) return@filter false

            if (cleanQuery.isEmpty()) {
                true
            } else {
                entry.name.lowercase().contains(cleanQuery) ||
                        entry.keywords.any { it.contains(cleanQuery) }
            }
        }
    }
}
