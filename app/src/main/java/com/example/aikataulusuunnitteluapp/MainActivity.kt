package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
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
import com.example.aikataulusuunnitteluapp.Data.SERVER_URL

// koodista n. 40% pelkkää AlertDialog -paskaa mitä ei voinut oikein optimoida
// ilman että appi rupes kaatuileen

class MainActivity : AppCompatActivity() {

    lateinit var groupDetails: Group
    lateinit var fragmentDetails: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //actionbar+back-button
        val actionbar = supportActionBar
        actionbar!!.title = "TimeCoach 1.0.0 "

        AndroidNetworking.initialize(applicationContext)
        val okHttpClient = OkHttpClient().newBuilder().build()
        AndroidNetworking.initialize(applicationContext, okHttpClient)
        // alustetaan okHttpClient

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnRegister = findViewById<Button>(R.id.btn_login2)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnLogin.setOnClickListener{

            println("${etUsername.text.toString()}, ${etPassword.text.toString()}")

            // okHttp -kirjastossa on Credentials -luokka, johon pistetään tunnukset meidän EditTexteistä,
            // jotka lähetetään Authorization headerin mukana API:iin
            AndroidNetworking.post("$SERVER_URL/login")
                .addHeaders("Authorization", Credentials.basic(etUsername.text.toString(), etPassword.text.toString()))
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(res: JSONObject) {
                        startActivity(Intent(this@MainActivity, Frontpage_activity::class.java))
                        finish()
                        // kun userID tulee takas eli login ok, lähtään etusivulle

                        println(res.get("idUser"))

                        var prefs: SharedPreferences = getSharedPreferences("myID", Context.MODE_PRIVATE)
                        var edit: SharedPreferences.Editor = prefs.edit()
                        try {
                            edit.putString("idUser", res.get("idUser").toString())
                            edit.commit()
                            println("User ID saved to SharedPreferences")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        // tallennetaan User ID SharedPreferences -olioon, jota voi käyttää muualla
                        // sama idea kuin LocalStorage Reactissa
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onError(error: ANError) {
                        println("Error: ${error.errorBody}")
                        if(error.errorBody == "Unauthorized") {
                            val builder = AlertDialog.Builder(this@MainActivity)
                            // kasataan AlertDialog -olio MainActivity -kontekstiin (ei ottanut tätä kontekstiksi)
                            builder.setTitle("Invalid username or password!")
                            builder.setMessage(error.errorBody)
                            // alerttiin title ja message
                            builder.setPositiveButton("OK"){dialogInterface, which ->
                                etUsername.setText("")
                                etPassword.setText("")
                            } // tyhjennetään napista kentät kun on väärä käyttäjätunnus/salasana
                            builder.show() // olio pitää vielä kutsua näkyviin
                        }
                    }
                })
        }

        btnRegister.setOnClickListener {

            val fragment = RegisterFragment()
            showRegisterFragment(fragment)

        }
    }

    private fun showRegisterFragment(fragment: RegisterFragment){

        groupDetails = findViewById(R.id.groupDetails)
        groupDetails.visibility = View.GONE // Change visibility

        fragmentDetails = findViewById(R.id.fragmentLayoutGroup)
        fragmentDetails.visibility = View.VISIBLE // Change visibility

        val fragmentmanager = supportFragmentManager.beginTransaction()
        fragmentmanager.replace(R.id.frameLayout, fragment)
        fragmentmanager.commit()
    }
}