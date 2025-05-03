package com.example.proyectofinalcurso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class UserDetailFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var surnameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var backButton: MaterialButton

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_detail, container, false)

        // Referencias de los elementos del layout
        profileImageView = view.findViewById(R.id.profileImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        surnameTextView = view.findViewById(R.id.surnameTextView)
        addressTextView = view.findViewById(R.id.addressTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        backButton = view.findViewById(R.id.backButton)

        val userId = arguments?.getString("userId") ?: return view

        // Cargar los datos del usuario desde Firestore
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nameTextView.text = doc.getString("nombre") ?: "Nombre no disponible"
                    surnameTextView.text = doc.getString("apellido") ?: "Apellido no disponible"
                    addressTextView.text = doc.getString("direccion") ?: "Dirección no disponible"
                    emailTextView.text = doc.getString("email") ?: "Correo no disponible"

                    val imageUrl = doc.getString("profileImageUrl")
                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.cp2)
                        .into(profileImageView)
                }
            }

        // Botón para volver atrás
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    companion object {
        fun newInstance(userId: String): UserDetailFragment {
            val fragment = UserDetailFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }
}
