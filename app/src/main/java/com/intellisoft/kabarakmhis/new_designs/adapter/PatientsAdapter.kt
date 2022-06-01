package com.intellisoft.kabarakmhis.new_designs.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.kabarakmhis.R
import com.intellisoft.kabarakmhis.helperclass.FormatterClass
import com.intellisoft.kabarakmhis.new_designs.data_class.DBEntry
import com.intellisoft.kabarakmhis.new_designs.screens.PatientProfile

class PatientsAdapter(private var entryList: List<DBEntry>,
                      private val context: Context
) : RecyclerView.Adapter<PatientsAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvName: TextView = itemView.findViewById(R.id.name)
        val tvFieldName: TextView = itemView.findViewById(R.id.field_name)
        val tvId: TextView = itemView.findViewById(R.id.id)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = entryList[pos].resource.id
            val dob = entryList[pos].resource.birthDate
            val name = entryList[pos].resource.name[0].family

            FormatterClass().saveSharedPreference(context, "patientId", id)
            FormatterClass().saveSharedPreference(context, "dob", dob)
            FormatterClass().saveSharedPreference(context, "name", name)

            context.startActivity(Intent(context, PatientProfile::class.java))

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.patient_list_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val id = entryList[position].resource.id
        val name = entryList[position].resource.name
        val birthDate = entryList[position].resource.birthDate

        holder.tvId.text = id
        holder.tvName.text = name[0].family
        holder.tvFieldName.text = birthDate


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}