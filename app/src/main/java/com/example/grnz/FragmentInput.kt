package com.example.grnz
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
class FragmentInput : Fragment() {
    private lateinit var numberEditText: EditText
    private lateinit var checkButton: Button
    private lateinit var resultTextView: TextView
    private val ACTION_NUMBER_CHECKED = "com.example.grnz.ACTION_NUMBER_CHECKED"
    private val EXTRA_NUMBER = "com.example.grnz.EXTRA_NUMBER"
    private val EXTRA_RESULT = "com.example.grnz.EXTRA_RESULT"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_input, container, false)
        numberEditText = view.findViewById(R.id.edit_text_number)
        checkButton = view.findViewById(R.id.button_check)
        resultTextView = view.findViewById(R.id.text_view_result)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkButton.setOnClickListener {
            val number = numberEditText.text.toString().trim()
            if (number.isNotEmpty()) {
                val result = simulateCheck(number)
                resultTextView.text = "Результат: $result"
                val intent = Intent(ACTION_NUMBER_CHECKED).apply {
                    putExtra(EXTRA_NUMBER, number)
                    putExtra(EXTRA_RESULT, result)
                }
                requireActivity().sendBroadcast(intent)
            } else Toast.makeText(context, "Введите номер", Toast.LENGTH_SHORT).show()
        }
    }
    private fun simulateCheck(number: String): String {
        return if (number.contains("111")) "Найдено: Авто А" else "Не найдено"
    }
}
