package es.elorrieta.app.gym_app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object Ajustes {

    // Fichero y claves de las SharedPreferences
    const val FICHERO = "gymapp_prefs"
    const val CLAVE_TEMA = "tema"
    const val CLAVE_IDIOMA = "idioma"

    const val TEMA_CLARO = "claro"
    const val TEMA_OSCURO = "oscuro"

    const val IDIOMA_ES = "es"
    const val IDIOMA_EN = "en"
    const val IDIOMA_KA = "ka"

    // ---------------- TEMA ----------------

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

    // ---------------- IDIOMA ----------------

    // Guarda el idioma elegido
    fun guardarIdioma(context: Context, idioma: String) {
        val prefs = context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE)
        prefs.edit().putString(CLAVE_IDIOMA, idioma).apply()
    }

    // Lee el idioma guardado (si no hay ninguno, castellano)
    fun leerIdioma(context: Context): String {
        val prefs = context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE)
        val idioma = prefs.getString(CLAVE_IDIOMA, IDIOMA_ES)
        if (idioma == null) {
            return IDIOMA_ES
        }
        return idioma
    }

    // Aplica el idioma a toda la App (usa el strings.xml de ese idioma)
    fun aplicarIdioma(idioma: String) {
        val idiomas = LocaleListCompat.forLanguageTags(idioma)
        AppCompatDelegate.setApplicationLocales(idiomas)
    }




    // ---------------- USUARIO LOGUEADO ----------------

    const val CLAVE_ID_USUARIO = "id_usuario"

    // El Login llama a esto cuando el usuario entra bien
    fun guardarIdUsuario(context: Context, id: Long) {
        val prefs = context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE)
        prefs.edit().putLong(CLAVE_ID_USUARIO, id).apply()
    }

    // Devuelve el id del usuario logueado (-1 si no hay ninguno)
    fun leerIdUsuario(context: Context): Long {
        val prefs = context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE)
        return prefs.getLong(CLAVE_ID_USUARIO, -1L)
    }
}