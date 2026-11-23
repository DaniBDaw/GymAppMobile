package es.elorrieta.app.gym_app.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM t_workouts ORDER BY id ASC")
    fun getAllWorkouts(): LiveData<List<WorkoutEntity>>

    @Query("SELECT * FROM t_workouts ORDER BY id ASC")
    suspend fun getAllWorkoutsSync(): List<WorkoutEntity>

    @Query("SELECT * FROM t_workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Query("SELECT * FROM t_workouts WHERE id_usuario = :idUsuario")
    suspend fun getWorkoutUser(idUsuario: Long): List<WorkoutEntity>

    @Query("DELETE FROM t_workouts WHERE id = :idWorkout AND id_usuario = :idUsuario")
    suspend fun deleteWorkoutByUser(idWorkout: Long, idUsuario: Long)

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity): Int

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity): Int
}