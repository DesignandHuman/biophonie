package com.example.biophonie.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager.widget.ViewPager
import com.example.biophonie.R
import java.net.URI


class MainActivity : AppCompatActivity() {

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
    private val myPrefs = "Preferences"
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
        val sp = getSharedPreferences(myPrefs, Context.MODE_PRIVATE)
        if (!sp.getBoolean("first", false)) {
            val editor = sp.edit()
            editor.putBoolean("first", true)
            editor.apply()
            Toast.makeText(this, "First", Toast.LENGTH_SHORT).show()
            val intent =
                Intent(this, ViewPager::class.java) //call your ViewPager class
            startActivity(intent)
        }
    }
}