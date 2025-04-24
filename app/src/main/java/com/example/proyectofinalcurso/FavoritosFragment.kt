package com.example.proyectofinalcurso

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalcurso.data.toComic        // ← mapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class FavoritosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var comicsAdapter: ListUsuComics
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_favoritos, container, false).apply {

        recyclerView = findViewById(R.id.recyclerViewFavoritos)
        recyclerView.layoutManager = LinearLayoutManager(context)

        comicsAdapter = ListUsuComics(emptyList()) { abrirDetallesComic(it) }
        recyclerView.adapter = comicsAdapter

        loadFavoriteComics()
    }

    /* ---------- cargar favoritos ---------- */

    private fun loadFavoriteComics() {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("FavoritosFragment", "Usuario no autenticado")
            return
        }

        db.collection("usuarios").document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { favDocs ->
                if (favDocs.isEmpty) {
                    comicsAdapter.updateData(emptyList())
                    return@addOnSuccessListener
                }

                val favoritos = mutableListOf<Comic>()
                var processed  = 0

                favDocs.forEach { fav ->
                    val comicId = fav.getString("comicId") ?: return@forEach

                    db.collection("comics").document(comicId).get()
                        .addOnSuccessListener { comicDoc ->
                            favoritos.add(comicDoc.toComic())
                        }
                        .addOnCompleteListener {
                            processed++
                            if (processed == favDocs.size()) {
                                comicsAdapter.updateData(favoritos)
                            }
                        }
                }
            }
            .addOnFailureListener {
                Log.e("FavoritosFragment", "Error al cargar favoritos", it)
            }
    }

    /* ---------- abrir detalle ---------- */

    private fun abrirDetallesComic(comic: Comic) {

        // Creamos la lista de imágenes
        val urls = ArrayList(comic.imageUrls)
        // Si proviene de documento viejo con imageUrl único
        comic.imageUrl?.let { if (urls.isEmpty()) urls.add(it) }

        val fragment = ComicDetailFragment.newInstance(
            comic.id, comic.title, comic.author, comic.genre,
            comic.location, comic.condition, comic.price, urls, comic.userId
        )

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
