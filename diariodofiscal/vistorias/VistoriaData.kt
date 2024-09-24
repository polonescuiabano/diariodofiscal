package com.example.diariodofiscal.vistorias

import java.util.*
import android.util.Log



data class VistoriaData(
    val dataVistoria: String,
    val dataProximaVistoria: String,
    val nomeFiscal: String,
    val imagemUrls: ArrayList<String>,
    var fileUris: Any,
    val comentarioFiscal: String,
    var id: String


) : Comparable<VistoriaData> {


    constructor() : this("", "", "", ArrayList(), "", "", "")

    override fun compareTo(other: VistoriaData): Int {
        val dateFormat = "dd/MM/yyyy"
        val sdf = java.text.SimpleDateFormat(dateFormat, Locale.getDefault())

        val date1 = sdf.parse(dataVistoria)
        val date2 = sdf.parse(other.dataVistoria)

        return date1.compareTo(date2)
    }
    fun getFileUrisList(): List<String> {
        val uriList = mutableListOf<String>()

        return when (val uris = fileUris) {
            is String -> {
                uriList.add(uris)
                Log.d("VistoriaData", "URI: $uris")
                uriList.toList()
            }
            is List<*> -> {
                for (item in uris) {
                    if (item is String) {
                        uriList.add(item)
                        Log.d("VistoriaData", "URI: $item")
                    }
                }
                uriList.toList()
            }
            else -> {
                Log.d("VistoriaData", "Empty URI list")
                emptyList()
            }
        }
    }
}



