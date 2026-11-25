package es.elorrieta.app.gym_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

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
                requireActivity().finish()
            }
        })

        return view
    }
}