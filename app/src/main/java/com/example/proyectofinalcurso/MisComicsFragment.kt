package com.example.proyectofinalcurso

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinalcurso.databinding.FragmentMisComicsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisComicsFragment : Fragment() {

    private var _binding: FragmentMisComicsBinding? = null
    private val binding get() = _binding!!

    private lateinit var comicsAdapter: ComicAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisComicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView
        comicsAdapter = ComicAdapter(mutableListOf(), isFavorites = false)
        binding.recyclerViewMisComics.apply {
            adapter = comicsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        // Pull-to-refresh
        binding.swipeMisComics.setOnRefreshListener { loadMyComics() }

        loadMyComics()

        // Botón “Agregar cómic”
        binding.btnAddComic.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddComicFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /** Descarga los cómics subidos por el usuario y actualiza el adapter */
    private fun loadMyComics() {
        val uid = currentUserId ?: run {
            binding.swipeMisComics.isRefreshing = false
            return
        }

        db.collection("comics")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { docs ->
                val myComics = docs.map { snap ->
                    val imageUrls = (snap.get("imageUrls") as? List<String>) ?: emptyList()

                    Comic(
                        id = snap.id,
                        title = snap.getString("title") ?: "Sin título",
                        author = snap.getString("author") ?: "Desconocido",
                        genre  = snap.getString("genre") ?: "Desconocido",
                        price  = (snap.get("price") as? Number)?.toFloat() ?: 0f,
                        condition = snap.getString("condition") ?: "N/A",
                        location  = snap.getString("location") ?: "N/A",
                        userId = snap.getString("userId") ?: "",
                        imageUrl = snap.getString("imageUrl"),
                        imageUrls = imageUrls
                    )
                }
                comicsAdapter.updateData(myComics)
                binding.swipeMisComics.isRefreshing = false
            }
            .addOnFailureListener { e ->
                Log.e("MisComics", "Error al cargar cómics", e)
                binding.swipeMisComics.isRefreshing = false
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
