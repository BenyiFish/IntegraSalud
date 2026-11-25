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
fun RestrictedFoodsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFFD32F2F)) // Red warning theme for restricted foods
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
                text = "Alimentos a limitar o evitar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // 1. Embutidos y carnes procesadas
        RestrictedCategoryCard(
            title = "Embutidos y carnes procesadas",
            description = "Se debe evitar el consumo de salchichas, chorizo, jamón, tocino, mortadela y carnes en conserva. Contienen altos niveles de sodio, grasas saturadas y aditivos de fósforo que sobrecargan los riñones.",
            icon = "🌭🥓"
        )

        // 2. Alimentos ricos en Sodio y Sal
        RestrictedCategoryCard(
            title = "Alimentos altos en Sodio",
            description = "Evitar sopas instantáneas, caldos concentrados en cubo, snacks salados (papas fritas de bolsa), enlatados y salsas comerciales (soya, ketchup, mayonesa industrial). El sodio eleva la presión arterial y daña los riñones.",
            icon = "🧂🥫"
        )

        // 3. Frutas y Verduras altas en Potasio
        RestrictedCategoryCard(
            title = "Frutas y verduras altas en Potasio",
            description = "Limitar o evitar plátanos (bananos), naranjas, kiwis, melón, papas sin remojo previo, espinacas y acelgas crudas, y tomates. El exceso de potasio en sangre es peligroso para el corazón en enfermedad renal.",
            icon = "🍌🍊🍅"
        )

        // 4. Lácteos enteros y quesos curados
        RestrictedCategoryCard(
            title = "Lácteos enteros y quesos maduros",
            description = "Limitar leche entera, crema y quesos muy curados o amarillos (parmesano, cheddar). Tienen concentraciones elevadas de fósforo y calcio que pueden acumularse en los huesos y vasos sanguíneos.",
            icon = "🧀🥛"
        )

        // 5. Bebidas oscuras y refrescos con fósforo
        RestrictedCategoryCard(
            title = "Bebidas oscuras y refrescos",
            description = "Evitar completamente bebidas cola y refrescos oscuros, así como jugos artificiales envasados. Contienen aditivos de ácido fosfórico de rápida absorción altamente perjudiciales para la función renal.",
            icon = "🥤🚫"
        )
    }
}

@Composable
fun RestrictedCategoryCard(
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
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
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
                    .background(Color(0xFFFFCDD2)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 50.sp)
            }
        }
    }
}
