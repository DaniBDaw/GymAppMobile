package es.elorrieta.app.gym_app.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SerieDao {

    @Query("SELECT * FROM t_series ORDER BY id ASC")
    fun getAllSeries(): LiveData<List<SerieEntity>>

    @Query("SELECT * FROM t_series ORDER BY id ASC")
    suspend fun getAllSeriesSync(): List<SerieEntity>

    @Query("SELECT * FROM t_series WHERE id = :id")
    suspend fun getSerieById(id: Long): SerieEntity?

    @Query("SELECT * FROM t_series WHERE id_ejercicio = :idEjercicio")
    suspend fun getSeriesByEjercicio(idEjercicio: Long): List<SerieEntity>

    @Query("DELETE FROM t_series WHERE id = :idSerie AND id_ejercicio = :idEjercicio")
    suspend fun deleteSerieByEjercicio(idSerie: Long, idEjercicio: Long)

    @Insert
    suspend fun insertSerie(serie: SerieEntity): Long

    @Update
    suspend fun updateSerie(serie: SerieEntity): Int

    @Delete
    suspend fun deleteSerie(serie: SerieEntity): Int

    // Métodos alias para compatibilidad
    @Query("SELECT * FROM t_series WHERE id_ejercicio = :idEjercicio")
    suspend fun getWorkoutUser(idEjercicio: Long): List<SerieEntity> = getSeriesByEjercicio(idEjercicio)

    @Query("DELETE FROM t_series WHERE id = :idSerie AND id_ejercicio = :idEjercicio")
    suspend fun deleteWorkoutByUser(idSerie: Long, idEjercicio: Long) = deleteSerieByEjercicio(idSerie, idEjercicio)
}