package com.example.diariodofiscal.telaprincipal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.diariodofiscal.MainActivity
import com.example.diariodofiscal.R
import com.example.diariodofiscal.adm.RelatoriosActivity
import com.example.diariodofiscal.adm.logs
import com.example.diariodofiscal.databinding.ActivityAdmBinding
import com.example.diariodofiscal.listacondominios.condominios
import com.google.firebase.auth.FirebaseAuth

class adm : AppCompatActivity() {
    private lateinit var binding: ActivityAdmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdmBinding.inflate(layoutInflater)
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
        binding.logs.setOnClickListener {
            val log = Intent(this, logs::class.java )
            startActivity(log)
            }
        binding.btnRelatorios.setOnClickListener {
            val relatorio = Intent(this, RelatoriosActivity::class.java)
            startActivity(relatorio)
        }
        }
    }