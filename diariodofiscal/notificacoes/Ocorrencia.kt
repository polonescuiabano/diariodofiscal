package com.example.diariodofiscal.notificacoes

import java.util.Date

data class Ocorrencia(
    val numeroOcorrencia: String, // Campo para armazenar o número da ocorrência
    val id: String,
    val descricao: String,
    val data: String // Supondo que a data seja uma string formatada
)

interface OcorrenciaClickListener {
    fun onOcorrenciaClick(ocorrencia: Ocorrencia)
}