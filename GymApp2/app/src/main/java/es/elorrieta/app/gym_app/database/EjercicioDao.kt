package es.elorrieta.app.gym_app.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EjercicioDao {

    @Query("SELECT * FROM t_ejercicios ORDER BY id ASC")
    fun getAllEjercicios(): LiveData<List<EjercicioEntity>>

    @Query("SELECT * FROM t_ejercicios ORDER BY id ASC")
    suspend fun getAllEjerciciosSync(): List<EjercicioEntity>

    @Query("SELECT * FROM t_ejercicios WHERE id = :id")
    suspend fun getEjercicioById(id: Long): EjercicioEntity?

    @Query("SELECT * FROM t_ejercicios WHERE id_workout = :idWorkout")
    suspend fun getEjerciciosByWorkout(idWorkout: Long): List<EjercicioEntity>

    @Query("DELETE FROM t_ejercicios WHERE id = :idEjercicio AND id_workout = :idWorkout")
    suspend fun deleteEjercicioByWorkout(idEjercicio: Long, idWorkout: Long)

    @Insert
    suspend fun insertEjercicio(ejercicio: EjercicioEntity): Long

    @Update
    suspend fun updateEjercicio(ejercicio: EjercicioEntity): Int

    @Delete
    suspend fun deleteEjercicio(ejercicio: EjercicioEntity): Int

    // Métodos alias para compatibilidad
    @Query("SELECT * FROM t_ejercicios WHERE id_workout = :idWorkout")
    suspend fun getWorkoutUser(idWorkout: Long): List<EjercicioEntity> = getEjerciciosByWorkout(idWorkout)

    @Query("DELETE FROM t_ejercicios WHERE id = :idEjercicio AND id_workout = :idWorkout")
    suspend fun deleteWorkoutByUser(idEjercicio: Long, idWorkout: Long) = deleteEjercicioByWorkout(idEjercicio, idWorkout)
}