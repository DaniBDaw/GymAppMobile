package es.elorrieta.app.gym_app.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import es.elorrieta.app.gym_app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.graphics.Bitmap

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

        val continuar = findViewById<Button>(R.id.BtnRegisterContinuar)
        val volver = findViewById<Button>(R.id.BtnRegisterVolver)

        val nombre= findViewById<EditText>(R.id.InputNombre)
        val apellido= findViewById<EditText>(R.id.InputApellido)
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

        email.visibility= View.GONE
        contra.visibility= View.GONE
        confirmContra.visibility= View.GONE
        cameraIcon.visibility= View.GONE

        var paso=1

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
                cameraIcon.visibility= View.GONE

                email.visibility = View.VISIBLE
                contra.visibility = View.VISIBLE
                confirmContra.visibility = View.VISIBLE
                continuar.setText(R.string.Continuar)
            } else if (paso == 3) {
                nombre.visibility = View.GONE
                apellido.visibility = View.GONE
                nacFecha.visibility = View.GONE

                email.visibility = View.GONE
                contra.visibility = View.GONE
                confirmContra.visibility = View.GONE

                cameraIcon.visibility= View.VISIBLE
                continuar.setText(R.string.Completar)

            }
        }

        fun gestionarPaso(sig: Boolean) {
            if (sig) {
                if (paso == 1) {
                    if (nombre.text.isNullOrBlank() || apellido.text.isNullOrBlank() || nacFecha.text.isNullOrBlank()) {
                        nombre.setText(getString(R.string.ErrorPrueba))
                        return
                    }
                } else if (paso == 2) {
                    if (email.text.isNullOrBlank() || contra.text.isNullOrBlank() || confirmContra.text.isNullOrBlank()||contra.text.toString()!=confirmContra.text.toString()) {
                        email.setText(getString(R.string.ErrorPrueba))
                        return
                    }
                }/*else if (paso==3){
                    //Futuro, volver al Login con el email puesto en el input
                }*/

                paso++
                Log.d("patata", "gestionarPaso: "+paso)
            } else {
                if (paso > 1) {
                    paso--
                    Log.d("patata", "gestionarPaso: "+paso)
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
                cameraIcon.setImageBitmap(bitmap)
            }
        }

        cameraIcon.setOnClickListener {
            abrirCamara.launch()
        }

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