package com.example.proyectofinalcurso

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ComicAdapter(
    private var comicsList: MutableList<Comic>,
    private val isFavorites: Boolean,
    private val onComicClick: (Comic) -> Unit
) : RecyclerView.Adapter<ComicAdapter.ComicViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comic, parent, false)
        return ComicViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val comic = comicsList[position]
        holder.titleTextView.text = comic.title
        holder.authorTextView.text = comic.author
        holder.genreTextView.text = comic.genre
        holder.priceTextView.text = "€${comic.price}"
        holder.conditionTextView.text = comic.condition
        holder.locationTextView.text = comic.location

        // Elegimos la primera imagen disponible (lista nueva o campo legacy)
        val thumbUrl = comic.imageUrls.firstOrNull() ?: comic.imageUrl

        Glide.with(holder.itemView.context)
            .load(thumbUrl)
            .transform(RoundedCorners(30))
            .placeholder(R.drawable.hb2)
            .error(R.drawable.hb3)
            .into(holder.comicImageView)

        // Mostrar botones según contexto
        holder.deleteButton.visibility = View.VISIBLE
        holder.deleteButton.setOnClickListener {
            if (isFavorites) {
                deleteFavoriteComic(comic.id)
            } else {
                deleteUserComic(comic.id)
            }
        }

        if (!isFavorites) {
            holder.editButton.visibility = View.VISIBLE
            holder.editButton.setOnClickListener {
                editComic(comic, holder.itemView.context)
            }
        } else {
            holder.editButton.visibility = View.GONE
        }

        // Click en el cómic solo si es de favoritos
        if (isFavorites) {
            holder.itemView.setOnClickListener {
                onComicClick(comic)
            }
        }
    }

    override fun getItemCount(): Int = comicsList.size

    fun updateData(newComicsList: List<Comic>) {
        comicsList = newComicsList.toMutableList()
        notifyDataSetChanged()
    }

    private fun editComic(comic: Comic, context: Context) {
        val editComicFragment = EditFragment()
        val bundle = Bundle()
        bundle.putParcelable("comic", comic)
        editComicFragment.arguments = bundle
        (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, editComicFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteFavoriteComic(comicId: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.collection("usuarios")
                .document(it.uid)
                .collection("favorites")
                .whereEqualTo("comicId", comicId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.delete()
                    }
                    comicsList = comicsList.filter { comic -> comic.id != comicId } as MutableList<Comic>
                    notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("Favorites", "Error al eliminar favorito: ${exception.message}")
                }
        }
    }

    private fun deleteUserComic(comicId: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.collection("comics")
                .document(comicId)
                .delete()
                .addOnSuccessListener {
                    comicsList.removeIf { comic -> comic.id == comicId }
                    notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Log.e("MisComics", "Error al eliminar cómic", it)
                }
        }
    }

    class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val authorTextView: TextView = itemView.findViewById(R.id.textViewAuthor)
        val genreTextView: TextView = itemView.findViewById(R.id.textViewGenre)
        val priceTextView: TextView = itemView.findViewById(R.id.textViewPrice)
        val conditionTextView: TextView = itemView.findViewById(R.id.textViewCondition)
        val locationTextView: TextView = itemView.findViewById(R.id.textViewLocation)
        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteComic)
        val editButton: Button = itemView.findViewById(R.id.btnEditComic)
        val comicImageView: ImageView = itemView.findViewById(R.id.imageViewComic)
    }
}