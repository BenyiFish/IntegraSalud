package com.example.integrasalud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.integrasalud.ui.theme.IntegraSaludTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntegraSaludTheme {
                IntegraSaludApp()
            }
        }
    }
}

sealed interface AppScreen {
    data object Login : AppScreen
    data object Register : AppScreen
    data class PatientHome(val user: UserProfile) : AppScreen
    data class Profile(val user: UserProfile) : AppScreen
    data class DoctorHome(val user: UserProfile) : AppScreen
    data class PatientDetail(val doctor: UserProfile, val patient: UserProfile) : AppScreen
}

@PreviewScreenSizes
@Composable
fun IntegraSaludApp() {
    val context = LocalContext.current
    var currentScreen by remember {
        val savedUser = UserRepository.getSavedSessionUser(context)
        val initialScreen = if (savedUser != null) {
            if (savedUser.rol == "DOCTOR") AppScreen.DoctorHome(savedUser)
            else AppScreen.PatientHome(savedUser)
        } else {
            AppScreen.Login
        }
        mutableStateOf(initialScreen)
    }

    when (val screen = currentScreen) {
        is AppScreen.Login -> {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                LoginScreen(
                    onLoginSuccess = { user ->
                        if (user.rol == "DOCTOR") {
                            currentScreen = AppScreen.DoctorHome(user)
                        } else {
                            currentScreen = AppScreen.PatientHome(user)
                        }
                    },
                    onNavigateToRegister = { currentScreen = AppScreen.Register },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        is AppScreen.Register -> {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                RegisterScreen(
                    onNavigateToLogin = { currentScreen = AppScreen.Login },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        is AppScreen.PatientHome -> {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                PatientHomeScreen(
                    user = screen.user,
                    onNavigateToProfile = { currentScreen = AppScreen.Profile(screen.user) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        is AppScreen.Profile -> {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                ProfileScreen(
                    user = screen.user,
                    onBack = {
                        if (screen.user.rol == "DOCTOR") {
                            currentScreen = AppScreen.DoctorHome(screen.user)
                        } else {
                            currentScreen = AppScreen.PatientHome(screen.user)
                        }
                    },
                    onProfileUpdated = { updatedUser ->
                        if (updatedUser.rol == "DOCTOR") {
                            currentScreen = AppScreen.DoctorHome(updatedUser)
                        } else {
                            currentScreen = AppScreen.PatientHome(updatedUser)
                        }
                    },
                    onLogout = {
                        UserRepository.clearSession(context)
                        currentScreen = AppScreen.Login
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        is AppScreen.DoctorHome -> {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                DoctorHomeScreen(
                    doctor = screen.user,
                    onSelectPatient = { patient ->
                        currentScreen = AppScreen.PatientDetail(screen.user, patient)
                    },
                    onNavigateToProfile = { currentScreen = AppScreen.Profile(screen.user) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
        is AppScreen.PatientDetail -> {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                PatientDetailScreen(
                    patient = screen.patient,
                    onBack = { currentScreen = AppScreen.DoctorHome(screen.doctor) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IntegraSaludTheme {
        PatientHomeScreen(
            user = UserProfile(
                nombres = "Vicente Alejandro",
                apellidos = "Muñoz López",
                correo = "vicente@test.com",
                contrasena = "123456",
                genero = "Masculino",
                fechaNacimiento = "15 - 04 - 1972",
                peso = 76.0,
                rol = "PACIENTE",
                antecedenteFamiliarRenal = "No",
                tieneHipertension = true,
                tieneDiabetes = false,
                tieneOtraEnfermedad = false,
                otraEnfermedadDetalle = ""
            ),
            onNavigateToProfile = {}
        )
    }
}
