package com.example.alhaja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alhaja.ui.AlhajaApp
import com.example.alhaja.ui.AlhajaViewModel
import com.example.alhaja.ui.theme.AlhajaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AlhajaApplication

        setContent {
            val viewModel: AlhajaViewModel = viewModel(
                factory = AlhajaViewModel.Factory(
                    app.joyasRepository,
                    app.lugaresRepository,
                    app.preferenciasRepository
                )
            )
            val preferencias by viewModel.preferencias.collectAsState()

            AlhajaTheme(modoOscuro = preferencias.modoOscuro) {
                AlhajaApp(viewModel)
            }
        }
    }
}
