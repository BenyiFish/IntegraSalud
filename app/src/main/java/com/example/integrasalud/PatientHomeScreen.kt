package com.example.integrasalud

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientHomeScreen(
    user: UserProfile,
    onNavigateToProfile: () -> Unit,
    onNavigateToRecommendedFoods: () -> Unit,
    onNavigateToRestrictedFoods: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Request notification permission for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0: Inicio, 1: Medicamentos, 2: Exámenes, 3: Evolución, 4: Perfil
    val currentDate = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")).format(Date())
        .replaceFirstChar { it.uppercase() }

    val scrollState = rememberScrollState()

    // Add medication dialog state
    var showAddMedDialog by rememberSaveable { mutableStateOf(false) }
    var nombreMed by rememberSaveable { mutableStateOf("") }
    var dosisNumero by rememberSaveable { mutableStateOf("") }
    var unidadDosis by rememberSaveable { mutableStateOf("mg") } // "mg" o "g"
    var horasMed by rememberSaveable { mutableStateOf("") }
    var dialogError by rememberSaveable { mutableStateOf<String?>(null) }
    var currentUserState by remember { mutableStateOf(user) }

    // Edit/Delete medication dialog state
    var editingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var editNombreMed by rememberSaveable { mutableStateOf("") }
    var editDosisNumero by rememberSaveable { mutableStateOf("") }
    var editUnidadDosis by rememberSaveable { mutableStateOf("mg") }
    var editHorasMed by rememberSaveable { mutableStateOf("") }
    var editDialogError by rememberSaveable { mutableStateOf<String?>(null) }

    if (showAddMedDialog) {
        AlertDialog(
            onDismissRequest = { showAddMedDialog = false },
            title = { Text("Agregar Medicamento") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nombreMed,
                        onValueChange = { nombreMed = it },
                        label = { Text("Nombre del medicamento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dosisNumero,
                        onValueChange = { dosisNumero = it },
                        label = { Text("Dosis recomendada (solo número)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Unidad de medida:", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = unidadDosis == "mg",
                            onClick = { unidadDosis = "mg" },
                            label = { Text("Miligramos (mg)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = unidadDosis == "g",
                            onClick = { unidadDosis = "g" },
                            label = { Text("Gramos (g)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = horasMed,
                        onValueChange = { horasMed = it },
                        label = { Text("Cada cuántas horas (ej. 8)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        val dosisVal = dosisNumero.toDoubleOrNull()
                        val hoursInterval = horasMed.toLongOrNull()
                        when {
                            nombreMed.isBlank() -> dialogError = "Ingresa el nombre del medicamento."
                            dosisVal == null || dosisVal <= 0.0 -> dialogError = "Ingresa una dosis numérica válida."
                            hoursInterval == null || hoursInterval <= 0L -> dialogError = "Ingresa un intervalo de horas válido."
                            else -> {
                                dialogError = null
                                val dosisCompleta = "$dosisNumero $unidadDosis"
                                val newReceta = Receta(
                                    nombreMedicamento = nombreMed.trim(),
                                    cadaCuantasHoras = "Cada $hoursInterval horas",
                                    dosisMg = dosisCompleta
                                )
                                val updatedRecetas = currentUserState.recetas + newReceta
                                val updatedUser = currentUserState.copy(recetas = updatedRecetas)

                                UserRepository.updateUser(context, updatedUser)
                                currentUserState = updatedUser

                                scheduleMedicationAlarm(context, newReceta.nombreMedicamento, newReceta.dosisMg, hoursInterval)

                                nombreMed = ""
                                dosisNumero = ""
                                unidadDosis = "mg"
                                horasMed = ""
                                showAddMedDialog = false
                            }
                        }
                    }
                ) {
                    Text("Guardar y Programar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (editingIndex != null && editingIndex!! in currentUserState.recetas.indices) {
        AlertDialog(
            onDismissRequest = { editingIndex = null },
            title = { Text("Editar o Eliminar Medicamento") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editNombreMed,
                        onValueChange = { editNombreMed = it },
                        label = { Text("Nombre del medicamento") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDosisNumero,
                        onValueChange = { editDosisNumero = it },
                        label = { Text("Dosis recomendada (solo número)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Unidad de medida:", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = editUnidadDosis == "mg",
                            onClick = { editUnidadDosis = "mg" },
                            label = { Text("Miligramos (mg)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = editUnidadDosis == "g",
                            onClick = { editUnidadDosis = "g" },
                            label = { Text("Gramos (g)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = editHorasMed,
                        onValueChange = { editHorasMed = it },
                        label = { Text("Cada cuántas horas (ej. 8)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (editDialogError != null) {
                        Text(
                            text = editDialogError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            val mutableList = currentUserState.recetas.toMutableList()
                            mutableList.removeAt(editingIndex!!)
                            val updatedUser = currentUserState.copy(recetas = mutableList)
                            UserRepository.updateUser(context, updatedUser)
                            currentUserState = updatedUser
                            editingIndex = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                    Button(
                        onClick = {
                            val dosisVal = editDosisNumero.toDoubleOrNull()
                            val hoursInterval = editHorasMed.toLongOrNull()
                            when {
                                editNombreMed.isBlank() -> editDialogError = "Ingresa el nombre del medicamento."
                                dosisVal == null || dosisVal <= 0.0 -> editDialogError = "Ingresa una dosis numérica válida."
                                hoursInterval == null || hoursInterval <= 0L -> editDialogError = "Ingresa un intervalo de horas válido."
                                else -> {
                                    editDialogError = null
                                    val dosisCompleta = "$editDosisNumero $editUnidadDosis"
                                    val updatedReceta = Receta(
                                        nombreMedicamento = editNombreMed.trim(),
                                        cadaCuantasHoras = "Cada $hoursInterval horas",
                                        dosisMg = dosisCompleta
                                    )
                                    val mutableList = currentUserState.recetas.toMutableList()
                                    mutableList[editingIndex!!] = updatedReceta
                                    val updatedUser = currentUserState.copy(recetas = mutableList)

                                    UserRepository.updateUser(context, updatedUser)
                                    currentUserState = updatedUser
                                    scheduleMedicationAlarm(context, updatedReceta.nombreMedicamento, updatedReceta.dosisMg, hoursInterval)

                                    editingIndex = null
                                }
                            }
                        }
                    ) {
                        Text("Actualizar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { editingIndex = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("🏠", fontSize = 20.sp) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("💊", fontSize = 20.sp) },
                    label = { Text("Medicamentos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("📋", fontSize = 20.sp) },
                    label = { Text("Exámenes") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("📈", fontSize = 20.sp) },
                    label = { Text("Evolución") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        onNavigateToProfile()
                    },
                    icon = { Text("👤", fontSize = 20.sp) },
                    label = { Text("Perfil") }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Rectangle card with profile picture icon, name and current date
            Card(
                onClick = onNavigateToProfile,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.avatar,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = user.nombreCompleto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(text = "⚙️", fontSize = 20.sp)
                }
            }

            when (selectedTab) {
                0 -> {
                    // Estado general section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00ACC1))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Estado general",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Status rows
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Resultado último análisis de condición:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "Estable",
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recordatorio siguiente toma de presión:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                BloodPressureCountdownTimer()
                            }
                        }
                    }

                    // Recomendaciones alimenticias
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Recomendaciones alimenticias:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Basado en tu condición y etapa según tus exámenes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Two recommendation cards side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Alimentos recomendados (Clickable)
                        Card(
                            onClick = onNavigateToRecommendedFoods,
                            modifier = Modifier
                                .weight(1f)
                                .height(220.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF00838F))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Alimentos recomendados",
                                    color = Color(0xFFB9F6CA),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🥗🐟", fontSize = 40.sp)
                                }
                                Text(
                                    text = "Verdes, salmón, frutos secos",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Alimentos a limitar o evitar (Clickable)
                        Card(
                            onClick = onNavigateToRestrictedFoods,
                            modifier = Modifier
                                .weight(1f)
                                .height(220.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF00838F))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Alimentos a limitar o evitar",
                                    color = Color(0xFFFF8A80),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🌭🧂", fontSize = 40.sp)
                                }
                                Text(
                                    text = "Embutidos, sodio, procesados",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Warning notice card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD54F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "⚠️", fontSize = 32.sp)
                            Text(
                                text = "Recuerda tratar de asistir a cada cita médica y seguir con tu tratamiento según lo indique tu médico.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                1 -> {
                    val context = LocalContext.current
                    val currentUser = remember(currentUserState) {
                        UserRepository.getUsers(context).find { it.correo.equals(user.correo, ignoreCase = true) } ?: currentUserState
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mis Medicamentos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(onClick = { showAddMedDialog = true }) {
                                Text("+ Agregar")
                            }
                        }

                        if (currentUser.recetas.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(text = "No tienes medicamentos registrados.")
                                }
                            }
                        } else {
                            currentUser.recetas.forEachIndexed { index, receta ->
                                Card(
                                    onClick = {
                                        editingIndex = index
                                        editNombreMed = receta.nombreMedicamento
                                        // Parse dosage parts (e.g., "500 mg" -> number="500", unit="mg")
                                        val parts = receta.dosisMg.split(" ")
                                        editDosisNumero = parts.getOrNull(0) ?: ""
                                        editUnidadDosis = parts.getOrNull(1) ?: "mg"
                                        // Parse frequency (e.g., "Cada 8 horas" -> "8")
                                        val hoursNum = receta.cadaCuantasHoras.replace(Regex("[^0-9]"), "")
                                        editHorasMed = hoursNum
                                        editDialogError = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "💊 ${receta.nombreMedicamento} (Toca para editar)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(text = "Dosis: ${receta.dosisMg}", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "Frecuencia: ${receta.cadaCuantasHoras}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    ExamenesScreen(user = user, modifier = Modifier.fillMaxSize())
                }
                3 -> {
                    EvolucionScreen(
                        user = user,
                        onNavigateToExamenes = { selectedTab = 2 },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

fun scheduleMedicationAlarm(context: Context, medName: String, dosage: String, hoursInterval: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
        putExtra("med_name", medName)
        putExtra("med_dosage", dosage)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        System.currentTimeMillis().toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTime = System.currentTimeMillis() + hoursInterval * 3600 * 1000L

    try {
        if (hoursInterval > 0) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                hoursInterval * 3600 * 1000L,
                pendingIntent
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun BloodPressureCountdownTimer() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("bp_timer_prefs", Context.MODE_PRIVATE) }

    var remainingSeconds by remember {
        val now = System.currentTimeMillis()
        var target = prefs.getLong("target_time", 0L)
        if (target <= now) {
            target = now + 3600 * 1000L
            prefs.edit().putLong("target_time", target).apply()
        }
        mutableStateOf(maxOf(0L, (target - now) / 1000L))
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            var target = prefs.getLong("target_time", 0L)
            if (target <= now) {
                target = now + 3600 * 1000L
                prefs.edit().putLong("target_time", target).apply()
            }
            remainingSeconds = maxOf(0L, (target - now) / 1000L)
            delay(1000L)
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    Surface(
        color = Color(0xFFE0E0E0),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = timeFormatted,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
