package com.example.grnz.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grnz.R
import com.example.grnz.data.network.ApiClient
import com.example.grnz.data.network.model.Photo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfilePhotosAdapter(
    private var photos: List<Photo>,
    private val onDeleteClick: (photoId: String) -> Unit
) : RecyclerView.Adapter<ProfilePhotosAdapter.PhotoViewHolder>() {

    private val timestampFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view_my_photo)
        val timestampTextView: TextView = view.findViewById(R.id.text_view_my_photo_timestamp)
        val deleteButton: ImageButton = view.findViewById(R.id.button_delete_photo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_my_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = photos[position]
        val baseUrl = ApiClient.BASE_URL.removeSuffix("/")
        val imageUrlPath = item.imageUrl
        if (imageUrlPath != null && imageUrlPath.startsWith("/")) {
            val fullImageUrl = baseUrl + imageUrlPath
            Glide.with(holder.itemView.context)
                .load(fullImageUrl)
                .placeholder(R.color.white)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imageView)
        } else {
            Glide.with(holder.itemView.context)
                .load(android.R.drawable.ic_menu_report_image)
                .into(holder.imageView)
        }

        try {
            val date = Date(item.timestamp)
            holder.timestampTextView.text = timestampFormatter.format(date)
        } catch (e: Exception) {
            holder.timestampTextView.text = "Timestamp: ${item.timestamp}"
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(item.id)
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    fun updateData(newPhotos: List<Photo>) {
        photos = newPhotos
        notifyDataSetChanged()
    }
}
