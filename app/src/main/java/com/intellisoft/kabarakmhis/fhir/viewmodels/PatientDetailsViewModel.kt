package com.intellisoft.kabarakmhis.fhir.viewmodels

import android.app.Application
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.fhir.data.MAX_RESOURCE_COUNT
import com.intellisoft.kabarakmhis.helperclass.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.codesystems.RiskProbability
import java.text.SimpleDateFormat

/**
 * The ViewModel helper class for PatientItemRecyclerViewAdapter, that is responsible for preparing
 * data for UI.
 */
class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String
) : AndroidViewModel(application) {
    val livePatientData = MutableLiveData<List<PatientDetailData>>()
    val context: Application = application

    /** Emits list of [PatientDetailData]. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getPatientDetailData(show: Boolean, code: String) {
        viewModelScope.launch {
            livePatientData.value = getPatientDetailDataModel(show, context, code)
        }
    }

    fun getMaternityDetailData(code: String) {
//        viewModelScope.launch { livePatientData.value = getMaternityDetailDataModel(context, code) }
    }

    /***
     * Retrieve Details of the Child
     * ***/
    fun getChildDetailData() {
//        viewModelScope.launch { livePatientData.value = getChildDetailDataModel() }
    }

    private suspend fun getPatient(): PatientItem {
        val patient = fhirEngine.load(Patient::class.java, patientId)
        return patient.toPatientItem(0)
    }

    /***
     * Child details from fhir
     * ***/

    private suspend fun getChild(): RelatedPersonItem {
        val child = fhirEngine.load(RelatedPerson::class.java, patientId)
        return child.toRelatedPersonItem(0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getPatientRelatedPersons(): List<RelatedPersonItem> {
        val relations: MutableList<RelatedPersonItem> = mutableListOf()

        fhirEngine
            .search<Patient> {
                filter(
                    Patient.LINK, { value = "Patient/$patientId" }

                )
            }
            .take(MAX_RESOURCE_COUNT)
            .map { createRelatedPersonItem(it, getApplication<Application>().resources) }
            .let { relations.addAll(it) }


        return relations
    }

    private suspend fun getPatientObservations(
        context: Application,
        code: String
    ): List<ObservationItem> {
        val observations: MutableList<ObservationItem> = mutableListOf()

        fhirEngine
            .search<Observation> {
                filter(
                    Observation.SUBJECT, { value = "Patient/$patientId" }

                )
                sort(Observation.DATE, Order.DESCENDING)
            }
            .take(MAX_RESOURCE_COUNT)

            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { data ->
                /**
                 * Check the current Unit and return related data
                 */
                observations.addAll(data)
//                when (code) {
//                    "0" -> {
//                        observations.addAll(
//                            getAssessmentDetails(
//                                data,
//                                concatenate(
//                                    maternity_unit_details,
//                                    maternity_unit_child_details,
//                                    maternity_baby_registration
//                                )
//                            )
//                        )
//                    }
//                    "1" -> {
//                        observations.addAll(
//                            getAssessmentDetails(
//                                data,
//                                concatenate(
//                                    newborn_unit_details,
//                                    child_newborn_unit_details,
//                                    assessment_unit_details,
//                                    child_feeding_needs, child_feed_prescription, child_feeding_data
//                                )
//                            )
//                        )
//                    }
//                    "2" -> {
//                        observations.addAll(
//                            getAssessmentDetails(
//                                data,
//                                concatenate(
//                                    postnatal_unit_details,
//                                    post_natal_milk_expression,
//                                    post_natal_child_feeding,
//                                    post_natal_child_supplements,
//                                    custom_unit_details
//                                )
//                            )
//                        )
//                    }
//                    "3" -> {
//                        observations.addAll(getAssessmentDetails(data, custom_unit_details))
//                    }
//                    "4" -> {
//                        observations.addAll(getAssessmentDetails(data, human_milk_details))
//                    }
//                    "5" -> {
//                        observations.addAll(
//                            getAssessmentDetails(
//                                data,
//                                concatenate(assessment_unit_details)
//                            )
//                        )
//                    }
//                    "6" -> {
//                        observations.addAll(
//                            getAssessmentDetails(
//                                data,
//                                concatenate(discharge_details)
//                            )
//                        )
//                    }
//                    else -> {
//                        observations.addAll(data)
//                    }
//
//                }
            }


        return observations.distinct()
    }

    private fun <T> concatenate(vararg lists: List<T>): List<T> {
        val result: MutableList<T> = ArrayList()
        lists.forEach { list: List<T> -> result.addAll(list) }
        return result
    }

    private fun getAssessmentDetails(
        data: List<ObservationItem>,
        human_milk_details: List<String>
    ): Collection<ObservationItem> {
        val minor: MutableList<ObservationItem> = mutableListOf()

        for (i in data.indices) {
            human_milk_details.forEach {
                if (data[i].code == it) {
                    val one =
                        ObservationItem(data[i].id, data[i].code, data[i].effective, data[i].value)
                    minor.add(one)
                }
            }

        }
        return minor.distinctBy { it.code }
    }


    /**
     * Child Observations
     * **/
    private suspend fun getChildObservations(): List<ObservationItem> {
        val observations: MutableList<ObservationItem> = mutableListOf()

        fhirEngine
            .search<Observation> {
                filter(
                    Observation.SUBJECT, { value = "RelatedPerson/$patientId" }

                )
            }
            .take(MAX_RESOURCE_COUNT)
            .map { createObservationItem(it, getApplication<Application>().resources) }
            .let { observations.addAll(it) }


        return observations
    }

    private suspend fun getPatientConditions(): List<ConditionItem> {
        val conditions: MutableList<ConditionItem> = mutableListOf()
        fhirEngine
            .search<Condition> { filter(Condition.SUBJECT, { value = "Patient/$patientId" }) }
            .take(MAX_RESOURCE_COUNT)
            .map { createConditionItem(it, getApplication<Application>().resources) }
            .let { conditions.addAll(it) }
        return conditions
    }


    /***
     * Load Child Details
     * ***/
//    private suspend fun getChildDetailDataModel(): List<PatientDetailData> {
//        val data = mutableListOf<PatientDetailData>()
//        val child = getChild()
//        child.riskItem = getChildRiskAssessment()
//        val observations = getChildObservations()
//
//        child.let {
//            data.add(ChildDetailOverview(it, firstInGroup = true))
//            data.add(
//                PatientDetailProperty(
//                    PatientProperty(
//                        getString(R.string.patient_property_dob),
//                        it.dob
//                    )
//                )
//            )
//            data.add(
//                PatientDetailProperty(
//                    PatientProperty(
//                        getString(R.string.patient_property_gender),
//                        it.gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
//
//                    ), lastInGroup = true
//                )
//            )
//        }
//        if (observations.isNotEmpty()) {
//
//            data.add(PatientDetailHeader(getString(R.string.header_observation)))
//
//            val observationDataModel =
//                observations.mapIndexed { index, observationItem ->
//
//                    PatientDetailObservation(
//                        observationItem,
//                        firstInGroup = index == 0,
//                        lastInGroup = index == observations.size - 1
//                    )
//
//                }
//
//            data.addAll(observationDataModel)
//
//        }
//
//        return data
//    }

//    private suspend fun getMaternityDetailDataModel(
//        context: Application,
//        code: String
//    ): List<PatientDetailData> {
//        val data = mutableListOf<PatientDetailData>()
//        val patient = getPatient()
//        patient.riskItem = getPatientRiskAssessment()
//
//        val relation = getPatientRelatedPersons()
//        val observations = getPatientObservations(context, code)
//        val conditions = getPatientConditions()
//
//        patient.let {
//            data.add(PatientDetailOverview(it, firstInGroup = true))
//            data.add(
//                PatientDetailProperty(
//                    PatientProperty(getString(R.string.patient_property_mobile), it.phone)
//                )
//            )
//            data.add(
//                PatientDetailProperty(
//                    PatientProperty(getString(R.string.patient_property_id), it.resourceId)
//                )
//            )
//            data.add(
//                PatientDetailProperty(
//                    PatientProperty(
//                        getString(R.string.patient_property_address),
//                        "${it.region},${it.district},${it.city}, ${it.country} "
//                    )
//                )
//            )
//            data.add(
//                PatientDetailProperty(
//                    PatientProperty(
//                        getString(R.string.patient_property_dob),
//                        it.dob
//                    )
//                )
//            )
//            data.add(
//                PatientDetailProperty(
//                    PatientProperty(
//                        getString(R.string.patient_property_gender),
//                        it.gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
//
//                    ), lastInGroup = true
//                )
//            )
//
//        }
//
//        if (relation.isNotEmpty()) {
//
//            data.add(PatientDetailHeader(getString(R.string.header_relation)))
//            val relationDataModel =
//                relation.mapIndexed { index, relationItem ->
//
//                    PatientDetailRelation(
//                        relationItem,
//                        firstInGroup = index == 0,
//                        lastInGroup = index == relation.size - 1
//                    )
//
//                }
//
//            data.addAll(relationDataModel)
//        }
//        if (observations.isNotEmpty()) {
//
//            data.add(PatientDetailHeader(getString(R.string.header_observation)))
//
//
//            val observationDataModel =
//                observations.mapIndexed { index, observationItem ->
//
//                    PatientDetailObservation(
//                        observationItem,
//                        firstInGroup = index == 0,
//                        lastInGroup = index == observations.size - 1
//                    )
//
//                }
//
//            data.addAll(observationDataModel)
//
//        }
//
//        if (conditions.isNotEmpty()) {
//            data.add(PatientDetailHeader(getString(R.string.header_conditions)))
//            val conditionDataModel =
//                conditions.mapIndexed { index, conditionItem ->
//                    PatientDetailCondition(
//                        conditionItem,
//                        firstInGroup = index == 0,
//                        lastInGroup = index == conditions.size - 1
//                    )
//                }
//            data.addAll(conditionDataModel)
//        }
//
//        return data
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getPatientDetailDataModel(
        show: Boolean,
        context: Application,
        code: String
    ): List<PatientDetailData> {
        val data = mutableListOf<PatientDetailData>()
        val patient = getPatient()
//        patient.riskItem = getPatientRiskAssessment()

        val relation = getPatientRelatedPersons()
        val observations = getPatientObservations(context, code)
        val conditions = getPatientConditions()
        data.clear()

        patient.let {
            data.add(PatientDetailOverview(it, firstInGroup = true))
            if (show) {
                data.add(
                    PatientDetailProperty(
                        PatientProperty(getString(R.string.patient_property_mobile), it.phone)
                    )
                )
            }
            data.add(
                PatientDetailProperty(
                    PatientProperty(getString(R.string.patient_property_id), it.resourceId)
                )
            )
            if (show) {
                data.add(
                    PatientDetailProperty(
                        PatientProperty(
                            getString(R.string.patient_property_address),
                            "${it.region},${it.district},${it.city}, ${it.country} "
                        )
                    )
                )
            }
            data.add(
                PatientDetailProperty(
                    PatientProperty(
                        getString(R.string.patient_property_dob),
                        it.dob
                    )
                )
            )
            data.add(
                PatientDetailProperty(
                    PatientProperty(
                        getString(R.string.patient_property_gender),
                        it.gender.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

                    ), lastInGroup = true
                )
            )

        }

        if (observations.isNotEmpty()) {

            data.add(PatientDetailHeader(getString(R.string.header_observation)))

            val observationDataModel =
                observations.mapIndexed { index, observationItem ->

                    PatientDetailObservation(
                        observationItem,
                        firstInGroup = index == 0,
                        lastInGroup = index == observations.size - 1
                    )

                }

            data.addAll(observationDataModel)

        }

        if (conditions.isNotEmpty()) {
            data.add(PatientDetailHeader(getString(R.string.header_conditions)))
            val conditionDataModel =
                conditions.mapIndexed { index, conditionItem ->
                    PatientDetailCondition(
                        conditionItem,
                        firstInGroup = index == 0,
                        lastInGroup = index == conditions.size - 1
                    )
                }
            data.addAll(conditionDataModel)
        }

        return data
    }

    private fun getString(resId: Int) = getApplication<Application>().resources.getString(resId)


    /**
     * Child Risk Assessment
     *
     * **/
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getChildRiskAssessment(): RiskAssessmentItem {
        val riskAssessment =
            fhirEngine
                .search<RiskAssessment> {
                    filter(
                        RiskAssessment.SUBJECT,
                        { value = "RelatedPerson/$patientId" })
                }
                .filter { it.hasOccurrence() }
                .sortedByDescending { it.occurrenceDateTimeType.value }
                .firstOrNull()
        return riskReport(riskAssessment)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getPatientRiskAssessment(): RiskAssessmentItem {
        val riskAssessment =
            fhirEngine
                .search<RiskAssessment> {
                    filter(
                        RiskAssessment.SUBJECT,
                        { value = "Patient/$patientId" })
                }
                .filter { it.hasOccurrence() }
                .sortedByDescending { it.occurrenceDateTimeType.value }
                .firstOrNull()
        return riskReport(riskAssessment)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun riskReport(riskAssessment: RiskAssessment?): RiskAssessmentItem {
        return RiskAssessmentItem(
            getRiskAssessmentStatusColor(riskAssessment),
            getRiskAssessmentStatus(riskAssessment),
            getLastContactedDate(riskAssessment),
            getPatientDetailsCardColor(riskAssessment)
        )
    }

    private fun getRiskAssessmentStatusColor(riskAssessment: RiskAssessment?): Int {
        riskAssessment?.let {
            return when (it.prediction.first().qualitativeRisk.coding.first().code) {
                RiskProbability.LOW.toCode() -> ContextCompat.getColor(
                    getApplication(),
                    R.color.low_risk
                )
                RiskProbability.MODERATE.toCode() ->
                    ContextCompat.getColor(getApplication(), R.color.moderate_risk)
                RiskProbability.HIGH.toCode() -> ContextCompat.getColor(
                    getApplication(),
                    R.color.high_risk
                )
                else -> ContextCompat.getColor(getApplication(), R.color.unknown_risk)
            }
        }
        return ContextCompat.getColor(getApplication(), R.color.unknown_risk)
    }

    private fun getPatientDetailsCardColor(riskAssessment: RiskAssessment?): Int {
        riskAssessment?.let {
            return when (it.prediction.first().qualitativeRisk.coding.first().code) {
                RiskProbability.LOW.toCode() ->
                    ContextCompat.getColor(getApplication(), R.color.low_risk_background)
                RiskProbability.MODERATE.toCode() ->
                    ContextCompat.getColor(getApplication(), R.color.moderate_risk_background)
                RiskProbability.HIGH.toCode() ->
                    ContextCompat.getColor(getApplication(), R.color.high_risk_background)
                else -> ContextCompat.getColor(getApplication(), R.color.unknown_risk_background)
            }
        }
        return ContextCompat.getColor(getApplication(), R.color.unknown_risk_background)
    }

    private fun getRiskAssessmentStatus(riskAssessment: RiskAssessment?): String {
        riskAssessment?.let {
            return StringUtils.upperCase(it.prediction.first().qualitativeRisk.coding.first().display)
        }
        return getString(R.string.unknown)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastContactedDate(riskAssessment: RiskAssessment?): String {
        riskAssessment?.let {
            if (it.hasOccurrence()) {
                return LocalDate.parse(
                    it.occurrenceDateTimeType.valueAsString,
                    DateTimeFormatter.ISO_DATE_TIME
                )
                    .toString()
            }
        }
        return getString(R.string.none)
    }

    fun clearLiveData() {
//        livePatientData.value = null
    }

    companion object {
        /**
         * Creates RelatedPersonItem objects with displayable values from the Fhir RelatedPerson objects.
         */
        @RequiresApi(Build.VERSION_CODES.O)
        private fun createRelatedPersonItem(
            relation: Patient,
            resources: Resources
        ): RelatedPersonItem {
            val gender = relation.gender ?: "Unknown"
            var dob = relation.birthDate ?: ""
            var name: String? = null
            var yea: String? = null

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            if (dob.toString().isNotEmpty()) {
                yea = formatter.format(dob)
                dob = FormatterClass().getFormattedAge(yea.toString(), resources)
            }
            name =
                if (relation.hasName()) relation.name[0].nameAsSingleString else relation.logicalId.substring(
                    0,
                    8
                )

            return RelatedPersonItem(
                relation.logicalId,
                name.toString(),
                gender.toString(),
                dob.toString()
            )
        }

        /**
         * Creates ObservationItem objects with displayable values from the Fhir Observation objects.
         */
        private fun createObservationItem(
            observation: Observation,
            resources: Resources
        ): ObservationItem {
            val observationCode = observation.code.codingFirstRep.code ?: ""


            // Show nothing if no values available for datetime and value quantity.
            val dateTimeString =
                if (observation.hasEffectiveDateTimeType()) {
                    observation.effectiveDateTimeType.asStringValue()
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }
            val value =
                when {
                    observation.hasValueQuantity() -> {
                        observation.valueQuantity.value.toString()
                    }
                    observation.hasValueCodeableConcept() -> {
                        observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
                    }
                    observation.hasNote() -> {
                        observation.note.firstOrNull()?.author
                    }
                    else -> {
                        observation.code.text ?: observation.code.codingFirstRep.display
                    }
                }
            val valueUnit =
                if (observation.hasValueQuantity()) {
                    observation.valueQuantity.unit ?: observation.valueQuantity.code
                } else {
                    ""
                }

            val valueString = "$value $valueUnit"

            return ObservationItem(
                observation.logicalId,
                observationCode,
                dateTimeString,
                valueString,

                )
        }

        /** Creates ConditionItem objects with displayable values from the Fhir Condition objects. */
        private fun createConditionItem(
            condition: Condition,
            resources: Resources
        ): ConditionItem {
            val observationCode = condition.code.text ?: condition.code.codingFirstRep.display ?: ""

            // Show nothing if no values available for datetime and value quantity.
            val dateTimeString =
                if (condition.hasOnsetDateTimeType()) {
                    condition.onsetDateTimeType.asStringValue()
                } else {
                    resources.getText(R.string.message_no_datetime).toString()
                }
            val value =
                if (condition.hasVerificationStatus()) {
                    condition.verificationStatus.codingFirstRep.code
                } else {
                    ""
                }

            return ConditionItem(
                condition.logicalId,
                observationCode,
                dateTimeString,
                value
            )
        }


    }
}

interface PatientDetailData {
    val firstInGroup: Boolean
    val lastInGroup: Boolean
}

data class PatientDetailHeader(
    val header: String,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData



data class PatientDetailOverview(
    val patient: PatientItem,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData

/**
 * Child Overview
 **/
data class ChildDetailOverview(
    val relation: RelatedPersonItem,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData

data class PatientDetailRelation(
    val relation: RelatedPersonItem,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData

data class PatientDetailObservation(
    val observation: ObservationItem,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData

data class PatientDetailProperty(
    val patientProperty: PatientProperty,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData

data class PatientDetailCondition(
    val condition: ConditionItem,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false
) : PatientDetailData

data class PatientProperty(val header: String, val value: String)

class PatientDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(PatientDetailsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return PatientDetailsViewModel(application, fhirEngine, patientId) as T
    }


}


/***
 *
 * Related Person Details
 * ***/
class RelatedPersonDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String
) : AndroidViewModel(application) {

}

class RelatedPersonDetailsViewModelFactory(
    private val application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(RelatedPersonDetailsViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        return PatientDetailsViewModel(application, fhirEngine, patientId) as T
    }


}

data class RiskAssessmentItem(
    var riskStatusColor: Int,
    var riskStatus: String,
    var lastContacted: String,
    var patientCardColor: Int
)