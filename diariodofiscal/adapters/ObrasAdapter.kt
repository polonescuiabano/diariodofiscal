package com.example.diariodofiscal.adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.google.firebase.firestore.FirebaseFirestore

class ObrasAdapter(
    private val adicionarEtiqueta: (String, String) -> Unit,
    private val clickListener: (String, String, String, String, String, String, String) -> Unit
) : RecyclerView.Adapter<ObrasAdapter.ObrasViewHolder>() {

    private lateinit var etiquetaContainer: FrameLayout





    private lateinit var condominio: String
    private var obrasList = mutableListOf<String>()
    private var quadraList = mutableListOf<String>()
    private var loteList = mutableListOf<String>()
    private var proprietarioList = mutableListOf<String>()
    private var emailList = mutableListOf<String>()
    private var telefoneList = mutableListOf<String>()
    private var obraIdList = mutableListOf<String>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObrasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_obra, parent, false)
        Log.d("ObrasAdapter", "Layout inflado com sucesso")
        val viewHolder = ObrasViewHolder(view)
        viewHolder.etiquetaContainer = view.findViewById(R.id.etiquetaContainer)

        if (viewHolder.etiquetaContainer != null) {
            Log.d("ObrasAdapter", "ID etiquetaContainer encontrado corretamente")
        } else {
            Log.e("ObrasAdapter", "ID etiquetaContainer não encontrado")
        }

        return viewHolder
    }








    fun setCondominio(condominio: String) {
        this.condominio = condominio
    }

    override fun onBindViewHolder(holder: ObrasViewHolder, position: Int) {
        if (position < obrasList.size) {
            val obra = obrasList[position]
            val quadra = quadraList.getOrNull(position) ?: ""
            val lote = loteList.getOrNull(position) ?: ""
            val proprietario = proprietarioList.getOrNull(position) ?: ""
            val email = emailList.getOrNull(position) ?: ""
            val telefone = telefoneList.getOrNull(position) ?: ""
            val obraId = obraIdList.getOrNull(position) ?: ""

            holder.bind(obra)

            // Inicializa a propriedade etiquetaContainer antes de qualquer operação que a utilize
            holder.itemView.setOnClickListener {
                clickListener(obra, quadra, lote, proprietario, email, telefone, obraId)
            }

            holder.seta.setOnClickListener {
                val popup = PopupMenu(holder.itemView.context, holder.seta)
                popup.menuInflater.inflate(R.menu.menu_status, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.construcao -> {
                            atualizarEtiqueta(obraId, "Construção", holder)
                            true
                        }
                        R.id.paralisada -> {
                            atualizarEtiqueta(obraId, "Paralisada", holder)
                            true
                        }
                        R.id.reforma -> {
                            atualizarEtiqueta(obraId, "Reforma", holder)
                            true
                        }
                        R.id.concluido -> {
                            atualizarEtiqueta(obraId, "Concluída", holder)
                            true
                        }
                        R.id.manutencao -> {
                            atualizarEtiqueta(obraId, "Manutenção", holder)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }

            // Carrega as etiquetas da obra
            holder.etiquetaContainer?.let { container ->
                carregarEtiquetas(obraId, holder.itemView.context, container)

                // Adiciona o Long Click Listener para remover etiquetas
                for (i in 0 until container.childCount) {
                    val etiquetaView = container.getChildAt(i)
                    etiquetaView.setOnLongClickListener { view ->
                        val etiquetaText = (view as TextView).text.toString()

                        // Exibir um diálogo de confirmação para excluir a etiqueta
                        val alertDialogBuilder = AlertDialog.Builder(view.context)
                        alertDialogBuilder.setTitle("Excluir Etiqueta")
                        alertDialogBuilder.setMessage("Deseja realmente excluir a etiqueta \"$etiquetaText\"?")
                        alertDialogBuilder.setPositiveButton("Sim") { dialog, which ->
                            // Remova a etiqueta da interface do usuário e do banco de dados
                            container.removeView(view)
                            // Remove a etiqueta do Firestore
                            removerEtiquetaDoBancoDeDados(obraId, etiquetaText)
                        }
                        alertDialogBuilder.setNegativeButton("Cancelar") { dialog, which ->
                            // Não faz nada, apenas fecha o diálogo
                        }

                        // Mostrar o diálogo
                        alertDialogBuilder.show()

                        // Retorna true para indicar que o evento de long click foi consumido
                        true
                    }
                }
            }
        }
    }


    private fun atualizarEtiqueta(obraId: String, novaEtiqueta: String, holder: ObrasViewHolder) {
        adicionarEtiquetainternamente(obraId, novaEtiqueta, holder.etiquetaContainer!!)
        // Atualiza a etiqueta no Firestore
        atualizarEtiquetaNoFirestore(obraId, novaEtiqueta)
    }






    override fun getItemCount(): Int {
        return obrasList.size
    }

    fun setObras(
        obras: List<String>, quadra: List<String>, lote: List<String>,
        proprietario: List<String>, email: List<String>, telefone: List<String>, obraId: List<String>
    ) {
        obrasList.clear()
        obrasList.addAll(obras)
        quadraList.clear()
        quadraList.addAll(quadra)
        loteList.clear()
        loteList.addAll(lote)
        proprietarioList.clear()
        proprietarioList.addAll(proprietario)
        emailList.clear()
        emailList.addAll(email)
        telefoneList.clear()
        telefoneList.addAll(telefone)
        obraIdList.clear()
        obraIdList.addAll(obraId)
        notifyDataSetChanged()
    }

    inner class ObrasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val seta: ImageButton = itemView.findViewById(R.id.seta)
        var etiquetaContainer: FrameLayout? = null

        init {
            if (etiquetaContainer == null) {
                Log.e("Obras Adapter", "etiquetaContainer não foi inicializado corretamente")
            } else {
                Log.d("ObrasAdapter", "ID etiquetaContainer encontrado no ViewHolder")
            }
        }

        fun bind(obra: String) {
            val textViewObra: TextView = itemView.findViewById(R.id.textViewObra)
            textViewObra.text = obra
        }
    }



    private fun atualizarEtiquetaNoFirestore(obraId: String, novaEtiqueta: String) {
        // Atualiza a etiqueta no Firestore
        // Supondo que você tenha a referência do Firestore disponível
        val etiquetaData = hashMapOf(
            "etiqueta" to novaEtiqueta
        )

        val etiquetasRef = firestore
            .collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("etiquetas")

        etiquetasRef.add(etiquetaData)
            .addOnSuccessListener { documentReference ->
                // A etiqueta foi adicionada com sucesso
                Log.d("Obras Adapter", "Etiqueta atualizada com ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Ocorreu um erro ao atualizar a etiqueta
                Log.e("Obras Adapter", "Erro ao atualizar etiqueta: $e")
            }
    }


    private fun adicionarEtiquetainternamente(obraId: String, novaEtiqueta: String, etiquetaContainer: FrameLayout) {
        etiquetaContainer?.let { container ->
            Log.d("ObrasAdapter", "etiquetaContainer não é nulo")

            val etiquetaText = when (novaEtiqueta) {
                "Construção" -> "Obra em Execução"
                "Reforma" -> "Obra em Reforma/Ampliação"
                "Paralisada" -> "Obra Paralisada"
                "Concluída" -> "Obra Concluída"
                "Manutenção" -> "Obra em Manutenção"
                else -> "" // Você pode adicionar mais casos conforme necessário
            }

            if (etiquetaText.isNotEmpty()) {
                // Salva a nova etiqueta no Firestore ou em qualquer outro local desejado
                val etiquetaData = hashMapOf(
                    "etiqueta" to novaEtiqueta
                )

                val etiquetasRef = firestore
                    .collection("Condominios")
                    .document(condominio)
                    .collection("Obras")
                    .document(obraId)
                    .collection("etiquetas")

                etiquetasRef.get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            // Remove a etiqueta existente do Firestore
                            etiquetasRef.document(document.id).delete()
                                .addOnSuccessListener {
                                    Log.d("Obras Adapter", "Etiqueta anterior removida com sucesso: ${document.id}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Obras Adapter", "Erro ao remover etiqueta anterior: $e")
                                }
                            // Remove a etiqueta existente do layout
                            container.removeAllViews()
                        }
                        // Adiciona a nova etiqueta ao Firestore
                        etiquetasRef.add(etiquetaData)
                            .addOnSuccessListener { documentReference ->
                                // A nova etiqueta foi adicionada com sucesso
                                Log.d("Obras Adapter", "Nova etiqueta adicionada com ID: ${documentReference.id}")

                                // Adiciona a nova etiqueta ao layout
                                val textView = TextView(container.context).apply {
                                    text = etiquetaText
                                    // Configurações adicionais de estilo, layout, etc.
                                }
                                container.addView(textView)
                            }
                            .addOnFailureListener { e ->
                                // Ocorreu um erro ao adicionar a nova etiqueta
                                Log.e("Obras Adapter", "Erro ao adicionar nova etiqueta: $e")
                            }
                    }
                    .addOnFailureListener { e ->
                        // Ocorreu um erro ao consultar as etiquetas existentes
                        Log.e("Obras Adapter", "Erro ao consultar etiquetas existentes: $e")
                    }
            } else {
                Log.e("ObrasAdapter", "Etiqueta inválida: $novaEtiqueta")
            }
        } ?: run {
            Log.e("ObrasAdapter", "etiquetaContainer é nulo")
        }
    }


    private fun carregarEtiquetas(obraId: String, contexto: Context, etiquetaContainer: FrameLayout) {
        val etiquetasRef = firestore
            .collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("etiquetas")

        etiquetasRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val etiqueta = document.getString("etiqueta")
                    if (etiqueta != null) {
                        // Cria uma nova TextView para representar a etiqueta
                        val textView = TextView(contexto).apply {
                            text = etiqueta
                            // Configurações adicionais de estilo, layout, etc.
                        }

                        // Adiciona a TextView ao contêiner
                        etiquetaContainer.addView(textView)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Obras Adapter", "Erro ao carregar etiquetas da obra $obraId", exception)
                Toast.makeText(
                    contexto,
                    "Erro ao carregar etiquetas da obra",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun removerEtiquetaDoBancoDeDados(obraId: String, etiqueta: String) {
        val etiquetasRef = firestore
            .collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("etiquetas")

        etiquetasRef.whereEqualTo("etiqueta", etiqueta)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Remove a etiqueta do banco de dados
                    etiquetasRef.document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Obras Adapter", "Etiqueta removida com sucesso: $etiqueta")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Obras Adapter", "Erro ao remover etiqueta: $e")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Obras Adapter", "Erro ao consultar etiquetas para remoção: $exception")
            }
    }





    interface OnItemClickListener {
        fun onItemClick(obra: String, quadra: String, lote: String, proprietario: String, email: String, telefone: String, obraId: String)
    }
}

