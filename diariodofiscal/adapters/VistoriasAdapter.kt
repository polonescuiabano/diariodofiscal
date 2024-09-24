package com.example.diariodofiscal.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R
import com.example.diariodofiscal.vistorias.VistoriaData
import android.util.Log

class VistoriasAdapter(
    private val context: Context,
    private val vistorias: MutableList<VistoriaData> = mutableListOf(),
    private val onItemClick: ((VistoriaData) -> Unit)? = null,
    private val onDeleteClick: (VistoriaData) -> Unit
) : RecyclerView.Adapter<VistoriasAdapter.VistoriaViewHolder>() {

    inner class VistoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDataVistoria: TextView = itemView.findViewById(R.id.dataVistoriaTextView)
        private val textViewDataProximaVistoria: TextView = itemView.findViewById(R.id.dataProximaVistoriaTextView)
        private val textViewNomeFiscal: TextView = itemView.findViewById(R.id.nomeFiscalTextView)
        private val textViewComentarioFiscal: TextView = itemView.findViewById(R.id.textViewComentarioFiscal)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)


        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(vistorias[position])
                }
            }



            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(vistorias[position])
                }
            }
        }

        fun bind(vistoria: VistoriaData) {
            textViewDataVistoria.text = "Data da vistoria: ${vistoria.dataVistoria}"
            textViewDataProximaVistoria.text = "Data da próxima vistoria: ${vistoria.dataProximaVistoria}"
            textViewNomeFiscal.text = "Nome do Fiscal: ${vistoria.nomeFiscal}"
            textViewComentarioFiscal.text = "Comentário Fiscal: ${vistoria.comentarioFiscal}"

            Log.d("VistoriaAdapter",    "File URL: ${vistoria.fileUris}")


            // Adiciona um clique para abrir o link do arquivo no navegador
            itemView.setOnClickListener {
                val uri = Uri.parse(vistoria.fileUris.toString()) // Convertendo fileUris para String explicitamente
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
            itemView.setOnClickListener {
                onItemClick?.invoke(vistoria)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VistoriaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_vistoria, parent, false)
        return VistoriaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VistoriaViewHolder, position: Int) {
        holder.bind(vistorias[position])
    }

    override fun getItemCount() = vistorias.size

    fun updateList(newVistorias: List<VistoriaData>) {
        vistorias.clear()
        vistorias.addAll(newVistorias)
        notifyDataSetChanged()
    }

    fun getVistoriasList(): List<VistoriaData> {
        return vistorias.toList()
    }

    fun removeVistoria(vistoria: VistoriaData) {
        val position = vistorias.indexOf(vistoria)
        if (position != -1) {
            vistorias.removeAt(position)
            notifyItemRemoved(position)
        }
    }


    fun sortVistoriasByDate() {
        vistorias.sortByDescending { it.dataVistoria }
    }
}
