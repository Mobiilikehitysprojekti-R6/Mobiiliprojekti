package com.example.aikataulusuunnitteluapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class AboutUs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
    }

    fun closeAboutUsPage(view: View) {
        finish()
    }
}