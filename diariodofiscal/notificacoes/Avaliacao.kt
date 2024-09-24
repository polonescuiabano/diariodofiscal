package com.example.diariodofiscal.notificacoes

import com.google.firebase.firestore.PropertyName

data class Avaliacao(
    @get:PropertyName("dataRetorno") @set:PropertyName("dataRetorno")
    var dataRetorno: String = "",
    @get:PropertyName("numerodaavaliacao") @set:PropertyName("numerodaavaliacao")
    var numeroDaAvaliacao: Int = 0,
    @get:PropertyName("observacoes") @set:PropertyName("observacoes")
    var observacoes: String = "",
    @get:PropertyName("fotosAnexadas") @set:PropertyName("fotosAnexadas")
    var fotosAnexadas: List<String>? = null
)
