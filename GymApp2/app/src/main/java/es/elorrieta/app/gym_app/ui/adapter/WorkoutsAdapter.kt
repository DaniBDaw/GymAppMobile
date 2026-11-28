// =====================================================================
// WorkoutsAdapter
// Conecta la lista de workouts (ROOM) con el RecyclerView.
// Cada tarjeta muestra: nombre, nivel (color + texto), nº de ejercicios,
// minutos, % de ejercicios completados, si está completado (fondo verde)
// y el botón de vídeo solo si tiene vídeo.
// =====================================================================
package es.elorrieta.app.gym_app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import es.elorrieta.app.gym_app.R
import es.elorrieta.app.gym_app.database.WorkoutEntity
import es.elorrieta.app.gym_app.util.Niveles

// Un workout + datos calculados a partir de sus ejercicios y series
data class WorkoutItem(
    val workout: WorkoutEntity,
    val numEjercicios: Int,
    val minutos: Int,
    val porcentaje: Int
)

class WorkoutsAdapter(
    private var workouts: MutableList<WorkoutItem>,
    private val listener: WorkoutListener
) : RecyclerView.Adapter<WorkoutsAdapter.WorkoutViewHolder>() {

    // El Fragment implementa esto para enterarse de los clics
    interface WorkoutListener {
        fun alPulsarWorkout(item: WorkoutItem)
        fun alPulsarVideo(item: WorkoutItem)
    }

    // ViewHolder: guarda las vistas de cada tarjeta
    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fondo: LinearLayout = view.findViewById(R.id.fondoWorkout)
        val franjaNivel: View = view.findViewById(R.id.franjaNivelWorkout)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreWorkoutItem)
        val txtInfo: TextView = view.findViewById(R.id.txtInfoWorkoutItem)
        val barra: ProgressBar = view.findViewById(R.id.barraWorkoutItem)
        val txtPorcentaje: TextView = view.findViewById(R.id.txtPorcentajeWorkoutItem)
        val btnVideo: FrameLayout = view.findViewById(R.id.btnVideoWorkout)
    }

    // 1. Crea una tarjeta vacía con el diseño de item_workout.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    // 2. Rellena la tarjeta de la posición indicada
    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val item = workouts[position]
        val contexto = holder.itemView.context

        // Nivel: nombre y color
        val posicion = Niveles.posicion(item.workout.nivel)
        val nombreNivel = contexto.getString(Niveles.nombres[posicion])
        val colorNivel = ContextCompat.getColor(contexto, Niveles.colores[posicion])

        holder.txtNombre.text = item.workout.nombre
        holder.txtInfo.text = contexto.getString(R.string.workout_resumen, nombreNivel, item.numEjercicios, item.minutos)
        holder.franjaNivel.setBackgroundColor(colorNivel)

        // % de ejercicios completados
        holder.barra.progress = item.porcentaje
        holder.txtPorcentaje.text = contexto.getString(R.string.workout_porcentaje, item.porcentaje)

        // Fondo verde si el workout está completado
        if (item.workout.isCompletado) {
            holder.fondo.setBackgroundResource(R.drawable.fondo_tarjeta_completada)
        } else {
            holder.fondo.setBackgroundResource(R.drawable.fondo_tarjeta)
        }

        // Botón de vídeo solo si tiene vídeo
        if (item.workout.video.isEmpty()) {
            holder.btnVideo.visibility = View.GONE
        } else {
            holder.btnVideo.visibility = View.VISIBLE
            holder.btnVideo.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    listener.alPulsarVideo(item)
                }
            })
        }

        // Clic en la tarjeta -> pantalla de Ejercicios
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                listener.alPulsarWorkout(item)
            }
        })
    }

    // 3. Cuántas tarjetas hay
    override fun getItemCount(): Int {
        return workouts.size
    }

    // 4. Cambia la lista y repinta
    fun update(nuevosWorkouts: MutableList<WorkoutItem>) {
        workouts = nuevosWorkouts
        notifyDataSetChanged()
    }
}