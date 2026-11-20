package com.example.integrasalud

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Receta(
    val nombreMedicamento: String,
    val cadaCuantasHoras: String,
    val dosisMg: String
)

data class UserProfile(
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val contrasena: String,
    val genero: String,
    val fechaNacimiento: String,
    val peso: Double,
    val rol: String, // "PACIENTE" o "DOCTOR"
    val antecedenteFamiliarRenal: String, // "Sí", "No", "No lo sé"
    val tieneHipertension: Boolean,
    val tieneDiabetes: Boolean,
    val tieneOtraEnfermedad: Boolean,
    val otraEnfermedadDetalle: String,
    val avatar: String = "👤",
    val recetas: List<Receta> = emptyList()
) {
    val nombreCompleto: String
        get() = "$nombres $apellidos"
}

object UserRepository {
    private const val PREF_NAME = "integrasalud_prefs"
    private const val KEY_USERS = "users_list"
    private const val KEY_LOGGED_IN_EMAIL = "logged_in_user_email"

    private fun getSeedUsers(): List<UserProfile> {
        return listOf(
            // 3 Doctrtores
            UserProfile(
                nombres = "Carlos", apellidos = "Mendoza",
                correo = "carlos.mendoza@integrasalud.com", contrasena = "123456",
                genero = "Masculino", fechaNacimiento = "10 - 05 - 1975", peso = 78.0,
                rol = "DOCTOR", antecedenteFamiliarRenal = "No",
                tieneHipertension = false, tieneDiabetes = false, tieneOtraEnfermedad = false, otraEnfermedadDetalle = "", avatar = "👨‍⚕️"
            ),
            UserProfile(
                nombres = "María", apellidos = "González",
                correo = "maria.gonzalez@integrasalud.com", contrasena = "123456",
                genero = "Femenino", fechaNacimiento = "15 - 08 - 1980", peso = 60.0,
                rol = "DOCTOR", antecedenteFamiliarRenal = "No",
                tieneHipertension = false, tieneDiabetes = false, tieneOtraEnfermedad = false, otraEnfermedadDetalle = "", avatar = "👩‍⚕️"
            ),
            UserProfile(
                nombres = "Roberto", apellidos = "Silva",
                correo = "roberto.silva@integrasalud.com", contrasena = "123456",
                genero = "Masculino", fechaNacimiento = "02 - 11 - 1970", peso = 82.0,
                rol = "DOCTOR", antecedenteFamiliarRenal = "No",
                tieneHipertension = false, tieneDiabetes = false, tieneOtraEnfermedad = false, otraEnfermedadDetalle = "", avatar = "👨‍⚕️"
            ),
            // 5 Pacientes
            UserProfile(
                nombres = "Vicente Alejandro", apellidos = "Muñoz López",
                correo = "vicente.munoz@integrasalud.com", contrasena = "123456",
                genero = "Masculino", fechaNacimiento = "15 - 04 - 1972", peso = 76.0,
                rol = "PACIENTE", antecedenteFamiliarRenal = "No",
                tieneHipertension = true, tieneDiabetes = false, tieneOtraEnfermedad = false, otraEnfermedadDetalle = "", avatar = "👤",
                recetas = listOf(Receta("Losartán", "Cada 24 horas", "50 mg"))
            ),
            UserProfile(
                nombres = "Ana Sofía", apellidos = "Torres",
                correo = "ana.torres@integrasalud.com", contrasena = "123456",
                genero = "Femenino", fechaNacimiento = "20 - 08 - 1985", peso = 62.5,
                rol = "PACIENTE", antecedenteFamiliarRenal = "Sí",
                tieneHipertension = false, tieneDiabetes = true, tieneOtraEnfermedad = false, otraEnfermedadDetalle = "", avatar = "👩",
                recetas = listOf(Receta("Metformina", "Cada 12 horas", "850 mg"))
            ),
            UserProfile(
                nombres = "Luis Fernando", apellidos = "Gómez",
                correo = "luis.gomez@integrasalud.com", contrasena = "123456",
                genero = "Masculino", fechaNacimiento = "10 - 12 - 1990", peso = 80.0,
                rol = "PACIENTE", antecedenteFamiliarRenal = "No lo sé",
                tieneHipertension = false, tieneDiabetes = false, tieneOtraEnfermedad = true, otraEnfermedadDetalle = "Asma leve", avatar = "👨"
            ),
            UserProfile(
                nombres = "Carmen Rosa", apellidos = "Morales",
                correo = "carmen.morales@integrasalud.com", contrasena = "123456",
                genero = "Femenino", fechaNacimiento = "05 - 03 - 1978", peso = 68.0,
                rol = "PACIENTE", antecedenteFamiliarRenal = "Sí",
                tieneHipertension = true, tieneDiabetes = true, tieneOtraEnfermedad = false, otraEnfermedadDetalle = "", avatar = "👩"
            ),
            UserProfile(
                nombres = "Jorge Alberto", apellidos = "Paredes",
                correo = "jorge.paredes@integrasalud.com", contrasena = "123456",
                genero = "Masculino", fechaNacimiento = "22 - 07 - 1982", peso = 85.0,
                rol = "PACIENTE", antecedenteFamiliarRenal = "No",
                tieneHipertension = false, tieneDiabetes = false, tieneOtraEnfermedad = false, otraEnfermedadDetalle = "", avatar = "🧑"
            )
        )
    }

