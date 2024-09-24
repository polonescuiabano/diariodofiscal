package com.example.diariodofiscal.notificacoes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.diariodofiscal.R

class editormensagem : AppCompatActivity() {

    private lateinit var editTextMensagem: EditText
    private lateinit var telefones: List<String>
    private lateinit var emails: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor_mensagem)

        editTextMensagem = findViewById(R.id.editTextMensagem)

        val conteudoPDF = intent.getStringExtra("conteudoPDF")
        editTextMensagem.setText(conteudoPDF)

        telefones = intent.getStringArrayListExtra("telefones") ?: emptyList()
        emails = intent.getStringArrayListExtra("emails") ?: emptyList()

        findViewById<Button>(R.id.buttonEnviar).setOnClickListener {
            enviarMensagem()
        }
    }

    private fun enviarMensagem() {
        val mensagemEditada = editTextMensagem.text.toString()

        // Enviar email
        enviarEmail(emails, mensagemEditada)

        // Enviar mensagem via WhatsApp
        enviarMensagemWhatsApp(telefones, mensagemEditada)

        // Fechar a activity e retornar para a tela anterior
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun enviarEmail(emails: List<String>, mensagem: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emails.toTypedArray())
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Assunto do Email")
        emailIntent.putExtra(Intent.EXTRA_TEXT, mensagem)
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(this, "Nenhum aplicativo de e-mail encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarMensagemWhatsApp(telefones: List<String>, mensagem: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, mensagem)
        whatsappIntent.setPackage("com.whatsapp")

        for (telefone in telefones) {
            val phoneUri = Uri.parse("smsto:$telefone")
            whatsappIntent.putExtra("jid", phoneUri)
            if (whatsappIntent.resolveActivity(packageManager) != null) {
                startActivity(whatsappIntent)
            } else {
                Toast.makeText(this, "Nenhum aplicativo WhatsApp encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
