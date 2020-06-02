package com.example.biophonie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonToMap: Button = findViewById(R.id.toMap)
        buttonToMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }
    }
}