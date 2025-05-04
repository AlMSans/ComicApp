package com.example.proyectofinalcurso

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SDisponiblesFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewSubastas: RecyclerView
    private lateinit var subastaAdapter: SubastaDisponiblesAdapter
    private var subastasList: MutableList<Subasta> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_s_disponibles, container, false)

        db = FirebaseFirestore.getInstance()
        recyclerViewSubastas = binding.findViewById(R.id.recyclerViewSubastasDisponibles)
        recyclerViewSubastas.layoutManager = LinearLayoutManager(context)

        // Se pasa el método para pujar por una subasta
        subastaAdapter = SubastaDisponiblesAdapter(
            subastasList,
            onPujarClick = { subasta ->
                pujarPorSubasta(subasta)
            },
            onImageClick = { subasta ->
                abrirDetallesComicDesdeSubasta(subasta)
            }
        )

        recyclerViewSubastas.adapter = subastaAdapter

        loadSubastas()

        return binding
    }

    private fun loadSubastas() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("subastas")
            .whereGreaterThan("precioInicial", 0) // Filtrar solo subastas activas
            .get()
            .addOnSuccessListener { result ->
                subastasList.clear()
                for (document in result) {
                    val subasta = document.toObject(Subasta::class.java)
                    // Solo agregar si no es del usuario actual
                    if (subasta.propietarioId != currentUserId) {
                        subastasList.add(subasta)
                    }
                }
                subastaAdapter.updateData(subastasList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar las subastas", Toast.LENGTH_SHORT).show()
            }
    }


    private fun pujarPorSubasta(subasta: Subasta) {
        // Lógica para pujar por una subasta
        val dialog = PujarDialogFragment(subasta) { updatedSubasta ->
            // Aquí puedes manejar lo que sucede después de que la puja se realice
            // Como actualizar la oferta mostrada o realizar alguna otra acción
            actualizarSubastaEnVista(updatedSubasta)
        }
        dialog.show(childFragmentManager, "PujarDialog")
    }

    // Función para actualizar la subasta en la vista después de la puja
    private fun actualizarSubastaEnVista(subasta: Subasta) {
        // Aquí puedes actualizar el RecyclerView o cualquier vista que muestre la subasta
        // Por ejemplo, actualizar la lista de subastas con el nuevo precio
        val index = subastasList.indexOfFirst { it.id == subasta.id }
        if (index != -1) {
            subastasList[index] = subasta
            subastaAdapter.notifyItemChanged(index)
        }


    }

    private fun abrirDetallesComicDesdeSubasta(subasta: Subasta) {
        val comicId = subasta.comicId
        if (comicId.isBlank()) {
            Toast.makeText(context, "No se pudo abrir el cómic, ID faltante.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("comics").document(comicId).get()
            .addOnSuccessListener { document ->
                val comic = document.toObject(Comic::class.java)
                if (comic != null) {
                    val urls = ArrayList(comic.imageUrls)
                    if (urls.isEmpty() && comic.imageUrl != null) {
                        urls.add(comic.imageUrl)
                    }

                    val fragment = ComicDetailFragment.newInstance(
                        id        = comic.id,
                        title     = comic.title,
                        author    = comic.author,
                        genre     = comic.genre,
                        location  = comic.location,
                        condition = comic.condition,
                        price     = comic.price,
                        imageUrls = urls,
                        userId    = comic.userId
                    )

                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(context, "No se encontró el cómic", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar el cómic", Toast.LENGTH_SHORT).show()
            }
    }



}
