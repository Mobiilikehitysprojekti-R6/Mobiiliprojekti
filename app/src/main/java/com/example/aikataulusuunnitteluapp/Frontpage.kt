package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.jsr310.setDateFormatter
import com.example.aikataulusuunnitteluapp.data.model.CalendarEntity
import com.example.aikataulusuunnitteluapp.data.model.toWeekViewEntity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import com.example.aikataulusuunnitteluapp.databinding.ActivityCalendarBinding
import com.example.aikataulusuunnitteluapp.util.*
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*


class Frontpage : AppCompatActivity(), PopupMenu.OnMenuItemClickListener  {

    lateinit var userId: String
    lateinit var headerList: MutableList<String>
    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
    lateinit var preferences: SharedPreferences

    private val binding: ActivityCalendarBinding by lazy {
        ActivityCalendarBinding.inflate(layoutInflater)
    }

    private val viewModel by genericViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        //user authentication
        preferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = preferences.getString("idUser","").toString()
        println("User ID from SharedPreferences in Frontpage: $userId")

        //actionbar+back-button
        val actionbar = supportActionBar
        actionbar!!.title = "Frontpage"




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


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //adds items to the action bar
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()

        AndroidNetworking.get("$SERVER_URL/tasks/$userId")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    //TODO: what to do with the response?
                    val toast = Toast.makeText(applicationContext, response.toString(), Toast.LENGTH_SHORT)
                    toast.show()

                    val res : JSONArray = response
                    headerList = mutableListOf()
                    for(i in 0 until res.length()) {
                        val obj : JSONObject = res[i] as JSONObject
                        headerList.add(obj.get("Header").toString())
                    }
                    println(headerList)

                }

                override fun onError(error: ANError) {
                    //TODO: handle error on task get request
                }
            })
    }

    fun openAddTask(view: View) {
        startActivity(Intent(this@Frontpage, AddTask::class.java))
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        TODO("Not yet implemented")
    }

    fun logOut(item: MenuItem) {

        val editor : SharedPreferences.Editor = preferences.edit()
        editor.clear()
        editor.apply()

        startActivity(Intent(this@Frontpage, MainActivity::class.java))
        finish()
    }

    fun toProfileAndSettings(item: MenuItem) {
        startActivity(Intent(this@Frontpage, ProfileSettings::class.java))
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