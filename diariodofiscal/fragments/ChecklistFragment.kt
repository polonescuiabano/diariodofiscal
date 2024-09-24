package com.example.diariodofiscal.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.adapters.ChecklistAdapter
import com.example.diariodofiscal.model.ChecklistItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class ChecklistFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ChecklistAdapter
    private lateinit var recyclerView: RecyclerView
    private var obraId: String = ""
    private var condominio: String = ""
    private lateinit var btnSalvar: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_checklist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        arguments?.let {
            obraId = it.getString("obraId") ?: ""
            condominio = it.getString("condominio") ?: ""
        }

        recyclerView = view.findViewById(R.id.recyclerViewChecklist)
        btnSalvar = view.findViewById(R.id.btnSalvar) // Adicione esta linha para acessar o botão

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChecklistAdapter(
            requireContext(),
            mutableListOf(),
            recyclerView,  // Passe a referência do recyclerView aqui
            db,            // Passe a referência do Firestore aqui
            condominio,    // Passe o ID do condomínio aqui
            obraId         // Passe o ID da obra aqui
        )

        recyclerView.adapter = adapter

        loadChecklist()

        btnSalvar.setOnClickListener {
            saveChecklist()
        }
    }


    private fun loadChecklist() {
        val checklistRef = db.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("items")

        checklistRef.get()
            .addOnSuccessListener { result ->
                val checklistItems = mutableListOf<ChecklistItem>()
                for (document in result) {
                    val item = document.toObject(ChecklistItem::class.java)
                    // Aqui você adiciona o timestamp ao item do checklist
                    val timestamp = document.getTimestamp("dataMarcacao")
                    item.dataMarcacao = timestamp?.toDate()
                    checklistItems.add(item)
                }

                if (checklistItems.isEmpty()) {
                    // Se o checklist estiver vazio, carregue o checklist padrão
                    val defaultChecklistItems = getDefaultChecklistItems()
                    adapter.setChecklistItems(defaultChecklistItems)
                } else {
                    // Limpe a lista antes de adicionar os itens carregados
                    adapter.getChecklistItems().clear()
                    adapter.setChecklistItems(checklistItems)
                }
            }
            .addOnFailureListener { exception ->
                Snackbar.make(requireView(), "Erro ao carregar checklist", Snackbar.LENGTH_SHORT).show()
            }
    }


    private fun getDefaultChecklistItems(): List<ChecklistItem> {
        return listOf(
            ChecklistItem("1", "Altura de corte no máximo 1,50 m","","",null),
            ChecklistItem("2", "Altura de aterro no máximo 1,50 m","","",null),
            ChecklistItem("3", "Altura de arrimo no máximo 1,50 m","","",null),
            ChecklistItem("4", "Altura do muro 3,00m","","",null),
            ChecklistItem("5", "Autorização de altura acima de 3,00m até 3,50m a partir da linha natural do terreno","","",null),
            ChecklistItem("6", "Reboco na parte externa","","",null),
            ChecklistItem("7", "Respeitando o recuo frontal (5 metros)","","",null),
            ChecklistItem("8", "Divisa com área verde (altura mureta máximo 0,50cm)","","",null),
            ChecklistItem("9", "Divisa com área verde (tela metálica, brise, cerca viva...)","","",null),
            ChecklistItem("10", "Autorização do lote de apoio","","",null),
            ChecklistItem("11", "Fazer portão para pedestre","","",null),
            ChecklistItem("12", "Altura do tapume 2 metros","","",null),
            ChecklistItem("13", "Pintura na cor branca (duas mão de pintura - continua)","","",null),
            ChecklistItem("14", "Instalação do mesmo modelo na mesma face (ondulado ou trapezoidal)","","",null),
            ChecklistItem("15", "Bom estado de conservação (sem rasgo, furos  aparentes e amassado)","","",null),
            ChecklistItem("16", "Vigotas ou caibros na altura limite do tapume","","",null),
            ChecklistItem("17", "Instalado em todo perímetro do lote (obra e apoio)","","",null),
            ChecklistItem("18", "Barração (parte superior ao tapume em chapa galvanizada)","","",null),
            ChecklistItem("19", "Bebedouro ou geladeira*","","",null),
            ChecklistItem("20", "Lixeira externa (padrão condomínio)","","",null),
            ChecklistItem("21", "Placa de obra","","",null),
            ChecklistItem("22", "Banheiro (instalação no esgoto do lote da obra)","","",null),
            ChecklistItem("23", "Caixa de Inspenção - limpeza e vedação","","",null),
            ChecklistItem("24", "Brita na calçada e retirada de vegetação (lote de apoio e obras)","","",null),
            ChecklistItem("25", "Energia própria da obra","","",null),
            ChecklistItem("26", "Tubulação e fiação subterrânea","","",null),
            ChecklistItem("27", "Ligação de água da própria obra","","",null),
            ChecklistItem("28", "Conferência de gabarito","","",null),
            ChecklistItem("29", "Pasta da obra (projetos, autorizações)","","",null),
            ChecklistItem("30", "Conferência de recuos antes da concretagem da baldrame","","",null),
            ChecklistItem("31", "Altura de garagem 5m a partir da linha natural do terreno","","",null),
            ChecklistItem("32", "Altura da edificação secundária","","",null),
            ChecklistItem("33", "Tela de proteção acompanhando a altura da edificação","","",null),
            ChecklistItem("34", "Marquises (antes da concretagem)","","",null),
            ChecklistItem("35", "Elemento arquitetônico nos recuos","","",null),
            ChecklistItem("36", "Casa de maquinas e abrigo de gás altura máxima 1,50m ","","",null),
            ChecklistItem("37", "Shaft (20cm) não pode na servidão","","",null),
            ChecklistItem("38", "Cisterna e poço artesiano (não pode na servidão)","","",null),
            ChecklistItem("39", "Cascata limitada a altura do muro","","",null),
            ChecklistItem("40", "Passagem da servidão","","",null),
            ChecklistItem("41", "Autorização para passagem da servidão","","",null),
            ChecklistItem("42", "Fonte ou espelho d'água nos recuos","","",null),
            ChecklistItem("43", "Pergolado dentro do recuo (sem cobertura)","","",null),
            ChecklistItem("44", "Altura do pergolado 3,0m","","",null),
            ChecklistItem("45", "Taxa de permeabilidade 25%","","",null),
            ChecklistItem("46", "Conferência do projeto com a execução","","",null),
            ChecklistItem("47", "Calçada padrão do condomínio concreto desempenado","","",null),
            ChecklistItem("48", "Calçada (1,25m + 0,75m)","","",null),
            ChecklistItem("49", "Calçada não pode conter rampas ou degraus ","","",null),
            ChecklistItem("50", "Acabamento do meio fio","","",null),
            ChecklistItem("51", "Selador externo muro","","",null),
            ChecklistItem("52", "Muro de arrimo inicio do nível 0 na calçada","","",null),
            ChecklistItem("53", "Cerca viva (quando houver)","","",null),
            ChecklistItem("54", "Todas esquadrias instaladas","","",null),
            ChecklistItem("55", "Pintura finalizada","","",null),
            ChecklistItem("56", "Calçada pronta","","",null),
            ChecklistItem("57", "Revestimentos instalados","","",null),
            ChecklistItem("58", "Reposição de curva de nível lote de apoio","","",null),
            ChecklistItem("59", "Limpeza do lote de apoio","","",null),
            ChecklistItem("60", "Limpeza no entorno da obra","","",null),
            ChecklistItem("61", "Devolução do lote de apoio da maneira que foi encontrado","","",null),
            ChecklistItem("62", "Teste de água pluvial e esgoto","","",null),
            ChecklistItem("63", "Conferir execução com projeto de as built","","",null),
            ChecklistItem("64", "Formulário - Solicitação de vistoria final","","",null),
            ChecklistItem("65", "Formulário - Solicitação de mudança","","",null),
            ChecklistItem("66", "Acabamento de guia e meio-fio","","",null),
            ChecklistItem("67", "Protocolo do habite-se","","",null)
        )
    }
    private fun saveChecklist() {
        val checklistRef = db.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection("items")

        val checklistItems = adapter.getChecklistItems()

        for (item in checklistItems) {
            checklistRef.document(item.id).set(item)
                .addOnSuccessListener {
                    Snackbar.make(requireView(), "Checklist salvo com sucesso", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(requireView(), "Erro ao salvar checklist", Snackbar.LENGTH_SHORT).show()
                }
        }
    }
}


