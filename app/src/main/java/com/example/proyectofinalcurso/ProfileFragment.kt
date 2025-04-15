package com.example.proyectofinalcurso

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var nameTextView: TextView
    private lateinit var surnameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var backButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas
        nameTextView = view.findViewById(R.id.nameTextView)
        surnameTextView = view.findViewById(R.id.surnameTextView)
        addressTextView = view.findViewById(R.id.addressTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        logoutButton = view.findViewById(R.id.logoutButton)
        backButton = view.findViewById(R.id.backButton)

        // Obtener datos del usuario desde Firestore
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        nameTextView.text = "Nombre: ${document.getString("nombre")}"
                        surnameTextView.text = "Apellido: ${document.getString("apellido")}"
                        addressTextView.text = "Dirección: ${document.getString("direccion")}"
                        emailTextView.text = "Correo: ${document.getString("email")}"
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Acción de cerrar sesión
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Cierra el MainPanelActivity
        }

        // Acción de volver al panel principal
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
