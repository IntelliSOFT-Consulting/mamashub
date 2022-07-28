package com.intellisoft.kabarakmhis.new_designs.matenal_serology

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.network_request.requests.RetrofitCallsFhir
import com.intellisoft.kabarakmhis.new_designs.data_class.DbObserveValue
import com.intellisoft.kabarakmhis.new_designs.data_class.DbResourceViews
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile
import kotlinx.android.synthetic.main.activity_malaria_prophylaxis.*
import kotlinx.android.synthetic.main.activity_maternal_serology.*
import kotlinx.android.synthetic.main.activity_maternal_serology.navigation
import kotlinx.android.synthetic.main.activity_maternal_serology.tvDate
import kotlinx.android.synthetic.main.fragment_details.view.*
import kotlinx.android.synthetic.main.fragment_medical.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import java.util.*
import kotlin.collections.ArrayList

class MaternalSerology : AppCompatActivity() {

    private val formatter = FormatterClass()

    private val retrofitCallsFhir = RetrofitCallsFhir()

    private lateinit var calendar : Calendar
    private var year = 0
    private  var month = 0
    private  var day = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maternal_serology)

        title = "Repeat Maternal Serology"
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        radioGrpRepeatSerology.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Yes") {
                    changeVisibility(linearRepeatYes, true)
                    changeVisibility(linearRepeatNo, false)
                } else {
                    changeVisibility(linearRepeatNo, true)
                    changeVisibility(linearRepeatYes, false)
                    changeVisibility(linearNoReactive, false)
                    changeVisibility(linearReactive, false)
                    radioGrpTestResults.clearCheck()
                }

            }
        }
        radioGrpTestResults.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup?.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton?.isChecked
            if (isChecked == true) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "R") {
                    changeVisibility(linearReactive, true)
                    changeVisibility(linearNoReactive, false)
                } else {
                    changeVisibility(linearNoReactive, true)
                    changeVisibility(linearReactive, false)
                }

            }
        }

        tvNextVisit.setOnClickListener { createDialog(999) }
        tvNoNextAppointment.setOnClickListener { createDialog(998) }
        tvDate.setOnClickListener { createDialog(997) }

        handleNavigation()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createDialog(id: Int) {
        // TODO Auto-generated method stub

        when (id) {
            999 -> {
                val datePickerDialog = DatePickerDialog(this,
                    myDateListener, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            998 -> {
                val datePickerDialog = DatePickerDialog(this,
                    myDateListener1, year, month, day)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()

            }
            997 -> {
                val datePickerDialog = DatePickerDialog(this,
                    myDateListener2, year, month, day)
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()

            }

            else -> null
        }


    }
    @RequiresApi(Build.VERSION_CODES.O)
    private val myDateListener =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            tvNextVisit.text = date



        }
    @RequiresApi(Build.VERSION_CODES.O)
    private val myDateListener1 =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            tvNoNextAppointment.text = date



        }

    @RequiresApi(Build.VERSION_CODES.O)
    private val myDateListener2 =
        DatePickerDialog.OnDateSetListener { arg0, arg1, arg2, arg3 -> // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day
            val date = showDate(arg1, arg2 + 1, arg3)
            tvDate.text = date



        }

    private fun showDate(year: Int, month: Int, day: Int) :String{

        var dayDate = day.toString()
        if (day.toString().length == 1){
            dayDate = "0$day"
        }
        var monthDate = month.toString()
        if (month.toString().length == 1){
            monthDate = "0$monthDate"
        }

        val date = StringBuilder().append(year).append("-")
            .append(monthDate).append("-").append(dayDate)

        return date.toString()

    }

    private fun handleNavigation() {

        navigation.btnNext.text = "Save"
        navigation.btnPrevious.text = "Cancel"

        navigation.btnNext.setOnClickListener { saveData() }
        navigation.btnPrevious.setOnClickListener { onBackPressed() }

    }

    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
        }

    }

    private fun saveData() {

        val repeatSerology = formatter.getRadioText(radioGrpRepeatSerology)
        if (repeatSerology != ""){

            val birthPlanList = ArrayList<DbObserveValue>()

            if (linearRepeatNo.visibility == View.VISIBLE){
               val nextAppointment = tvNoNextAppointment.text.toString()
                val valueName = DbObserveValue("Date of Next Appointment", nextAppointment)
                birthPlanList.add(valueName)
            }


            if (linearRepeatYes.visibility == View.VISIBLE){
                val testDoneDate = tvDate.text.toString()
                val valueName = DbObserveValue("Date Test was done", testDoneDate)
                birthPlanList.add(valueName)

                val radioGrpTestResults = formatter.getRadioText(radioGrpTestResults)
                if (radioGrpTestResults != ""){

                    if (linearReactive.visibility == View.VISIBLE){
                        val pmtctClinic = etPMTCTClinic.text.toString()
                        val partnerTested = etTestPartner.text.toString()

                        if (!TextUtils.isEmpty(pmtctClinic) && !TextUtils.isEmpty(partnerTested)){

                            val valueName1 = DbObserveValue("Refer PMTCT Clinic", pmtctClinic)
                            val valueName2 = DbObserveValue("Partner Test", partnerTested)

                            birthPlanList.addAll(listOf(valueName1, valueName2))

                        }else{
                            Toast.makeText(this, "Please fill all records", Toast.LENGTH_SHORT).show()
                        }

                    }
                    if (linearNoReactive.visibility == View.VISIBLE){
                        val bookSerology = etRepeatSerology.text.toString()
                        val breastFeeding = etContinueTest.text.toString()
                        val nextVisit = tvNextVisit.text.toString()

                        if (!TextUtils.isEmpty(bookSerology) && !TextUtils.isEmpty(breastFeeding) && !TextUtils.isEmpty(nextVisit)){

                            val valueName1 = DbObserveValue("Book Serology Test", bookSerology)
                            val valueName2 = DbObserveValue("Complete Breastfeeding Cessation", breastFeeding)
                            val valueName3 = DbObserveValue("Next appointment", nextVisit)

                            birthPlanList.addAll(listOf(valueName1, valueName2, valueName3))

                        }else{
                            Toast.makeText(this, "Please fill all records", Toast.LENGTH_SHORT).show()
                        }

                    }

                }

            }



            val dbObservationValue = formatter.createObservation(birthPlanList,
                DbResourceViews.MATERNAL_SEROLOGY.name)

            retrofitCallsFhir.createFhirEncounter(this, dbObservationValue,
                DbResourceViews.MATERNAL_SEROLOGY.name)

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