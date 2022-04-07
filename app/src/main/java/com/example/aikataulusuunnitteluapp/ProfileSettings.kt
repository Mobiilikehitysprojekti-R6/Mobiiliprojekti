package com.example.aikataulusuunnitteluapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class ProfileSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)
    }

    fun closeSettings(view: View) {
        finish()
    }
}