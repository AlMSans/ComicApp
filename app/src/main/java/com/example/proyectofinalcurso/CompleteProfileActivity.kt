package com.example.proyectofinalcurso


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CompleteProfileActivity : AppCompatActivity() {

    // Declaramos las variables
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile) // tu XML

        // Inicializamos Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Referenciamos los elementos del XML
        nameEditText = findViewById(R.id.nameEditText)
        surnameEditText = findViewById(R.id.surnameEditText)
        addressEditText = findViewById(R.id.addressEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Botón Guardar
        saveButton.setOnClickListener {
            saveUserProfile()
        }

        // Botón Volver
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent) // Inicia MainActivity
            finish() // Cierra la actividad actual (CompleteProfileActivity)
        }

    }

    private fun saveUserProfile() {
        // Recogemos datos de los campos
        val nombre = nameEditText.text.toString().trim()
        val apellido = surnameEditText.text.toString().trim()
        val direccion = addressEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validamos que no estén vacíos
        if (nombre.isEmpty() || apellido.isEmpty() || direccion.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Primero: Registramos en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Usuario registrado exitosamente
                    val uid = auth.currentUser?.uid

                    // Creamos un mapa de datos para Firestore
                    val userData = hashMapOf(
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "direccion" to direccion,
                        "email" to email // Opcional, porque ya estará en Authentication, pero lo guardamos también
                    )

                    // Guardamos en Firestore en la colección "users"
                    uid?.let {
                        db.collection("usuarios").document(it)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Perfil completado correctamente", Toast.LENGTH_SHORT).show()
                                // Redirigimos al Main Panel
                                val intent = Intent(this, MainPanelActivity::class.java)
                                startActivity(intent)
                                finish() // Cerramos esta actividad
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Error al crear usuario: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
