package es.elorrieta.app.gym_app

import android.content.Context
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.EjercicioEntity
import es.elorrieta.app.gym_app.database.SerieEntity
import es.elorrieta.app.gym_app.database.UsuarioEntity
import es.elorrieta.app.gym_app.database.WorkoutEntity

// CUIDAOOOOOOOOOOOOOOOOOO
// SOLO PARA PRUEBAS: datos de ejemplo mientras no haya login ni carga de datos real.
// "suspend" = la función hace consultas a ROOM, así que solo se llama dentro de una corrutina.
object TempDatosPrueba {

    // Usuario logueado; si no hay, el primero de la BD; si la BD está vacía, crea a Carlos
    suspend fun obtenerUsuario(context: Context): UsuarioEntity? {
        val dao = AppDatabase.getDatabase(context).usuarioDao()
        var usuario: UsuarioEntity? = null

        val idUsuario = Ajustes.leerIdUsuario(context)
        if (idUsuario != -1L) {
            usuario = dao.getUserById(idUsuario)
        }

        if (usuario == null) {
            val usuarios = dao.getAllUsersSync()
            if (usuarios.isNotEmpty()) {
                usuario = usuarios[0]
            }
        }

        if (usuario == null) {
            val usuarioPrueba = UsuarioEntity(
                nombre = "Carlos",
                apellidos = "Martínez Ruiz",
                mail = "carlos@gymapp.com",
                fechaNac = "12/03/1999",
                contra = "1234",
                nivel = 3,
                isEntrenador = false
            )
            val idNuevo = dao.insertUser(usuarioPrueba)
            usuario = dao.getUserById(idNuevo)
        }

        return usuario
    }

    // Crea 2 workouts de ejemplo para ese usuario
    suspend fun crearWorkouts(context: Context, idUsuario: Long) {
        val db = AppDatabase.getDatabase(context)

        val idUpper = db.workoutDao().insertWorkout(
            WorkoutEntity(
                idUsuario = idUsuario,
                nombre = "Full Upper Body",
                desc = "Pecho, espalda y hombros",
                nivel = 3,
                isCompletado = false,
                video = "https://www.youtube.com/results?search_query=full+upper+body+workout"
            )
        )
        crearEjercicio(db, idUpper, "Press banca", "Tumbado en el banco, baja la barra al pecho y empuja hacia arriba.", 1, 3, 10, 45)
        crearEjercicio(db, idUpper, "Remo con barra", "Inclinado hacia delante, tira de la barra hacia el abdomen.", 2, 3, 10, 45)
        crearEjercicio(db, idUpper, "Press militar", "De pie, empuja la barra desde los hombros hasta estirar los brazos.", 3, 3, 8, 40)
        crearEjercicio(db, idUpper, "Dominadas", "Colgado de la barra, sube hasta que la barbilla pase la barra.", 4, 4, 6, 40)
        crearEjercicio(db, idUpper, "Fondos", "En paralelas, baja doblando los codos y sube.", 5, 3, 10, 40)

        val idPiernas = db.workoutDao().insertWorkout(
            WorkoutEntity(
                idUsuario = idUsuario,
                nombre = "Piernas en casa",
                desc = "Piernas sin material",
                nivel = 1,
                isCompletado = false,
                video = ""
            )
        )
        crearEjercicio(db, idPiernas, "Sentadillas", "Baja como si te sentaras en una silla y sube.", 1, 3, 15, 40)
        crearEjercicio(db, idPiernas, "Zancadas", "Da un paso largo y baja la rodilla de atrás casi al suelo.", 2, 3, 12, 40)
        crearEjercicio(db, idPiernas, "Puente de glúteos", "Tumbado boca arriba, sube la cadera apretando glúteos.", 3, 3, 15, 30)
        crearEjercicio(db, idPiernas, "Elevación de talones", "De pie, sube de puntillas y baja despacio.", 4, 3, 20, 30)
    }

    // Crea un ejercicio con sus series
    private suspend fun crearEjercicio(
        db: AppDatabase, idWorkout: Long, nombre: String, desc: String,
        orden: Int, numSeries: Int, repeticiones: Int, segundos: Int
    ) {
        val idEjercicio = db.ejercicioDao().insertEjercicio(
            EjercicioEntity(
                idWorkout = idWorkout,
                nombre = nombre,
                desc = desc,
                orden = orden,
                isCompletado = false
            )
        )
        for (i in 1..numSeries) {
            db.serieDao().insertSerie(
                SerieEntity(
                    idEjercicio = idEjercicio,
                    nombre = "Serie " + i,
                    repeticiones = repeticiones,
                    tiempo = segundos,
                    imagen = ""
                )
            )
        }
    }
}