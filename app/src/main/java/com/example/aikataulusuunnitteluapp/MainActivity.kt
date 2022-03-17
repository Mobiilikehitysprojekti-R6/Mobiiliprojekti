package com.example.aikataulusuunnitteluapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var btn_login = findViewById<Button>(R.id.btn_login)

        btn_login.setOnClickListener{
            startActivity(Intent(this@MainActivity, Frontpage_activity::class.java))
        }



    }
}