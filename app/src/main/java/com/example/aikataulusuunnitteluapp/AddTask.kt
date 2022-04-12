package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import org.json.JSONObject
import vadiole.colorpicker.ColorModel
import vadiole.colorpicker.ColorPickerDialog
import java.text.SimpleDateFormat
import java.util.*


class AddTask : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private  var hexColor: String = "#ff0000"

    private val cal: Calendar = Calendar.getInstance()
    private var dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    private var month = cal.get(Calendar.MONTH)
    private var year = cal.get(Calendar.YEAR)
    private var hour = cal.get(Calendar.HOUR)
    private var minute = cal.get(Calendar.MINUTE)

    private lateinit var startingTime: TextView
    private lateinit var title: EditText
    private lateinit var duration: EditText
    private lateinit var location: EditText
    private lateinit var colorPreview: View

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val submitTaskButton: Button = findViewById(R.id.btn_submitTask)
        val timepickerButton: Button = findViewById(R.id.btn_timePicker)
        val colorPickerButton: Button = findViewById(R.id.btn_color)

        title = findViewById(R.id.et_addTaskHeader)
        duration = findViewById(R.id.et_addTaskDuration)
        location = findViewById(R.id.et_location)
        startingTime = findViewById(R.id.tv_startingtime)
        colorPreview = findViewById(R.id.view_colorPreview)


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

        colorPickerButton.setOnClickListener{
            @Suppress("DEPRECATION") val colorPicker: ColorPickerDialog = ColorPickerDialog.Builder()
                .setInitialColor(-65536)
                .setColorModel(ColorModel.HSV)
                .setColorModelSwitchEnabled(true)
                .setButtonOkText(android.R.string.ok)
                .setButtonCancelText(android.R.string.cancel)
                //  callback for picked color (required)
                .onColorSelected { color: Int ->
                    hexColor = java.lang.String.format("#%06X", 0xFFFFFF and color)
                    val colorPreviewColor: GradientDrawable = colorPreview.background as GradientDrawable
                    colorPreviewColor.setColor(Color.parseColor(hexColor))
                }
                .create()
            colorPicker.show(supportFragmentManager, "color_picker")
        }

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
    }
    fun closeTaskSetter(view: View) {
        finish()
    }
}
