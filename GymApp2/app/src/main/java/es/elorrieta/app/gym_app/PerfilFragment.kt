package es.elorrieta.app.gym_app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import es.elorrieta.app.gym_app.database.AppDatabase
import es.elorrieta.app.gym_app.database.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class PerfilFragment : Fragment() {

    // Usuario que estamos viendo / editando
    private var usuarioActual: UsuarioEntity? = null

    // Nombre y color de cada nivel (0 Novato ... 4 Experto)
    private val nombresNivel = arrayOf(
        R.string.nivel_novato,
        R.string.nivel_principiante,
        R.string.nivel_intermedio,
        R.string.nivel_avanzado,
        R.string.nivel_experto
    )
    private val coloresNivel = arrayOf(
        R.color.nivel_novato,
        R.color.nivel_principiante,
        R.color.nivel_intermedio,
        R.color.nivel_avanzado,
        R.color.nivel_experto
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Cargamos el layout del fragment
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Flecha para volver a la pantalla anterior
        val btnVolver = view.findViewById<ImageView>(R.id.btnVolver)
        btnVolver.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                MenuInferior.mostrar(requireActivity() as AppCompatActivity, MenuInferior.pantallaAnterior)
            }
        })

        configurarTema(view)
        configurarIdioma(view)
        configurarFecha(view)
        configurarGuardar(view)
        cargarUsuario(view)

        return view
    }

    // ---------------- CARGAR DATOS ----------------

    private fun cargarUsuario(view: View) {
        val contexto = requireContext()
        val dao = AppDatabase.getDatabase(contexto).usuarioDao()

        // Las consultas a ROOM se hacen en segundo plano (corrutina)
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            var usuario: UsuarioEntity? = null

            // 1. Buscamos el usuario logueado
            val idUsuario = Ajustes.leerIdUsuario(contexto)
            if (idUsuario != -1L) {
                usuario = dao.getUserById(idUsuario)
            }

            // 2. SOLO PARA PRUEBAS: si no hay login, cogemos el primero de la BD
            if (usuario == null) {
                val usuarios = dao.getAllUsersSync()
                if (usuarios.isNotEmpty()) {
                    usuario = usuarios[0]
                }
            }

            // 3. SOLO PARA PRUEBAS: si la BD está vacía, creamos uno
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

            val usuarioEncontrado = usuario

            // Volvemos al hilo principal para tocar la pantalla
            launch(Dispatchers.Main) {
                if (usuarioEncontrado == null) {
                    mostrarMensaje(R.string.error_bd)
                } else {
                    usuarioActual = usuarioEncontrado
                    mostrarDatos(view, usuarioEncontrado)
                }
            }
        }
    }

    private fun mostrarDatos(view: View, usuario: UsuarioEntity) {
        view.findViewById<EditText>(R.id.etNombre).setText(usuario.nombre)
        view.findViewById<EditText>(R.id.etApellidos).setText(usuario.apellidos)
        view.findViewById<EditText>(R.id.etEmail).setText(usuario.mail)
        view.findViewById<EditText>(R.id.etFecha).setText(usuario.fechaNac)
        mostrarNivel(view, usuario.nivel)
    }

    private fun mostrarNivel(view: View, nivel: Int) {
        // Por si viene un número raro, lo dejamos entre 0 y 4
        var posicion = nivel
        if (posicion < 0) {
            posicion = 0
        }
        if (posicion > 4) {
            posicion = 4
        }

        val color = ContextCompat.getColor(requireContext(), coloresNivel[posicion])

        val txtNivel = view.findViewById<TextView>(R.id.txtNivel)
        txtNivel.setText(nombresNivel[posicion])
        txtNivel.setTextColor(color)

        val imgNivel = view.findViewById<ImageView>(R.id.imgNivel)
        imgNivel.setColorFilter(color)
    }

    // ---------------- FECHA (calendario) ----------------

    private fun configurarFecha(view: View) {
        val etFecha = view.findViewById<EditText>(R.id.etFecha)
        etFecha.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val calendario = Calendar.getInstance()
                val dialogo = DatePickerDialog(
                    requireContext(),
                    object : DatePickerDialog.OnDateSetListener {
                        override fun onDateSet(picker: DatePicker, anio: Int, mes: Int, dia: Int) {
                            // Los meses empiezan en 0, por eso sumamos 1
                            val fecha = String.format("%02d/%02d/%04d", dia, mes + 1, anio)
                            etFecha.setText(fecha)
                        }
                    },
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)
                )
                dialogo.show()
            }
        })
    }

    // ---------------- GUARDAR CAMBIOS ----------------

    private fun configurarGuardar(view: View) {
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)
        btnGuardar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                guardarCambios(view)
            }
        })
    }

    private fun guardarCambios(view: View) {
        val usuario = usuarioActual
        if (usuario == null) {
            mostrarMensaje(R.string.error_bd)
            return
        }

        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etApellidos = view.findViewById<EditText>(R.id.etApellidos)
        val etFecha = view.findViewById<EditText>(R.id.etFecha)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etRepetirPassword = view.findViewById<EditText>(R.id.etRepetirPassword)

        val nombre = etNombre.text.toString().trim()
        val apellidos = etApellidos.text.toString().trim()
        val fecha = etFecha.text.toString().trim()
        val password = etPassword.text.toString()
        val repetirPassword = etRepetirPassword.text.toString()

        // Comprobación: campos obligatorios
        if (nombre.isEmpty() || apellidos.isEmpty() || fecha.isEmpty()) {
            mostrarMensaje(R.string.error_campos_vacios)
            return
        }

        // Comprobación: doble contraseña (solo si quiere cambiarla)
        var contra = usuario.contra
        if (password.isNotEmpty() || repetirPassword.isNotEmpty()) {
            if (password != repetirPassword) {
                mostrarMensaje(R.string.error_password_no_coincide)
                return
            }
            contra = password
        }

        // copy() crea una copia del usuario cambiando solo estos campos
        val usuarioModificado = usuario.copy(
            nombre = nombre,
            apellidos = apellidos,
            fechaNac = fecha,
            contra = contra
        )

        val dao = AppDatabase.getDatabase(requireContext()).usuarioDao()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val filas = dao.updateUser(usuarioModificado)

            launch(Dispatchers.Main) {
                if (filas > 0) {
                    usuarioActual = usuarioModificado
                    etPassword.setText("")
                    etRepetirPassword.setText("")
                    mostrarMensaje(R.string.perfil_guardado)
                } else {
                    mostrarMensaje(R.string.error_bd)
                }
            }
        }
    }

    private fun mostrarMensaje(idTexto: Int) {
        Toast.makeText(requireContext(), getString(idTexto), Toast.LENGTH_SHORT).show()
    }

    // ---------------- TEMA ----------------

    private fun configurarTema(view: View) {
        val btnTemaClaro = view.findViewById<TextView>(R.id.btnTemaClaro)
        val btnTemaOscuro = view.findViewById<TextView>(R.id.btnTemaOscuro)
        val opcionesTema = arrayOf(btnTemaClaro, btnTemaOscuro)

        // Marcamos el tema que está guardado
        if (Ajustes.leerTema(requireContext()) == Ajustes.TEMA_OSCURO) {
            marcarOpcion(btnTemaOscuro, opcionesTema)
        } else {
            marcarOpcion(btnTemaClaro, opcionesTema)
        }

        btnTemaClaro.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Ajustes.guardarTema(requireContext(), Ajustes.TEMA_CLARO)
                Ajustes.aplicarTema(Ajustes.TEMA_CLARO)
            }
        })

        btnTemaOscuro.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Ajustes.guardarTema(requireContext(), Ajustes.TEMA_OSCURO)
                Ajustes.aplicarTema(Ajustes.TEMA_OSCURO)
            }
        })
    }

    // ---------------- IDIOMA ----------------

    private fun configurarIdioma(view: View) {
        val btnIdiomaEs = view.findViewById<TextView>(R.id.btnIdiomaEs)
        val btnIdiomaEn = view.findViewById<TextView>(R.id.btnIdiomaEn)
        val btnIdiomaKa = view.findViewById<TextView>(R.id.btnIdiomaKa)
        val opcionesIdioma = arrayOf(btnIdiomaEs, btnIdiomaEn, btnIdiomaKa)

        // Marcamos el idioma que está guardado
        val idiomaGuardado = Ajustes.leerIdioma(requireContext())
        if (idiomaGuardado == Ajustes.IDIOMA_EN) {
            marcarOpcion(btnIdiomaEn, opcionesIdioma)
        } else if (idiomaGuardado == Ajustes.IDIOMA_KA) {
            marcarOpcion(btnIdiomaKa, opcionesIdioma)
        } else {
            marcarOpcion(btnIdiomaEs, opcionesIdioma)
        }

        btnIdiomaEs.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                cambiarIdioma(Ajustes.IDIOMA_ES)
            }
        })

        btnIdiomaEn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                cambiarIdioma(Ajustes.IDIOMA_EN)
            }
        })

        btnIdiomaKa.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                cambiarIdioma(Ajustes.IDIOMA_KA)
            }
        })
    }

    private fun cambiarIdioma(idioma: String) {
        Ajustes.guardarIdioma(requireContext(), idioma)
        Ajustes.aplicarIdioma(idioma)
    }

    // Pone en negro la opción elegida y deja las demás sin fondo
    private fun marcarOpcion(seleccionada: TextView, opciones: Array<TextView>) {
        for (opcion in opciones) {
            if (opcion == seleccionada) {
                opcion.setBackgroundResource(R.drawable.fondo_opcion_activa)
                opcion.setTextColor(ContextCompat.getColor(requireContext(), R.color.boton_principal_texto))
            } else {
                opcion.background = null
                opcion.setTextColor(ContextCompat.getColor(requireContext(), R.color.texto_principal))
            }
        }
    }
}