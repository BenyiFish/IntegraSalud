package com.example.integrasalud

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecommendedFoodsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFF4CAF50)) // Green theme matching reference images
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text("← Volver", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(
                text = "Alimentos recomendados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // 1. Proteínas de alta calidad
        FoodCategoryCard(
            title = "Proteínas de alta calidad",
            description = "Dentro de la etapa en que se encuentra, se recomienda el consumo de: pollo sin piel, pavo, pescados (como merluza, reineta o salmón) y clara de huevo. Recomendable el tamaño de la palma de la mano por comida.",
            icon = "🥩🐟🥚"
        )

        // 2. Carbohidratos y cereales
        FoodCategoryCard(
            title = "Carbohidratos y cereales",
            description = "Dentro de la etapa en que se encuentra, se recomienda el consumo de: Arroz blanco o integral (en porciones moderadas), pasta, pan blanco o de masa madre bajo en sal, avena, maíz y papas cocidas.",
            icon = "🍚🍞🥣"
        )

        // 3. Grasas saludables
        FoodCategoryCard(
            title = "Grasas saludables",
            description = "Dentro de la etapa en que se encuentra, se recomienda el consumo de: Aceite de oliva extra virgen (principal grasa para cocinar y aliñar) y aguacate/palta (vigilando el potasio).",
            icon = "🫒🥑"
        )

        // 4. Frutas bajas en Potasio
        FoodCategoryCard(
            title = "Frutas bajas en Potasio",
            description = "Dentro de la etapa en que se encuentra, se recomienda el consumo de: Manzanas, peras, uvas, piña, arándanos, frutillas, sandía y mandarinas.",
            icon = "🍎🍐🍇🍍"
        )

        // 5. Verduras bajas en Potasio
        FoodCategoryCard(
            title = "Verduras bajas en Potasio",
            description = "Dentro de la etapa en que se encuentra, se recomienda el consumo de: Coliflor, pimiento morrón, zanahorias, calabacín (zapallo italiano), lechuga y pepino.",
            icon = "🥦🥕🥒🥬"
        )
    }
}

@Composable
fun FoodCategoryCard(
    title: String,
    description: String,
    icon: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F8E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 50.sp)
            }
        }
    }
}
