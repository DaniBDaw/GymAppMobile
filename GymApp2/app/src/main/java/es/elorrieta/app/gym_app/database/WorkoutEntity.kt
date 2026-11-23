package es.elorrieta.app.gym_app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import java.net.URL

@Entity(
    tableName = "t_workouts",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_usuario"],
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ]
)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "id_usuario", index = true)
    val idUsuario: Long,
    val nombre: String,
    val desc: String,
    val nivel: Int,
    val isCompletado : Boolean,
    val video: String,
)
