package es.elorrieta.app.gym_app.database

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutsDeUsuario(
    @Embedded val usuario: UsuarioEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "idUsuario"
    )
    val workouts: List<WorkoutEntity>
)

data class EjerciciosDeWorkout(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "idWorkout"
    )
    val ejercicios: List<EjercicioEntity>
)

data class SeriesDeEjercicios(
    @Embedded val Ejercicio: EjercicioEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "idEjercicio"
    )
    val series: List<SerieEntity>
)