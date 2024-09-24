package com.example.diariodofiscal.addobra

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.diariodofiscal.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.DocumentReference

class AddObraActivity : AppCompatActivity() {

    private lateinit var quadraEditText: EditText
    private lateinit var loteEditText: EditText
    private lateinit var proprietarioEditText: EditText
    private lateinit var prepostoEditText: EditText
    private lateinit var responsaveltecnicoEditText: EditText
    private lateinit var mestredeobrasEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var telefoneEditText: EditText
    private lateinit var observacoesEditText: EditText
    private lateinit var firestore: FirebaseFirestore
    private lateinit var condominio: String
    private lateinit var proprietarioLayout: LinearLayout
    private lateinit var responsavelTecnicoLayout: LinearLayout
    private lateinit var mestreObrasLayout: LinearLayout
    private lateinit var prepostoLayout: LinearLayout
    private lateinit var emailLayout: LinearLayout
    private lateinit var telefoneLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addobra)

        firestore = FirebaseFirestore.getInstance()

        quadraEditText = findViewById(R.id.desc)
        loteEditText = findViewById(R.id.lote)
        proprietarioEditText = findViewById(R.id.proprietario)
        prepostoEditText = findViewById(R.id.preposto)
        responsaveltecnicoEditText = findViewById(R.id.responsavelTecnico)
        mestredeobrasEditText = findViewById(R.id.mestreObras)
        emailEditText = findViewById(R.id.email)
        telefoneEditText = findViewById(R.id.telefone)
        observacoesEditText = findViewById(R.id.observacoes)
        proprietarioLayout = findViewById(R.id.proprietarioLayout)
        responsavelTecnicoLayout = findViewById(R.id.responsavelTecnicoLayout)
        mestreObrasLayout = findViewById(R.id.mestreObrasLayout)
        prepostoLayout = findViewById(R.id.prepostoLayout)
        emailLayout = findViewById(R.id.emailLayout)
        telefoneLayout = findViewById(R.id.telefoneLayout)

        condominio = intent.getStringExtra("condominio") ?: ""

        val addButtonProprietario: ImageView = findViewById(R.id.addbutton)
        addButtonProprietario.setOnClickListener {
            val textoprop = proprietarioEditText
            adicionarEtiqueta(proprietarioLayout, textoprop)
        }

        val addButtonResponsavelTecnico: ImageView = findViewById(R.id.addbuttonRT)
        addButtonResponsavelTecnico.setOnClickListener {
            val rt = responsaveltecnicoEditText
            adicionarEtiqueta(responsavelTecnicoLayout, rt)
        }

        val addButtonMestreObras: ImageView = findViewById(R.id.addbuttonMO)
        addButtonMestreObras.setOnClickListener {
            val mo = mestredeobrasEditText
            adicionarEtiqueta(mestreObrasLayout, mo)
        }

        val addButtonPreposto: ImageView = findViewById(R.id.addbuttonPreposto)
        addButtonPreposto.setOnClickListener {
            val preposto = prepostoEditText
            adicionarEtiqueta(prepostoLayout, preposto)
        }

        val addButtonEmail: ImageView = findViewById(R.id.addbuttonemail)
        addButtonEmail.setOnClickListener {
            val emailedit = emailEditText
            adicionarEtiqueta(emailLayout, emailedit)
        }

        val addButtonTelefone: ImageView = findViewById(R.id.addbuttontelefone)
        addButtonTelefone.setOnClickListener {
            val telefoneedit = telefoneEditText
            adicionarEtiqueta(telefoneLayout, telefoneedit)
        }

        val saveButton: Button = findViewById(R.id.btnsalvar)
        saveButton.setOnClickListener {
            salvarDados()
        }
    }

    private fun adicionarEtiqueta(layout: LinearLayout, editText: EditText) {
        val texto = editText.text.toString().trim()
        if (texto.isNotEmpty()) {
            val novoTextView = TextView(this)
            novoTextView.text = texto
            layout.addView(novoTextView)
        }
    }


    private var isSalvando = false // Variável para controlar se os dados estão sendo salvos

    private fun salvarDados() {
        if (isSalvando) {
            // Se já estiver salvando, ignore o clique do botão
            return
        }

        isSalvando = true // Marcamos que estamos iniciando o processo de salvar

        val quadra = quadraEditText.text.toString().trim()
        val lote = loteEditText.text.toString().trim()
        val observacoes = observacoesEditText.text.toString().trim() // Obtendo as observações

        if (quadra.isEmpty() || lote.isEmpty()) {
            Toast.makeText(this, "Preencha a quadra e o lote", Toast.LENGTH_SHORT).show()
            isSalvando = false // Marque que o processo de salvar terminou
            return
        }

        // Verificar se a quadra e o lote já existem
        val obrasRef = firestore.collection("Condominios")
            .document(condominio)
            .collection("Obras")
            .whereEqualTo("quadra", quadra)
            .whereEqualTo("lote", lote)

        obrasRef.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(this, "Quadra e lote já existem", Toast.LENGTH_SHORT).show()
                    isSalvando = false // Marque que o processo de salvar terminou
                } else {
                    val obra = hashMapOf(
                        "quadra" to quadra,
                        "lote" to lote,
                        "observacoes" to observacoes // Incluindo observações nos dados da obra
                    )

                    val novaObraRef = firestore.collection("Condominios")
                        .document(condominio)
                        .collection("Obras")
                        .document()

                    novaObraRef.set(obra)
                        .addOnSuccessListener {
                            salvarDadosAdicionais(novaObraRef)
                            Toast.makeText(this, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao salvar os dados: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        .addOnCompleteListener {
                            isSalvando = false // Marque que o processo de salvar terminou
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao verificar a existência da obra: ${e.message}", Toast.LENGTH_SHORT).show()
                isSalvando = false // Marque que o processo de salvar terminou
            }
    }




    private fun salvarDadosAdicionais(obrasRef: DocumentReference) {
        val layouts = listOf(
            proprietarioLayout,
            responsavelTecnicoLayout,
            mestreObrasLayout,
            prepostoLayout,
            emailLayout, // Layout de email
            telefoneLayout // Layout de telefone
        )

        layouts.forEach { layout ->
            val etiqueta = layout.tag.toString() // Obtém a tag do layout
            val dados = mutableListOf<String>()
            for (i in 0 until layout.childCount) {
                val campo = layout.getChildAt(i) as? TextView // Usamos TextView em vez de EditText
                campo?.let {
                    val texto = campo.text.toString().trim()
                    if (texto.isNotEmpty()) {
                        dados.add(texto)
                    }
                }
            }
            if (dados.isNotEmpty()) {
                dados.forEach { texto ->
                    obrasRef.collection(etiqueta).add(hashMapOf("nome" to texto))
                }
            }
        }
    }
}