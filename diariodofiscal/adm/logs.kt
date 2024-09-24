package com.example.diariodofiscal.adm

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.diariodofiscal.R
import com.google.firebase.firestore.Query

class logs : AppCompatActivity() {

    private lateinit var containerLogs: LinearLayout
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        containerLogs = findViewById(R.id.container_logs)

        // Obter os logs do Firestore e exibi-los
        fetchAndDisplayLogs()
    }

    private fun fetchAndDisplayLogs() {
        // Consulta para obter os logs do Firestore, ordenados pela data e hora
        db.collection("logs")
            .orderBy("data", Query.Direction.DESCENDING)
            .orderBy("hora", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userEmail = document.getString("usuario")
                    val alteration = document.getString("alteracao")
                    val data = document.getString("data")
                    val hora = document.getString("hora")
                    val condominio = document.getString("condominio")
                    val quadra = document.getString("quadra")
                    val lote = document.getString("lote")

                    // Crie um TextView para exibir cada log
                    val logTextView = TextView(this)
                    val logText = "Usuário: $userEmail\nAlteração: $alteration\nData: $data\nHora: $hora\nCondomínio: $condominio\nQuadra: $quadra\nLote: $lote\n\n"
                    logTextView.text = logText

                    // Adicione o TextView ao layout
                    containerLogs.addView(logTextView)
                }
            }
            .addOnFailureListener { exception ->
                // Em caso de falha ao recuperar os logs
                // Exemplo de tratamento de erro: exibir uma mensagem de erro
                val errorTextView = TextView(this)
                errorTextView.text = "Erro ao recuperar os logs: $exception"
                containerLogs.addView(errorTextView)
            }
    }

}
