package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import com.example.aikataulusuunnitteluapp.databinding.ActivityAddTaskBinding
import com.example.aikataulusuunnitteluapp.themes.MyAppTheme
import com.example.aikataulusuunnitteluapp.themes.NightTheme
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class AddTask : ThemeActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private  var hexColor: String = "#ff0000"

    private val cal: Calendar = Calendar.getInstance()
    private var dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    private var month = cal.get(Calendar.MONTH)
    private var year = cal.get(Calendar.YEAR)
    private var hour = cal.get(Calendar.HOUR_OF_DAY)
    private var minute = cal.get(Calendar.MINUTE)

    private lateinit var startingTime: TextView
    private lateinit var title: EditText
    private lateinit var duration: EditText
    private lateinit var location: EditText

    private lateinit var binder: ActivityAddTaskBinding

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAddTaskBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)

        val submitTaskButton: Button = findViewById(R.id.btn_submitTask)
        val timepickerButton: Button = findViewById(R.id.btn_timePicker)
        val btnFreeTime: Button = findViewById(R.id.btn_freeTime)
        val btnWork: Button = findViewById(R.id.btn_work)
        val btnMeeting: Button = findViewById(R.id.btn_meeting)
        val btnHobby: Button = findViewById(R.id.btn_hobby)
        val btnOther: Button = findViewById(R.id.btn_other)


        title = findViewById(R.id.et_addTaskHeader)
        duration = findViewById(R.id.et_addTaskDuration)
        location = findViewById(R.id.et_location)

        sharedPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("idUser","").toString()

        submitTaskButton.setOnClickListener {
            val timeFormat = SimpleDateFormat("HH:mm")
            val jsonObject: JSONObject = JSONObject()
            jsonObject.put("idUser", userId.toInt())
            jsonObject.put("title", title.editableText.toString())
            jsonObject.put("location", location.editableText.toString())
            jsonObject.put("day_of_month", dayOfMonth)
            jsonObject.put("start_time", timeFormat.format(cal.time))
            jsonObject.put("duration", duration.editableText)
            jsonObject.put("color", hexColor)
            println(jsonObject)

            AndroidNetworking.post("${SERVER_URL}/tasks")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(res: String?) {
                        finish()
                        val intent = Intent(this@AddTask, Frontpage::class.java)
                        startActivity(intent)
                        Toast.makeText(applicationContext,"$res",Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(err: ANError?) {
                        TODO("Not yet implemented")
                    }
                })
        }

        timepickerButton.setOnClickListener{
            DatePickerDialog(this, this, year, month, dayOfMonth).show()
        }

        //Category color buttons
        btnFreeTime.setOnClickListener{
            hexColor = java.lang.String.format("#E5BB7A")
            println(hexColor)
        }
        btnWork.setOnClickListener{
            hexColor = java.lang.String.format("#96A88C")
            println(hexColor)
        }
        btnMeeting.setOnClickListener{
            hexColor = java.lang.String.format("#A7CB92")
            println(hexColor)
        }
        btnHobby.setOnClickListener{
            hexColor = java.lang.String.format("#DB813C")
            println(hexColor)
        }
        btnOther.setOnClickListener{
            hexColor = java.lang.String.format("#5493D6")
            println(hexColor)
        }

    }

    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here
        val myAppTheme = appTheme as MyAppTheme
        binder.root.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        //change the background of the arrow icon
        //change the color of the arrow
        binder.backOutFromAddTask.setColorFilter(myAppTheme.activityIconColor(this))
        //buttons
        binder.btnTimePicker.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        binder.btnColor.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        binder.btnSubmitTask.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        //texts
        binder.tvAddNewTaskTitle.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvAddTask.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvTaskLocation.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvTaskStartTime.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvStartingtime.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvTaskDuration.setTextColor(myAppTheme.activityTextColor(this))
        //edit texts
        binder.etAddTaskHeader.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etLocation.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etAddTaskDuration.setHintTextColor(myAppTheme.activityHintColor(this))
    }

    // This function takes year, month and day values from DatePicker widget and puts them in variables
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        this.year = year
        this.month = month+1 // java.util.Calendar january = 0 :D
        this.dayOfMonth = dayOfMonth
        TimePickerDialog(this,this, hour, minute, true).show() // Opens timepicker when clicking ok in datepicker
    }
    // This function takes hours and minutes from TimePicker widget and puts them in variables
    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)

        val timepickerButton: Button = findViewById(R.id.btn_timePicker)
        timepickerButton.text = String.format(Locale.getDefault(),"%02d.%02d.%02d  %02d:%02d",dayOfMonth,month, year, hour, minute);
    }

    override fun getStartTheme(): AppTheme {
        return NightTheme()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@AddTask, Frontpage::class.java)
        startActivity(intent)
        finish()
    }

    fun closeTaskSetter(view: View) {
        val intent = Intent(this@AddTask, Frontpage::class.java)
        startActivity(intent)
        finish()
    }


}
