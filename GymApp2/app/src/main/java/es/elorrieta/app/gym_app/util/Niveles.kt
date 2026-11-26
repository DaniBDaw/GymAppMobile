package es.elorrieta.app.gym_app.util

import es.elorrieta.app.gym_app.R

object Niveles {

    // Posición 0 = Novato ... 4 = Experto
    val nombres = arrayOf(
        R.string.nivel_novato,
        R.string.nivel_principiante,
        R.string.nivel_intermedio,
        R.string.nivel_avanzado,
        R.string.nivel_experto
    )

    val colores = arrayOf(
        R.color.nivel_novato,
        R.color.nivel_principiante,
        R.color.nivel_intermedio,
        R.color.nivel_avanzado,
        R.color.nivel_experto
    )

    // Por si viene un número raro, lo dejamos entre 0 y 4
    fun posicion(nivel: Int): Int {
        if (nivel < 0) {
            return 0
        }
        if (nivel > 4) {
            return 4
        }
        return nivel
    }
}