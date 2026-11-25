package es.elorrieta.app.gym_app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        WorkoutEntity::class,
        EjercicioEntity::class,
        SerieEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun ejercicioDao(): EjercicioDao
    abstract fun serieDao(): SerieDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_app" // Nombre del archivo físico en el móvil
                )
                    // Opcional: borra y recrea la BD si cambias la versión sin hacer migración (útil mientras desarrollas)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}