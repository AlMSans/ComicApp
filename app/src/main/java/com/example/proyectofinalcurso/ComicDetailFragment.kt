package com.example.proyectofinalcurso

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class ComicDetailFragment : Fragment() {

    private lateinit var comicImageView: ImageView

    companion object {
        fun newInstance(
            id: String, title: String, author: String, genre: String,
            location: String, condition: String, price: Float, imageUrl: String, userId: String
        ): ComicDetailFragment {
            val fragment = ComicDetailFragment()
            val args = Bundle()
            args.putString("id", id)
            args.putString("title", title)
            args.putString("author", author)
            args.putString("genre", genre)
            args.putString("location", location)
            args.putString("condition", condition)
            args.putFloat("price", price)
            args.putString("imageUrl", imageUrl)
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comic_detail, container, false)

        // Obtener datos
        val title = arguments?.getString("title") ?: "Sin título"
        val author = arguments?.getString("author") ?: "Desconocido"
        val genre = arguments?.getString("genre") ?: "Desconocido"
        val location = arguments?.getString("location") ?: "Desconocida"
        val condition = arguments?.getString("condition") ?: "Desconocido"
        val price = arguments?.getFloat("price") ?: 0f
        val imageUrl = arguments?.getString("imageUrl") ?: ""
        val userId = arguments?.getString("userId") ?: ""

        Log.d("ComicDetailFragment", "User ID al cargar el fragmento: $userId")

        val titleView: TextView = view.findViewById(R.id.detailTitle)
        val authorView: TextView = view.findViewById(R.id.detailAuthor)
        val genreView: TextView = view.findViewById(R.id.detailGenre)
        val locationView: TextView = view.findViewById(R.id.detailLocation)
        val conditionView: TextView = view.findViewById(R.id.detailCondition)
        val priceView: TextView = view.findViewById(R.id.detailPrice)
        comicImageView = view.findViewById(R.id.detailImageView)

        titleView.text = title
        authorView.text = "Autor: $author"
        genreView.text = "Género: $genre"
        locationView.text = "Ubicación: $location"
        conditionView.text = "Estado: $condition"
        priceView.text = "Precio: €$price"

        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.hb2)
            .error(R.drawable.hb3)
            .into(comicImageView)

        // Cargar el nombre del usuario
        if (userId.isNotEmpty()) {
            val userRef = FirebaseFirestore.getInstance().collection("usuarios").document(userId)

            userRef.get().addOnSuccessListener { document ->
                val userName = document.getString("nombre") ?: "Desconocido"
                Log.d("ComicDetailFragment", "Usuario encontrado: $userName")

                activity?.runOnUiThread {
                    view.findViewById<TextView>(R.id.detailUserName).text = "Nombre: $userName"
                }
            }.addOnFailureListener { exception ->
                Log.e("ComicDetailFragment", "Error al cargar los datos del usuario", exception)
                Toast.makeText(requireContext(), "Error al cargar el nombre del usuario", Toast.LENGTH_SHORT).show()
                activity?.runOnUiThread {
                    view.findViewById<TextView>(R.id.detailUserName).text = "Usuario no disponible"
                }
            }
        }

        val favoriteButton: Button = view.findViewById(R.id.favoriteButton)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val favoritesRef = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(currentUser.uid)
                .collection("favorites")

            // Comprobar si el cómic ya está en favoritos
            val comicId = arguments?.getString("id") ?: ""
            favoritesRef.whereEqualTo("comicId", comicId).get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // El cómic ya está en favoritos, ocultamos el botón
                    favoriteButton.visibility = View.GONE
                }
            }

            // Acción al hacer clic en el botón de favoritos
            favoriteButton.setOnClickListener {
                val comicData = hashMapOf(
                    "comicId" to arguments?.getString("id"),
                    "title" to arguments?.getString("title"),
                    "imageUrl" to arguments?.getString("imageUrl")
                )

                favoritesRef.add(comicData).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cómic guardado como favorito", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            favoriteButton.visibility = View.GONE  // Si no hay usuario logueado, ocultar el botón
        }

        // Botón para contactar con el propietario del cómic
        val contactButton = view.findViewById<Button>(R.id.btnContactar)
        if (currentUser?.uid != userId) {
            contactButton.setOnClickListener {
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("receiverId", userId)
                startActivity(intent)
            }
        } else {
            contactButton.visibility = View.GONE
        }

        return view
    }
}
