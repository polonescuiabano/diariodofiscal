package com.example.diariodofiscal.telaprincipal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.diariodofiscal.MainActivity
import com.example.diariodofiscal.agenda.AgendaGeral
import com.example.diariodofiscal.listacondominios.condominios
import com.example.diariodofiscal.databinding.ActivityTelaprincipalBinding
import com.google.firebase.auth.FirebaseAuth

class telaprincipal : AppCompatActivity() {
    private lateinit var binding: ActivityTelaprincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaprincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btDeslogar.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val voltarTelaLogin = Intent(this, MainActivity::class.java)
            startActivity(voltarTelaLogin)
            finish()
        }
        binding.btcond.setOnClickListener {
            val condominios = Intent(this, condominios::class.java)
            startActivity(condominios)
        }
    }
}