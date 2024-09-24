package com.example.diariodofiscal

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import java.io.ByteArrayInputStream
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart



class addocorrencia : AppCompatActivity() {

    private lateinit var condominio: String
    private lateinit var obraId: String
    private lateinit var editTextDataRetorno: EditText
    private val imageUris = mutableListOf<Uri>()
    private lateinit var spinnerArtigo: Spinner
    private lateinit var editTextDescricaoArtigo: EditText
    private lateinit var spinnerTiposMulta: Spinner
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                imageUris.add(selectedImageUri)
                // Atualizar a exibição das imagens anexadas, por exemplo, exibindo miniaturas ou nomes dos arquivos
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addocorrencia)

        // Inicialize o Firestore e o Firebase Storage
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inicializar os componentes
        initializeViews()

        // Configurar o clique do botão "Salvar Ocorrência"
        findViewById<Button>(R.id.buttonSalvarOcorrencia).setOnClickListener {
            salvarOcorrencia()
        }

        // Configurar o Spinner de Artigos
        configurarSpinnerArtigos()

        // Configurar cliques dos botões de data de retorno
        configurarCliquesDataRetorno()

        // Configurar o clique do botão de anexar imagens
        findViewById<Button>(R.id.buttonAnexarImagens).setOnClickListener { onButtonAttachImageClick(it) }
    }

    private fun onButtonAttachImageClick(view: View) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }


    private fun initializeViews() {
        // Obter dados da Intent
        condominio = intent.getStringExtra("condominio") ?: ""
        obraId = intent.getStringExtra("obraId") ?: ""

        // Inicializar os componentes de UI
        editTextDataRetorno = findViewById(R.id.textViewDataRetorno)
        spinnerArtigo = findViewById(R.id.spinnerArtigo)
        editTextDescricaoArtigo = findViewById(R.id.editTextDescricaoArtigo)
        spinnerTiposMulta = findViewById(R.id.spinnerTipoMulta)
    }

    private fun configurarSpinnerArtigos() {
        // Configurar o Spinner de Artigos
        val spinnerArtigos: Spinner = findViewById(R.id.spinnerArtigo)
        val adapterArtigos = ArrayAdapter.createFromResource(
            this,
            R.array.artigos_array,
            android.R.layout.simple_spinner_item
        )
        adapterArtigos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerArtigos.adapter = adapterArtigos

        spinnerArtigos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedArtigo = parent?.getItemAtPosition(position).toString()
                // Buscar a descrição e o tipo de multa no Firestore
                buscarDescricaoETipoMulta(selectedArtigo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implemente conforme necessário
            }
        }
    }

    private fun configurarCliquesDataRetorno() {
        // Configurar cliques dos botões de data de retorno
        findViewById<Button>(R.id.buttonDataRetorno7).setOnClickListener { onButtonAddDaysClick(it, 7) }
        findViewById<Button>(R.id.buttonDataRetorno10).setOnClickListener { onButtonAddDaysClick(it, 10) }
        findViewById<Button>(R.id.buttonDataRetorno15).setOnClickListener { onButtonAddDaysClick(it, 15) }
        findViewById<Button>(R.id.buttonDataRetorno21).setOnClickListener { onButtonAddDaysClick(it, 21) }
        findViewById<Button>(R.id.buttonDataRetorno30).setOnClickListener { onButtonAddDaysClick(it, 30) }
    }

    private fun salvarOcorrencia() {
        val selectedArtigo = spinnerArtigo.selectedItem.toString()
        val descricao = editTextDescricaoArtigo.text.toString()
        val tipoMulta = spinnerTiposMulta.selectedItem.toString()
        val dataRetorno = editTextDataRetorno.text.toString()

        val numeroOcorrenciaTask = obterProximoNumeroOcorrencia()
        numeroOcorrenciaTask.addOnSuccessListener { numeroOcorrencia ->
            gerarConteudoPDF(numeroOcorrencia, selectedArtigo, descricao, tipoMulta, dataRetorno) { conteudoPDF ->
                salvarOcorrenciaNoFirestore(numeroOcorrencia, selectedArtigo, descricao, tipoMulta, dataRetorno) // Salva a ocorrência no Firestore

                // Após salvar a ocorrência, envie as informações para o código Python gerar o PDF
                enviarInformacoesParaPython(numeroOcorrencia, selectedArtigo, descricao, tipoMulta, dataRetorno)
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Erro ao obter próximo número de ocorrência", e)
            Toast.makeText(this, "Falha ao obter próximo número de ocorrência", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarInformacoesParaPython(
        numeroOcorrencia: String,
        selectedArtigo: String,
        descricao: String,
        tipoMulta: String,
        dataRetorno: String
    ) {
        // Consultar os dados da obra para obter quadra, lote e nome do proprietário
        val obraRef = firestore.collection("Condominios").document(condominio).collection("Obras").document(obraId)
        obraRef.get().addOnSuccessListener { obraSnapshot ->
            if (obraSnapshot.exists()) {
                val quadra = obraSnapshot.getString("quadra") ?: ""
                val lote = obraSnapshot.getString("lote") ?: ""

                // Buscar o nome do proprietário no Firestore
                obraRef.collection("proprietario").get().addOnSuccessListener { proprietariosSnapshot ->
                    if (!proprietariosSnapshot.isEmpty) {
                        val primeiroProprietario = proprietariosSnapshot.documents.first()
                        val nomeProprietario = primeiroProprietario.getString("nome") ?: ""

                        // Buscar o número de telefone do proprietário no Firestore
                        val telefonesRef = obraRef.collection("telefones")
                        telefonesRef.get().addOnSuccessListener { telefonesSnapshot ->
                            val telefones = telefonesSnapshot.documents.mapNotNull { it.getString("nome") }

                            // Selecionar apenas o primeiro número de telefone encontrado
                            val numeroTelefone = telefones.firstOrNull() ?: ""

                            // Construir o JSON com os dados da ocorrência, nome do proprietário e número de telefone
                            val json = """
                        {
                            "numeroOcorrencia": "$numeroOcorrencia",
                            "selectedArtigo": "$selectedArtigo",
                            "descricao": "$descricao",
                            "tipoMulta": "$tipoMulta",
                            "dataRetorno": "$dataRetorno",
                            "quadra": "$quadra",
                            "lote": "$lote",
                            "nomeProprietario": "$nomeProprietario",
                            "numeroTelefone": "$numeroTelefone"
                        }
                        """.trimIndent()

                            // Enviar as informações para o código Python
                            enviarParaCodigoPython(json, obraId)
                        }
                    } else {
                        Log.e(TAG, "Proprietário não encontrado para esta obra")
                    }
                }
            } else {
                Log.e(TAG, "Obra não encontrada")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Erro ao obter dados da obra", e)
        }
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
            Log.e(TAG, "Erro ao buscar telefones da obraId $obraId", exception)
            // Se ocorrer um erro, você pode lidar com ele aqui
        }
    }

    fun enviarArquivoPDFParaTelefones(obraId: String) {
        // Enviar informações para o código Python para gerar o PDF
        enviarParaCodigoPython(jsonData, obraId) // Remova a lambda aqui
    }

    fun criarJsonData(): String {
        // Lógica para criar e retornar os dados em formato JSON
        return "{\"key\": \"value\"}"
    }

    // Em algum lugar do seu código onde você precisa usar jsonData:
    val jsonData = criarJsonData()

    private fun enviarParaCodigoPython(json: String, obraId: String) {
        val client = OkHttpClient()
        val body = RequestBody.create(MediaType.parse("application/json"), json)
        val request = Request.Builder()
            .url("http://192.168.0.8:5000/gerar_pdf")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Erro ao enviar informações para o código Python", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Log.d(TAG, "Resposta do servidor Python: $responseBody")

                    // Chamar a função para recuperar e enviar e-mails
                    recuperarEmailsDaObraId(obraId, responseBody)
                    buscarTelefonesDaObraId(obraId) { telefones ->
                        enviarPDFParaTelefones(responseBody, telefones)
                    }
                } else {
                    Log.e(TAG, "Erro na resposta do servidor Python: ${response.code()}")
                }
            }
        })
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
            Log.e(TAG, "Erro ao buscar e-mails da obraId $obraId", exception)
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
                            dataHandler = DataHandler(ByteArrayDataSource(responseBody.toByteArray(), "application/pdf"))
                            fileName = "arquivo.pdf"
                        }
                        multipart.addBodyPart(bodyPart)
                        multipart.addBodyPart(pdfAttachment)
                        setContent(multipart)
                    }

                    // Enviar o e-mail
                    Transport.send(message)
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao enviar o e-mail", e)
                }
            }.start()
        } else {
            Log.e(TAG, "Conteúdo do PDF está nulo")
        }
    }



    private fun enviarPDFParaTelefones(pdfContent: String?, telefones: List<String>) {
        // Verifica se o WhatsApp está instalado no dispositivo
        if (isAppInstalled("com.whatsapp")) {
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

    private fun isAppInstalled(packageName: String): Boolean {
        val packageManager = packageManager
        try {
            // Verifica se o pacote está instalado no dispositivo
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            // Se o pacote não for encontrado, retorna false
            return false
        }
    }



    private fun buscarDescricaoETipoMulta(artigo: String) {
        // Referência ao documento do condomínio no Firestore
        val docRef = firestore.collection("Condominios")
            .document(condominio)
            .collection("Artigos")
            .whereEqualTo("nome", artigo)

        docRef.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val descricao = document.getString("descricao")
                        val tipoMulta = document.getString("tipomulta")

                        if (descricao != null && tipoMulta != null) {
                            Log.d(TAG, "Descrição do artigo $artigo: $descricao")
                            Log.d(TAG, "Tipo de multa do artigo $artigo: $tipoMulta")

                            // Atualizar os campos na UI com os valores recuperados
                            editTextDescricaoArtigo.setText(descricao)
                            val position = (spinnerTiposMulta.adapter as ArrayAdapter<String>).getPosition(tipoMulta)
                            spinnerTiposMulta.setSelection(position)
                        } else {
                            Log.e(TAG, "Dados inválidos para o artigo $artigo")
                        }
                    }
                } else {
                    Log.d(TAG, "Documento não encontrado para o artigo: $artigo")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao buscar o artigo $artigo", exception)
            }
    }

    private fun onButtonAddDaysClick(view: View, daysToAdd: Int) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        val newDate = sdf.format(calendar.time)
        editTextDataRetorno.setText(newDate)
    }

    private fun obterProximoNumeroOcorrencia(): Task<String> {
        // Referência à coleção de ocorrências
        val ocorrenciasRef = firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("Ocorrencias")

        // Consultar todas as ocorrências e ordenar por número
        return ocorrenciasRef
            .orderBy("numeroOcorrencia", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .continueWith { task ->
                if (task.isSuccessful) {
                    val lastDocument = task.result?.documents?.firstOrNull()
                    if (lastDocument != null) {
                        val lastOcorrenciaId = lastDocument.getString("numeroOcorrencia")
                        if (lastOcorrenciaId != null) {
                            val ultimoNumero = lastOcorrenciaId.split("-").lastOrNull()?.toIntOrNull() ?: 0
                            val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
                            val proximoNumero = ultimoNumero + 1
                            "${anoAtual}-${String.format("%03d", proximoNumero)}"
                        } else {
                            // Se não houver ocorrências, começar com o número 1 no ano atual
                            val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
                            "${anoAtual}-001"
                        }
                    } else {
                        // Se não houver ocorrências, começar com o número 1 no ano atual
                        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
                        "${anoAtual}-001"
                    }
                } else {
                    // Em caso de falha, retornar um valor padrão
                    Log.e(TAG, "Erro ao obter ocorrências", task.exception)
                    "Erro ao obter número de ocorrência"
                }
            }
    }




    private fun gerarConteudoPDF(
        numeroOcorrencia: String,
        selectedArtigo: String,
        descricao: String,
        tipoMulta: String,
        dataRetorno: String,
        onComplete: (String) -> Unit
    ) {
        var quadra = ""
        var lote = ""
        var nomeProprietario = ""
        val obraRef = firestore.collection("Condominios").document(condominio).collection("Obras").document(obraId)
        obraRef.get().addOnSuccessListener { obraSnapshot ->
            if (obraSnapshot.exists()) {
                quadra = obraSnapshot.getString("quadra") ?: ""
                lote = obraSnapshot.getString("lote") ?: ""

                // Buscar o nome do proprietário no Firestore
                obraRef.collection("proprietario").get().addOnSuccessListener { proprietariosSnapshot ->
                    if (!proprietariosSnapshot.isEmpty) {
                        val primeiroProprietario = proprietariosSnapshot.documents.first()
                        nomeProprietario = primeiroProprietario.getString("nome") ?: ""
                        // Montar o conteúdo do PDF com as informações recuperadas
                        val conteudo = buildString {
                            append("Número da Ocorrência: $numeroOcorrencia\n")
                            append("Cuiabá, ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}\n")
                            append("Ao senhor, $nomeProprietario\n")
                            append("Quadra/Lote: $quadra/$lote\n")
                            append("Ref.: Abertura de ocorrência de obra por descumprimento de normas construtivas.\n")
                            append("de fiscalização de obras, durante a vistoria realizada no dia $dataRetorno\n")
                            append("das normas construtivas e urbanísticas do condomínio, referente ao seguinte dispositivo:\n")
                            append("Dispositivo: $selectedArtigo\n")
                            append("Descrição da impropriedade: $descricao\n")
                            append("Tipo de Multa: $tipoMulta\n")
                            append("NOTIFICO-LHE para sanar a irregularidade apontada, até 5 dias úteis a contar a partir do\n")
                            append("recebimento desta notificação.\n")
                            append("Dúvidas entre em contato no whatsapp da engenharia do condomínio: +55 65 9663-6389\n")
                            append("Providenciar a placa de obra.\n")
                            append("Apraz-nos cumprimentá-lo cordialmente, sirvo-me do presente para informar que a equipe\n")
                            append(", constatou o descumprimento")
                        }

                        onComplete(conteudo)
                    } else {
                        Log.e(TAG, "Proprietário não encontrado para esta obra")
                    }
                }
            } else {
                Log.e(TAG, "Obra não encontrada")
            }
        }
    }

    private fun salvarOcorrenciaNoFirestore(
        numeroOcorrencia: String,
        selectedArtigo: String,
        descricao: String,
        tipoMulta: String,
        dataRetorno: String
    ) {
        // Aqui você pode implementar a lógica para salvar os dados da ocorrência no Firestore
        // Por exemplo:
        val obraRef = firestore.collection("Condominios").document(condominio)
            .collection("Obras").document(obraId)
            .collection("Ocorrencias").document(numeroOcorrencia)

        val ocorrenciaData = hashMapOf(
            "numeroOcorrencia" to numeroOcorrencia,
            "artigo" to selectedArtigo,
            "descricao" to descricao,
            "tipoMulta" to tipoMulta,
            "dataRetorno" to dataRetorno,
            "status" to "Em Aberto" // Definir o status como "Em Aberto"
            // Adicione mais campos conforme necessário
        )

        obraRef.set(ocorrenciaData)
            .addOnSuccessListener {
                Log.d(TAG, "Ocorrência salva com sucesso no Firestore")
                Toast.makeText(this, "Ocorrência salva com sucesso", Toast.LENGTH_SHORT).show()

                // Após salvar a ocorrência, enviar as imagens para o armazenamento
                enviarImagensParaStorage(numeroOcorrencia)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erro ao salvar ocorrência no Firestore", e)
                Toast.makeText(this, "Erro ao salvar ocorrência", Toast.LENGTH_SHORT).show()
            }
    }


    private fun enviarImagensParaStorage(ocorrenciaId: String) {
        for ((index, imageUri) in imageUris.withIndex()) {
            val storageRef = storage.reference.child("imagens/$ocorrenciaId/image$index.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    Log.d(TAG, "Imagem $index enviada com sucesso.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao enviar imagem $index", e)
                }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                EMAIL_REQUEST_CODE -> {
                    // Exibir mensagem de sucesso para o envio por e-mail
                    Toast.makeText(this, "E-mail enviado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val TAG = "AddOcorrenciaActivity"
        const val EMAIL_REQUEST_CODE = 101 // Código de solicitação para envio por e-mail
        const val EDITAR_MENSAGEM_REQUEST_CODE = 102 // Código de solicitação para editar a mensagem
    }
}

private class ByteArrayDataSource(private val data: ByteArray, private val type: String) : DataSource {
    override fun getInputStream() = ByteArrayInputStream(data)
    override fun getOutputStream() = throw IOException("Not Supported")
    override fun getContentType() = type
    override fun getName() = "ByteArrayDataSource"
}
