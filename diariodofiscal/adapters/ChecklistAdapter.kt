package com.example.diariodofiscal.adapters

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.model.ChecklistItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ChecklistAdapter(
    private val context: Context,
    private val items: MutableList<ChecklistItem>,
    private val recyclerView: RecyclerView,
    private val db: FirebaseFirestore,
    private val condominio: String,
    private val obraId: String
) : RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val spinnerStatus: Spinner = itemView.findViewById(R.id.spinnerStatus)
        val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        val editTextObservations: EditText = itemView.findViewById(R.id.editTextObservations)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton) // Adicionando a referência ao botão de edição

        init {
            // Configurar o clique do botão de edição aqui, pois o método onCreateViewHolder não é chamado novamente quando reciclamos a visualização
            editButton.setOnClickListener {
                // Implemente a lógica de edição aqui
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_checklist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        holder.apply {
            textViewTitle.text = currentItem.text

            val options = arrayOf("", "Regular", "Irregular", "Não Consta") // Adicionando uma opção em branco no início do array
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, options)
            spinnerStatus.adapter = adapter

            // Definindo a seleção do Spinner com base no status atual do item do checklist
            spinnerStatus.setSelection(options.indexOf(currentItem.status))

            spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    currentItem.status = options[position]
                    if (position != 0) { // Verificando se a opção selecionada não é a opção em branco
                        // Criar um Timestamp com a data atual
                        currentItem.dataMarcacao = Timestamp(System.currentTimeMillis())
                        textViewDate.text = "Data de Seleção: ${currentItem.dataMarcacao}"
                        textViewDate.visibility = View.VISIBLE
                    } else {
                        // Se a opção selecionada for a opção em branco, ocultar a data
                        textViewDate.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Se nada for selecionado, não fazer nada
                }
            }

            editTextObservations.setText(currentItem.observacoes)
            editTextObservations.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    currentItem.observacoes = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // Verifica se o usuário atual tem permissão para editar
            if (currentUserCanEdit()) {
                // Mostra o ícone de edição
                // Supondo que editButton seja o ID do ícone de edição
                editButton.visibility = View.VISIBLE
                editButton.setOnClickListener {
                    showEditDialog(currentItem, recyclerView, db, condominio, obraId)
                }
            } else {
                // Oculta o ícone de edição
                editButton.visibility = View.GONE
            }
        }
    }

    private fun showEditDialog(item: ChecklistItem, recyclerView: RecyclerView, db: FirebaseFirestore, condominio: String, obraId: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_checklist_item, null)
        val editTextNewText = dialogView.findViewById<EditText>(R.id.editTextNewText)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Editar Nome do Item")
            .setView(dialogView)
            .setPositiveButton("Salvar") { dialog, _ ->
                val newText = editTextNewText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    // Atualiza o nome do item no Firestore
                    updateItemNameInFirestore(item, newText, recyclerView, db, condominio, obraId)
                } else {
                    Snackbar.make(recyclerView, "O novo nome não pode estar vazio", Snackbar.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    // Método para atualizar o nome do item no Firestore
    private fun updateItemNameInFirestore(item: ChecklistItem, newText: String, recyclerView: RecyclerView, db: FirebaseFirestore, condominio: String, obraId: String) {
        val checklistRef = db.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("items")

        checklistRef.document(item.id).update("text", newText)
            .addOnSuccessListener {
                Snackbar.make(recyclerView, "Nome do item atualizado com sucesso", Snackbar.LENGTH_SHORT).show()
                // Atualiza o valor localmente no RecyclerView após a atualização bem-sucedida
                item.text = newText
                notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Snackbar.make(recyclerView, "Erro ao atualizar o nome do item", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun currentUserCanEdit(): Boolean {
        val currentUserEmail = getCurrentUserEmail() // Implemente a função para obter o e-mail do usuário atual
        val allowedEmails = setOf("fernandopkb@gmail.com", "gessica.micaeli17@gmail.com", "carlosmmiura@gmail.com")
        return currentUserEmail in allowedEmails
    }

    private fun getCurrentUserEmail(): String? {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser?.email
    }




    override fun getItemCount() = items.size

    fun addChecklistItem(item: ChecklistItem) {
        items.add(item)
        notifyDataSetChanged()
    }

    fun getChecklistItems() = items

    fun setChecklistItems(newItems: List<ChecklistItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}



