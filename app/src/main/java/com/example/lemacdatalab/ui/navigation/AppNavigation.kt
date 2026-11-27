package com.example.lemacdatalab.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lemacdatalab.data.repository.PreferencesRegistroRepository
import com.example.lemacdatalab.data.repository.PreferencesUserRepository
import com.example.lemacdatalab.ui.screens.ConfiguracionNotificacionesScreen
import com.example.lemacdatalab.ui.screens.DetalleRegistroScreen
import com.example.lemacdatalab.ui.screens.FormularioRegistroScreen
import com.example.lemacdatalab.ui.screens.ListaRegistrosScreen
import com.example.lemacdatalab.ui.screens.LoginScreen
import com.example.lemacdatalab.ui.screens.MisDatosScreen
import com.example.lemacdatalab.ui.screens.ReingresoScreen
import com.example.lemacdatalab.ui.screens.RegistroUsuarioScreen
import com.example.lemacdatalab.ui.viewmodel.AuthViewModel
import com.example.lemacdatalab.ui.viewmodel.ActividadesViewModel
import com.example.lemacdatalab.ui.viewmodel.NotificationViewModel
import com.example.lemacdatalab.ui.viewmodel.RegistroViewModel
import kotlinx.coroutines.delay

object NavDestinations {
    // Autenticación
    const val LOGIN = "login"
    const val REGISTRO_USUARIO = "registro_usuario"
    const val MIS_DATOS = "mis_datos"
    const val REINGRESO = "reingreso"
    const val NOTIFICACIONES = "notificaciones"

    // Autocuidado (CRUD)
    const val LISTA = "lista"
    const val FORMULARIO = "formulario"
    const val EDITAR = "editar/{registroId}"
    const val DETALLE = "detalle/{registroId}"

    fun createDetalleRoute(registroId: String) = "detalle/$registroId"
    fun createEditarRoute(registroId: String) = "editar/$registroId"
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

    val authViewModel: AuthViewModel = viewModel(
        factory = viewModelFactoryOf { AuthViewModel(PreferencesUserRepository(appContext)) }
    )
    val registroViewModel: RegistroViewModel = viewModel(
        factory = viewModelFactoryOf { RegistroViewModel(PreferencesRegistroRepository(appContext)) }
    )
    val actividadesViewModel: ActividadesViewModel = viewModel(
        factory = viewModelFactoryOf { ActividadesViewModel() }
    )
    val notificationViewModel: NotificationViewModel = viewModel()

    val authUiState by authViewModel.uiState.collectAsState()
    val startDestination = if (authUiState.sesionUsuario != null) NavDestinations.LISTA else NavDestinations.LOGIN

    // Observador del ciclo de vida para bloquear la sesión al pasar la app a segundo plano (ON_STOP)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                authViewModel.bloquearSesionPorSegundoPlano()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Timer en segundo plano para bloquear la sesión tras 5 minutos de inactividad (300.000 ms)
    val maxInactividadMs = 5 * 60 * 1000L
    LaunchedEffect(authUiState.sesionUsuario, authUiState.sesionBloqueada) {
        if (authUiState.sesionUsuario != null && !authUiState.sesionBloqueada) {
            while (true) {
                delay(5000)
                if (authViewModel.verificarInactividadExcedida(maxInactividadMs)) {
                    authViewModel.bloquearSesionPorInactividad()
                }
            }
        }
    }

