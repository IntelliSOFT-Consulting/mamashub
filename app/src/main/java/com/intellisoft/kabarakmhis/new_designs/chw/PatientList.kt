package com.intellisoft.kabarakmhis.new_designs.chw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.auth.Login
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientListViewModel
import com.intellisoft.kabarakmhis.helperclass.DbChwPatientData
import com.intellisoft.kabarakmhis.helperclass.DbPatientDetails
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.adapter.PatientsListAdapter
import com.intellisoft.kabarakmhis.new_designs.chw.adapter.ChwPatientsListAdapter
import com.intellisoft.kabarakmhis.new_designs.chw.viewmodel.ChwPatientListViewModel
import kotlinx.android.synthetic.main.activity_new_main.*
import kotlinx.android.synthetic.main.activity_patient_list.*
import kotlinx.android.synthetic.main.activity_patient_list.btnRegisterPatient
import kotlinx.android.synthetic.main.activity_patient_list.mySpinner
import kotlinx.android.synthetic.main.activity_patient_list.no_record
import kotlinx.android.synthetic.main.activity_patient_list.refreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PatientList : AppCompatActivity() , AdapterView.OnItemSelectedListener{

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var patientListViewModel: ChwPatientListViewModel
    private lateinit var fhirEngine: FhirEngine

    private val viewModel: MainActivityViewModel by viewModels()

    private var formatter = FormatterClass()

    var clientList = arrayOf("","All", "Referred to", "Referred from")
    private var spinnerClientValue  = clientList[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)

        title = "Client List"

        fhirEngine = FhirApplication.fhirEngine(this)

        patientListViewModel = ChwPatientListViewModel(application, fhirEngine)
        patientListViewModel = ViewModelProvider(this,
            ChwPatientListViewModel.FhirFormatterClassViewModelFactory
                (application, fhirEngine)
        )[ChwPatientListViewModel::class.java]

        recyclerView = findViewById(R.id.patient_list);
        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        search_round.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // collapse the view ?
                //menu.findItem(R.id.menu_search).collapseActionView();
                Log.e("queryText", query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // search goes here !!
                // listAdapter.getFilter().filter(query);

                val txtSearch = newText.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    if (!TextUtils.isEmpty(txtSearch)) {
                        patientListViewModel.searchPatientsByName(txtSearch)
                    } else {
                        val patientList = patientListViewModel.getPatientList()
                        showPatients(patientList)
                    }
                }

                return false
            }
        })

        refreshLayout.setOnRefreshListener{

            getData()
            refreshLayout.isRefreshing = false
        }


        btnRegisterPatient.setOnClickListener {

            startActivity(Intent(this@PatientList, CommunityHealthWorkerForm::class.java))

        }
    }

    override fun onStart() {
        super.onStart()

        initSpinner()
        getData()
    }

    private fun initSpinner() {

        val filterValue =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, clientList)
        filterValue.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner!!.adapter = filterValue
        mySpinner.onItemSelectedListener = this

    }

    override fun onItemSelected(arg0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
        when (arg0.id) {
            R.id.mySpinner -> { spinnerClientValue = mySpinner.selectedItem.toString() }
            else -> {}
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


    private fun getData() {
        patientListViewModel.liveSearchedPatients.observe(this) {
            showPatients(it)
        }

        formatter.saveSharedPreference(this@PatientList, "patientName", "")
        formatter.saveSharedPreference(this@PatientList, "identifier", "")

        viewModel.poll()
    }

    private fun showPatients(patientList: List<DbChwPatientData>) {

        FormatterClass().nukeEncounters(this@PatientList)

        CoroutineScope(Dispatchers.Main).launch {

            if (patientList.isEmpty()) {
                no_record.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                no_record.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val dateTimeFormatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val result = patientList.sortedByDescending {
                    (if (it.referralDate != "----/--/--"){

                        try {
                            LocalDate.parse(it.referralDate, dateTimeFormatter)
                        }catch (e: Exception){
                            LocalDate.parse(it.referralDate, dateTimeFormatter1)
                        }

                    }else{
                        it.name
                    }).toString()

                }
                //Get referral date


                val adapter = ChwPatientsListAdapter(result, this@PatientList)
                recyclerView.adapter = adapter
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.log_out -> {

                startActivity(Intent(this, Login::class.java))
                finish()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}