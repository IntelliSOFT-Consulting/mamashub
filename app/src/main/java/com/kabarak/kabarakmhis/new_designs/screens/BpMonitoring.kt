package com.kabarak.kabarakmhis.new_designs.screens

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.*
import com.kabarak.kabarakmhis.new_designs.physical_examination.tab_layout.SmartWatchReadingDataAdapter
import com.vivekkaushik.datepicker.DatePickerTimeline
import com.vivekkaushik.datepicker.OnDateSelectedListener
import kotlinx.android.synthetic.main.activity_bp_monitoring.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class BpMonitoring : AppCompatActivity() {

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine
    private val mainViewModel: MainActivityViewModel by viewModels()
    private val formatterClass = FormatterClass()

    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var recyclerView: RecyclerView

    private lateinit var datePickerTimeline: DatePickerTimeline

    //Get current year
    private val currentYear = LocalDate.now().year
    //Get current month
    private val currentMonth = LocalDate.now().monthValue - 2
    //Get current day
    private val currentDay = LocalDate.now().dayOfMonth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bp_monitoring)

        mainViewModel.poll()

        datePickerTimeline = findViewById(R.id.datePickerTimeline)

        patientId = formatterClass.retrieveSharedPreference(this, "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(this)

        patientDetailsViewModel = ViewModelProvider(
            this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(
                this.application,
                fhirEngine,
                patientId
            )
        )[PatientDetailsViewModel::class.java]

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        initDateSelector(currentYear, currentMonth, currentDay)




    }

    private fun initDateSelector(year: Int, month: Int, day: Int) {

        // Set a Start date as today's date

        datePickerTimeline.setInitialDate(year, month, day)
        // Set a date Selected Listener
        datePickerTimeline.setOnDateSelectedListener(object : OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {

                //Pass the date to the function
                getWatchReadings(year, month, day)

            }

            override fun onDisabledDateSelected(
                year: Int,
                month: Int,
                day: Int,
                dayOfWeek: Int,
                isDisabled: Boolean
            ) {
                // Do Something
            }
        })

    }


    override fun onStart() {
        super.onStart()

        getWatchReadings(currentYear, currentMonth, currentDay)

    }

    private fun getWatchReadings(year: Int, month: Int, day: Int) {

        val newDate = "$year-${month + 1}-$day"

        CoroutineScope(Dispatchers.IO).launch {

            val smartWatchReadingList = patientDetailsViewModel.getObservationFromEncounter(
                DbObservationValues.CLIENT_WEARABLE_RECORDING.name)
            if (smartWatchReadingList.isNotEmpty()){

                val datesAvailable = ArrayList<String>()

                //Get Observations
                val id = smartWatchReadingList[0].id
                val observationList = patientDetailsViewModel.getObservationsFromEncounter(id)
                val dbWatchDataList = ArrayList<DbWatchData>()
                observationList.groupBy { it.issued }.forEach { (issued, observationItems) ->
                    //Get the group date and convert to human readable date
                    val issuedDate = issued.toString()
                    datesAvailable.add(issuedDate)
                    val dbWatchDataValuesList = ArrayList<DbWatchDataValues>()
                    //Get the observations for the group date
                    observationItems.forEach {
                        val dbSmartWatchReading = DbWatchDataValues(
                            time = it.issuedTime.toString(),
                            text = it.text,
                            value = it.value)
                        dbWatchDataValuesList.add(dbSmartWatchReading)
                    }
                    dbWatchDataValuesList.sortBy { it.time }
                    val dbWatchData = DbWatchData(date = issuedDate, readings = dbWatchDataValuesList)
                    dbWatchDataList.add(dbWatchData)
                }
                //Sort the list by date
                dbWatchDataList.sortBy { it.date }
                val dbWatchTimeDataList = ArrayList<DbWatchTimeData>()
                //Check if the date is in the list and filter the list
                val filteredList = dbWatchDataList.filter { it.date == newDate }
                filteredList.forEach {
                    it.readings.groupBy { it.time }.forEach { (time, readings) ->

                        var systolic = ""
                        var diastolic = ""
                        var pulse = ""

                        readings.forEach { watch ->

                            val text = watch.text
                            val value = watch.value

                            if (text.contains("Systolic")){
                                systolic = value
                            }
                            if (text.contains("Diastolic")){
                                diastolic = value
                            }
                            if (text.contains("Heart")){
                                pulse = value
                            }

                        }

                        val readingsData = DbWatchRecord(systolic = systolic, diastolic = diastolic, pulse = pulse)

                        val dbWatchTimeData = DbWatchTimeData(time = time, readings = readingsData)
                        dbWatchTimeDataList.add(dbWatchTimeData)

                    }
                }

                datesAvailable.sortBy { it }

                CoroutineScope(Dispatchers.Main).launch {

                    //Convert the list to string
                    val datesAvailableString = datesAvailable.joinToString(separator = " , ")
                    tvDate.text = "Available Recordings: $datesAvailableString"

                    if (filteredList.isNotEmpty()){
                        no_record.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }else{
                        no_record.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                    val configurationListingAdapter = SmartWatchReadingDataAdapter(
                        dbWatchTimeDataList,this@BpMonitoring)
                    recyclerView.adapter = configurationListingAdapter
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