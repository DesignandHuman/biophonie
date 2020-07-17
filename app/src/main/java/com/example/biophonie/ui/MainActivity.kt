package com.example.biophonie.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.biophonie.R


class MainActivity : AppCompatActivity() {
    private val myPrefs = "Preferences"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonToMap: Button = findViewById(R.id.toMap)
        buttonToMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
        checkTutorial()
    }

    private fun checkTutorial() {
        val sp = getSharedPreferences(myPrefs, Context.MODE_PRIVATE)
        //if (!sp.getBoolean("first", false)) {
            val editor = sp.edit()
            editor.putBoolean("first", true)
            editor.apply()
            Toast.makeText(this, "First", Toast.LENGTH_SHORT).show()
            /*val intent =
                Intent(this, SampleCirclesDefault::class.java) //call your ViewPager class*/
            startActivity(intent)
        //}
    }
}