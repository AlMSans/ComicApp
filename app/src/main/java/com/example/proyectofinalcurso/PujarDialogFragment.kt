package com.example.proyectofinalcurso

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class PujarDialogFragment(
    private val subasta: Subasta,
    private val onPujaConfirmada: (Subasta) -> Unit
) : DialogFragment() {

    private lateinit var pujaCantidadEditText: EditText
    private lateinit var btnConfirmarPuja: Button
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_pujar_dialog, container, false)

        pujaCantidadEditText = binding.findViewById(R.id.etPujaCantidad)
        btnConfirmarPuja = binding.findViewById(R.id.btnConfirmarPuja)

        btnConfirmarPuja.setOnClickListener {
            val nuevaPuja = pujaCantidadEditText.text.toString().toDoubleOrNull()
            val uid = currentUser?.uid ?: return@setOnClickListener

            if (nuevaPuja != null && nuevaPuja > subasta.mejorOferta) {
                // Obtener nombre del usuario
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val nombreUsuario = doc.getString("nombre") ?: "Anónimo"

                        val pujaMap = mapOf(
                            "mejorOferta" to nuevaPuja,
                            "mejorPostor" to uid,
                            "nombrePostor" to nombreUsuario
                        )

                        db.collection("subastas").document(subasta.id)
                            .update(pujaMap)
                            .addOnSuccessListener {
                                val subastaActualizada = subasta.copy(
                                    mejorOferta = nuevaPuja,
                                    mejorPostor = uid,
                                    nombrePostor = nombreUsuario
                                )
                                onPujaConfirmada(subastaActualizada)
                                dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al pujar", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al obtener nombre de usuario", Toast.LENGTH_SHORT).show()
                    }

            } else {
                Toast.makeText(context, "La puja debe ser mayor que ${subasta.mejorOferta}€", Toast.LENGTH_SHORT).show()
            }
        }

        return binding
    }
}
