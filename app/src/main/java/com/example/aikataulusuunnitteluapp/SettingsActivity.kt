package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import org.json.JSONException
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val etNotifications = findViewById<EditText>(R.id.etNotifications)
        val etSleepTimeStart = findViewById<EditText>(R.id.etSleepTimeStart)
        val etSleepTimeDuration = findViewById<EditText>(R.id.etSleepTimeDuration)
        val etThemeColor = findViewById<EditText>(R.id.etThemeColor)
        val button: Button = findViewById<Button>(R.id.btnApply)

        button.setOnClickListener {

            var notificationTinyInt : Int = 0

            if(etNotifications.text.length > 1) {
                val builder = AlertDialog.Builder(this@SettingsActivity)
                builder.setTitle("More than one character found in Notifications field!")
                builder.setMessage("Please only write y for yes or n for no")
                builder.setPositiveButton("OK") { dialogInterface, which ->
                    println("Failed")
                }
                builder.show()
            } else {

                if(etNotifications.text.toString() == "y") {
                    notificationTinyInt = 1
                } else {
                    notificationTinyInt = 0
                }

                postSettings(notificationTinyInt, etThemeColor.text.toString(),
                    etSleepTimeStart.text.toString(), etSleepTimeDuration.text)
            }
        }
    }

    private fun postSettings(notification: Int, themeColor: String,
                             sleepTimeStart: String, sleepTimeDuration: Editable) {

        val userIDString = intent.getStringExtra("userID")
        val userIDInt = userIDString?.toInt()

        val jsonObject = JSONObject()
        try {
            jsonObject.put("enableNotifications", notification)
            jsonObject.put("themeColor", themeColor)
            jsonObject.put("sleepTimeStart", sleepTimeStart)
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

                        val builder = AlertDialog.Builder(this@SettingsActivity)
                        builder.setTitle("Settings registered!")
                        builder.setMessage("Settings were successfully registered, please login now")
                        builder.setPositiveButton("OK") { dialogInterface, which ->
                            val intent =
                                Intent(this@SettingsActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        builder.show()
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
}