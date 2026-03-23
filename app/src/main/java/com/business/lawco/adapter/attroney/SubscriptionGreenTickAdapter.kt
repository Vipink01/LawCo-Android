package com.business.lawco.adapter.attroney

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.databinding.SubcriptionGreenTickListBinding

class SubscriptionGreenTickAdapter(
    private var pointList: ArrayList<String>,
    var requireActivity: Context
) : RecyclerView.Adapter<SubscriptionGreenTickAdapter.Holder>() {

    class Holder(val binding: SubcriptionGreenTickListBinding) : RecyclerView.ViewHolder(binding.root) {



        fun bind(dataItem: String) {

            binding.description.text = dataItem

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = SubcriptionGreenTickListBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return pointList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bind(pointList[position])
    }


}