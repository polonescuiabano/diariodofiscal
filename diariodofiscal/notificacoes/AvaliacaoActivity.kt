package com.example.diariodofiscal.notificacoes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import com.example.diariodofiscal.R
import com.example.diariodofiscal.addocorrencia
import okhttp3.*
import java.io.IOException
import javax.activation.DataHandler
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


class AvaliacaoActivity : AppCompatActivity() {

    private lateinit var edtDataRetorno: EditText
    private lateinit var edtObservacoes: EditText
    private lateinit var condominio: String
    private lateinit var obraId: String
    private lateinit var ocorrenciaId: String


    private lateinit var calendar: Calendar

    private lateinit var firestore: FirebaseFirestore


    private val imagesList = mutableListOf<Uri>()

    private val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        imagesList.clear()
        imagesList.addAll(uris)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avaliacao)

        edtDataRetorno = findViewById(R.id.edtDataRetorno)
        edtObservacoes = findViewById(R.id.edtObservacoes)
        firestore = FirebaseFirestore.getInstance()

        condominio = intent.getStringExtra("condominio") ?: ""
        obraId = intent.getStringExtra("obraId") ?: ""
        ocorrenciaId = intent.getStringExtra("ocorrenciaId") ?: ""



        calendar = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtDataRetorno.setText(dateFormat.format(calendar.time))

        findViewById<Button>(R.id.btnAdd7Days).setOnClickListener {
            addDaysToDate(7)
        }

        findViewById<Button>(R.id.btnAdd15Days).setOnClickListener {
            addDaysToDate(15)
        }

        findViewById<Button>(R.id.btnAdd21Days).setOnClickListener {
            addDaysToDate(21)
        }

        findViewById<Button>(R.id.btnAdd30Days).setOnClickListener {
            addDaysToDate(30)
        }

        findViewById<Button>(R.id.btnAnexarImagens).setOnClickListener {
            pickImages.launch("image/*")
        }

        findViewById<Button>(R.id.btnSalvarAvaliacao).setOnClickListener {
            saveAvaliacaoToFirestore()
        }
    }

    private fun addDaysToDate(days: Int) {
        calendar.add(Calendar.DAY_OF_MONTH, days)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtDataRetorno.setText(dateFormat.format(calendar.time))
    }

    private fun uploadImagesToFirebaseStorage(avaliacaoId: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images").child(avaliacaoId)

        imagesList.forEachIndexed { index, uri ->
            val imageRef = storageRef.child("image_${index + 1}")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    Toast.makeText(this, "Imagem ${index + 1} enviada com sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao enviar a imagem ${index + 1}: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun saveAvaliacaoToFirestore() {
        val condominio = intent.getStringExtra("condominio") ?: ""
        val obraId = intent.getStringExtra("obraId") ?: ""
        val ocorrenciaId = intent.getStringExtra("ocorrenciaId") ?: ""

        Log.d("AvaliacaoActivity", "Condomínio: $condominio")

        val dataRetorno = edtDataRetorno.text.toString()
        val observacoes = edtObservacoes.text.toString()

        val firestore = FirebaseFirestore.getInstance()
        val avaliacoesRef = firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("Ocorrencias").document(ocorrenciaId)
            .collection("Avaliacoes")

        // Consultar as avaliações existentes para obter o maior número de avaliação
        avaliacoesRef.get()
            .addOnSuccessListener { documents ->
                var maiorNumeroAvaliacao = 0
                for (document in documents) {
                    val numeroAvaliacao = document.getLong("numerodaavaliacao") ?: 0
                    if (numeroAvaliacao > maiorNumeroAvaliacao) {
                        maiorNumeroAvaliacao = numeroAvaliacao.toInt()
                    }
                }
                val novoNumeroAvaliacao = maiorNumeroAvaliacao + 1

                // Agora, você pode salvar a nova avaliação com o novo número
                val avaliacao = hashMapOf(
                    "numerodaavaliacao" to novoNumeroAvaliacao,
                    "dataRetorno" to dataRetorno,
                    "observacoes" to observacoes
                )

                // Adiciona a nova avaliação ao Firestore
                avaliacoesRef.add(avaliacao)
                    .addOnSuccessListener { documentReference ->
                        val avaliacaoId = documentReference.id // Obtém o ID da avaliação recém-criada
                        uploadImagesToFirebaseStorage(avaliacaoId) // Chama o método de upload de imagens passando o ID da avaliação
                        Toast.makeText(this, "Avaliação salva com sucesso", Toast.LENGTH_SHORT).show()

                        // Verifica se o número da avaliação é 2
                        if (novoNumeroAvaliacao == 2 &&
                                (condominio == "Primor das Torres" ||
                                    condominio == "Florais Itália" ||
                                    condominio == "Villa Jardim")) {
                            // Se for, chama a função para enviar as informações para o código Python
                            enviarInformacoesParaCodigoPython(obraId, dataRetorno, observacoes)
                        }

                        finish() // Finaliza a atividade após salvar a avaliação
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Erro ao salvar a avaliação: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao consultar as avaliações: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun enviarParaCodigoPython(json: String, obraId: String) {
        val client = OkHttpClient()
        val body = RequestBody.create(MediaType.parse("application/json"), json)
        val request = Request.Builder()
            .url("http://192.168.0.8:5000/gerar_pdfav")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("AvaliacaoActivity:", "Erro ao enviar informações para o código Python", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Log.d(addocorrencia.TAG, "Resposta do servidor Python: $responseBody")

                    // Chamar a função para recuperar e enviar e-mails
                    recuperarEmailsDaObraId(obraId, responseBody)
                    buscarTelefonesDaObraId(obraId) { telefones ->
                        enviarPDFParaTelefones(responseBody, telefones)
                    }
                } else {
                    Log.e(addocorrencia.TAG, "Erro na resposta do servidor Python: ${response.code()}")
                }
            }
        })
    }

    private fun enviarInformacoesParaCodigoPython(obraId: String, dataRetorno: String, observacoes: String) {
        val jsonData = "{\"dataRetorno\": \"$dataRetorno\", \"observacoes\": \"$observacoes\"}"
        enviarParaCodigoPython(jsonData, obraId)
        enviarImagensParaCodigoPython(obraId)
    }

    private fun buscarTelefonesDaObraId(obraId: String, onComplete: (List<String>) -> Unit) {
        val telefones = mutableListOf<String>()

        val obraRef = firestore.collection("Condominios").document(condominio).collection("Obras").document(obraId)
        obraRef.collection("telefones").get().addOnSuccessListener { telefonesSnapshot ->
            for (doc in telefonesSnapshot.documents) {
                val telefone = doc.getString("nome")
                if (telefone != null) {
                    telefones.add(telefone)
                }
            }
            onComplete(telefones)
        }.addOnFailureListener { exception ->
            Log.e(addocorrencia.TAG, "Erro ao buscar telefones da obraId $obraId", exception)
            // Se ocorrer um erro, você pode lidar com ele aqui
        }
    }

    private fun recuperarEmailsDaObraId(obraId: String, responseBody: String?) {
        val emails = mutableListOf<String>()

        val obraRef = firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("emails")

        obraRef.get().addOnSuccessListener { emailsSnapshot ->
            for (doc in emailsSnapshot.documents) {
                val email = doc.getString("nome")
                if (email != null) {
                    emails.add(email)
                }
            }

            // Após recuperar os e-mails, chame a função para enviar o e-mail com o PDF
            enviarEmail(responseBody, emails)
        }.addOnFailureListener { exception ->
            Log.e(addocorrencia.TAG, "Erro ao buscar e-mails da obraId $obraId", exception)
            // Se ocorrer um erro, você pode lidar com ele aqui
        }
    }

    private fun enviarEmail(responseBody: String?, emails: List<String>) {
        // Verificar se o responseBody está disponível
        if (responseBody != null) {
            Thread {
                val props = Properties().apply {
                    // Configuração do servidor de e-mail (por exemplo, Gmail)
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                }

                // Criar uma sessão de e-mail com autenticação
                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        // Substitua com seu e-mail e senha
                        return PasswordAuthentication("fernandopkb@gmail.com", "gocd zvym fezr vbab")
                    }
                })

                try {
                    // Configurar o conteúdo do e-mail
                    val message = MimeMessage(session).apply {
                        setFrom(InternetAddress("fernandopkb@gmail.com"))
                        for (email in emails) {
                            addRecipient(Message.RecipientType.TO, InternetAddress(email))
                        }
                        setSubject("Assunto do E-mail")

                        // Criar o conteúdo do e-mail
                        val multipart = MimeMultipart()
                        val bodyPart = MimeBodyPart().apply {
                            setText("Conteúdo do E-mail")
                        }
                        val pdfAttachment = MimeBodyPart().apply {
                            dataHandler = DataHandler(javax.mail.util.ByteArrayDataSource(responseBody.toByteArray(), "application/pdf"))
                            fileName = "arquivo.pdf"
                        }
                        multipart.addBodyPart(bodyPart)
                        multipart.addBodyPart(pdfAttachment)
                        setContent(multipart)
                    }

                    // Enviar o e-mail
                    Transport.send(message)
                } catch (e: Exception) {
                    Log.e("AvaliacaoActivity", "Erro ao enviar o e-mail", e)
                }
            }.start()
        } else {
            Log.e("AvaliacaoActivity", "Conteúdo do PDF está nulo")
        }
    }



    private fun enviarPDFParaTelefones(pdfContent: String?, telefones: List<String>) {
        // Verifica se o WhatsApp está instalado no dispositivo
        if (isWhatsAppInstalled()) {
            // Cria uma Intent para enviar o PDF pelo WhatsApp
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/pdf"
            intent.setPackage("com.whatsapp")

            // Adiciona o PDF como um anexo
            val pdfUri = Uri.parse(pdfContent) // Supondo que o pdfContent seja o conteúdo do PDF
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri)

            // Adiciona os telefones como destinatários (opcional, dependendo da sua necessidade)
            intent.putExtra("jids", telefones.toTypedArray())

            // Inicia a Intent
            startActivity(intent)
        } else {
            // Se o WhatsApp não estiver instalado, mostra uma mensagem de erro
            Toast.makeText(this, "WhatsApp não está instalado no dispositivo", Toast.LENGTH_SHORT).show()
        }
    }


    private fun isWhatsAppInstalled(): Boolean {
        val packageManager = packageManager
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        val resolveInfoList = packageManager.queryIntentActivities(whatsappIntent, 0)
        for (resolveInfo in resolveInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            if (packageName != null && packageName.contains("com.whatsapp")) {
                return true
            }
        }
        return false
    }



    private fun enviarImagensParaCodigoPython(obraId: String) {
        val client = OkHttpClient()

        imagesList.forEachIndexed { index, uri ->
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            val requestBody = RequestBody.create(MediaType.parse("image/*"), bytes)

            val request = Request.Builder()
                .url("http://192.168.0.8:5000/upload_image/$obraId")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("AvaliacaoActivity:", "Erro ao enviar imagem para o código Python", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        Log.d("AvaliacaoActivity:", "Resposta do servidor Python: $responseBody")

                        // Aqui você pode fazer algo com a resposta do servidor Python, se necessário
                    } else {
                        Log.e("AvaliacaoActivity:", "Erro na resposta do servidor Python: ${response.code()}")
                    }
                }
            })
        }
    }
}