package com.mymaterials.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mymaterials.app.data.db.DatabaseProvider
import com.mymaterials.app.data.preferences.AppPreferences
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.navigation.AppNavGraph
import com.mymaterials.app.ui.theme.MyMaterialsTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val locale = Locale("ar")
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // إجبار اتجاه ولغة التطبيق على العربية دائماً حتى لو كانت لغة الهاتف مختلفة
        val locale = Locale("ar")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        val db = DatabaseProvider.get(this)
        val repo = MaterialsRepository(db.subjectDao(), db.unitDao(), db.lessonDao())
        val preferences = AppPreferences.get(this)

        setContent {
            val themeMode by preferences.themeMode.collectAsState()
            MyMaterialsTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(repository = repo, preferences = preferences)
                }
            }
        }
    }
}
