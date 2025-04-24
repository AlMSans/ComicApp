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
import com.example.proyectofinalcurso.data.toComic      // ← importa la extensión
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
    ): View = inflater.inflate(R.layout.fragment_home, container, false).apply {

        auth = FirebaseAuth.getInstance()
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        recyclerView = findViewById(R.id.recyclerViewComics)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        comicsAdapter = ListUsuComics(emptyList()) { comic -> abrirDetallesComic(comic) }
        recyclerView.adapter = comicsAdapter

        swipeRefreshLayout.setOnRefreshListener {
            lastDocumentSnapshot = null
            loadComics()
        }

        configureFirestoreCache()
        loadComics()
    }

    private fun configureFirestoreCache() {
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    private fun loadComics() {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("HomeFragment", "Usuario no autenticado")
            return
        }

        var query: Query = db.collection("comics")
            .whereNotEqualTo("userId", userId)
            .limit(10)

        lastDocumentSnapshot?.let { query = query.startAfter(it) }

        query.get(Source.CACHE)
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    comicsAdapter.updateData(emptyList())
                    swipeRefreshLayout.isRefreshing = false
                    return@addOnSuccessListener
                }

                lastDocumentSnapshot = docs.documents.last()
                comicsAdapter.updateData(docs.map { it.toComic() })
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener {
                Log.e("HomeFragment", "Error al cargar cómics", it)
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun abrirDetallesComic(comic: Comic) {

        // Creamos la lista de imágenes (puede tener 1 o varias)
        val urls = ArrayList(comic.imageUrls)          // field nuevo
        if (urls.isEmpty() && comic.imageUrl != null)  // compat. con modelo viejo
            urls.add(comic.imageUrl)

        val fragment = ComicDetailFragment.newInstance(
            id        = comic.id,
            title     = comic.title,
            author    = comic.author,
            genre     = comic.genre,
            location  = comic.location,
            condition = comic.condition,
            price     = comic.price,
            imageUrls = urls,          //  ←  ahora enviamos la lista
            userId    = comic.userId
        )

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}
