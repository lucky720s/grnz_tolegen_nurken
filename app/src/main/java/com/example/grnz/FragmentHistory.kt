package com.example.grnz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class FragmentHistory : Fragment() {
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        historyRecyclerView = view.findViewById(R.id.recycler_view_history)
        historyAdapter = HistoryAdapter(emptyList())
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
        return view
    }
    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.let {
            historyAdapter.updateData(it.getHistory())
        }
    }
}
