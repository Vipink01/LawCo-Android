package com.business.lawco.adapter.consumer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.databinding.SelectAttorneyItemBinding
import com.business.lawco.model.consumer.SelectAreaOfPracticeModel

class SelectAreaOfPracticeAdapter(private var selectAttorneyList: MutableList<SelectAreaOfPracticeModel>,
    var requireContext: Context, val selectLocation:SelectPractice) :
    RecyclerView.Adapter<SelectAreaOfPracticeAdapter.Holder>() {

    class Holder(val binding: SelectAttorneyItemBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(dataItem: SelectAreaOfPracticeModel) {
            binding.checkBoxAttorney.isChecked = dataItem.status
            binding.checkBoxAttorney.text = dataItem.areaOfPractice+" Attorneys"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            SelectAttorneyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return selectAttorneyList.size
    }

    @SuppressLint("SuspiciousIndentation", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: Holder, position: Int) {
       /* holder.binding.checkBoxAttorney.setOnCheckedChangeListener { _, isChecked ->
            selectAttorneyList[position].status = isChecked
        }*/

        holder.binding.checkBoxAttorney.isChecked = selectAttorneyList[position].status


        //                selectAttorneyList[position].status==false
//                selectLocation.practice(selectAttorneyList[position].status==false, selectAttorneyList[position].areaOfPractice,false)

        holder.binding.checkBoxAttorney.setOnClickListener {
            val range: SelectAreaOfPracticeModel = selectAttorneyList.get(position)
            range.id=(selectAttorneyList[position].id)
            range.areaOfPractice=(selectAttorneyList[position].areaOfPractice)
            range.status = holder.binding.checkBoxAttorney.isChecked
            selectAttorneyList[position] = range
            notifyDataSetChanged()
        }
        holder.bind(selectAttorneyList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<SelectAreaOfPracticeModel>) {
        selectAttorneyList = newData.toMutableList() as ArrayList<SelectAreaOfPracticeModel>
        notifyDataSetChanged()
    }

    interface SelectPractice {
        fun practice(status:Boolean,name: String,check:Boolean)
    }

}