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
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import com.example.aikataulusuunnitteluapp.Data.SERVER_URL

// koodista n. 40% pelkkää AlertDialog -paskaa mitä ei voinut oikein optimoida
// ilman että appi rupes kaatuileen

class MainActivity : AppCompatActivity() {

    var failedAPICalls: Int = 0

    fun failedTimes() {
        failedAPICalls++

        // tein tällaisen funktion seuraamaan käyttäjän burgerointia
        // ainakun tulee network error niin tämä funktio kutsutaan
        // tehdään sitten joku 10s odotteluaika kun on tarpeeksi monta network erroria saanut kasaan
        // ei todellakaan pakollinen

        println("Failed API Call count: $failedAPICalls")

        if(failedAPICalls >= 5) {
            // TODO : jos käyttäjällä >= 5 virheellistä api kutsua, pistää 10s odotteluajan ja resettaa laskimen
        }
    }

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

        // KAIKKI alert -liittyvät dialog interface ja which voi nimetä _ _, koska niitä ei käytetä,
        // jos haluaa

        btnLogin.setOnClickListener{

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
                        if(error.errorBody.toString() == "Unauthorized") {
                            val builder = AlertDialog.Builder(this@MainActivity)
                            // kasataan AlertDialog -olio MainActivity -kontekstiin (ei ottanut tätä kontekstiksi)
                            builder.setTitle("Invalid username or password!")
                            builder.setMessage(error.errorBody.toString())
                            // alerttiin title ja message
                            builder.setPositiveButton("OK"){dialogInterface, which ->
                                failedTimes()
                                etUsername.setText("")
                                etPassword.setText("")
                            } // tyhjennetään napista kentät kun on väärä käyttäjätunnus/salasana
                            builder.show() // olio pitää vielä kutsua näkyviin
                        }
                    }
                })
        }

        btnRegister.setOnClickListener {


            val jsonObject = JSONObject()
            try {
                jsonObject.put("username", etUsername.text.toString())
                jsonObject.put("password", etPassword.text.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            } // lähetetään käyttäjätunnus + salasana rekisteröintiä varten JSON-objektina

            AndroidNetworking.post("$SERVER_URL/register")
                .addJSONObjectBody(jsonObject)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(res: String?) {
                        println("Got response from API: $res")
                        if (res.toString() == "Registered") {

                            val builder = AlertDialog.Builder(this@MainActivity)
                            builder.setTitle("Account registered!")
                            builder.setMessage("Your account was successfully registered, please login")
                            builder.setPositiveButton("OK"){dialogInterface, which ->
                                failedAPICalls = 0
                            }
                            builder.show()
                        }

                        if (res.toString() == "Username is already taken") {

                            val builder = AlertDialog.Builder(this@MainActivity)
                            builder.setTitle("Username is taken")
                            builder.setMessage("Username has been taken!")
                            builder.setPositiveButton("OK"){dialogInterface, which ->

                                failedTimes()
                                etUsername.setText("")
                                etPassword.setText("")
                            }
                            builder.show()
                        }
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onError(error: ANError) {
                        println("Error: ${error.errorBody}")

                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle("Network error!")
                        if (error.errorBody == null) {
                            builder.setMessage("Unknown error")
                        } else {
                            builder.setMessage(error.errorBody.toString())
                        } // alerttia ei tule jos errorBody on null
                        // errorBody on null esim. jos API-yhteyttä ei saada
                        // siihen tehtiin tarkistus että alert tulee jokatapauxessa

                        builder.setPositiveButton("OK"){dialogInterface, which ->
                            failedTimes()
                        }

                        builder.setNegativeButton("Retry"){dialogInterface, which ->
                            btnRegister.performClick()
                        }

                        builder.show()
                    }
                })
        }
    }
}