package com.example.diariodofiscal.agenda

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.example.diariodofiscal.R
import com.google.firebase.firestore.Query

class historico : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var condominio: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico)

        db = FirebaseFirestore.getInstance()
        condominio = intent.getStringExtra("condominio").toString()

        loadDiary()
    }

    private fun loadDiary() {
        db.collection("Condominios")
            .document(condominio)
            .collection("diario_do_fiscal")
            .orderBy("date", Query.Direction.DESCENDING) // Ordena os documentos pela data em ordem decrescente
            .get()
            .addOnSuccessListener { documents ->
                displayDiaryEntries(documents)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao carregar o diário: $exception", Toast.LENGTH_SHORT).show()
            }
    }


    private fun displayDiaryEntries(documents: QuerySnapshot) {
        val container: LinearLayout = findViewById(R.id.container)

        for (document in documents) {
            val eventType = document.getString("eventType")
            val date = document.getString("date")
            val time = document.getString("time")
            val observations = document.getString("observations")
            val fiscalName = document.getString("fiscalName")

            val textView = TextView(this)
            textView.text = "Tipo de Evento: $eventType\nData: $date\nHora: $time\nObservações: $observations\nFiscal: $fiscalName\n"

            // Adiciona uma linha separadora entre os documentos
            val separator = TextView(this)
            separator.text = "------------------------------------------"

            container.addView(textView)
            container.addView(separator)

            // Verifica se há a coleção "files" dentro do documento
            val files = document.get("files") as? ArrayList<*>
            if (files != null) {
                for (fileUrl in files) {
                    val fileTextView = TextView(this)
                    fileTextView.text = "Arquivo: $fileUrl"
                    container.addView(fileTextView)
                }
            }
        }
    }
}
