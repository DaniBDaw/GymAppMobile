package es.elorrieta.app.gym_app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EjercicioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_ejercicio"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
data class SerieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "id_ejercicio", index = true)
    val idEjercicio: Long,
    val nombre: String,
    val repeticiones: Int,
    val tiempo: Int,
    val imagen: String
)
