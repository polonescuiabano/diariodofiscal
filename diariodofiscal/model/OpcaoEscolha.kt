package com.example.diariodofiscal.model

enum class OpcaoEscolha {
    REGULAR,
    IRREGULAR,
    NAO_CONSTA,
    EM_BRANCO;

    companion object {
        fun fromString(value: String): OpcaoEscolha {
            return when (value.trim().toUpperCase()) {
                "REGULAR" -> REGULAR
                "IRREGULAR" -> IRREGULAR
                "NÃO CONSTA" -> NAO_CONSTA
                else -> EM_BRANCO
            }
        }
    }
}

