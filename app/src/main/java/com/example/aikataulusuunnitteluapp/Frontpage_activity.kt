package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.SharedPreferences

class Frontpage_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        var prefs: SharedPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        val idUser: String? = prefs.getString("idUser","");
        println("User ID from SharedPreferences in Frontpage: $idUser")


        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Frontpage"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}