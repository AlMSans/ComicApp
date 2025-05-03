package com.example.proyectofinalcurso.subastas

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalcurso.AddSubastaFragment
import com.example.proyectofinalcurso.R
import com.example.proyectofinalcurso.Subasta
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class MisSubastasFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var subastasAdapter: MisSubastasAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_mis_subastas, container, false).apply {

        recyclerView = findViewById(R.id.recyclerViewMisSubastas)
        recyclerView.layoutManager = LinearLayoutManager(context)
        subastasAdapter = MisSubastasAdapter(emptyList(), ::onCerrarSubasta, ::onEliminarSubasta)
        recyclerView.adapter = subastasAdapter

        findViewById<FloatingActionButton>(R.id.btnNuevaSubasta).setOnClickListener {
            // Crear una instancia del AddSubastaFragment
            val addSubastaFragment = AddSubastaFragment()

            // Realizar la transacción para reemplazar el fragmento actual con AddSubastaFragment
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, addSubastaFragment)
            fragmentTransaction.addToBackStack(null) // Si quieres poder navegar hacia atrás
            fragmentTransaction.commit()

            // Puedes agregar un mensaje opcional para confirmar la acción
            Toast.makeText(context, "Crear nueva subasta", Toast.LENGTH_SHORT).show()
        }


        cargarMisSubastas()
    }

    private fun cargarMisSubastas() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("subastas")
            .whereEqualTo("propietarioId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("MisSubastasFragment", "Error al cargar subastas", error)
                    return@addSnapshotListener
                }

                val lista = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Subasta::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                subastasAdapter.updateData(lista)
            }
    }

    private fun onCerrarSubasta(subasta: Subasta) {
        db.collection("subastas").document(subasta.id)
            .update("cerrada", true)
            .addOnSuccessListener {
                Toast.makeText(context, "Subasta cerrada", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onEliminarSubasta(subasta: Subasta) {
        db.collection("subastas").document(subasta.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Subasta eliminada", Toast.LENGTH_SHORT).show()
            }
    }

}

