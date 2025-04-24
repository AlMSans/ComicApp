package com.example.proyectofinalcurso

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinalcurso.data.toComic       // mapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

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
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false).apply {

        searchEditText     = findViewById(R.id.searchEditText)
        genreSpinner       = findViewById(R.id.genreSpinner)
        statusSpinner      = findViewById(R.id.statusSpinner)
        priceSeekBar       = findViewById(R.id.priceSeekBar)
        priceTextView      = findViewById(R.id.priceTextView)
        locationEditText   = findViewById(R.id.locationEditText)
        noResultsTextView  = findViewById(R.id.noResultsTextView)
        btnSearch          = findViewById(R.id.btnSearch)

        recyclerView = findViewById(R.id.recyclerViewSearchResults)
        recyclerView.layoutManager = LinearLayoutManager(context)

        setupSpinners()
        setupPriceSeekBar()

        comicsAdapter = ListUsuComics(emptyList()) { abrirDetallesComic(it) }
        recyclerView.adapter = comicsAdapter

        btnSearch.setOnClickListener { applyFilters() }
    }

    /* ---------- abrir detalle ---------- */

    private fun abrirDetallesComic(comic: Comic) {
        val urls = ArrayList(comic.imageUrls)
        comic.imageUrl?.let { if (urls.isEmpty()) urls.add(it) }  // compat.

        val fragment = ComicDetailFragment.newInstance(
            comic.id, comic.title, comic.author, comic.genre,
            comic.location, comic.condition, comic.price, urls, comic.userId
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /* ---------- filtros UI  ---------- */

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

    /* ---------- aplicar filtros ---------- */

    private fun applyFilters() {
        val searchText     = searchEditText.text.toString().trim()

        // Asegurarse de que el valor no sea nulo o vacío antes de llamar a toString()
        val selectedGenre  = genreSpinner.selectedItem?.toString() ?: "Selecciona Género"
        val selectedStatus = statusSpinner.selectedItem?.toString() ?: "Selecciona Estado"

        val maxPrice       = priceSeekBar.progress
        val location       = locationEditText.text.toString().trim()
        val currentUserId  = auth.currentUser?.uid

        var query: Query = db.collection("comics")

        if (searchText.isNotEmpty()) {
            query = query.whereGreaterThanOrEqualTo("title", searchText)
                .whereLessThanOrEqualTo("title", searchText + "\uf8ff")
        }

        // Aplicar filtros solo si los valores seleccionados son distintos de los predeterminados
        if (selectedGenre != "Selecciona Género") query = query.whereEqualTo("genre", selectedGenre)
        if (selectedStatus != "Selecciona Estado") query = query.whereEqualTo("condition", selectedStatus)
        if (location.isNotEmpty())                 query = query.whereEqualTo("location", location)

        currentUserId?.let { query = query.whereNotEqualTo("userId", it) }

        query.get().addOnSuccessListener { docs ->
            val comics = docs.mapNotNull { snap ->
                val comic = snap.toComic()
                if (comic.price <= maxPrice) comic else null
            }

            comicsAdapter.updateData(comics)
            noResultsTextView.visibility = if (comics.isEmpty()) View.VISIBLE else View.GONE
        }
    }

}
