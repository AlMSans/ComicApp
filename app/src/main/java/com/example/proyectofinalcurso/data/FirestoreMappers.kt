package com.example.proyectofinalcurso.data   // usa tu paquete raíz + .data

import com.example.proyectofinalcurso.Comic
import com.google.firebase.firestore.DocumentSnapshot

/** Convierte un DocumentSnapshot en un objeto Comic, compat. imageUrl + imageUrls */
fun DocumentSnapshot.toComic(): Comic {
    val list   = get("imageUrls") as? List<String>
    val legacy = getString("imageUrl")
    val urls   = list ?: legacy?.let { listOf(it) } ?: emptyList()

    return Comic(
        id        = id,
        title     = getString("title") ?: "Sin título",
        author    = getString("author") ?: "Desconocido",
        genre     = getString("genre") ?: "Desconocido",
        price     = (get("price") as? Number)?.toFloat() ?: 0f,
        condition = getString("condition") ?: "Desconocido",
        location  = getString("location") ?: "Desconocida",
        userId    = getString("userId") ?: "",
        imageUrls = urls
    )
}
