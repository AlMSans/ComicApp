package com.example.proyectofinalcurso.subastas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalcurso.R
import com.example.proyectofinalcurso.Subasta

class MisSubastasAdapter(
    private var subastas: List<Subasta>,
    private val onCerrarClick: (Subasta) -> Unit,
    private val onEliminarClick: (Subasta) -> Unit
) : RecyclerView.Adapter<MisSubastasAdapter.SubastaViewHolder>() {

    // ViewHolder para cada subasta
    inner class SubastaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.tvTituloSubasta)
        val precioActual: TextView = view.findViewById(R.id.tvPrecioActual)
        val btnCerrar: Button = view.findViewById(R.id.btnCerrarSubasta)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminarSubasta)
    }

    // Crea un nuevo ViewHolder y le asigna el layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubastaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mi_subasta, parent, false)
        return SubastaViewHolder(view)
    }

    // Asigna los datos de la subasta a la vista
    override fun onBindViewHolder(holder: SubastaViewHolder, position: Int) {
        val subasta = subastas[position]

        holder.titulo.text = subasta.titulo
        holder.precioActual.text = "Mejor oferta: ${subasta.mejorOferta} €"

        holder.btnCerrar.setOnClickListener { onCerrarClick(subasta) }
        holder.btnEliminar.setOnClickListener { onEliminarClick(subasta) }
    }

    // Retorna la cantidad de items en la lista de subastas
    override fun getItemCount(): Int = subastas.size

    // Actualiza los datos y notifica a RecyclerView que se ha hecho un cambio
    fun updateData(newSubastas: List<Subasta>) {
        this.subastas = newSubastas
        notifyDataSetChanged()
    }
}
