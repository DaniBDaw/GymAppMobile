package es.elorrieta.app.gym_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity

class PerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Cargamos el layout del fragment
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Flecha para volver a la pantalla anterior
        val btnVolver = view.findViewById<ImageView>(R.id.btnVolver)
        btnVolver.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                // Volvemos a la pantalla en la que estábamos antes
                MenuInferior.mostrar(requireActivity() as AppCompatActivity, MenuInferior.pantallaAnterior)
            }
        })

        configurarIdioma(view)
        configurarTema(view)


        return view
    }

    // ---------------- TEMA ----------------

    private fun configurarTema(view: View) {
        val btnTemaClaro = view.findViewById<TextView>(R.id.btnTemaClaro)
        val btnTemaOscuro = view.findViewById<TextView>(R.id.btnTemaOscuro)
        val opcionesTema = arrayOf(btnTemaClaro, btnTemaOscuro)

        // Marcamos el tema que está guardado
        if (Ajustes.leerTema(requireContext()) == Ajustes.TEMA_OSCURO) {
            marcarOpcion(btnTemaOscuro, opcionesTema)
        } else {
            marcarOpcion(btnTemaClaro, opcionesTema)
        }

        btnTemaClaro.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Ajustes.guardarTema(requireContext(), Ajustes.TEMA_CLARO)
                Ajustes.aplicarTema(Ajustes.TEMA_CLARO)
            }
        })

        btnTemaOscuro.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Ajustes.guardarTema(requireContext(), Ajustes.TEMA_OSCURO)
                Ajustes.aplicarTema(Ajustes.TEMA_OSCURO)
            }
        })
    }



    // ---------------- IDIOMA ----------------

    private fun configurarIdioma(view: View) {
        val btnIdiomaEs = view.findViewById<TextView>(R.id.btnIdiomaEs)
        val btnIdiomaEn = view.findViewById<TextView>(R.id.btnIdiomaEn)
        val btnIdiomaKa = view.findViewById<TextView>(R.id.btnIdiomaKa)
        val opcionesIdioma = arrayOf(btnIdiomaEs, btnIdiomaEn, btnIdiomaKa)

        // Marcamos el idioma que está guardado
        val idiomaGuardado = Ajustes.leerIdioma(requireContext())
        if (idiomaGuardado == Ajustes.IDIOMA_EN) {
            marcarOpcion(btnIdiomaEn, opcionesIdioma)
        } else if (idiomaGuardado == Ajustes.IDIOMA_KA) {
            marcarOpcion(btnIdiomaKa, opcionesIdioma)
        } else {
            marcarOpcion(btnIdiomaEs, opcionesIdioma)
        }

        btnIdiomaEs.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                cambiarIdioma(Ajustes.IDIOMA_ES)
            }
        })

        btnIdiomaEn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                cambiarIdioma(Ajustes.IDIOMA_EN)
            }
        })

        btnIdiomaKa.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                cambiarIdioma(Ajustes.IDIOMA_KA)
            }
        })
    }

    private fun cambiarIdioma(idioma: String) {
        Ajustes.guardarIdioma(requireContext(), idioma)
        Ajustes.aplicarIdioma(idioma)
    }



    // Pone en negro la opción elegida y deja las demás sin fondo
    private fun marcarOpcion(seleccionada: TextView, opciones: Array<TextView>) {
        for (opcion in opciones) {
            if (opcion == seleccionada) {
                opcion.setBackgroundResource(R.drawable.fondo_opcion_activa)
                opcion.setTextColor(ContextCompat.getColor(requireContext(), R.color.boton_principal_texto))
            } else {
                opcion.background = null
                opcion.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_principal))
            }
        }
    }
}