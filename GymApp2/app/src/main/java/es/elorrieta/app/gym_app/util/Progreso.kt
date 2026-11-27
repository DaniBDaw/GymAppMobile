// =====================================================================
// Progreso
// Lógica de marcar / desmarcar workouts y ejercicios en ROOM,
// y de recalcular el nivel del usuario (enunciado del reto):
//  - Completar un workout de nivel SUPERIOR al del usuario -> sube a ese nivel.
//  - Desmarcar: si ya no tiene otro workout completo de su nivel o superior,
//    baja al nivel del workout completo más alto (o a Novato si no tiene ninguno).
//  - Marcar un ejercicio marca también los ANTERIORES.
//  - Desmarcar un ejercicio desmarca también los SIGUIENTES.
// Todas son "suspend" porque usan ROOM: se llaman dentro de una corrutina.
// Devuelven el nuevo nivel si ha cambiado, o -1 si no ha cambiado.
// =====================================================================
package es.elorrieta.app.gym_app.util

import android.content.Context
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.EjercicioEntity
import es.elorrieta.app.gym_app.database.WorkoutEntity

object Progreso {

    // ---------------- WORKOUT ----------------

    // Marca el workout y TODOS sus ejercicios como completados
    suspend fun completarWorkout(context: Context, workout: WorkoutEntity): Int {
        val db = AppDatabase.getDatabase(context)

        val ejercicios = db.ejercicioDao().getEjerciciosByWorkout(workout.id)
        for (ejercicio in ejercicios) {
            db.ejercicioDao().updateEjercicio(ejercicio.copy(isCompletado = true))
        }
        db.workoutDao().updateWorkout(workout.copy(isCompletado = true))

        return subirNivelSiHaceFalta(context, workout)
    }

    // Desmarca el workout y TODOS sus ejercicios
    suspend fun desmarcarWorkout(context: Context, workout: WorkoutEntity): Int {
        val db = AppDatabase.getDatabase(context)

        val ejercicios = db.ejercicioDao().getEjerciciosByWorkout(workout.id)
        for (ejercicio in ejercicios) {
            db.ejercicioDao().updateEjercicio(ejercicio.copy(isCompletado = false))
        }
        db.workoutDao().updateWorkout(workout.copy(isCompletado = false))

        return bajarNivelSiHaceFalta(context, workout.idUsuario)
    }

    // ---------------- EJERCICIO ----------------

    // Marca este ejercicio y todos los ANTERIORES (los ejercicios son series).
    // Si con esto se completan todos, se completa el workout entero.
    suspend fun marcarEjercicio(context: Context, ejercicio: EjercicioEntity): Int {
        val db = AppDatabase.getDatabase(context)

        val ejercicios = db.ejercicioDao().getEjerciciosByWorkout(ejercicio.idWorkout)
        var todosCompletados = true
        for (e in ejercicios) {
            if (e.orden <= ejercicio.orden) {
                db.ejercicioDao().updateEjercicio(e.copy(isCompletado = true))
            } else if (!e.isCompletado) {
                todosCompletados = false
            }
        }

        // Era el último que faltaba -> workout completo -> revisar nivel
        val workout = db.workoutDao().getWorkoutById(ejercicio.idWorkout)
        if (workout != null && todosCompletados) {
            return completarWorkout(context, workout)
        }
        return -1
    }

    // Desmarca este ejercicio y todos los SIGUIENTES.
    // El workout deja de estar completo -> revisar nivel.
    suspend fun desmarcarEjercicio(context: Context, ejercicio: EjercicioEntity): Int {
        val db = AppDatabase.getDatabase(context)

        val ejercicios = db.ejercicioDao().getEjerciciosByWorkout(ejercicio.idWorkout)
        for (e in ejercicios) {
            if (e.orden >= ejercicio.orden) {
                db.ejercicioDao().updateEjercicio(e.copy(isCompletado = false))
            }
        }

        val workout = db.workoutDao().getWorkoutById(ejercicio.idWorkout)
        if (workout != null && workout.isCompletado) {
            db.workoutDao().updateWorkout(workout.copy(isCompletado = false))
            return bajarNivelSiHaceFalta(context, workout.idUsuario)
        }
        return -1
    }

    // ---------------- NIVEL ----------------

    // Si el workout completado es de nivel superior al del usuario, sube a ese nivel
    private suspend fun subirNivelSiHaceFalta(context: Context, workout: WorkoutEntity): Int {
        val dao = AppDatabase.getDatabase(context).usuarioDao()
        val usuario = dao.getUserById(workout.idUsuario)
        if (usuario == null) {
            return -1
        }

        if (workout.nivel > usuario.nivel) {
            dao.updateUser(usuario.copy(nivel = workout.nivel))
            return workout.nivel
        }
        return -1
    }

    // Si ya no tiene ningún workout completo de su nivel o superior,
    // baja al nivel del workout completo más alto (o a 0 si no tiene ninguno)
    private suspend fun bajarNivelSiHaceFalta(context: Context, idUsuario: Long): Int {
        val db = AppDatabase.getDatabase(context)
        val usuario = db.usuarioDao().getUserById(idUsuario)
        if (usuario == null) {
            return -1
        }

        val workouts = db.workoutDao().getWorkoutUser(idUsuario)
        var nivelMasAlto = 0
        var tieneDeSuNivel = false
        for (w in workouts) {
            if (w.isCompletado) {
                if (w.nivel > nivelMasAlto) {
                    nivelMasAlto = w.nivel
                }
                if (w.nivel >= usuario.nivel) {
                    tieneDeSuNivel = true
                }
            }
        }

        if (!tieneDeSuNivel && nivelMasAlto != usuario.nivel) {
            db.usuarioDao().updateUser(usuario.copy(nivel = nivelMasAlto))
            return nivelMasAlto
        }
        return -1
    }
}