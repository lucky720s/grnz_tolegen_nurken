package com.example.grnz
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.grnz.data.NumberResult
import com.google.android.material.bottomnavigation.BottomNavigationView
class MainActivity : AppCompatActivity() {
    private val historyList = mutableListOf<NumberResult>()
    private lateinit var numberResultReceiver: BroadcastReceiver
    private val ACTION_NUMBER_CHECKED = "com.example.grnz.ACTION_NUMBER_CHECKED"
    private val EXTRA_NUMBER = "com.example.grnz.EXTRA_NUMBER"
    private val EXTRA_RESULT = "com.example.grnz.EXTRA_RESULT"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navHostFragment.navController)
        numberResultReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_NUMBER_CHECKED) {
                    val number = intent.getStringExtra(EXTRA_NUMBER)
                    val result = intent.getStringExtra(EXTRA_RESULT)
                    if (number != null && result != null)
                        historyList.add(0, NumberResult(number, result))
                }
            }
        }
        ContextCompat.registerReceiver(
            this,
            numberResultReceiver,
            IntentFilter(ACTION_NUMBER_CHECKED),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    fun getHistory(): List<NumberResult>{
        return historyList.toList()
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(numberResultReceiver)
    }
}
