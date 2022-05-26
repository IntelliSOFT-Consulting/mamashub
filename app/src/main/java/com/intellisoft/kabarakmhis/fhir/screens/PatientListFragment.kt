package com.intellisoft.kabarakmhis.fhir.screens

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.sync.State
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.databinding.FragmentPatientListBinding
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.adapters.PatientItemRecyclerViewAdapter
import com.intellisoft.kabarakmhis.fhir.viewmodels.MainActivityViewModel
import com.intellisoft.kabarakmhis.fhir.viewmodels.PatientListViewModel
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.helperclass.PatientItem

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception

class PatientListFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var fhirEngine: FhirEngine
    private lateinit var patientListViewModel: PatientListViewModel
    private lateinit var searchView: SearchView
    private var _binding: FragmentPatientListBinding? = null
    private val binding
        get() = _binding!!
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val args: PatientListFragmentArgs by navArgs()

    private lateinit var mySpinner: Spinner

    private var formatter = FormatterClass()

    private var filterData: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.title_patient_list)
            setDisplayHomeAsUpEnabled(true)
        }

        try {
            fhirEngine = FhirApplication.fhirEngine(requireContext())
            patientListViewModel =
                ViewModelProvider(
                    this,
                    PatientListViewModel.PatientListViewModelFactory(
                        requireActivity().application,
                        fhirEngine
                    )
                )
                    .get(PatientListViewModel::class.java)
            val recyclerView: RecyclerView = binding.patientList
            val adapter = PatientItemRecyclerViewAdapter(this::onPatientItemClicked)
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(

                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
                    setDrawable(ColorDrawable(Color.LTGRAY))
                }
            )


            patientListViewModel.liveSearchedPatients.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


        mySpinner = binding.mySpinner

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.birds,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = adapter
        mySpinner.onItemSelectedListener = this


        patientListViewModel.patientCount.observe(
            viewLifecycleOwner
        ) {
            binding.patientCount.text = "$it Patient(s)"
        }
        searchView = binding.search
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {

                    patientListViewModel.searchPatientsByName(newText)


                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    patientListViewModel.searchPatientsByName(query)
                    return true
                }
            }
        )
        searchView.setOnQueryTextFocusChangeListener { view, focused ->
            if (!focused) {
                // hide soft keyboard
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (searchView.query.isNotEmpty()) {
                            searchView.setQuery("", true)
                        } else {
                            isEnabled = false
                            activity?.onBackPressed()
                        }
                    }
                }
            )

        binding.apply {
            addPatient.setOnClickListener { onAddPatientClick() }
            addPatient.setColorFilter(Color.WHITE)
        }
        setHasOptionsMenu(true)

        lifecycleScope.launch {
            mainActivityViewModel.pollState.collect {
                // After the sync is successful, update the patients list on the page.
                if (it is State.Finished) {
                    patientListViewModel.searchPatientsByName(searchView.query.toString().trim())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // hide the soft keyboard when the navigation drawer is shown on the screen.
                searchView.clearFocus()

                true
            }
            else -> false
        }
    }

    private fun onPatientItemClicked(patientItem: PatientItem) {
        if (args.step.isNotEmpty()) {

            Log.e("----- ", args.step)

//            findNavController().navigate(
//                PatientListFragmentDirections.navigateToProductDetail(
//                    patientItem.resourceId, "0"
//                )
//            )

            when (args.step) {
                "0" -> {
                    findNavController().navigate(
                        PatientListFragmentDirections.navigateToProductDetail(
                            patientItem.resourceId, "0"
                        )
                    )
                }
//                "1" -> {
//                    findNavController().navigate(
//                        PatientListFragmentDirections.navigateToAssessment(
//                            patientItem.resourceId, "6"
//                        )
//                    )
//
//                }
                "2" -> {

                    findNavController().navigate(
                        PatientListFragmentDirections.navigateToDhm(
                            patientItem.resourceId, "4"
                        )
                    )

                }
                "3" -> {

                    findNavController().navigate(
                        PatientListFragmentDirections.navigateMedicalHistory(
                            patientItem.resourceId,
                            "2"
                        )
                    )
                }
//                "4" -> {
//
//                    findNavController().navigate(
//                        PatientListFragmentDirections.navigateToProductDetail(
//                            patientItem.resourceId, "3"
//                        )
//                    )
//                }
//                "2" -> {
//
//                    findNavController().navigate(
//                        PatientListFragmentDirections.navigateToDhm(
//                            patientItem.resourceId, "4"
//                        )
//                    )
//                }
//                else -> {
//                    findNavController().navigate(
//                        PatientListFragmentDirections.navigateToAssessment(
//                            patientItem.resourceId, "5"
//                        )
//                    )
//                }


            }
        }
    }


    private fun onAddPatientClick() {
        findNavController().navigate(PatientListFragmentDirections.actionPatientListToAddPatientFragment())
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val text: String = p0?.getItemAtPosition(p2).toString()
//        filterData = formatter.getSearchQuery(text, requireContext())

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}