package com.mymaterials.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mymaterials.app.data.preferences.AppPreferences
import com.mymaterials.app.data.repository.MaterialsRepository
import com.mymaterials.app.ui.screens.SettingsScreen
import com.mymaterials.app.ui.screens.StatsScreen
import com.mymaterials.app.ui.screens.SubjectDetailScreen
import com.mymaterials.app.ui.screens.SubjectsScreen
import com.mymaterials.app.ui.viewmodels.SettingsViewModel
import com.mymaterials.app.ui.viewmodels.StatsViewModel
import com.mymaterials.app.ui.viewmodels.SubjectDetailViewModel
import com.mymaterials.app.ui.viewmodels.SubjectsViewModel

sealed class Screen(val route: String) {
    object Subjects : Screen("subjects")
    object SubjectDetail : Screen("subject/{subjectId}") {
        fun createRoute(id: Long) = "subject/$id"
    }
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

@Composable
fun AppNavGraph(
    repository: MaterialsRepository,
    preferences: AppPreferences
) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Screen.Subjects.route) {
        composable(Screen.Subjects.route) {
            val vm: SubjectsViewModel = viewModel(
                factory = SubjectsViewModel.Factory(repository, preferences)
            )
            SubjectsScreen(
                viewModel = vm,
                repository = repository,
                onSubjectClick = { id -> nav.navigate(Screen.SubjectDetail.createRoute(id)) },
                onStatsClick = { nav.navigate(Screen.Stats.route) },
                onSettingsClick = { nav.navigate(Screen.Settings.route) }
            )
        }
        composable(
            Screen.SubjectDetail.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("subjectId") ?: 0L
            val vm: SubjectDetailViewModel = viewModel(
                key = "subject_$id",
                factory = SubjectDetailViewModel.Factory(id, repository)
            )
            SubjectDetailScreen(
                viewModel = vm,
                onBack = { nav.popBackStack() }
            )
        }
        composable(Screen.Stats.route) {
            val vm: StatsViewModel = viewModel(
                factory = StatsViewModel.Factory(repository)
            )
            StatsScreen(
                viewModel = vm,
                onBack = { nav.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(preferences, repository)
            )
            SettingsScreen(
                viewModel = vm,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
