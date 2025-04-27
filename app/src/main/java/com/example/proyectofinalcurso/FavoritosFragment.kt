package com.example.proyectofinalcurso

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var comicAdapter: ComicAdapter
    private val comicsList = mutableListOf<Comic>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewFavoritos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        comicAdapter = ComicAdapter(comicsList, isFavorites = true) { comic ->
            abrirDetallesComic(comic)
        }
        recyclerView.adapter = comicAdapter

        cargarFavoritos()

        return view
    }

    private fun cargarFavoritos() {
        val user = auth.currentUser
        user?.let {
            db.collection("usuarios")
                .document(it.uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    comicsList.clear()
                    var count = 0
                    for (document in querySnapshot) {
                        val comicId = document.getString("comicId")
                        comicId?.let { id ->
                            db.collection("comics").document(id)
                                .get()
                                .addOnSuccessListener { comicDoc ->
                                    comicDoc.toObject(Comic::class.java)?.let { comic ->
                                        comic.id = comicDoc.id
                                        comicsList.add(comic)
                                    }
                                    count++
                                    // Solo actualizar la vista después de que todos los cómics se hayan cargado
                                    if (count == querySnapshot.size()) {
                                        comicAdapter.notifyDataSetChanged()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("FavoritosFragment", "Error al cargar cómic: ${exception.message}")
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FavoritosFragment", "Error al cargar favoritos: ${exception.message}")
                }
        }
    }

    private fun abrirDetallesComic(comic: Comic) {
        val urls = ArrayList(comic.imageUrls)
        if (urls.isEmpty() && comic.imageUrl != null)
            urls.add(comic.imageUrl)

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
    }
}
