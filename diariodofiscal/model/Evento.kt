package com.example.diariodofiscal.model

import java.util.*

data class Evento(
    val id: String, // Identificador único do evento
    val titulo: String,
    val descricao: String,
    val data: Date // Data do evento
)
