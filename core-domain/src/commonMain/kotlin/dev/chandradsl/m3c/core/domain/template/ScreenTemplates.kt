package dev.chandradsl.m3c.core.domain.template

import dev.chandradsl.m3c.core.domain.model.AlignmentDef
import dev.chandradsl.m3c.core.domain.model.AlignmentHorizontalDef
import dev.chandradsl.m3c.core.domain.model.AlignmentVerticalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementHorizontalDef
import dev.chandradsl.m3c.core.domain.model.ArrangementVerticalDef
import dev.chandradsl.m3c.core.domain.model.BorderDef
import dev.chandradsl.m3c.core.domain.model.ColorSource
import dev.chandradsl.m3c.core.domain.model.ColorToken
import dev.chandradsl.m3c.core.domain.model.ComposableNode
import dev.chandradsl.m3c.core.domain.model.DpVal
import dev.chandradsl.m3c.core.domain.model.ModifierDef
import dev.chandradsl.m3c.core.domain.model.NodeId
import dev.chandradsl.m3c.core.domain.model.ShapeDef
import dev.chandradsl.m3c.core.domain.model.ShapeToken
import dev.chandradsl.m3c.core.domain.model.TypographyToken

enum class TemplateCategory(val displayName: String) {
    Auth("Authentication"),
    Social("Social & User"),
    Settings("Preferences & Settings"),
    Content("Content & Media"),
    Commerce("E-Commerce & Orders")
}

data class ScreenTemplate(
    val id: String,
    val name: String,
    val defaultRoute: String,
    val description: String,
    val category: TemplateCategory,
    val iconName: String,
    val createRoot: () -> ComposableNode
)

object ScreenTemplates {

