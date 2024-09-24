package com.example.diariodofiscal.adapters

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.R

class ObrasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val seta: ImageButton = itemView.findViewById(R.id.seta)
    val etiquetaContainer: LinearLayout = itemView.findViewById(R.id.etiquetaContainer)
    private val textViewObra: TextView = itemView.findViewById(R.id.textViewObra)

    fun bind(obra: String) {
        textViewObra.text = obra

        val etiquetaContainer: LinearLayout = itemView.findViewById(R.id.etiquetaContainer)

    }
}
