package com.example.integrasalud

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    user: UserProfile,
    onBack: () -> Unit,
    onProfileUpdated: (UserProfile) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var nombres by rememberSaveable { mutableStateOf(user.nombres) }
    var apellidos by rememberSaveable { mutableStateOf(user.apellidos) }
    var fechaNacimiento by rememberSaveable { mutableStateOf(user.fechaNacimiento) }
    var peso by rememberSaveable { mutableStateOf(user.peso.toString()) }
    var genero by rememberSaveable { mutableStateOf(user.genero) }
    var selectedAvatar by rememberSaveable { mutableStateOf(user.avatar) }

    var showAvatarDialog by rememberSaveable { mutableStateOf(false) }
    var successMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    val availableAvatars = listOf("👤", "👨", "👩", "🧑", "⭐", "🩺", "👨‍⚕️", "👩‍⚕️")

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Elige tu avatar") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableAvatars.chunked(4).forEach { rowAvatars ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            rowAvatars.forEach { av ->
                                Surface(
                                    shape = CircleShape,
                                    color = if (selectedAvatar == av) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clickable {
                                            selectedAvatar = av
                                            showAvatarDialog = false
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = av, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Volver")
            }
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Profile Picture Avatar clickable to change
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .size(90.dp)
                .clickable { showAvatarDialog = true }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = selectedAvatar,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }

        TextButton(onClick = { showAvatarDialog = true }) {
            Text("Cambiar foto de perfil (Avatar)")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (successMessage != null) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val pesoDouble = peso.toDoubleOrNull()
                        if (nombres.isBlank() || apellidos.isBlank() || pesoDouble == null || pesoDouble <= 0.0) {
                            errorMessage = "Por favor verifica que los campos sean válidos."
                            successMessage = null
                        } else {
                            errorMessage = null
                            val updatedUser = user.copy(
                                nombres = nombres.trim(),
                                apellidos = apellidos.trim(),
                                genero = genero,
                                fechaNacimiento = fechaNacimiento.trim(),
                                peso = pesoDouble,
                                avatar = selectedAvatar
                            )
                            UserRepository.updateUser(context, updatedUser)
                            successMessage = "¡Perfil actualizado con éxito!"
                            onProfileUpdated(updatedUser)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logout Button ONLY on Profile Screen
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
    }
}
