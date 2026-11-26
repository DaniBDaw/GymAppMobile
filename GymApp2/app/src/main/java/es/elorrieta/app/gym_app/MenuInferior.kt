package es.elorrieta.app.gym_app

import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object MenuInferior {

    // Pantallas del menú
    const val HOME = 0
    const val COMPLETADOS = 1
    const val WORKOUTS = 2
    const val PERFIL = 3

    // La pantalla que se ve ahora y la anterior (para volver desde el Perfil)
    var pantallaActual = HOME
    var pantallaAnterior = HOME

    // Se llama una vez desde MainActivity
    fun configurar(activity: AppCompatActivity) {
        val btnHome = activity.findViewById<ImageView>(R.id.btnMenuHome)
        val btnCompletados = activity.findViewById<ImageView>(R.id.btnMenuCompletados)
        val btnWorkouts = activity.findViewById<ImageView>(R.id.btnMenuWorkouts)
        val btnPerfil = activity.findViewById<ImageView>(R.id.btnMenuPerfil)

        btnHome.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                mostrar(activity, HOME)
            }
        })

        btnCompletados.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                mostrar(activity, COMPLETADOS)
            }
        })

        btnWorkouts.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                mostrar(activity, WORKOUTS)
            }
        })

        btnPerfil.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                mostrar(activity, PERFIL)
            }
        })

        actualizarIconos(activity)
    }

    // Cambia el Fragment que se ve en el contenedor
    fun mostrar(activity: AppCompatActivity, pantalla: Int) {
        val hayFragment = activity.supportFragmentManager.findFragmentById(R.id.fragment_container) != null

        // Si ya estamos en esa pantalla, no hacemos nada
        if (pantalla == pantallaActual && hayFragment) {
            return
        }

        val fragment: Fragment
        if (pantalla == COMPLETADOS) {
            fragment = CompletadosFragment()
        } else if (pantalla == WORKOUTS) {
            fragment = WorkoutsFragment()
        } else if (pantalla == PERFIL) {
            fragment = PerfilFragment()
        } else {
            fragment = HomeFragment()
        }

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        pantallaAnterior = pantallaActual
        pantallaActual = pantalla
        actualizarIconos(activity)
    }

    // Pinta el icono activo y pone el punto debajo
    fun actualizarIconos(activity: AppCompatActivity) {
        val btnHome = activity.findViewById<ImageView>(R.id.btnMenuHome)
        val btnCompletados = activity.findViewById<ImageView>(R.id.btnMenuCompletados)
        val puntoHome = activity.findViewById<ImageView>(R.id.puntoHome)
        val puntoCompletados = activity.findViewById<ImageView>(R.id.puntoCompletados)
        val puntoPerfil = activity.findViewById<ImageView>(R.id.puntoPerfil)

        pintar(activity, btnHome, pantallaActual == HOME)
        pintar(activity, btnCompletados, pantallaActual == COMPLETADOS)

        mostrarPunto(puntoHome, pantallaActual == HOME)
        mostrarPunto(puntoCompletados, pantallaActual == COMPLETADOS)
        mostrarPunto(puntoPerfil, pantallaActual == PERFIL)
    }

    private fun pintar(activity: AppCompatActivity, boton: ImageView, activo: Boolean) {
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
}