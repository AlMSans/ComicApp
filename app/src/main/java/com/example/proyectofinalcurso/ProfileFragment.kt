package com.example.proyectofinalcurso

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var nameTextView: TextView
    private lateinit var surnameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var backButton: Button
    private lateinit var editButton: Button
    private lateinit var profileImageView: ImageView

    private var isEditingProfile = false  // Indica si estamos en el proceso de edición de perfil o no

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ProfileFragment", "onCreateView ejecutado")  // Log de prueba

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inicializar vistas
        nameTextView = view.findViewById(R.id.nameTextView)
        surnameTextView = view.findViewById(R.id.surnameTextView)
        addressTextView = view.findViewById(R.id.addressTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        logoutButton = view.findViewById(R.id.logoutButton)
        backButton = view.findViewById(R.id.backButton)
        editButton = view.findViewById(R.id.editButton)
        profileImageView = view.findViewById(R.id.profileImageView)

        // Obtener datos del usuario desde Firestore
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d("ProfileFragment", "Datos del usuario obtenidos correctamente")  // Log de prueba
                        // Llenar los datos del perfil con los valores de Firestore
                        nameTextView.text = "Nombre: ${document.getString("nombre")}"
                        surnameTextView.text = "Apellido: ${document.getString("apellido")}"
                        addressTextView.text = "Dirección: ${document.getString("direccion")}"
                        emailTextView.text = "Correo: ${document.getString("email")}"

                        // Obtener la URL de la imagen de perfil desde Firebase Storage
                        val imageUrl = document.getString("profileImageUrl")
                        if (imageUrl != null && imageUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(imageUrl)
                                .transform(RoundedCorners(30))
                                .placeholder(R.drawable.hb1) // Placeholder si no hay imagen
                                .into(profileImageView)
                        } else {
                            // Usar imagen por defecto (placeholder)
                            profileImageView.setImageResource(R.drawable.hb2)
                        }

                        // Si los datos existen, significa que estamos editando el perfil
                        isEditingProfile = true
                    } else {
                        Log.d("ProfileFragment", "No se encontraron datos, creando perfil nuevo")  // Log de prueba
                        // Si no existen datos del perfil, estamos en el proceso de creación
                        isEditingProfile = false
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("ProfileFragment", "Error al obtener datos: ${e.message}")  // Log de prueba
                    Toast.makeText(requireContext(), "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Acción de editar perfil, se abre EditProfileActivity
        editButton.setOnClickListener {
            Log.d("ProfileFragment", "Botón de editar perfil presionado")  // Log de prueba
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Acción de cerrar sesión
        logoutButton.setOnClickListener {
            Log.d("ProfileFragment", "Botón de cerrar sesión presionado")  // Log de prueba
            auth.signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Cierra el MainPanelActivity
        }

        // Acción de volver al panel principal
        backButton.setOnClickListener {
            Log.d("ProfileFragment", "Botón de volver presionado")  // Log de prueba
            if (isEditingProfile) {
                Log.d("ProfileFragment", "Volviendo al fragmento anterior (editando perfil)")  // Log de prueba
                requireActivity().onBackPressed()  // Esto solo quita el fragmento si es necesario
            } else {
                Log.d("ProfileFragment", "Volviendo a MainActivity (creando perfil)")  // Log de prueba
                // Navegar explícitamente a MainActivity
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()  // Cierra la actividad actual
            }
        }



        return view
    }
}


