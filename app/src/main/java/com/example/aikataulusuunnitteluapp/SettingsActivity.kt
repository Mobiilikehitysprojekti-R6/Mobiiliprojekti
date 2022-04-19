package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {

    private val cal: Calendar = Calendar.getInstance()
    private var hour = cal.get(Calendar.HOUR_OF_DAY)
    private var minute = cal.get(Calendar.MINUTE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val numberPickerHours = findViewById<NumberPicker>(R.id.numberPickerHours)
        numberPickerHours.minValue = 1
        numberPickerHours.maxValue = 12

        val numberPickerMinutes = findViewById<NumberPicker>(R.id.numberPickerMinutes)
        numberPickerMinutes.minValue = 0
        numberPickerMinutes.maxValue = 60

        numberPickerHours.wrapSelectorWheel = false
        numberPickerMinutes.wrapSelectorWheel = false

        val button: Button = findViewById<Button>(R.id.btnApply)
        val timepickerButton: Button = findViewById(R.id.btn_SleepStart)

        var sleepTimeHours: Int = 0
        var sleepTimeMinutes: Int = 0
        var totalSleepTime: Int = 0

        timepickerButton.setOnClickListener {
            TimePickerDialog(this,this, hour, minute, true).show()
        }

        numberPickerHours.setOnValueChangedListener { picker, oldVal, newVal ->

            //Display the newly selected number to text view
            println("Selected Hour Value : $newVal")
            sleepTimeHours = newVal
            totalSleepTime = (sleepTimeHours * 60) + sleepTimeMinutes
            println("$totalSleepTime ; total sleep time in minutes")
        }

        numberPickerMinutes.setOnValueChangedListener { picker, oldVal, newVal ->

            //Display the newly selected number to text view
            println("Selected Minute Value : $newVal")
            sleepTimeMinutes = newVal
            totalSleepTime = (sleepTimeHours * 60) + sleepTimeMinutes
            println("$totalSleepTime ; total sleep time in minutes")
        }


        button.setOnClickListener {
            val timeFormat = SimpleDateFormat("HH:mm")
            println(timeFormat.format(cal.time))
            postSettings(timeFormat.format(cal.time), totalSleepTime)
        }
    }

    private fun postSettings(weekDaySleepTimeStart: String, sleepTimeDuration: Int) {

        val userIDString = intent.getStringExtra("userID")
        val userIDInt = userIDString?.toInt()

        val jsonObject = JSONObject()
        try {
            jsonObject.put("weekdaySleepTimeStart", weekDaySleepTimeStart)
            jsonObject.put("sleepTimeDuration", sleepTimeDuration)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        AndroidNetworking.post("$SERVER_URL/settings/$userIDInt")
            .addJSONObjectBody(jsonObject)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(res: String?) {
                    println("Got response from API: $res")
                    if (res.toString() == "Created") {

                        val toast = Toast.makeText(applicationContext, "Settings created," +
                                " please login", Toast.LENGTH_SHORT)
                        toast.show()
                        val intent =
                            Intent(this@SettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    }
                }

                @SuppressLint("SetTextI18n")
                override fun onError(error: ANError) {
                    println("Error: ${error.errorBody}")

                    val builder = AlertDialog.Builder(this@SettingsActivity)
                    builder.setTitle("Network error!")
                    if (error.errorBody == null) {
                        builder.setMessage("Unknown error")
                    } else {
                        builder.setMessage(error.errorBody.toString())
                    }

                    builder.setPositiveButton("OK") { dialogInterface, which ->
                        println("Failed API Call, not retried")
                    }

                    builder.show()
                }
            })
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
    }
}