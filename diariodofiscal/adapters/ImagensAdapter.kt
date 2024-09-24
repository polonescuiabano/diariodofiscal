package com.example.diariodofiscal.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.databinding.ItemImagemBinding

class ImagensAdapter(
    private val imageUris: List<String>,
    private val itemClickListener: (String) -> Unit
) : RecyclerView.Adapter<ImagensAdapter.ImagemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagemViewHolder {
        val binding = ItemImagemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImagemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImagemViewHolder, position: Int) {
        val uri = imageUris[position]
        holder.bind(uri)
    }

    override fun getItemCount(): Int = imageUris.size

    inner class ImagemViewHolder(private val binding: ItemImagemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: String) {
            // Exibir o link da imagem
            binding.textViewImageUrl.text = uri

            // Definir o evento de clique para abrir a imagem
            binding.root.setOnClickListener {
                itemClickListener(uri)
            }
        }
    }
}


