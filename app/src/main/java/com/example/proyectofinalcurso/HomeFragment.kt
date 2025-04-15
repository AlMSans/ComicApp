package com.example.proyectofinalcurso

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var comicsAdapter: ListUsuComics
    private lateinit var auth: FirebaseAuth
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val db = FirebaseFirestore.getInstance()
    private var lastDocumentSnapshot: DocumentSnapshot? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        recyclerView = view.findViewById(R.id.recyclerViewComics)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        comicsAdapter = ListUsuComics(emptyList()) { comic ->
            abrirDetallesComic(comic)
        }

        recyclerView.adapter = comicsAdapter

        swipeRefreshLayout.setOnRefreshListener {
            lastDocumentSnapshot = null // Reset paginación
            loadComics()
        }

        configureFirestoreCache()
        loadComics()

        return view
    }

    private fun configureFirestoreCache() {
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Habilita caché local
            .build()
    }

    private fun loadComics() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e("HomeFragment", "Usuario no autenticado")
            return
        }

        var query = db.collection("comics")
            .whereNotEqualTo("userId", userId)
            .limit(10)

        if (lastDocumentSnapshot != null) {
            query = query.startAfter(lastDocumentSnapshot!!)
        }

        query.get(Source.CACHE) // Prioriza caché
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    lastDocumentSnapshot = documents.documents.last()
                }

                val comicsList = documents.map { document ->

                    val price = when (val priceValue = document.get("price")) {
                        is Number -> priceValue.toFloat()
                        is String -> priceValue.toFloatOrNull() ?: 0f
                        else -> 0f
                    }

                    Comic(
                        id = document.id,
                        title = document.getString("title") ?: "Sin título",
                        author = document.getString("author") ?: "Desconocido",
                        imageUrl = document.getString("imageUrl") ?: "",
                        location = document.getString("location") ?: "Desconocida",
                        condition = document.getString("condition") ?: "Desconocido",
                        price = price,
                        genre = document.getString("genre") ?: "Desconocido",
                        userId = document.getString("userId") ?: ""
                    )
                }

                comicsAdapter.updateData(comicsList)
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error al cargar cómics: ${exception.message}")
                swipeRefreshLayout.isRefreshing = false
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
