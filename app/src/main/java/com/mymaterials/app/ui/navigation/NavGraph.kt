package com.mymaterials.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.screens.StatsScreen
import com.mymaterials.app.ui.screens.SubjectDetailScreen
import com.mymaterials.app.ui.screens.SubjectsScreen

sealed class Screen(val route: String) {
    object Subjects : Screen("subjects")
    object SubjectDetail : Screen("subject/{subjectId}") {
        fun createRoute(id: Long) = "subject/$id"
    }
    object Stats : Screen("stats")
}

@Composable
fun AppNavGraph(repository: MaterialsRepository) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Screen.Subjects.route) {
        composable(Screen.Subjects.route) {
            SubjectsScreen(
                repository = repository,
                onSubjectClick = { id -> nav.navigate(Screen.SubjectDetail.createRoute(id)) },
                onStatsClick = { nav.navigate(Screen.Stats.route) }
            )
        }
        composable(
            Screen.SubjectDetail.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("subjectId") ?: 0L
            SubjectDetailScreen(
                subjectId = id,
                repository = repository,
                onBack = { nav.popBackStack() }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(repository = repository, onBack = { nav.popBackStack() })
        }
    }
}
