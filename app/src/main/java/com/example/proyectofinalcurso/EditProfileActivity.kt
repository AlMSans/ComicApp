package com.example.proyectofinalcurso

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var selectImageButton: Button
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
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Inicializamos Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Referencias de vistas
        profileImageView = findViewById(R.id.profileImageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        nameEditText = findViewById(R.id.nameEditText)
        surnameEditText = findViewById(R.id.surnameEditText)
        addressEditText = findViewById(R.id.addressEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Cargar datos actuales
        loadUserData()

        // Botón seleccionar imagen
        selectImageButton.setOnClickListener {
            pickImageFromGallery()
        }

        // Botón guardar cambios
        saveButton.setOnClickListener {
            saveProfileChanges()
        }

        // Botón volver (confirmación)
        backButton.setOnClickListener {
            showExitConfirmation()
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        user?.let {
            val uid = user.uid
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        nameEditText.setText(document.getString("nombre"))
                        surnameEditText.setText(document.getString("apellido"))
                        addressEditText.setText(document.getString("direccion"))
                        emailEditText.setText(document.getString("email"))

                        // Hacer que el email sea visible pero no editable
                        emailEditText.isEnabled = false

                        // Cargar imagen de perfil si existe
                        val imageRef = storage.reference.child("profile_images/$uid.jpg")
                        imageRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                Glide.with(this)
                                    .load(uri)
                                    .placeholder(R.drawable.hb1)
                                    .into(profileImageView)
                            }
                            .addOnFailureListener {
                                // No hacemos nada especial si no encuentra imagen
                            }
                    }
                }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            profileImageView.setImageURI(selectedImageUri)
        }
    }

    private fun saveProfileChanges() {
        val name = nameEditText.text.toString().trim()
        val surname = surnameEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        val user = auth.currentUser

        if (user != null) {
            val uid = user.uid
            val userData = mutableMapOf<String, Any>()

            if (name.isNotEmpty()) userData["nombre"] = name
            if (surname.isNotEmpty()) userData["apellido"] = surname
            if (address.isNotEmpty()) userData["direccion"] = address

            db.collection("usuarios").document(uid)
                .update(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    if (password.isNotEmpty()) {
                        updatePassword(user, password)
                    }
                    uploadProfileImage(uid)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updatePassword(user: FirebaseUser, newPassword: String) {
        user.updatePassword(newPassword)
            .addOnSuccessListener {
                Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cambiar contraseña: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfileImage(uid: String) {
        selectedImageUri?.let { uri ->
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Una vez subida, obtenemos la URL de descarga
                        val imageUrl = downloadUri.toString()

                        // Actualizamos el documento del usuario en Firestore
                        db.collection("usuarios").document(uid)
                            .update("profileImageUrl", imageUrl)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Imagen subida y URL guardada", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar URL en Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Salir sin guardar")
            .setMessage("¿Estás seguro de que quieres salir sin guardar los cambios?")
            .setPositiveButton("Sí") { dialog, which ->
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
