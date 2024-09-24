package com.example.diariodofiscal.atividades

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.diariodofiscal.databinding.ActivityDetalhesObraBinding
import com.example.diariodofiscal.fragments.ArquivosFragment
import com.example.diariodofiscal.fragments.ChecklistFragment
import com.example.diariodofiscal.fragments.DetalhesFragment
import com.example.diariodofiscal.fragments.VistoriasFragment
import com.example.diariodofiscal.model.vistoria
import com.example.diariodofiscal.notificacoes.notificacoes
import com.example.diariodofiscal.vistorias.VistoriaData
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore

class DetalhesObraActivity : AppCompatActivity(), DetalhesFragment.OnDataReadyListener {

    private lateinit var binding: ActivityDetalhesObraBinding
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var obraId: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var condominio: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesObraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        val adapter = ViewPagerAdapter(supportFragmentManager)

        binding.btnVistorias.setOnClickListener {
            val intent = Intent(this, notificacoes::class.java).apply {
                putExtra("condominio", condominio)
                putExtra("obraId", obraId)
            }
            startActivity(intent)
        }

        obraId = intent.getStringExtra("obraId") ?: ""
        condominio = intent.getStringExtra("condominio") ?: ""


            firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val obraData = document.data

                    if (obraData != null) {
                        val quadra = obraData["quadra"] as? String ?: ""
                        val lote = obraData["lote"] as? String ?: ""

                        val vistoriasFragment = VistoriasFragment().apply {
                            val args = Bundle().apply {
                                putString("obraId", obraId)
                                putString("condominio", condominio)
                                putString("quadra", quadra)
                                putString("lote", lote)
                            }
                            arguments = args
                        }

                        // Aqui você pode adicionar o vistoriasFragment ao adaptador
                        adapter.addFragment(vistoriasFragment, "Vistorias")
                    }
                }
            }




        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val obraData = document.data

                    if (obraData != null) {
                        val quadra = obraData["quadra"] as? String ?: ""
                        val lote = obraData["lote"] as? String ?: ""

        val checklistFragment = ChecklistFragment().apply {
            val args = Bundle().apply {
                putString("obraId", obraId)
                putString("condominio", condominio)
                putString("quadra", quadra) // Adicionando a quadra ao Bundle
                putString("lote", lote) // Adicionando o lote ao Bundle
            }
            arguments = args
        }

        adapter.addFragment(checklistFragment, "Checklist")
        }
    }
}

        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val obraData = document.data


                    if (obraData != null) {
                        val quadra = obraData["quadra"] as? String ?: ""
                        val lote = obraData["lote"] as? String ?: ""

                        val detalhesFragment = DetalhesFragment().apply {
                            arguments = Bundle().apply {
                                putString("quadra", quadra)
                                putString("lote", lote)
                                putString("condominio", condominio)
                                putString("obraId", obraId)


                                retrieveCollectionData(condominio, obraId, "proprietario") { proprietariosList ->
                                    putStringArrayList("proprietario", ArrayList(proprietariosList))
                                }

                                retrieveCollectionData(condominio, obraId, "prepostos") { prepostosList ->
                                    putStringArrayList("prepostos", ArrayList(prepostosList))
                                }

                                retrieveCollectionData(condominio, obraId, "responsaveltecnico") { responsaveisTecnicosList ->
                                    putStringArrayList("responsaveltecnico", ArrayList(responsaveisTecnicosList))
                                }

                                retrieveCollectionData(condominio, obraId, "mestreobras") { mestresObrasList ->
                                    putStringArrayList("mestreobras", ArrayList(mestresObrasList))
                                }

                                retrieveCollectionData(condominio, obraId, "emails") { emailsList ->
                                    putStringArrayList("emails", ArrayList(emailsList))
                                }

                                retrieveCollectionData(condominio, obraId, "telefones") { telefonesList ->
                                    putStringArrayList("telefones", ArrayList(telefonesList))
                                }
                            }

                        }


                        val arquivosFragment = ArquivosFragment.newInstance(obraId, condominio)

                        adapter.addFragment(detalhesFragment, "Detalhes")
                        adapter.addFragment(arquivosFragment, "Arquivos")


                        viewPager.adapter = adapter
                        tabLayout.setupWithViewPager(viewPager)

                    }
                } else {
                    Log.e(TAG, "Documento não encontrado para o ID da obra: $obraId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao obter detalhes da obra: $exception")
            }
    }

    class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList: MutableList<Fragment> = ArrayList()
        private val fragmentTitleList: MutableList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }
    }

    override fun onDataReady(quadra: String, lote: String, observacoes: String,
                             proprietario: List<String>, prepostos: List<String>,
                             responsaveisTecnicos: List<String>, mestresDeObra: List<String>,
                             emailsList: List<String>, telefones: List<String>) {
        // Implemente o que precisa ser feito com os dados
    }

    companion object {
        const val TAG = "DetalhesObraActivity"
    }

    private fun retrieveCollectionData(condominio: String, obraId: String, collectionName: String, action: (List<String>) -> Unit) {
        firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .document(obraId)
            .collection(collectionName)
            .get()
            .addOnSuccessListener { documents ->
                val dataList = mutableListOf<String>()
                for (document in documents) {
                    val nome = document["nome"] as? String
                    nome?.let { dataList.add(it)
                        Log.d(TAG, "Dados da coleção $collectionName recuperados: $it")
                    }
                }
                action(dataList)
            }
            .addOnFailureListener { e ->
                // Trate falhas na obtenção dos dados da coleção
            }
    }
}
