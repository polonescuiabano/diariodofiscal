package com.example.diariodofiscal.agenda

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import com.example.diariodofiscal.R
import com.google.firebase.auth.FirebaseAuth

class diario : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var editTextDate: EditText
    private lateinit var editTextEventType: EditText
    private lateinit var editTextTime: EditText
    private lateinit var editTextObservations: EditText
    private lateinit var editTextFiscalName: EditText
    private lateinit var condominio: String
    private lateinit var buttonSave: Button
    private lateinit var storageReference: StorageReference
    private val fileUris: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diario)

        // Recebendo o nome do condomínio da tela anterior
        condominio = intent.getStringExtra("condominio") ?: ""

        // Inicialize o FirebaseStorage
        storageReference = FirebaseStorage.getInstance().reference

        val buttonSave: Button = findViewById(R.id.buttonSave)
        val buttonAddFile: Button = findViewById(R.id.buttonAddFile)
        editTextDate = findViewById(R.id.editTextDate)
        editTextEventType = findViewById(R.id.editTextEventType)
        editTextTime = findViewById(R.id.editTextTime)
        editTextObservations = findViewById(R.id.editTextObservations)
        editTextFiscalName = findViewById(R.id.editTextFiscalName)

        editTextDate.setOnClickListener {
            showDatePickerDialog()
        }

        editTextEventType.setOnClickListener { showEventTypeDialog() }

        editTextTime.setOnClickListener { showTimePickerDialog() }

        buttonAddFile.setOnClickListener {
            openFileChooser()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            // Verificar o e-mail do usuário autenticado
            val userEmail = currentUser.email
            // Preencher automaticamente o campo editTextFiscalName com base no e-mail do usuário
            when (userEmail) {
                "fernandopkb@gmail.com" -> editTextFiscalName.setText("Fernando Przybyszewski")
                "gessica.micaeli17@gmail.com" -> editTextFiscalName.setText("Gessica Poloni")
                "carolina.miura.terra@hotmail.com" -> editTextFiscalName.setText("Carolina Miura")
                "carlosmmiura@gmail.com" -> editTextFiscalName.setText("Carlos Miura")
                "jonathanaragao1@outlook.com" -> editTextFiscalName.setText("Jonathan Aragão")
                "dyelsontavares@hotmail.com" -> editTextFiscalName.setText("Dyelson Tavares")
                "hulleanhof@gmail.com" -> editTextFiscalName.setText("Hullean Firmino")
                "lollaamorim35@hotmail.com" -> editTextFiscalName.setText("Paolla Amorim ")

                // Adicione mais casos conforme necessário para outros usuários
                else -> editTextFiscalName.setText("") // Limpar o campo se o e-mail não estiver mapeado
            }
        }

        buttonSave.setOnClickListener {
            saveDiary()
            // Desabilitar o botão após o primeiro clique
            buttonSave.isEnabled = false
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                editTextDate.setText("$dayOfMonth/${monthOfYear + 1}/$year")
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showEventTypeDialog() {
        val eventTypes = arrayOf(
            "Reunião de Início de Obra",
            "Conferência de Gabarito",
            "Atendimento à Administração do Condomínio",
            "Atendimento ao Condômino/Construtor/Responsável Técnico",
            "Visita In Loco",
            "Vistoria Final do dia",
            "Outros"
        )

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Selecione o Tipo de Evento")
        alertDialogBuilder.setItems(eventTypes) { dialog, which ->
            editTextEventType.setText(eventTypes[which])
            dialog.dismiss()
        }
        alertDialogBuilder.create().show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this,
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                editTextTime.setText(time)
            }, hourOfDay, minute, true)
        timePickerDialog.show()
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*" // Aqui você pode especificar o tipo de arquivo que deseja permitir que o usuário selecione
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Selecione os Arquivos"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                // Caso o usuário tenha selecionado múltiplos arquivos
                for (i in 0 until data.clipData!!.itemCount) {
                    val fileUri: Uri = data.clipData!!.getItemAt(i).uri
                    fileUris.add(fileUri)
                }
            } else if (data.data != null) {
                // Caso o usuário tenha selecionado apenas um arquivo
                val fileUri: Uri = data.data!!
                fileUris.add(fileUri)
            }
        }
        // Reabilitar o botão após o retorno do resultado da seleção de arquivos
        buttonSave.isEnabled = true
    }

    private fun saveDiary() {
        val eventType = editTextEventType.text.toString()
        val date = editTextDate.text.toString()
        val time = editTextTime.text.toString()
        val observations = editTextObservations.text.toString()
        val fiscalName = editTextFiscalName.text.toString()

        // Save to Firestore
        val diary = hashMapOf(
            "eventType" to eventType,
            "date" to date,
            "time" to time,
            "observations" to observations,
            "fiscalName" to fiscalName
        )

        db.collection("Condominios")
            .document(condominio)
            .collection("diario_do_fiscal")
            .add(diary)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Diário salvo com ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
                // Enviar arquivos para o Firebase Storage
                uploadFiles(documentReference.id)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar o diário: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadFiles(diaryId: String) {
        for (i in 0 until fileUris.size) {
            val fileUri = fileUris[i]
            val fileReference = storageReference.child("uploads/${UUID.randomUUID()}")

            fileReference.putFile(fileUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Obter a URL do arquivo enviado
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        // Salvar a URL do arquivo no Firestore
                        db.collection("Condominios")
                            .document(condominio)
                            .collection("diario_do_fiscal")
                            .document(diaryId)
                            .collection("files")
                            .add(hashMapOf("fileUrl" to uri.toString()))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao enviar arquivo: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
