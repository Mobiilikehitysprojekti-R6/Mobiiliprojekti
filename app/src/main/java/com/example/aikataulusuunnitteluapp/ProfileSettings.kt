package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
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


class ProfileSettings : ThemeActivity() {

    private lateinit var binder: ActivityProfileSettingsBinding
    lateinit var myIdPreferences: SharedPreferences
    lateinit var themePreferences: SharedPreferences
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

        //retrieve userid with sharedPreferences
        myIdPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = myIdPreferences.getString("idUser", "").toString()
        println("User ID from SharedPreferences in ProfileSettings: $userId")

        //retrieve theme with sharedPreferences
        themePreferences = getSharedPreferences("myTheme", Context.MODE_PRIVATE)
        themeId = themePreferences.getString("myTheme","").toString()
        println("Theme from SharedPreferences in ProfileSettings: $themeId")

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
            //TODO: create a new async function to disable the button for a few seconds
            ThemeManager.instance.changeTheme(NightTheme(), it)
            }
            updateButtonText()
            updateDatabaseTheme()
        }

        binder.btnUpdatePassword.setOnClickListener {

            if (binder.etNewPassword.text.toString() == binder.etNewPassWordRepeat.text.toString()
                && binder.etNewPassword.text.length > 4
                && binder.etNewPassWordRepeat.text.length > 4
                && binder.etOldPassword.text.length > 4
            ) {
                println("new passwords match and fields are filled")

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
            } else {
                val toast = Toast.makeText(
                    applicationContext,
                    "Error in the password fields.",
                    Toast.LENGTH_LONG
                )
                toast.show()
            }
        }
    }



    //this function changes the colors of the different views
    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here
        val myAppTheme = appTheme as MyAppTheme

        // set background color
        binder.root.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        //change the color of the buttons
        binder.btnEnableNotifications.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        binder.btnSaveUserSettings.setBackgroundColor((myAppTheme.activityThemeButtonColor(this)))
        binder.btnOrderPremium.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        binder.btnLogOut.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        binder.btnUpdatePassword.setBackgroundColor(myAppTheme.activityThemeButtonColor(this))
        //change the color of the text views
        binder.tvSettingsText.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvChooseATheme.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvEnableNotifications.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvGoToBedAt.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvWhatAboutWeekends.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvIwantToSleepFor.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvProfile.setTextColor(myAppTheme.activityTextColor(this))
        binder.tvNewPassword.setTextColor(myAppTheme.activityTextColor(this))
        //change the color of all the edit texts
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
        binder.btnChangeTheme.setCardBackgroundColor(appTheme.activityThemeButtonColor(this))
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

                    val toast = Toast.makeText(
                        applicationContext,
                        "Your theme color has been updated to $themeId",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()

                    themePreferences = getSharedPreferences("myTheme", Context.MODE_PRIVATE)
                    val edit: SharedPreferences.Editor = themePreferences.edit()
                    try {
                        edit.putString("myTheme", themeId)
                        edit.commit()
                        println("Theme saved to SharedPreferences = $themeId")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
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
    //set default theme
    override fun getStartTheme(): AppTheme {

        themePreferences = getSharedPreferences("myTheme", Context.MODE_PRIVATE)
        var startTheme = themePreferences.getString("myTheme","").toString()
        println("This is the theme2 in getstarttheme $startTheme")

        //change the theme to match users theme

        if(startTheme.isNotEmpty()) {
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

    fun logOut(view: View) {
        var editor: SharedPreferences.Editor = myIdPreferences.edit()
        editor.clear()
        editor.apply()
        editor = themePreferences.edit()
        editor.clear()
        editor.apply()
        startActivity(Intent(this@ProfileSettings, MainActivity::class.java))
        finish()
    }
}


