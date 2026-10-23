package com.example.lemacdatalab.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.lemacdatalab.data.model.Solicitud
import com.example.lemacdatalab.data.repository.InMemorySolicitudRepository
import com.example.lemacdatalab.data.repository.SolicitudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class SolicitudViewModel(
    private val repository: SolicitudRepository = InMemorySolicitudRepository()
) : ViewModel() {

    private val _formUiState = MutableStateFlow(SolicitudFormUiState())
    val formUiState: StateFlow<SolicitudFormUiState> = _formUiState.asStateFlow()

    private val _solicitudes = MutableStateFlow(repository.cargar())
    val solicitudes: StateFlow<List<Solicitud>> = _solicitudes.asStateFlow()

    fun onCodigoChanged(codigo: String) {
        _formUiState.update { it.copy(codigoMuestra = codigo, codigoError = null) }
    }

    fun onSolicitanteChanged(solicitante: String) {
        _formUiState.update { it.copy(solicitante = solicitante, solicitanteError = null) }
    }

    fun onTipoEnsayoChanged(tipo: String) {
        _formUiState.update { it.copy(tipoEnsayo = tipo, tipoEnsayoError = null) }
    }

    fun onCantidadChanged(cantidad: String) {
        _formUiState.update { it.copy(cantidadText = cantidad, cantidadError = null) }
    }

    fun onUrgenteChanged(urgente: Boolean) {
        _formUiState.update { it.copy(esUrgente = urgente) }
    }

    fun onObservacionesChanged(obs: String) {
        _formUiState.update { it.copy(observaciones = obs) }
    }

    fun validarYGuardar(): Boolean {
        val currentState = _formUiState.value

        val codigoErr = if (currentState.codigoMuestra.isBlank()) "El código de muestra es obligatorio" else null
        val solicitanteErr = if (currentState.solicitante.isBlank()) "El nombre del solicitante es obligatorio" else null
        val tipoErr = if (currentState.tipoEnsayo.isBlank()) "Debe seleccionar un tipo de ensayo" else null

        // Regla de negocio: la cantidad debe ser un entero mayor a 0
        val cantidad = currentState.cantidadText.trim().toIntOrNull()?.takeIf { it > 0 }
        val cantidadErr = if (cantidad == null) "La cantidad debe ser un número entero mayor a 0" else null

        if (codigoErr != null || solicitanteErr != null || tipoErr != null || cantidad == null) {
            _formUiState.update {
                it.copy(
                    codigoError = codigoErr,
                    solicitanteError = solicitanteErr,
                    tipoEnsayoError = tipoErr,
                    cantidadError = cantidadErr
                )
            }
            return false
        }

        val nuevaSolicitud = Solicitud(
            id = UUID.randomUUID().toString(),
            codigoMuestra = currentState.codigoMuestra.trim(),
            solicitante = currentState.solicitante.trim(),
            tipoEnsayo = currentState.tipoEnsayo,
            cantidadMuestras = cantidad, // smart cast: ya no es nulo
            esUrgente = currentState.esUrgente,
            observaciones = currentState.observaciones.trim(),
            fechaRegistro = System.currentTimeMillis()
        )

        val nuevaLista = listOf(nuevaSolicitud) + _solicitudes.value
        _solicitudes.value = nuevaLista
        repository.guardar(nuevaLista)

        _formUiState.update {
            SolicitudFormUiState(
                mensajeExito = "¡Solicitud '${nuevaSolicitud.codigoMuestra}' registrada exitosamente!",
                solicitudCreadaId = nuevaSolicitud.id
            )
        }
        return true
    }

    fun limpiarMensajeExito() {
        _formUiState.update { it.copy(mensajeExito = null, solicitudCreadaId = null) }
    }

    fun getSolicitudById(id: String): Solicitud? = _solicitudes.value.find { it.id == id }
}
