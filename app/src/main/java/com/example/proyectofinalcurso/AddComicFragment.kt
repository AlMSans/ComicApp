package com.example.proyectofinalcurso

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageMetadata
import java.util.*

class AddComicFragment : Fragment() {

    private lateinit var etComicTitle: EditText
    private lateinit var etComicAuthor: EditText
    private lateinit var etComicLocation: EditText
    private lateinit var spinnerComicCondition: Spinner
    private lateinit var spinnerComicGenre: Spinner
    private lateinit var etComicPrice: EditText
    private lateinit var btnUploadImage: Button
    private lateinit var ivComicImage: ImageView
    private lateinit var btnSaveComic: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private var comicImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            comicImageUri = uri
            ivComicImage.setImageURI(uri)
        } else {
            Toast.makeText(requireContext(), "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_comic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        etComicTitle = view.findViewById(R.id.etComicTitle)
        etComicAuthor = view.findViewById(R.id.etComicAuthor)
        etComicLocation = view.findViewById(R.id.etComicLocation)
        spinnerComicCondition = view.findViewById(R.id.spinnerComicCondition)
        spinnerComicGenre = view.findViewById(R.id.spinnerComicGenre)
        etComicPrice = view.findViewById(R.id.etComicPrice)
        btnUploadImage = view.findViewById(R.id.btnUploadImage)
        ivComicImage = view.findViewById(R.id.ivComicImage)
        btnSaveComic = view.findViewById(R.id.btnSaveComic)
        progressBar = view.findViewById(R.id.progressBar)

        setupSpinners()

        btnUploadImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSaveComic.setOnClickListener {
            saveComicToFirebase()
        }
    }

    private fun setupSpinners() {
        val genres = arrayOf("Selecciona Género", "Acción", "Aventura", "Ciencia Ficción", "Fantasía", "Terror", "Superhéroes", "Policiaco")
        val conditions = arrayOf("Selecciona Estado", "Nuevo", "Usado")

        spinnerComicGenre.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genres)
        spinnerComicCondition.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, conditions)
    }

    private fun saveComicToFirebase() {
        val title = etComicTitle.text.toString().trim()
        val author = etComicAuthor.text.toString().trim()
        val location = etComicLocation.text.toString().trim()
        val condition = spinnerComicCondition.selectedItem.toString()
        val priceString = etComicPrice.text.toString().trim()
        val genre = spinnerComicGenre.selectedItem.toString()

        if (title.isEmpty() || author.isEmpty() || location.isEmpty() ||
            condition == "Selecciona Estado" || priceString.isEmpty() || genre == "Selecciona Género") {
            Toast.makeText(requireContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val price = priceString.toFloatOrNull() ?: run {
            Toast.makeText(requireContext(), "Por favor ingrese un precio válido", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        if (comicImageUri != null) {
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(comicImageUri!!)
                if (inputStream == null) {
                    throw Exception("No se pudo abrir el InputStream de la URI.")
                }

                val fileName = "comic_${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child("comics/$fileName")

                val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()

                val imageData = inputStream.readBytes()
                inputStream.close()

                Log.d("UploadDebug", "Subiendo imagen como byte array: URI=$comicImageUri")

                storageRef.putBytes(imageData, metadata)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        storageRef.downloadUrl
                    }
                    .addOnSuccessListener { uri ->
                        val comicData = mapOf(
                            "title" to title,
                            "author" to author,
                            "location" to location,
                            "condition" to condition,
                            "price" to price,
                            "genre" to genre,
                            "imageUrl" to uri.toString(),
                            "userId" to userId,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        saveComicDataToFirestore(comicData)
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        Log.e("AddComicFragment", "Error al subir la imagen desde bytes", e)
                        Toast.makeText(requireContext(), "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("AddComicFragment", "No se pudo acceder a la imagen", e)
                Toast.makeText(requireContext(), "No se pudo acceder a la imagen seleccionada", Toast.LENGTH_SHORT).show()
            }

        } else {
            val comicData = mapOf(
                "title" to title,
                "author" to author,
                "location" to location,
                "condition" to condition,
                "price" to price,
                "genre" to genre,
                "userId" to userId,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            saveComicDataToFirestore(comicData)
        }
    }



    private fun saveComicDataToFirestore(comicData: Map<String, Any>) {
        db.collection("comics")
            .add(comicData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Cómic guardado correctamente", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al guardar el cómic: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }

}
