package com.example.diariodofiscal.model

import android.os.Parcel
import android.os.Parcelable

data class DetalhesObra(
    val quadra: String?,
    val lote: String?,
    val nomeProprietario: String?,
    val email: String?,
    val telefone: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(quadra)
        parcel.writeString(lote)
        parcel.writeString(nomeProprietario)
        parcel.writeString(email)
        parcel.writeString(telefone)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DetalhesObra> {
        override fun createFromParcel(parcel: Parcel): DetalhesObra {
            return DetalhesObra(parcel)
        }

        override fun newArray(size: Int): Array<DetalhesObra?> {
            return arrayOfNulls(size)
        }
    }
}
