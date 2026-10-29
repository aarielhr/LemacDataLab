package com.example.lemacdatalab.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lemacdatalab.data.repository.PreferencesRegistroRepository
import com.example.lemacdatalab.ui.screens.DetalleRegistroScreen
import com.example.lemacdatalab.ui.screens.FormularioRegistroScreen
import com.example.lemacdatalab.ui.screens.ListaRegistrosScreen
import com.example.lemacdatalab.ui.viewmodel.RegistroViewModel

object NavDestinations {
    // Sección Autocuidado
    const val LISTA = "lista"
    const val FORMULARIO = "formulario"
    const val DETALLE = "detalle/{registroId}"

    fun createDetalleRoute(registroId: String) = "detalle/$registroId"
}

private inline fun viewModelFactoryOf(crossinline build: () -> ViewModel): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = build() as T
    }

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val appContext = LocalContext.current.applicationContext

    val registroViewModel: RegistroViewModel = viewModel(
        factory = viewModelFactoryOf { RegistroViewModel(PreferencesRegistroRepository(appContext)) }
    )

    NavHost(
        navController = navController,
        startDestination = NavDestinations.LISTA,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 3 }) },
        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 3 }) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it / 3 }) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { it / 3 }) }
    ) {
        // ----- Autocuidado -----
        composable(NavDestinations.LISTA) {
            ListaRegistrosScreen(
                viewModel = registroViewModel,
                onNavigateToFormulario = { navController.navigate(NavDestinations.FORMULARIO) },
                onNavigateToDetalle = { id -> navController.navigate(NavDestinations.createDetalleRoute(id)) }
            )
        }

        composable(NavDestinations.FORMULARIO) {
            FormularioRegistroScreen(
                viewModel = registroViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegistroCreado = { nuevaId ->
                    navController.navigate(NavDestinations.createDetalleRoute(nuevaId)) {
                        popUpTo(NavDestinations.FORMULARIO) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavDestinations.DETALLE,
            arguments = listOf(navArgument("registroId") { type = NavType.StringType })
        ) { backStackEntry ->
            val registroId = backStackEntry.arguments?.getString("registroId") ?: ""
            DetalleRegistroScreen(
                registroId = registroId,
                viewModel = registroViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
