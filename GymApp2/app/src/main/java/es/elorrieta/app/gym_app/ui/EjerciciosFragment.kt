package es.elorrieta.app.gym_app.ui

import es.elorrieta.app.gym_app.util.Progreso
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.elorrieta.app.gym_app.ui.adapter.EjercicioItem
import es.elorrieta.app.gym_app.ui.adapter.EjerciciosAdapter
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.WorkoutEntity
import es.elorrieta.app.gym_app.temp.TempDatosPrueba
import es.elorrieta.app.gym_app.util.Niveles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EjerciciosFragment : Fragment() {

    companion object {
        const val ARG_ID_WORKOUT = "id_workout"

        // Crea el fragment pasándole el id del workout en un Bundle
        fun nuevaInstancia(idWorkout: Long): EjerciciosFragment {
            val fragment = EjerciciosFragment()
            val bundle = Bundle()
            bundle.putLong(ARG_ID_WORKOUT, idWorkout)
            fragment.arguments = bundle
            return fragment
        }
    }

    // Workout que estamos viendo y sus ejercicios
    private var workoutActual: WorkoutEntity? = null
    private var listaEjercicios: MutableList<EjercicioItem> = mutableListOf()
    // Texto escrito en el buscador
    private var textoBusqueda = ""

    // Adapter de la lista; aquí recibimos los clics de cada tarjeta
    // Adapter de la lista; aquí recibimos los clics de cada tarjeta
    private val adaptador =
        EjerciciosAdapter(mutableListOf(), object : EjerciciosAdapter.EjercicioListener {
            override fun alPulsarEjercicio(item: EjercicioItem) {
                // Paso 9: abrir el detalle del ejercicio
            }

            override fun alMarcarEjercicio(item: EjercicioItem, marcado: Boolean) {
                cambiarEjercicio(item, marcado)
            }
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ejercicios, container, false)

        // Flecha: volvemos a la pantalla de antes (Workouts)
        val btnVolver = view.findViewById<ImageView>(R.id.btnVolver)
        btnVolver.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                parentFragmentManager.popBackStack()
            }
        })

        // RecyclerView en vertical con nuestro adapter
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerEjercicios)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adaptador
        configurarBuscador(view)
        configurarBotonesWorkout(view)

        cargarDatos(view)

        return view
    }

    // ---------------- CARGAR DATOS ----------------

    private fun cargarDatos(view: View) {
        val contexto = requireContext()

        // Id del workout que nos han pasado (-1 si no hay)
        var idWorkout = -1L
        val argumentos = arguments
        if (argumentos != null) {
            idWorkout = argumentos.getLong(ARG_ID_WORKOUT, -1L)
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(contexto)
            var workout: WorkoutEntity? = null

            if (idWorkout != -1L) {
                workout = db.workoutDao().getWorkoutById(idWorkout)
            }

            // TODO TEMP: si no nos pasan workout, cogemos el primero del usuario
            if (workout == null) {
                val usuario = TempDatosPrueba.obtenerUsuario(contexto)
                if (usuario != null) {
                    var workouts = db.workoutDao().getWorkoutUser(usuario.id)
                    if (workouts.isEmpty()) {
                        TempDatosPrueba.crearWorkouts(contexto, usuario.id)
                        workouts = db.workoutDao().getWorkoutUser(usuario.id)
                    }
                    if (workouts.isNotEmpty()) {
                        workout = workouts[0]
                    }
                }
            }

            // Ejercicios del workout, ordenados por su campo "orden"
            val items = mutableListOf<EjercicioItem>()
            if (workout != null) {
                val ejercicios = db.ejercicioDao().getEjerciciosByWorkout(workout.id).sortedBy { it.orden }
                for (ejercicio in ejercicios) {
                    // El tiempo de un ejercicio es la suma del tiempo de sus series (en segundos)
                    val series = db.serieDao().getSeriesByEjercicio(ejercicio.id)
                    var segundos = 0
                    for (serie in series) {
                        segundos = segundos + serie.tiempo
                    }
                    val minutos = (segundos + 59) / 60   // redondeamos hacia arriba
                    items.add(EjercicioItem(ejercicio, series.size, minutos))
                }
            }

            val workoutEncontrado = workout

            // Volvemos al hilo principal para pintar
            launch(Dispatchers.Main) {
                if (workoutEncontrado == null) {
                    mostrarMensaje(R.string.error_sin_workouts)
                } else {
                    workoutActual = workoutEncontrado
                    listaEjercicios = items
                    mostrarWorkout(view)
                    refrescarLista()
                }
            }
        }
    }

    // ---------------- PINTAR PANTALLA ----------------

    // Nombre, nivel, resumen y botón de vídeo
    private fun mostrarWorkout(view: View) {
        val workout = workoutActual
        if (workout == null) {
            return
        }

        val posicion = Niveles.posicion(workout.nivel)
        val color = ContextCompat.getColor(requireContext(), Niveles.colores[posicion])

        view.findViewById<TextView>(R.id.txtNombreWorkout).text = workout.nombre
        view.findViewById<ImageView>(R.id.imgNivel).setColorFilter(color)

        // Minutos totales del workout
        var minutosTotales = 0
        for (item in listaEjercicios) {
            minutosTotales = minutosTotales + item.minutos
        }

        val nombreNivel = getString(Niveles.nombres[posicion])
        view.findViewById<TextView>(R.id.txtResumenWorkout).text =
            getString(R.string.workout_resumen, nombreNivel, listaEjercicios.size, minutosTotales)

        // Botón de vídeo solo si el workout tiene vídeo
        // Botón de vídeo solo si el workout tiene vídeo
        val btnVideo = view.findViewById<Button>(R.id.btnVideo)
        if (workout.video.isEmpty()) {
            btnVideo.visibility = View.GONE
        } else {
            btnVideo.visibility = View.VISIBLE
            btnVideo.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    abrirVideo(workout.video)
                }
            })
        }

        adaptador.colorNivel = color
    }

    // Repinta la lista (filtrada por el buscador) y el estado
    private fun refrescarLista() {
        // Solo los ejercicios cuyo nombre contiene el texto buscado
        val listaFiltrada = mutableListOf<EjercicioItem>()
        for (item in listaEjercicios) {
            if (item.ejercicio.nombre.lowercase().contains(textoBusqueda.lowercase())) {
                listaFiltrada.add(item)
            }
        }
        adaptador.update(listaFiltrada)

        val view = getView()
        if (view != null) {
            actualizarEstado(view)
        }
    }

    // Cada vez que se escribe en el buscador, se filtra la lista
    private fun configurarBuscador(view: View) {
        val etBuscar = view.findViewById<EditText>(R.id.etBuscar)
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                textoBusqueda = s.toString().trim()
                refrescarLista()
            }
        })
    }

    // Progreso, badge "Completado" y qué botón se ve
    private fun actualizarEstado(view: View) {
        val workout = workoutActual
        if (workout == null) {
            return
        }

        var completados = 0
        for (item in listaEjercicios) {
            if (item.ejercicio.isCompletado) {
                completados = completados + 1
            }
        }
        val total = listaEjercicios.size

        view.findViewById<TextView>(R.id.txtProgreso).text =
            getString(R.string.ejercicios_progreso, completados, total)

        val barra = view.findViewById<ProgressBar>(R.id.barraProgreso)
        if (total > 0) {
            barra.progress = completados * 100 / total
        } else {
            barra.progress = 0
        }

        // Mensaje si el workout no tiene ejercicios
        val txtSinEjercicios = view.findViewById<TextView>(R.id.txtSinEjercicios)
        if (total == 0) {
            txtSinEjercicios.visibility = View.VISIBLE
        } else {
            txtSinEjercicios.visibility = View.GONE
        }

        val txtBadge = view.findViewById<TextView>(R.id.txtBadgeCompletado)
        val btnMarcar = view.findViewById<Button>(R.id.btnMarcarWorkout)
        val btnDesmarcar = view.findViewById<Button>(R.id.btnDesmarcarWorkout)

        if (workout.isCompletado) {
            txtBadge.visibility = View.VISIBLE
            btnMarcar.visibility = View.GONE
            btnDesmarcar.visibility = View.VISIBLE
        } else {
            txtBadge.visibility = View.GONE
            btnMarcar.visibility = View.VISIBLE
            btnDesmarcar.visibility = View.GONE
        }
    }


    // ---------------- MARCAR / DESMARCAR ----------------

    // Check de un ejercicio de la lista
    private fun cambiarEjercicio(item: EjercicioItem, marcado: Boolean) {
        val contexto = requireContext()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val nivelNuevo: Int
            if (marcado) {
                nivelNuevo = Progreso.marcarEjercicio(contexto, item.ejercicio)
            } else {
                nivelNuevo = Progreso.desmarcarEjercicio(contexto, item.ejercicio)
            }

            launch(Dispatchers.Main) {
                avisarCambioNivel(nivelNuevo, marcado)
                val view = getView()
                if (view != null) {
                    cargarDatos(view)   // recargamos para ver los cambios
                }
            }
        }
    }

    // Botones "Marcar workout completo" y "Desmarcar workout"
    private fun configurarBotonesWorkout(view: View) {
        val btnMarcar = view.findViewById<Button>(R.id.btnMarcarWorkout)
        val btnDesmarcar = view.findViewById<Button>(R.id.btnDesmarcarWorkout)

        btnMarcar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                cambiarWorkout(true)
            }
        })

        btnDesmarcar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                cambiarWorkout(false)
            }
        })
    }

    private fun cambiarWorkout(completar: Boolean) {
        val workout = workoutActual
        if (workout == null) {
            return
        }
        val contexto = requireContext()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val nivelNuevo: Int
            if (completar) {
                nivelNuevo = Progreso.completarWorkout(contexto, workout)
            } else {
                nivelNuevo = Progreso.desmarcarWorkout(contexto, workout)
            }

            launch(Dispatchers.Main) {
                avisarCambioNivel(nivelNuevo, completar)
                val view = getView()
                if (view != null) {
                    cargarDatos(view)
                }
            }
        }
    }

    // Si el nivel ha cambiado, se lo decimos al usuario
    private fun avisarCambioNivel(nivelNuevo: Int, subiendo: Boolean) {
        if (nivelNuevo == -1) {
            return
        }
        val nombreNivel = getString(Niveles.nombres[Niveles.posicion(nivelNuevo)])
        val texto: String
        if (subiendo) {
            texto = getString(R.string.nivel_subido, nombreNivel)
        } else {
            texto = getString(R.string.nivel_bajado, nombreNivel)
        }
        Toast.makeText(requireContext(), texto, Toast.LENGTH_LONG).show()
    }

    private fun mostrarMensaje(idTexto: Int) {
        Toast.makeText(requireContext(), getString(idTexto), Toast.LENGTH_SHORT).show()
    }

    // Abre el vídeo fuera de la App (YouTube o el navegador) con un Intent implícito
    private fun abrirVideo(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No hay ninguna App en el móvil que sepa abrir ese enlace
            mostrarMensaje(R.string.error_sin_navegador)
        }
    }

}