package com.example.lemacdatalab.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lemacdatalab.data.model.Ambitos
import com.example.lemacdatalab.data.model.RegistroAutoReporte
import com.example.lemacdatalab.data.repository.InMemoryRegistroRepository
import com.example.lemacdatalab.data.repository.RegistroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class RegistroViewModel(
    private val repository: RegistroRepository = InMemoryRegistroRepository()
) : ViewModel() {

    private val _formUiState = MutableStateFlow(RegistroFormUiState())
    val formUiState: StateFlow<RegistroFormUiState> = _formUiState.asStateFlow()

    private val _filtroAmbito = MutableStateFlow("Todos")
    val filtroAmbito: StateFlow<String> = _filtroAmbito.asStateFlow()

    private val _registros = MutableStateFlow(repository.cargar())
    val registros: StateFlow<List<RegistroAutoReporte>> = _registros.asStateFlow()

    fun onAmbitoChanged(nuevoAmbito: String) {
        _formUiState.update {
            it.copy(
                ambitoSeleccionado = nuevoAmbito,
                tipoActividad = "", // Limpiar tipo al cambiar ámbito
                tipoActividadError = null,
                desencadenanteError = null
            )
        }
    }

    fun onTipoActividadChanged(tipo: String) {
        _formUiState.update { it.copy(tipoActividad = tipo, tipoActividadError = null) }
    }

    fun onNivelIntensidadChanged(intensidad: Float) {
        _formUiState.update { it.copy(nivelIntensidad = intensidad.coerceIn(1f, 10f)) }
    }

    fun onDesencadenanteChanged(desencadenante: String) {
        _formUiState.update { it.copy(desencadenanteEstrategia = desencadenante, desencadenanteError = null) }
    }

    fun onObservacionesChanged(obs: String) {
        _formUiState.update { it.copy(observacionesContexto = obs) }
    }

    fun onUbicacionObtenida(lat: Double, lon: Double) {
        _formUiState.update {
            it.copy(
                latitud = lat,
                longitud = lon,
                obteniendoUbicacion = false,
                ubicacionError = null
            )
        }
    }

    fun onUbicacionError(mensaje: String) {
        _formUiState.update {
            it.copy(
                obteniendoUbicacion = false,
                ubicacionError = mensaje
            )
        }
    }

    fun onObteniendoUbicacionChanged(obteniendo: Boolean) {
        _formUiState.update {
            it.copy(
                obteniendoUbicacion = obteniendo,
                ubicacionError = if (obteniendo) null else it.ubicacionError
            )
        }
    }

    fun onLimpiarUbicacion() {
        _formUiState.update {
            it.copy(
                latitud = null,
                longitud = null,
                ubicacionError = null
            )
        }
    }

    fun setFiltroAmbito(filtro: String) {
        _filtroAmbito.value = filtro
    }

    fun validarYGuardar(): Boolean {
        val currentState = _formUiState.value
        var hasError = false
        var tipoErr: String? = null
        var desencadenanteErr: String? = null

        // Validación 1: el tipo de actividad es obligatorio
        if (currentState.tipoActividad.isBlank()) {
            tipoErr = "Debe seleccionar o ingresar el tipo de actividad u habilidad"
            hasError = true
        }

        // Validación 2 (regla de dominio): desencadenante/estrategia obligatorio en DBT y Adicciones
        if ((currentState.ambitoSeleccionado == Ambitos.HABILIDADES_DBT ||
                    currentState.ambitoSeleccionado == Ambitos.ADICCIONES) &&
            currentState.desencadenanteEstrategia.isBlank()
        ) {
            desencadenanteErr = "Para ${currentState.ambitoSeleccionado} es obligatorio registrar la situación, desencadenante o acción protectora"
            hasError = true
        }

        if (hasError) {
            _formUiState.update {
                it.copy(tipoActividadError = tipoErr, desencadenanteError = desencadenanteErr)
            }
            return false
        }

        val nuevoRegistro = RegistroAutoReporte(
            id = UUID.randomUUID().toString(),
            aliasUsuario = currentState.aliasUsuario,
            ambito = currentState.ambitoSeleccionado,
            tipoActividad = currentState.tipoActividad.trim(),
            nivelIntensidad = currentState.nivelIntensidad.toInt(),
            desencadenanteEstrategia = currentState.desencadenanteEstrategia.trim(),
            observacionesContexto = currentState.observacionesContexto.trim(),
            fechaHora = System.currentTimeMillis(),
            latitud = currentState.latitud,
            longitud = currentState.longitud
        )

        val nuevaLista = listOf(nuevoRegistro) + _registros.value
        _registros.value = nuevaLista
        repository.guardar(nuevaLista)

        _formUiState.update {
            RegistroFormUiState(
                ambitoSeleccionado = currentState.ambitoSeleccionado,
                mensajeExito = "¡Registro en '${nuevoRegistro.ambito}' guardado exitosamente!",
                registroCreadoId = nuevoRegistro.id
            )
        }
        return true
    }

    fun eliminarRegistro(id: String) {
        val nuevaLista = _registros.value.filterNot { it.id == id }
        _registros.value = nuevaLista
        repository.guardar(nuevaLista)
    }

    fun limpiarMensajeExito() {
        _formUiState.update { it.copy(mensajeExito = null, registroCreadoId = null) }
    }

    fun getRegistroById(id: String): RegistroAutoReporte? = _registros.value.find { it.id == id }
}
