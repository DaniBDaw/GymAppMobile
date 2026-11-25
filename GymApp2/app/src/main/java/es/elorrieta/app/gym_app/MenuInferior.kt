package es.elorrieta.app.gym_app

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat

object MenuInferior {

    // Pantallas del menú
    const val HOME = 0
    const val COMPLETADOS = 1
    const val WORKOUTS = 2
    const val PERFIL = 3

    // Se llama desde cada Activity que tenga el menú
    fun configurar(activity: Activity, pantallaActual: Int) {
        val btnHome = activity.findViewById<ImageView>(R.id.btnMenuHome)
        val btnCompletados = activity.findViewById<ImageView>(R.id.btnMenuCompletados)
        val btnWorkouts = activity.findViewById<ImageView>(R.id.btnMenuWorkouts)
        val btnPerfil = activity.findViewById<ImageView>(R.id.btnMenuPerfil)

        val puntoHome = activity.findViewById<ImageView>(R.id.puntoHome)
        val puntoCompletados = activity.findViewById<ImageView>(R.id.puntoCompletados)
        val puntoPerfil = activity.findViewById<ImageView>(R.id.puntoPerfil)

        // Icono activo en blanco, los demás en gris
        pintar(activity, btnHome, pantallaActual == HOME)
        pintar(activity, btnCompletados, pantallaActual == COMPLETADOS)

        // Punto debajo de la pantalla activa
        mostrarPunto(puntoHome, pantallaActual == HOME)
        mostrarPunto(puntoCompletados, pantallaActual == COMPLETADOS)
        mostrarPunto(puntoPerfil, pantallaActual == PERFIL)

        btnHome.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                abrir(activity, MainActivity::class.java, pantallaActual == HOME)
            }
        })

        btnCompletados.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                abrir(activity, CompletadosActivity::class.java, pantallaActual == COMPLETADOS)
            }
        })

        btnWorkouts.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                abrir(activity, WorkoutsActivity::class.java, pantallaActual == WORKOUTS)
            }
        })

        btnPerfil.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                abrir(activity, PerfilActivity::class.java, pantallaActual == PERFIL)
            }
        })
    }

    private fun pintar(activity: Activity, boton: ImageView, activo: Boolean) {
        if (activo) {
            boton.setColorFilter(ContextCompat.getColor(activity, R.color.boton_principal_texto))
        } else {
            boton.setColorFilter(ContextCompat.getColor(activity, R.color.texto_placeholder))
        }
    }

    private fun mostrarPunto(punto: ImageView, activo: Boolean) {
        if (activo) {
            punto.visibility = View.VISIBLE
        } else {
            punto.visibility = View.INVISIBLE
        }
    }

    private fun abrir(activity: Activity, destino: Class<*>, esLaActual: Boolean) {
        // Si ya estamos en esa pantalla, no hacemos nada
        if (esLaActual) {
            return
        }
        val intent = Intent(activity, destino)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0) // sin animación entre pestañas

        // Al ir al Perfil no cerramos la pantalla actual, para poder volver a ella
        if (destino != PerfilActivity::class.java) {
            activity.finish()
        }
    }
}