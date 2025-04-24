package com.example.grnz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.grnz.R // Убедись, что R импортирован
import com.example.grnz.data.NumberResult

class HistoryAdapter(private var historyList: List<NumberResult>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.text_view_history_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.textView.text = "${item.number} - ${item.result}"
    }

    override fun getItemCount(): Int = historyList.size
    fun updateData(newData: List<NumberResult>) {
        historyList = newData
        notifyDataSetChanged()
    }
}
