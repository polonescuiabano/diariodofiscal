package com.example.diariodofiscal

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputBinding
import com.example.diariodofiscal.databinding.ActivityMainBinding
import com.example.diariodofiscal.telaprincipal.adm
import com.example.diariodofiscal.telaprincipal.telaprincipal
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // Use binding.root para definir o layout

        binding.BtEntrar.setOnClickListener { view ->
            val email = binding.textInputEditText.text.toString()
            val senha = binding.txtInputLayoutSenha.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                val snackbar = Snackbar.make(view, "Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.show()
            } else {
                auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener { autenticacao ->
                    if (autenticacao.isSuccessful) {
                        onStart()
                    } else {
                        val snackbar = Snackbar.make(view, "Credenciais inválidas!", Snackbar.LENGTH_SHORT)
                        snackbar.setBackgroundTint(Color.RED)
                        snackbar.show()
                    }
                }
            }
        }
    }

    private fun navegarTelaPrincipal() {
        val intent = Intent(this, telaprincipal::class.java)
        startActivity(intent)
        finish()
    }

    private fun navegarTelaADM() {
        val intent = Intent(this, adm::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()

        val usuarioAtual = FirebaseAuth.getInstance().currentUser

        if (usuarioAtual != null) {
            if (usuarioAtual.email == "fernandopkb@gmail.com" || usuarioAtual.email == "gessica.micaeli17@gmail.com" || usuarioAtual.email == "carlosmmiura@gmail.com" ) {
                navegarTelaADM()
            } else {
                navegarTelaPrincipal()
            }
        }
    }
}