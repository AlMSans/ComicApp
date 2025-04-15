package com.example.proyectofinalcurso

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CompleteProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializamos las vistas
        nameEditText = findViewById(R.id.nameEditText)
        surnameEditText = findViewById(R.id.surnameEditText)
        addressEditText = findViewById(R.id.addressEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        saveButton = findViewById(R.id.saveButton)

        // Acción del botón guardar
        saveButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Verificamos si el correo y la contraseña no están vacíos
            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password)
            } else {
                showToast("Por favor, ingresa un correo y una contraseña")
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        // Registro del usuario en Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Guardar los demás datos en Firestore
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = hashMapOf(
                            "nombre" to nameEditText.text.toString().trim(),
                            "apellido" to surnameEditText.text.toString().trim(),
                            "direccion" to addressEditText.text.toString().trim(),
                            "email" to email,
                            "contraseña" to password
                        )

                        db.collection("usuarios").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                showToast("Registro exitoso")
                                goToMainPanel()
                            }
                            .addOnFailureListener { e ->
                                showToast("Error al guardar: ${e.message}")
                            }
                    }
                } else {
                    showToast("Error al registrar: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun goToMainPanel() {
        // Redirigir al panel principal
        val intent = Intent(this, MainPanelActivity::class.java)
        startActivity(intent)
        finish()
    }
}
