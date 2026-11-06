package io.cobrowse.sample.compose.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.cobrowse.cobrowseRedacted
import io.cobrowse.sample.compose.ui.theme.SampleComposeTheme
import io.cobrowse.sample.data.CobrowseDeepLinks
import io.cobrowse.sample.data.LoginRepository

/**
 * The single activity of the app, all screens are composables.
 */
class MainActivity : ComponentActivity() {

    private val loginRepository = LoginRepository.getInstance()

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            handleDeepLink(intent)
        }

        setContent {
            SampleComposeTheme {
                val navController = rememberNavController().also { navController = it }
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        // Everything is redacted, apart from the composables marked as unredacted
                        .then(if (RedactionByDefault.isEnabled) Modifier.cobrowseRedacted() else Modifier),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(navController)
                }
                ConsentDialog()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data ?: return
        if (CobrowseDeepLinks.handle(uri)
            && CobrowseDeepLinks.isDemo(uri)
            && !loginRepository.isLoggedIn) {
            loginRepository.login("demo", "demo")
            navController?.navigate(Screen.Main.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
}
