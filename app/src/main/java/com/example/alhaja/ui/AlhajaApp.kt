package com.example.alhaja.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.alhaja.domain.model.EstadoCatalogo
import com.example.alhaja.domain.model.Joya
import com.example.alhaja.domain.model.PreferenciasUsuario
import com.example.alhaja.ui.screens.PantallaLugares
import java.util.Locale

private object Rutas {
    const val CATALOGO = "catalogo"
    const val FAVORITAS = "favoritas"
    const val LUGARES = "lugares"
    const val AJUSTES = "ajustes"
    const val DETALLE = "detalle/{joyaId}"
    fun detalle(id: Int) = "detalle/$id"
}

private data class Destino(
    val ruta: String,
    val titulo: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector
)

private val destinos = listOf(
    Destino(Rutas.CATALOGO, "Catálogo", Icons.Outlined.Storefront),
    Destino(Rutas.FAVORITAS, "Favoritas", Icons.Outlined.Favorite),
    Destino(Rutas.LUGARES, "Lugares", Icons.Outlined.Place),
    Destino(Rutas.AJUSTES, "Ajustes", Icons.Outlined.Settings)
)

@Composable
fun AlhajaApp(viewModel: AlhajaViewModel) {
    val navController = rememberNavController()
    val catalogo by viewModel.catalogo.collectAsState()
    val favoritas by viewModel.favoritas.collectAsState()
    val lugares by viewModel.lugares.collectAsState()
    val preferencias by viewModel.preferencias.collectAsState()
    val mensajeHardware by viewModel.mensajeHardware.collectAsState()
    val backStack by navController.currentBackStackEntryAsState()
    val rutaActual = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (rutaActual in destinos.map { it.ruta }) {
                BarraNavegacion(navController, rutaActual)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.CATALOGO,
            modifier = Modifier.padding(padding)
        ) {
            composable(Rutas.CATALOGO) {
                PantallaCatalogo(
                    estado = catalogo,
                    moneda = preferencias.moneda,
                    onAbrirDetalle = { navController.navigate(Rutas.detalle(it)) },
                    onAlternarFavorita = viewModel::alternarFavorita,
                    onReintentar = viewModel::recargarCatalogo
                )
            }
            composable(Rutas.FAVORITAS) {
                PantallaFavoritas(
                    joyas = favoritas,
                    moneda = preferencias.moneda,
                    onAbrirDetalle = { navController.navigate(Rutas.detalle(it)) },
                    onAlternarFavorita = viewModel::alternarFavorita
                )
            }
            composable(Rutas.LUGARES) {
                PantallaLugares(
                    lugares = lugares,
                    mensaje = mensajeHardware,
                    onGuardarUbicacion = viewModel::guardarUbicacionActual,
                    onGuardarFoto = viewModel::guardarCaptura,
                    onEliminar = viewModel::eliminarLugar,
                    onPermisoRechazado = viewModel::informarPermisoRechazado,
                    onMensajeVisto = viewModel::limpiarMensajeHardware
                )
            }
            composable(Rutas.AJUSTES) {
                PantallaAjustes(
                    preferencias = preferencias,
                    onModoOscuro = viewModel::cambiarModoOscuro,
                    onMoneda = viewModel::cambiarMoneda
                )
            }
            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("joyaId") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("joyaId") ?: -1
                val delCatalogo = (catalogo as? EstadoCatalogo.Exito)?.joyas?.firstOrNull { it.id == id }
                val joya = delCatalogo
                    ?: favoritas.firstOrNull { it.id == id }
                    ?: viewModel.obtenerJoya(id)
                PantallaDetalle(
                    joya = joya,
                    moneda = preferencias.moneda,
                    onVolver = navController::popBackStack,
                    onAlternarFavorita = viewModel::alternarFavorita
                )
            }
        }
    }
}

