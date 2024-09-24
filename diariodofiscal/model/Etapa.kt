package com.example.diariodofiscal.model

data class Etapa(
    var nome: String = "",
    var campos: List<Campo> = mutableListOf(),
    var mostrarCampos: Boolean = false
)