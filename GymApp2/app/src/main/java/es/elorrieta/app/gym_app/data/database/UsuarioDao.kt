package es.elorrieta.app.gym_app.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM t_usuarios ORDER BY id ASC")
    fun getAllUsers(): LiveData<List<UsuarioEntity>>

    @Query("SELECT * FROM t_usuarios ORDER BY id ASC")
    suspend fun getAllUsersSync(): List<UsuarioEntity>

    @Query("SELECT * FROM t_usuarios WHERE id = :id")
    suspend fun getUserById(id: Long): UsuarioEntity?

    @Query("SELECT * FROM t_usuarios WHERE mail = :mail")
    suspend fun getUserByMail(mail: String): UsuarioEntity?

    @Insert
    suspend fun insertUser(usuario: UsuarioEntity): Long

    @Update
    suspend fun updateUser(usuario: UsuarioEntity): Int

    @Delete
    suspend fun deleteUser(usuario: UsuarioEntity): Int

}