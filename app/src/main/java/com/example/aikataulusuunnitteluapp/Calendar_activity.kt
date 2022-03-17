package com.example.aikataulusuunnitteluapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class Calendar_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)


        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Calendar"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}