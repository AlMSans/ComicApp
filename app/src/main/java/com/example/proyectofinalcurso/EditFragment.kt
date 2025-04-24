package com.example.proyectofinalcurso

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class EditFragment : Fragment() {

    private lateinit var comic: Comic
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etLocation: EditText
    private lateinit var spinnerCondition: Spinner
    private lateinit var spinnerGenre: Spinner
    private lateinit var etPrice: EditText
    private lateinit var ivComicImage: ImageView
    private lateinit var btnUploadImage: Button
    private lateinit var btnSaveComic: Button

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    private val PICK_IMAGE_REQUEST = 71
    private var imageUris: MutableList<Uri> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        // Obtener el cómic a editar desde los argumentos
        arguments?.let {
            comic = it.getParcelable("comic")!!
        }

        // Inicializar vistas
        etTitle = view.findViewById(R.id.etComicTitle)
        etAuthor = view.findViewById(R.id.etComicAuthor)
        etLocation = view.findViewById(R.id.etComicLocation)
        spinnerCondition = view.findViewById(R.id.spinnerComicCondition)
        spinnerGenre = view.findViewById(R.id.spinnerComicGenre)
        etPrice = view.findViewById(R.id.etComicPrice)
        ivComicImage = view.findViewById(R.id.ivComicImage)
        btnUploadImage = view.findViewById(R.id.btnUploadImage)
        btnSaveComic = view.findViewById(R.id.btnSaveComic)

        // Rellenar los campos con los datos del cómic
        etTitle.setText(comic.title)
        etAuthor.setText(comic.author)
        etLocation.setText(comic.location)
        etPrice.setText(comic.price.toString())

        // Usar Glide para cargar la primera imagen del cómic
        Glide.with(this)
            .load(comic.imageUrl) // Si el cómic tiene una URL de imagen, la carga
            .into(ivComicImage)

        // Configurar el spinner de condición y género
        val conditions = arrayOf("Nuevo", "Usado", "Deteriorado")
        val genres = arrayOf("Acción", "Aventura", "Ciencia Ficción", "Misterio", "Comedia", "Superhéroes", "Policiaco")

        val conditionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, conditions)
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCondition.adapter = conditionAdapter

        val genreAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genres)
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGenre.adapter = genreAdapter

        // Establecer los valores seleccionados de los spinner
        spinnerCondition.setSelection(conditions.indexOf(comic.condition))
        spinnerGenre.setSelection(genres.indexOf(comic.genre))

        // Configurar el botón de subir imagen
        btnUploadImage.setOnClickListener {
            openImageChooser() // Abrir el selector de imágenes
        }

        // Configurar el botón de guardar
        btnSaveComic.setOnClickListener {
            saveComicChanges()
        }

        return view
    }

    // Método para abrir el selector de imágenes para permitir selección múltiple
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)  // Permitir selección múltiple
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Manejar el resultado de la selección de imágenes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            imageUris.clear() // Limpiar lista previa de imágenes seleccionadas

            data?.let {
                if (it.clipData != null) {
                    // Seleccionaron múltiples imágenes
                    val count = it.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = it.clipData!!.getItemAt(i).uri
                        imageUris.add(imageUri)
                    }
                } else {
                    // Solo seleccionaron una imagen
                    it.data?.let { uri ->
                        imageUris.add(uri)
                    }
                }
            }

            // Si hay imágenes seleccionadas, mostrar la primera (o una miniatura)
            if (imageUris.isNotEmpty()) {
                ivComicImage.setImageURI(imageUris[0])
            }
        }
    }

    private fun saveComicChanges() {
        val title = etTitle.text.toString()
        val author = etAuthor.text.toString()
        val location = etLocation.text.toString()
        val condition = spinnerCondition.selectedItem.toString()
        val genre = spinnerGenre.selectedItem.toString()
        val priceString = etPrice.text.toString()

        val price = priceString.toFloatOrNull()
        if (title.isEmpty() || author.isEmpty() || location.isEmpty() || price == null || price <= 0f) {
            Toast.makeText(context, "Por favor, complete todos los campos correctamente.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedComic = Comic(
            id = comic.id,
            title = title,
            author = author,
            genre = genre,
            price = price,
            condition = condition,
            location = location,
            userId = user?.uid ?: "",
            imageUrls = imageUris.map { it.toString() } // Convertir las URIs en Strings
        )

        if (imageUris.isNotEmpty()) {
            uploadImagesToFirebase(updatedComic)
        } else {
            saveComicToFirestore(updatedComic)
        }
    }

    // Subir múltiples imágenes a Firebase Storage
    private fun uploadImagesToFirebase(comic: Comic) {
        val imageUrls = mutableListOf<String>()
        val storageRef = storage.reference

        imageUris.forEachIndexed { index, uri ->
            val imageRef = storageRef.child("comic_images/${UUID.randomUUID()}.jpg")
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrls.add(downloadUri.toString())
                        if (imageUrls.size == imageUris.size) {
                            comic.imageUrls = imageUrls // Asignar las URLs de todas las imágenes al cómic
                            saveComicToFirestore(comic)
                        }
                    }
                }
        }
    }

    // Guardar el cómic actualizado en Firestore
    private fun saveComicToFirestore(comic: Comic) {
        user?.let {
            db.collection("comics").document(comic.id).set(comic)
                .addOnSuccessListener {
                    Toast.makeText(context, "Cómic actualizado correctamente.", Toast.LENGTH_SHORT).show()
                    activity?.onBackPressed() // Volver al fragmento anterior
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al actualizar el cómic.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
