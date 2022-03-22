package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.content.SharedPreferences
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView

class Frontpage_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        var prefs: SharedPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        val idUser: String? = prefs.getString("idUser","");
        println("User ID from SharedPreferences in Frontpage: $idUser")

        var monthYearText: TextView;
        var dayOfTheWeek: TextView;
        var hourListView: ListView;

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Frontpage"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        initWidgets();

    }
    fun showPopup(view: View?) {
        val popup = PopupMenu(this, view)
        //popup.setOnMenuItemClickListener(this)
        popup.inflate(R.menu.menu)
        popup.show()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //adds items to the action bar
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    private fun initWidgets() {
        var monthYearText = findViewById<TextView>(R.id.monthYear_tv)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun actionPreviousDay(view: View) {}
    fun actionNextDay(view: View) {}
}