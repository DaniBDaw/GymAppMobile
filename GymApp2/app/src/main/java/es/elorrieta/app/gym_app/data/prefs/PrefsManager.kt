package es.elorrieta.app.gym_app.data.prefs

import android.content.Context

class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences("GymAppPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REMEMBER = "remember_me"
        private const val KEY_LOGIN = "login_usuario"
        private const val KEY_PASSWORD = "password_usuario"
        private const val KEY_SESION_ACTUAL = "sesion_actual"
    }

    fun guardarRememberMe(email: String, password: String) {
        prefs.edit()
            .putBoolean(KEY_REMEMBER, true)
            .putString(KEY_LOGIN, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun borrarRememberMe() {
        prefs.edit()
            .remove(KEY_REMEMBER)
            .remove(KEY_LOGIN)
            .remove(KEY_PASSWORD)
            .apply()
    }

    fun isRememberMeActivo(): Boolean = prefs.getBoolean(KEY_REMEMBER, false)
    fun getLoginGuardado(): String = prefs.getString(KEY_LOGIN, "") ?: ""
    fun getPasswordGuardada(): String = prefs.getString(KEY_PASSWORD, "") ?: ""

    fun guardarSesionActual(email: String) {
        prefs.edit().putString(KEY_SESION_ACTUAL, email).apply()
    }

    fun getSesionActual(): String = prefs.getString(KEY_SESION_ACTUAL, "") ?: ""

    fun cerrarSesion() {
        prefs.edit().remove(KEY_SESION_ACTUAL).apply()
    }
}