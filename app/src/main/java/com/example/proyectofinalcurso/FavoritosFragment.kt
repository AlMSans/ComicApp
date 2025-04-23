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
import com.google.firebase.firestore.*

class FavoritosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var comicsAdapter: ListUsuComics
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var lastDocumentSnapshot: DocumentSnapshot? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)

        auth = FirebaseAuth.getInstance()

        recyclerView = view.findViewById(R.id.recyclerViewFavoritos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Aquí se pasa el adaptador de cómics y la función de clic para los cómics
        comicsAdapter = ListUsuComics(emptyList()) { comic ->
            abrirDetallesComic(comic)
        }

        recyclerView.adapter = comicsAdapter

        loadFavoriteComics()

        return view
    }

    private fun loadFavoriteComics() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e("FavoritosFragment", "Usuario no autenticado")
            return
        }

        var query = db.collection("usuarios")
            .document(userId)
            .collection("favorites")
            .limit(10)

        query.get()
            .addOnSuccessListener { documents ->
                val favoritosList = mutableListOf<Comic>()

                for (document in documents) {
                    val comicId = document.getString("comicId") ?: continue
                    db.collection("comics").document(comicId).get()
                        .addOnSuccessListener { comicDoc ->
                            val comic = Comic(
                                id = comicDoc.id,
                                title = comicDoc.getString("title") ?: "Sin título",
                                author = comicDoc.getString("author") ?: "Desconocido",
                                imageUrl = comicDoc.getString("imageUrl") ?: "",
                                location = comicDoc.getString("location") ?: "Desconocida",
                                condition = comicDoc.getString("condition") ?: "Desconocido",
                                price = (comicDoc.get("price") as? Number)?.toFloat() ?: 0f,
                                genre = comicDoc.getString("genre") ?: "Desconocido",
                                userId = comicDoc.getString("userId") ?: ""
                            )
                            favoritosList.add(comic)

                            // Cuando ya se han cargado todos los cómics, actualizamos el adaptador
                            comicsAdapter.updateData(favoritosList)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FavoritosFragment", "Error al cargar favoritos: ${exception.message}")
            }
    }

    private fun abrirDetallesComic(comic: Comic) {
        val fragment = ComicDetailFragment.newInstance(
            id = comic.id,
            title = comic.title,
            author = comic.author,
            genre = comic.genre,
            location = comic.location,
            condition = comic.condition,
            price = comic.price,
            imageUrl = comic.imageUrl ?: "",
            userId = comic.userId
        )

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
