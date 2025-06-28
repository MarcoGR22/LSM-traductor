package com.example.lsmtraductor.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.lsmtraductor.ui.theme.LSMTraductorTheme
import com.example.lsmtraductor.viewmodel.TranslatorViewModel
import com.example.lsmtraductor.viewmodel.TranslatorViewModelFactory
import com.example.lsmtraductor.R

class MainActivity : ComponentActivity() {
    private lateinit var windowRef: Window

    // Solicitar permisos de cámara
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) showToast("Permiso de cámara requerido")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        windowRef = window

        // Configurar la ventana para mantener la pantalla encendida cuando la cámara esté activa
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Verifica permisos de cámara al iniciar
        if (!hasCameraPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // Configura el contenido de la UI usando Jetpack Compose
        setContent {
            val context = LocalContext.current

            // Se obtiene el ViewModel con su Factory
            val viewModel: TranslatorViewModel by viewModels {
                TranslatorViewModelFactory(context.applicationContext)
            }

            // Observa el texto detectado y errores desde el ViewModel
            val detectedText by viewModel.textoDetectado.collectAsState()
            val error by viewModel.errorMessage.collectAsState()

            // Variables para manejar la cámara
            var isCameraActive by remember { mutableStateOf(false) }
            var useBackCamera by remember { mutableStateOf(true) }
            var flashEnabled by remember { mutableStateOf(false) }

            // Aplicación del tema visual
            LSMTraductorTheme {
                // Column para todo el contenido y agregar scroll
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()), // Agregar scroll
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Título principal
                    Text(
                        text = "Traductor LSM",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Área de la cámara o logo si está apagada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.8f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (isCameraActive) {
                            CameraPreview(
                                modifier = Modifier.fillMaxSize(),
                                useBackCamera = useBackCamera,
                                flashEnabled = flashEnabled,
                                isCameraActive = isCameraActive
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.lsm_logo),
                                    contentDescription = "LSM Logo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Muestra el texto detectado (letras traducidas)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Texto detectado:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (detectedText.isEmpty()) {
                                    Text(
                                        text = "Las letras aparecerán aquí...",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                } else {
                                    detectedText.forEach { char ->
                                        Text(
                                            text = char.toString(),
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontSize = 32.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botones de control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (isCameraActive) {

                            ActionButton(
                                icon = Icons.Default.Refresh,
                                text = "Reiniciar",
                                onClick = { viewModel.reiniciar() }
                            )

                            ActionButton(
                                icon = Icons.Default.Warning,
                                text = "Apagar",
                                onClick = {
                                    isCameraActive = false
                                    // Permitir que la pantalla se apague cuando la cámara está apagada
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                }
                            )
                        } else {
                            ActionButton(
                                icon = Icons.Default.PlayArrow,
                                text = "Iniciar",
                                onClick = {
                                    if (hasCameraPermission()) {
                                        isCameraActive = true
                                        // Mantener la pantalla encendida cuando la cámara está activa
                                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                    } else {
                                        showToast("Permiso de cámara no concedido")
                                    }
                                },
                                enabled = hasCameraPermission()
                            )
                        }
                    }

                    // Muestra error si hay alguno
                    if (!error.isNullOrBlank()) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ActionButton(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        text: String,
        onClick: () -> Unit,
        enabled: Boolean = true
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.width(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = text)
                Text(text, fontSize = 12.sp)
            }
        }
    }

    // Verifica si se ha concedido el permiso de cámara.
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Muestra un Toast con el mensaje recibido.
        private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
