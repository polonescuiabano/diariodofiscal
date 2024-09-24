package com.example.diariodofiscal.notificacoes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.example.diariodofiscal.R
import com.example.diariodofiscal.adapters.OcorrenciasAdapter
import com.google.firebase.firestore.Query


class notificacoes : AppCompatActivity(), OcorrenciasAdapter.OcorrenciaClickListener {
    private lateinit var condominio: String
    private lateinit var obraId: String
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificacoes)

        // Recuperar os extras da intent
        condominio = intent.getStringExtra("condominio") ?: ""
        obraId = intent.getStringExtra("obraId") ?: ""
        firestore = FirebaseFirestore.getInstance()

        // Referência ao layout onde as ocorrências serão exibidas
        val recyclerViewOcorrencias = findViewById<RecyclerView>(R.id.recyclerViewOcorrencias)

        // Definir um LinearLayoutManager para o RecyclerView
        recyclerViewOcorrencias.layoutManager = LinearLayoutManager(this)

        // Recuperar todas as ocorrências da obra específica do Firestore
        // Recuperar todas as ocorrências da obra específica do Firestore, ordenadas por númeroOcorrencia em ordem decrescente
        firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("Ocorrencias")
            .orderBy("numeroOcorrencia", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val ocorrenciasList = mutableListOf<Ocorrencia>()
                for (document in documents) {
                    val numeroOcorrencia: String = document.getString("numeroOcorrencia") ?: ""
                    val descricao: String = document.getString("descricao") ?: ""
                    val data: String = document.getString("data") ?: ""
                    val id: String = document.id // Obtém o ID do documento
                    val ocorrencia = Ocorrencia(numeroOcorrencia, id, descricao, data)
                    ocorrenciasList.add(ocorrencia)
                }

                // Criar um adapter para o RecyclerView e definir os dados
                val adapter = OcorrenciasAdapter(ocorrenciasList, this)
                recyclerViewOcorrencias.adapter = adapter
            }
            .addOnFailureListener { exception ->
                // Lidar com erros ao recuperar as ocorrências
                Log.e("notificacoes", "Falha ao recuperar as ocorrências: $exception")
            }

        // Configurar o clique no botão para adicionar uma nova ocorrência
        val btnOptions = findViewById<ImageButton>(R.id.btnOptions)
        btnOptions.setOnClickListener {
            val intent = Intent(this, com.example.diariodofiscal.addocorrencia::class.java).apply {
                putExtra("condominio", condominio)
                putExtra("obraId", obraId)
            }
            startActivity(intent)
        }
    }

    // Corrija a função onOcorrenciaClick em notificacoes.kt para usar o ID da ocorrência
    override fun onOcorrenciaClick(ocorrencia: Ocorrencia) {

        Log.d("notificacoes", "ID da ocorrência: ${ocorrencia.id}")

        // Lidar com o clique na ocorrência
        val intent = Intent(this, detalhesocorrencia::class.java).apply {
            putExtra("condominio", condominio)
            putExtra("obraId", obraId)
            putExtra("ocorrenciaId", ocorrencia.id)
        }
        startActivity(intent)
    }
}
