package com.example.proyectofinalcurso

import android.os.Parcel
import android.os.Parcelable

data class Comic(
    val id: String,
    val title: String,
    val author: String,
    val genre: String,
    val price: Float,
    val condition: String,
    val location: String,
    val userId: String,
    var imageUrl: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: null
    )

    // Métodos de Parcelable
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(genre)
        parcel.writeFloat(price)
        parcel.writeString(condition)
        parcel.writeString(location)
        parcel.writeString(userId)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Comic> = object : Parcelable.Creator<Comic> {
            override fun createFromParcel(parcel: Parcel): Comic {
                return Comic(parcel)
            }

            override fun newArray(size: Int): Array<Comic?> {
                return arrayOfNulls(size)
            }
        }
    }
}

