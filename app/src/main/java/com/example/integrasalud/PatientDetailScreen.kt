package com.example.integrasalud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PatientDetailScreen(
    patient: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPatient by remember { mutableStateOf(patient) }

    var showPrescribeDialog by rememberSaveable { mutableStateOf(false) }
    var nombreMedicamento by rememberSaveable { mutableStateOf("") }
    var cadaCuantasHoras by rememberSaveable { mutableStateOf("") }
    var dosisMg by rememberSaveable { mutableStateOf("") }
    var dialogError by rememberSaveable { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    if (showPrescribeDialog) {
        AlertDialog(
            onDismissRequest = { showPrescribeDialog = false },
            title = { Text("Recetar Medicamento") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nombreMedicamento,
                        onValueChange = { nombreMedicamento = it },
                        label = { Text("Nombre del medicamento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cadaCuantasHoras,
                        onValueChange = { cadaCuantasHoras = it },
                        label = { Text("Cada cuántas horas (ej. Cada 8 horas)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dosisMg,
                        onValueChange = { dosisMg = it },
                        label = { Text("Dosis en mg (ej. 500 mg)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

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
                            nombreMedicamento.isBlank() -> dialogError = "Ingresa el nombre del medicamento."
                            cadaCuantasHoras.isBlank() -> dialogError = "Ingresa la frecuencia en horas."
                            dosisMg.isBlank() -> dialogError = "Ingresa la dosis en mg."
                            else -> {
                                dialogError = null
                                val newReceta = Receta(
                                    nombreMedicamento = nombreMedicamento.trim(),
                                    cadaCuantasHoras = cadaCuantasHoras.trim(),
                                    dosisMg = dosisMg.trim()
                                )
                                val updatedRecetas = currentPatient.recetas + newReceta
                                val updatedPatient = currentPatient.copy(recetas = updatedRecetas)

                                UserRepository.updateUser(context, updatedPatient)
                                currentPatient = updatedPatient

                                nombreMedicamento = ""
                                cadaCuantasHoras = ""
                                dosisMg = ""
                                showPrescribeDialog = false
                            }
                        }
                    }
                ) {
                    Text("Guardar y Recetar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrescribeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Volver a pacientes")
            }
            Text(
                text = "Detalle del Paciente",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Patient Card Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = currentPatient.avatar, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = currentPatient.nombreCompleto,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentPatient.correo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Text(text = "Género: ${currentPatient.genero}")
                Text(text = "Fecha de Nacimiento: ${currentPatient.fechaNacimiento}")
                Text(text = "Peso: ${currentPatient.peso} kg")
                Text(text = "Antecedente Familiar Renal: ${currentPatient.antecedenteFamiliarRenal}")
                Text(text = "Hipertensión: ${if (currentPatient.tieneHipertension) "Sí" else "No"}")
                Text(text = "Diabetes: ${if (currentPatient.tieneDiabetes) "Sí" else "No"}")
                if (currentPatient.tieneOtraEnfermedad) {
                    Text(text = "Otra enfermedad: ${currentPatient.otraEnfermedadDetalle}")
                }
            }
        }

        // Prescriptions Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recetas Médicas (${currentPatient.recetas.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(onClick = { showPrescribeDialog = true }) {
                Text("+ Recetar")
            }
        }

        if (currentPatient.recetas.isEmpty()) {
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
                    Text(text = "No hay medicamentos recetados aún.")
                }
            }
        } else {
            currentPatient.recetas.forEach { receta ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "💊 ${receta.nombreMedicamento}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Dosis: ${receta.dosisMg}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Frecuencia: ${receta.cadaCuantasHoras}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
