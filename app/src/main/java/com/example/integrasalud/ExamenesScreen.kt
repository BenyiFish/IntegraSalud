package com.example.integrasalud

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExamenesScreen(
    user: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentUserState by remember { mutableStateOf(user) }

    var showUploadDialog by rememberSaveable { mutableStateOf(false) }
    var examenTitulo by rememberSaveable { mutableStateOf("") }
    var examenSubtitulo by rememberSaveable { mutableStateOf("") }
    var examenValor by rememberSaveable { mutableStateOf("") }
    var examenEtapa by rememberSaveable { mutableStateOf("Etapa G3a") }
    var selectedFileUri by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFileName by rememberSaveable { mutableStateOf<String?>(null) }
    var dialogError by rememberSaveable { mutableStateOf<String?>(null) }

    // File picker launcher for documents (PDF) and images (JPG/PNG)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri.toString()
            selectedFileName = uri.lastPathSegment ?: "documento_examen"
        }
    }

    val latestUser = remember(currentUserState) {
        UserRepository.getUsers(context).find { it.correo.equals(user.correo, ignoreCase = true) } ?: currentUserState
    }

    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Subir Resultado de Examen") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = examenTitulo,
                        onValueChange = { examenTitulo = it },
                        label = { Text("Título del examen (ej. Función renal)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = examenSubtitulo,
                        onValueChange = { examenSubtitulo = it },
                        label = { Text("Subtítulo (ej. Tasa de filtración glomerular)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = examenValor,
                        onValueChange = { examenValor = it },
                        label = { Text("Valor numérico (ej. 54)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = examenEtapa,
                        onValueChange = { examenEtapa = it },
                        label = { Text("Etapa o diagnóstico (ej. Etapa G3a)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // File selection button inside dialog
                    Button(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/pdf", "image/*"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedFileName != null) "📄 $selectedFileName" else "📂 Seleccionar PDF o Imagen")
                    }

                    if (dialogError != null) {
                        Text(
                            text = dialogError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            examenTitulo.isBlank() -> dialogError = "Ingresa el título del examen."
                            examenValor.isBlank() -> dialogError = "Ingresa el valor del examen."
                            else -> {
                                dialogError = null
                                val newExamen = ExamenItem(
                                    titulo = examenTitulo.trim(),
                                    subtitulo = examenSubtitulo.ifBlank { "Resultado de laboratorio" },
                                    valor = examenValor.trim(),
                                    unidad = "mL/min/1,73 m²",
                                    etapa = examenEtapa.ifBlank { "Evaluado" },
                                    fecha = "Fecha de hoy",
                                    estado = "Revisado",
                                    archivoUri = selectedFileUri ?: ""
                                )
                                val updatedExamenes = latestUser.examenes + newExamen
                                val updatedUser = latestUser.copy(examenes = updatedExamenes)

                                UserRepository.updateUser(context, updatedUser)
                                currentUserState = updatedUser

                                examenTitulo = ""
                                examenSubtitulo = ""
                                examenValor = ""
                                selectedFileUri = null
                                selectedFileName = null
                                showUploadDialog = false
                            }
                        }
                    }
                ) {
                    Text("Subir y Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Mis exámenes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tus pendientes y resultados revisados",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Upload Card (Subir PDF o JPG)
        Card(
            onClick = { showUploadDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(64.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "📁⬆️", fontSize = 28.sp)
                    }
                }
                Text(
                    text = "Subir PDF o JPG",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006064)
                )
                Text(
                    text = "Máx. 20 MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00838F)
                )
                Button(
                    onClick = { showUploadDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1))
                ) {
                    Text("Elegir archivo")
                }
            }
        }

        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Resultados revisados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (latestUser.examenes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No hay exámenes registrados aún.")
                }
            }
        } else {
            latestUser.examenes.forEach { examen ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = examen.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = Color(0xFFC8E6C9),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = examen.estado,
                                    color = Color(0xFF2E7D32),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = examen.subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Metric Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = examen.valor,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = examen.unidad,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Surface(
                                    color = Color(0xFFEDE7F6),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = examen.etapa,
                                        color = Color(0xFF512DA8),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅 ${examen.fecha}" + if (examen.archivoUri.isNotBlank()) " | 📎 Adjunto" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            TextButton(onClick = { /* View result details */ }) {
                                Text("Ver resultado", color = Color(0xFF512DA8))
                            }
                        }
                    }
                }
            }
        }
    }
}
