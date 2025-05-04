package com.example.proyectofinalcurso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class ListUsuComics(
    private var comicsList: List<Comic>,
    private val onComicClick: (Comic) -> Unit
) : RecyclerView.Adapter<ListUsuComics.ComicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder =
        ComicViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.comic_list, parent, false)
        )

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) =
        holder.bind(comicsList[position])

    override fun getItemCount() = comicsList.size

    fun updateData(newList: List<Comic>) {
        comicsList = newList
        notifyDataSetChanged()
    }

    inner class ComicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val titleTv:      TextView  = itemView.findViewById(R.id.textViewTitle)
        private val authorTv:     TextView  = itemView.findViewById(R.id.textViewAuthor)
        private val locationTv:   TextView  = itemView.findViewById(R.id.textViewLocation)
        private val conditionTv:  TextView  = itemView.findViewById(R.id.textViewCondition)
        private val priceTv:      TextView  = itemView.findViewById(R.id.textViewPrice)
        private val genreTv:      TextView  = itemView.findViewById(R.id.textViewGenre)
        private val comicIv:      ImageView = itemView.findViewById(R.id.imageViewComic)

        fun bind(comic: Comic) {
            titleTv.text     = comic.title
            authorTv.text    = comic.author
            locationTv.text  = comic.location
            conditionTv.text = comic.condition
            priceTv.text     = "Precio: €${comic.price}"
            genreTv.text     = "Género: ${comic.genre}"

            // Elegimos la primera imagen disponible (lista nueva o campo legacy)
            val thumbUrl = comic.imageUrls.firstOrNull() ?: comic.imageUrl

            Glide.with(itemView.context)
                .load(thumbUrl)
                .transform(RoundedCorners(30))
                .placeholder(R.drawable.hb2)
                .error(R.drawable.hb3)
                .into(comicIv)

            itemView.setOnClickListener { onComicClick(comic) }
        }
    }
}
