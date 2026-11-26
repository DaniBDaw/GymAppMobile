package es.elorrieta.app.gym_app.ui

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.UsuarioEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.util.Patterns
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class Registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // NUEVO: obtenemos la base de datos y, de ella, el DAO de usuarios.
        // El DAO es el objeto que tiene los métodos para leer y guardar en la tabla t_usuarios
        // (getUserByMail, insertUser...). getDatabase siempre devuelve la misma instancia,
        // así que no se crea una base de datos nueva cada vez que se abre esta pantalla.
        val usuarioDao = AppDatabase.getDatabase(this).usuarioDao()

        val continuar = findViewById<Button>(R.id.BtnRegisterContinuar)
        val volver = findViewById<Button>(R.id.BtnRegisterVolver)

        val nombre = findViewById<EditText>(R.id.InputNombre)
        val apellido = findViewById<EditText>(R.id.InputApellido)
        val nacFecha = findViewById<EditText>(R.id.editTextDate)

        val email = findViewById<EditText>(R.id.InputEmail)
        val contra = findViewById<EditText>(R.id.InputContra)
        val confirmContra = findViewById<EditText>(R.id.InputConfirmContra)

        val cameraIcon = findViewById<ImageView>(R.id.CameraIcon)

        nombre.setText("")
        apellido.setText("")
        nacFecha.setText("")
        email.setText("")
        contra.setText("")
        confirmContra.setText("")

        email.visibility = View.GONE
        contra.visibility = View.GONE
        confirmContra.visibility = View.GONE
        cameraIcon.visibility = View.GONE

        var paso = 1
        var fotoTomada: Bitmap? = null

        // NUEVO: devuelve el email sin espacios y en minúsculas.
        // Así "Ana@mail.com" y " ana@mail.com" se consideran el mismo email
        // y no se pueden registrar dos veces.
        fun emailNormalizado(): String {
            return email.text.toString().trim().lowercase()
        }

        fun actualizarVista() {
            if (paso == 1) {
                nombre.visibility = View.VISIBLE
                apellido.visibility = View.VISIBLE
                nacFecha.visibility = View.VISIBLE

                email.visibility = View.GONE
                contra.visibility = View.GONE
                confirmContra.visibility = View.GONE
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

        //Comprueba en la base de datos si el email ya está registrado.
        // Si no existe, pasa al paso 3. Si existe, muestra un error en el campo del email.
        fun comprobarEmailYAvanzar() {
            val emailActual = emailNormalizado()

            // Patterns.EMAIL_ADDRESS valida que sea email, no sé que conlleva eso pero funciona
            if (!Patterns.EMAIL_ADDRESS.matcher(emailActual).matches()) {
                email.error = getString(R.string.error_email)
                return
            }

            // Las consultas a la base de datos son "suspend": tardan un poco y no se pueden
            // hacer directamente en un clic, porque congelarían la app.
            // lifecycleScope.launch abre una corrutina, que es un bloque donde sí se pueden
            // llamar funciones suspend. Además, si se cierra la pantalla, se cancela sola.
            lifecycleScope.launch {
                // Desactivamos el botón mientras se consulta
                continuar.isEnabled = false

                // getUserByMail devuelve el usuario si existe, o null si no existe
                val usuarioExistente = usuarioDao.getUserByMail(emailNormalizado())

                continuar.isEnabled = true

                if (usuarioExistente != null) {
                    // .error muestra un aviso rojo en el propio EditText
                    email.error = getString((R.string.error_usuario_existe))
                } else {
                    paso++
                    Log.d("patata", "gestionarPaso: " + paso)
                    actualizarVista()
                }
            }
        }

        fun guardarFoto(idUsuario: Long) {
            val foto = fotoTomada
            if (foto == null) {
                return
            }
            try {
                val fichero = File(filesDir, "foto_" + idUsuario + ".png")
                val salida = FileOutputStream(fichero)
                foto.compress(Bitmap.CompressFormat.PNG, 100, salida)
                salida.close()
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.camara_error), Toast.LENGTH_SHORT).show()
            }
        }

        //Crea el usuario con los datos de los EditText y lo guarda en la base de datos
        fun guardarUsuario() {
            // UsuarioEntity representa una fila de la tabla t_usuarios.
            // El id no se pone porque Room lo genera solo (autoGenerate = true).
            val usuario = UsuarioEntity(
                nombre = nombre.text.toString().trim(),
                apellidos = apellido.text.toString().trim(),
                mail = emailNormalizado(),
                fechaNac = nacFecha.text.toString(),
                contra = contra.text.toString(),
                nivel = 0,              // Valor inicial, no se pide en el registro
                isEntrenador = false    // Por defecto se registra como cliente
            )

            lifecycleScope.launch {
                continuar.isEnabled = false

                // try/catch: si la inserción falla, en vez de cerrarse la app
                // salta al catch y gestionamos el error nosotros
                try {
                    // insertUser guarda el usuario y devuelve el id que se le ha asignado
                    val id = usuarioDao.insertUser(usuario)
                    guardarFoto(id)
                    Log.d("patata", "Usuario insertado con id: " + id)

                    // Toast: mensaje corto que aparece abajo de la pantalla
                    Toast.makeText(this@Registro, getString(R.string.registro_ok), Toast.LENGTH_SHORT).show()

                    // setResult devuelve datos a la pantalla que abrió el registro (MainActivity).
                    // Le pasamos el email para que en el futuro el Login lo pueda rellenar solo.
                    val resultado = Intent()
                    resultado.putExtra("email", usuario.mail)
                    setResult(RESULT_OK, resultado)

                    // finish() cierra esta pantalla y vuelve a la anterior
                    finish()
                } catch (_: SQLiteConstraintException) {
                    // En la tabla el email es único (unique = true). Si por lo que sea se intenta
                    // guardar uno repetido, la BD lanza este error y volvemos al paso del email.
                    paso = 2
                    actualizarVista()
                    email.error = getString(R.string.error_usuario_existe)
                    continuar.isEnabled = true
                }
            }
        }

        fun gestionarPaso(sig: Boolean) {
            if (sig) {
                if (paso == 1) {
                    if (nombre.text.isNullOrBlank() || apellido.text.isNullOrBlank() || nacFecha.text.isNullOrBlank()) {
                        nombre.setText(getString(R.string.error_campos_vacios))
                        return
                    }
                } else if (paso == 2) {
                    if (email.text.isNullOrBlank() || contra.text.isNullOrBlank() || confirmContra.text.isNullOrBlank() || contra.text.toString() != confirmContra.text.toString()) {
                        email.setText(getString(R.string.error_campos_vacios))
                        return
                    }
                    // NUEVO: la comprobación del email en la BD se encarga ella misma de avanzar
                    // al paso 3, por eso hacemos return y no seguimos con el paso++ de abajo
                    comprobarEmailYAvanzar()
                    return
                } else if (paso == 3) {
                    // NUEVO: en el último paso, al pulsar "Completar" se guarda el usuario
                    guardarUsuario()
                    return
                }

                paso++
                Log.d("patata", "gestionarPaso: " + paso)
            } else {
                if (paso > 1) {
                    paso--
                    Log.d("patata", "gestionarPaso: " + paso)
                } else {
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

        val abrirCamara = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                // Aquí recibes la foto tomada y la muestras en el imageView donde estaba el icono
                fotoTomada = bitmap
                cameraIcon.setImageBitmap(bitmap)
            }
        }

        val pedirPermisoCamara = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido: Boolean ->
            if (concedido) {
                abrirCamara.launch()
            } else {
                Toast.makeText(this, getString(R.string.camara_permiso_denegado), Toast.LENGTH_SHORT).show()
            }
        }

        cameraIcon.setOnClickListener {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Toast.makeText(this, getString(R.string.camara_no_disponible), Toast.LENGTH_SHORT).show()
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamara.launch()
            } else {
                pedirPermisoCamara.launch(Manifest.permission.CAMERA)
            }
        }


        //Esto es para que solo necesite 1 click, si es focusable se necesitan 2
        nacFecha.isFocusable = false
        nacFecha.isFocusableInTouchMode = false
        nacFecha.isCursorVisible = false

        //Toda esta wea de aquí abajo es para que salga el desplegable de fechas
        nacFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Selecciona una fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                // Formatea el timestamp (en milisegundos) a texto legible
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                nacFecha.setText(formato.format(Date(selection)))
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
    }
}
