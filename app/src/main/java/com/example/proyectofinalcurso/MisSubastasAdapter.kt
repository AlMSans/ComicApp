package com.example.proyectofinalcurso.subastas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectofinalcurso.R
import com.example.proyectofinalcurso.Subasta

class MisSubastasAdapter(
    private var subastas: List<Subasta>,
    private val onCerrarClick: (Subasta) -> Unit,
    private val onEliminarClick: (Subasta) -> Unit
) : RecyclerView.Adapter<MisSubastasAdapter.SubastaViewHolder>() {

    inner class SubastaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.tvTituloSubasta)
        val precioActual: TextView = view.findViewById(R.id.tvPrecioActual)
        val tvPrecioInicial: TextView = view.findViewById(R.id.tvPrecioInicial)
        val mejorPostor: TextView = view.findViewById(R.id.tvMejorPostor)
        val btnCerrar: Button = view.findViewById(R.id.btnCerrarSubasta)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminarSubasta)
        val imagen: ImageView = view.findViewById(R.id.ivComicSubasta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubastaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mi_subasta, parent, false)
        return SubastaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubastaViewHolder, position: Int) {
        val subasta = subastas[position]

        holder.titulo.text = subasta.titulo
        holder.precioActual.text = "Mejor oferta: ${subasta.mejorOferta} €"
        holder.tvPrecioInicial.text = "Precio inicial: ${subasta.precioInicial} €"
        holder.mejorPostor.text = if (subasta.nombrePostor.isNotEmpty()) {
            "Mejor postor: ${subasta.nombrePostor}"
        } else {
            "Nadie ha pujado todavia"
        }

        Glide.with(holder.itemView.context)
            .load(subasta.imagenUrl)
            .placeholder(R.drawable.bc1)
            .error(R.drawable.bc2)
            .into(holder.imagen)

        holder.btnCerrar.setOnClickListener { onCerrarClick(subasta) }
        holder.btnEliminar.setOnClickListener { onEliminarClick(subasta) }
    }


    override fun getItemCount(): Int = subastas.size

    fun updateData(newSubastas: List<Subasta>) {
        this.subastas = newSubastas
        notifyDataSetChanged()
    }
}
