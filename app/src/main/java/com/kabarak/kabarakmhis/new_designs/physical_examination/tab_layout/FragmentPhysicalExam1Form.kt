package com.kabarak.kabarakmhis.new_designs.physical_examination.tab_layout

import android.app.Application
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.fhir.FhirEngine
import com.kabarak.kabarakmhis.R
import com.kabarak.kabarakmhis.fhir.FhirApplication
import com.kabarak.kabarakmhis.fhir.viewmodels.PatientDetailsViewModel
import com.kabarak.kabarakmhis.helperclass.DbObservationLabel
import com.kabarak.kabarakmhis.helperclass.DbObservationValues
import com.kabarak.kabarakmhis.helperclass.DbSummaryTitle
import com.kabarak.kabarakmhis.helperclass.FormatterClass
import com.kabarak.kabarakmhis.new_designs.data_class.*
import com.kabarak.kabarakmhis.new_designs.physical_examination.FragmentPhysicalExam2
import com.kabarak.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_physical_exam_1_form.view.*
import kotlinx.android.synthetic.main.navigation.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentPhysicalExam1Form : Fragment() {

    private val formatter = FormatterClass()

    private var observationList = mutableMapOf<String, DbObservationLabel>()
    private lateinit var kabarakViewModel: KabarakViewModel

    private lateinit var rootView: View

    private lateinit var patientDetailsViewModel: PatientDetailsViewModel
    private lateinit var patientId: String
    private lateinit var fhirEngine: FhirEngine

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_physical_exam_1_form, container, false)

        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        patientId = formatter.retrieveSharedPreference(requireContext(), "patientId").toString()
        fhirEngine = FhirApplication.fhirEngine(requireContext())

        patientDetailsViewModel = ViewModelProvider(this,
            PatientDetailsViewModel.PatientDetailsViewModelFactory(requireContext().applicationContext as Application,fhirEngine, patientId)
        )[PatientDetailsViewModel::class.java]
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        rootView.radioGrpGeneralExam.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearGeneralExam, true)
                } else {
                    changeVisibility(rootView.linearGeneralExam, false)
                }

            }
        }
        rootView.radioGrpCVS.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearCvs, true)
                } else {
                    changeVisibility(rootView.linearCvs, false)
                }

            }
        }
        rootView.radioGrpRespiratory.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearResp, true)
                } else {
                    changeVisibility(rootView.linearResp, false)
                }

            }
        }
        rootView.radioGrpRespiratory.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                val checkedBtn = checkedRadioButton.text.toString()
                if (checkedBtn == "Abnormal") {
                    changeVisibility(rootView.linearResp, true)
                } else {
                    changeVisibility(rootView.linearResp, false)
                }

            }
        }
        rootView.radioGrpBreasts.setOnCheckedChangeListener { radioGroup, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<RadioButton>(checkedId)
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                when (checkedRadioButton.text.toString()) {
                    "Abnormal" -> {
                        changeVisibility(rootView.linearAbnormal, true)
                        changeVisibility(rootView.linearNormal, false)
                    }
                    "Normal" -> {
                        changeVisibility(rootView.linearNormal, true)
                        changeVisibility(rootView.linearAbnormal, false)
                    }
                    else -> {
                        changeVisibility(rootView.linearNormal, false)
                        changeVisibility(rootView.linearAbnormal, false)

                    }
                }

            }
        }

        handleNavigation()

        rootView.etSystolicBp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etSystolicBp.text.toString()
                try {
                    if (!TextUtils.isEmpty(value)){
                        validateSystolicBloodPressure(rootView.etSystolicBp, value.toInt())
                    }
                }catch (e: NumberFormatException){
                    e.printStackTrace()
                }


            }

        })
        rootView.etDiastolicBp.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etDiastolicBp.text.toString()

                try {
                    if (!TextUtils.isEmpty(value)){
                        validateDiastolicBloodPressure(rootView.etDiastolicBp, value.toInt())
                    }
                }catch (e: NumberFormatException){
                    e.printStackTrace()
                }

            }

        })
        rootView.etPulseRate.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val value = rootView.etPulseRate.text.toString()

                try {

                    if (!TextUtils.isEmpty(value)){
                        validatePulseRateBloodPressure(rootView.etPulseRate, value.toInt())
                    }

                }catch (e: NumberFormatException){
                    e.printStackTrace()
                }

            }

        })

        return rootView
    }


    private fun validateSystolicBloodPressure(editText: EditText, value: Int){

        if (value <= 70){
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }else if (value <= 80){
            editText.setBackgroundColor(resources.getColor(R.color.orange))
        }else if (value <= 110){
            editText.setBackgroundColor(resources.getColor(R.color.yellow))
        }else if (value <= 130)
            editText.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
        else {
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }



    }
    private fun validateDiastolicBloodPressure(editText: EditText, value: Int){

        if (value <= 60){
            editText.setBackgroundColor(resources.getColor(R.color.yellow))
        }else if (value <= 90){
            editText.setBackgroundColor(resources.getColor(R.color.low_risk))
        }else {
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }

    }
    private fun validatePulseRateBloodPressure(editText: EditText, value: Int){

        if (value < 60){
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }else if (value <= 100){
            editText.setBackgroundColor(resources.getColor(R.color.low_risk))
        }else {
            editText.setBackgroundColor(resources.getColor(R.color.moderate_risk))
        }

    }

    private fun handleNavigation() {

        rootView.navigation.btnNext.text = "Next"
        rootView.navigation.btnPrevious.text = "Cancel"

        rootView.navigation.btnNext.setOnClickListener { saveData() }
        rootView.navigation.btnPrevious.setOnClickListener { activity?.onBackPressed() }

    }
    private fun saveData() {

        val errorList = ArrayList<String>()
        val dbDataList = ArrayList<DbDataList>()

        val generalExam  = formatter.getRadioText(rootView.radioGrpGeneralExam)
        if (generalExam != ""){

            addData("General Examination",generalExam, DbObservationValues.GENERAL_EXAMINATION.name)

            if(rootView.linearGeneralExam.visibility == View.VISIBLE){

                val abnormalityValue = rootView.etAbnomality.text.toString()
                if(!TextUtils.isEmpty(abnormalityValue)){
                    addData("If abnormal, specify",abnormalityValue, DbObservationValues.ABNORMAL_GENERAL_EXAMINATION.name)
                }else{
                    errorList.add("If abnormal, please specify")
                }

            }

        }else{
            errorList.add("General Exam is required")
        }

        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.A_PHYSICAL_EXAMINATION.name, DbResourceType.Observation.name, label)
            dbDataList.add(data)

        }
        observationList.clear()

        val systolicBp = rootView.etSystolicBp.text.toString()
        val diastolicBp = rootView.etDiastolicBp.text.toString()
        val pulseRate = rootView.etPulseRate.text.toString()
        val temparature = rootView.etTemperature.text.toString()

        val cvsText = formatter.getRadioText(rootView.radioGrpCVS)
        if (cvsText != "") {
            addData("CVS",cvsText, DbObservationValues.CVS.name)

            if(rootView.linearCvs.visibility == View.VISIBLE){
                val text = rootView.etCvsAbnormal.text.toString()
                if(!TextUtils.isEmpty(text)){
                    addData("If abnormal CVS, specify",text, DbObservationValues.ABNORMAL_CVS.name)
                }else{
                    errorList.add("If abnormal CVS, please specify")
                }

            }

        } else{
            errorList.add("Please select CVS value")
        }

        if (!TextUtils.isEmpty(systolicBp)){
            addData("Systolic Bp",systolicBp, DbObservationValues.SYSTOLIC_BP.name)
        }else{
            errorList.add("Systolic Bp is required")
        }
        if (!TextUtils.isEmpty(diastolicBp)){
            addData("Diastolic BP",diastolicBp, DbObservationValues.DIASTOLIC_BP.name)
        }else{
            errorList.add("Diastolic BP is required")
        }
        if (!TextUtils.isEmpty(pulseRate)){
            addData("Pulse Rate",pulseRate, DbObservationValues.PULSE_RATE.name)
        }else{
            errorList.add("Pulse Rate is required")
        }
        if (!TextUtils.isEmpty(temparature)){
            addData("Temperature",temparature, DbObservationValues.TEMPARATURE.name)
        }else{
            errorList.add("Temperature is required")
        }

        val textValue = formatter.getRadioText(rootView.radioGrpRespiratory)
        if (textValue != "") {
            addData("Respiratory", textValue, DbObservationValues.RESPIRATORY_MONITORING.name)
            if(rootView.linearResp.visibility == View.VISIBLE){
                val text = rootView.etCvsRespiratory.text.toString()
                if (!TextUtils.isEmpty(text)){
                    addData("If Abnormal Respiratory, specify",text, DbObservationValues.ABNORMAL_RESPIRATORY_MONITORING.name)
                }else{
                    errorList.add("If Abnormal Respiratory, please specify")
                }
            }
        } else{
            errorList.add("Please select Respiratory value")
        }


        val textValueBreast = formatter.getRadioText(rootView.radioGrpBreasts)
        if (textValue != "") {
            addData("Breast Exams", textValueBreast, DbObservationValues.BREAST_EXAM.name)

            if(rootView.linearNormal.visibility == View.VISIBLE){
                val text = rootView.etBreastFinding.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData("Normal Breasts Findings", text, DbObservationValues.NORMAL_BREAST_EXAM.name)
                } else {
                    errorList.add("Normal Breasts Findings is required")
                }
            }
            if(rootView.linearAbnormal.visibility == View.VISIBLE){
                val text = rootView.etBreastAbnormal.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    addData("Abnormal Breasts Findings", text, DbObservationValues.ABNORMAL_BREAST_EXAM.name)
                } else {
                    errorList.add("Abnormal Breasts Findings is required")
                }
            }

        } else{
            errorList.add("Please select Breast Exams value")
        }


        for (items in observationList){

            val key = items.key
            val dbObservationLabel = observationList.getValue(key)

            val value = dbObservationLabel.value
            val label = dbObservationLabel.label

            val data = DbDataList(key, value, DbSummaryTitle.B_PHYSICAL_BLOOD_PRESSURE.name, DbResourceType.Observation.name , label)
            dbDataList.add(data)

        }
        observationList.clear()


        val motherWeight = rootView.etMotherWeight.text.toString()
        val gestation = rootView.etGestation.text.toString()
        if (!TextUtils.isEmpty(motherWeight) && !TextUtils.isEmpty(gestation)){

            val isWeight = formatter.validateWeight(motherWeight)
            if (isWeight){

                addData("Mother Weight (kgs)",motherWeight, DbObservationValues.WEIGHT.name)
                addData("Gestation (weeks)",gestation, DbObservationValues.GESTATION.name)

                for (items in observationList){

                    val key = items.key
                    val dbObservationLabel = observationList.getValue(key)

                    val value = dbObservationLabel.value
                    val label = dbObservationLabel.label

                    val data = DbDataList(key, value, DbSummaryTitle.C_WEIGHT_MONITORING.name, DbResourceType.Observation.name , label)
                    dbDataList.add(data)

                }
                observationList.clear()


            }else{
                errorList.add("Please enter valid weight")
            }


        }else{
            if (TextUtils.isEmpty(motherWeight)){
                errorList.add("Mother Weight is required")
            }
            if (TextUtils.isEmpty(gestation)){
                errorList.add("Gestation is required")
            }
        }

        if (errorList.size == 0){

            val dbDataDetailsList = ArrayList<DbDataDetails>()
            val dbDataDetails = DbDataDetails(dbDataList)
            dbDataDetailsList.add(dbDataDetails)
            val dbPatientData = DbPatientData(DbResourceViews.PHYSICAL_EXAMINATION.name, dbDataDetailsList)
            kabarakViewModel.insertInfo(requireContext(), dbPatientData)

            val ft = requireActivity().supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentHolder, FragmentPhysicalExam2())
            ft.addToBackStack(null)
            ft.commit()

        }else{
            formatter.showErrorDialog(errorList, requireContext())
        }


    }


    override fun onStart() {
        super.onStart()

        getSavedData()
    }

    private fun getSavedData() {

        try {

            CoroutineScope(Dispatchers.IO).launch {

                val encounterId = formatter.retrieveSharedPreference(requireContext(),
                    DbResourceViews.PHYSICAL_EXAMINATION.name)

                if (encounterId != null){

                    val generalExamination = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.GENERAL_EXAMINATION.name), encounterId)

                    val specificAbnormal = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABNORMAL_GENERAL_EXAMINATION.name), encounterId)

                    val systolicBp = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.SYSTOLIC_BP.name), encounterId)

                    val diastolicBp = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.DIASTOLIC_BP.name), encounterId)

                    val pulseRate = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.PULSE_RATE.name), encounterId)

                    val temparature = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.TEMPARATURE.name), encounterId)

                    val cvs = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.CVS.name), encounterId)

                    val specificCvs = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABNORMAL_CVS.name), encounterId)

                    val respiratoryMonitoring = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.RESPIRATORY_MONITORING.name), encounterId)

                    val abnormalRespiratoryMonitoring = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABNORMAL_RESPIRATORY_MONITORING.name), encounterId)

                    val breastExam = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.BREAST_EXAM.name), encounterId)

                    val abnormalBreastExam = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.ABNORMAL_BREAST_EXAM.name), encounterId)

                    val normalBreastExam = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.NORMAL_BREAST_EXAM.name), encounterId)

                    val motherWeight = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.WEIGHT.name), encounterId)

                    val gestation = patientDetailsViewModel.getObservationsPerCodeFromEncounter(
                        formatter.getCodes(DbObservationValues.GESTATION.name), encounterId)

                    CoroutineScope(Dispatchers.Main).launch {

                        if (generalExamination.isNotEmpty()){
                            val value = generalExamination[0].value
                            if (value.contains("Normal")) rootView.radioGrpGeneralExam.check(R.id.radioYesNormal)
                            if (value.contains("Abnormal")) rootView.radioGrpGeneralExam.check(R.id.radioNoAbnormal)
                        }

                        if (specificAbnormal.isNotEmpty()){
                            val value = specificAbnormal[0].value
                            rootView.etAbnomality.setText(value)
                        }

                        if (systolicBp.isNotEmpty()){
                            val value = systolicBp[0].value
                            val noValue = formatter.getValues(value, 5)
                            rootView.etSystolicBp.setText(noValue)
                        }
                        if (diastolicBp.isNotEmpty()){
                            val value = diastolicBp[0].value
                            val noValue = formatter.getValues(value, 5)
                            rootView.etDiastolicBp.setText(noValue)
                        }
                        if (pulseRate.isNotEmpty()){
                            val value = pulseRate[0].value
                            val noValue = formatter.getValues(value, 4)
                            rootView.etPulseRate.setText(noValue)
                        }
                        if (temparature.isNotEmpty()){
                            val value = temparature[0].value
                            val noValue = formatter.getValues(value, 3)
                            rootView.etTemperature.setText(noValue)
                        }
                        if (cvs.isNotEmpty()){
                            val value = cvs[0].value
                            if (value.contains("Normal")) rootView.radioGrpCVS.check(R.id.radioYesCvs)
                            if (value.contains("Abnormal")) rootView.radioGrpCVS.check(R.id.radioNoCvs)
                        }
                        if (specificCvs.isNotEmpty()){
                            val value = specificCvs[0].value
                            rootView.etCvsAbnormal.setText(value)
                        }
                        if (respiratoryMonitoring.isNotEmpty()){
                            val value = respiratoryMonitoring[0].value
                            if (value.contains("Normal")) rootView.radioGrpRespiratory.check(R.id.radioYesRespiratory)
                            if (value.contains("Abnormal")) rootView.radioGrpRespiratory.check(R.id.radioNoRespiratory)
                        }
                        if (abnormalRespiratoryMonitoring.isNotEmpty()){
                            val value = abnormalRespiratoryMonitoring[0].value
                            rootView.etCvsRespiratory.setText(value)
                        }
                        if (breastExam.isNotEmpty()){
                            val value = breastExam[0].value
                            if (value.contains("Normal")) rootView.radioGrpBreasts.check(R.id.radioYesBreast)
                            if (value.contains("Abnormal")) rootView.radioGrpBreasts.check(R.id.radioNoBreast)
                        }
                        if (abnormalBreastExam.isNotEmpty()){
                            val value = abnormalBreastExam[0].value
                            rootView.etBreastAbnormal.setText(value)
                        }
                        if (normalBreastExam.isNotEmpty()){
                            val value = normalBreastExam[0].value
                            rootView.etBreastFinding.setText(value)
                        }
                        if (motherWeight.isNotEmpty()){
                            val value = motherWeight[0].value
                            val noValue = formatter.getValues(value, 3)
                            rootView.etMotherWeight.setText(noValue)
                        }
                        if (gestation.isNotEmpty()){
                            val value = gestation[0].value
                            val noValue = formatter.getValues(value, 6)
                            rootView.etGestation.setText(noValue)
                        }

                    }

                }

            }

        }catch (e: Exception){
            e.printStackTrace()
        }



    }

    private fun addData(key: String, value: String, codeLabel: String) {
        if (key != ""){
            val dbObservationLabel = DbObservationLabel(value, codeLabel)
            observationList[key] = dbObservationLabel
        }

    }

    private fun changeVisibility(linearLayout: LinearLayout, showLinear: Boolean){
        if (showLinear){
            linearLayout.visibility = View.VISIBLE
        }else{
            linearLayout.visibility = View.GONE
        }

    }


}