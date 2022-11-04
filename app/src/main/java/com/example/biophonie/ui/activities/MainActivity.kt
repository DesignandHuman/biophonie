package com.example.biophonie.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.biophonie.R
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.util.AppPrefs
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.toMap).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
        findViewById<Button>(R.id.toTutorial).setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }
        initPrefs()
        checkTutorial()
    }

    private fun initPrefs() {
        AppPrefs.setup(application)
    }

    private fun checkTutorial() {
        if (AppPrefs.userId == null) {
            val intent =
                Intent(this@MainActivity, TutorialActivity::class.java)
            startActivity(intent)
        }
    }
}