    // Redirección al reingreso cuando la sesión se bloquea
    LaunchedEffect(authUiState.sesionBloqueada) {
        if (authUiState.sesionBloqueada) {
            navController.navigate(NavDestinations.REINGRESO) {
                popUpTo(NavDestinations.LOGIN) { inclusive = false }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        authViewModel.registrarActividadUsuario()
                    }
                }
            }
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 3 }) },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 3 }) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it / 3 }) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(targetOffsetX = { it / 3 }) }
        ) {
            // ----- Autenticación -----
            composable(NavDestinations.LOGIN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginExitoso = {
                        navController.navigate(NavDestinations.LISTA) {
                            popUpTo(NavDestinations.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegistroUsuario = {
                        authViewModel.limpiarMensajes()
                        navController.navigate(NavDestinations.REGISTRO_USUARIO)
                    }
                )
            }

            composable(NavDestinations.REGISTRO_USUARIO) {
                RegistroUsuarioScreen(
                    authViewModel = authViewModel,
                    onRegistroExitoso = {
                        navController.navigate(NavDestinations.LISTA) {
                            popUpTo(NavDestinations.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        authViewModel.limpiarMensajes()
                        navController.popBackStack()
                    }
                )
            }

            composable(NavDestinations.REINGRESO) {
                ReingresoScreen(
                    authViewModel = authViewModel,
                    onReingresoExitoso = {
                        navController.navigate(NavDestinations.LISTA) {
                            popUpTo(NavDestinations.REINGRESO) { inclusive = true }
                        }
                    },
                    onCerrarSesion = {
                        authViewModel.cerrarSesion()
                        navController.navigate(NavDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavDestinations.MIS_DATOS) {
                val usuarioActual = authViewModel.getUsuarioActual()
                MisDatosScreen(
                    usuario = usuarioActual,
                    onNavigateToNotificaciones = {
                        navController.navigate(NavDestinations.NOTIFICACIONES)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavDestinations.NOTIFICACIONES) {
                ConfiguracionNotificacionesScreen(
                    viewModel = notificationViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ----- Autocuidado (CRUD) -----
            composable(NavDestinations.LISTA) {
                ListaRegistrosScreen(
                    viewModel = registroViewModel,
                    usuarioSesion = authUiState.sesionUsuario,
                    onNavigateToMisDatos = {
                        navController.navigate(NavDestinations.MIS_DATOS)
                    },
                    onNavigateToNotificaciones = {
                        navController.navigate(NavDestinations.NOTIFICACIONES)
                    },
                    onCerrarSesion = {
                        authViewModel.cerrarSesion()
                        navController.navigate(NavDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToFormulario = {
                        registroViewModel.prepararNuevoFormulario(authUiState.sesionUsuario ?: "Usuario")
                        navController.navigate(NavDestinations.FORMULARIO)
                    },
                    onNavigateToEditar = { id ->
                        registroViewModel.cargarParaEdicion(id)
                        navController.navigate(NavDestinations.createEditarRoute(id))
                    },
                    onNavigateToDetalle = { id ->
                        navController.navigate(NavDestinations.createDetalleRoute(id))
                    }
                )
            }

            composable(NavDestinations.FORMULARIO) {
                FormularioRegistroScreen(
                    viewModel = registroViewModel,
                    actividadesViewModel = actividadesViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegistroCreado = { nuevaId ->
                        navController.navigate(NavDestinations.createDetalleRoute(nuevaId)) {
                            popUpTo(NavDestinations.LISTA)
                        }
                    }
                )
            }

            composable(
                route = NavDestinations.EDITAR,
                arguments = listOf(navArgument("registroId") { type = NavType.StringType })
            ) { backStackEntry ->
                val registroId = backStackEntry.arguments?.getString("registroId") ?: ""
                LaunchedEffect(registroId) {
                    if (registroId.isNotBlank()) {
                        registroViewModel.cargarParaEdicion(registroId)
                    }
                }
                FormularioRegistroScreen(
                    viewModel = registroViewModel,
                    actividadesViewModel = actividadesViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegistroCreado = { id ->
                        navController.navigate(NavDestinations.createDetalleRoute(id)) {
                            popUpTo(NavDestinations.LISTA)
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
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditar = { id ->
                        registroViewModel.cargarParaEdicion(registroId)
                        navController.navigate(NavDestinations.createEditarRoute(id))
                    }
                )
            }
        }
    }
}
