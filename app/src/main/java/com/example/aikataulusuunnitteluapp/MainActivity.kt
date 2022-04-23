package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import com.example.aikataulusuunnitteluapp.data.SERVER_URL

// In this part of the Code there mostly code involving the Alert Dialog that is not possible to optimise any further.

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initializing the okHttpClient
        AndroidNetworking.initialize(applicationContext)
        val okHttpClient = OkHttpClient().newBuilder().build()
        AndroidNetworking.initialize(applicationContext, okHttpClient)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_login2)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnLogin.setOnClickListener{

            println("${etUsername.text}, ${etPassword.text}")


            // The login credentials gotten from the EditText is put into the okHttp-library's Credentials -class.
            // From  where the are sent forward to the API inside the Authorization header.

            AndroidNetworking.post("$SERVER_URL/login")
                .addHeaders("Authorization", Credentials.basic(etUsername.text.toString(), etPassword.text.toString()))
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    // When the userID comes back the login is Accepted and the FrontPage opens.
                    override fun onResponse(res: JSONObject) {
                        startActivity(Intent(this@MainActivity, Frontpage::class.java))
                        finish()

                        println(res.get("idUser"))
                        // Save the UserID into the SharedPreferences so it can be used while user is logged in, in all of the activities.
                        val prefs: SharedPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
                        val edit: SharedPreferences.Editor = prefs.edit()
                        try {
                            edit.putString("idUser", res.get("idUser").toString())
                            edit.putString("premiumStatus", res.get("premiumAccount").toString())
                            edit.putString("username", res.get("username").toString())
                            edit.apply()
                            println("User ID saved to SharedPreferences")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    // Build an AlertDialog object for MainActivity
                    @SuppressLint("SetTextI18n")
                    override fun onError(error: ANError) {
                        println("Error: ${error.errorBody}")
                        if(error.errorCode == 401) {
                            val builder = AlertDialog.Builder(this@MainActivity)

                            // The title and message for the alert.
                            builder.setTitle("Invalid username or password!")
                            builder.setMessage(error.errorBody)
                            // When the OK button in the dialog is tapped the login credentials from the edit text empties.
                            builder.setPositiveButton("OK"){dialogInterface, which ->
                                etUsername.setText("")
                                etPassword.setText("")
                            }
                            builder.show() // display the alertDialog
                        }
                    }
                })
        }

        btnRegister.setOnClickListener {

            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))

        }
    }
}