package com.example.diariodofiscal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Arquivo(
    @PrimaryKey
    val id: String, // Mantendo o tipo de dado como String
    val fileName: String,
    val fileUrl: String,
    var isSelected: Boolean = false // Propriedade para controlar o estado de seleção
)





