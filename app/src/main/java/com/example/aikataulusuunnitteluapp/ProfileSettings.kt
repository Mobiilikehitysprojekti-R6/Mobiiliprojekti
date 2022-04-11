package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
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
import java.util.*
import kotlin.concurrent.schedule

class ProfileSettings : ThemeActivity() {

    private lateinit var binder: ActivityProfileSettingsBinding
    lateinit var preferences: SharedPreferences
    lateinit var userId: String
    lateinit var themeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // full screen app
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // create and bind views
        binder = ActivityProfileSettingsBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)


        // set change theme click listeners for buttons
        updateButtonText()
        //TODO:change the name of the button
        binder.button.setOnClickListener {
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


        //retrieve userid with sharedPreferences
        preferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = preferences.getString("idUser", "").toString()
        println("User ID from SharedPreferences in ProfileSettings: $userId")


        binder.btnUpdatePassword.setOnClickListener {

            if (binder.etNewPassword.text.toString() == binder.etNewPassWordRepeat.text.toString()) {
                println("new passwords match")

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
                    //TODO: Change the priority of this request if there are going to be more requests in this file
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsString(object : StringRequestListener {
                        override fun onResponse(response: String?) {
                            val toast = Toast.makeText(
                                applicationContext,
                                response.toString(),
                                Toast.LENGTH_LONG
                            )
                            toast.show()
                            //clear the fields, give a toast and close the settings page
                            binder.etOldPassword.text = null
                            binder.etNewPassword.text = null
                            binder.etNewPassWordRepeat.text = null
                            println("password updated")
                            finish()
                        }
                        override fun onError(error: ANError?) {
                            if (error != null) { println("Error: ${error.errorBody}")}
                            println("password update failed")
                        }
                    })
            }
        }

    }
    //this function changes the colors of the different views
    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here
        val myAppTheme = appTheme as MyAppTheme

        // set background color
        binder.root.setBackgroundColor(myAppTheme.activityBackgroundColor(this))

        //TODO:change the color of all the buttons


        //change the color of the string values
        binder.tvSettingsText.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvChooseATheme.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvEnableNotifications.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvGoToBedAt.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvWhatAboutWeekends.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvIwantToSleepFor.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvProfile.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvNewPassword.setTextColor(myAppTheme.activityTextColor(this))

        //change the color of all the edittexts
        binder.etGoToBed.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etGoToBedONWeekends.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etSleephours.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etUsername.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etOldPassword.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etNewPassword.setHintTextColor(myAppTheme.activityHintColor(this))
        binder.etNewPassWordRepeat.setHintTextColor(myAppTheme.activityHintColor(this))

        //change the background of the arrow icon
        binder.backOutFromSettings.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        //change the color of the arrow
        binder.backOutFromSettings.setColorFilter(myAppTheme.activityIconColor(this))
        //set card view colors
        binder.button.setCardBackgroundColor(appTheme.activityThemeButtonColor(this))

        //syncStatusBarIconColors
        syncStatusBarIconColors(appTheme)
    }

    private fun syncStatusBarIconColors(theme: MyAppTheme) {
        ThemeManager.instance.syncStatusBarIconsColorWithBackground(
            this,
            theme.activityBackgroundColor(this)
        )
        ThemeManager.instance.syncNavigationBarButtonsColorWithBackground(
            this,
            theme.activityBackgroundColor(this)
        )
    }
    private fun updateButtonText() {
        if (ThemeManager.instance.getCurrentTheme()?.id() == NightTheme.ThemeId) {
            binder.buttonTextView.text = "Light"
        } else {
            binder.buttonTextView.text = "Night"
        }
    }

    private fun updateDatabaseTheme() {
        themeId = if (ThemeManager.instance.getCurrentTheme()?.id() == NightTheme.ThemeId) {
            "#FFFFFF"
        } else {
            "#000000"
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

                    val toast = Toast.makeText(
                        applicationContext,
                        "Your theme color has been updated to $themeId",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                    println("Theme color updated to $themeId")
                }

                override fun onError(error: ANError?) {
                    //TODO: handle error
                    if (error != null) {
                        println("Error: ${error.errorBody}")
                    }
                    println("Theme color update failed")
                }
            })


    }

    override fun getStartTheme(): AppTheme {
        return LightTheme()
    }

    fun closeSettings(view: View) {
        finish()
    }

    fun logOut(view: View) {
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.clear()
        editor.apply()
        startActivity(Intent(this@ProfileSettings, MainActivity::class.java))
        finish()
    }
}
