package dev.chandradsl.m3c.runtime.renderer.renderers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.store.WorkspaceIntent
import dev.chandradsl.m3c.core.domain.store.WorkspaceState
import dev.chandradsl.m3c.runtime.renderer.decorator.SelectionDecorator
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeColor
import dev.chandradsl.m3c.runtime.renderer.mapper.toComposeModifier

val StandardMaterialIcons: List<String> = listOf(
    "Favorite",
    "Home",
    "Search",
    "Person",
    "Menu",
    "Add",
    "Close",
    "Settings",
    "Check",
    "Edit",
    "Delete",
    "Star",
    "Share",
    "Notifications",
    "Info",
    "Warning",
    "MoreVert",
    "Refresh",
    "Email",
    "Phone",
    "ShoppingCart",
    "ThumbUp",
    "AccountCircle",
    "Build",
    "Done",
    "Lock",
    "LocationOn",
    "ArrowBack",
    "ArrowForward",
    "Send"
)

private val StaticMaterialIconMap: Map<String, ImageVector> = mapOf(
    "favorite" to Icons.Filled.Favorite,
    "home" to Icons.Filled.Home,
    "search" to Icons.Filled.Search,
    "person" to Icons.Filled.Person,
    "menu" to Icons.Filled.Menu,
    "add" to Icons.Filled.Add,
    "close" to Icons.Filled.Close,
    "settings" to Icons.Filled.Settings,
    "check" to Icons.Filled.Check,
    "edit" to Icons.Filled.Edit,
    "delete" to Icons.Filled.Delete,
    "star" to Icons.Filled.Star,
    "share" to Icons.Filled.Share,
    "notifications" to Icons.Filled.Notifications,
    "info" to Icons.Filled.Info,
    "warning" to Icons.Filled.Warning,
    "morevert" to Icons.Filled.MoreVert,
    "refresh" to Icons.Filled.Refresh,
    "email" to Icons.Filled.Email,
    "phone" to Icons.Filled.Phone,
    "shoppingcart" to Icons.Filled.ShoppingCart,
    "thumbup" to Icons.Filled.ThumbUp,
    "accountcircle" to Icons.Filled.AccountCircle,
    "build" to Icons.Filled.Build,
    "done" to Icons.Filled.Done,
    "lock" to Icons.Filled.Lock,
    "locationon" to Icons.Filled.LocationOn,
    "arrowback" to Icons.AutoMirrored.Filled.ArrowBack,
    "arrowforward" to Icons.AutoMirrored.Filled.ArrowForward,
    "send" to Icons.AutoMirrored.Filled.Send
)

private val resolvedIconCache = mutableMapOf<String, ImageVector>()

fun resolveMaterialIcon(iconName: String): ImageVector {
    val clean = iconName.trim()
    resolvedIconCache[clean]?.let { return it }

    val resolved = StaticMaterialIconMap[clean.lowercase()]
        ?: tryResolveReflection(clean)
        ?: Icons.Filled.Star

    resolvedIconCache[clean] = resolved
    return resolved
}

private fun tryResolveReflection(name: String): ImageVector? {
    return try {
        val filledClass = Class.forName("androidx.compose.material.icons.filled." + name + "Kt")
        val getter = filledClass.getMethod("get" + name, Icons.Filled::class.java)
        getter.invoke(null, Icons.Filled) as? ImageVector
    } catch (_: Throwable) {
        try {
            val mirroredClass = Class.forName("androidx.compose.material.icons.automirrored.filled." + name + "Kt")
            val getter = mirroredClass.getMethod("get" + name, Icons.AutoMirrored.Filled::class.java)
            getter.invoke(null, Icons.AutoMirrored.Filled) as? ImageVector
        } catch (_: Throwable) {
            null
        }
    }
}

@Composable
fun RenderIcon(
    node: ComposableNode.IconNode,
    state: WorkspaceState,
    onIntent: (WorkspaceIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageVector = resolveMaterialIcon(node.iconName)
    val tint = node.tint?.toComposeColor() ?: LocalContentColor.current

    SelectionDecorator(
        nodeId = node.id,
        nodeTag = "Icon",
        isSelected = state.selectedNodeId == node.id,
        onSelect = { onIntent(WorkspaceIntent.SelectNode(it)) },
        modifier = modifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = node.contentDescription,
            modifier = node.modifiers.toComposeModifier(),
            tint = tint
        )
    }
}
