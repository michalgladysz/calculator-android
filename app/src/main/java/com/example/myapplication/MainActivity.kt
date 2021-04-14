package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val simpleButton = findViewById<Button>(R.id.simpleButton)
        val advancedButton = findViewById<Button>(R.id.advancedButton)

        simpleButton.setOnClickListener {
            val intent = Intent(this, SimpleActivity::class.java)
            startActivity(intent)
        }

        advancedButton.setOnClickListener {
            val intent = Intent(this, AdvancedActivity::class.java)
            startActivity(intent)
        }
    }

}