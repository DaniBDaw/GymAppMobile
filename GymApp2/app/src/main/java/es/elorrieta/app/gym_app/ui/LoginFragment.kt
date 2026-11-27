// =====================================================================
// LoginFragment
// Pantalla de Login (antes LoginActivity). Comprueba email y contraseña
// contra ROOM, gestiona "Recuérdame" y guarda el id del usuario logueado.
// El tipo de usuario (cliente/entrenador) se detecta desde la BD (isEntrenador).
// Al entrar, va a Workouts y aparece el menú.
// =====================================================================
package es.elorrieta.app.gym_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.data.prefs.PrefsManager
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.UsuarioEntity
import es.elorrieta.app.gym_app.util.Ajustes
import es.elorrieta.app.gym_app.util.MenuInferior
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    companion object {
        // El Registro deja aquí el email nuevo para que el Login lo rellene solo
        var emailRegistrado: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_login, container, false)

        val etUsuario = view.findViewById<EditText>(R.id.etUsuario)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val cbRecordar = view.findViewById<CheckBox>(R.id.cbRecordar)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnRegistro = view.findViewById<Button>(R.id.btnRegistro)

        val usuarioDao = AppDatabase.getDatabase(requireContext()).usuarioDao()
        val prefs = PrefsManager(requireContext())

        // Usuarios de prueba (cliente y entrenador) si no existen
        viewLifecycleOwner.lifecycleScope.launch {
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
                        nivel = 4,              // niveles de 0 (Novato) a 4 (Experto)
                        isEntrenador = true
                    )
                )
            }
        }

        // Recuérdame: si estaba activo, rellenamos los campos
        if (prefs.isRememberMeActivo()) {
            etUsuario.setText(prefs.getLoginGuardado())
            etPassword.setText(prefs.getPasswordGuardada())
            cbRecordar.isChecked = true
        }

        // Si venimos del Registro, ponemos el email nuevo
        val emailNuevo = emailRegistrado
        if (emailNuevo != null) {
            etUsuario.setText(emailNuevo)
            etPassword.text.clear()
            etPassword.requestFocus()
            emailRegistrado = null
        }

        cbRecordar.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                etUsuario.text.clear()
                etPassword.text.clear()
                prefs.borrarRememberMe()
            }
        }

        // Ir al Registro (pantalla hija: con "atrás" se vuelve al Login)
        btnRegistro.setOnClickListener {
            MenuInferior.abrirSubpantalla(requireActivity() as AppCompatActivity, RegistroFragment())
        }

        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim().lowercase()
            val password = etPassword.text.toString()

            if (email.isEmpty()) {
                etUsuario.error = getString(R.string.error_campos_vacios)
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = getString(R.string.error_campos_vacios)
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val usuario = usuarioDao.getUserByMail(email)

                if (usuario != null && usuario.contra == password) {
                    prefs.guardarSesionActual(usuario.mail)
                    Ajustes.guardarIdUsuario(requireContext(), usuario.id)

                    if (cbRecordar.isChecked) {
                        prefs.guardarRememberMe(email, password)
                    } else {
                        prefs.borrarRememberMe()
                    }

                    // El tipo de usuario viene de la BD
                    if (usuario.isEntrenador) {
                        Toast.makeText(requireContext(), getString(R.string.login_bienvenido_entrenador, usuario.nombre), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.login_bienvenido, usuario.nombre), Toast.LENGTH_SHORT).show()
                    }

                    // Rúbrica: tras el login se va a la pantalla de Workouts (y aparece el menú)
                    MenuInferior.mostrar(requireActivity() as AppCompatActivity, MenuInferior.WORKOUTS)
                } else {
                    etUsuario.error = getString(R.string.error_login)
                    etPassword.error = getString(R.string.error_login)
                    Toast.makeText(requireContext(), getString(R.string.error_login), Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}