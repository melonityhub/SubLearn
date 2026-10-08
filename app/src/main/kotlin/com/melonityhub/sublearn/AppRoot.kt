package com.melonityhub.sublearn

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.melonityhub.sublearn.core.data.repo.RecentVideosRepository
import com.melonityhub.sublearn.core.design.ComingSoonBadge
import com.melonityhub.sublearn.core.design.SublearnTokens
import com.melonityhub.sublearn.core.model.MediaSource
import com.melonityhub.sublearn.core.player.Media3PlayerController
import com.melonityhub.sublearn.core.settings.DecoderMode
import com.melonityhub.sublearn.feature.home.HomeScreen
import com.melonityhub.sublearn.feature.player.PlayerScreen
import com.melonityhub.sublearn.feature.settings.SettingsScreen
import com.melonityhub.sublearn.feature.words.MyWordsScreen
import com.melonityhub.sublearn.feature.words.MyWordsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.androidx.compose.koinViewModel

private const val ROUTE_MAIN = "main"
private const val ROUTE_PLAYER = "player/{uri}/{title}"
private const val ROUTE_WORDS = "words"
private const val ROUTE_SETTINGS = "settings"

/** Route for a video. Arguments are URI-encoded because they can contain slashes. */
fun playerRoute(source: MediaSource): String = "player/${Uri.encode(source.uri)}/${Uri.encode(source.title)}"

/** Top-level navigation: the main shell (tabs + side menu), the player, My Words and Settings. */
@Composable
fun AppRoot(external: MediaSource?, onExternalConsumed: () -> Unit) {
    val nav = rememberNavController()
    LaunchedEffect(external) {
        if (external != null) {
            nav.navigate(playerRoute(external))
            onExternalConsumed()
        }
    }
    NavHost(navController = nav, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainShell(
                onOpenVideo = { nav.navigate(playerRoute(it)) },
                onOpenWords = { nav.navigate(ROUTE_WORDS) },
                onOpenSettings = { nav.navigate(ROUTE_SETTINGS) },
            )
        }
        composable(
            ROUTE_PLAYER,
            arguments = listOf(
                navArgument("uri") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
            ),
        ) { entry ->
            val source = MediaSource(
                uri = entry.arguments?.getString("uri").orEmpty(),
                title = entry.arguments?.getString("title").orEmpty(),
            )
            PlayerRoute(
                source = source,
                onBack = { nav.popBackStack() },
                onOpenDetails = { text -> openTranslatePage(nav, text) },
            )
        }
        composable(ROUTE_WORDS) {
            val vm: MyWordsViewModel = koinViewModel()
            MyWordsScreen(viewModel = vm, onBack = { nav.popBackStack() })
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}

@Composable
private fun PlayerRoute(source: MediaSource, onBack: () -> Unit, onOpenDetails: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // One controller per opened video. The ViewModel sets the real decoder mode from settings before loading.
    val controller = remember(source.uri) {
        Media3PlayerController(context.applicationContext, scope, DecoderMode.HW_PLUS)
    }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    PlayerScreen(source = source, controller = controller, onBack = onBack, onOpenDetails = onOpenDetails)
}

private fun openTranslatePage(nav: androidx.navigation.NavHostController, text: String) {
    val context = nav.context
    val uri = Uri.parse("https://translate.google.com/?sl=en&tl=fa&op=translate&text=${Uri.encode(text)}")
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}

private enum class Tab(val icon: androidx.compose.ui.graphics.vector.ImageVector, val labelRes: Int, val comingSoonRes: Int?) {
    HOME(Icons.Filled.Home, R.string.nav_home, null),
    YOUTUBE(Icons.Filled.PlayCircle, R.string.nav_youtube, R.string.coming_soon_youtube),
    LEARN(Icons.Filled.School, R.string.nav_learn, R.string.coming_soon_learn),
    DICTIONARY(Icons.AutoMirrored.Filled.MenuBook, R.string.nav_dictionary, R.string.coming_soon_dictionary),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(
    onOpenVideo: (MediaSource) -> Unit,
    onOpenWords: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val recents: RecentVideosRepository = koinInject()
    var tab by remember { mutableStateOf(Tab.HOME) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var comingSoonMessage by remember { mutableStateOf<Int?>(null) }

    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            // Keep read access across restarts so Recent videos still open (SAF persistable grant).
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onOpenVideo(MediaSource(uri.toString(), displayNameOf(context, uri)))
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(SublearnTokens.SpaceL),
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_level)) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onOpenSettings() },
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_my_words)) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onOpenWords() },
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_quiz)) },
                    selected = false,
                    onClick = { comingSoonMessage = R.string.coming_soon_quiz },
                    badge = { ComingSoonBadge(stringResource(R.string.coming_soon_badge)) },
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_updates)) },
                    selected = false,
                    onClick = { comingSoonMessage = R.string.coming_soon_updates },
                    badge = { ComingSoonBadge(stringResource(R.string.coming_soon_badge)) },
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_settings)) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onOpenSettings() },
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu_open))
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.labelRes)) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    Tab.HOME -> HomeScreen(
                        recent = recents.observeRecent(),
                        onPickVideo = { pickVideo.launch(arrayOf("video/*")) },
                        onOpenUrl = { showUrlDialog = true },
                        onOpenRecent = onOpenVideo,
                        onRemoveRecent = { uri -> scope.launch { recents.remove(uri) } },
                    )
                    else -> ComingSoonPanel(tab)
                }
            }
        }
    }

    if (showUrlDialog) {
        UrlDialog(
            onDismiss = { showUrlDialog = false },
            onOpen = { url ->
                showUrlDialog = false
                onOpenVideo(MediaSource(url, Uri.parse(url).lastPathSegment ?: url))
            },
        )
    }
    comingSoonMessage?.let { messageRes ->
        AlertDialog(
            onDismissRequest = { comingSoonMessage = null },
            confirmButton = { TextButton(onClick = { comingSoonMessage = null }) { Text(stringResource(R.string.cancel)) } },
            title = { ComingSoonBadge(stringResource(R.string.coming_soon_badge)) },
            text = { Text(stringResource(messageRes)) },
        )
    }
}

