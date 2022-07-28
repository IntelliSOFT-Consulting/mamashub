package com.intellisoft.kabarakmhis.new_designs.weight_monitoring

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_weight_monitoring_chart.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WeightMonitoringChart : AppCompatActivity() {

    private val formatter = FormatterClass()
    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var chart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_monitoring_chart)

        title = "Weight Monitoring Chart"

        chart = findViewById(R.id.chart)


    }


    private fun  setData(observationList: ArrayList<DbObserveValue>) {

        CoroutineScope(Dispatchers.Main).launch {
            tvYaxis.visibility = View.VISIBLE
            tvXaxis.visibility = View.VISIBLE
        }


        val weightMonitorList = ArrayList<Entry>()

        for(item in observationList){

            val weight = item.title.toFloat()
            val gestation = item.value.toFloat()

            val entry = Entry(weight, gestation)
            weightMonitorList.add(entry)

        }

        val lineDataSet1 = LineDataSet(weightMonitorList, "Weight Monitoring Chart")
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet1)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.textColor = Color.WHITE
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        xAxis.textColor = Color.rgb(255, 192, 56)
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f // one hour


        xAxis.valueFormatter = DayAxisValueFormatter(chart)

        val lineData = LineData(dataSets)
        chart.data = lineData
        chart.invalidate()

    }

    class DayAxisValueFormatter(chart: LineChart) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "$value weeks"
        }
    }


    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.IO).launch {

            val encounterId = formatter.retrieveSharedPreference(this@WeightMonitoringChart, DbResourceViews.PHYSICAL_EXAMINATION.name)

            if (encounterId != null) {
                val observationList = retrofitCallsFhir.getEncounterDetails(this@WeightMonitoringChart, encounterId, DbResourceViews.WEIGHT_MONITORING.name)
                if (observationList.isNotEmpty()){

                    setData(observationList)

                    Log.e("----- ", observationList.toString())
                }


            }




        }




    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {

                startActivity(Intent(this, PatientProfile::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    
}