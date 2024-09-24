package com.example.diariodofiscal.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class VistoriasFragment : Fragment() {

    private lateinit var obraId: String
    private lateinit var vistoriasRecyclerView: RecyclerView
    private lateinit var addVistoriaButton: Button
    private lateinit var vistoriasAdapter: VistoriasAdapter
    private lateinit var condominio: String
    var quadra: String? = null
    var lote: String? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vistorias, container, false)
        arguments?.let {
            obraId = it.getString("obraId") ?: ""
            condominio = it.getString("condominio") ?: ""
            quadra = it.getString("quadra")
            lote = it.getString("lote")
        }

        // Inicializar os componentes da interface
        vistoriasRecyclerView = view.findViewById(R.id.vistoriasRecyclerView)
        addVistoriaButton = view.findViewById(R.id.addvisto)

        // Configurar o RecyclerView e o adaptador
        vistoriasAdapter = VistoriasAdapter(requireContext(), mutableListOf(),
            onItemClick = { vistoria ->
                Log.d("VistoriasFragment", "Item clicado: $vistoria")
                val intent = Intent(requireContext(), DetalhesVistoria::class.java)
                intent.putExtra("obraId", obraId)
                intent.putExtra("condominio", condominio)
                intent.putExtra("dataVistoria", vistoria.dataVistoria)
                intent.putExtra("dataProximaVistoria", vistoria.dataProximaVistoria)
                intent.putExtra("nomeFiscal", vistoria.nomeFiscal)
                intent.putStringArrayListExtra("imagemUrls", vistoria.imagemUrls)
                intent.putExtra("vistoriaId", vistoria.id)
                intent.putExtra("comentarioFiscal", vistoria.comentarioFiscal)
                val fileUrisList = vistoria.fileUris.toString().split(",") // Dividindo a string em uma lista de strings
                intent.putStringArrayListExtra("fileUris", ArrayList(fileUrisList))

                Log.d("VistoriaFragment", "Item clicado: $vistoria")


                Log.d("VistoriaFragment", "fileUris: ${vistoria.fileUris}")
                startActivity(intent)
            },
            onDeleteClick = { vistoria ->
                onDeleteVistoria(vistoria)
            }
        )

        vistoriasRecyclerView.adapter = vistoriasAdapter
        vistoriasRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Obter os dados das vistorias
        exibirVistorias()


        // Configurar o clique do botão para adicionar vistoria
        addVistoriaButton.setOnClickListener {
            val intent = Intent(requireContext(), AddVistoria::class.java)
            intent.putExtra("obraId", obraId)
            intent.putExtra("condominio", condominio)
            startActivity(intent)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obter os argumentos passados para o fragmento
        obraId = arguments?.getString("obraId") ?: ""
        condominio = arguments?.getString("condominio") ?: ""

        // Verificar se obraId e condominio foram passados corretamente
        if (obraId.isEmpty() || condominio.isEmpty()) {
            Toast.makeText(requireContext(), "ID da obra ou nome do condomínio não estão disponíveis", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }

        // Agora que condominio foi inicializado, você pode usá-lo em outros métodos, como exibirVistorias()
        exibirVistorias()
    }


    private fun exibirVistorias() {
        // Obter os argumentos passados para o fragmento
        obraId = arguments?.getString("obraId") ?: ""
        condominio = arguments?.getString("condominio") ?: ""

        // Verificar se obraId e condominio foram passados corretamente
        if (obraId.isEmpty() || condominio.isEmpty()) {
            Toast.makeText(requireContext(), "ID da obra ou nome do condomínio não estão disponíveis", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }


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
                    val vistoria = document.toObject(VistoriaData::class.java)
                    vistoria.id = document.id // Configurar o ID da vistoria
                    vistoriasList.add(vistoria)
                    Log.d("VistoriasFragment", "ID da vistoria: ${vistoria.id}")
                }

                // Ordenar as vistorias pela data
                vistoriasList.sortBy { it.dataVistoria.toDate() }
                vistoriasAdapter.updateList(vistoriasList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao obter as vistorias: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun onDeleteVistoria(vistoria: VistoriaData) {
        val db = FirebaseFirestore.getInstance()

        val vistoriaId = vistoria.id

        if (vistoriaId.isNullOrEmpty()) {
            Log.e("VistoriasFragment", "ID da vistoria é nulo ou vazio")
            return
        }

        // Construir a caixa de diálogo de confirmação
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmar Exclusão")
        builder.setMessage("Você deseja realmente excluir essa vistoria?")

        // Configurar o botão de confirmação
        builder.setPositiveButton("Sim") { dialog, which ->
            // Excluir a vistoria
            db.collection("Condominios")
                .document(condominio)
                .collection("Obras")
                .document(obraId)
                .collection("vistorias")
                .document(vistoriaId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Vistoria excluída com sucesso!", Toast.LENGTH_SHORT).show()
                    vistoriasAdapter.removeVistoria(vistoria)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Erro ao excluir vistoria: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Configurar o botão de cancelamento
        builder.setNegativeButton("Cancelar") { dialog, which ->
            // Não fazer nada, apenas fechar a caixa de diálogo
            dialog.dismiss()
        }

        // Mostrar a caixa de diálogo de confirmação
        val dialog = builder.create()
        dialog.show()
    }




    private fun String.toDate(): Date {
        val pattern = "dd/MM/yyyy"
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.parse(this) ?: Date(0)
    }

}