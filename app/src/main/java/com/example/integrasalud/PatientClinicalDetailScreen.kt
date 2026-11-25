package com.example.integrasalud

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PatientClinicalDetailScreen(
    patient: UserProfile,
    doctor: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPatient by remember { mutableStateOf(patient) }
    var selectedTab by rememberSaveable { mutableStateOf(0) } // 0: TFG, 1: Laboratorio, 2: Ajuste Dosis, 3: Recetas

    // Prescribe dialog state
    var showPrescribeDialog by rememberSaveable { mutableStateOf(false) }
    var nombreMedicamento by rememberSaveable { mutableStateOf("") }
    var cadaCuantasHoras by rememberSaveable { mutableStateOf("") }
    var dosisMg by rememberSaveable { mutableStateOf("") }
    var dialogError by rememberSaveable { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    if (showPrescribeDialog) {
        AlertDialog(
            onDismissRequest = { showPrescribeDialog = false },
            title = { Text("Recetar Medicamento al Paciente") },
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
                        label = { Text("Frecuencia (ej. Cada 12 horas)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dosisMg,
                        onValueChange = { dosisMg = it },
                        label = { Text("Dosis (ej. 250 mg)") },
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
                            nombreMedicamento.isBlank() -> dialogError = "Ingresa el nombre."
                            cadaCuantasHoras.isBlank() -> dialogError = "Ingresa la frecuencia."
                            dosisMg.isBlank() -> dialogError = "Ingresa la dosis."
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
                    Text("Recetar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrescribeDialog = false }) {
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
                    icon = { Text("📉", fontSize = 20.sp) },
                    label = { Text("TFG") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🔬", fontSize = 20.sp) },
                    label = { Text("Lab") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("💊", fontSize = 20.sp) },
                    label = { Text("Dosis") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("📋", fontSize = 20.sp) },
                    label = { Text("Recetas") }
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
            // Top Back & Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Volver")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Paciente: ${currentPatient.nombreCompleto}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = Color(0xFFEF5350),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Riesgo Alto",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Tab 0: Función renal (TFG)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Función renal (TFG)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("1M", "3M", "6M", "1A").forEach { period ->
                                        Surface(
                                            color = if (period == "6M") Color(0xFF00ACC1) else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = period,
                                                color = if (period == "6M") Color.White else Color.DarkGray,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ Descenso sostenido",
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            // Chart Canvas (Downward curve)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height

                                    // Grid lines
                                    for (i in 0..3) {
                                        val y = height * i / 3
                                        drawLine(
                                            color = Color.LightGray.copy(alpha = 0.5f),
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = 1f
                                        )
                                    }

                                    // Downward red curve
                                    val path = Path().apply {
                                        moveTo(0f, height * 0.2f)
                                        cubicTo(width * 0.3f, height * 0.3f, width * 0.7f, height * 0.7f, width, height * 0.85f)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color(0xFFE53935),
                                        style = Stroke(width = 3f)
                                    )
                                }
                            }

                            Text(
                                text = "Última TFG: 54 mL/min/1,73 m² · Etapa G3a",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Button(
                                onClick = { /* MDCal link action */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Calcular GFR (CKD-EPI) en MDCal.com ↗")
                            }
                        }
                    }

                    // Factores de Riesgo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Factores De Riesgo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Por qué éste en alerta:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )

                            RiskFactorBar("Caída TFG (45%)", 0.45f, Color(0xFFE53935))
                            RiskFactorBar("Medicamento (30%)", 0.30f, Color(0xFFFB8C00))
                            RiskFactorBar("Comorbilidades (25%)", 0.25f, Color(0xFF00ACC1))
                        }
                    }

                    // Alerta de Medicación
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE082)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(text = "⚠️", fontSize = 28.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Alerta de Medicación",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Alerta Nefrotóxica: Paciente registró uso de Ibuprofeno 400mg (Venta libre) por 3 días.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Tab 1: Bandeja de laboratorio
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Bandeja de laboratorio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "9 por revisar · 2 requieren atención prioritaria",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(selected = true, onClick = {}, label = { Text("Nuevos 9") })
                        FilterChip(selected = false, onClick = {}, label = { Text("Prioritarios 2") })
                        FilterChip(selected = false, onClick = {}, label = { Text("Validados") })
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = currentPatient.nombreCompleto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Surface(color = Color(0xFFFFCDD2), shape = RoundedCornerShape(12.dp)) {
                                    Text(text = "Prioritario", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                            Text(text = "Laboratorio Central · Hoy, 09:32", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "Creatinina sérica", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                                        Text(text = "2,80 mg/dL", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                        Text(text = "Anterior: 1,40 mg/dL", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "TFG estimada · CKD-EPI 2021", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                    Text(text = "54 → 24 mL/min/1,73 m²", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { /* Validate TFG */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Validar y Actualizar TFG")
                            }
                        }
                    }
                }
                2 -> {
                    // Tab 2: Ajuste de Dosis
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Ajuste de Dosis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Prevención de toxicidad por función renal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentPatient.nombreCompleto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "54 mL/min/1,73 m²", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00ACC1))
                            }
                            Text(text = "Última TFG validada · 10 nov.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            OutlinedTextField(
                                value = "Antibiótico de ejemplo",
                                onValueChange = {},
                                label = { Text("Fármaco a simular") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "Dosis estándar", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(text = "500 mg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "Frecuencia", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(text = "Cada 12 h", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "Ajuste sugerido · -50 %", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                                    Text(text = "Debido a TFG de 54 mL/min, reducir la dosis estándar de este antibiótico al 50%.", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "250 mg cada 12 h", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                    Text(text = "Resultado del escenario de demostración", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }

                            Button(
                                onClick = { showPrescribeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Recetar Dosis Ajustada")
                            }
                        }
                    }
                }
                3 -> {
                    // Tab 3: Recetas del Paciente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Recetas Médicas (${currentPatient.recetas.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Button(onClick = { showPrescribeDialog = true }) {
                            Text("+ Recetar")
                        }
                    }

                    if (currentPatient.recetas.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(text = "No hay recetas médicas registradas.")
                            }
                        }
                    } else {
                        currentPatient.recetas.forEach { receta ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "💊 ${receta.nombreMedicamento}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = "Dosis: ${receta.dosisMg}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Frecuencia: ${receta.cadaCuantasHoras}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiskFactorBar(label: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}
