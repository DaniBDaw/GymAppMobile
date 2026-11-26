package es.elorrieta.app.gym_app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import es.elorrieta.app.gym_app.Ajustes
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.data.prefs.PrefsManager
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.UsuarioEntity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val cbRecordar = findViewById<CheckBox>(R.id.cbRecordar)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)

        val usuarioDao = AppDatabase.getDatabase(this).usuarioDao()
        val prefs = PrefsManager(this)


        val registroLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val emailRegistrado = result.data?.getStringExtra("email")
                if (!emailRegistrado.isNullOrEmpty()) {
                    etUsuario.setText(emailRegistrado)
                    etPassword.text.clear()
                    etPassword.requestFocus()
                }
            }
        }


        lifecycleScope.launch {
            if (usuarioDao.getUserByMail("cliente@gym.com") == null) {
                usuarioDao.insertUser(
                    UsuarioEntity(
                        nombre = "Asier",
                        apellidos = "Cliente",
                        mail = "cliente@gym.com",
                        fechaNac = "01/01/2000",
                        contra = "1234",
                        nivel = 1,
                        isEntrenador = false
                    )
                )
            }
            if (usuarioDao.getUserByMail("entrenador@gym.com") == null) {
                usuarioDao.insertUser(
                    UsuarioEntity(
                        nombre = "Coach",
                        apellidos = "Elorrieta",
                        mail = "entrenador@gym.com",
                        fechaNac = "01/01/1990",
                        contra = "1234",
                        nivel = 5,
                        isEntrenador = true
                    )
                )
            }
        }


        if (prefs.isRememberMeActivo()) {
            etUsuario.setText(prefs.getLoginGuardado())
            etPassword.setText(prefs.getPasswordGuardada())
            cbRecordar.isChecked = true
        }


        cbRecordar.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                etUsuario.text.clear()
                etPassword.text.clear()
                prefs.borrarRememberMe()
            }
        }


        btnRegistro.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            registroLauncher.launch(intent)
        }


        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim().lowercase()
            val password = etPassword.text.toString()

            if (email.isEmpty()) {
                etUsuario.error = "Introduce tu email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Introduce tu contraseña"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val usuario = usuarioDao.getUserByMail(email)

                if (usuario != null && usuario.contra == password) {
                    prefs.guardarSesionActual(usuario.mail)
                    Ajustes.guardarIdUsuario(this@LoginActivity, usuario.id)

                    if (cbRecordar.isChecked) {
                        prefs.guardarRememberMe(email, password)
                    } else {
                        prefs.borrarRememberMe()
                    }

                    // Comprobar rol en la BD (isEntrenador)
                    if (usuario.isEntrenador) {
                        Toast.makeText(this@LoginActivity, "Bienvenido Entrenador ${usuario.nombre}", Toast.LENGTH_SHORT).show()
                        // Cambiar MainActivity por EntrenadorActivity cuando esté disponible
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USER_ID", usuario.id)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@LoginActivity, "Bienvenido ${usuario.nombre}", Toast.LENGTH_SHORT).show()
                        // Cambiar MainActivity por WorkoutsActivity cuando esté disponible
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("USER_ID", usuario.id)
                        startActivity(intent)
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