package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class AboutUs : AppCompatActivity() {

    lateinit var preferences: SharedPreferences
    lateinit var theme: String
    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        //user theme
        preferences = getSharedPreferences("myTheme", Context.MODE_PRIVATE)
        theme = preferences.getString("myTheme","").toString()
        println("Theme from SharedPreferences in About us: $theme")

        //user id
        preferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = preferences.getString("idUser","").toString()
        println("User ID from SharedPreferences in About us: $userId")

    }

    fun closeAboutUsPage(view: View) {
        finish()
    }
}