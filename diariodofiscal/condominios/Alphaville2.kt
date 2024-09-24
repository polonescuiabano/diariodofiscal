package com.example.diariodofiscal.condominios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.addobra.AddObraActivity
import com.example.diariodofiscal.adapters.ObrasAdapter
import com.example.diariodofiscal.agenda.diario
import com.example.diariodofiscal.agenda.historico
import com.example.diariodofiscal.atividades.DetalhesObraActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView


class Alphaville2 : AppCompatActivity() {

    private lateinit var obrasAdapter: ObrasAdapter
    private lateinit var searchEditText: EditText
    private lateinit var agendainternaButton: ImageButton
    private lateinit var condominio: String
    lateinit var etiquetaContainer: ViewGroup


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_floraisitalia)



        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewObras)
        recyclerView.layoutManager = LinearLayoutManager(this)
        obrasAdapter = ObrasAdapter(
            adicionarEtiqueta = { obraId, etiqueta ->
                // Lógica para adicionar etiqueta
            },
            clickListener = { obra, quadra, lote, proprietario, email, telefone, obraId->
                val intent = Intent(this@Alphaville2, DetalhesObraActivity::class.java).apply {
                    putExtra("obra", obra)
                    putExtra("quadra", quadra)
                    putExtra("lote", lote)
                    putExtra("proprietario", proprietario)
                    putExtra("email", email)
                    putExtra("telefone", telefone)
                    putExtra("obraId", obraId)
                    putExtra("condominio", condominio)
                }
                startActivity(intent)
            }
        )

        obrasAdapter.setCondominio("Alphaville 2")
        recyclerView.adapter = obrasAdapter

        searchEditText = findViewById(R.id.searchEditText)
        agendainternaButton = findViewById(R.id.agendainterna)

        val firestore = FirebaseFirestore.getInstance()
        condominio = "Alphaville 2" // Definindo o nome do condomínio dinamicamente

        loadObras()

        val addObraButton: Button = findViewById(R.id.addobra)
        addObraButton.setOnClickListener {
            val intent = Intent(this, AddObraActivity::class.java)
            intent.putExtra("condominio", condominio) // Passando o nome do condomínio como parâmetro
            startActivity(intent)
        }

        agendainternaButton.setOnClickListener {
            showMenu()
        }

        val filterButton: ImageButton = findViewById(R.id.filter)
        filterButton.setOnClickListener {
            showFilterMenu(filterButton)
        }




        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.item_obra, null, false)
        val seta: ImageButton = view.findViewById(R.id.seta)
        val cardView: CardView = view.findViewById(R.id.cardview)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_filtro, menu)
        return true
    }



    private fun showMenu() {
        val popupMenu = PopupMenu(this, agendainternaButton)
        popupMenu.menuInflater.inflate(R.menu.menu_agenda, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_acessar_diario -> {
                    // Ação ao clicar em "Acessar Diário de Obras"
                    val intent = Intent(this, historico::class.java)
                    intent.putExtra("condominio", condominio)
                    startActivity(intent)
                    true
                }
                R.id.menu_adicionar_evento -> {
                    // Ação ao clicar em "Adicionar Evento"
                    val intent = Intent(this, diario::class.java)
                    intent.putExtra("condominio", condominio)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }



    private fun loadObras() {
        val firestore = FirebaseFirestore.getInstance()
        val obrasRef = firestore.collection("Condominios").document(condominio).collection("Obras")

        obrasRef.get()
            .addOnSuccessListener { documents ->
                val obrasList = mutableListOf<String>()
                val quadraList = mutableListOf<String>()
                val loteList = mutableListOf<String>()
                val proprietarioList = mutableListOf<String>()
                val emailList = mutableListOf<String>()
                val telefoneList = mutableListOf<String>()
                val obraIdList = mutableListOf<String>()
                for (document in documents) {
                    val quadra = document.getString("quadra")
                    val lote = document.getString("lote")
                    val proprietario = document.getString("proprietario")
                    val email = document.getString("email")
                    val telefone = document.getString("telefone")
                    val obraId = document.id
                    val obra = "Quadra: $quadra, Lote: $lote"
                    obrasList.add(obra)
                    quadraList.add(quadra ?: "")
                    loteList.add(lote ?: "")
                    proprietarioList.add(proprietario ?: "")
                    emailList.add(email ?: "")
                    telefoneList.add(telefone ?: "")
                    obraIdList.add(obraId)
                }
                obrasAdapter.setObras(obrasList, quadraList, loteList, proprietarioList, emailList, telefoneList, obraIdList)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }







    private fun showFilterMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_filtro, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val searchText = searchEditText.text.toString() // Obtém o texto do campo searchEditText
            when (menuItem.itemId) {
                R.id.filter_quadra -> {
                    filterByQuadra(searchText)
                    true
                }
                R.id.filter_lote -> {
                    filterByLote(searchText)
                    true
                }
                R.id.filter_proprietario -> {
                    filterByProprietario(searchText)
                    true
                }
                R.id.action_search -> {
                    loadObras()
                    true
                }
                R.id.filter_vencidos -> {
                    filterVencidos()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun filterByQuadra(quadra: String) {
        Log.d("FilterByQuadra", "Quadra selecionada: $quadra")
        val obrasRef = FirebaseFirestore.getInstance().collection("Condominios")
            .document(condominio).collection("Obras")
            .whereEqualTo("quadra", quadra)

        obrasRef.get()
            .addOnSuccessListener { documents ->
                val obrasList = mutableListOf<String>()
                val quadraList = mutableListOf<String>()
                val loteList = mutableListOf<String>()
                val proprietarioList = mutableListOf<String>()
                val emailList = mutableListOf<String>()
                val telefoneList = mutableListOf<String>()
                val obraIdList = mutableListOf<String>()

                for (document in documents) {
                    val quadra = document.getString("quadra") ?: ""
                    val lote = document.getString("lote") ?: ""
                    val proprietario = document.getString("proprietario") ?: ""
                    val email = document.getString("email") ?: ""
                    val telefone = document.getString("telefone") ?: ""
                    val obraId = document.id

                    val obra = "Quadra: $quadra, Lote: $lote"
                    obrasList.add(obra)
                    quadraList.add(quadra)
                    loteList.add(lote)
                    proprietarioList.add(proprietario)
                    emailList.add(email)
                    telefoneList.add(telefone)
                    obraIdList.add(obraId)
                }

                obrasAdapter.setObras(obrasList, quadraList, loteList, proprietarioList, emailList, telefoneList, obraIdList)
            }
            .addOnFailureListener { exception ->
                Log.e("FilterByQuadra", "Error: $exception")
            }
    }


    private fun filterByLote(lote: String) {
        Log.d("FilterByLote", "Lote selecionado: $lote")
        val obrasRef = FirebaseFirestore.getInstance().collection("Condominios")
            .document(condominio).collection("Obras")
            .whereEqualTo("lote", lote)

        obrasRef.get()
            .addOnSuccessListener { documents ->
                val obrasList = mutableListOf<String>()
                val quadraList = mutableListOf<String>()
                val loteList = mutableListOf<String>()
                val proprietarioList = mutableListOf<String>()
                val emailList = mutableListOf<String>()
                val telefoneList = mutableListOf<String>()
                val obraIdList = mutableListOf<String>()

                for (document in documents) {
                    val quadra = document.getString("quadra") ?: ""
                    val lote = document.getString("lote") ?: ""
                    val proprietario = document.getString("proprietario") ?: ""
                    val email = document.getString("email") ?: ""
                    val telefone = document.getString("telefone") ?: ""
                    val obraId = document.id

                    val obra = "Quadra: $quadra, Lote: $lote"
                    obrasList.add(obra)
                    quadraList.add(quadra)
                    loteList.add(lote)
                    proprietarioList.add(proprietario)
                    emailList.add(email)
                    telefoneList.add(telefone)
                    obraIdList.add(obraId)
                }

                obrasAdapter.setObras(obrasList, quadraList, loteList, proprietarioList, emailList, telefoneList, obraIdList)
            }
            .addOnFailureListener { exception ->
                Log.e("FilterByLote", "Error: $exception")
            }
    }



    private fun filterByProprietario(proprietario: String) {
        Log.d("FilterByProprietario", "Proprietário selecionado: $proprietario")

        val firestore = FirebaseFirestore.getInstance()
        val obrasRef = firestore.collection("Condominios").document(condominio).collection("Obras")

        obrasRef.get()
            .addOnSuccessListener { documents ->
                val filteredObrasList = mutableListOf<String>()
                val filteredQuadraList = mutableListOf<String>()
                val filteredLoteList = mutableListOf<String>()
                val filteredProprietarioList = mutableListOf<String>()
                val filteredEmailList = mutableListOf<String>()
                val filteredTelefoneList = mutableListOf<String>()
                val filteredObraIdList = mutableListOf<String>()

                for (document in documents) {
                    val proprietariosRef = document.reference.collection("proprietario")
                    proprietariosRef.get()
                        .addOnSuccessListener { proprietarioDocuments ->
                            for (proprietarioDocument in proprietarioDocuments) {
                                val nome = proprietarioDocument.getString("nome")
                                if (nome != null && nome.contains(proprietario, ignoreCase = true)) {
                                    // Proprietário encontrado, adiciona a obra à lista filtrada
                                    val quadra = document.getString("quadra") ?: ""
                                    val lote = document.getString("lote") ?: ""
                                    val email = document.getString("email") ?: ""
                                    val telefone = document.getString("telefone") ?: ""
                                    val obraId = document.id

                                    filteredObrasList.add("Quadra: $quadra, Lote: $lote")
                                    filteredQuadraList.add(quadra)
                                    filteredLoteList.add(lote)
                                    filteredProprietarioList.add(nome)
                                    filteredEmailList.add(email)
                                    filteredTelefoneList.add(telefone)
                                    filteredObraIdList.add(obraId)
                                }
                            }

                            // Atualiza o RecyclerView com as obras filtradas
                            obrasAdapter.setObras(
                                filteredObrasList,
                                filteredQuadraList,
                                filteredLoteList,
                                filteredProprietarioList,
                                filteredEmailList,
                                filteredTelefoneList,
                                filteredObraIdList
                            )
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FilterByProprietario", "Error getting proprietarios: ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FilterByProprietario", "Error getting obras: ", exception)
            }
    }



    private fun filterVencidos() {
        val firestore = FirebaseFirestore.getInstance()
        val obrasRef = firestore.collection("Condominios").document(condominio).collection("Obras")

        obrasRef.get()
            .addOnSuccessListener { documents ->
                val obrasList = mutableListOf<String>()
                val quadraList = mutableListOf<String>()
                val loteList = mutableListOf<String>()
                val proprietarioList = mutableListOf<String>()
                val emailList = mutableListOf<String>()
                val telefoneList = mutableListOf<String>()
                val obraIdList = mutableListOf<String>()

                val currentDate = Calendar.getInstance().time

                for (document in documents) {
                    val vistoriasRef = document.reference.collection("vistorias")
                        .orderBy("dataProximaVistoria", Query.Direction.DESCENDING)
                        .limit(1)

                    vistoriasRef.get().addOnSuccessListener { vistoriasDocuments ->
                        val ultimaVistoria = vistoriasDocuments.documents.firstOrNull()
                        val dataProximaVistoria = ultimaVistoria?.getString("dataProximaVistoria")
                        if (dataProximaVistoria != null) {
                            val isVencida = isDataVencida(dataProximaVistoria)
                            Log.d("DataVencida", "Data: $dataProximaVistoria, Vencida: $isVencida")

                            if (isVencida) {
                                val quadra = document.getString("quadra") ?: ""
                                val lote = document.getString("lote") ?: ""
                                val proprietario = document.getString("proprietario") ?: ""
                                val email = document.getString("email") ?: ""
                                val telefone = document.getString("telefone") ?: ""
                                val obraId = document.id

                                val obra = "Quadra: $quadra, Lote: $lote - Vencida"
                                obrasList.add(obra)
                                quadraList.add(quadra)
                                loteList.add(lote)
                                proprietarioList.add(proprietario)
                                emailList.add(email)
                                telefoneList.add(telefone)
                                obraIdList.add(obraId)

                                // Atualize o adaptador aqui dentro, depois de verificar todas as obras vencidas
                                obrasAdapter.setObras(obrasList, quadraList, loteList, proprietarioList, emailList, telefoneList, obraIdList)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FilterVencidos", "Error: $exception")
            }
    }




    private fun isDataVencida(data: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        val dataProximaVistoria: Date = try {
            dateFormat.parse(data)
        } catch (e: Exception) {
            Log.e("DateParsing", "Error parsing date: $e")
            return false
        }
        val isVencida = currentDate.after(dataProximaVistoria) || currentDate == dataProximaVistoria
        Log.d("DataVencida", "Data: $dataProximaVistoria, Vencida: $isVencida")
        return isVencida
    }
}

