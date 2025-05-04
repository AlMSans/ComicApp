package com.example.proyectofinalcurso

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SubastaDisponiblesAdapter(
    private var subastas: List<Subasta>,
    private val onPujarClick: (Subasta) -> Unit
) : RecyclerView.Adapter<SubastaDisponiblesAdapter.SubastaViewHolder>() {

    inner class SubastaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.tvTituloSubasta)
        val mejorPostor: TextView = view.findViewById(R.id.tvMejorPostor)
        val precioActual: TextView = view.findViewById(R.id.tvPrecioActual)
        val tvPrecioInicial: TextView = view.findViewById(R.id.tvPrecioInicial)  // Nuevo TextView para el precio inicial
        val btnPujar: Button = view.findViewById(R.id.btnPujar)
        val tvSubastaCerrada: TextView = itemView.findViewById(R.id.tvSubastaCerrada)
        val imagen: ImageView = view.findViewById(R.id.ivComicSubasta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubastaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subasta_disponible, parent, false)
        return SubastaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubastaViewHolder, position: Int) {
        val subasta = subastas[position]

        holder.titulo.text = subasta.titulo
        holder.precioActual.text = "Mejor oferta: ${subasta.mejorOferta} €"
        holder.tvPrecioInicial.text = "Precio inicial: ${subasta.precioInicial} €"  // Cargar el precio inicial
        holder.mejorPostor.text = if (subasta.nombrePostor.isNotEmpty()) {
            "Mejor postor: ${subasta.nombrePostor}"
        } else {
            "Nadie ha pujado todavia"
        }

        // Cargar la imagen
        Glide.with(holder.itemView.context)
            .load(subasta.imagenUrl)
            .into(holder.imagen)

        if (subasta.cerrada) {
            holder.btnPujar.visibility = View.GONE
            holder.tvSubastaCerrada.visibility = View.VISIBLE
        } else {
            holder.btnPujar.visibility = View.VISIBLE
            holder.tvSubastaCerrada.visibility = View.GONE
            holder.btnPujar.setOnClickListener { onPujarClick(subasta) }
        }
    }

    override fun getItemCount(): Int = subastas.size

    fun updateData(newSubastas: List<Subasta>) {
        this.subastas = newSubastas
        notifyDataSetChanged()
    }
}