    fun getUsers(context: Context): MutableList<UserProfile> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_USERS)) {
            saveUsersToPrefs(context, getSeedUsers())
        }
        val jsonString = prefs.getString(KEY_USERS, "[]") ?: "[]"
        val users = mutableListOf<UserProfile>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val recetasArray = obj.optJSONArray("recetas") ?: JSONArray()
                val recetasList = mutableListOf<Receta>()
                for (j in 0 until recetasArray.length()) {
                    val rObj = recetasArray.getJSONObject(j)
                    recetasList.add(
                        Receta(
                            nombreMedicamento = rObj.optString("nombreMedicamento"),
                            cadaCuantasHoras = rObj.optString("cadaCuantasHoras"),
                            dosisMg = rObj.optString("dosisMg")
                        )
                    )
                }

                users.add(
                    UserProfile(
                        nombres = obj.optString("nombres"),
                        apellidos = obj.optString("apellidos"),
                        correo = obj.optString("correo"),
                        contrasena = obj.optString("contrasena"),
                        genero = obj.optString("genero", "Masculino"),
                        fechaNacimiento = obj.optString("fechaNacimiento"),
                        peso = obj.optDouble("peso"),
                        rol = obj.optString("rol", "PACIENTE"),
                        antecedenteFamiliarRenal = obj.optString("antecedenteFamiliarRenal", "No"),
                        tieneHipertension = obj.optBoolean("tieneHipertension", false),
                        tieneDiabetes = obj.optBoolean("tieneDiabetes", false),
                        tieneOtraEnfermedad = obj.optBoolean("tieneOtraEnfermedad", false),
                        otraEnfermedadDetalle = obj.optString("otraEnfermedadDetalle", ""),
                        avatar = obj.optString("avatar", "👤"),
                        recetas = recetasList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return users
    }

    fun saveUser(context: Context, user: UserProfile): Boolean {
        val users = getUsers(context)
        if (users.any { it.correo.equals(user.correo, ignoreCase = true) }) {
            return false
        }
        users.add(user)
        saveUsersToPrefs(context, users)
        return true
    }

    fun updateUser(context: Context, user: UserProfile) {
        val users = getUsers(context)
        val index = users.indexOfFirst { it.correo.equals(user.correo, ignoreCase = true) }
        if (index >= 0) {
            users[index] = user
            saveUsersToPrefs(context, users)
        }
    }

    fun saveSession(context: Context, correo: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LOGGED_IN_EMAIL, correo).apply()
    }

    fun login(context: Context, correo: String, contrasena: String): UserProfile? {
        val users = getUsers(context)
        val user = users.find {
            it.correo.equals(correo.trim(), ignoreCase = true) && it.contrasena == contrasena
        }
        if (user != null) {
            saveSession(context, user.correo)
        }
        return user
    }

    fun getSavedSessionUser(context: Context): UserProfile? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_LOGGED_IN_EMAIL, null) ?: return null
        val users = getUsers(context)
        return users.find { it.correo.equals(email, ignoreCase = true) }
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_LOGGED_IN_EMAIL).apply()
    }

    private fun saveUsersToPrefs(context: Context, users: List<UserProfile>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (user in users) {
            val obj = JSONObject().apply {
                put("nombres", user.nombres)
                put("apellidos", user.apellidos)
                put("correo", user.correo)
                put("contrasena", user.contrasena)
                put("genero", user.genero)
                put("fechaNacimiento", user.fechaNacimiento)
                put("peso", user.peso)
                put("rol", user.rol)
                put("antecedenteFamiliarRenal", user.antecedenteFamiliarRenal)
                put("tieneHipertension", user.tieneHipertension)
                put("tieneDiabetes", user.tieneDiabetes)
                put("tieneOtraEnfermedad", user.tieneOtraEnfermedad)
                put("otraEnfermedadDetalle", user.otraEnfermedadDetalle)
                put("avatar", user.avatar)

                val recetasArr = JSONArray()
                for (receta in user.recetas) {
                    val rObj = JSONObject().apply {
                        put("nombreMedicamento", receta.nombreMedicamento)
                        put("cadaCuantasHoras", receta.cadaCuantasHoras)
                        put("dosisMg", receta.dosisMg)
                    }
                    recetasArr.put(rObj)
                }
                put("recetas", recetasArr)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_USERS, jsonArray.toString()).apply()
    }
}
