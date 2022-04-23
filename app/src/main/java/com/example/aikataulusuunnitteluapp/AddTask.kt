package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
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
import com.example.aikataulusuunnitteluapp.themes.LightTheme
import com.example.aikataulusuunnitteluapp.themes.MyAppTheme
import com.example.aikataulusuunnitteluapp.themes.NightTheme
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class AddTask : ThemeActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var settingsPreferences: SharedPreferences
    private lateinit var userId: String
    private lateinit var userTheme: String
    private  var hexColor: String = "#5493D6"

    private val cal: Calendar = Calendar.getInstance()
    private var dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    private var month = cal.get(Calendar.MONTH)
    private var year = cal.get(Calendar.YEAR)
    private var hour = cal.get(Calendar.HOUR_OF_DAY)
    private var minute = cal.get(Calendar.MINUTE)

    private lateinit var title: EditText
    private lateinit var duration: EditText
    private lateinit var location: EditText

    private lateinit var binder: ActivityAddTaskBinding

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAddTaskBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)

        val submitTaskButton: com.google.android.material.floatingactionbutton.FloatingActionButton = findViewById(R.id.btn_submitTask)
        val timepickerButton: Button = findViewById(R.id.btn_timePicker)
        val btnFreeTime: Button = findViewById(R.id.btn_freeTime)
        val btnWork: Button = findViewById(R.id.btn_work)
        val btnMeeting: Button = findViewById(R.id.btn_meeting)
        val btnHobby: Button = findViewById(R.id.btn_hobby)
        val btnOther: Button = findViewById(R.id.btn_other)

        title = findViewById(R.id.et_addTaskHeader)
        duration = findViewById(R.id.et_addTaskDuration)
        location = findViewById(R.id.et_location)
        settingsPreferences = getSharedPreferences("mySettings", Context.MODE_PRIVATE)

        sharedPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("idUser","").toString()

        // On clicking to submit the task, all given values is put into jsonObjects
        // and then into a jsonBody that is sent forward to be saved in the database.
        submitTaskButton.setOnClickListener {
            val timeFormat = SimpleDateFormat("HH:mm")
            val jsonObject = JSONObject()
            jsonObject.put("idUser", userId.toInt())
            jsonObject.put("title", title.editableText.toString())
            jsonObject.put("location", location.editableText.toString())
            jsonObject.put("day_of_month", dayOfMonth)
            jsonObject.put("start_time", timeFormat.format(cal.time))
            jsonObject.put("duration", duration.editableText)
            jsonObject.put("color", hexColor)
            println(jsonObject)

            if(jsonObject["title"] != ""){
                AndroidNetworking.post("${SERVER_URL}/tasks")
                    .addJSONObjectBody(jsonObject)
                    .build()
                    .getAsString(object : StringRequestListener {
                        // When the response is a string the task is saved it closes the AddTask activity and the FrontPage opens.
                        override fun onResponse(res: String?) {
                            finish()
                            val intent = Intent(this@AddTask, Frontpage::class.java)
                            startActivity(intent)
                            Toast.makeText(applicationContext,"$res",Toast.LENGTH_SHORT).show()
                        }
                        // If there is an error given wrong values an AlertDialog will pop up with information.
                        // If the error is something else an error line is printed en the task will not be submitted.
                        override fun onError(err: ANError?) {
                            if (err != null) {
                                println("Error: ${err.errorBody}")
                            }
                            println("There was an error submitting the task")
                        }
                    })
            } else {
                val errorDialog = AlertDialog.Builder(this)
                errorDialog.setTitle("Otsikko puuttuu!")
                errorDialog.setMessage("Otsikko ei voi olla tyhjä")
                errorDialog.setPositiveButton("Ok",{_, i->})
                errorDialog.show()
            }

        }

        // On clicking the choose time button open the DatePickerDialog that then opens the timepicker after Clicking OK.
        timepickerButton.setOnClickListener{
            DatePickerDialog(this, this, year, month, dayOfMonth).show()
        }

        //Category color buttons changes to the color of the of the activity chosen from the category.
        //Free-time beach
        btnFreeTime.setOnClickListener{
            hexColor = java.lang.String.format("#E5BB7A")
            println(hexColor)
        }
        //Work sunkissed_green
        btnWork.setOnClickListener{
            hexColor = java.lang.String.format("#96A88C")
            println(hexColor)
        }
        //Meeting pastel_green
        btnMeeting.setOnClickListener{
            hexColor = java.lang.String.format("#A7CB92")
            println(hexColor)
        }
        //Hobby dry_orange
        btnHobby.setOnClickListener{
            hexColor = java.lang.String.format("#DB813C")
            println(hexColor)
        }
        // other sunkissed_blue
        btnOther.setOnClickListener{
            hexColor = java.lang.String.format("#5493D6")
            println(hexColor)
        }

    }

    override fun syncTheme(appTheme: AppTheme) {
        // The UI color following the users Theme of choice.
        val myAppTheme = appTheme as MyAppTheme
        binder.root.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        //change the color of the arrow
        binder.backOutFromAddTask.setColorFilter(myAppTheme.activityIconColor(this))
        //buttons
        binder.btnSubmitTask.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        binder.btnTimePicker.setTextColor(myAppTheme.activityTextColor(this))
        //texts
        binder.tvTaskSetterText.setTextColor(myAppTheme.activityTextColor(this))
        binder.etAddTaskHeader.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.tvChooseCategoty.setTextColor(myAppTheme.activityHintColor(this))
        binder.tvTaskDuration.setTextColor(myAppTheme.activityHintColor(this))
        binder.tvStartingTime.setTextColor(myAppTheme.activityHintColor(this))
        //edit texts
        binder.etAddTaskHeader.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etLocation.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etAddTaskDuration.setHintTextColor(myAppTheme.activityTextColor(this))
    }

    // This function takes year, month and day values from DatePicker widget and puts them in variables
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        this.year = year
        this.month = month+1 // java.util.Calendar january = 0
        this.dayOfMonth = dayOfMonth
        TimePickerDialog(this,this, hour, minute, true).show() // Opens timepicker when clicking ok in datePicker
    }
    // This function takes hours and minutes from TimePicker widget and puts them in variables
    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        // The timepickerButton now takes the chosen date and time and changes the text on the invisible borderline button to chosen date and time.
        val timepickerButton: Button = findViewById(R.id.btn_timePicker)
        timepickerButton.text = String.format(Locale.getDefault(),"%02d.%02d.%02d  %02d:%02d",dayOfMonth,month, year, hourOfDay, minute);
    }

    // get the Theme the user have chosen.
    override fun getStartTheme(): AppTheme {
        settingsPreferences = getSharedPreferences("mySettings", Context.MODE_PRIVATE)
        val startTheme = settingsPreferences.getString("userTheme", "").toString()
        //change the theme to match users theme
        if (startTheme.isNotEmpty()) {
            when {
                startTheme.contains("Night") -> {
                    return NightTheme()
                }
                startTheme.contains("Light") -> {
                    return LightTheme()
                }
            }
        }
        return LightTheme()
    }

     //overrides the backbutton and goes back to the frontpage and closes AddTask.
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@AddTask, Frontpage::class.java)
        startActivity(intent)
        finish()
    }
    //Backbutton goes back to the frontpage and closes AddTask.
    fun closeTaskSetter(view: View) {
        val intent = Intent(this@AddTask, Frontpage::class.java)
        startActivity(intent)
        finish()
    }


}
