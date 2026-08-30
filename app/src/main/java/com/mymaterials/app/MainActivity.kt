package com.mymaterials.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mymaterials.app.data.db.DatabaseProvider
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.navigation.AppNavGraph
import com.mymaterials.app.ui.theme.IosBackground
import com.mymaterials.app.ui.theme.MyMaterialsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = DatabaseProvider.get(this)
        val repo = MaterialsRepository(db.subjectDao(), db.unitDao(), db.lessonDao())
        setContent {
            MyMaterialsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = IosBackground) {
                    AppNavGraph(repository = repo)
                }
            }
        }
    }
}
