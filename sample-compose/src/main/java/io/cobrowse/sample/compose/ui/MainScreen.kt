package io.cobrowse.sample.compose.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import io.cobrowse.sample.data.model.detailsUrl
import io.cobrowse.sample.compose.ui.transactions.TransactionsChartScreen
import io.cobrowse.sample.compose.ui.transactions.TransactionsScreen
import kotlinx.coroutines.delay

private sealed class BottomSheetDestination {
    object TransactionsList : BottomSheetDestination()
    data class TransactionDetail(val url: String) : BottomSheetDestination()
}

/**
 * Colored app-bar style header used inside the bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetTopBar(
    title: String,
    showBackButton: Boolean,
    onBack: () -> Unit
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back to transactions"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModelFactory: CobrowseViewModelFactory
) {
    val context = LocalContext.current
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    var bottomSheetDestination by remember {
        mutableStateOf<BottomSheetDestination>(BottomSheetDestination.TransactionsList)
    }

    // Slide transition shared by both the header and the body so they move as one atomic unit,
    // avoiding any mismatch/flicker between two independently animated blocks.
    val slideTransitionSpec: AnimatedContentTransitionScope<BottomSheetDestination>.() -> ContentTransform = {
        val forward = targetState is BottomSheetDestination.TransactionDetail
        (
            slideInHorizontally(animationSpec = tween(300)) { fullWidth -> if (forward) fullWidth else -fullWidth } +
                fadeIn(animationSpec = tween(220))
            ) togetherWith (
            slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> if (forward) -fullWidth else fullWidth } +
                fadeOut(animationSpec = tween(180))
            )
    }

    // TODO make sheetPeekHeight=120.dp if the available height is less that ~300.dp?
    val sheetPeekHeight = 320.dp

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle()
        },
        sheetContent = {
            // Full height (not a fraction) so that swiping the sheet all the way up reaches
            // a genuine full-screen state.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(1f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                AnimatedContent(
                    targetState = bottomSheetDestination,
                    transitionSpec = slideTransitionSpec,
                    modifier = Modifier.fillMaxSize(),
                    label = "sheet_content_animation"
                ) { destination ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        when (destination) {
                            is BottomSheetDestination.TransactionsList -> {
                                SheetTopBar(
                                    title = stringResource(io.cobrowse.sample.core.R.string.fragment_transaction),
                                    showBackButton = false,
                                    onBack = {}
                                )

                                TransactionsScreen(
                                    viewModelFactory = viewModelFactory,
                                    onTransactionClick = { transaction ->
                                        val url = transaction.detailsUrl(context)
                                        bottomSheetDestination = BottomSheetDestination.TransactionDetail(url)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                            is BottomSheetDestination.TransactionDetail -> {
                                SheetTopBar(
                                    title = "Transaction Detail",
                                    showBackButton = true,
                                    onBack = { bottomSheetDestination = BottomSheetDestination.TransactionsList }
                                )

                                // FIXME: mounting an android.webkit.WebView can still momentarily stall the
                                //  main thread.
                                var isWebViewReady by remember(destination) { mutableStateOf(false) }
                                LaunchedEffect(destination) {
                                    delay(32)
                                    isWebViewReady = true
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isWebViewReady) {
                                        val webViewState = rememberWebViewState(url = destination.url)
                                        WebView(
                                            state = webViewState,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (webViewState.isLoading) {
                                            CircularProgressIndicator()
                                        }
                                    } else {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            TransactionsChartScreen(
                viewModelFactory = viewModelFactory,
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = {
                    navController.navigate(Screen.Account.route)
                }) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = stringResource(io.cobrowse.sample.core.R.string.menu_item_account),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
