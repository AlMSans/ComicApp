package com.example.proyectofinalcurso

import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SearchFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var comicsAdapter: ListUsuComics
    private lateinit var searchEditText: EditText
    private lateinit var genreSpinner: Spinner
    private lateinit var statusSpinner: Spinner
    private lateinit var priceSeekBar: SeekBar
    private lateinit var priceTextView: TextView
    private lateinit var locationEditText: EditText
    private lateinit var noResultsTextView: TextView
    private lateinit var btnSearch: Button
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        genreSpinner = view.findViewById(R.id.genreSpinner)
        statusSpinner = view.findViewById(R.id.statusSpinner)
        priceSeekBar = view.findViewById(R.id.priceSeekBar)
        priceTextView = view.findViewById(R.id.priceTextView)
        locationEditText = view.findViewById(R.id.locationEditText)
        noResultsTextView = view.findViewById(R.id.noResultsTextView)
        btnSearch = view.findViewById(R.id.btnSearch)
        recyclerView = view.findViewById(R.id.recyclerViewSearchResults)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        auth = FirebaseAuth.getInstance()

        setupSpinners()
        setupPriceSeekBar()

        // Configurar el clic en los cómics
        val onComicClick: (Comic) -> Unit = { comic ->
            val fragment = ComicDetailFragment.newInstance(
                comic.id,
                comic.title,
                comic.author,
                comic.genre,
                comic.location,
                comic.condition,
                comic.price,
                comic.imageUrl ?: "", // Si imageUrl es null, se pasa un string vacío
                comic.userId
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)  // Ajusta el contenedor de acuerdo a tu estructura
                .addToBackStack(null)
                .commit()
        }




        // Inicializar el adaptador con el clic
        comicsAdapter = ListUsuComics(emptyList(), onComicClick)
        recyclerView.adapter = comicsAdapter

        btnSearch.setOnClickListener {
            applyFilters()
        }

        return view
    }

    private fun setupSpinners() {
        val genres = arrayOf("Selecciona Género", "Acción", "Aventura", "Ciencia Ficción", "Fantasía", "Terror", "Superheroes", "Policiaco")
        val statuses = arrayOf("Selecciona Estado", "Nuevo", "Usado")

        // Crear adaptadores para el Spinner de Géneros y Estados
        val genreAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, genres)
        genreSpinner.adapter = genreAdapter
        genreSpinner.setSelection(0) // Establece el primer ítem como seleccionado por defecto (el título)

        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses)
        statusSpinner.adapter = statusAdapter
        statusSpinner.setSelection(0) // Establece el primer ítem como seleccionado por defecto (el título)
    }


    private fun setupPriceSeekBar() {
        priceSeekBar.max = 100
        priceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                priceTextView.text = "Máximo: €$progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }


    private fun applyFilters() {
        val searchText = searchEditText.text.toString().trim()
        val selectedGenre = genreSpinner.selectedItem.toString()
        val selectedStatus = statusSpinner.selectedItem.toString()
        val maxPrice = priceSeekBar.progress
        val location = locationEditText.text.toString().trim()

        // Obtener el UID del usuario actual
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        var query: Query = db.collection("comics")

        if (searchText.isNotEmpty()) {
            query = query.whereGreaterThanOrEqualTo("title", searchText)
                .whereLessThanOrEqualTo("title", searchText + "\uf8ff")
        }

        // Verificar si no se seleccionó el título, y aplicar el filtro solo si se seleccionó una opción distinta
        if (selectedGenre != "Selecciona Género") {
            query = query.whereEqualTo("genre", selectedGenre)
        }

        if (selectedStatus != "Selecciona Estado") {
            query = query.whereEqualTo("condition", selectedStatus)
        }

        if (location.isNotEmpty()) {
            query = query.whereEqualTo("location", location)
        }

        // Excluir los cómics del propio usuario
        if (currentUserId != null) {
            query = query.whereNotEqualTo("userId", currentUserId)
        }

        query.get().addOnSuccessListener { documents ->
            val filteredComics = documents.mapNotNull { document ->
                // Obtener el precio como número (Number)
                val priceValue = document.get("price") as? Number

                // Si priceValue es un número, lo convertimos a Float
                val price = priceValue?.toFloat() ?: 0f

                // Filtrar cómics según el precio máximo del SeekBar
                if (price <= maxPrice) {
                    Comic(
                        id = document.id,
                        title = document.getString("title") ?: "Sin título",
                        author = document.getString("author") ?: "Desconocido",
                        genre = document.getString("genre") ?: "Desconocido",
                        condition = document.getString("condition") ?: "Desconocido",
                        price = price,  // Guardamos el precio como Float
                        location = document.getString("location") ?: "Desconocido",
                        imageUrl = document.getString("imageUrl") ?: "",
                        userId = document.getString("userId") ?: ""  // Aquí agregamos el userId
                    )
                } else {
                    null  // Si el precio es mayor que el valor del SeekBar, no se incluye el cómic
                }
            }

            // Actualizar la lista de cómics en el adaptador
            comicsAdapter.updateData(filteredComics)

            // Si no hay resultados, muestra el mensaje
            if (filteredComics.isEmpty()) {
                noResultsTextView.visibility = View.VISIBLE
            } else {
                noResultsTextView.visibility = View.GONE
            }
        }

    }




}


