package com.example.diariodofiscal.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.diariodofiscal.databinding.FragmentDetalhesBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.diariodofiscal.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetalhesFragment : Fragment() {

    private var _binding: FragmentDetalhesBinding? = null
    private val binding get() = _binding!!

    private lateinit var obraId: String
    private lateinit var condominio: String
    private lateinit var firestore: FirebaseFirestore

    private lateinit var quadra: String
    private lateinit var lote: String
    private lateinit var observacoes: String
    private lateinit var proprietarios: List<String>
    private lateinit var prepostos: List<String>
    private lateinit var responsaveisTecnicos: List<String>
    private lateinit var mestresDeObra: List<String>
    private lateinit var emails: List<String>
    private lateinit var telefones: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalhesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        obraId = arguments?.getString("obraId") ?: ""
        condominio = arguments?.getString("condominio") ?: ""

        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val obra = document.toObject(Obra::class.java)
                    obra?.let {
                        quadra = obra.quadra
                        lote = obra.lote
                        observacoes = obra.observacoes

                        // Carregar os prepostos da subcoleção "preposto"
                        carregarDadosColecao("preposto", "nome") { prepostosResult ->
                            prepostos = prepostosResult.map { it.toString() } // Converter para String
                            // Atualizar os detalhes da obra na UI
                            with(binding) {
                                textViewPrepostos.text = "Prepostos: ${prepostos.joinToString(", ")}"
                            }
                        }

                        // Carregar os mestres de obras
                        carregarDadosColecao("mestreobras", "nome") { mestresDeObraResult ->
                            mestresDeObra = mestresDeObraResult.map {it.toString()}

                            with(binding) {
                                textViewMestresDeObra.text = "Mestres de Obra: ${mestresDeObra.joinToString(", ")}"
                            }
                        }

                        // Carregar os proprietários
                        carregarDadosColecao("proprietario", "nome") { proprietariosResult ->
                            proprietarios = proprietariosResult.map {it.toString()}

                            with(binding) {
                                textViewProprietarios.text = "Proprietários: ${proprietarios.joinToString(", ")}"
                            }
                        }

                        // Carregar os responsáveis técnicos
                        carregarDadosColecao("responsaveltecnico", "nome") { responsaveisTecnicosResult ->
                            responsaveisTecnicos = responsaveisTecnicosResult.map {it.toString()}

                            with(binding) {
                                textViewResponsaveisTecnicos.text = "Responsáveis Técnicos: ${responsaveisTecnicos.joinToString(", ")}"
                            }
                        }

                        // Carregar os telefones
                        carregarDadosColecao("telefones", "nome") { telefonesResult ->
                            telefones = telefonesResult.map {it.toString()}

                            with(binding) {
                                textViewTelefones.text = "Telefones: ${telefones.joinToString(", ")}"
                            }
                        }

                        // Carregar os e-mails
                        carregarDadosColecao("emails", "nome") { emailsResult ->
                            emails = emailsResult.map {it.toString()}

                            with(binding) {
                                textViewEmails.text = "E-mails: ${emails.joinToString(", ")}"
                            }
                        }

                        // Exibindo informações diretas da obra
                        with(binding) {
                            textViewQuadra.text = "Quadra: $quadra"
                            textViewLote.text = "Lote: $lote"
                            textViewObservacoes.text = "Observações: $observacoes"
                        }
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        binding.editarObraButton.setOnClickListener {
            editarObra(obraId)
        }
    }


    private fun carregarDadosColecao(
        colecao: String,
        campo: String,
        callback: (List<Any>) -> Unit
    ) {
        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection(colecao)
            .get()
            .addOnSuccessListener { documents ->
                val result = documents.mapNotNull { it.get(campo) } // Use get() em vez de getString()
                callback(result)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Erro ao obter $colecao: ${exception.message}")
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun editarObra(obraId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Editar Obra")

        val view = layoutInflater.inflate(R.layout.dialog_editar_obra, null)
        val quadraEditText = view.findViewById<EditText>(R.id.editTextQuadra)
        val loteEditText = view.findViewById<EditText>(R.id.editTextLote)
        val observacoesEditText = view.findViewById<EditText>(R.id.editTextObservacoes)
        val proprietariosEditText = view.findViewById<EditText>(R.id.editTextProprietarios)
        val prepostosEditText = view.findViewById<EditText>(R.id.editTextPrepostos)
        val responsaveisTecnicosEditText = view.findViewById<EditText>(R.id.editTextResponsaveisTecnicos)
        val mestresDeObraEditText = view.findViewById<EditText>(R.id.editTextMestresDeObra)
        val emailsEditText = view.findViewById<EditText>(R.id.editTextEmails)
        val telefonesEditText = view.findViewById<EditText>(R.id.editTextTelefones)

        // Buscar os dados da obra para pré-preencher os campos de edição
        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val obra = document.toObject(Obra::class.java)
                    obra?.let {
                        // Preencher os campos com os dados existentes da obra
                        quadraEditText.setText(obra.quadra)
                        loteEditText.setText(obra.lote)
                        observacoesEditText.setText(obra.observacoes)
                        // Buscar e pré-preencher os dados das coleções
                        carregarDadosColecao("proprietario", "nome") { proprietariosResult ->
                            proprietariosEditText.setText(proprietariosResult.joinToString(", "))
                        }
                        carregarDadosColecao("preposto", "nome") { prepostosResult ->
                            prepostosEditText.setText(prepostosResult.joinToString(", "))
                        }
                        carregarDadosColecao("responsaveltecnico", "nome") { responsaveisResult ->
                            responsaveisTecnicosEditText.setText(responsaveisResult.joinToString(", "))
                        }
                        carregarDadosColecao("mestreobras", "nome") { mestresResult ->
                            mestresDeObraEditText.setText(mestresResult.joinToString(", "))
                        }
                        carregarDadosColecao("emails", "nome") { emailsResult ->
                            emailsEditText.setText(emailsResult.joinToString(", "))
                        }
                        carregarDadosColecao("telefones", "nome") { telefonesResult ->
                            telefonesEditText.setText(telefonesResult.joinToString(", "))
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Erro ao obter dados da obra: ${exception.message}")
            }

        builder.setView(view)

        builder.setPositiveButton("Salvar") { dialog, _ ->
            val novaQuadra = quadraEditText.text.toString()
            val novoLote = loteEditText.text.toString()
            val novasObservacoes = observacoesEditText.text.toString()

            // Atualizar os dados da obra diretamente
            atualizarDadosObra(novaQuadra, novoLote, novasObservacoes, obraId)

            // Atualizar coleções associadas à obra
            val novosProprietarios = proprietariosEditText.text.toString().split(", ")
            val novosPrepostos = prepostosEditText.text.toString().split(", ")
            val novosResponsaveisTecnicos = responsaveisTecnicosEditText.text.toString().split(", ")
            val novosMestresDeObra = mestresDeObraEditText.text.toString().split(", ")
            val novosEmails = emailsEditText.text.toString().split(",")
            val novosTelefones = telefonesEditText.text.toString().split(",")

            atualizarColecoesObra(
                obraId,
                novosPrepostos,
                novosMestresDeObra,
                novosProprietarios,
                novosTelefones,
                novosEmails
            )

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }




    private fun atualizarDadosObra(
        novaQuadra: String,
        novoLote: String,
        novasObservacoes: String,
        obraId: String
    ) {
        Log.d(TAG, "Condominio: $condominio")
        Log.d(TAG, "Obra ID: $obraId")

        val obraRef = firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)

        val data = hashMapOf<String, Any>(
            "quadra" to novaQuadra,
            "lote" to novoLote,
            "observacoes" to novasObservacoes
        )

        obraRef.update(data)
            .addOnSuccessListener {
                Log.d(TAG, "Dados da obra atualizados com sucesso")
                addObraEditLog()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao atualizar os dados da obra: ${e.message}")
                exibirMensagem("Erro ao atualizar os dados da obra")
            }
    }



    private fun atualizarColecoesObra(
        obraId: String,
        novosPrepostos: List<String>,
        novosMestresDeObra: List<String>,
        novosProprietarios: List<String>,
        novosTelefones: List<String>,
        novosEmails: List<String>
    ) {
        val firestore = FirebaseFirestore.getInstance()

        val colecoes = mapOf(
            "preposto" to novosPrepostos,
            "mestreobras" to novosMestresDeObra,
            "proprietario" to novosProprietarios,
            "telefones" to novosTelefones,
            "emails" to novosEmails
        )

        colecoes.forEach { (colecao, dados) ->
            val colecaoRef = firestore.collection("Condominios")
                .document(condominio)
                .collection("Obras")
                .document(obraId)
                .collection(colecao)

            colecaoRef.get()
                .addOnSuccessListener { documents ->
                    val dadosExistentes = documents.mapNotNull { it.getString("nome") }
                    val dadosParaAdicionar = dados.filter { it !in dadosExistentes }
                    val dadosParaRemover = dadosExistentes.filter { it !in dados }

                    dadosParaAdicionar.forEach { novoDado ->
                        colecaoRef.add(mapOf("nome" to novoDado))
                    }

                    dadosParaRemover.forEach { dadoRemover ->
                        colecaoRef.whereEqualTo("nome", dadoRemover)
                            .get()
                            .addOnSuccessListener { documentos ->
                                documentos.forEach { documento ->
                                    documento.reference.delete()
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao atualizar coleção $colecao: ${e.message}")
                }
        }
    }



    private fun addObraEditLog() {
        val dataHoraAtual = Calendar.getInstance().time
        val usuarioAtual = FirebaseAuth.getInstance().currentUser?.email

        val logData = hashMapOf(
            "usuario" to usuarioAtual,
            "alteracao" to "Edição de obra",
            "data" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dataHoraAtual),
            "hora" to SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(dataHoraAtual),
            "condominio" to condominio,
            "quadra" to quadra,
            "lote" to lote,
            "obra" to obraId
        )

        firestore.collection("logs")
            .add(logData)
            .addOnSuccessListener { logDoc ->
                Log.d(TAG, "Log de edição de obra adicionado com ID: ${logDoc.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao adicionar log de edição de obra: ${e.message}")
            }
    }

    private fun exibirMensagem(mensagem: String) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
    }

    interface OnDataReadyListener {
        fun onDataReady(
            quadra: String, lote: String, observacoes: String,
            proprietarios: List<String>, prepostos: List<String>,
            responsaveisTecnicos: List<String>, mestresDeObra: List<String>,
            emailsList: List<String>, telefones: List<String>
        )
    }

    data class Obra(
        val quadra: String = "",
        val lote: String = "",
        val observacoes: String = "",
        val proprietarios: List<Any> = emptyList(),
        val prepostos: List<Any> = emptyList(),
        val responsaveisTecnicos: List<Any> = emptyList(),
        val mestresDeObra: List<Any> = emptyList(),
        val emails: List<Any> = emptyList(),
        val telefones: List<Any> = emptyList()
    )

    companion object {
        const val TAG = "DetalhesFragment"
    }
}
