package com.example.diariodofiscal.vistorias

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.adapters.VistoriasAdapter
import com.example.diariodofiscal.addvistoria.AddVistoria
import com.example.diariodofiscal.atividades.DetalhesVistoria
import com.example.diariodofiscal.vistorias.VistoriaData
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Vistoria : AppCompatActivity() {

    private lateinit var obraId: String
    private lateinit var vistoriasRecyclerView: RecyclerView
    private lateinit var addVistoriaButton: Button
    private lateinit var vistoriasAdapter: VistoriasAdapter
    private lateinit var condominio: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vistoria)

        obraId = intent.getStringExtra("obraId") ?: ""
        condominio = intent.getStringExtra("condominio") ?: ""

        if (obraId.isEmpty() || condominio.isEmpty()) {
            Toast.makeText(this, "ID da obra ou nome do condomínio não estão disponíveis", Toast.LENGTH_SHORT).show()
            finish()
        }

        vistoriasRecyclerView = findViewById(R.id.vistoriasRecyclerView)
        addVistoriaButton = findViewById(R.id.addvisto)

        vistoriasAdapter = VistoriasAdapter(this, mutableListOf(),
            onItemClick = { vistoria ->
                val intent = Intent(this, DetalhesVistoria::class.java)
                intent.putExtra("dataVistoria", vistoria.dataVistoria)
                intent.putExtra("dataProximaVistoria", vistoria.dataProximaVistoria)
                intent.putExtra("nomeFiscal", vistoria.nomeFiscal)
                intent.putStringArrayListExtra("imagemUrls", vistoria.imagemUrls)
                intent.putExtra("comentarioFiscal", vistoria.comentarioFiscal)
                val fileUrisList = vistoria.fileUris.toString().split(",") // Dividindo a string em uma lista de strings
                intent.putStringArrayListExtra("fileUris", ArrayList(fileUrisList))
                startActivity(intent)
            },
            onDeleteClick = { vistoria ->
                onDeleteVistoria(vistoria)
            }
        )


        vistoriasRecyclerView.adapter = vistoriasAdapter
        vistoriasRecyclerView.layoutManager = LinearLayoutManager(this)

        exibirVistorias()

        addVistoriaButton.setOnClickListener {
            val intent = Intent(this, AddVistoria::class.java)
            intent.putExtra("obraId", obraId)
            intent.putExtra("condominio", condominio)
            startActivity(intent)
        }
    }

    private fun exibirVistorias() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("vistorias")
            .get()
            .addOnSuccessListener { documents ->
                val vistoriasList = mutableListOf<VistoriaData>()
                for (document in documents) {
                    val dataVistoria = document.getString("dataVistoria") ?: ""
                    val dataProximaVistoria = document.getString("dataProximaVistoria") ?: ""
                    val nomeFiscal = document.getString("nomeFiscal") ?: ""
                    val imagemUrls = document.get("imagemUrls") as? ArrayList<String> ?: ArrayList()
                    val comentarioFiscal = document.getString("comentarioFiscal") ?: ""

                    val fileUris = document.get("fileUris") as? ArrayList<String> ?: ArrayList()
                    val fileUrisString = fileUris.joinToString(separator = ",") // Use o delimitador que melhor se encaixa

                    val vistoria = VistoriaData(
                        id = "id_da_vistoria",
                        dataVistoria = dataVistoria,
                        dataProximaVistoria = dataProximaVistoria,
                        nomeFiscal = nomeFiscal,
                        imagemUrls = imagemUrls,
                        comentarioFiscal = comentarioFiscal,
                        fileUris = fileUrisString // Agora é uma única String, não uma lista
                    )

                    vistoriasList.add(vistoria)
                }

                // Ordena a lista de vistorias por data completa (dia, mês e ano)
                vistoriasList.sortBy { it.dataVistoria.toDate() }
                vistoriasAdapter.updateList(vistoriasList)
            }
            .addOnFailureListener { e ->
                // Trate as falhas na obtenção dos dados
                Toast.makeText(this, "Erro ao obter as vistorias: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onDeleteVistoria(vistoria: VistoriaData) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("vistorias")
            .document(vistoria.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Vistoria excluída com sucesso!", Toast.LENGTH_SHORT).show()

                // Obtenha a lista de vistorias do adaptador
                val vistoriasList = vistoriasAdapter.getVistoriasList()

                // Encontra a posição da vistoria na lista
                val position = vistoriasList.indexOf(vistoria)

                // Verifica se a vistoria foi encontrada na lista
                if (position != -1) {
                    // Remove a vistoria do adaptador
                    vistoriasAdapter.removeVistoria(vistoria)
                } else {
                    Toast.makeText(this, "Vistoria não encontrada na lista", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao excluir vistoria: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun String.toDate(): Date {
        val pattern = "dd/MM/yyyy"
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.parse(this) ?: Date(0)
    }
}
