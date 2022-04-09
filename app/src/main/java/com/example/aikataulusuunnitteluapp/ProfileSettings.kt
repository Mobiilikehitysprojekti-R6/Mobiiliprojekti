package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import org.json.JSONException
import org.json.JSONObject

class ProfileSettings : AppCompatActivity() {

    lateinit var preferences: SharedPreferences
    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        var updatePasswordButton: Button = findViewById(R.id.btn_update_password)
        var oldPasswordEditText: EditText = findViewById(R.id.et_oldPassword)
        var newPasswordEditText: EditText = findViewById(R.id.et_newPassword)
        var repeatPasswordEditText: EditText = findViewById(R.id.et_newPassWord_repeat)

        //retrieve userid with sharedPreferences
        preferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
        userId = preferences.getString("idUser", "").toString()
        println("User ID from SharedPreferences in ProfileSettings: $userId")


        updatePasswordButton.setOnClickListener {

            if (newPasswordEditText.text.toString() == repeatPasswordEditText.text.toString()) {
                println("new passwords match")

                val jsonObject = JSONObject()
                try {
                    jsonObject.put("idUser", userId)
                    jsonObject.put("password", oldPasswordEditText.text.toString())
                    jsonObject.put("newPassword", newPasswordEditText.text.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                AndroidNetworking.put("$SERVER_URL/settings")
                    .addJSONObjectBody(jsonObject)
                    //TODO: Change the priority of this request if there are going to be more requests in this file
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject?) {
                            val toast = Toast.makeText(
                                applicationContext,
                                "Your password has been updated",
                                Toast.LENGTH_SHORT
                            )
                            toast.show()
                            println("password updated")
                        }

                        override fun onError(error: ANError?) {
                            //TODO: handle error
                            if (error != null) {
                                println("Error: ${error.errorBody}")
                            }
                            println("password update failed")
                        }
                    })
            }
        }
    }

    fun closeSettings(view: View) {
        finish()
    }
}
