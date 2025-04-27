package com.example.proyectofinalcurso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisComicsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var comicAdapter: ComicAdapter
    private val comicsList = mutableListOf<Comic>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mis_comics, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMisComics)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        comicAdapter = ComicAdapter(comicsList, isFavorites = false) { }
        recyclerView.adapter = comicAdapter

        cargarMisComics()

        return view
    }

    private fun cargarMisComics() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            FirebaseFirestore.getInstance()
                .collection("comics")
                .whereEqualTo("userId", it.uid)
                .get()
                .addOnSuccessListener { documents ->
                    comicsList.clear()
                    for (document in documents) {
                        val comic = document.toObject(Comic::class.java).copy(id = document.id)
                        comicsList.add(comic)
                    }
                    comicAdapter.updateData(comicsList)
                }
                .addOnFailureListener { exception ->
                    // Manejar error
                }
        }
    }
}
