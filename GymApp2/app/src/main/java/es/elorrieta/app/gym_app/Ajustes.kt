package es.elorrieta.app.gym_app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object Ajustes {

    // Fichero y claves de las SharedPreferences
    const val FICHERO = "gymapp_prefs"
    const val CLAVE_TEMA = "tema"

    const val TEMA_CLARO = "claro"
    const val TEMA_OSCURO = "oscuro"

    // Guarda el tema elegido
    fun guardarTema(context: Context, tema: String) {
        val prefs = context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE)
        prefs.edit().putString(CLAVE_TEMA, tema).apply()
    }

    // Lee el tema guardado (si no hay ninguno, claro)
    fun leerTema(context: Context): String {
        val prefs = context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE)
        val tema = prefs.getString(CLAVE_TEMA, TEMA_CLARO)
        if (tema == null) {
            return TEMA_CLARO
        }
        return tema
    }

    // Aplica el tema a toda la App
    fun aplicarTema(tema: String) {
        if (tema == TEMA_OSCURO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}