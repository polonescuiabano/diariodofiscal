package com.example.diariodofiscal.atividades

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diariodofiscal.adapters.SelectedImagesAdapter
import com.example.diariodofiscal.databinding.ActivityDetalhesVistoriaBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.Target



class DetalhesVistoria : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesVistoriaBinding
    private lateinit var arquivoUrls: MutableList<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var context: Context
    private var condominio: String? = null
    private var obraId: String? = null
    private var vistoriaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesVistoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        condominio = intent.getStringExtra("condominio")
        obraId = intent.getStringExtra("obraId")
        vistoriaId = intent.getStringExtra("vistoriaId")

        context = this
        firestore = FirebaseFirestore.getInstance()

        val dataVistoria = intent.getStringExtra("dataVistoria") ?: ""
        val dataProximaVistoria = intent.getStringExtra("dataProximaVistoria") ?: ""
        val nomeFiscal = intent.getStringExtra("nomeFiscal") ?: ""
        val comentarioFiscal = intent.getStringExtra("comentarioFiscal") ?: ""
        Log.d("DetalhesVistoria", "VistoriaId recebido: $vistoriaId")

        binding.textViewDataVistoria.text = "Data da Vistoria: $dataVistoria"
        binding.textViewDataProximaVistoria.text = "Próxima Vistoria: $dataProximaVistoria"
        binding.textViewNomeFiscal.text = "Fiscal: $nomeFiscal"
        binding.textViewComentarioFiscal.text = "Comentário do Fiscal: $comentarioFiscal"

        binding.recyclerViewImagens.layoutManager = LinearLayoutManager(this)
        arquivoUrls = mutableListOf()
        retrieveFileUrlsFromFirebaseStorage()
    }

    private fun retrieveFileUrlsFromFirebaseStorage() {
        vistoriaId?.let { vistoriaId ->
            val storageRef = FirebaseStorage.getInstance().reference
            val filesRef = storageRef.child("vistorias/$vistoriaId/")

            filesRef.listAll()
                .addOnSuccessListener { listResult ->
                    listResult.items.forEachIndexed { index, fileRef ->
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val fileUrl = uri.toString()
                            arquivoUrls.add(fileUrl)
                            Log.d("DetalhesVistoria", "URL do arquivo adicionada [$index]: $fileUrl")
                            if (arquivoUrls.size == listResult.items.size) {
                                setupRecyclerView()
                                loadImagesIntoAdapter(arquivoUrls)
                            }
                        }.addOnFailureListener { exception ->
                            Log.w("DetalhesVistoria", "Erro ao obter URL do arquivo", exception)
                        }
                    }
                    Log.d("DetalhesVistoria", "URLs dos arquivos recuperadas com sucesso")
                }
                .addOnFailureListener { exception ->
                    Log.w("DetalhesVistoria", "Erro ao listar arquivos no Firebase Storage", exception)
                }
        }
    }

    private fun setupRecyclerView() {
        val adapter = SelectedImagesAdapter(arquivoUrls.map { Uri.parse(it) }.toMutableList())

        adapter.setOnItemClickListener { uri ->
            uri?.let {
                openFileInBrowser(this, uri.toString())
            }
        }

        binding.recyclerViewImagens.adapter = adapter
    }

    private fun loadImagesIntoAdapter(urls: List<String>) {
        val adapter = binding.recyclerViewImagens.adapter as? SelectedImagesAdapter
        adapter?.let {
            urls.forEach { imageUrl ->
                Glide.with(this)
                    .load(imageUrl)
                    .override(200,200)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("Glide", "Falha ao carregar imagem: $e")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("Glide", "Imagem carregada com sucesso: $model")
                            adapter.addUri(Uri.parse(imageUrl))
                            return true
                        }
                    })
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            // Não é necessário fazer nada aqui, já que a imagem já foi processada no listener
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Implementação opcional se desejar realizar alguma ação quando a imagem for removida
                        }
                    })
            }
        }
    }


    fun launchCustomTab(context: Context, url: String) {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    private fun openFileInBrowser(context: Context, url: String) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        launchCustomTab(context, url)
    }
}
