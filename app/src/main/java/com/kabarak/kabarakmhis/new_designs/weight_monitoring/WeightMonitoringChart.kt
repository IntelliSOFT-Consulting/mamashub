package com.kabarak.kabarakmhis.new_designs.weight_monitoring

import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbWeightChart
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.kabarak.kabarakmhis.new_designs.data_class.DbResourceViews
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import com.kabarak.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_weight_monitoring_chart.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WeightMonitoringChart : AppCompatActivity() {

    private val formatter = FormatterClass()
    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var chart: LineChart
    private lateinit var kabarakViewModel: KabarakViewModel
    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_monitoring_chart)

        title = "Weight Monitoring Chart"

        chart = findViewById(R.id.chart)

        kabarakViewModel = KabarakViewModel(this.applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]


    }


    private fun  setData(observationList: ArrayList<DbWeightChart>) {

        CoroutineScope(Dispatchers.Main).launch {
            tvYaxis.visibility = View.VISIBLE
            tvXaxis.visibility = View.VISIBLE
        }

        observationList.sortBy { it.gestation }

        val weightMonitorList = ArrayList<Entry>()

        for(item in observationList){

            val weight = item.weight
            val gestation = item.gestation

            val entry = Entry(gestation, weight)
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

            //Get encounters for patient
            val encounterList = patientDetailsViewModel.getObservationFromEncounter(DbResourceViews.PHYSICAL_EXAMINATION.name)

            val weightList = ArrayList<String>()
            val gestationList = ArrayList<String>()

            encounterList.forEach { encounter ->

                val encounterId = encounter.id

                //Get observations for encounter
                val observationList = patientDetailsViewModel.getObservationsFromEncounter(encounterId)
                observationList.forEach {

                    val code = it.code
                    val value = it.value

                    if (code == "77386006"){
                        //Gestation in weeks
                        val valueReversed = value.reversed()
                        val valueGestation = valueReversed.substring(5, valueReversed.length)
                        val gestation = valueGestation.reversed()

                        gestationList.add(gestation)
                    }
                    if (code == "726527001"){
                        //Weight in kgs
                        weightList.add(value)
                    }


                }

            }

            val dbWeightChartList = ArrayList<DbWeightChart>()

            if (weightList.size == gestationList.size){

                for (i in 0 until weightList.size){

                    val weightFloat = weightList[i].toFloat()
                    val gestationFloat = gestationList[i].toFloat()

                    val dbWeightChart = DbWeightChart(gestationFloat, weightFloat)
                    dbWeightChartList.add(dbWeightChart)
                }


            }

            setData(dbWeightChartList)

            getPersonalData()

        }




    }

    private fun getPersonalData() {

        val identifier = formatter.retrieveSharedPreference(this, "identifier")
        val patientName = formatter.retrieveSharedPreference(this, "patientName")
        val dob = formatter.retrieveSharedPreference(this, "dob")

        if (identifier != null) tvAncId.text = identifier
        if (patientName != null) tvPatientName.text = patientName
        if (dob != null){
            val age = "${formatter.calculateAge(dob)} years"
            tvAge.text = age
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