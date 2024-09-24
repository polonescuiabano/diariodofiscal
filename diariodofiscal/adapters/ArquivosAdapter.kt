package com.example.diariodofiscal.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.diariodofiscal.databinding.ItemArquivoBinding
import com.example.diariodofiscal.model.Arquivo

class ArquivosAdapter(
    private val itemClickListener: (Arquivo) -> Unit,
    private val deleteClickListener: (Arquivo) -> Unit,
    private val renameClickListener: (Arquivo) -> Unit,

) :
    ListAdapter<Arquivo, ArquivosAdapter.ArquivoViewHolder>(DiffCallback()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArquivoViewHolder {
        val binding = ItemArquivoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArquivoViewHolder(binding, itemClickListener, deleteClickListener, renameClickListener)
    }

    override fun onBindViewHolder(holder: ArquivoViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class ArquivoViewHolder(
        private val binding: ItemArquivoBinding,
        private val itemClickListener: (Arquivo) -> Unit,
        private val deleteClickListener: (Arquivo) -> Unit,
        private val renameClickListener: (Arquivo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(arquivo: Arquivo) {
            binding.apply {
                val nomeArquivo = arquivo.fileName.substringAfterLast('/')
                nomeArquivoTextView.text = nomeArquivo

                // Definindo o evento de clique no arquivo
                root.setOnClickListener {
                    itemClickListener(arquivo)
                }

                // Definindo o evento de clique no botão de exclusão
                btnDelete.setOnClickListener {
                    deleteClickListener(arquivo)
                }

                // Definindo o evento de clique no botão de renomear
                btnRename.setOnClickListener {
                    renameClickListener(arquivo)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Arquivo>() {
        override fun areItemsTheSame(oldItem: Arquivo, newItem: Arquivo) =
            oldItem.fileName == newItem.fileName

        override fun areContentsTheSame(oldItem: Arquivo, newItem: Arquivo) =
            oldItem == newItem
    }
}
