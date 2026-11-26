package com.worldprices.converter

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldprices.converter.data.local.PreferencesDataStore
import com.worldprices.converter.data.repository.CurrencyRepository
import com.worldprices.converter.ui.main.MainScreen
import com.worldprices.converter.ui.main.MainViewModel
import com.worldprices.converter.ui.onboarding.OnboardingScreen
import com.worldprices.converter.ui.theme.WorldPricesTheme
import com.worldprices.converter.ui.utils.AppStrings
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val prefs = PreferencesDataStore(applicationContext)
        val repo = CurrencyRepository(prefs)
        MainViewModel.Factory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            val locale = Locale(uiState.language)
            val currentConfig = LocalConfiguration.current
            val localizedConfig = Configuration(currentConfig).apply {
                setLocale(locale)
            }
            val localizedContext = LocalContext.current.createConfigurationContext(localizedConfig)

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides this
            ) {
                WorldPricesTheme(themeMode = uiState.themeMode) {
                    Crossfade(
                        targetState = Triple(
                            uiState.isPreferencesLoaded,
                            uiState.hasSeenOnboarding,
                            uiState.isRussian
                        ),
                        label = "screen_transition"
                    ) { (isLoaded, hasSeenOnboarding, isRussian) ->
                        when {
                            !isLoaded -> {
                                SplashScreen(isRussian = isRussian)
                            }
                            !hasSeenOnboarding -> {
                                OnboardingScreen(
                                    isRussian = isRussian,
                                    onToggleLanguage = { viewModel.setLanguage(it) },
                                    onFinish = { source, targets ->
                                        viewModel.completeOnboarding(source, targets)
                                    }
                                )
                            }
                            else -> {
                                MainScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(isRussian: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💱", fontSize = 42.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = AppStrings.headerTitle(isRussian),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
