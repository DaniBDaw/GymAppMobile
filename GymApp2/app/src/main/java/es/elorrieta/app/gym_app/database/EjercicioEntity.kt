package es.elorrieta.app.gym_app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_workout"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
data class EjercicioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "id_workout", index = true)
    val idWorkout: Long,
    val nombre: String,
    val desc: String,
    val orden: Int,
    val isCompletado: Boolean,
)
