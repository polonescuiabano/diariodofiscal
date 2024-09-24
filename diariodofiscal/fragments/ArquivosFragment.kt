package com.example.diariodofiscal.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diariodofiscal.adapters.ArquivosAdapter
import com.example.diariodofiscal.databinding.FragmentArquivosBinding
import com.example.diariodofiscal.model.Arquivo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.diariodofiscal.R

class ArquivosFragment : Fragment() {

    private var _binding: FragmentArquivosBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var obraId: String
    private lateinit var condominio: String
    private lateinit var storageReference: StorageReference
    private var selectedFiles = mutableListOf<Arquivo>()

    private lateinit var arquivosAdapter: ArquivosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArquivosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        obraId = arguments?.getString(ARG_OBRA_ID) ?: ""
        condominio = arguments?.getString(ARG_CONDOMINIO) ?: ""


        setupRecyclerView()

        loadFiles()

        binding.btnAdicionarArquivo.setOnClickListener {
            openFilePicker()
        }
    }

    private fun setupRecyclerView() {
        arquivosAdapter = ArquivosAdapter(
            { arquivo -> openFileInBrowser(arquivo.fileUrl) },
            { arquivo -> onDeleteClick(arquivo.fileUrl) },
            { arquivo -> onRenameClick(arquivo) }
        )
        binding.recyclerViewArquivos.apply {
            adapter = arquivosAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }






    private fun loadFiles() {
        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("Arquivos")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val files = mutableListOf<Arquivo>()
                for (document in querySnapshot.documents) {
                    val fileId = document.id // Obtém o ID do documento
                    val fileName = document.getString("fileName")
                    val fileUrl = document.getString("fileUrl")
                    if (fileName != null && fileUrl != null) {
                        files.add(Arquivo(id = fileId, fileName = fileName, fileUrl = fileUrl))
                    }
                }
                arquivosAdapter.submitList(files)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Erro ao carregar arquivos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openFileInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(intent)
    }
    private val PICK_FILES_REQUEST_CODE = 123


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Permitir seleção múltipla
        }
        startActivityForResult(Intent.createChooser(intent, "Selecione os arquivos"), PICK_FILES_REQUEST_CODE)
    }


    private fun onDeleteClick(fileUrl: String) {
        // Remover o arquivo do Firestore
        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("Arquivos")
            .whereEqualTo("fileUrl", fileUrl)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Remover o arquivo da lista exibida
                            arquivosAdapter.notifyDataSetChanged()

                            // Adicionar log de exclusão
                            addFileDeletionLog(fileUrl)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Erro ao excluir arquivo: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Erro ao excluir arquivo: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onRenameClick(arquivo: Arquivo) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rename_file, null)
        val editTextNewFileName = dialogView.findViewById<EditText>(R.id.editTextNewFileName)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Renomear Arquivo")
            .setView(dialogView)
            .setPositiveButton("Renomear") { dialog, _ ->
                val newFileName = editTextNewFileName.text.toString().trim()
                if (newFileName.isNotEmpty()) {
                    renameFile(arquivo, newFileName)
                } else {
                    Toast.makeText(requireContext(), "Por favor, insira um novo nome para o arquivo", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun renameFile(arquivo: Arquivo, newFileName: String) {
        // Log para verificar o ID do arquivo antes de atualizar
        Log.d("renameFile", "ID do arquivo: ${arquivo.id}")

        // Atualizar o nome do arquivo no Firestore
        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("Arquivos")
            .document(arquivo.id.toString())
            .update("fileName", newFileName)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Arquivo renomeado com sucesso", Toast.LENGTH_SHORT).show()
                // Adicionar log de renomeação
                addFileRenameLog(arquivo.fileName, newFileName)
                // Atualizar a lista de arquivos exibidos
                loadFiles()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Erro ao renomear o arquivo: ${exception.message}", Toast.LENGTH_SHORT).show()
                // Log para verificar o erro, se ocorrer
                Log.e("renameFile", "Erro ao renomear o arquivo: ${exception.message}", exception)
            }
    }


    private fun addFileRenameLog(oldFileName: String, newFileName: String) {
        val dataHoraAtual = Calendar.getInstance().time // Data e hora atuais
        val usuarioAtual = FirebaseAuth.getInstance().currentUser?.email // Usuário atual

        val logData = hashMapOf(
            "usuario" to usuarioAtual,
            "alteracao" to "Renomeação de arquivo",
            "data" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dataHoraAtual),
            "hora" to SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(dataHoraAtual),
            "condominio" to condominio,
            "obra" to obraId,
            "oldFileName" to oldFileName,
            "newFileName" to newFileName
        )

        // Adicionar o log à coleção "logs"
        firestore.collection("logs")
            .add(logData)
            .addOnSuccessListener { logDoc ->
                Log.d("ArquivosFragment", "Log de renomeação de arquivo adicionado com ID: ${logDoc.id}")
            }
            .addOnFailureListener { e ->
                Log.e("ArquivosFragment", "Erro ao adicionar log de renomeação de arquivo: ${e.message}")
            }
    }



    private fun addFileDeletionLog(fileUrl: String, quadra: String = "", lote: String = "") {
        val dataHoraAtual = Calendar.getInstance().time // Data e hora atuais
        val usuarioAtual = FirebaseAuth.getInstance().currentUser?.email // Usuário atual

        val logData = hashMapOf(
            "usuario" to usuarioAtual,
            "alteracao" to "Exclusão de arquivo",
            "data" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dataHoraAtual),
            "hora" to SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(dataHoraAtual),
            "condominio" to condominio,
            "obra" to obraId,
            "quadra" to quadra,
            "lote" to lote,
            "fileUrl" to fileUrl
        )

        // Adicionar o log à coleção "logs"
        firestore.collection("logs")
            .add(logData)
            .addOnSuccessListener { logDoc ->
                Log.d("ArquivosFragment", "Log de exclusão de arquivo adicionado com ID: ${logDoc.id}")
            }
            .addOnFailureListener { e ->
                Log.e("ArquivosFragment", "Erro ao adicionar log de exclusão de arquivo: ${e.message}")
            }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedFiles: MutableList<Uri> = mutableListOf()
            data?.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    selectedFiles.add(uri)
                }
            } ?: data?.data?.let { uri ->
                selectedFiles.add(uri)
            }
            // Processar os arquivos selecionados
            processSelectedFiles(selectedFiles)
        }
    }

    private fun processSelectedFiles(selectedFiles: List<Uri>) {
        for (uri in selectedFiles) {
            uploadFileToFirebase(uri)
        }
    }

    private fun uploadFileToFirebase(fileUri: Uri) {
        val fileName = "${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
        val fileReference = storageReference.child("obras/$obraId/$fileName")

        fileReference.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                Toast.makeText(
                    requireContext(),
                    "Arquivo enviado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    saveFileToFirestore(fileName, downloadUrl)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao enviar o arquivo: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveFileToFirestore(fileName: String, fileUrl: String) {
        val arquivo = hashMapOf(
            "fileName" to fileName,
            "fileUrl" to fileUrl
        )

        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("Arquivos")
            .add(arquivo)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Arquivo salvo com sucesso no Firestore!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Erro ao salvar o arquivo no Firestore: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        private const val REQUEST_PICK_FILE = 101
        private const val ARG_OBRA_ID = "obraId"
        private const val ARG_CONDOMINIO = "condominio"

        @JvmStatic
        fun newInstance(obraId: String, condominio: String): ArquivosFragment {
            val fragment = ArquivosFragment()
            val args = Bundle()
            args.putString(ARG_OBRA_ID, obraId)
            args.putString(ARG_CONDOMINIO, condominio)
            fragment.arguments = args
            return fragment
        }
    }
}

