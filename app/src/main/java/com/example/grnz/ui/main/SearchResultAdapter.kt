package com.example.grnz.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grnz.R
import com.example.grnz.data.network.ApiClient
import com.example.grnz.data.network.model.Photo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchResultAdapter(
    private var results: List<Photo>,
    private val onUserClick: (userId: String) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder>() {

    private val timestampFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    inner class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view_result)
        val uploaderInfoTextView: TextView = view.findViewById(R.id.text_view_uploader_info)
        val timestampTextView: TextView = view.findViewById(R.id.text_view_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_search_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = results[position]
        val baseUrl = ApiClient.BASE_URL.removeSuffix("/")
        val imageUrlPath = item.imageUrl
        if (imageUrlPath != null && imageUrlPath.startsWith("/")) {
            val fullImageUrl = baseUrl + imageUrlPath
            Log.d("SearchResultAdapter", "Loading image from URL: $fullImageUrl")
            Glide.with(holder.itemView.context)
                .load(fullImageUrl)
                .placeholder(R.color.white)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imageView)
        } else {
            Log.e("SearchResultAdapter", "Invalid or null imageUrl format: $imageUrlPath")
            Glide.with(holder.itemView.context)
                .load(android.R.drawable.ic_menu_report_image)
                .into(holder.imageView)
        }

        val uploaderText = item.uploaderEmail ?: "ID: ${item.userId}"
        holder.uploaderInfoTextView.text = "Added by: $uploaderText"
        holder.uploaderInfoTextView.setOnClickListener { view ->
            Log.d("AdapterClick", "Clicked on uploader: ${item.userId}")
            onUserClick(item.userId)

        }
        try {
            val date = Date(item.timestamp)
            holder.timestampTextView.text = timestampFormatter.format(date)
        } catch (e: Exception) {
            Log.e("SearchResultAdapter", "Error formatting timestamp: ${item.timestamp}", e)
            holder.timestampTextView.text = "Timestamp: ${item.timestamp}"
        }
    }

    override fun getItemCount(): Int {
        return results.size
    }

    fun updateData(newResults: List<Photo>) {
        results = newResults
        notifyDataSetChanged()
    }
}
