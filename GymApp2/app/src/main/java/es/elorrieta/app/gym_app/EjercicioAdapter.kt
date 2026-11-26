// =====================================================================
// EjerciciosAdapter
// Conecta la lista de ejercicios (datos de ROOM) con el RecyclerView.
// Rellena cada tarjeta (item_ejercicio.xml) con nombre, series, minutos,
// color del nivel y si está completado, y avisa al Fragment de los clics.
// =====================================================================
package es.elorrieta.app.gym_app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.elorrieta.app.gym_app.database.EjercicioEntity

// Un ejercicio + datos calculados a partir de sus series
data class EjercicioItem(
    val ejercicio: EjercicioEntity,
    val numSeries: Int,
    val minutos: Int
)

class EjerciciosAdapter(
    private var ejercicios: MutableList<EjercicioItem>,
    private val listener: EjercicioListener
) : RecyclerView.Adapter<EjerciciosAdapter.EjercicioViewHolder>() {

    // El Fragment implementa esto para enterarse de los clics
    interface EjercicioListener {
        fun alPulsarEjercicio(item: EjercicioItem)
        fun alMarcarEjercicio(item: EjercicioItem, marcado: Boolean)
    }

    // Color del nivel del workout (para la franja de la izquierda)
    var colorNivel: Int = Color.GRAY

    // ViewHolder: guarda las vistas de cada tarjeta
    class EjercicioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fondoItem: LinearLayout = view.findViewById(R.id.fondoItem)
        val franjaNivel: View = view.findViewById(R.id.franjaNivel)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreEjercicio)
        val txtInfo: TextView = view.findViewById(R.id.txtInfoEjercicio)
        val chkCompletado: CheckBox = view.findViewById(R.id.chkCompletado)
    }

    // 1. Crea una tarjeta vacía con el diseño de item_ejercicio.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio, parent, false)
        return EjercicioViewHolder(view)
    }

    // 2. Rellena la tarjeta de la posición indicada
    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val item = ejercicios[position]
        val contexto = holder.itemView.context

        holder.txtNombre.text = item.ejercicio.nombre
        holder.txtInfo.text = contexto.getString(R.string.ejercicio_resumen, item.numSeries, item.minutos)
        holder.franjaNivel.setBackgroundColor(colorNivel)
        holder.chkCompletado.isChecked = item.ejercicio.isCompletado

        // Fondo verde si está completado
        if (item.ejercicio.isCompletado) {
            holder.fondoItem.setBackgroundResource(R.drawable.fondo_tarjeta_completada)
        } else {
            holder.fondoItem.setBackgroundResource(R.drawable.fondo_tarjeta)
        }

        // Clic en la tarjeta -> detalle del ejercicio
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                listener.alPulsarEjercicio(item)
            }
        })

        // Clic en el check -> marcar / desmarcar
        holder.chkCompletado.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                listener.alMarcarEjercicio(item, holder.chkCompletado.isChecked)
            }
        })
    }

    // 3. Cuántas tarjetas hay
    override fun getItemCount(): Int {
        return ejercicios.size
    }

    // 4. Cambia la lista y repinta
    fun update(nuevosEjercicios: MutableList<EjercicioItem>) {
        ejercicios = nuevosEjercicios
        notifyDataSetChanged()
    }
}