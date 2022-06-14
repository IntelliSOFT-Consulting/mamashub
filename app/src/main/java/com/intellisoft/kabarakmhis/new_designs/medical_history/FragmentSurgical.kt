package com.intellisoft.kabarakmhis.new_designs.medical_history

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.*
import com.intellisoft.kabarakmhis.new_designs.roomdb.KabarakViewModel
import kotlinx.android.synthetic.main.fragment_surgical.view.*


class FragmentSurgical : Fragment() {

    private val formatter = FormatterClass()

    private lateinit var rootView: View
    private var observationList = mutableMapOf<String, String>()
    private lateinit var kabarakViewModel: KabarakViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(R.layout.fragment_surgical, container, false)
        kabarakViewModel = KabarakViewModel(requireContext().applicationContext as Application)

        rootView.btnSave.setOnClickListener {

            saveData()

        }

        formatter.saveCurrentPage("1", requireContext())
        getPageDetails()

        return rootView
    }

    private fun saveData() {

        if (rootView.checkboxNoPast.isChecked) addData("Surgical History",rootView.checkboxNoPast.text.toString())
        if (rootView.checkboxNoKnowledge.isChecked) addData("Surgical History",rootView.checkboxNoKnowledge.text.toString())
        if (rootView.checkboxDilation.isChecked) addData("Surgical History",rootView.checkboxDilation.text.toString())
        if (rootView.checkboxMyomectomy.isChecked) addData("Surgical History",rootView.checkboxMyomectomy.text.toString())
        if (rootView.checkboxRemoval.isChecked) addData("Surgical History",rootView.checkboxRemoval.text.toString())
        if (rootView.checkboxOophorectomy.isChecked) addData("Surgical History",rootView.checkboxOophorectomy.text.toString())
        if (rootView.checkboxSalpi.isChecked) addData("Surgical History",rootView.checkboxSalpi.text.toString())
        if (rootView.checkboxCervical.isChecked) addData("Surgical History",rootView.checkboxCervical.text.toString())

        val otherGyna = rootView.etOtherGyna.text.toString()
        val otherSurgeries = rootView.etOtherSurgery.text.toString()

        if (!TextUtils.isEmpty(otherGyna)){
            addData("Other Gynecological Procedures",otherGyna)
        }
        if (!TextUtils.isEmpty(otherSurgeries)){
            addData("Other Surgeries",otherSurgeries)
        }

        val dbDataList = ArrayList<DbDataList>()

        for (items in observationList){

            val key = items.key
            val value = observationList.getValue(key)

            val data = DbDataList(key, value, "Medical and Surgical History", DbResourceType.Observation.name)
            dbDataList.add(data)

        }

        val dbDataDetailsList = ArrayList<DbDataDetails>()
        val dbDataDetails = DbDataDetails(dbDataList)
        dbDataDetailsList.add(dbDataDetails)
        val dbPatientData = DbPatientData(DbResourceViews.MEDICAL_HISTORY.name, dbDataDetailsList)
        kabarakViewModel.insertInfo(requireContext(), dbPatientData)

        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragmentHolder, FragmentMedical())
        ft.addToBackStack(null)
        ft.commit()

    }

    private fun addData(key: String, value: String) {
        observationList[key] = value
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun getPageDetails() {

        val totalPages = formatter.retrieveSharedPreference(requireContext(), "totalPages")
        val currentPage = formatter.retrieveSharedPreference(requireContext(), "currentPage")

        if (totalPages != null && currentPage != null){

            formatter.progressBarFun(requireContext(), currentPage.toInt(), totalPages.toInt(), rootView)

        }


    }


}