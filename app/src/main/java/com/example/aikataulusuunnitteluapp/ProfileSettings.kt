package com.example.aikataulusuunnitteluapp

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.dolatkia.animatedThemeManager.ThemeManager
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import com.example.aikataulusuunnitteluapp.databinding.ActivityProfileSettingsBinding
import com.example.aikataulusuunnitteluapp.themes.LightTheme
import com.example.aikataulusuunnitteluapp.themes.MyAppTheme
import com.example.aikataulusuunnitteluapp.themes.NightTheme
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.util.*


class ProfileSettings : ThemeActivity(), TimePickerDialog.OnTimeSetListener {

    private val cal: Calendar = Calendar.getInstance()
    private lateinit var binder: ActivityProfileSettingsBinding
    lateinit var myIdPreferences: SharedPreferences
    lateinit var themePreferences: SharedPreferences
    lateinit var userId: String
    lateinit var themeId: String
    lateinit var sleepTimeStart: String
    lateinit var sleepTimeDuration: String
    lateinit var enableNotifications: String
    lateinit var premiumMessage: String
    private var hour = cal.get(Calendar.HOUR_OF_DAY)
    private var minute = cal.get(Calendar.MINUTE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // full screen app->makes a transparent status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        // create and bind views
        binder = ActivityProfileSettingsBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)

        //retrieve user id from shared preferences
        myIdPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        val premiumStatus = myIdPreferences.getString("premiumStatus", "").toString()
        if (premiumStatus == "1") {
            //if premium status is checked
            println("premiumstatus if function works")
            binder.switchChangePremiumStatus.isChecked = true
        }
        userId = myIdPreferences.getString("idUser", "").toString()
        val username = myIdPreferences.getString("username", "").toString()
        binder.tvUsername.text = "Tervetuloa takaisin: $username"

        //retrieve sleeptimestart and sleeptimeduration from shared preferences
        themePreferences = getSharedPreferences("mySettings", Context.MODE_PRIVATE)
        sleepTimeStart = themePreferences.getString("sleepTimeStart", "").toString()
        sleepTimeDuration = themePreferences.getString("sleepTimeDuration", "").toString()
        enableNotifications = themePreferences.getString("enableNotifications", "").toString()

        //set button text to users sleeptimestart
        binder.btnGoToBedAt.text = sleepTimeStart

        //if user has notifications on --> set switch as checked
        if (enableNotifications == "1") {
            //if premium status is checked
            println("premiumstatus if function works")
            binder.switchChangeNotificationStatus.isChecked = true
        }

        //Number pickers
        val numberPickerHours = findViewById<NumberPicker>(R.id.numberPickerHours)
        val numberPickerMinutes = findViewById<NumberPicker>(R.id.numberPickerMinutes)

        //set min and max limits
        numberPickerHours.minValue = 1
        numberPickerHours.maxValue = 23
        numberPickerMinutes.minValue = 1
        numberPickerMinutes.maxValue = 60
        //set number picker wheel not to wrap
        numberPickerHours.wrapSelectorWheel = false
        numberPickerMinutes.wrapSelectorWheel = false

        numberPickerHours.setOnValueChangedListener { picker, oldVal, newVal ->
            //Display the newly selected number to text view
            println("Selected Hour Value : $newVal")
            sleepTimeDuration = binder.numberPickerHours.value.toString()
        }
        //parse the sleeptimeduration from minutes to hours and minutes
        val hours  = sleepTimeDuration.toInt().div(60)
        val minutes = sleepTimeDuration.toInt()-hours*60
        println("Users hours: $hours and minutes: $minutes")

        //set number picker values to represent values of the sharedPrefs
        if (hours== 0) {
            numberPickerHours.value = 0
            numberPickerMinutes.value = minutes
        }
        else {
            numberPickerHours.value = hours
            numberPickerMinutes.value = minutes
        }

