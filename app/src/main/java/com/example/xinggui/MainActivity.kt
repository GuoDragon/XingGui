package com.example.xinggui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.xinggui.common.locale.ForcedLocale
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.navigation.AppNavGraph
import com.example.xinggui.ui.theme.XingGuiTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ForcedLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ForcedLocale.ensureProcessLocale()
        super.onCreate(savedInstanceState)
        DataRepository.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            XingGuiTheme {
                XingGuiApp()
            }
        }
    }
}

@Composable
private fun XingGuiApp() {
    val navController = rememberNavController()
    Surface(modifier = Modifier.fillMaxSize()) {
        AppNavGraph(navController = navController)
    }
}
