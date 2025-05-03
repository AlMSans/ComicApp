package com.example.proyectofinalcurso

import android.os.Parcel
import android.os.Parcelable

data class Comic(
    var id: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    var price: Float = 0f,
    val condition: String = "",
    val location: String = "",
    val userId: String = "",
    var imageUrls: List<String> = emptyList(),
    var rating: Float = 0f,
    @Deprecated("Usa imageUrls", ReplaceWith("imageUrls"))
    var imageUrl: String? = null
) : Parcelable {

    /* ----  Parcelable  ---- */

    private constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        title = parcel.readString() ?: "",
        author = parcel.readString() ?: "",
        genre = parcel.readString() ?: "",
        price = parcel.readFloat(),
        condition = parcel.readString() ?: "",
        location = parcel.readString() ?: "",
        userId = parcel.readString() ?: "",
        imageUrls = mutableListOf<String>().apply {
            parcel.readStringList(this)
        },
        rating = parcel.readFloat(),
        imageUrl = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(genre)
        parcel.writeFloat(price)
        parcel.writeString(condition)
        parcel.writeString(location)
        parcel.writeString(userId)
        parcel.writeStringList(imageUrls)
        parcel.writeFloat(rating) // Escribir la puntuación
        parcel.writeString(imageUrl)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Comic> {
        override fun createFromParcel(parcel: Parcel) = Comic(parcel)
        override fun newArray(size: Int): Array<Comic?> = arrayOfNulls(size)
    }
}
