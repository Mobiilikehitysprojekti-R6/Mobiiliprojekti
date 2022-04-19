package com.example.aikataulusuunnitteluapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.jsr310.scrollToDateTime
import com.alamkanak.weekview.jsr310.setDateFormatter
import com.example.aikataulusuunnitteluapp.data.model.CalendarEntity
import com.example.aikataulusuunnitteluapp.data.model.toWeekViewEntity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import com.example.aikataulusuunnitteluapp.databinding.ActivityCalendarBinding
import com.example.aikataulusuunnitteluapp.util.*
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONStringer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*


class Frontpage : AppCompatActivity(), PopupMenu.OnMenuItemClickListener  {

    lateinit var userId: String
    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
    lateinit var preferences: SharedPreferences
    lateinit var preferencesSettings: SharedPreferences
    private lateinit var calendarView: com.alamkanak.weekview.WeekView

    private val binding: ActivityCalendarBinding by lazy {
        ActivityCalendarBinding.inflate(layoutInflater)
    }

    private val viewModel by genericViewModel()

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this@Frontpage)
        builder.setTitle("Do you want to log out?")
        builder.setPositiveButton("YES"){dialogInterface, which ->
            val editor : SharedPreferences.Editor = preferences.edit()
            editor.clear()
            editor.apply()
            startActivity(Intent(this@Frontpage, MainActivity::class.java))
            finish()
        }
        builder.setNegativeButton("NO"){dialogInterface, which ->
        }
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val adapter = BasicActivityWeekViewAdapter(
            loadMoreHandler = viewModel::fetchEvents,
        )
        binding.weekView.adapter = adapter

        binding.weekView.setDateFormatter { date: LocalDate ->
            val weekdayLabel = weekdayFormatter.format(date)
            val dateLabel = dateFormatter.format(date)
            weekdayLabel + "\n" + dateLabel
        }

        viewModel.viewState.observe(this) { viewState ->
            adapter.submitList(viewState.entities)
        }

        viewModel.actions.subscribeToEvents(this) { action ->
            when (action) {
                is GenericAction.ShowSnackbar -> {
                    Snackbar
                        .make(binding.weekView, action.message, Snackbar.LENGTH_SHORT)
                        .setAction("Undo") { action.undoAction() }
                        .show()

                }
            }
        }
        //user authentication
        preferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = preferences.getString("idUser","").toString()
        println("User ID from SharedPreferences in Frontpage: $userId")


        //actionbar+back-button
        val actionbar = supportActionBar
        actionbar!!.title = "Frontpage"


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //adds items to the action bar
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()


        println("OnResume printline")

        AndroidNetworking.get("$SERVER_URL/settings/$userId")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    val toast = Toast.makeText(applicationContext, response.toString(), Toast.LENGTH_SHORT)
                    toast.show()
                    println("response = $response")

                    val objectList: JSONObject = response.get(0) as JSONObject
                    val theme = objectList.get("ThemeColor").toString()
                    val enableNotifications = objectList.get("EnableNotifications").toString()
                    val weekdaySleepTimeStart = objectList.get("WeekdaySleepTimeStart").toString()
                    //val weekendSleepTimeStart = objectList.get("WeekendSleepTimeStart").toString()
                    val sleepTimeDuration = objectList.get("SleepTimeDuration").toString()

                    preferencesSettings = getSharedPreferences("mySettings", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = preferencesSettings.edit()
                    try {
                        edit.putString("userTheme", theme)
                        edit.putString("enableNotifications", enableNotifications)
                        edit.putString("weekdaySleepTimeStart", weekdaySleepTimeStart)
                        //edit.putString("weekendSleepTimeStart", weekendSleepTimeStart)
                        edit.putString("sleepTimeDuration", sleepTimeDuration)
                        edit.apply()
                        println("Theme saved to SharedPreferences = $theme")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                override fun onError(error: ANError) {
                    //TODO: handle error on task get request
                }
            })

        //TODO: tee tänne funktio joka kirjoittaa themeidN kohdalle


    }

    fun openAddTask(view: View) {
        startActivity(Intent(this@Frontpage, AddTask::class.java))
        finish()
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        TODO("Not yet implemented")
    }

    fun logOut(item: MenuItem) {
        var editor : SharedPreferences.Editor = preferences.edit()
        editor.clear()
        editor.apply()
        editor = preferencesSettings.edit()
        editor.clear()
        editor.apply()
        startActivity(Intent(this@Frontpage, MainActivity::class.java))
        finish()
    }

    fun toProfileAndSettings(item: MenuItem) {
        startActivity(Intent(this@Frontpage, ProfileSettings::class.java))
    }

    fun toAboutUsPage(item: MenuItem) {
        startActivity(Intent(this@Frontpage, AboutUs::class.java))
    }

    fun selectOneDay(item: MenuItem) {
        calendarView =  findViewById(R.id.weekView)
        if(calendarView.numberOfVisibleDays != 1) {
            calendarView.numberOfVisibleDays = 1
        } else {
            val toast = Toast.makeText(applicationContext, "The number of days is already 1!", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
    fun selectThreeDays(item: MenuItem) {
        calendarView =  findViewById(R.id.weekView)
        if(calendarView.numberOfVisibleDays != 3) {
            calendarView.numberOfVisibleDays = 3
        } else {
            val toast = Toast.makeText(applicationContext, "The number of days is already 3!", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
    fun selectSevenDays(item: MenuItem) {
        calendarView =  findViewById(R.id.weekView)
        if(calendarView.numberOfVisibleDays != 7) {
            calendarView.numberOfVisibleDays = 7
        } else {
            val toast = Toast.makeText(applicationContext, "The number of days is already 7!", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
    fun toCurrentDay(item: MenuItem) {
        calendarView =  findViewById(R.id.weekView)
        with(calendarView) { scrollToDateTime(dateTime = LocalDateTime.now()) }
        }
}


private class BasicActivityWeekViewAdapter(
    private val loadMoreHandler: (List<YearMonth>) -> Unit
) : WeekViewPagingAdapterJsr310<CalendarEntity>() {

    override fun onCreateEntity(item: CalendarEntity): WeekViewEntity = item.toWeekViewEntity()

    override fun onEventClick(data: CalendarEntity, bounds: RectF) {
        if (data is CalendarEntity.Event) {
            context.showToast("Clicked ${data.title}")
        }
    }
    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        loadMoreHandler(yearMonthsBetween(startDate, endDate))
    }
}