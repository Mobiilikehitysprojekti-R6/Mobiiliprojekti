package com.example.aikataulusuunnitteluapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AndroidNetworking.initialize(applicationContext)
        val okHttpClient = OkHttpClient().newBuilder().build()
        AndroidNetworking.initialize(applicationContext, okHttpClient)
        // alustetaan okHttpClient

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_login2)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnLogin.setOnClickListener{

            Credentials.basic(etUsername.text.toString(), etPassword.text.toString())

            AndroidNetworking.post("http://192.168.1.150:3000/login")
                .addHeaders("Authorization", Credentials.basic(etUsername.text.toString(), etPassword.text.toString()))
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(res: String?) {
                        println(res)
                        startActivity(Intent(this@MainActivity, Frontpage_activity::class.java))
                    }

                    override fun onError(error: ANError) {
                        println(error)
                        // TODO : if error -> alert with retry & ok buttons
                    }
                })
        }

        btnRegister.setOnClickListener {

            val jsonObject = JSONObject()
            try {
                jsonObject.put("Username", etUsername.text.toString())
                jsonObject.put("Password", etPassword.text.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            AndroidNetworking.post("http://192.168.1.150:3000/register")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(res: String?) {
                        println("Got response from API: $res")
                    }

                    override fun onError(error: ANError) {
                        println(error)
                        // TODO : if error -> alert with retry & ok buttons
                    }
                })
        }
    }
}