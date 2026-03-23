package com.business.lawco.adapter.consumer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.business.lawco.R
import com.business.lawco.databinding.ConnectionsListBinding
import com.business.lawco.model.consumer.ConnectionsDataModel
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.ValidationData
import com.google.gson.Gson

class ConnectionAdapter(private var connectionList: List<ConnectionsDataModel>, var requireContext: Context) :
    RecyclerView.Adapter<ConnectionAdapter.Holder>() {

    class Holder(val binding: ConnectionsListBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(dataItem: ConnectionsDataModel, requireContext: Context) {
            binding.viewClick.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(AppConstant.ATTORNEY_PROFILE, Gson().toJson(dataItem))
                it.findNavController().navigate(R.id.action_connectionsFragment_to_attorneyDetailsFragment, bundle)
            }
            if (dataItem.profile_picture_url!=null){
                val progressDrawable = CircularProgressDrawable(requireContext).apply {
                    strokeWidth = 5f
                    centerRadius = 30f
                    setColorSchemeColors(
                        ContextCompat.getColor(requireContext, R.color.orange)
                    )
                    start()
                }
                Glide.with(requireContext)
                    .load(/*AppConstant.BASE_URL + */dataItem.profile_picture_url)
                    .placeholder(progressDrawable)
                    .into(binding.tvProfile)
            }else{
                binding.tvProfile.setImageResource(R.drawable.demo_user)
            }
            if (dataItem.full_name!=null){
                binding.tvAttorneyName.text = dataItem.full_name.replaceFirstChar { it.uppercase() }
            }
            if (dataItem.area_of_practice!=null) {
                binding.tvTypeofAttorney.text = dataItem.area_of_practice+" Attorney"
            }
            if (dataItem.distance != null){
                binding.tvDistance.text = ValidationData.formatDistance(dataItem.distance.toDouble())
            }
            if (dataItem.accepted_request_updated_at!=null && dataItem.accepted_request_updated_time!=null) {
                binding.tvAcceptedDate.text = dataItem.accepted_request_updated_time+" | "+dataItem.accepted_request_updated_at//"6:00PM | Jan 16,2024 "
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ConnectionsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return connectionList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = connectionList[position]
        holder.bind(dataItem, requireContext)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<ConnectionsDataModel>) {
        connectionList = list
        notifyDataSetChanged()
    }

}