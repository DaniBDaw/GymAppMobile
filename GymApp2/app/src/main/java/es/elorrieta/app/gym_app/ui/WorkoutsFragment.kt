// =====================================================================
// WorkoutsFragment
// Primera pantalla tras el login. Lista (RecyclerView) con los workouts
// del usuario desde ROOM: nombre, nivel, completado, % y vídeo.
// Ordenados por nivel, con filtro por nivel y mensaje si no hay ninguno.
// Pulsar un workout abre Ejercicios; pulsar ▶ abre el vídeo en YouTube.
// =====================================================================
package es.elorrieta.app.gym_app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.UsuarioEntity
import es.elorrieta.app.gym_app.ui.adapter.WorkoutItem
import es.elorrieta.app.gym_app.ui.adapter.WorkoutsAdapter
import es.elorrieta.app.gym_app.util.Ajustes
import es.elorrieta.app.gym_app.util.DatosIniciales
import es.elorrieta.app.gym_app.util.MenuInferior
import es.elorrieta.app.gym_app.util.Niveles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutsFragment : Fragment() {

    // Todos los workouts del usuario (sin filtrar)
    private var listaWorkouts: MutableList<WorkoutItem> = mutableListOf()

    // Nivel elegido en el filtro (-1 = TODOS)
    private var nivelFiltro = -1

    // Botones del filtro (TODOS + 5 niveles)
    private val botonesFiltro = mutableListOf<TextView>()

    // Adapter de la lista; aquí recibimos los clics de cada tarjeta
    private val adaptador = WorkoutsAdapter(mutableListOf(), object : WorkoutsAdapter.WorkoutListener {
        override fun alPulsarWorkout(item: WorkoutItem) {
            // Abrimos Ejercicios con el id del workout pulsado
            MenuInferior.abrirSubpantalla(
                requireActivity() as AppCompatActivity,
                EjerciciosFragment.nuevaInstancia(item.workout.id)
            )
        }

        override fun alPulsarVideo(item: WorkoutItem) {
            abrirVideo(item.workout.video)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workouts, container, false)

        // RecyclerView en vertical con nuestro adapter
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerWorkouts)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adaptador

        crearFiltro(view)
        cargarDatos(view)

        return view
    }

    // ---------------- CARGAR DATOS ----------------

    private fun cargarDatos(view: View) {
        val contexto = requireContext()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(contexto)
            val usuario = db.usuarioDao().getUserById(Ajustes.leerIdUsuario(contexto))
            val items = mutableListOf<WorkoutItem>()

            if (usuario != null) {
                // Si es la primera vez, se le crea su catálogo de workouts
                DatosIniciales.crearCatalogoSiHaceFalta(contexto, usuario.id)

                // Workouts del usuario, ordenados por nivel
                val workouts = db.workoutDao().getWorkoutUser(usuario.id).sortedBy { it.nivel }

                for (workout in workouts) {
                    val ejercicios = db.ejercicioDao().getEjerciciosByWorkout(workout.id)

                    var completados = 0
                    var segundos = 0
                    for (ejercicio in ejercicios) {
                        if (ejercicio.isCompletado) {
                            completados = completados + 1
                        }
                        // Tiempo = suma del tiempo de las series
                        val series = db.serieDao().getSeriesByEjercicio(ejercicio.id)
                        for (serie in series) {
                            segundos = segundos + serie.tiempo
                        }
                    }

                    var porcentaje = 0
                    if (ejercicios.isNotEmpty()) {
                        porcentaje = completados * 100 / ejercicios.size
                    }
                    val minutos = (segundos + 59) / 60

                    items.add(WorkoutItem(workout, ejercicios.size, minutos, porcentaje))
                }
            }

            // Volvemos al hilo principal para pintar
            launch(Dispatchers.Main) {
                if (usuario == null) {
                    Toast.makeText(contexto, getString(R.string.error_bd), Toast.LENGTH_SHORT).show()
                } else {
                    mostrarNivelUsuario(view, usuario)
                }
                listaWorkouts = items
                refrescarLista()
            }
        }
    }

    // ---------------- PINTAR ----------------

    // Cabecera: nivel del usuario con su color
    private fun mostrarNivelUsuario(view: View, usuario: UsuarioEntity) {
        val posicion = Niveles.posicion(usuario.nivel)
        val color = ContextCompat.getColor(requireContext(), Niveles.colores[posicion])

        val txtNivel = view.findViewById<TextView>(R.id.txtNivelUsuario)
        txtNivel.setText(Niveles.nombres[posicion])
        txtNivel.setTextColor(color)
        view.findViewById<ImageView>(R.id.imgNivelUsuario).setColorFilter(color)
    }

    // Aplica el filtro y repinta la lista
    private fun refrescarLista() {
        val listaFiltrada = mutableListOf<WorkoutItem>()
        for (item in listaWorkouts) {
            if (nivelFiltro == -1 || item.workout.nivel == nivelFiltro) {
                listaFiltrada.add(item)
            }
        }
        adaptador.update(listaFiltrada)

        val view = getView()
        if (view != null) {
            view.findViewById<TextView>(R.id.txtDisponibles).text =
                getString(R.string.workouts_disponibles, listaFiltrada.size)

            // Mensaje de error si no hay workouts
            val txtSinWorkouts = view.findViewById<TextView>(R.id.txtSinWorkouts)
            if (listaFiltrada.isEmpty()) {
                txtSinWorkouts.visibility = View.VISIBLE
            } else {
                txtSinWorkouts.visibility = View.GONE
            }
        }
    }

    // ---------------- FILTRO POR NIVEL ----------------

    // Crea los botones TODOS + Novato ... Experto con un bucle
    private fun crearFiltro(view: View) {
        val contenedor = view.findViewById<LinearLayout>(R.id.filtroNiveles)
        contenedor.removeAllViews()
        botonesFiltro.clear()

        val margen = (8 * resources.displayMetrics.density).toInt()
        val relleno = (14 * resources.displayMetrics.density).toInt()

        for (nivel in -1..4) {
            val boton = TextView(requireContext())
            if (nivel == -1) {
                boton.setText(R.string.workouts_todos)
            } else {
                boton.setText(Niveles.nombres[nivel])
            }
            boton.textSize = 11f
            boton.isAllCaps = true
            boton.gravity = Gravity.CENTER
            boton.setPadding(relleno, 0, relleno, 0)

            val parametros = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            parametros.marginEnd = margen
            boton.layoutParams = parametros

            boton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    nivelFiltro = nivel
                    pintarFiltro()
                    refrescarLista()
                }
            })

            contenedor.addView(boton)
            botonesFiltro.add(boton)
        }
        pintarFiltro()
    }

    // El botón elegido en negro; los demás con borde y el color de su nivel
    private fun pintarFiltro() {
        for (i in 0 until botonesFiltro.size) {
            val boton = botonesFiltro[i]
            val nivel = i - 1   // posición 0 = TODOS (-1)

            if (nivel == nivelFiltro) {
                boton.setBackgroundResource(R.drawable.fondo_chip_activo)
                boton.setTextColor(ContextCompat.getColor(requireContext(), R.color.boton_principal_texto))
            } else {
                boton.setBackgroundResource(R.drawable.fondo_chip)
                if (nivel == -1) {
                    boton.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_principal))
                } else {
                    boton.setTextColor(ContextCompat.getColor(requireContext(), Niveles.colores[nivel]))
                }
            }
        }
    }

    // ---------------- VÍDEO ----------------

    // Abre el vídeo fuera de la App con un Intent implícito
    private fun abrirVideo(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), getString(R.string.error_sin_navegador), Toast.LENGTH_SHORT).show()
        }
    }
}