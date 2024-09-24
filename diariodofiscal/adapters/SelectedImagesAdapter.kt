package com.example.diariodofiscal.adapters

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diariodofiscal.R

class SelectedImagesAdapter(
    private val imageUriList: MutableList<Uri>,
    private var itemClickListener: ((Uri) -> Unit)? = null
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    fun addUri(uri: Uri) {
        imageUriList.add(uri)
        notifyItemInserted(imageUriList.size - 1)
    }

    fun setOnItemClickListener(listener: (Uri) -> Unit) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUriList[position]
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            itemClickListener?.invoke(imageUrl)
        }
    }

    override fun getItemCount(): Int {
        return imageUriList.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
