package com.example.proyectofinalcurso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListUsuComics(private var comicsList: List<Comic>, private val onComicClick: (Comic) -> Unit) : RecyclerView.Adapter<ListUsuComics.ComicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comic_list, parent, false)
        return ComicViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        val comic = comicsList[position]
        holder.bind(comic)
    }

    override fun getItemCount(): Int = comicsList.size

    fun updateData(newComicsList: List<Comic>) {
        comicsList = newComicsList
        notifyDataSetChanged()
    }

    inner class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val authorTextView: TextView = itemView.findViewById(R.id.textViewAuthor)
        private val locationTextView: TextView = itemView.findViewById(R.id.textViewLocation)
        private val conditionTextView: TextView = itemView.findViewById(R.id.textViewCondition)
        private val priceTextView: TextView = itemView.findViewById(R.id.textViewPrice)
        private val genreTextView: TextView = itemView.findViewById(R.id.textViewGenre)
        private val comicImageView: ImageView = itemView.findViewById(R.id.imageViewComic)

        fun bind(comic: Comic) {
            titleTextView.text = comic.title
            authorTextView.text = comic.author
            locationTextView.text = comic.location
            conditionTextView.text = comic.condition
            priceTextView.text = "Precio: €${comic.price}"
            genreTextView.text = "Género: ${comic.genre}"

            itemView.setOnClickListener {
                onComicClick(comic)
            }
        }
    }
}
