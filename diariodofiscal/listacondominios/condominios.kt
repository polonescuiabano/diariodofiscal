package com.example.diariodofiscal.listacondominios

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.diariodofiscal.condominios.Alphaville2
import com.example.diariodofiscal.condominios.Belvedere
import com.example.diariodofiscal.condominios.Belvedere2
import com.example.diariodofiscal.condominios.FloraisCuiaba
import com.example.diariodofiscal.databinding.ActivityCondominiosBinding
import com.example.diariodofiscal.condominios.FloraisItaliaActivity
import com.example.diariodofiscal.condominios.FloraisParque
import com.example.diariodofiscal.condominios.PrimorDasTorres
import com.example.diariodofiscal.condominios.Villajardim


class condominios : AppCompatActivity() {
    private lateinit var binding: ActivityCondominiosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCondominiosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floraisitbttn.setOnClickListener {
            val floraisit = Intent(this, FloraisItaliaActivity::class.java)
            startActivity(floraisit)
        }
        binding.vj.setOnClickListener {
            val vj = Intent(this, Villajardim::class.java)
            startActivity(vj)
        }
        binding.fcui.setOnClickListener {
            val fcui = Intent(this, FloraisCuiaba::class.java)
            startActivity(fcui)
        }
        binding.belvedere.setOnClickListener {
            val belvedere = Intent(this, Belvedere::class.java)
            startActivity(belvedere)
        }
        binding.belvedere2.setOnClickListener {
            val belvedere2 = Intent(this, Belvedere2::class.java)
            startActivity(belvedere2)
        }
        binding.alpha2.setOnClickListener {
            val alpha2 = Intent(this, Alphaville2::class.java)
            startActivity(alpha2)
        }
        binding.ptorres.setOnClickListener {
            val ptorres = Intent(this, PrimorDasTorres::class.java)
            startActivity(ptorres)
        }
    }
}