package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.jsr310.scrollToDateTime
import com.alamkanak.weekview.jsr310.setDateFormatter
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.dolatkia.animatedThemeManager.ThemeManager
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import com.example.aikataulusuunnitteluapp.data.model.CalendarEntity
import com.example.aikataulusuunnitteluapp.data.model.toWeekViewEntity
import com.example.aikataulusuunnitteluapp.databinding.ActivityCalendarBinding
import com.example.aikataulusuunnitteluapp.themes.LightTheme
import com.example.aikataulusuunnitteluapp.themes.MyAppTheme
import com.example.aikataulusuunnitteluapp.themes.NightTheme
import com.example.aikataulusuunnitteluapp.util.*
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import com.example.aikataulusuunnitteluapp.BasicActivityWeekViewAdapter
import kotlin.properties.Delegates

class Frontpage : ThemeActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener  {

    lateinit var userId: String
    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
    lateinit var preferences: SharedPreferences
    lateinit var preferencesSettings: SharedPreferences
    private lateinit var calendarView: com.alamkanak.weekview.WeekView

    private val cal: Calendar = Calendar.getInstance()
    private var dayOfMonth by Delegates.notNull<Int>()
    private var month by Delegates.notNull<Int>()
    private var year by Delegates.notNull<Int>()
    private var hour by Delegates.notNull<Int>()
    private var minute by Delegates.notNull<Int>()

    private lateinit var binder: ActivityCalendarBinding

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
        binder = ActivityCalendarBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)

        val adapter = BasicActivityWeekViewAdapter(
            loadMoreHandler = viewModel::fetchEvents,
        )
        binder.weekView.adapter = adapter

        binder.weekView.setDateFormatter { date: LocalDate ->
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
                        .make(binder.weekView, action.message, Snackbar.LENGTH_SHORT)
                        .setAction("Undo") { action.undoAction() }
                        .show()


                }
            }
        }
        //user authentication
        preferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = preferences.getString("idUser","").toString()
        println("User ID from SharedPreferences in Frontpage: $userId")


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //adds items to the action bar
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()

        AndroidNetworking.get("$SERVER_URL/settings/$userId")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    val toast = Toast.makeText(applicationContext, response.toString(), Toast.LENGTH_SHORT)
                    //toast.show()
                    println("response = $response")

                    val objectList: JSONObject = response.get(0) as JSONObject
                    val theme = objectList.get("ThemeColor").toString()
                    val enableNotifications = objectList.get("EnableNotifications").toString()
                    val sleepTimeStart = objectList.get("WeekdaySleepTimeStart").toString()
                    val sleepTimeDuration = objectList.get("SleepTimeDuration").toString()

                    preferencesSettings = getSharedPreferences("mySettings", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = preferencesSettings.edit()
                    try {
                        edit.putString("userTheme", theme)
                        edit.putString("enableNotifications", enableNotifications)
                        edit.putString("sleepTimeStart", sleepTimeStart)
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
    }

    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here
        val myAppTheme = appTheme as MyAppTheme

        binder.root.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        binder.weekView.headerBackgroundColor = myAppTheme.activityBackgroundColor(this)
        binder.weekView.timeColumnBackgroundColor = myAppTheme.activityBackgroundColor(this)
        //binder.weekView.todayBackgroundColor = myAppTheme.activityBackgroundColor(this)
        binder.weekView.timeColumnTextColor = myAppTheme.activityTextColor(this)
        //the color change of the days doesn't work
        binder.weekView.dayBackgroundColor = myAppTheme.activityHintColor(this)
        //change the color of the floating action button
        //binder.addTaskBtn.backgroundTintList = ColorStateList.valueOf(myAppTheme.activityThemeButtonColor(this))
        binder.addTaskBtn.backgroundTintList = ColorStateList.valueOf(myAppTheme.activityThemeButtonColor(this))
    }

    override fun getStartTheme(): AppTheme {
        //actionbar+back-button
        val actionbar = supportActionBar
        actionbar!!.title = ""
        supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(
                Color.parseColor("#9E9696"))
        )


        preferencesSettings = getSharedPreferences("mySettings", Context.MODE_PRIVATE)
        val startTheme = preferencesSettings.getString("userTheme","").toString()
        println("This is the theme in frontpage: $startTheme")

        //change the theme to match users theme
        if(startTheme.isNotEmpty()) {
            when {
                startTheme.contains("Night") -> {
                    supportActionBar!!.setBackgroundDrawable(
                        ColorDrawable(
                            Color.parseColor("#373232")))
                    return NightTheme()
                }
                startTheme.contains("Light") -> {
                    supportActionBar!!.setBackgroundDrawable(
                        ColorDrawable(
                            Color.parseColor("#9E9696")))
                    return LightTheme()
                }
            }
        }
        return LightTheme()
    }

    fun openAddTask(view: View) {
        startActivity(Intent(this@Frontpage, AddTask::class.java))
        finish()
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


    fun refreshCalendar() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }



    @SuppressLint("SimpleDateFormat")
    fun openEditDialog(eventTitle: String, eventLoc: String, eventTime: String, idTask: Int, eventEndTime: String) {
        val inflater = LayoutInflater.from(this)
        val dialogLayout: View = inflater.inflate(R.layout.dialog_edittask, null)
        val dialog = AlertDialog.Builder(this)

        dialog.setView(dialogLayout)
        dialog.setTitle("Muokkaa")

        val editTitle: EditText = dialogLayout.findViewById(R.id.et_edit_title)
        val editLocation: EditText = dialogLayout.findViewById(R.id.et_edit_location)
        val editTimeBtn: Button = dialogLayout.findViewById(R.id.btn_editTime)
        val editDuration: EditText = dialogLayout.findViewById(R.id.et_editDuration)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")


        val date: Date = sdf.parse(eventTime)
        val endTimeDate: Date = sdf.parse(eventEndTime)
        cal.time = date
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)

        val duration: Long = (endTimeDate.time - date.time) / 60000
        editDuration.setText(duration.toString())

        editTitle.setText(eventTitle)
        editLocation.setText(eventLoc)

        editTimeBtn.setOnClickListener {
            DatePickerDialog(this, this, year, month, dayOfMonth).show()
        }
        dialog.setPositiveButton("Muokkaa") { _, which ->
            val timeFormat = SimpleDateFormat("HH:mm")
            val obj = JSONObject()
            obj.put("title", editTitle.text.toString())
            obj.put("location", editLocation.text.toString())
            obj.put("start_time", timeFormat.format(cal.time))
            obj.put("day_of_month", dayOfMonth)
            obj.put("idTask", idTask)
            obj.put("duration", editDuration.text.toString().toInt())

            AndroidNetworking.put("${SERVER_URL}/tasks")
                .addJSONObjectBody(obj)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(res: String?) {
                        println(res.toString())
                    }

                    override fun onError(err: ANError?) {
                        println(err.toString())
                    }
                })
            refreshCalendar()
        }
        dialog.setNegativeButton("Peru") { _, which -> }

        dialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        this.year = year
        this.month = month+1 // java.util.Calendar january = 0 :D
        this.dayOfMonth = dayOfMonth
        TimePickerDialog(this,this, hour, minute, true).show() // Opens timepicker when clicking ok in datepicker
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minuteOfHour: Int) {
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minuteOfHour)
    }


}

