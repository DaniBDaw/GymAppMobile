// =====================================================================
// RegistroFragment
// Pantalla de Registro en 3 pasos (antes Registro, Activity):
//   1. nombre, apellidos y fecha   2. email y contraseña   3. foto (opcional)
// Guarda el usuario en ROOM y la foto como foto_ID.png. Al terminar,
// vuelve al Login con el email ya puesto.
// =====================================================================
package es.elorrieta.app.gym_app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.UsuarioEntity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RegistroFragment : Fragment() {

    // Foto hecha con la cámara (null si no ha hecho ninguna)
    private var fotoTomada: Bitmap? = null

    // Callback: la cámara devuelve la foto
    private val abrirCamara = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoTomada = bitmap
            requireView().findViewById<ImageView>(R.id.CameraIcon).setImageBitmap(bitmap)
        }
    }

    // Callback: respuesta del usuario al pedir permiso de cámara
    private val pedirPermisoCamara = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido: Boolean ->
        if (concedido) {
            abrirCamara.launch()
        } else {
            Toast.makeText(requireContext(), getString(R.string.camara_permiso_denegado), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_registro, container, false)

        // Obtenemos el DAO de usuarios (métodos para leer y guardar en t_usuarios)
        val usuarioDao = AppDatabase.getDatabase(requireContext()).usuarioDao()

        val continuar = view.findViewById<Button>(R.id.BtnRegisterContinuar)
        val volver = view.findViewById<Button>(R.id.BtnRegisterVolver)

        val nombre = view.findViewById<EditText>(R.id.InputNombre)
        val apellido = view.findViewById<EditText>(R.id.InputApellido)
        val nacFecha = view.findViewById<EditText>(R.id.editTextDate)

        val email = view.findViewById<EditText>(R.id.InputEmail)
        val contra = view.findViewById<EditText>(R.id.InputContra)
        val confirmContra = view.findViewById<EditText>(R.id.InputConfirmContra)

        val cameraIcon = view.findViewById<ImageView>(R.id.CameraIcon)

        email.visibility = View.GONE
        contra.visibility = View.GONE
        confirmContra.visibility = View.GONE
        cameraIcon.visibility = View.GONE

        var paso = 1

        // Email sin espacios y en minúsculas, para no registrar el mismo dos veces
        fun emailNormalizado(): String {
            return email.text.toString().trim().lowercase()
        }

        // Enseña los campos del paso actual
        fun actualizarVista() {
            if (paso == 1) {
                nombre.visibility = View.VISIBLE
                apellido.visibility = View.VISIBLE
                nacFecha.visibility = View.VISIBLE

                email.visibility = View.GONE
                contra.visibility = View.GONE
                confirmContra.visibility = View.GONE
                cameraIcon.visibility = View.GONE
            } else if (paso == 2) {
                nombre.visibility = View.GONE
                apellido.visibility = View.GONE
                nacFecha.visibility = View.GONE
                cameraIcon.visibility = View.GONE

                email.visibility = View.VISIBLE
                contra.visibility = View.VISIBLE
                confirmContra.visibility = View.VISIBLE
                continuar.setText(R.string.continuar)
            } else if (paso == 3) {
                nombre.visibility = View.GONE
                apellido.visibility = View.GONE
                nacFecha.visibility = View.GONE

                email.visibility = View.GONE
                contra.visibility = View.GONE
                confirmContra.visibility = View.GONE

                cameraIcon.visibility = View.VISIBLE
                continuar.setText(R.string.registro_boton)
            }
        }

        // Comprueba en la BD si el email ya existe. Si no existe, pasa al paso 3.
        fun comprobarEmailYAvanzar() {
            val emailActual = emailNormalizado()

            if (!Patterns.EMAIL_ADDRESS.matcher(emailActual).matches()) {
                email.error = getString(R.string.error_email)
                return
            }

            viewLifecycleOwner.lifecycleScope.launch {
                continuar.isEnabled = false
                val usuarioExistente = usuarioDao.getUserByMail(emailNormalizado())
                continuar.isEnabled = true

                if (usuarioExistente != null) {
                    email.error = getString(R.string.error_usuario_existe)
                } else {
                    paso++
                    actualizarVista()
                }
            }
        }

        // Guarda la foto en el almacenamiento interno como foto_ID.png
        fun guardarFoto(idUsuario: Long) {
            val foto = fotoTomada
            if (foto == null) {
                return
            }
            try {
                val fichero = File(requireContext().filesDir, "foto_" + idUsuario + ".png")
                val salida = FileOutputStream(fichero)
                foto.compress(Bitmap.CompressFormat.PNG, 100, salida)
                salida.close()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.camara_error), Toast.LENGTH_SHORT).show()
            }
        }

        // Crea el usuario y lo guarda en ROOM
        fun guardarUsuario() {
            val usuario = UsuarioEntity(
                nombre = nombre.text.toString().trim(),
                apellidos = apellido.text.toString().trim(),
                mail = emailNormalizado(),
                fechaNac = nacFecha.text.toString(),
                contra = contra.text.toString(),
                nivel = 0,              // Empieza en Novato
                isEntrenador = false    // Por defecto se registra como cliente
            )

            viewLifecycleOwner.lifecycleScope.launch {
                continuar.isEnabled = false
                try {
                    val id = usuarioDao.insertUser(usuario)
                    guardarFoto(id)

                    Toast.makeText(requireContext(), getString(R.string.registro_ok), Toast.LENGTH_SHORT).show()

                    // Volvemos al Login con el email ya puesto
                    LoginFragment.emailRegistrado = usuario.mail
                    parentFragmentManager.popBackStack()
                } catch (_: SQLiteConstraintException) {
                    // El email es único en la tabla: si ya existe, volvemos al paso 2
                    paso = 2
                    actualizarVista()
                    email.error = getString(R.string.error_usuario_existe)
                    continuar.isEnabled = true
                }
            }
        }

        // Avanza (sig = true) o retrocede (sig = false) un paso
        fun gestionarPaso(sig: Boolean) {
            if (sig) {
                if (paso == 1) {
                    if (nombre.text.isNullOrBlank() || apellido.text.isNullOrBlank() || nacFecha.text.isNullOrBlank()) {
                        nombre.error = getString(R.string.error_campos_vacios)
                        return
                    }
                } else if (paso == 2) {
                    if (email.text.isNullOrBlank() || contra.text.isNullOrBlank() || confirmContra.text.isNullOrBlank()) {
                        email.error = getString(R.string.error_campos_vacios)
                        return
                    }
                    if (contra.text.toString() != confirmContra.text.toString()) {
                        confirmContra.error = getString(R.string.error_password_no_coincide)
                        return
                    }
                    comprobarEmailYAvanzar()
                    return
                } else if (paso == 3) {
                    guardarUsuario()
                    return
                }
                paso++
            } else {
                if (paso > 1) {
                    paso--
                } else {
                    // En el paso 1, "Volver" regresa al Login
                    parentFragmentManager.popBackStack()
                    return
                }
            }
            actualizarVista()
        }

        volver.setOnClickListener {
            gestionarPaso(false)
        }
        continuar.setOnClickListener {
            gestionarPaso(true)
        }

        // Foto: comprobamos cámara y permiso antes de abrirla
        cameraIcon.setOnClickListener {
            if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Toast.makeText(requireContext(), getString(R.string.camara_no_disponible), Toast.LENGTH_SHORT).show()
            } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamara.launch()
            } else {
                pedirPermisoCamara.launch(Manifest.permission.CAMERA)
            }
        }

        // La fecha se elige con un calendario (con 1 solo clic)
        nacFecha.isFocusable = false
        nacFecha.isFocusableInTouchMode = false
        nacFecha.isCursorVisible = false

        nacFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.registro_fecha_nacimiento))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formato.timeZone = TimeZone.getTimeZone("UTC")
                nacFecha.setText(formato.format(Date(selection)))
            }

            datePicker.show(childFragmentManager, "DATE_PICKER")
        }

        return view
    }
}