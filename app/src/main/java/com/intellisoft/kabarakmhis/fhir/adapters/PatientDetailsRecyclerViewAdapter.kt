package com.intellisoft.kabarakmhis.fhir.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.databinding.PatientDetailsCardViewBinding
import com.intellisoft.kabarakmhis.databinding.PatientDetailsHeaderBinding
import com.intellisoft.kabarakmhis.databinding.PatientListItemViewBinding
import com.intellisoft.kabarakmhis.fhir.viewmodels.*
import com.intellisoft.kabarakmhis.helperclass.*

class PatientDetailsRecyclerViewAdapter(
    private val onScreenerClick: () -> Unit,
    private val onMaternityClick: () -> Unit
) :

    ListAdapter<PatientDetailData, PatientDetailItemViewHolder>(PatientDetailDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientDetailItemViewHolder {
        return when (ViewTypes.from(viewType)) {
            ViewTypes.HEADER ->
                PatientDetailsHeaderItemViewHolder(
                    PatientDetailsCardViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ViewTypes.PATIENT ->
                PatientOverviewItemViewHolder(
                    PatientDetailsHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onScreenerClick, onMaternityClick
                )
            ViewTypes.PATIENT_PROPERTY ->
                PatientPropertyItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )

            ViewTypes.OBSERVATION ->
                PatientDetailsObservationItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            ViewTypes.CONDITION ->
                PatientDetailsConditionItemViewHolder(
                    PatientListItemViewBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }
    }

    override fun onBindViewHolder(holder: PatientDetailItemViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
        if (holder is PatientDetailsHeaderItemViewHolder) return

        holder.itemView.background =
            if (model.firstInGroup && model.lastInGroup) {
                allCornersRounded()
            } else if (model.firstInGroup) {
                topCornersRounded()
            } else if (model.lastInGroup) {
                bottomCornersRounded()
            } else {
                noCornersRounded()
            }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item) {
            is PatientDetailHeader -> ViewTypes.HEADER
            is PatientDetailOverview -> ViewTypes.PATIENT
            is PatientDetailProperty -> ViewTypes.PATIENT_PROPERTY
            is PatientDetailObservation -> ViewTypes.OBSERVATION
            is PatientDetailCondition -> ViewTypes.CONDITION
            else -> {
                throw IllegalArgumentException("Undefined Item type")
            }
        }.ordinal
    }
}

abstract class PatientDetailItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    abstract fun bind(data: PatientDetailData)
}

class PatientOverviewItemViewHolder(
    private val binding: PatientDetailsHeaderBinding,
    val onScreenerClick: () -> Unit,
    val onMaternityClick: () -> Unit
) : PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        binding.screener.setOnClickListener { onScreenerClick() }
        binding.maternity.setOnClickListener { onMaternityClick() }
        (data as PatientDetailOverview).let { binding.title.text = it.patient.name }
        data.patient.riskItem?.let {
            binding.patientContainer.setBackgroundColor(it.patientCardColor)
            binding.statusValue.text = it.riskStatus
            binding.statusValue.setTextColor(Color.BLACK)
            binding.statusValue.background =
                allCornersRounded().apply { fillColor = ColorStateList.valueOf(it.riskStatusColor) }
            binding.lastContactValue.text = it.lastContacted
        }
    }
}

class PatientPropertyItemViewHolder(
    private val binding: PatientListItemViewBinding,

    ) :
    PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as PatientDetailProperty).let {
            binding.name.text = it.patientProperty.header
            binding.fieldName.text = it.patientProperty.value
        }
//        binding.status.visibility = View.GONE
        binding.id.visibility = View.GONE
        binding.tvView.visibility = View.INVISIBLE
    }
}

class PatientDetailsHeaderItemViewHolder(private val binding: PatientDetailsCardViewBinding) :
    PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as PatientDetailHeader).let { binding.header.text = it.header }
    }
}

class PatientDetailsObservationItemViewHolder(private val binding: PatientListItemViewBinding) :
    PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as PatientDetailObservation).let {

            val title = it.observation.code
            val value = it.observation.value

            binding.name.text = title
            binding.fieldName.text = value

        }
//        binding.status.visibility = View.GONE
        binding.id.visibility = View.GONE
        binding.tvView.visibility = View.INVISIBLE
    }
}

class PatientDetailsConditionItemViewHolder(private val binding: PatientListItemViewBinding) :
    PatientDetailItemViewHolder(binding.root) {
    override fun bind(data: PatientDetailData) {
        (data as PatientDetailCondition).let {
            binding.name.text = it.condition.code
            binding.fieldName.text = it.condition.value
        }
//        binding.status.visibility = View.GONE
        binding.id.visibility = View.GONE
        binding.tvView.visibility = View.INVISIBLE
    }
}


class PatientDetailDiffUtil : DiffUtil.ItemCallback<PatientDetailData>() {
    override fun areItemsTheSame(o: PatientDetailData, n: PatientDetailData) = o == n

    override fun areContentsTheSame(o: PatientDetailData, n: PatientDetailData) =
        areItemsTheSame(o, n)
}