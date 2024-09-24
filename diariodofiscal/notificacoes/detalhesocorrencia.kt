package com.example.diariodofiscal.notificacoes

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import com.example.diariodofiscal.R
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text

class detalhesocorrencia : AppCompatActivity() {

    private lateinit var condominio: String
    private lateinit var obraId: String
    private lateinit var ocorrenciaId: String
    private lateinit var statusObraTextView: TextView

    private val db = FirebaseFirestore.getInstance()

    private lateinit var avaliacoesContainer: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhesocorrencia)

        statusObraTextView = findViewById(R.id.status_obra)

        avaliacoesContainer = findViewById(R.id.avaliacoesContainer)


        ocorrenciaId = intent.getStringExtra("ocorrenciaId") ?: ""
        condominio = intent.getStringExtra("condominio") ?: ""
        obraId = intent.getStringExtra("obraId") ?: ""

        if (ocorrenciaId.isNullOrEmpty()) {
            Log.e("detalhes ocorrencia", "ID da ocorrência não foi passado corretamente")
            return
        } else {
            Log.d("detalhes ocorrencia", "ID da ocorrência: $ocorrenciaId")
        }

        // Estrutura para os botões "Recurso" e "Encerrar"
        val btnRecurso = findViewById<Button>(R.id.recursoButton)
        btnRecurso.setOnClickListener {
            showConfirmationDialog("Paralisada")
        }

        val btnEncerrar = findViewById<Button>(R.id.encerrarButton)
        btnEncerrar.setOnClickListener {
            showConfirmationDialog("Encerrada")
        }

        val btnAvaliacao = findViewById<Button>(R.id.fazerAvaliacaoButton)
        btnAvaliacao.setOnClickListener {
            val intent = Intent(this, AvaliacaoActivity::class.java)
            intent.putExtra("condominio", condominio)
            intent.putExtra("obraId", obraId)
            intent.putExtra("ocorrenciaId", ocorrenciaId)
            startActivity(intent)
        }

        buscarDetalhesOcorrencia()


        // Buscar e exibir o status da obra
        exibirStatusObra()
    }

    private fun buscarDetalhesOcorrencia() {
        db.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("Ocorrencias").document(ocorrenciaId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    exibirDetalhesOcorrencia(data)
                } else {
                    // Tratar documento não encontrado
                }
            }
            .addOnFailureListener { exception ->
                // Tratar erro na busca dos detalhes da ocorrência
            }
    }

    private fun buscarAvaliacoes() {
        db.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("Ocorrencias").document(ocorrenciaId)
            .collection("Avaliacoes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val avaliacoes = mutableListOf<Map<String, Any>>()
                for (document in querySnapshot.documents) {
                    val avaliacao = document.data
                    if (avaliacao != null) {
                        avaliacoes.add(avaliacao)
                    }
                }
                // Chama a função para exibir as avaliações
                exibirAvaliacoes(avaliacoes, avaliacoesContainer)
            }
            .addOnFailureListener { exception ->
                Log.e("detalhes ocorrencia", "Erro ao buscar avaliações", exception)
            }
    }

    private fun exibirAvaliacoes(avaliacoes: List<Map<String, Any>>, parentContainer: LinearLayout) {
        for (avaliacao in avaliacoes) {
            exibirAvaliacao(avaliacao, parentContainer)
        }
    }


    private fun exibirDetalhesOcorrencia(data: Map<String, Any>?) {
        val nomeArtigoTextView = findViewById<TextView>(R.id.nomeArtigoTextView)
        val dataRetornoTextView = findViewById<TextView>(R.id.dataRetornoTextView)
        val descricaoMultaTextView = findViewById<TextView>(R.id.descricaoMultaTextView)
        val tipoMultaTextView = findViewById<TextView>(R.id.tipoMultaTextView)
        val imagensContainer = findViewById<LinearLayout>(R.id.imagensContainer)
        val avaliacoesContainer = findViewById<LinearLayout>(R.id.avaliacoesContainer) // Adicionando esta linha


        if (data != null) {
            // Preencher os campos com os dados da ocorrência
            nomeArtigoTextView.text = "Nome do Artigo: ${data["artigo"]}"
            dataRetornoTextView.text = "Data de Retorno: ${data["dataRetorno"]}"
            descricaoMultaTextView.text = "Descrição da Multa: ${data["descricao"]}"
            tipoMultaTextView.text = "Tipo de Multa: ${data["tipoMulta"]}"

            buscarAvaliacoes()

            // Recuperar as URLs das imagens do Firebase Storage
            val ocorrenciaId = data["ocorrenciaId"] as? String
            if (!ocorrenciaId.isNullOrEmpty()) {
                val storageReference = FirebaseStorage.getInstance().reference
                val path = "imagens/$ocorrenciaId/"
                storageReference.child(path).listAll().addOnSuccessListener { result ->
                    for (item in result.items) {
                        // Carregar a imagem em um ImageView e adicioná-lo ao LinearLayout
                        val imageView = ImageView(this)
                        item.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this).load(uri).into(imageView)
                            imagensContainer.addView(imageView)
                        }.addOnFailureListener { exception ->
                            Log.e("detalhes ocorrencia", "Erro ao recuperar URL da imagem", exception)
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("detalhes ocorrencia", "Erro ao listar imagens no Storage", exception)
                }
            }
        }
    }


    private fun exibirAvaliacao(avaliacao: Map<String, Any>, parentContainer: LinearLayout) {
        val avaliacaoView = layoutInflater.inflate(R.layout.activity_detalhesocorrencia, null)
        val dataAvaliacaoTextView = avaliacaoView.findViewById<TextView>(R.id.dataAvaliacaoTextView)
        val descricaoAvaliacaoTextView = avaliacaoView.findViewById<TextView>(R.id.descricaoAvaliacaoTextView)
        val imagensAvaliacaoContainer = avaliacaoView.findViewById<LinearLayout>(R.id.imagensAvaliacaoContainer)
        val dataRetornoav = avaliacaoView.findViewById<TextView>(R.id.dataRetornoAvTextView)


        dataAvaliacaoTextView.text = "Data da Avaliação: ${avaliacao["dataAvaliacao"]}"
        descricaoAvaliacaoTextView.text = "Descrição da Avaliação: ${avaliacao["observacoes"]}"
        dataRetornoav.text = "Data de Retorno: ${avaliacao["dataRetorno"]}"

        // Recuperar as URLs das imagens da avaliação no Firebase Storage
        val avaliacaoId = avaliacao["avaliacaoId"] as? String
        if (!avaliacaoId.isNullOrEmpty()) {
            val storageReference = FirebaseStorage.getInstance().reference
            val pathAvaliacao = "imagens/$avaliacaoId/"
            storageReference.child(pathAvaliacao).listAll().addOnSuccessListener { result ->
                for (item in result.items) {
                    // Carregar a imagem da avaliação em um ImageView e adicioná-lo ao LinearLayout
                    val imageView = ImageView(this)
                    item.downloadUrl.addOnSuccessListener { uri ->
                        Glide.with(this)
                            .load(uri)
                            .into(imageView)
                        imagensAvaliacaoContainer.addView(imageView)
                    }.addOnFailureListener { exception ->
                        Log.e("detalhes ocorrencia", "Erro ao recuperar URL da imagem da avaliação", exception)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("detalhes ocorrencia", "Erro ao listar imagens da avaliação no Storage", exception)
            }
        }

        parentContainer.addView(avaliacaoView)
    }




    private fun showConfirmationDialog(newStatus: String) {
        val message = "Você deseja realmente trocar o status da ocorrência para $newStatus?"
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("Sim") { dialogInterface: DialogInterface, _: Int ->
            alterarStatusObra(newStatus)
            dialogInterface.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Não") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun alterarStatusObra(newStatus: String) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("Ocorrencias").document(ocorrenciaId)
            .update("status", newStatus)
            .addOnSuccessListener {
                statusObraTextView.text = newStatus
            }
            .addOnFailureListener { exception ->
                Log.e("detalhes ocorrencia", "Erro ao atualizar o status da obra", exception)
            }
    }

    private fun exibirStatusObra() {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val statusObra = document.getString("status")
                    statusObraTextView.text = statusObra
                } else {
                    Log.e("detalhes ocorrencia", "Documento da obra não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("detalhes ocorrencia", "Erro ao obter o status da obra", exception)
            }
    }
}
