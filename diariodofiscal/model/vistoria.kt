package com.example.diariodofiscal.model

import java.util.Date

data class vistoria(
    val id: String, // ID único da vistoria
    val dataVistoria: Date, // Data da vistoria
    val dataProximaVistoria: Date, // Data da próxima vistoria
    val nomeFiscal: String, // Nome do fiscal
    val comentarioFiscal: String, // Comentário do fiscal
    val imageUris: List<String> // Lista de URIs das imagens relacionadas à vistoria
)