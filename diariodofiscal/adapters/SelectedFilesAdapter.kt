package com.example.diariodofiscal.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diariodofiscal.R
import android.util.Log



class SelectedFilesAdapter(private val fileList: List<Uri>) :
    RecyclerView.Adapter<SelectedFilesAdapter.FileViewHolder>() {

    private var itemClickListener: ((Uri) -> Unit)? = null


    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileImageView: ImageView = itemView.findViewById(R.id.fileImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_selected_file, parent, false)
        Log.d("SelectedFilesAdapter", "onCreateViewHolder: fileList size - ${fileList.size}")
        return FileViewHolder(view)
    }

    fun setOnItemClickListener(listener: (Uri) -> Unit) {
        itemClickListener = listener
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileUri = fileList[position]
        Log.d("SelectedFilesAdapter", "onBindViewHolder: fileUri - $fileUri")
        Glide.with(holder.itemView.context)
            .load(fileUri)
            .placeholder(R.drawable.placeholder_file)
            .error(R.drawable.error_file)
            .into(holder.fileImageView)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }
}
