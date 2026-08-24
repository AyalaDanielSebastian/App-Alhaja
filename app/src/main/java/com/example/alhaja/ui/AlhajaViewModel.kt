package com.example.alhaja.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.alhaja.domain.model.EstadoCatalogo
import com.example.alhaja.domain.model.Joya
import com.example.alhaja.domain.model.LugarJoyeria
import com.example.alhaja.domain.model.PreferenciasUsuario
import com.example.alhaja.domain.model.ResultadoUbicacion
import com.example.alhaja.domain.repository.JoyasRepository
import com.example.alhaja.domain.repository.LugaresRepository
import com.example.alhaja.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlhajaViewModel(
    private val joyasRepository: JoyasRepository,
    private val lugaresRepository: LugaresRepository,
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    val catalogo: StateFlow<EstadoCatalogo> = joyasRepository.estadoCatalogo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoCatalogo.Cargando)

    val favoritas: StateFlow<List<Joya>> = joyasRepository.observarFavoritas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lugares: StateFlow<List<LugarJoyeria>> = lugaresRepository.observarLugares()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preferencias: StateFlow<PreferenciasUsuario> = preferenciasRepository.preferencias
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            PreferenciasUsuario()
        )

    private val _mensajeHardware = MutableStateFlow<String?>(null)
    val mensajeHardware: StateFlow<String?> = _mensajeHardware.asStateFlow()

    init {
        recargarCatalogo()
    }

    fun recargarCatalogo() {
        viewModelScope.launch {
            joyasRepository.cargarCatalogo()
        }
    }

    fun obtenerJoya(id: Int): Joya? {
        val remota = when (val estado = catalogo.value) {
            is EstadoCatalogo.Exito -> estado.joyas.firstOrNull { it.id == id }
            else -> null
        }
        return remota ?: favoritas.value.firstOrNull { it.id == id }
            ?: joyasRepository.obtenerPorId(id)
    }

    fun alternarFavorita(joya: Joya) {
        viewModelScope.launch {
            joyasRepository.alternarFavorita(joya)
        }
    }

    fun guardarUbicacionActual() {
        viewModelScope.launch {
            _mensajeHardware.value = when (lugaresRepository.guardarUbicacionActual()) {
                ResultadoUbicacion.Guardado -> "Ubicación guardada en el dispositivo."
                ResultadoUbicacion.SinPermiso ->
                    "Sin permiso de ubicación. Alhaja no puede guardar el lugar."
                ResultadoUbicacion.NoDisponible ->
                    "No se obtuvo GPS. Activa la ubicación e inténtalo de nuevo."
            }
        }
    }

    fun guardarCaptura(fotoUri: String) {
        viewModelScope.launch {
            lugaresRepository.guardarCaptura(fotoUri)
            _mensajeHardware.value = "Foto de vitrina guardada en Room."
        }
    }

    fun eliminarLugar(id: Long) {
        viewModelScope.launch {
            lugaresRepository.eliminar(id)
        }
    }

    fun informarPermisoRechazado(tipo: String) {
        _mensajeHardware.value = "Permiso de $tipo rechazado. Puedes activarlo más tarde en Ajustes."
    }

    fun limpiarMensajeHardware() {
        _mensajeHardware.value = null
    }

    fun cambiarModoOscuro(activo: Boolean) {
        viewModelScope.launch {
            preferenciasRepository.cambiarModoOscuro(activo)
        }
    }

    fun cambiarMoneda(moneda: String) {
        viewModelScope.launch {
            preferenciasRepository.cambiarMoneda(moneda)
        }
    }

    class Factory(
        private val joyasRepository: JoyasRepository,
        private val lugaresRepository: LugaresRepository,
        private val preferenciasRepository: PreferenciasRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlhajaViewModel(
                joyasRepository,
                lugaresRepository,
                preferenciasRepository
            ) as T
        }
    }
}