@Composable
private fun ComingSoonPanel(tab: Tab) {
    Column(
        modifier = Modifier.fillMaxSize().padding(SublearnTokens.SpaceXl),
        verticalArrangement = Arrangement.spacedBy(SublearnTokens.SpaceM),
    ) {
        ComingSoonBadge(stringResource(R.string.coming_soon_badge))
        Text(stringResource(tab.labelRes), style = MaterialTheme.typography.headlineSmall)
        tab.comingSoonRes?.let { Text(stringResource(it), style = MaterialTheme.typography.bodyMedium) }
        OutlinedButton(onClick = {}, enabled = false) { Text(stringResource(R.string.coming_soon_badge)) }
    }
}

@Composable
private fun UrlDialog(onDismiss: () -> Unit, onOpen: (String) -> Unit) {
    var url by remember { mutableStateOf("https://") }
    val valid = url.startsWith("http://") || url.startsWith("https://")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.url_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it.trim() },
                    placeholder = { Text(stringResource(R.string.url_dialog_hint)) },
                    singleLine = true,
                    isError = url.isNotEmpty() && !valid,
                    supportingText = { if (url.isNotEmpty() && !valid) Text(stringResource(R.string.url_invalid)) },
                )
            }
        },
        confirmButton = {
            Button(onClick = { onOpen(url) }, enabled = valid) { Text(stringResource(R.string.open)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private fun displayNameOf(context: android.content.Context, uri: Uri): String {
    runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0)?.takeIf { it.isNotBlank() }?.let { return it }
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/') ?: context.getString(R.string.unlisted_title)
}
