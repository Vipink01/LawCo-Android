package com.business.lawco.adapter.consumer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.databinding.SelectCountryItemBinding
import com.business.lawco.model.consumer.Data

class SelectAddressAdapter(private var selectCountryList: MutableList<Data>, var requireContext: Context, val selectLocation: SelectLocation) : RecyclerView.Adapter<SelectAddressAdapter.Holder>() {
    var selectedItemPos = -1
    var lastItemSelectedPos = -1

    class Holder(val binding: SelectCountryItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dataItem: Data) {
            if (dataItem.address != null) {
                binding.checkboxCountry.text = dataItem.address
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            SelectCountryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return selectCountryList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: Holder, position: Int) {
//        holder.binding.checkboxCountry.isChecked = position == selectedItemPos

//        if (selectCountryList[position].locationStatus){
//            selectLocation.location(selectCountryList[position])
//        }

        if (position==selectedItemPos){
            holder.binding.checkboxCountry.isChecked = true
            selectLocation.location(selectCountryList[position])
        }else{
            holder.binding.checkboxCountry.isChecked = false
        }

        holder.binding.checkboxCountry.setOnClickListener {
            selectedItemPos = holder.adapterPosition
            if (lastItemSelectedPos == -1)
                lastItemSelectedPos = selectedItemPos
            else {
                notifyItemChanged(lastItemSelectedPos)
                lastItemSelectedPos = selectedItemPos
            }
            notifyItemChanged(selectedItemPos)

        }

        holder.bind(selectCountryList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<Data>) {
        selectCountryList = newData.toMutableList() as ArrayList<Data>
        notifyDataSetChanged()
    }

}

interface SelectLocation {
    fun location(dataItem: Data)
}
