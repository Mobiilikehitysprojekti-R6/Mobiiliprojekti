package com.example.aikataulusuunnitteluapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import okhttp3.OkHttpClient
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
        // napit, edittextit

        btnLogin.setOnClickListener{
            startActivity(Intent(this@MainActivity, Frontpage_activity::class.java))
        }

        btnRegister.setOnClickListener {

            val jsonObject = JSONObject()
            try {
                jsonObject.put("Username", etUsername.text)
                jsonObject.put("Password", etPassword.text)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            AndroidNetworking.post("http://PISTÄ TÄHÄN OMAN KONEEN IP:3000/register")
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