package com.example.proyectofinalcurso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class SubastaAdapter(
    private var comicsList: MutableList<Comic>,
    private val onComicClick: (Comic) -> Unit
) : RecyclerView.Adapter<SubastaAdapter.SubastaViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubastaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comic, parent, false)
        return SubastaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubastaViewHolder, position: Int) {
        val comic = comicsList[position]

        holder.titleTextView.text = comic.title
        holder.authorTextView.text = comic.author
        holder.genreTextView.text = comic.genre
        holder.priceTextView.text = "€${comic.price}"
        holder.conditionTextView.text = comic.condition
        holder.locationTextView.text = comic.location

        val thumbUrl = comic.imageUrls.firstOrNull() ?: comic.imageUrl
        Glide.with(holder.itemView.context)
            .load(thumbUrl)
            .transform(RoundedCorners(30))
            .placeholder(R.drawable.hb2)
            .error(R.drawable.hb3)
            .into(holder.comicImageView)

        // Ocultar botones de edición y eliminación en este adaptador
        holder.deleteButton.visibility = View.GONE
        holder.editButton.visibility = View.GONE

        // Fondo si está seleccionado
        holder.itemView.setBackgroundResource(
            if (position == selectedPosition) R.drawable.bg_selected_comic else R.drawable.bg_default_comic
        )

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onComicClick(comic)
        }
    }

    override fun getItemCount(): Int = comicsList.size

    fun updateData(newComicsList: List<Comic>) {
        comicsList = newComicsList.toMutableList()
        notifyDataSetChanged()
    }

    class SubastaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val authorTextView: TextView = itemView.findViewById(R.id.textViewAuthor)
        val genreTextView: TextView = itemView.findViewById(R.id.textViewGenre)
        val priceTextView: TextView = itemView.findViewById(R.id.textViewPrice)
        val conditionTextView: TextView = itemView.findViewById(R.id.textViewCondition)
        val locationTextView: TextView = itemView.findViewById(R.id.textViewLocation)
        val comicImageView: ImageView = itemView.findViewById(R.id.imageViewComic)

        // Referencias a los botones para ocultarlos
        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteComic)
        val editButton: Button = itemView.findViewById(R.id.btnEditComic)
    }
}
