// =====================================================================
// DatosIniciales
// Catálogo de workouts del gimnasio. Como en la BD cada workout es de un
// usuario (id_usuario + isCompletado), la primera vez que un usuario entra
// sin workouts se le crea su propia copia del catálogo: workouts + ejercicios
// + series. Hay al menos uno por nivel (0 Novato ... 4 Experto).
// =====================================================================
package es.elorrieta.app.gym_app.util

import android.content.Context
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.EjercicioEntity
import es.elorrieta.app.gym_app.database.SerieEntity
import es.elorrieta.app.gym_app.database.WorkoutEntity

object DatosIniciales {

    // Crea el catálogo para ese usuario, solo si todavía no tiene workouts
    suspend fun crearCatalogoSiHaceFalta(context: Context, idUsuario: Long) {
        val db = AppDatabase.getDatabase(context)
        if (db.workoutDao().getWorkoutUser(idUsuario).isNotEmpty()) {
            return
        }

        // ---------- NIVEL 0: NOVATO ----------
        var id = crearWorkout(db, idUsuario, "Iniciación full body", "Rutina suave para empezar", 0,
            "https://www.youtube.com/results?search_query=rutina+full+body+principiantes")
        crearEjercicio(db, id, "Sentadilla con silla", "Siéntate y levántate de una silla sin usar las manos.", 1, 3, 10, 30)
        crearEjercicio(db, id, "Flexiones en pared", "Apoya las manos en la pared y acerca el pecho.", 2, 3, 10, 30)
        crearEjercicio(db, id, "Puente de glúteos", "Tumbado boca arriba, sube la cadera apretando glúteos.", 3, 3, 12, 30)
        crearEjercicio(db, id, "Plancha", "Apoyado en antebrazos, mantén el cuerpo recto.", 4, 3, 1, 20)

        id = crearWorkout(db, idUsuario, "Kardio suave", "Cardio de baja intensidad", 0, "")
        crearEjercicio(db, id, "Marcha en el sitio", "Camina sin moverte subiendo las rodillas.", 1, 3, 30, 60)
        crearEjercicio(db, id, "Jumping jacks suaves", "Abre y cierra brazos y piernas sin saltar fuerte.", 2, 3, 15, 40)
        crearEjercicio(db, id, "Rodillas al pecho", "De pie, sube una rodilla al pecho y alterna.", 3, 3, 15, 40)

        // ---------- NIVEL 1: PRINCIPIANTE ----------
        id = crearWorkout(db, idUsuario, "Piernas en casa", "Piernas sin material", 1, "")
        crearEjercicio(db, id, "Sentadillas", "Baja como si te sentaras en una silla y sube.", 1, 3, 15, 40)
        crearEjercicio(db, id, "Zancadas", "Da un paso largo y baja la rodilla de atrás casi al suelo.", 2, 3, 12, 40)
        crearEjercicio(db, id, "Puente de glúteos", "Tumbado boca arriba, sube la cadera apretando glúteos.", 3, 3, 15, 30)
        crearEjercicio(db, id, "Elevación de talones", "De pie, sube de puntillas y baja despacio.", 4, 3, 20, 30)

        id = crearWorkout(db, idUsuario, "Pilates core", "Abdomen y zona media", 1,
            "https://www.youtube.com/results?search_query=pilates+core")
        crearEjercicio(db, id, "Plancha", "Apoyado en antebrazos, mantén el cuerpo recto.", 1, 3, 1, 30)
        crearEjercicio(db, id, "Crunch", "Tumbado, sube los hombros del suelo contrayendo el abdomen.", 2, 3, 15, 40)
        crearEjercicio(db, id, "Elevación de piernas", "Tumbado, sube las piernas rectas y bájalas despacio.", 3, 3, 12, 40)

        // ---------- NIVEL 2: INTERMEDIO ----------
        id = crearWorkout(db, idUsuario, "Kardio HIIT 20", "Intervalos de alta intensidad", 2,
            "https://www.youtube.com/results?search_query=hiit+20+minutos")
        crearEjercicio(db, id, "Burpees", "Agáchate, salta atrás a plancha, vuelve y salta arriba.", 1, 4, 10, 40)
        crearEjercicio(db, id, "Mountain climbers", "En plancha, lleva las rodillas al pecho rápido.", 2, 4, 20, 40)
        crearEjercicio(db, id, "Saltos de tijera", "Salta abriendo y cerrando piernas y brazos.", 3, 4, 20, 40)
        crearEjercicio(db, id, "Sprint en el sitio", "Corre sin moverte lo más rápido que puedas.", 4, 4, 1, 30)

        // ---------- NIVEL 3: AVANZADO ----------
        id = crearWorkout(db, idUsuario, "Full Upper Body", "Pecho, espalda y hombros", 3,
            "https://www.youtube.com/results?search_query=full+upper+body+workout")
        crearEjercicio(db, id, "Press banca", "Tumbado en el banco, baja la barra al pecho y empuja hacia arriba.", 1, 3, 10, 45)
        crearEjercicio(db, id, "Remo con barra", "Inclinado hacia delante, tira de la barra hacia el abdomen.", 2, 3, 10, 45)
        crearEjercicio(db, id, "Press militar", "De pie, empuja la barra desde los hombros hasta estirar los brazos.", 3, 3, 8, 40)
        crearEjercicio(db, id, "Dominadas", "Colgado de la barra, sube hasta que la barbilla pase la barra.", 4, 4, 6, 40)
        crearEjercicio(db, id, "Fondos", "En paralelas, baja doblando los codos y sube.", 5, 3, 10, 40)

        // ---------- NIVEL 4: EXPERTO ----------
        id = crearWorkout(db, idUsuario, "Fuerza máxima", "Básicos con mucho peso", 4,
            "https://www.youtube.com/results?search_query=rutina+fuerza+maxima")
        crearEjercicio(db, id, "Peso muerto", "Levanta la barra del suelo con la espalda recta.", 1, 5, 5, 60)
        crearEjercicio(db, id, "Sentadilla con barra", "Barra en la espalda, baja hasta paralelo y sube.", 2, 5, 5, 60)
        crearEjercicio(db, id, "Press banca pesado", "Press banca con mucho peso y pocas repeticiones.", 3, 5, 5, 60)
        crearEjercicio(db, id, "Dominadas lastradas", "Dominadas con peso extra colgado.", 4, 4, 6, 50)
    }

    // Crea un workout para el usuario y devuelve su id
    private suspend fun crearWorkout(
        db: AppDatabase, idUsuario: Long, nombre: String, desc: String, nivel: Int, video: String
    ): Long {
        return db.workoutDao().insertWorkout(
            WorkoutEntity(
                idUsuario = idUsuario,
                nombre = nombre,
                desc = desc,
                nivel = nivel,
                isCompletado = false,
                video = video
            )
        )
    }

    // Crea un ejercicio con sus series (cada serie: repeticiones y segundos)
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