    val Login = ScreenTemplate(
        id = "template_login",
        name = "Login",
        defaultRoute = "login",
        description = "Sign-in screen featuring email/password fields with icons, remember me checkbox, and primary action buttons.",
        category = TemplateCategory.Auth,
        iconName = "Lock",
        createRoot = {
            ComposableNode.ScaffoldNode(
                id = NodeId.generate("scaffold"),
                topBar = ComposableNode.TopAppBarNode(
                    id = NodeId.generate("top_bar"),
                    title = ComposableNode.TextNode(
                        id = NodeId.generate("txt_title"),
                        text = "Sign In",
                        typography = TypographyToken.TitleLarge
                    ),
                    containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
                ),
                content = ComposableNode.ColumnNode(
                    id = NodeId.generate("col_content"),
                    modifiers = listOf(
                        ModifierDef.FillMaxWidth(),
                        ModifierDef.Padding(
                            start = DpVal(24f),
                            top = DpVal(24f),
                            end = DpVal(24f),
                            bottom = DpVal(24f)
                        )
                    ),
                    verticalArrangement = ArrangementVerticalDef.Top,
                    horizontalAlignment = AlignmentHorizontalDef.Start,
                    children = listOf(
                        ComposableNode.TextNode(
                            id = NodeId.generate("txt_headline"),
                            text = "Welcome Back",
                            typography = TypographyToken.HeadlineMedium,
                            color = ColorSource.Theme(ColorToken.Primary)
                        ),
                        ComposableNode.TextNode(
                            id = NodeId.generate("txt_sub"),
                            text = "Enter your credentials to continue",
                            typography = TypographyToken.BodyMedium,
                            color = ColorSource.Theme(ColorToken.OnSurfaceVariant)
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_1"),
                            modifiers = listOf(ModifierDef.Height(DpVal(16f)))
                        ),
                        ComposableNode.OutlinedTextFieldNode(
                            id = NodeId.generate("tf_email"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            label = "Email Address",
                            placeholder = "name@company.com",
                            value = "user@example.com",
                            leadingIcon = ComposableNode.IconNode(
                                id = NodeId.generate("ic_email"),
                                iconName = "Email",
                                contentDescription = "Email"
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_2"),
                            modifiers = listOf(ModifierDef.Height(DpVal(8f)))
                        ),
                        ComposableNode.OutlinedTextFieldNode(
                            id = NodeId.generate("tf_pass"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            label = "Password",
                            placeholder = "Enter your password",
                            value = "••••••••",
                            leadingIcon = ComposableNode.IconNode(
                                id = NodeId.generate("ic_lock"),
                                iconName = "Lock",
                                contentDescription = "Password"
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_3"),
                            modifiers = listOf(ModifierDef.Height(DpVal(8f)))
                        ),
                        ComposableNode.RowNode(
                            id = NodeId.generate("row_options"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                            children = listOf(
                                ComposableNode.RowNode(
                                    id = NodeId.generate("row_remember"),
                                    verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                    children = listOf(
                                        ComposableNode.CheckboxNode(
                                            id = NodeId.generate("cb_remember"),
                                            checked = true
                                        ),
                                        ComposableNode.TextNode(
                                            id = NodeId.generate("txt_remember"),
                                            text = "Remember me",
                                            typography = TypographyToken.BodySmall
                                        )
                                    )
                                ),
                                ComposableNode.TextButtonNode(
                                    id = NodeId.generate("btn_forgot"),
                                    content = listOf(
                                        ComposableNode.TextNode(
                                            id = NodeId.generate("txt_forgot"),
                                            text = "Forgot Password?",
                                            typography = TypographyToken.LabelMedium
                                        )
                                    )
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_4"),
                            modifiers = listOf(ModifierDef.Height(DpVal(20f)))
                        ),
                        ComposableNode.ButtonNode(
                            id = NodeId.generate("btn_signin"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.TextNode(
                                    id = NodeId.generate("txt_btn_signin"),
                                    text = "Sign In",
                                    typography = TypographyToken.LabelLarge
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_5"),
                            modifiers = listOf(ModifierDef.Height(DpVal(8f)))
                        ),
                        ComposableNode.OutlinedButtonNode(
                            id = NodeId.generate("btn_create"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.TextNode(
                                    id = NodeId.generate("txt_btn_create"),
                                    text = "Create Account",
                                    typography = TypographyToken.LabelLarge
                                )
                            )
                        )
                    )
                )
            )
        }
    )

    val Profile = ScreenTemplate(
        id = "template_profile",
        name = "Profile",
        defaultRoute = "profile",
        description = "User profile screen with centered avatar, follower/activity stats card, bio, and edit actions.",
        category = TemplateCategory.Social,
        iconName = "AccountCircle",
        createRoot = {
            ComposableNode.ScaffoldNode(
                id = NodeId.generate("scaffold"),
                topBar = ComposableNode.TopAppBarNode(
                    id = NodeId.generate("top_bar"),
                    title = ComposableNode.TextNode(
                        id = NodeId.generate("txt_title"),
                        text = "User Profile",
                        typography = TypographyToken.TitleLarge
                    ),
                    containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
                ),
                content = ComposableNode.ColumnNode(
                    id = NodeId.generate("col_profile"),
                    modifiers = listOf(
                        ModifierDef.FillMaxWidth(),
                        ModifierDef.Padding(
                            start = DpVal(20f),
                            top = DpVal(20f),
                            end = DpVal(20f),
                            bottom = DpVal(20f)
                        )
                    ),
                    horizontalAlignment = AlignmentHorizontalDef.CenterHorizontally,
                    children = listOf(
                        ComposableNode.SurfaceNode(
                            id = NodeId.generate("surf_avatar"),
                            modifiers = listOf(
                                ModifierDef.Size(DpVal(96f), DpVal(96f))
                            ),
                            shape = ShapeDef.Token(ShapeToken.Full),
                            color = ColorSource.Theme(ColorToken.PrimaryContainer),
                            children = listOf(
                                ComposableNode.BoxNode(
                                    id = NodeId.generate("box_avatar_inner"),
                                    contentAlignment = AlignmentDef.Center,
                                    children = listOf(
                                        ComposableNode.IconNode(
                                            id = NodeId.generate("ic_avatar"),
                                            iconName = "Person",
                                            tint = ColorSource.Theme(ColorToken.OnPrimaryContainer),
                                            contentDescription = "Profile Picture"
                                        )
                                    )
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_1"),
                            modifiers = listOf(ModifierDef.Height(DpVal(12f)))
                        ),
                        ComposableNode.TextNode(
                            id = NodeId.generate("txt_name"),
                            text = "Alex Morgan",
                            typography = TypographyToken.HeadlineSmall
                        ),
                        ComposableNode.TextNode(
                            id = NodeId.generate("txt_role"),
                            text = "Product Designer & Architect",
                            typography = TypographyToken.BodyMedium,
                            color = ColorSource.Theme(ColorToken.OnSurfaceVariant)
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_2"),
                            modifiers = listOf(ModifierDef.Height(DpVal(16f)))
                        ),
                        ComposableNode.OutlinedCardNode(
                            id = NodeId.generate("crd_stats"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.RowNode(
                                    id = NodeId.generate("row_stats"),
                                    modifiers = listOf(
                                        ModifierDef.FillMaxWidth(),
                                        ModifierDef.Padding(
                                            start = DpVal(16f),
                                            top = DpVal(12f),
                                            end = DpVal(16f),
                                            bottom = DpVal(12f)
                                        )
                                    ),
                                    horizontalArrangement = ArrangementHorizontalDef.SpaceEvenly,
                                    children = listOf(
                                        ComposableNode.ColumnNode(
                                            id = NodeId.generate("col_stat_1"),
                                            horizontalAlignment = AlignmentHorizontalDef.CenterHorizontally,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("stat_val_1"),
                                                    text = "128",
                                                    typography = TypographyToken.TitleMedium
                                                ),
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("stat_lbl_1"),
                                                    text = "Posts",
                                                    typography = TypographyToken.BodySmall,
                                                    color = ColorSource.Theme(ColorToken.OnSurfaceVariant)
                                                )
                                            )
                                        ),
                                        ComposableNode.ColumnNode(
                                            id = NodeId.generate("col_stat_2"),
                                            horizontalAlignment = AlignmentHorizontalDef.CenterHorizontally,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("stat_val_2"),
                                                    text = "14.2k",
                                                    typography = TypographyToken.TitleMedium
                                                ),
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("stat_lbl_2"),
                                                    text = "Followers",
                                                    typography = TypographyToken.BodySmall,
                                                    color = ColorSource.Theme(ColorToken.OnSurfaceVariant)
                                                )
                                            )
                                        ),
                                        ComposableNode.ColumnNode(
                                            id = NodeId.generate("col_stat_3"),
                                            horizontalAlignment = AlignmentHorizontalDef.CenterHorizontally,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("stat_val_3"),
                                                    text = "482",
                                                    typography = TypographyToken.TitleMedium
                                                ),
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("stat_lbl_3"),
                                                    text = "Following",
                                                    typography = TypographyToken.BodySmall,
                                                    color = ColorSource.Theme(ColorToken.OnSurfaceVariant)
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_3"),
                            modifiers = listOf(ModifierDef.Height(DpVal(16f)))
                        ),
                        ComposableNode.ButtonNode(
                            id = NodeId.generate("btn_edit"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.TextNode(
                                    id = NodeId.generate("txt_btn_edit"),
                                    text = "Edit Profile",
                                    typography = TypographyToken.LabelLarge
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_4"),
                            modifiers = listOf(ModifierDef.Height(DpVal(16f)))
                        ),
                        ComposableNode.ColumnNode(
                            id = NodeId.generate("col_bio"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            horizontalAlignment = AlignmentHorizontalDef.Start,
                            children = listOf(
                                ComposableNode.TextNode(
                                    id = NodeId.generate("lbl_about"),
                                    text = "About",
                                    typography = TypographyToken.TitleSmall,
                                    color = ColorSource.Theme(ColorToken.Primary)
                                ),
                                ComposableNode.SpacerNode(
                                    id = NodeId.generate("spc_bio"),
                                    modifiers = listOf(ModifierDef.Height(DpVal(4f)))
                                ),
                                ComposableNode.TextNode(
                                    id = NodeId.generate("txt_bio_body"),
                                    text = "Passionate about crafting intuitive user interfaces and building multiplatform experiences with Material 3 & Compose.",
                                    typography = TypographyToken.BodyMedium
                                )
                            )
                        )
                    )
                )
            )
        }
    )

    val Settings = ScreenTemplate(
        id = "template_settings",
        name = "Settings",
        defaultRoute = "settings",
        description = "App preferences and settings screen with theme switch, notification toggles, cards, and logout action.",
        category = TemplateCategory.Settings,
        iconName = "Settings",
        createRoot = {
            ComposableNode.ScaffoldNode(
                id = NodeId.generate("scaffold"),
                topBar = ComposableNode.TopAppBarNode(
                    id = NodeId.generate("top_bar"),
                    title = ComposableNode.TextNode(
                        id = NodeId.generate("txt_title"),
                        text = "Settings",
                        typography = TypographyToken.TitleLarge
                    ),
                    containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
                ),
                content = ComposableNode.ColumnNode(
                    id = NodeId.generate("col_settings"),
                    modifiers = listOf(
                        ModifierDef.FillMaxWidth(),
                        ModifierDef.Padding(
                            start = DpVal(16f),
                            top = DpVal(16f),
                            end = DpVal(16f),
                            bottom = DpVal(16f)
                        )
                    ),
                    horizontalAlignment = AlignmentHorizontalDef.Start,
                    children = listOf(
                        ComposableNode.TextNode(
                            id = NodeId.generate("hdr_appearance"),
                            text = "Preferences",
                            typography = TypographyToken.TitleSmall,
                            color = ColorSource.Theme(ColorToken.Primary)
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_1"),
                            modifiers = listOf(ModifierDef.Height(DpVal(8f)))
                        ),
                        ComposableNode.ElevatedCardNode(
                            id = NodeId.generate("crd_pref"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.ColumnNode(
                                    id = NodeId.generate("col_pref_items"),
                                    modifiers = listOf(
                                        ModifierDef.FillMaxWidth(),
                                        ModifierDef.Padding(
                                            start = DpVal(16f),
                                            top = DpVal(14f),
                                            end = DpVal(16f),
                                            bottom = DpVal(14f)
                                        )
                                    ),
                                    children = listOf(
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_dark_theme"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_dark_theme"),
                                                    text = "Dark Theme",
                                                    typography = TypographyToken.BodyLarge
                                                ),
                                                ComposableNode.SwitchNode(
                                                    id = NodeId.generate("sw_dark_theme"),
                                                    checked = true
                                                )
                                            )
                                        ),
                                        ComposableNode.HorizontalDividerNode(
                                            id = NodeId.generate("div_1"),
                                            modifiers = listOf(
                                                ModifierDef.Padding(
                                                    top = DpVal(8f),
                                                    bottom = DpVal(8f)
                                                )
                                            )
                                        ),
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_notif"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_notif"),
                                                    text = "Push Notifications",
                                                    typography = TypographyToken.BodyLarge
                                                ),
                                                ComposableNode.SwitchNode(
                                                    id = NodeId.generate("sw_notif"),
                                                    checked = true
                                                )
                                            )
                                        ),
                                        ComposableNode.HorizontalDividerNode(
                                            id = NodeId.generate("div_2"),
                                            modifiers = listOf(
                                                ModifierDef.Padding(
                                                    top = DpVal(8f),
                                                    bottom = DpVal(8f)
                                                )
                                            )
                                        ),
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_sound"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_sound"),
                                                    text = "Sound Effects",
                                                    typography = TypographyToken.BodyLarge
                                                ),
                                                ComposableNode.CheckboxNode(
                                                    id = NodeId.generate("cb_sound"),
                                                    checked = false
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_2"),
                            modifiers = listOf(ModifierDef.Height(DpVal(18f)))
                        ),
                        ComposableNode.TextNode(
                            id = NodeId.generate("hdr_account"),
                            text = "Account & Privacy",
                            typography = TypographyToken.TitleSmall,
                            color = ColorSource.Theme(ColorToken.Primary)
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_3"),
                            modifiers = listOf(ModifierDef.Height(DpVal(8f)))
                        ),
                        ComposableNode.OutlinedCardNode(
                            id = NodeId.generate("crd_account"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.ColumnNode(
                                    id = NodeId.generate("col_acc_items"),
                                    modifiers = listOf(
                                        ModifierDef.FillMaxWidth(),
                                        ModifierDef.Padding(
                                            start = DpVal(16f),
                                            top = DpVal(14f),
                                            end = DpVal(16f),
                                            bottom = DpVal(14f)
                                        )
                                    ),
                                    children = listOf(
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_2fa"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_2fa"),
                                                    text = "Two-Factor Authentication",
                                                    typography = TypographyToken.BodyMedium
                                                ),
                                                ComposableNode.FilledTonalButtonNode(
                                                    id = NodeId.generate("btn_2fa"),
                                                    content = listOf(
                                                        ComposableNode.TextNode(
                                                            id = NodeId.generate("txt_2fa_action"),
                                                            text = "Enable",
                                                            typography = TypographyToken.LabelMedium
                                                        )
                                                    )
                                                )
                                            )
                                        ),
                                        ComposableNode.HorizontalDividerNode(
                                            id = NodeId.generate("div_3"),
                                            modifiers = listOf(
                                                ModifierDef.Padding(
                                                    top = DpVal(8f),
                                                    bottom = DpVal(8f)
                                                )
                                            )
                                        ),
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_privacy"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_privacy"),
                                                    text = "Privacy Policy",
                                                    typography = TypographyToken.BodyMedium
                                                ),
                                                ComposableNode.IconButtonNode(
                                                    id = NodeId.generate("btn_privacy_chevron"),
                                                    content = listOf(
                                                        ComposableNode.IconNode(
                                                            id = NodeId.generate("ic_chevron"),
                                                            iconName = "ChevronRight",
                                                            contentDescription = "Open"
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_4"),
                            modifiers = listOf(ModifierDef.Height(DpVal(20f)))
                        ),
                        ComposableNode.OutlinedButtonNode(
                            id = NodeId.generate("btn_logout"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.TextNode(
                                    id = NodeId.generate("txt_btn_logout"),
                                    text = "Log Out",
                                    typography = TypographyToken.LabelLarge
                                )
                            )
                        )
                    )
                )
            )
        }
    )

    val Feed = ScreenTemplate(
        id = "template_feed",
        name = "Feed",
        defaultRoute = "feed",
        description = "Content discovery feed with horizontal category filter chips, interactive post cards, and a FloatingActionButton.",
        category = TemplateCategory.Content,
        iconName = "Dashboard",
        createRoot = {
            ComposableNode.ScaffoldNode(
                id = NodeId.generate("scaffold"),
                topBar = ComposableNode.TopAppBarNode(
                    id = NodeId.generate("top_bar"),
                    title = ComposableNode.TextNode(
                        id = NodeId.generate("txt_title"),
                        text = "Discover Feed",
                        typography = TypographyToken.TitleLarge
                    ),
                    actions = listOf(
                        ComposableNode.IconButtonNode(
                            id = NodeId.generate("btn_search"),
                            content = listOf(
                                ComposableNode.IconNode(
                                    id = NodeId.generate("ic_search"),
                                    iconName = "Search",
                                    contentDescription = "Search"
                                )
                            )
                        )
                    ),
                    containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
                ),
                floatingActionButton = ComposableNode.FloatingActionButtonNode(
                    id = NodeId.generate("fab_add"),
                    content = listOf(
                        ComposableNode.IconNode(
                            id = NodeId.generate("ic_fab"),
                            iconName = "Add",
                            contentDescription = "Create Post"
                        )
                    )
                ),
                content = ComposableNode.ColumnNode(
                    id = NodeId.generate("col_feed"),
                    modifiers = listOf(
                        ModifierDef.FillMaxWidth(),
                        ModifierDef.Padding(
                            start = DpVal(16f),
                            top = DpVal(14f),
                            end = DpVal(16f),
                            bottom = DpVal(14f)
                        )
                    ),
                    children = listOf(
                        ComposableNode.RowNode(
                            id = NodeId.generate("row_chips"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            horizontalArrangement = ArrangementHorizontalDef.Start,
                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                            children = listOf(
                                ComposableNode.FilterChipNode(
                                    id = NodeId.generate("chip_all"),
                                    label = "Trending",
                                    selected = true
                                ),
                                ComposableNode.SpacerNode(
                                    id = NodeId.generate("spc_chip_1"),
                                    modifiers = listOf(ModifierDef.Width(DpVal(8f)))
                                ),
                                ComposableNode.FilterChipNode(
                                    id = NodeId.generate("chip_design"),
                                    label = "Design",
                                    selected = false
                                ),
                                ComposableNode.SpacerNode(
                                    id = NodeId.generate("spc_chip_2"),
                                    modifiers = listOf(ModifierDef.Width(DpVal(8f)))
                                ),
                                ComposableNode.FilterChipNode(
                                    id = NodeId.generate("chip_compose"),
                                    label = "Compose",
                                    selected = false
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_post_1"),
                            modifiers = listOf(ModifierDef.Height(DpVal(14f)))
                        ),
                        ComposableNode.ElevatedCardNode(
                            id = NodeId.generate("crd_post_1"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.ColumnNode(
                                    id = NodeId.generate("col_post_1"),
                                    modifiers = listOf(
                                        ModifierDef.FillMaxWidth(),
                                        ModifierDef.Padding(
                                            start = DpVal(16f),
                                            top = DpVal(14f),
                                            end = DpVal(16f),
                                            bottom = DpVal(14f)
                                        )
                                    ),
                                    children = listOf(
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_post_header"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_post_title"),
                                                    text = "Material 3 Adaptive Layouts",
                                                    typography = TypographyToken.TitleMedium
                                                ),
                                                ComposableNode.IconNode(
                                                    id = NodeId.generate("ic_bookmark"),
                                                    iconName = "Star",
                                                    contentDescription = "Bookmark"
                                                )
                                            )
                                        ),
                                        ComposableNode.SpacerNode(
                                            id = NodeId.generate("spc_post_sub"),
                                            modifiers = listOf(ModifierDef.Height(DpVal(6f)))
                                        ),
                                        ComposableNode.TextNode(
                                            id = NodeId.generate("txt_post_body"),
                                            text = "Building modern multiplatform apps with Material Design 3 and adaptive window size classes.",
                                            typography = TypographyToken.BodyMedium,
                                            color = ColorSource.Theme(ColorToken.OnSurfaceVariant)
                                        ),
                                        ComposableNode.SpacerNode(
                                            id = NodeId.generate("spc_post_actions"),
                                            modifiers = listOf(ModifierDef.Height(DpVal(10f)))
                                        ),
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_post_actions"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.RowNode(
                                                    id = NodeId.generate("row_likes"),
                                                    verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                                    children = listOf(
                                                        ComposableNode.IconButtonNode(
                                                            id = NodeId.generate("btn_like"),
                                                            content = listOf(
                                                                ComposableNode.IconNode(
                                                                    id = NodeId.generate("ic_heart"),
                                                                    iconName = "Favorite",
                                                                    contentDescription = "Like",
                                                                    tint = ColorSource.Theme(ColorToken.Primary)
                                                                )
                                                            )
                                                        ),
                                                        ComposableNode.TextNode(
                                                            id = NodeId.generate("txt_like_count"),
                                                            text = "342",
                                                            typography = TypographyToken.LabelMedium
                                                        )
                                                    )
                                                ),
                                                ComposableNode.IconButtonNode(
                                                    id = NodeId.generate("btn_share"),
                                                    content = listOf(
                                                        ComposableNode.IconNode(
                                                            id = NodeId.generate("ic_share"),
                                                            iconName = "Share",
                                                            contentDescription = "Share"
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_post_2"),
                            modifiers = listOf(ModifierDef.Height(DpVal(12f)))
                        ),
                        ComposableNode.OutlinedCardNode(
                            id = NodeId.generate("crd_post_2"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.ColumnNode(
                                    id = NodeId.generate("col_post_2"),
                                    modifiers = listOf(
                                        ModifierDef.FillMaxWidth(),
                                        ModifierDef.Padding(
                                            start = DpVal(16f),
                                            top = DpVal(14f),
                                            end = DpVal(16f),
                                            bottom = DpVal(14f)
                                        )
                                    ),
                                    children = listOf(
                                        ComposableNode.TextNode(
                                            id = NodeId.generate("txt_post2_title"),
                                            text = "Desktop Compose Workflows",
                                            typography = TypographyToken.TitleMedium
                                        ),
                                        ComposableNode.SpacerNode(
                                            id = NodeId.generate("spc_post2_sub"),
                                            modifiers = listOf(ModifierDef.Height(DpVal(6f)))
                                        ),
                                        ComposableNode.TextNode(
                                            id = NodeId.generate("txt_post2_body"),
                                            text = "Speed up development cycles with live canvas rendering and instant Kotlin code generation.",
                                            typography = TypographyToken.BodyMedium,
                                            color = ColorSource.Theme(ColorToken.OnSurfaceVariant)
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }
    )

    val Checkout = ScreenTemplate(
        id = "template_checkout",
        name = "Checkout",
        defaultRoute = "checkout",
        description = "E-commerce order checkout screen with shipping details, item summary, price calculation, and payment button.",
        category = TemplateCategory.Commerce,
        iconName = "ShoppingCart",
        createRoot = {
            ComposableNode.ScaffoldNode(
                id = NodeId.generate("scaffold"),
                topBar = ComposableNode.TopAppBarNode(
                    id = NodeId.generate("top_bar"),
                    title = ComposableNode.TextNode(
                        id = NodeId.generate("txt_title"),
                        text = "Order Checkout",
                        typography = TypographyToken.TitleLarge
                    ),
                    containerColor = ColorSource.Theme(ColorToken.SurfaceContainer)
                ),
                content = ComposableNode.ColumnNode(
                    id = NodeId.generate("col_checkout"),
                    modifiers = listOf(
                        ModifierDef.FillMaxWidth(),
                        ModifierDef.Padding(
                            start = DpVal(16f),
                            top = DpVal(16f),
                            end = DpVal(16f),
                            bottom = DpVal(16f)
                        )
                    ),
                    children = listOf(
                        ComposableNode.TextNode(
                            id = NodeId.generate("hdr_shipping"),
                            text = "Shipping Details",
                            typography = TypographyToken.TitleSmall,
                            color = ColorSource.Theme(ColorToken.Primary)
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_ship"),
                            modifiers = listOf(ModifierDef.Height(DpVal(8f)))
                        ),
                        ComposableNode.OutlinedCardNode(
                            id = NodeId.generate("crd_address"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.ColumnNode(
                                    id = NodeId.generate("col_addr"),
                                    modifiers = listOf(
                                        ModifierDef.FillMaxWidth(),
                                        ModifierDef.Padding(
                                            start = DpVal(16f),
                                            top = DpVal(12f),
                                            end = DpVal(16f),
                                            bottom = DpVal(12f)
                                        )
                                    ),
                                    children = listOf(
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_addr_title"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_addr_label"),
                                                    text = "Home Address",
                                                    typography = TypographyToken.TitleMedium
                                                ),
                                                ComposableNode.TextButtonNode(
                                                    id = NodeId.generate("btn_change_addr"),
                                                    content = listOf(
                                                        ComposableNode.TextNode(
                                                            id = NodeId.generate("txt_btn_change"),
                                                            text = "Change",
                                                            typography = TypographyToken.LabelMedium
                                                        )
                                                    )
                                                )
                                            )
                                        ),
                                        ComposableNode.TextNode(
                                            id = NodeId.generate("txt_addr_line1"),
                                            text = "742 Evergreen Terrace",
                                            typography = TypographyToken.BodyMedium
                                        ),
                                        ComposableNode.TextNode(
                                            id = NodeId.generate("txt_addr_line2"),
                                            text = "Springfield, OR 97477",
                                            typography = TypographyToken.BodySmall,
                                            color = ColorSource.Theme(ColorToken.OnSurfaceVariant)
                                        )
                                    )
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_summary"),
                            modifiers = listOf(ModifierDef.Height(DpVal(16f)))
                        ),
                        ComposableNode.TextNode(
                            id = NodeId.generate("hdr_summary"),
                            text = "Order Summary",
                            typography = TypographyToken.TitleSmall,
                            color = ColorSource.Theme(ColorToken.Primary)
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_summary_gap"),
                            modifiers = listOf(ModifierDef.Height(DpVal(8f)))
                        ),
                        ComposableNode.ElevatedCardNode(
                            id = NodeId.generate("crd_summary"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.ColumnNode(
                                    id = NodeId.generate("col_summary_items"),
                                    modifiers = listOf(
                                        ModifierDef.FillMaxWidth(),
                                        ModifierDef.Padding(
                                            start = DpVal(16f),
                                            top = DpVal(14f),
                                            end = DpVal(16f),
                                            bottom = DpVal(14f)
                                        )
                                    ),
                                    children = listOf(
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_item_1"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_item_name"),
                                                    text = "M3 Wireless Headphones",
                                                    typography = TypographyToken.BodyMedium
                                                ),
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_item_price"),
                                                    text = "$129.99",
                                                    typography = TypographyToken.BodyMedium
                                                )
                                            )
                                        ),
                                        ComposableNode.SpacerNode(
                                            id = NodeId.generate("spc_item_gap"),
                                            modifiers = listOf(ModifierDef.Height(DpVal(6f)))
                                        ),
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_shipping_fee"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_shipping_label"),
                                                    text = "Express Delivery",
                                                    typography = TypographyToken.BodyMedium
                                                ),
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_shipping_price"),
                                                    text = "FREE",
                                                    typography = TypographyToken.BodyMedium,
                                                    color = ColorSource.Theme(ColorToken.Primary)
                                                )
                                            )
                                        ),
                                        ComposableNode.HorizontalDividerNode(
                                            id = NodeId.generate("div_summary"),
                                            modifiers = listOf(
                                                ModifierDef.Padding(
                                                    top = DpVal(10f),
                                                    bottom = DpVal(10f)
                                                )
                                            )
                                        ),
                                        ComposableNode.RowNode(
                                            id = NodeId.generate("row_total"),
                                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                                            horizontalArrangement = ArrangementHorizontalDef.SpaceBetween,
                                            verticalAlignment = AlignmentVerticalDef.CenterVertically,
                                            children = listOf(
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_total_label"),
                                                    text = "Total Payable",
                                                    typography = TypographyToken.TitleMedium
                                                ),
                                                ComposableNode.TextNode(
                                                    id = NodeId.generate("txt_total_price"),
                                                    text = "$129.99",
                                                    typography = TypographyToken.TitleMedium,
                                                    color = ColorSource.Theme(ColorToken.Primary)
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        ComposableNode.SpacerNode(
                            id = NodeId.generate("spc_pay_btn"),
                            modifiers = listOf(ModifierDef.Height(DpVal(20f)))
                        ),
                        ComposableNode.ButtonNode(
                            id = NodeId.generate("btn_place_order"),
                            modifiers = listOf(ModifierDef.FillMaxWidth()),
                            content = listOf(
                                ComposableNode.TextNode(
                                    id = NodeId.generate("txt_btn_place"),
                                    text = "Confirm & Pay ($129.99)",
                                    typography = TypographyToken.LabelLarge
                                )
                            )
                        )
                    )
                )
            )
        }
    )

    val all: List<ScreenTemplate> = listOf(Login, Profile, Settings, Feed, Checkout)

    fun findById(id: String): ScreenTemplate? = all.find { it.id == id }
}
