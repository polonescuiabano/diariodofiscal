package com.example.diariodofiscal.notificacoes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.diariodofiscal.R
import com.google.firebase.firestore.FirebaseFirestore

class enviarocorrencia : AppCompatActivity() {
    private lateinit var telefoneSelecionado: String
    private lateinit var emailSelecionado: String
    private lateinit var spinnerTelefone: Spinner
    private lateinit var spinnerEmail: Spinner
    private lateinit var btnEnviar: Button
    private lateinit var condominio: String
    private lateinit var obraId: String

    private val telefonesList = mutableListOf<String>()
    private val emailsList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enviarocorrencia)

        // Inicializar views
        spinnerTelefone = findViewById(R.id.spinnerTelefone)
        spinnerEmail = findViewById(R.id.spinnerEmail)
        btnEnviar = findViewById(R.id.btnEnviar)

        // Recuperar o condomínio e o obraId da Intent
        condominio = intent.getStringExtra("condominio") ?: ""
        obraId = intent.getStringExtra("obraId") ?: ""

        // Recuperar os emails e telefones
        recuperarEmails()
        recuperarTelefones()

        btnEnviar.setOnClickListener {
            enviarOcorrencia()
        }
    }

    private fun recuperarEmails() {
        val firestore = FirebaseFirestore.getInstance()
        val emailsRef = firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("emails")

        emailsRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val email = document.getString("nome") ?: ""
                    emailsList.add(email)
                    Log.d("enviarocorrencia", "Email recuperado: $email")
                }
                configurarSpinnerEmail()
            }
            .addOnFailureListener { exception ->
                Log.e("enviarocorrencia", "Erro ao recuperar emails: $exception")
            }
    }

    private fun recuperarTelefones() {
        val firestore = FirebaseFirestore.getInstance()
        val telefonesRef = firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("telefones")

        telefonesRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val telefone = document.getString("nome") ?: ""
                    telefonesList.add(telefone)
                    Log.d("enviarocorrencia", "Telefone recuperado: $telefone")
                }
                configurarSpinnerTelefone()
            }
            .addOnFailureListener { exception ->
                Log.e("enviarocorrencia", "Erro ao recuperar telefones: $exception")
            }
    }

    private fun configurarSpinnerTelefone() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, telefonesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTelefone.adapter = adapter

        // Configurar listener para selecionar o telefone
        spinnerTelefone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                telefoneSelecionado = telefonesList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implementação opcional caso necessário
            }
        }
    }

    private fun configurarSpinnerEmail() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, emailsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEmail.adapter = adapter

        // Configurar listener para selecionar o e-mail
        spinnerEmail.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                emailSelecionado = emailsList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implementação opcional caso necessário
            }
        }
    }



    private fun enviarOcorrencia() {
        // Implemente a lógica para enviar a ocorrência
        val ocorrencia = intent.getStringExtra("ocorrencia") ?: ""

        try {
            // Verificar se o número de telefone está disponível
            if (::telefoneSelecionado.isInitialized) {
                val whatsappIntent = Intent(Intent.ACTION_SEND)
                whatsappIntent.type = "text/plain"
                whatsappIntent.`package` = "com.whatsapp"
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Ocorrência: $ocorrencia")
                whatsappIntent.putExtra("jid", "$telefoneSelecionado@s.whatsapp.net")
                startActivity(whatsappIntent)
            }
        } catch (e: ActivityNotFoundException) {
            // Lidar com a exceção quando não há aplicativo WhatsApp disponível
            Log.e("enviarocorrencia", "Nenhum aplicativo WhatsApp encontrado", e)
            // Adicione aqui o código para notificar o usuário ou lidar com a situação adequadamente
        }

        // Verificar se o endereço de e-mail está disponível
        if (::emailSelecionado.isInitialized) {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "text/plain"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailSelecionado))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Nova ocorrência")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Ocorrência: $ocorrencia")
            startActivity(emailIntent)
        }
    }
    }

