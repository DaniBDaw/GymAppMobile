package es.elorrieta.app.gym_app.util

import es.elorrieta.app.gym_app.ui.WorkoutsFragment
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import es.elorrieta.app.gym_app.ui.CompletadosFragment
import es.elorrieta.app.gym_app.ui.HomeFragment
import es.elorrieta.app.gym_app.ui.PerfilFragment
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.ui.LoginFragment

object MenuInferior {

    // Pantallas del menú
    const val HOME = 0
    const val COMPLETADOS = 1
    const val WORKOUTS = 2
    const val PERFIL = 3

    const val LOGIN = 4

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

        mostrarMenu(activity, pantallaActual != LOGIN)
        actualizarIconos(activity)
    }

    // Cambia el Fragment que se ve en el contenedor
    fun mostrar(activity: AppCompatActivity, pantalla: Int) {
        val gestor = activity.supportFragmentManager
        val hayFragment = gestor.findFragmentById(R.id.fragment_container) != null

        // Si ya estamos en esa pantalla (y no dentro de una hija), no hacemos nada
        if (pantalla == pantallaActual && hayFragment && gestor.backStackEntryCount == 0) {
            return
        }

        // Cerramos las pantallas hijas que hubiera abiertas (p.ej. Ejercicios)
        gestor.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

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

        gestor.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        pantallaAnterior = pantallaActual
        pantallaActual = pantalla
        actualizarIconos(activity)
        mostrarMenu(activity, true)
    }

    // Abre una pantalla "hija" encima de la actual (Ejercicios, Detalle...).
    // addToBackStack hace que con "atrás" se vuelva a la anterior.
    fun abrirSubpantalla(activity: AppCompatActivity, fragment: Fragment) {
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Muestra el Login (sin menú). Se usa al abrir la App y al cerrar sesión.
    fun mostrarLogin(activity: AppCompatActivity) {
        val gestor = activity.supportFragmentManager
        gestor.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        gestor.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment())
            .commit()

        pantallaActual = LOGIN
        mostrarMenu(activity, false)
    }

    // Enseña u oculta el menú inferior
    fun mostrarMenu(activity: AppCompatActivity, visible: Boolean) {
        val menu = activity.findViewById<View>(R.id.menuInferior)
        if (visible) {
            menu.visibility = View.VISIBLE
        } else {
            menu.visibility = View.GONE
        }
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