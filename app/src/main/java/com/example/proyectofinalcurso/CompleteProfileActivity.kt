package com.example.proyectofinalcurso

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class CompleteProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var profileImageView: ImageView

    private var isEditingProfile = false
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inicialización de vistas
        nameEditText = findViewById(R.id.nameEditText)
        surnameEditText = findViewById(R.id.surnameEditText)
        addressEditText = findViewById(R.id.addressEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        saveButton = findViewById(R.id.saveButton)
        uploadImageButton = findViewById(R.id.selectImageButton)
        profileImageView = findViewById(R.id.profileImageView)
        val backButton: Button = findViewById(R.id.backButton)

        // Verificar si el usuario ya está autenticado
        val user = auth.currentUser
        if (user != null) {
            isEditingProfile = true
            loadUserProfile(user.uid)
        }

        // Acción para seleccionar una imagen
        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Acción del botón guardar
        saveButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (isEditingProfile) {
                updateUserProfile(email, password)
            } else {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    registerUser(email, password)
                } else {
                    showToast("Por favor, ingresa un correo y una contraseña")
                }
            }
        }

        // Acción para volver
        backButton.setOnClickListener {
            finish()  // Regresa a la actividad anterior
        }
    }

    private fun loadUserProfile(userId: String) {
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.data
                    userData?.let {
                        nameEditText.setText(it["nombre"] as String?)
                        surnameEditText.setText(it["apellido"] as String?)
                        addressEditText.setText(it["direccion"] as String?)
                        emailEditText.setText(it["email"] as String?)
                        passwordEditText.setText(it["contraseña"] as String?)

                        // Deshabilitar la edición del email si estamos editando el perfil
                        emailEditText.isFocusable = false
                        emailEditText.isClickable = false
                        emailEditText.isCursorVisible = false
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al cargar el perfil: ${e.message}")
            }
    }

    private fun updateUserProfile(email: String, password: String) {
        val user = auth.currentUser
        if (user != null) {
            val userData: MutableMap<String, Any> = hashMapOf(
                "nombre" to nameEditText.text.toString().trim(),
                "apellido" to surnameEditText.text.toString().trim(),
                "direccion" to addressEditText.text.toString().trim(),
                "email" to email,
                "contraseña" to passwordEditText.text.toString().trim()
            )

            db.collection("usuarios").document(user.uid)
                .update(userData)
                .addOnSuccessListener {
                    showToast("Perfil actualizado con éxito")
                    goToMainPanel()
                }
                .addOnFailureListener { e ->
                    showToast("Error al actualizar el perfil: ${e.message}")
                }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        if (imageUri != null) {
                            uploadImageAndSaveProfile(imageUri!!)
                        } else {
                            saveUserData(user.uid, email, password)
                        }
                    }
                } else {
                    showToast("Error al registrar: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserData(uid: String, email: String, password: String) {
        val userData = hashMapOf(
            "nombre" to nameEditText.text.toString().trim(),
            "apellido" to surnameEditText.text.toString().trim(),
            "direccion" to addressEditText.text.toString().trim(),
            "email" to email,
            "contraseña" to password
        )

        db.collection("usuarios").document(uid)
            .set(userData)
            .addOnSuccessListener {
                showToast("Registro exitoso")
                goToMainPanel()
            }
            .addOnFailureListener { e ->
                showToast("Error al guardar: ${e.message}")
            }
    }

    private fun uploadImageAndSaveProfile(imageUri: Uri) {
        val storageRef = storage.reference
        val user = auth.currentUser

        // Crear una referencia única para la imagen
        val imageRef = storageRef.child("profile_images/${user?.uid}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveUserData(user!!.uid, emailEditText.text.toString().trim(), passwordEditText.text.toString().trim(), imageUrl)
                }
            }
            .addOnFailureListener { e ->
                showToast("Error al subir la imagen: ${e.message}")
            }
    }

    private fun saveUserData(uid: String, email: String, password: String, imageUrl: String? = null) {
        val userData: MutableMap<String, Any> = hashMapOf(
            "nombre" to nameEditText.text.toString().trim(),
            "apellido" to surnameEditText.text.toString().trim(),
            "direccion" to addressEditText.text.toString().trim(),
            "email" to email,
            "contraseña" to password
        )

        // Si hay imagen, agregar URL de la imagen al mapa
        imageUrl?.let { userData["profileImageUrl"] = it }

        db.collection("usuarios").document(uid)
            .set(userData)
            .addOnSuccessListener {
                showToast("Perfil actualizado con éxito")
                goToMainPanel()
            }
            .addOnFailureListener { e ->
                showToast("Error al guardar los datos: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun goToMainPanel() {
        val intent = Intent(this, MainPanelActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Manejo de la selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            imageUri = data?.data
            profileImageView.setImageURI(imageUri) // Mostrar la imagen seleccionada
        }
    }

    companion object {
        const val IMAGE_PICK_CODE = 1000 // Código para seleccionar imagen
    }
}