@Composable
private fun BarraNavegacion(
    navController: NavHostController,
    rutaActual: String?
) {
    NavigationBar {
        destinos.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = {
                    navController.navigate(destino.ruta) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destino.icono, contentDescription = destino.titulo) },
                label = { Text(destino.titulo) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaCatalogo(
    estado: EstadoCatalogo,
    moneda: String,
    onAbrirDetalle: (Int) -> Unit,
    onAlternarFavorita: (Joya) -> Unit,
    onReintentar: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Alhaja", fontWeight = FontWeight.Bold)
                    Text(
                        "Catálogo remoto · Fake Store API",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
        when (estado) {
            EstadoCatalogo.Cargando -> EstadoCarga()
            is EstadoCatalogo.Error -> EstadoError(
                mensaje = estado.mensaje,
                onReintentar = onReintentar
            )
            is EstadoCatalogo.Exito -> ListaJoyas(
                joyas = estado.joyas,
                moneda = moneda,
                onAbrirDetalle = onAbrirDetalle,
                onAlternarFavorita = onAlternarFavorita
            )
        }
    }
}

@Composable
private fun EstadoCarga() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Cargando joyas…")
        }
    }
}

@Composable
private fun EstadoError(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(mensaje, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onReintentar) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun ListaJoyas(
    joyas: List<Joya>,
    moneda: String,
    onAbrirDetalle: (Int) -> Unit,
    onAlternarFavorita: (Joya) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(joyas, key = { it.id }) { joya ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAbrirDetalle(joya.id) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImagenJoya(
                        url = joya.imagenUrl,
                        descripcion = joya.nombre,
                        modifier = Modifier.size(72.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(joya.categoria, style = MaterialTheme.typography.labelMedium)
                        Text(
                            joya.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            joya.descripcion,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            precio(joya.precio, moneda),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { onAlternarFavorita(joya) }) {
                        Icon(
                            if (joya.esFavorita) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Cambiar favorita",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaFavoritas(
    joyas: List<Joya>,
    moneda: String,
    onAbrirDetalle: (Int) -> Unit,
    onAlternarFavorita: (Joya) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Mis favoritas") })
        if (joyas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Aún no guardaste ninguna joya")
                }
            }
        } else {
            ListaJoyas(joyas, moneda, onAbrirDetalle, onAlternarFavorita)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaDetalle(
    joya: Joya?,
    moneda: String,
    onVolver: () -> Unit,
    onAlternarFavorita: (Joya) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(joya?.nombre ?: "Detalle") },
            navigationIcon = {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                joya?.let {
                    IconButton(onClick = { onAlternarFavorita(it) }) {
                        Icon(
                            if (it.esFavorita) Icons.Filled.Favorite
                            else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Cambiar favorita"
                        )
                    }
                }
            }
        )
        if (joya == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontró la joya")
            }
            return
        }
        Column(Modifier.padding(24.dp)) {
            ImagenJoya(
                url = joya.imagenUrl,
                descripcion = joya.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Text(joya.categoria, color = MaterialTheme.colorScheme.primary)
            Text(
                joya.nombre,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(joya.descripcion, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Material") },
                supportingContent = { Text(joya.material) }
            )
            ListItem(
                headlineContent = { Text("Precio") },
                supportingContent = {
                    Text(
                        precio(joya.precio, moneda),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaAjustes(
    preferencias: PreferenciasUsuario,
    onModoOscuro: (Boolean) -> Unit,
    onMoneda: (String) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Ajustes") })
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Modo oscuro", style = MaterialTheme.typography.titleMedium)
                Text("Guardar apariencia en DataStore")
            }
            Switch(
                checked = preferencias.modoOscuro,
                onCheckedChange = onModoOscuro
            )
        }
        HorizontalDivider()
        Column(Modifier.padding(20.dp)) {
            Text("Moneda", style = MaterialTheme.typography.titleMedium)
            Text("Elige cómo mostrar los precios")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("USD", "COP").forEach { moneda ->
                    FilterChip(
                        selected = preferencias.moneda == moneda,
                        onClick = { onMoneda(moneda) },
                        label = { Text(moneda) }
                    )
                }
            }
        }
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Acerca de Alhaja") },
            supportingContent = {
                Text("API Fake Store · GPS, cámara, Room y DataStore")
            },
            leadingContent = {
                Icon(Icons.Outlined.Diamond, contentDescription = null)
            }
        )
    }
}

@Composable
private fun ImagenJoya(
    url: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = url,
        contentDescription = descripcion,
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}

private fun precio(valorUsd: Double, moneda: String): String {
    return if (moneda == "COP") {
        "$ ${String.format(Locale.US, "%,.0f", valorUsd * 4_000)} COP"
    } else {
        "$ ${String.format(Locale.US, "%.2f", valorUsd)} USD"
    }
}
