package es.elorrieta.app.gym_app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.data.prefs.PrefsManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val prefs = PrefsManager(this)


        val emailActivo = prefs.getSesionActual()
        tvBienvenida.text = "Sesión iniciada como:\n$emailActivo"


        btnLogout.setOnClickListener {
            prefs.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}