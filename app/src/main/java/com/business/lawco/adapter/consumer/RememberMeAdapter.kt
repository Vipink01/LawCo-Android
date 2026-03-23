package com.business.lawco.adapter.consumer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.databinding.SelectAttorneyItemBinding
import com.business.lawco.model.consumer.RememberMe
import com.business.lawco.model.consumer.SelectAreaOfPracticeModel

class RememberMeAdapter(
    private var selectAttorneyList: List<RememberMe>,
    var requireContext: Context,
    var select:RememberSelect
) : RecyclerView.Adapter<RememberMeAdapter.Holder>() {
    var selectedItemPos = -1
    var lastItemSelectedPos = -1
    class Holder(val binding: SelectAttorneyItemBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(dataItem: RememberMe) {
            binding.checkBoxAttorney.text = dataItem.email
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
        holder.binding.checkBoxAttorney.isChecked = position == selectedItemPos
        holder.binding.checkBoxAttorney.setOnClickListener {
            selectedItemPos = holder.getAdapterPosition()
            if (lastItemSelectedPos == -1)
                lastItemSelectedPos = selectedItemPos
            else {
                notifyItemChanged(lastItemSelectedPos)
                lastItemSelectedPos = selectedItemPos
            }
            notifyItemChanged(selectedItemPos)
            select.selectRemember(selectAttorneyList.get(position))
        }
        holder.bind(selectAttorneyList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<SelectAreaOfPracticeModel>) {
        selectAttorneyList = newData.toMutableList() as ArrayList<RememberMe>
        notifyDataSetChanged()
    }
}

interface RememberSelect {
    fun selectRemember(remember:RememberMe)

}
