package com.example.biophonie.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.biophonie.R
import com.example.biophonie.ENCRYPTED_PREFS_NAME
import com.example.biophonie.PREFS_NAME

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
        checkTutorial()
    }

    private fun checkTutorial() {
        with (getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)) {
            val name = getString("username","")
            if (name.isNullOrBlank()) {
                Toast.makeText(this@MainActivity, "name: $name", Toast.LENGTH_SHORT).show()
                val intent =
                    Intent(this@MainActivity, TutorialActivity::class.java) //call your ViewPager class
                startActivity(intent)
            }
        }
    }
}