        // set change theme click listeners for buttons
        updateButtonText()
        binder.btnChangeTheme.setOnClickListener {
            if (ThemeManager.instance.getCurrentTheme()
                    ?.id() == NightTheme.ThemeId
            ) {
                ThemeManager.instance.reverseChangeTheme(LightTheme(), it)
            } else if (ThemeManager.instance.getCurrentTheme()
                    ?.id() != NightTheme.ThemeId
            ) {
                ThemeManager.instance.changeTheme(NightTheme(), it)
            }
            updateButtonText()
            updateDatabaseTheme()
        }

        binder.btnUpdatePassword.setOnClickListener {
            if (binder.etNewPassword.text.toString() == binder.etNewPassWordRepeat.text.toString()
            ) {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("idUser", userId)
                    jsonObject.put("password", binder.etOldPassword.text.toString())
                    jsonObject.put("newPassword", binder.etNewPassword.text.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                AndroidNetworking.put("$SERVER_URL/settings/editpassword")
                    .addJSONObjectBody(jsonObject)
                    .build()
                    .getAsString(object : StringRequestListener {
                        override fun onResponse(response: String?) {
                            Toast.makeText(
                                applicationContext,
                                response.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                            //clear the fields, give a toast and close the settings page
                            binder.etOldPassword.text = null
                            binder.etNewPassword.text = null
                            binder.etNewPassWordRepeat.text = null
                            println("password updated")
                            finish()
                        }
                        override fun onError(error: ANError?) {
                            if (error != null) {
                                println("Error: ${error.errorBody}")
                            }
                            println("password update failed")
                        }
                    })
            } else {
                Toast.makeText(
                    applicationContext,
                    "Error in the password fields.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binder.switchChangePremiumStatus.setOnCheckedChangeListener { buttonView, isChecked ->
            val jsonObject = JSONObject()
            val userId = myIdPreferences.getString("idUser", "").toString()
            if (binder.switchChangePremiumStatus.isChecked) {
                //if premium status is checked
                try {
                    jsonObject.put("premiumStatus", 1)
                    jsonObject.put("idUser", userId)
                    premiumMessage = "Premium ordered"
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                //if premium status is unchecked
                try {
                    jsonObject.put("premiumStatus", 0)
                    premiumMessage = "Premium cancelled"
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            AndroidNetworking.put("$SERVER_URL/settings/editpremiumstatus")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String?) {
                        Toast.makeText(
                            applicationContext,
                            premiumMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                        println("premium status updated")
                       val edit: SharedPreferences.Editor =  myIdPreferences.edit()
                        try {
                            edit.putString("premiumStatus", premiumStatus)
                            edit.apply()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    override fun onError(error: ANError?) {
                        if (error != null) {
                            println("Error: ${error.errorBody}")
                        }
                        println("premium status update failed")
                    }
                })
        }

        binder.switchChangeNotificationStatus.setOnCheckedChangeListener { buttonView, isChecked ->
            val jsonObject = JSONObject()

            if (binder.switchChangeNotificationStatus.isChecked) {
                //if premium status is checked
                try {
                    println("status 1")
                    jsonObject.put("notificationStatus", 1)
                    jsonObject.put("idUser", userId)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                 //if premium status is unchecked
                try {
                    println("status 0")
                    jsonObject.put("notificationStatus", 0)
                    jsonObject.put("idUser", userId)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            AndroidNetworking.put("$SERVER_URL/settings/editnotificationstatus")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String?) {
                        Toast.makeText(
                            applicationContext,
                            "Notifications enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                        println("Notifications enabled")
                    }
                    override fun onError(error: ANError?) {
                        if (error != null) {
                            println("Error: ${error.errorBody}")
                        }
                        println("Notification change failed")
                    }
                })
        }

        binder.btnSaveUserSettings.setOnClickListener {
            val jsonObject = JSONObject()
            //count the hours and minutes together
            val totalHours = (60*binder.numberPickerHours.value) + binder.numberPickerMinutes.value

            try {
                jsonObject.put("sleepTimeStart", binder.btnGoToBedAt.text.toString())
                jsonObject.put("sleepTimeDuration",totalHours.toString())
                jsonObject.put("idUser", userId)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            AndroidNetworking.put("$SERVER_URL/settings/editSleepInformation")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String?) {
                        println("sleep settings updated")
                        Toast.makeText(
                            applicationContext,
                            "Your sleep settings have been updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    override fun onError(error: ANError?) {
                        if (error != null) {
                            println("Error: ${error.errorBody}")
                        }
                        println("error with updating sleep settings ")
                    }
                })
        }
        binder.btnGoToBedAt.setOnClickListener {
            TimePickerDialog(
                this,
                this,
                hour,
                minute,
                true
            ).show() // Opens timepicker when clicking ok in datepicker
        }
    }

    //this function changes the colors of the different views
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here
        val myAppTheme = appTheme as MyAppTheme
        // set background color
        binder.root.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        //change the color of the buttons
        binder.switchChangeNotificationStatus.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        binder.btnSaveUserSettings.setBackgroundColor((myAppTheme.activityThemeButtonColor(this)))
        binder.switchChangePremiumStatus.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        binder.btnUpdatePassword.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        binder.btnGoToBedAt.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        //change the color of the text views
        binder.tvSettingsText.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvChooseATheme.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvEnableNotifications.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvGoToBedAt.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvSleepDurationHeader.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvProfile.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvNewPassword.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvUsername.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvHours.setTextColor(myAppTheme.activityTextColor(this))
        binder.switchChangeNotificationStatus.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvMinutes.setTextColor(myAppTheme.activityTextColor(this))
        //change the color of all the edit texts
        binder.tvUsername.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etOldPassword.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etNewPassword.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etNewPassWordRepeat.setHintTextColor(myAppTheme.activityHintColor(this))
        //switch
        binder.switchChangePremiumStatus.setTextColor(myAppTheme.activityTextColor(this))
        binder.switchChangePremiumStatus.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        //return arrow
        binder.backOutFromSettings.setColorFilter(myAppTheme.activityIconColor(this))
        //change number picker text color
        binder.btnChangeTheme.setCardBackgroundColor(appTheme.activityThemeButtonColor(this))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binder.numberPickerHours.textColor = appTheme.activityTextColor(this)
        }
        //syncStatusBarIconColors
       // syncStatusBarIconColors(appTheme)
    }

/*    private fun syncStatusBarIconColors(theme: MyAppTheme) {
        ThemeManager.instance.syncStatusBarIconsColorWithBackground(
            this,
            theme.activityBackgroundColor(this)
        )
        ThemeManager.instance.syncNavigationBarButtonsColorWithBackground(
            this,
            theme.activityBackgroundColor(this)
        )
    }*/

    private fun updateButtonText() {
        if (ThemeManager.instance.getCurrentTheme()?.id() == NightTheme.ThemeId) {
            binder.buttonTextView.text = "Light"
        } else {
            binder.buttonTextView.text = "Night"
        }
    }

    private fun updateDatabaseTheme() {
        themeId = if (ThemeManager.instance.getCurrentTheme()?.id() == NightTheme.ThemeId) {
            "Night"
        } else {
            "Light"
        }

        val jsonObject = JSONObject()
        try {
            jsonObject.put("newColor", themeId)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        AndroidNetworking.put("$SERVER_URL/settings/edittheme/${userId}")
            .addJSONObjectBody(jsonObject)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String?) {
                    Toast.makeText(
                        applicationContext,
                        "Your theme color has been updated to $themeId",
                        Toast.LENGTH_SHORT).show()
                    themePreferences = getSharedPreferences("mySettings", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = themePreferences.edit()
                    try {
                        edit.putString("userTheme", themeId)
                        edit.apply()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                override fun onError(error: ANError?) {
                    if (error != null) {
                        println("Error: ${error.errorBody}")
                    }
                    println("Theme color update failed")
                }
            })
    }

    //set default theme
    override fun getStartTheme(): AppTheme {
        themePreferences = getSharedPreferences("mySettings", Context.MODE_PRIVATE)
        val startTheme = themePreferences.getString("userTheme", "").toString()

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

    fun closeSettings(view: View) {
        finish()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
        cal.set(Calendar.MINUTE, minute)
        val formattedTime = SimpleDateFormat("HH:mm").format(cal.time)
        println(formattedTime)
        binder.btnGoToBedAt.text = formattedTime
    }

}


