package com.worldprices.converter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.worldprices.converter.data.local.PreferencesDataStore
import com.worldprices.converter.data.repository.CurrencyRepository
import com.worldprices.converter.ui.main.MainScreen
import com.worldprices.converter.ui.main.MainViewModel
import com.worldprices.converter.ui.onboarding.OnboardingScreen
import com.worldprices.converter.ui.theme.WorldPricesTheme

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

            WorldPricesTheme(themeMode = uiState.themeMode) {
                if (!uiState.hasSeenOnboarding) {
                    OnboardingScreen(
                        isRussian = uiState.isRussian,
                        onFinish = { source, targets ->
                            viewModel.completeOnboarding(source, targets)
                        }
                    )
                } else {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
