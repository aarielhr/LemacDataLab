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
import com.example.lemacdatalab.data.repository.PreferencesSolicitudRepository
import com.example.lemacdatalab.ui.components.AppBottomBar
import com.example.lemacdatalab.ui.screens.DetalleRegistroScreen
import com.example.lemacdatalab.ui.screens.DetalleSolicitudScreen
import com.example.lemacdatalab.ui.screens.FormularioRegistroScreen
import com.example.lemacdatalab.ui.screens.FormularioSolicitudScreen
import com.example.lemacdatalab.ui.screens.ListaRegistrosScreen
import com.example.lemacdatalab.ui.screens.ListaSolicitudesScreen
import com.example.lemacdatalab.ui.viewmodel.RegistroViewModel
import com.example.lemacdatalab.ui.viewmodel.SolicitudViewModel

object NavDestinations {
    // Sección Autocuidado
    const val LISTA = "lista"
    const val FORMULARIO = "formulario"
    const val DETALLE = "detalle/{registroId}"

    // Sección Solicitudes de laboratorio
    const val SOLICITUDES = "solicitudes"
    const val FORMULARIO_SOLICITUD = "solicitudes/formulario"
    const val DETALLE_SOLICITUD = "solicitudes/detalle/{solicitudId}"

    fun createDetalleRoute(registroId: String) = "detalle/$registroId"
    fun createDetalleSolicitudRoute(solicitudId: String) = "solicitudes/detalle/$solicitudId"
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
    val solicitudViewModel: SolicitudViewModel = viewModel(
        factory = viewModelFactoryOf { SolicitudViewModel(PreferencesSolicitudRepository(appContext)) }
    )

    // Cambio entre las dos secciones principales conservando el estado de cada una
    val irASeccion: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(NavDestinations.LISTA) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

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
                onNavigateToDetalle = { id -> navController.navigate(NavDestinations.createDetalleRoute(id)) },
                bottomBar = {
                    AppBottomBar(
                        rutaActual = NavDestinations.LISTA,
                        rutaAutocuidado = NavDestinations.LISTA,
                        rutaSolicitudes = NavDestinations.SOLICITUDES,
                        onSeleccionar = irASeccion
                    )
                }
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

        // ----- Solicitudes -----
        composable(NavDestinations.SOLICITUDES) {
            ListaSolicitudesScreen(
                viewModel = solicitudViewModel,
                onNavigateToFormulario = { navController.navigate(NavDestinations.FORMULARIO_SOLICITUD) },
                onNavigateToDetalle = { id -> navController.navigate(NavDestinations.createDetalleSolicitudRoute(id)) },
                bottomBar = {
                    AppBottomBar(
                        rutaActual = NavDestinations.SOLICITUDES,
                        rutaAutocuidado = NavDestinations.LISTA,
                        rutaSolicitudes = NavDestinations.SOLICITUDES,
                        onSeleccionar = irASeccion
                    )
                }
            )
        }

        composable(NavDestinations.FORMULARIO_SOLICITUD) {
            FormularioSolicitudScreen(
                viewModel = solicitudViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSolicitudCreada = { nuevaId ->
                    navController.navigate(NavDestinations.createDetalleSolicitudRoute(nuevaId)) {
                        popUpTo(NavDestinations.FORMULARIO_SOLICITUD) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavDestinations.DETALLE_SOLICITUD,
            arguments = listOf(navArgument("solicitudId") { type = NavType.StringType })
        ) { backStackEntry ->
            val solicitudId = backStackEntry.arguments?.getString("solicitudId") ?: ""
            DetalleSolicitudScreen(
                solicitudId = solicitudId,
                viewModel = solicitudViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
