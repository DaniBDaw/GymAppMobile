package es.elorrieta.app.gym_app.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "t_usuarios",
    indices = [Index(value = ["mail"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val apellidos: String,
    val mail: String,
    val fechaNac: String,
    val contra: String,
    val nivel: Int,
    val isEntrenador: Boolean
)