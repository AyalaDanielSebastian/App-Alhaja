package com.example.alhaja.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.alhaja.domain.model.LugarJoyeria
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLugares(
    lugares: List<LugarJoyeria>,
    mensaje: String?,
    onGuardarUbicacion: () -> Unit,
    onGuardarFoto: (String) -> Unit,
    onEliminar: (Long) -> Unit,
    onPermisoRechazado: (String) -> Unit,
    onMensajeVisto: () -> Unit
) {
    val contexto = LocalContext.current
    val actividad = contexto as? Activity
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }
    var ubicacionRechazadaPermanente by remember { mutableStateOf(false) }
    var camaraRechazadaPermanente by remember { mutableStateOf(false) }

    val lanzadorUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedido = resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            ubicacionRechazadaPermanente = false
            onGuardarUbicacion()
        } else {
            val pedirDeNuevo = actividad?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: false
            ubicacionRechazadaPermanente = !pedirDeNuevo
            onPermisoRechazado("ubicación")
        }
    }

    val lanzadorCamaraCaptura = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val uri = uriPendiente
        if (exito && uri != null) {
            onGuardarFoto(uri.toString())
        }
        uriPendiente = null
    }

    val lanzadorCamaraPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            camaraRechazadaPermanente = false
            val uri = crearUriFoto(contexto)
            uriPendiente = uri
            lanzadorCamaraCaptura.launch(uri)
        } else {
            val pedirDeNuevo = actividad?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: false
            camaraRechazadaPermanente = !pedirDeNuevo
            onPermisoRechazado("cámara")
        }
    }

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(4_000)
            onMensajeVisto()
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Lugares") })
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Guarda la joyería que visites con GPS y una foto de la vitrina.",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = {
                    val fino = ContextCompat.checkSelfPermission(
                        contexto,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val aproximado = ContextCompat.checkSelfPermission(
                        contexto,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (fino || aproximado) {
                        onGuardarUbicacion()
                    } else {
                        lanzadorUbicacion.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Guardar mi ubicación")
            }
            OutlinedButton(
                onClick = {
                    val concedido = ContextCompat.checkSelfPermission(
                        contexto,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (concedido) {
                        val uri = crearUriFoto(contexto)
                        uriPendiente = uri
                        lanzadorCamaraCaptura.launch(uri)
                    } else {
                        lanzadorCamaraPermiso.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Fotografiar vitrina")
            }
            if (ubicacionRechazadaPermanente || camaraRechazadaPermanente) {
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Un permiso quedó denegado. Alhaja sigue funcionando, pero esa función no estará disponible hasta que lo actives.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", contexto.packageName, null)
                            )
                            contexto.startActivity(intent)
                        }) {
                            Text("Abrir ajustes del sistema")
                        }
                    }
                }
            }
            mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
        }
        if (lugares.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.Place, contentDescription = null, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text("Todavía no hay lugares ni fotos guardadas")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lugares, key = { it.id }) { lugar ->
                    TarjetaLugar(
                        lugar = lugar,
                        onAbrirMapa = {
                            val uri = Uri.parse(
                                "geo:${lugar.latitud},${lugar.longitud}?q=${lugar.latitud},${lugar.longitud}(${Uri.encode(lugar.nombre)})"
                            )
                            runCatching {
                                contexto.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        },
                        onEliminar = { onEliminar(lugar.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaLugar(
    lugar: LugarJoyeria,
    onAbrirMapa: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            lugar.fotoUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = lugar.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(lugar.nombre, fontWeight = FontWeight.SemiBold)
            if (lugar.latitud != 0.0 || lugar.longitud != 0.0) {
                Text(
                    String.format(Locale.US, "%.5f, %.5f", lugar.latitud, lugar.longitud),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (lugar.latitud != 0.0 || lugar.longitud != 0.0) {
                    IconButton(onClick = onAbrirMapa) {
                        Icon(Icons.Outlined.Map, contentDescription = "Abrir mapa")
                    }
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

private fun crearUriFoto(contexto: android.content.Context): Uri {
    val carpeta = File(contexto.filesDir, "capturas").apply { mkdirs() }
    val archivo = File(carpeta, "vitrina_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        contexto,
        "${contexto.packageName}.fileprovider",
        archivo
    )
}
