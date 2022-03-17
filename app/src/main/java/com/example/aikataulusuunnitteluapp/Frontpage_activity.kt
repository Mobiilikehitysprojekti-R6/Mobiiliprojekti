package com.example.aikataulusuunnitteluapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Frontpage_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frontpage)

        var btn_showCalendar = findViewById<Button>(R.id.btn_toCalendar)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Frontpage"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)


        btn_showCalendar.setOnClickListener {
            startActivity(Intent(this@Frontpage_activity, Calendar_activity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }



}