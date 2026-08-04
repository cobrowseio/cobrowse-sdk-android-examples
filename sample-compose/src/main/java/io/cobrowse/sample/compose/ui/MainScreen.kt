package io.cobrowse.sample.compose.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import io.cobrowse.sample.compose.ui.transactions.TransactionsChartScreen
import io.cobrowse.sample.compose.ui.transactions.TransactionsScreen
import io.cobrowse.sample.data.model.detailsUrl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "MainScreen"

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
                        contentDescription = stringResource(io.cobrowse.sample.compose.R.string.icon_back)
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
    val scope = rememberCoroutineScope()

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
                                        scope.launch { scaffoldState.bottomSheetState.expand() }
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
                                    onBack = {
                                        bottomSheetDestination = BottomSheetDestination.TransactionsList
                                    }
                                )

                                TransactionWebViewScreen(destination) {
                                    bottomSheetDestination = BottomSheetDestination.TransactionDetail(it.toString())
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ColumnScope.TransactionWebViewScreen(
    destination: BottomSheetDestination.TransactionDetail,
    onLinkClick: (Uri) -> Unit) {
    // FIXME: mounting an android.webkit.WebView can still momentarily stall the
    //  main thread.
    var isWebViewReady by remember(destination) { mutableStateOf(false) }
    LaunchedEffect(destination) {
        delay(32)
        isWebViewReady = true
    }


    fun Context.invokeViewIntent(uri: Uri) {
        try {
            startActivity(Intent.createChooser(Intent().apply {
                action = Intent.ACTION_VIEW
                data = uri
            }, null))
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open a URI of the scheme ${uri.scheme}")
        }
    }

    fun invokeNewWebView(uri: Uri) {
        onLinkClick.invoke(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        if (isWebViewReady) {
            val webViewState = rememberWebViewState(url = destination.url)
            val client: AccompanistWebViewClient = remember {
                object : AccompanistWebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        request?.let {
                            when (request.url?.scheme) {
                                "tel", "sms", "mailto" -> {
                                    view?.context?.invokeViewIntent(request.url)
                                    return true
                                }

                                else -> {}
                            }
                            if (request.isForMainFrame) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !request.isRedirect) {
                                    invokeNewWebView(request.url)
                                    return true
                                }
                            }
                        }
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                onCreated = {
                    it.settings.javaScriptEnabled = true
                },
                client = client
            )
            if (webViewState.isLoading) {
                CircularProgressIndicator()
            }
        } else {
            CircularProgressIndicator()
        }
    }
}


@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun MainScreenPreview() {
    MainScreen(rememberNavController(), CobrowseViewModelFactory())
}
