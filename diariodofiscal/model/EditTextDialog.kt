package com.example.diariodofiscal.model

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class EditTextDialog(
    context: Context,
    private val currentText: String,
    private val onEdited: (String) -> Unit
) {

    init {
        val editText = EditText(context)
        editText.setText(currentText)

        AlertDialog.Builder(context)
            .setTitle("Editar Campo")
            .setView(editText)
            .setPositiveButton("Salvar") { dialog, which ->
                // Quando o usuário clicar em "Salvar", chame a função de retorno com o texto editado
                onEdited(editText.text.toString())
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                // Não há necessidade de fazer nada ao clicar em "Cancelar"
            }
            .show()
    }
}
