package com.example.diariodofiscal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R


class AgendaAdapter(private val eventos: MutableList<String>) : RecyclerView.Adapter<AgendaAdapter.EventoViewHolder>() {

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventoTextView: TextView = itemView.findViewById(R.id.text_view_eventos_do_dia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agenda_event, parent, false)
        return EventoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val currentItem = eventos[position]
        holder.eventoTextView.text = currentItem
    }

    override fun getItemCount() = eventos.size

    fun adicionarEvento(evento: String) {
        eventos.add(evento)
        notifyItemInserted(eventos.size - 1)
    }
}
