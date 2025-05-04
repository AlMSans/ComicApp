package com.example.proyectofinalcurso

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class ComicDetailFragment : Fragment() {

    companion object {
        fun newInstance(
            id: String,
            title: String,
            author: String,
            genre: String,
            location: String,
            condition: String,
            price: Float,
            imageUrls: ArrayList<String>,
            userId: String
        ): ComicDetailFragment = ComicDetailFragment().apply {
            arguments = Bundle().apply {
                putString("id", id)
                putString("title", title)
                putString("author", author)
                putString("genre", genre)
                putString("location", location)
                putString("condition", condition)
                putFloat("price", price)
                putStringArrayList("imageUrls", imageUrls)
                putString("userId", userId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_detail, container, false).apply {

        val titleTv: TextView       = findViewById(R.id.detailTitle)
        val authorTv: TextView      = findViewById(R.id.detailAuthor)
        val genreTv: TextView       = findViewById(R.id.detailGenre)
        val locationTv: TextView    = findViewById(R.id.detailLocation)
        val conditionTv: TextView   = findViewById(R.id.detailCondition)
        val priceTv: TextView       = findViewById(R.id.detailPrice)
        val userNameTv: TextView    = findViewById(R.id.detailUserName)
        val favBtn: Button          = findViewById(R.id.favoriteButton)
        val contactBtn: Button      = findViewById(R.id.btnContactar)
        val pager: ViewPager2       = findViewById(R.id.viewPagerImages)
        val ratingBar: RatingBar    = findViewById(R.id.ratingBar)
        val backBtn: Button         = findViewById(R.id.btnVolver) // Botón Volver

        val args      = requireArguments()
        val comicId   = args.getString("id") ?: ""
        val userId    = args.getString("userId") ?: ""
        val images    = args.getStringArrayList("imageUrls") ?: arrayListOf()

        args.getString("imageUrl")?.let { if (images.isEmpty()) images.add(it) }

        titleTv.text     = args.getString("title")
        authorTv.text    = "Autor: ${args.getString("author")}"
        genreTv.text     = "Género: ${args.getString("genre")}"
        locationTv.text  = "Ubicación: ${args.getString("location")}"
        conditionTv.text = "Estado: ${args.getString("condition")}"
        priceTv.text     = "Precio: €${args.getFloat("price")}"

        pager.adapter = ImagePagerAdapter(images)

        ratingBar.stepSize = 0.5f
        ratingBar.numStars = 5

        FirebaseFirestore.getInstance()
            .collection("comics")
            .document(comicId)
            .get()
            .addOnSuccessListener { comic ->
                val rating = comic.getDouble("rating") ?: 0.0
                ratingBar.rating = rating.toFloat()
                ratingBar.invalidate()
            }
            .addOnFailureListener {
                Log.e("RatingError", "Error cargando rating", it)
            }

        if (userId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener {
                    userNameTv.text = "Nombre: ${it.getString("nombre") ?: "Desconocido"}"
                }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) favBtn.visibility = View.GONE else {
            val favRef = FirebaseFirestore.getInstance()
                .collection("usuarios").document(currentUser.uid)
                .collection("favorites")

            favRef.whereEqualTo("comicId", comicId).get().addOnSuccessListener {
                if (!it.isEmpty) favBtn.visibility = View.GONE
            }

            favBtn.setOnClickListener {
                favRef.add(
                    mapOf(
                        "comicId"  to comicId,
                        "title"    to args.getString("title"),
                        "imageUrl" to images.firstOrNull()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(context, "Cómic guardado como favorito", Toast.LENGTH_SHORT).show()
                    favBtn.visibility = View.GONE
                }
            }
        }

        if (currentUser?.uid != userId) {
            contactBtn.setOnClickListener {
                startActivity(Intent(requireContext(), ChatActivity::class.java)
                    .putExtra("receiverId", userId))
            }
        } else contactBtn.visibility = View.GONE

        // Implementación del botón Volver
        backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private inner class ImagePagerAdapter(private val urls: List<String>) :
        RecyclerView.Adapter<ImagePagerAdapter.ImageVH>() {

        inner class ImageVH(val iv: ImageView) : RecyclerView.ViewHolder(iv)

        override fun onCreateViewHolder(p: ViewGroup, vType: Int) =
            ImageVH(ImageView(p.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            })

        override fun getItemCount() = urls.size

        override fun onBindViewHolder(h: ImageVH, pos: Int) {
            Glide.with(h.iv.context)
                .load(urls[pos])
                .transform(RoundedCorners(30))
                .placeholder(R.drawable.hb2)
                .error(R.drawable.hb3)
                .into(h.iv)
        }
    }
}