private class BasicActivityWeekViewAdapter(
    private val loadMoreHandler: (List<YearMonth>) -> Unit
) : WeekViewPagingAdapterJsr310<CalendarEntity>() {

    override fun onCreateEntity(item: CalendarEntity): WeekViewEntity = item.toWeekViewEntity()

    override fun onEventClick(data: CalendarEntity, bounds: RectF) {
        if (data is CalendarEntity.Event) {
            context.showToast("Task id:  ${data.idTask}")
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

    override fun onEventLongClick(data: CalendarEntity, bounds: RectF) {

        if (data is CalendarEntity.Event) {
            val dialog = AlertDialog.Builder(context)
            dialog.setTitle("Haluatko poistaa tämän tehtävän?")
            dialog.setItems(arrayOf("Poista", "Muokkaa")) { _, index ->
                when (index) {
                    0 -> { // Delete clicked
                        val obj = JSONObject()
                        obj.put("idTask", data.idTask)
                        AndroidNetworking.delete("${SERVER_URL}/tasks")
                            .addJSONObjectBody(obj)
                            .build()
                            .getAsString(object : StringRequestListener{
                                override fun onResponse(p0: String?) {
                                    context.showToast("Deleted")
                                    val frontpage = context as Frontpage
                                    frontpage.refreshCalendar()

                                }
                                override fun onError(p0: ANError?) {
                                    println(p0)
                                }
                            })
                    }
                    1 -> { // Edit clicked
                        val frontpage = context as Frontpage
                        frontpage.openEditDialog(data.title.toString(), data.location.toString(), data.startTime.toString(), data.idTask, data.endTime.toString())
                    }
                }
            }
            dialog.setNegativeButton("Peru"){dialogInterface, which ->

            }
            dialog.show()
        }
    }
}
