package com.example.proyectofinalcurso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalcurso.subastas.MisSubastasFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddSubastaFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etTituloSubasta: EditText
    private lateinit var etDescripcionSubasta: EditText
    private lateinit var etPrecioSubasta: EditText
    private lateinit var recyclerViewComicsUsuario: RecyclerView
    private lateinit var subastaAdapter: SubastaAdapter
    private var comicsList: MutableList<Comic> = mutableListOf()
    private var comicSeleccionado: Comic? = null  // Cómic que se va a subastar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_add_subasta, container, false)

        etTituloSubasta = binding.findViewById(R.id.etTituloSubasta)
        etDescripcionSubasta = binding.findViewById(R.id.etDescripcionSubasta)
        etPrecioSubasta = binding.findViewById(R.id.etPrecioSubasta)
        recyclerViewComicsUsuario = binding.findViewById(R.id.recyclerViewComicsUsuario)

        db = FirebaseFirestore.getInstance()

        recyclerViewComicsUsuario.layoutManager = LinearLayoutManager(context)
        subastaAdapter = SubastaAdapter(comicsList) { comic ->
            comicSeleccionado = comic
            Toast.makeText(context, "Cómic seleccionado: ${comic.title}", Toast.LENGTH_SHORT).show()
        }
        recyclerViewComicsUsuario.adapter = subastaAdapter

        loadComics()

        binding.findViewById<View>(R.id.btnGuardarSubasta).setOnClickListener {
            saveSubasta()
        }

        return binding
    }

    private fun loadComics() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("comics")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    comicsList.clear()
                    if (result.isEmpty) {
                        Toast.makeText(context, "No tienes cómics disponibles para subastar", Toast.LENGTH_SHORT).show()
                    } else {
                        for (document in result) {
                            val comic = document.toObject(Comic::class.java)
                            comicsList.add(comic)
                        }
                    }
                    subastaAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al cargar los cómics", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSubasta() {
        val titulo = etTituloSubasta.text.toString().trim()
        val descripcion = etDescripcionSubasta.text.toString().trim()
        val precio = etPrecioSubasta.text.toString().trim()

        if (titulo.isEmpty() || descripcion.isEmpty() || precio.isEmpty()) {
            Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (comicSeleccionado == null) {
            Toast.makeText(context, "Seleccione un cómic para subastar", Toast.LENGTH_SHORT).show()
            return
        }

        val precioInicial = precio.toDoubleOrNull() ?: 0.0
        val imagenUrl = comicSeleccionado?.imageUrls?.firstOrNull() ?: comicSeleccionado?.imageUrl ?: "url_imagen_predeterminada"
        val comicId = comicSeleccionado?.id

        if (comicId.isNullOrEmpty()) {
            Toast.makeText(context, "Error: el cómic no tiene ID", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val propietarioId = currentUser?.uid ?: ""
        val subastaId = db.collection("subastas").document().id

        // Verificar si ya existe una subasta activa con este cómic
        db.collection("subastas")
            .whereEqualTo("comicId", comicId)
            .whereEqualTo("cerrada", false)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    Toast.makeText(context, "Este cómic ya está en una subasta activa", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val subasta = Subasta(
                    id = subastaId,
                    comicId = comicId,
                    titulo = titulo,
                    descripcion = descripcion,
                    imagenUrl = imagenUrl,
                    precioInicial = precioInicial,
                    mejorOferta = 0.0,
                    mejorPostor = "",
                    nombrePostor = "",
                    propietarioId = propietarioId,
                    cerrada = false
                )

                db.collection("subastas").document(subastaId)
                    .set(subasta)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Subasta guardada correctamente", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, MisSubastasFragment())
                            .addToBackStack(null)
                            .commit()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Error al guardar la subasta: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al verificar si el cómic ya está en subasta: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
