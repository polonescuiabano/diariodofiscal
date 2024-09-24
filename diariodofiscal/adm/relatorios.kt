package com.example.diariodofiscal.adm

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.example.diariodofiscal.R
import com.example.diariodofiscal.adapters.RelatoriosAdapter

class RelatoriosActivity : AppCompatActivity() {
    private lateinit var spinnerFiscal: Spinner
    private lateinit var recyclerViewRelatorios: RecyclerView
    private lateinit var adapter: RelatoriosAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        spinnerFiscal = findViewById(R.id.spinnerFiscal)
        recyclerViewRelatorios = findViewById(R.id.recyclerViewRelatorios)

        val fiscais = listOf("Fernando Barros", "Gessica Poloni", "Hullean Firmino", "Dyelson Tavares")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fiscais)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFiscal.adapter = spinnerAdapter

        adapter = RelatoriosAdapter(emptyList())
        recyclerViewRelatorios.layoutManager = LinearLayoutManager(this)
        recyclerViewRelatorios.adapter = adapter

        spinnerFiscal.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val fiscalSelecionado = fiscais[position]
                carregarRelatoriosDoFiscal(fiscalSelecionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Não é necessário implementar nada aqui
            }
        }
    }

    private fun carregarRelatoriosDoFiscal(fiscalSelecionado: String) {
        val relatorios = mutableListOf<Relatorio>()

        val condominios = listOf(
            "Florais Italia",
            "Florais Cuiabá",
            "Belvedere",
            "Belvedere 2",
            "Alphaville 2",
            "Primor das Torres",
            "Villa Jardim"
        )

        for (condominio in condominios) {
            db.collection("Condominios").document(condominio).collection("Obras")
                .get()
                .addOnSuccessListener { obras ->
                    obras.forEach { obra ->
                        obra.reference.collection("vistorias")
                            .whereEqualTo("nomeFiscal", fiscalSelecionado)
                            .get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {
                                    val condominio = document.getString("condominio") ?: ""
                                    val data = document.getString("data") ?: ""
                                    val local = document.getString("local") ?: ""
                                    Log.d("Relatorios", "Condomínio: $condominio, Data: $data, Local: $local")

                                    val relatorio = Relatorio(
                                        condominio,
                                        data,
                                        local
                                    )
                                    relatorios.add(relatorio)
                                }
                                if (condominio == condominios.last() && obra.id == obras.last().id) {
                                    exibirRelatorios(relatorios)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Relatorios", "Erro ao obter vistorias da obra ${obra.id} do condomínio $condominio", exception)
                                // Tratar o erro adequadamente
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Relatorios", "Erro ao obter obras do condomínio $condominio", exception)
                    // Tratar o erro adequadamente
                }
        }
    }

    private fun exibirRelatorios(relatorios: List<Relatorio>) {
        adapter.atualizarRelatorios(relatorios)
    }
}

    data class Relatorio(val condominio: String, val data: String, val local: String)

