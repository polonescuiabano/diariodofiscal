package com.example.diariodofiscal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.notificacoes.Ocorrencia

class OcorrenciasAdapter(private val ocorrencias: List<Ocorrencia>, private val ocorrenciaClickListener: OcorrenciaClickListener) : RecyclerView.Adapter<OcorrenciasAdapter.OcorrenciaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OcorrenciaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_ocorrencia, parent, false)
        return OcorrenciaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OcorrenciaViewHolder, position: Int) {
        val ocorrencia = ocorrencias[position]
        holder.bind(ocorrencia)

        holder.itemView.setOnClickListener {
            ocorrenciaClickListener.onOcorrenciaClick(ocorrencia)
        }
    }

    override fun getItemCount(): Int {
        return ocorrencias.size
    }

    interface OcorrenciaClickListener {
        fun onOcorrenciaClick(ocorrencia: Ocorrencia)
    }

    inner class OcorrenciaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tituloTextView: TextView = itemView.findViewById(R.id.tituloTextView)
        private val dataTextView: TextView = itemView.findViewById(R.id.dataTextView)
        private val descricaoTextView: TextView = itemView.findViewById(R.id.descricaoTextView)

        fun bind(ocorrencia: Ocorrencia) {
            tituloTextView.text = "Ocorrência número ${ocorrencia.numeroOcorrencia}"
            dataTextView.text = ocorrencia.data // Supondo que a data seja um String formatado
            descricaoTextView.text = ocorrencia.descricao
        }
    }
}


