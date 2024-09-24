package com.example.diariodofiscal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.model.Evento

class EventosAdapter : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    private var eventosList = listOf<Evento>()

    inner class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tituloTextView: TextView = itemView.findViewById(R.id.tituloTextView)
        val descricaoTextView: TextView = itemView.findViewById(R.id.descricaoTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventosList[position]
        holder.tituloTextView.text = evento.titulo
        holder.descricaoTextView.text = evento.descricao
    }

    override fun getItemCount(): Int {
        return eventosList.size
    }

    fun atualizarLista(novaLista: List<Evento>) {
        eventosList = novaLista
        notifyDataSetChanged()
    }
}
