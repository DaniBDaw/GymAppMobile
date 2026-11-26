// =====================================================================
// TempWorkoutsFragment (TEMPORAL)
// Hueco para la pantalla de Workouts de DB. De momento solo tiene
// un botón de prueba que abre Ejercicios con el primer workout.
// =====================================================================
package es.elorrieta.app.gym_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class TempWorkoutsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workouts, container, false)

        // TODO TEMP: botón de prueba que abre Ejercicios con el primer workout del usuario (-1)
        val btnProbar = view.findViewById<Button>(R.id.btnProbarEjercicios)
        btnProbar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                MenuInferior.abrirSubpantalla(
                    requireActivity() as AppCompatActivity,
                    EjerciciosFragment.nuevaInstancia(-1L)
                )
            }
        })

        return view
    }
}