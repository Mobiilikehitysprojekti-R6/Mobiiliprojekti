package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.aikataulusuunnitteluapp.Data.HoursDatasource
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class Frontpage_activity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener  {

    private lateinit var monthYearText: TextView
    private lateinit var datetext: TextView
    private lateinit var weekdaytext: TextView
    private lateinit var selectedDate: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        //user authentication
        var prefs: SharedPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        val idUser: String? = prefs.getString("idUser","")
        println("User ID from SharedPreferences in Frontpage: $idUser")

        //actionbar+back-button
        val actionbar = supportActionBar
        actionbar!!.title = "Frontpage"
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)

        // Initialize calendar hour data.
        val myDataset = HoursDatasource().loadHourModel()
        val recyclerView = findViewById<RecyclerView>(R.id.RecyclerView)
        recyclerView.adapter = HourAdapter(this, myDataset)
        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        initWidgets()
        selectedDate = LocalDate.now()
        setDates()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout_item -> {
                Toast.makeText(baseContext, "Log out", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    private fun initWidgets() {
        monthYearText = findViewById(R.id.monthYear_tv)
        datetext = findViewById(R.id.date_tv)
        weekdaytext = findViewById(R.id.weekday_tv)
    }

    private fun setDates() {
        monthYearText.text = monthYearFromDate(selectedDate)
        datetext.text = fullDate(selectedDate)
       weekdaytext.text = dayDate(selectedDate)
    }

    private fun monthYearFromDate(date: LocalDate): String? {
        var formatter : DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter)
    }

    private fun fullDate(date: LocalDate): String? {
        var formatter : DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd yyyy")
        return date.format(formatter)
    }

    private fun dayDate(date: LocalDate): String? {
        var formatter : DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE")
        return date.format(formatter)
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun actionPreviousDay(view: View) {
        selectedDate = selectedDate.minusDays(1)
        setDates()
    }
    fun actionNextDay(view: View) {
        selectedDate = selectedDate.plusDays(1)
        setDates()
    }



}