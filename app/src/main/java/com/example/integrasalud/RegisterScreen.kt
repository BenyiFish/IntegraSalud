package com.example.integrasalud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Form fields
    var nombres by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmarContrasena by rememberSaveable { mutableStateOf("") }
    var genero by rememberSaveable { mutableStateOf("Masculino") }
    var fechaNacimiento by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }

    // Antecedentes
    var antecedenteFamiliarRenal by rememberSaveable { mutableStateOf("No") } // "Sí", "No", "No lo sé"
    var tieneHipertension by rememberSaveable { mutableStateOf(false) }
    var tieneDiabetes by rememberSaveable { mutableStateOf(false) }
    var tieneOtraEnfermedad by rememberSaveable { mutableStateOf(false) }
    var otraEnfermedadDetalle by rememberSaveable { mutableStateOf("") }
    var documentoAdjunto by rememberSaveable { mutableStateOf(false) }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var registeredUser by remember { mutableStateOf<UserProfile?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IntegraSalud",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cuestionario para el\nregistro del usuario",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (registeredUser == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Información básica
                    Text(
                        text = "Información básica",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Completa todos los campos con tus datos actuales para guardar y crear tu perfil",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Género
                    Text(text = "Género:", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Masculino", "Femenino", "Otro").forEach { gen ->
                            FilterChip(
                                selected = genero == gen,
                                onClick = { genero = gen },
                                label = { Text(gen) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = nombres,
                        onValueChange = { nombres = it },
                        label = { Text("Nombres") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = apellidos,
                        onValueChange = { apellidos = it },
                        label = { Text("Apellidos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo Electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fechaNacimiento,
                        onValueChange = { fechaNacimiento = it },
                        label = { Text("Fecha de Nacimiento (DD - MM - AAAA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = peso,
                        onValueChange = { peso = it },
                        label = { Text("Peso (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmarContrasena,
                        onValueChange = { confirmarContrasena = it },
                        label = { Text("Confirmar Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    // Antecedentes familiares
                    Text(
                        text = "Antecedentes familiares.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "¿Tiene algún familiar directo que haya sido diagnosticado o esté en tratamiento por alguna enfermedad renal?",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Sí", "No", "No lo sé").forEach { op ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = antecedenteFamiliarRenal == op,
                                    onClick = { antecedenteFamiliarRenal = op }
                                )
                                Text(text = op, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }

                    HorizontalDivider()

                    // Antecedentes clínicos
                    Text(
                        text = "Antecedentes clínicos.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = tieneHipertension,
                                onCheckedChange = { tieneHipertension = it }
                            )
                            Text(text = "Hipertensión", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = tieneDiabetes,
                                onCheckedChange = { tieneDiabetes = it }
                            )
                            Text(text = "Diabetes", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = tieneOtraEnfermedad,
                            onCheckedChange = { tieneOtraEnfermedad = it }
                        )
                        Text(text = "Otra (especificar)", style = MaterialTheme.typography.bodyMedium)
                    }

                    if (tieneOtraEnfermedad) {
                        OutlinedTextField(
                            value = otraEnfermedadDetalle,
                            onValueChange = { otraEnfermedadDetalle = it },
                            label = { Text("Especificar otra enfermedad diagnosticada.") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Adjuntar evidencia
                    Card(
                        onClick = { documentoAdjunto = !documentoAdjunto },
                        colors = CardDefaults.cardColors(
                            containerColor = if (documentoAdjunto) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = if (documentoAdjunto) "✅ Documento adjuntado con éxito" else "📁 Adjuntar evidencia o documento del diagnóstico.")
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            when {
                                nombres.isBlank() -> errorMessage = "Por favor ingresa tus nombres."
                                apellidos.isBlank() -> errorMessage = "Por favor ingresa tus apellidos."
                                correo.isBlank() || !correo.contains("@") -> errorMessage = "Ingresa un correo electrónico válido."
                                fechaNacimiento.isBlank() -> errorMessage = "Ingresa tu fecha de nacimiento."
                                peso.isBlank() -> errorMessage = "Ingresa tu peso."
                                contrasena.isBlank() -> errorMessage = "Ingresa una contraseña."
                                confirmarContrasena.isBlank() -> errorMessage = "Confirma tu contraseña."
                                contrasena != confirmarContrasena -> errorMessage = "Las contraseñas no coinciden."
                                else -> {
                                    val pesoDouble = peso.toDoubleOrNull()
                                    if (pesoDouble == null || pesoDouble <= 0.0) {
                                        errorMessage = "Ingresa un peso válido."
                                    } else {
                                        val newUser = UserProfile(
                                            nombres = nombres.trim(),
                                            apellidos = apellidos.trim(),
                                            correo = correo.trim(),
                                            contrasena = contrasena,
                                            genero = genero,
                                            fechaNacimiento = fechaNacimiento.trim(),
                                            peso = pesoDouble,
                                            rol = "PACIENTE", // Default role assigned by admin / system
                                            antecedenteFamiliarRenal = antecedenteFamiliarRenal,
                                            tieneHipertension = tieneHipertension,
                                            tieneDiabetes = tieneDiabetes,
                                            tieneOtraEnfermedad = tieneOtraEnfermedad,
                                            otraEnfermedadDetalle = otraEnfermedadDetalle.trim()
                                        )
                                        val saved = UserRepository.saveUser(context, newUser)
                                        if (!saved) {
                                            errorMessage = "El correo ya está registrado."
                                        } else {
                                            errorMessage = null
                                            registeredUser = newUser
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Guardar y finalizar")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¡Registro Exitoso!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Rol: ${registeredUser!!.rol}")
                    Text(text = "Nombre: ${registeredUser!!.nombreCompleto}")
                    Text(text = "Correo: ${registeredUser!!.correo}")
                    Text(text = "Género: ${registeredUser!!.genero}")
                    Text(text = "Fecha de Nacimiento: ${registeredUser!!.fechaNacimiento}")
                    Text(text = "Peso: ${registeredUser!!.peso} kg")
                    Text(text = "Antecedente Familiar Renal: ${registeredUser!!.antecedenteFamiliarRenal}")
                    Text(text = "Hipertensión: ${if (registeredUser!!.tieneHipertension) "Sí" else "No"}")
                    Text(text = "Diabetes: ${if (registeredUser!!.tieneDiabetes) "Sí" else "No"}")
                    if (registeredUser!!.tieneOtraEnfermedad) {
                        Text(text = "Otra: ${registeredUser!!.otraEnfermedadDetalle}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            registeredUser = null
                            nombres = ""
                            apellidos = ""
                            correo = ""
                            contrasena = ""
                            confirmarContrasena = ""
                            genero = "Masculino"
                            fechaNacimiento = ""
                            peso = ""
                            antecedenteFamiliarRenal = "No"
                            tieneHipertension = false
                            tieneDiabetes = false
                            tieneOtraEnfermedad = false
                            otraEnfermedadDetalle = ""
                            documentoAdjunto = false
                            errorMessage = null
                        }
                    ) {
                        Text("Nuevo Registro")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ir a iniciar sesión")
        }
    }
}
