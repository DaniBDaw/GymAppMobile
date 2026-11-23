package es.elorrieta.app.gym_app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.data.db.AppDatabase
import es.elorrieta.app.gym_app.data.db.Usuario
import es.elorrieta.app.gym_app.data.prefs.PrefsManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val cbRecordar = findViewById<CheckBox>(R.id.cbRecordar)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val db = AppDatabase(this)
        val prefs = PrefsManager(this)


        lifecycleScope.launch {
            if (db.usuarioDao().login("cliente@gym.com", "1234") == null) {
                db.usuarioDao().insertar(
                    Usuario(email = "cliente@gym.com", password = "1234", tipoUsuario = "Cliente")
                )
            }
            if (db.usuarioDao().login("entrenador@gym.com", "1234") == null) {
                db.usuarioDao().insertar(
                    Usuario(email = "entrenador@gym.com", password = "1234", tipoUsuario = "Entrenador")
                )
            }
        }


        if (prefs.isRememberMeActivo()) {
            etUsuario.setText(prefs.getLoginGuardado())
            etPassword.setText(prefs.getPasswordGuardada())
            cbRecordar.isChecked = true
        }


        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etUsuario.error = "Introduce tu email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Introduce tu contraseña"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val usuarioValido = db.usuarioDao().login(email, password)

                if (usuarioValido != null) {
                    prefs.guardarSesionActual(usuarioValido.email)

                    if (cbRecordar.isChecked) {
                        prefs.guardarRememberMe(email, password)
                    } else {
                        prefs.borrarRememberMe()
                    }


                    if (usuarioValido.tipoUsuario == "Cliente") {
                        Toast.makeText(this@LoginActivity, "Bienvenido Cliente", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(this@LoginActivity, "Bienvenido Entrenador", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }
                    finish()
                } else {
                    etUsuario.error = "Credenciales incorrectas"
                    etPassword.error = "Credenciales incorrectas"
                    Toast.makeText(this@LoginActivity, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
