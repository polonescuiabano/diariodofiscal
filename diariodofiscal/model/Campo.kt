package com.example.diariodofiscal.model

import java.text.SimpleDateFormat
import java.util.*


data class Campo(
    var nome: String = "",
    var dataMarcacao: String? = null,
    var opcaoEscolhida: OpcaoEscolha? = null,
    var observacoes: String? = null,

) {

    var data: Date = Date() // Data padrão é a data atual

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(data)
    }
}
