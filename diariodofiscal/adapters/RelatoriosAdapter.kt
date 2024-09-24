package com.example.diariodofiscal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.adm.Relatorio
import com.example.diariodofiscal.R

class RelatoriosAdapter(private var relatorios: List<Relatorio>) : RecyclerView.Adapter<RelatoriosAdapter.RelatorioViewHolder>() {

    inner class RelatorioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCondominio: TextView = itemView.findViewById(R.id.textViewCondominio)
        val textViewDataLocal: TextView = itemView.findViewById(R.id.textViewDataLocal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatorioViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_relatorio, parent, false)
        return RelatorioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RelatorioViewHolder, position: Int) {
        val relatorio = relatorios[position]
        holder.textViewCondominio.text = relatorio.condominio
        holder.textViewDataLocal.text = "Data: ${relatorio.data}, Local: ${relatorio.local}"
    }

    override fun getItemCount() = relatorios.size

    fun atualizarRelatorios(novosRelatorios: List<Relatorio>) {
        relatorios = novosRelatorios
        notifyDataSetChanged()
    }
}

