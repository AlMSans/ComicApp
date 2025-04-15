package com.example.proyectofinalcurso

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisComicsFragment : Fragment() {

    private lateinit var recyclerViewMisComics: RecyclerView
    private lateinit var recyclerViewFavoritos: RecyclerView
    private lateinit var comicsAdapter: ComicAdapter
    private lateinit var favoritosAdapter: ComicAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mis_comics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        recyclerViewMisComics = view.findViewById(R.id.recyclerViewMisComics)
        recyclerViewFavoritos = view.findViewById(R.id.recyclerViewFavoritos)

        recyclerViewMisComics.layoutManager = LinearLayoutManager(context)
        recyclerViewFavoritos.layoutManager = LinearLayoutManager(context)

        comicsAdapter = ComicAdapter(mutableListOf(), isFavorites = false)
        favoritosAdapter = ComicAdapter(mutableListOf(), isFavorites = true)

        recyclerViewMisComics.adapter = comicsAdapter
        recyclerViewFavoritos.adapter = favoritosAdapter

        loadComics()

        val btnAddComic: Button = view.findViewById(R.id.btnAddComic)
        btnAddComic.setOnClickListener {
            goToAddComic()
        }
    }

    private fun loadComics() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userId = user.uid

            db.collection("comics")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val comicsList = documents.map { document ->
                        Comic(
                            id = document.id,
                            title = document.getString("title") ?: "Sin título",
                            author = document.getString("author") ?: "Desconocido",
                            imageUrl = document.getString("imageUrl") ?: "",
                            genre = document.getString("genre") ?: "Desconocido",
                            price = document.getDouble("price")?.toFloat() ?: 0f,  // Cambio aquí
                            condition = document.getString("condition") ?: "Nuevo",
                            location = document.getString("location") ?: "Desconocido",
                            userId = document.getString("userId") ?: ""
                        )
                    }

                    comicsAdapter.updateData(comicsList as MutableList<Comic>)

                    loadFavoriteComics(userId)
                }
                .addOnFailureListener { exception ->
                    Log.e("MisComics", "Error al cargar cómics: ${exception.message}")
                }
        }
    }

    private fun loadFavoriteComics(userId: String) {
        db.collection("usuarios")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { documents ->
                val tasks = documents.mapNotNull { document ->
                    document.getString("comicId")?.let { comicId ->
                        db.collection("comics").document(comicId).get()
                    }
                }

                // Esperar a que todas las tareas se completen
                com.google.android.gms.tasks.Tasks.whenAllSuccess<com.google.firebase.firestore.DocumentSnapshot>(tasks)
                    .addOnSuccessListener { results ->
                        val favoriteComics = results.map { comicDocument ->
                            Comic(
                                id = comicDocument.id,
                                title = comicDocument.getString("title") ?: "Sin título",
                                author = comicDocument.getString("author") ?: "Desconocido",
                                imageUrl = comicDocument.getString("imageUrl") ?: "",
                                genre = comicDocument.getString("genre") ?: "Desconocido",
                                price = comicDocument.getDouble("price")?.toFloat() ?: 0f,  // Cambio aquí
                                condition = comicDocument.getString("condition") ?: "Nuevo",
                                location = comicDocument.getString("location") ?: "Desconocido",
                                userId = comicDocument.getString("userId") ?: ""
                            )
                        }

                        favoritosAdapter.updateData(favoriteComics)
                    }
                    .addOnFailureListener {
                        Log.e("MisComics", "Error al cargar cómics favoritos")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("MisComics", "Error al consultar favoritos: ${exception.message}")
            }
    }

    private fun goToAddComic() {
        val addComicFragment = AddComicFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, addComicFragment)
            .addToBackStack(null)
            .commit()
    }
}
