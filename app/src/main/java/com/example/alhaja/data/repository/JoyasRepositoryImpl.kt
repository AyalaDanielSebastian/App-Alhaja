package com.example.alhaja.data.repository

import com.example.alhaja.data.local.JoyaFavoritaDao
import com.example.alhaja.data.local.JoyaFavoritaEntity
import com.example.alhaja.data.remote.JoyasApi
import com.example.alhaja.data.remote.ProductoRemotoDto
import com.example.alhaja.domain.model.EstadoCatalogo
import com.example.alhaja.domain.model.Joya
import com.example.alhaja.domain.repository.JoyasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.IOException

class JoyasRepositoryImpl(
    private val dao: JoyaFavoritaDao,
    private val api: JoyasApi
) : JoyasRepository {

    private val remotas = MutableStateFlow<List<Joya>>(emptyList())
    private val cargando = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    override val estadoCatalogo: Flow<EstadoCatalogo> = combine(
        remotas,
        dao.observarFavoritas(),
        cargando,
        error
    ) { piezas, favoritas, estaCargando, mensajeError ->
        val idsFavoritos = favoritas.mapTo(mutableSetOf()) { it.id }
        val catalogo = piezas.map { it.copy(esFavorita = it.id in idsFavoritos) }
        when {
            estaCargando && catalogo.isEmpty() -> EstadoCatalogo.Cargando
            mensajeError != null && catalogo.isEmpty() -> EstadoCatalogo.Error(mensajeError)
            else -> EstadoCatalogo.Exito(catalogo)
        }
    }

    override fun observarFavoritas(): Flow<List<Joya>> =
        dao.observarFavoritas().map { entidades -> entidades.map { it.toDomain() } }

    override fun obtenerPorId(id: Int): Joya? = remotas.value.firstOrNull { it.id == id }

    override suspend fun cargarCatalogo() {
        cargando.value = true
        error.value = null
        try {
            remotas.value = api.obtenerJoyas().map { it.toDomain() }
        } catch (_: IOException) {
            error.value = "Sin conexión. Revisa internet e inténtalo de nuevo."
        } catch (_: Exception) {
            error.value = "No se pudo cargar el catálogo remoto."
        } finally {
            cargando.value = false
        }
    }

    override suspend fun alternarFavorita(joya: Joya) {
        if (dao.esFavorita(joya.id)) {
            dao.eliminar(joya.id)
        } else {
            dao.guardar(joya.toEntity())
        }
    }
}

private fun ProductoRemotoDto.toDomain() = Joya(
    id = id,
    nombre = title,
    categoria = if (category.equals("jewelery", ignoreCase = true)) "Joyería" else category,
    descripcion = description,
    precio = price,
    material = materialDesde(title, description),
    imagenUrl = image
)

private fun materialDesde(titulo: String, descripcion: String): String {
    val texto = "$titulo $descripcion".lowercase()
    return when {
        "gold" in texto || "oro" in texto -> "Oro"
        "silver" in texto || "plata" in texto -> "Plata"
        "rose" in texto -> "Oro rosa"
        "pearl" in texto || "perla" in texto -> "Perla"
        else -> "Diseño de autor"
    }
}

private fun Joya.toEntity() = JoyaFavoritaEntity(
    id = id,
    nombre = nombre,
    categoria = categoria,
    descripcion = descripcion,
    precio = precio,
    material = material,
    imagenUrl = imagenUrl
)

private fun JoyaFavoritaEntity.toDomain() = Joya(
    id = id,
    nombre = nombre,
    categoria = categoria,
    descripcion = descripcion,
    precio = precio,
    material = material,
    imagenUrl = imagenUrl,
    esFavorita = true
)
