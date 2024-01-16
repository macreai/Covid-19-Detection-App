package com.apicta.wraphumic

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.apicta.wraphumic.databinding.ActivityMainBinding
import com.apicta.wraphumic.util.TFLiteHelper
import id.co.telkom.iot.AntaresHTTPAPI
import id.co.telkom.iot.AntaresResponse
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), AntaresHTTPAPI.OnResponseListener {

    private var antaresAPIHTTP: AntaresHTTPAPI? = null
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        antaresAPIHTTP = AntaresHTTPAPI()
        antaresAPIHTTP!!.addListener(this)

        antaresAPIHTTP!!.getLatestDataofDevice("a455797898623473:11bedb08b59e62a5", "HUMIC", "HUMIC1")
    }

    override fun onResponse(response: AntaresResponse?) {
        binding.btnRefresh.setOnClickListener {
            Log.d(TAG, "onResponse: ${response?.requestCode}")
            if (response?.requestCode == 0){
                try {
                    val body: JSONObject = JSONObject(response?.body)
                    val dataDevice = body.getJSONObject("m2m:cin").getString("con")

                    val jsonObject = JSONObject(dataDevice)
                    val temperature = jsonObject.getDouble("temperature")
                    val heartRate = jsonObject.getInt("heartRate")
                    val spo2 = jsonObject.getInt("SPO2")

                    Log.d(TAG, "data: $temperature , $heartRate, $spo2")

                    binding.tvThermo.text = temperature.toString()
                    binding.tvHeart.text = heartRate.toString()
                    binding.tvSpo2.text = spo2.toString()
//
//                    binding.tvThermo.text = "29.6875"
//                    binding.tvHeart.text = "35"
//                    binding.tvSpo2.text = "97"

                    val tfLiteHelper = TFLiteHelper(this)

                val inputValues = floatArrayOf(spo2.toFloat(), heartRate.toFloat(), ((temperature * 9 / 5) + 32).toFloat())
//                    val inputValues = floatArrayOf(94F, 100F, 103F)
                    val results = tfLiteHelper.runInference(inputValues)

                    binding.tvResult.text = if (results.isNotEmpty() && results[0] < 0.5) {
                        "Negative Covid"
                    } else {
                        "Positive Covid"
                    }

                    for (result in results) {
                        Log.d(TAG, "result: $result")
                    }
//                    for (result in results){
//                        Log.d(TAG, "result: $result")
//                        if (result < 0.5){
//                            binding.tvResult.text = "Negative Covid"
//                        } else {
//                            binding.tvResult.text = "Positive Covid"
//                        }
//                    }
                } catch (e: JSONException){
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        private const val TAG = "Main Activity"
    }
}