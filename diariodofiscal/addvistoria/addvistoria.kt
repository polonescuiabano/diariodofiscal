package com.example.diariodofiscal.addvistoria

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.adapters.SelectedFilesAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import com.google.firebase.auth.FirebaseAuth

class AddVistoria : AppCompatActivity() {

    companion object {
        private const val REQUEST_FILE_PICK = 1
        private const val PERMISSION_READ_EXTERNAL_STORAGE = 101
        private const val OBRA_ID_EXTRA = "obraId"
        private const val CONDOMINIO_ID_EXTRA = "condominioId"


        fun newIntent(context: Context, obraId: String?, condominioId: String?): Intent {
            val intent = Intent(context, AddVistoria::class.java)
            intent.putExtra(OBRA_ID_EXTRA, obraId)
            intent.putExtra(CONDOMINIO_ID_EXTRA, condominioId)
            return intent
        }
    }

    private lateinit var dataVistoriaEditText: EditText
    private lateinit var dataProximaVistoriaEditText: EditText
    private lateinit var nomeFiscalEditText: EditText
    private lateinit var comentarioFiscalEditText: EditText
    private lateinit var attachFilesButton: Button
    private lateinit var saveButton: Button
    private lateinit var selectedFilesRecyclerView: RecyclerView
    private lateinit var selectedFilesAdapter: SelectedFilesAdapter

    private var obraId: String? = null
    private var fileUriList : MutableList<Uri> = mutableListOf()
    private var condominioId: String? = null
    private lateinit var storageReference: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addvistoria)

        val db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance()

        dataVistoriaEditText = findViewById(R.id.dataVistoriaEditText)
        dataProximaVistoriaEditText = findViewById(R.id.dataProximaVistoriaEditText)
        nomeFiscalEditText = findViewById(R.id.nomeFiscalEditText)
        comentarioFiscalEditText = findViewById(R.id.comentarioFiscalEditText)
        attachFilesButton = findViewById(R.id.attachFilesButton)
        saveButton = findViewById(R.id.saveButton)
        selectedFilesRecyclerView = findViewById(R.id.recyclerViewSelectedFiles)

        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        dataVistoriaEditText.setText(formattedDate)

        selectedFilesAdapter = SelectedFilesAdapter(fileUriList)
        selectedFilesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        selectedFilesRecyclerView.adapter = selectedFilesAdapter

        obraId = intent.getStringExtra("obraId")
        condominioId = intent.getStringExtra("condominio")

        if (obraId.isNullOrEmpty() || condominioId.isNullOrEmpty()) {
            Toast.makeText(this, "ID da obra ou ID do condomínio não estão disponíveis", Toast.LENGTH_SHORT).show()
            finish()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            val userEmail = currentUser.email
            when (userEmail) {
                "fernandopkb@gmail.com" -> nomeFiscalEditText.setText("Fernando Przybyszewski")
                "gessica.micaeli17@gmail.com" -> nomeFiscalEditText.setText("Gessica Poloni")
                "carolina.miura.terra@hotmail.com" -> nomeFiscalEditText.setText("Carolina Miura")
                "carlosmmiura@gmail.com" -> nomeFiscalEditText.setText("Carlos Miura")
                "jonathanaragao1@outlook.com" -> nomeFiscalEditText.setText("Jonathan Aragão")
                "dyelsontavares@hotmail.com" -> nomeFiscalEditText.setText("Dyelson Tavares")
                "hulleanhof@gmail.com" -> nomeFiscalEditText.setText("Hullean Firmino")
                "lollaamorim35@hotmail.com" -> nomeFiscalEditText.setText("Paolla Amorim ")
                else -> nomeFiscalEditText.setText("")
            }
        }

        attachFilesButton.setOnClickListener {
            showFileOptions()
        }

        saveButton.setOnClickListener {
            val dataVistoria = dataVistoriaEditText.text.toString()
            val dataProximaVistoria = dataProximaVistoriaEditText.text.toString()
            val nomeFiscal = nomeFiscalEditText.text.toString()
            val comentarioFiscal = comentarioFiscalEditText.text.toString()

            if (dataVistoria.isNotEmpty() && dataProximaVistoria.isNotEmpty() && nomeFiscal.isNotEmpty()) {
                val vistoriaData = hashMapOf(
                    "dataVistoria" to dataVistoria,
                    "dataProximaVistoria" to dataProximaVistoria,
                    "nomeFiscal" to nomeFiscal,
                    "comentarioFiscal" to comentarioFiscal,
                    "fileUris" to fileUriList.map { it.toString() }
                )

                db.collection("Condominios").document(condominioId!!).collection("Obras").document(obraId!!)
                    .collection("vistorias")
                    .add(vistoriaData)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(
                            this,
                            "Vistoria salva com sucesso com ID: ${documentReference.id}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Chame o método uploadFiles após adicionar os dados da vistoria ao Firestore
                        uploadFiles(documentReference.id, condominioId!!, obraId!!)

                        clearFields()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro ao salvar a vistoria: $e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    this,
                    "Por favor, preencha todos os campos obrigatórios",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val add7DaysButton: Button = findViewById(R.id.add7DaysButton)
        val add15DaysButton: Button = findViewById(R.id.add15DaysButton)
        val add21DaysButton: Button = findViewById(R.id.add21DaysButton)
        val add30DaysButton: Button = findViewById(R.id.add30DaysButton)

        add7DaysButton.setOnClickListener { addDaysToNextInspection(7) }
        add15DaysButton.setOnClickListener { addDaysToNextInspection(15) }
        add21DaysButton.setOnClickListener { addDaysToNextInspection(21) }
        add30DaysButton.setOnClickListener { addDaysToNextInspection(30) }
    }

    private fun showFileOptions() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

        val chooser = Intent.createChooser(intent, "Selecione os arquivos")

        startActivityForResult(chooser, REQUEST_FILE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_FILE_PICK -> {
                data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        val fileUri = clipData.getItemAt(i).uri
                        fileUriList.add(fileUri)
                    }
                }
                data?.data?.let { fileUri ->
                    fileUriList.add(fileUri)
                }
                selectedFilesAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun uploadFiles(vistoriaId: String, condominioId: String, obraId: String) {
        for (i in 0 until fileUriList.size) {
            val fileUri = fileUriList[i]
            val fileReference = storageReference.reference.child("vistorias/$vistoriaId/${UUID.randomUUID()}")

            fileReference.putFile(fileUri)
                .addOnSuccessListener { taskSnapshot ->
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        FirebaseFirestore.getInstance()
                            .collection("Condominios")
                            .document(condominioId)
                            .collection("Obras")
                            .document(obraId)
                            .collection("vistorias")
                            .document(vistoriaId)
                            .collection("files")
                            .add(hashMapOf("fileUrl" to uri.toString()))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao enviar arquivo: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun clearFields() {
        dataVistoriaEditText.text.clear()
        dataProximaVistoriaEditText.text.clear()
        nomeFiscalEditText.text.clear()
        comentarioFiscalEditText.text.clear()
        fileUriList.clear()
        selectedFilesAdapter.notifyDataSetChanged()
    }

    private fun addDaysToNextInspection(days: Int) {
        val nextInspectionDateEditText: EditText = findViewById(R.id.dataProximaVistoriaEditText)

        val currentDate = Calendar.getInstance()
        currentDate.add(Calendar.DAY_OF_YEAR, days)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val newDate = dateFormat.format(currentDate.time)

        nextInspectionDateEditText.setText(newDate)
    }
}
