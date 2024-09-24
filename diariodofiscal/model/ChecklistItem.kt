package com.example.diariodofiscal.model

import java.util.Date

data class ChecklistItem(
    var id: String = "",
    var text: String = "",
    var status: String = "",
    var observacoes: String = "",
    var dataMarcacao: Date? = null,
) {
    // Construtor sem argumentos
    constructor() : this("", "", "", "", null)
}
