package com.mahesaiqbal.asynctask

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    lateinit var context: Context
    var questionsList: MutableList<Question> = ArrayList()
    var index =- 1
    var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this
        btn_next.isEnabled = false
        btn_next.alpha - 0.5.toFloat()
        getQuestions().execute()
    }

    internal inner class getQuestions : AsyncTask<Void, Void, String>() {

        lateinit var progressDialog: ProgressDialog
        var hasInternet = false

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Downloading Questions...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void?): String {
            if (isNetworkAvailable()) {
                hasInternet = true

                val client = OkHttpClient()
                val url = "https://script.googleusercontent.com/macros/echo?user_content_key=1tgBN-ES-vsiLin8Lggs7R094sUSEWlBY3Lv7yLt0KnrexUuaTvreORsTenxGH0HaPDQ0rUkXVqmkc903P_gQrpXCbi98gcsm5_BxDlH2jW0nuo2oDemN9CCS2h10ox_1xSncGQajx_ryfhECjZEnBg4Wj9So2Q_mI0_S0Bm21-AGmcRnplmVaRcxvVzvCi9cnQQJegsnVb9TgJzPufw35cdv3aNHr6K&lib=MKMzvVvSFmMa3ZLOyg67WCThf1WVRYg6Z"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                return response.body()?.string().toString()
            } else {
                return ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressDialog.dismiss()

            if (hasInternet) {
                try {
                    val resultArray = JSONArray(result)

                    for (i in 0..(resultArray.length()-1)) {
                        val currentObject = resultArray.getJSONObject(i)
                        val obj = Question()
                        obj.question = currentObject.getString("Question")
                        obj.option1 = currentObject.getString("Option1")
                        obj.option2 = currentObject.getString("Option2")
                        obj.option3 = currentObject.getString("Option3")
                        obj.option4 = currentObject.getString("Option4")
                        obj.answer = currentObject.getInt("Answer")
                        questionsList.add(obj)
                    }

                    if (index == -1) {
                        index++
                        tv_question.text = questionsList[index].question
                        rb_choice1.text = questionsList[index].option1
                        rb_choice2.text = questionsList[index].option2
                        rb_choice3.text = questionsList[index].option3
                        rb_choice4.text = questionsList[index].option4
                    }

                    btn_next.isEnabled = true
                    btn_next.alpha = 1.toFloat()
                    btn_next.setOnClickListener{
                        updateQuestions()
                    }
                } catch (e: JSONException) {

                }
            }
        }
    }

    fun updateQuestions() {
        val selected = rg_choice.checkedRadioButtonId
        if (selected == -1) {
            Toast.makeText(this, "Please select answer.", Toast.LENGTH_SHORT).show()
        }

        if (index < questionsList.size) {
            when (selected) {
                rb_choice1.id -> {
                    if (questionsList[index].answer == 1) {
                        score++
                    }
                }
                rb_choice2.id -> {
                    if (questionsList[index].answer == 2) {
                        score
                    }
                }
                rb_choice3.id -> {
                    if (questionsList[index].answer == 3) {
                        score++
                    }
                }
                rb_choice4.id -> {
                    if (questionsList[index].answer == 4) {
                        score++
                    }
                }
            }
            index++
            if (index < questionsList.size) {
                tv_question.text = questionsList[index].question
                rb_choice1.text = questionsList[index].option1
                rb_choice2.text = questionsList[index].option2
                rb_choice3.text = questionsList[index].option3
                rb_choice4.text = questionsList[index].option4
                rg_choice.clearCheck()

                if ((index + 1) == questionsList.size) {
                    btn_next.text = "Finish"
                }
            } else {
                val dialog = AlertDialog.Builder(context)
                dialog.setTitle("Your Score")
                dialog.setMessage("You have answered correct " + score + " out of " + questionsList.size + " questions correctly.")
                dialog.setPositiveButton("Close") {
                    dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss()
                    finish()
                }
                dialog.show()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}
