package com.intellisoft.kabarakmhis.fhir.screens

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.FhirApplication
import com.intellisoft.kabarakmhis.fhir.viewmodels.ScreenerViewModel


/** A fragment class to show screener questionnaire screen. */
class ScreenerFragment : Fragment(R.layout.screener_encounter_fragment) {

    private val viewModel: ScreenerViewModel by viewModels()
    private val args: ScreenerFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpActionBar()
        setHasOptionsMenu(true)
        updateArguments()
        onBackPressed()
        observeResourcesSaveAction()
        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = args.title
            setDisplayHomeAsUpEnabled(true)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.screen_encounter_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_patient_submit -> {
                onSubmitAction()
                true
            }
            android.R.id.home -> {
                showCancelScreenerQuestionnaireAlertDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observeResourcesSaveAction() {
        viewModel.isResourcesSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.inputs_missing),
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@observe
            }else{
                Toast.makeText(
                    requireContext(),
                    getString(R.string.resources_saved),
                    Toast.LENGTH_SHORT
                )
                    .show()
                NavHostFragment.findNavController(this).navigateUp()
            }


        }

    }


    private fun setUpActionBar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun updateArguments() {
        requireArguments().putString(QUESTIONNAIRE_FILE_PATH_KEY, args.question)
    }

    private fun addQuestionnaireFragment() {
        try {
            val fragment = QuestionnaireFragment()
            fragment.arguments =
                bundleOf(QuestionnaireFragment.EXTRA_QUESTIONNAIRE_JSON_STRING to viewModel.questionnaire)
            childFragmentManager.commit {
                add(R.id.add_patient_container, fragment, QUESTIONNAIRE_FRAGMENT_TAG)
            }
        } catch (e: Exception) {

        }
    }

    private fun onSubmitAction() {
        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment

        viewModel.saveAssessment(
            questionnaireFragment.getQuestionnaireResponse(),
            args.patientId
        )
    }

    private fun showCancelScreenerQuestionnaireAlertDialog() {
        val alertDialog: AlertDialog? =
            activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.cancel_questionnaire_message))
                    setPositiveButton(getString(android.R.string.yes)) { _, _ ->
                        NavHostFragment.findNavController(this@ScreenerFragment).navigateUp()
                    }
                    setNegativeButton(getString(android.R.string.no)) { _, _ -> }
                }
                builder.create()
            }
        alertDialog?.show()
    }

    private fun onBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            showCancelScreenerQuestionnaireAlertDialog()
        }
    }

//    private fun observeResourcesSaveAction() {
//        viewModel.isResourcesSaved.observe(viewLifecycleOwner) {
//            if (!it) {
//                Toast.makeText(
//                    requireContext(),
//                    getString(R.string.inputs_missing),
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//                return@observe
//            }
//
//
//
//            when (activity?.let { FhirApplication.getCurrent(it) }) {
//                CHILD_ASSESSMENT -> {
//                    Toast.makeText(
//                        requireContext(),
//                        getString(R.string.resources_child_assessed),
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//
//
//                    /**
//                     * Reload Page with feeding needs assessment questionnaire
//                     */
//
//
//                    NavHostFragment.findNavController(this).navigateUp()
//
//                }
//                else -> {
//                    Toast.makeText(
//                        requireContext(),
//                        getString(R.string.resources_saved),
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                    NavHostFragment.findNavController(this).navigateUp()
//                }
//            }
//        }
//        /***
//         * Listen for APGAR SCORE
//         * ***/
////        viewModel.apgarScore.observe(viewLifecycleOwner) {
////            if (it.score.isEmpty()) {
////                Toast.makeText(
////                    requireContext(), it.message,
////                    Toast.LENGTH_SHORT
////                )
////                    .show()
////                return@observe
////            }
////            Toast.makeText(
////                requireContext(), it?.message,
////                Toast.LENGTH_SHORT
////            )
////                .show()
////            NavHostFragment.findNavController(this).navigateUp()
////        }
//    }

    companion object {
        const val QUESTIONNAIRE_FILE_PATH_KEY = "questionnaire-file-path-key"
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}