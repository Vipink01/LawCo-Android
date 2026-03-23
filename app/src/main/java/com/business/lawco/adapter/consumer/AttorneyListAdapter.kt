package com.business.lawco.adapter.consumer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.databinding.ListOfAttorneyItemBinding
import com.business.lawco.model.consumer.AttorneyProfile
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.ValidationData
import androidx.navigation.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable

class AttorneyListAdapter(
    private var attorneyList: List<AttorneyProfile>,
    var requireContext: Context
) : RecyclerView.Adapter<AttorneyListAdapter.Holder>() {


    interface OnSendRequest {
        fun onSendRequest(position: Int, attorneyId: String, action: String)
    }

    private var listener: OnSendRequest? = null

    fun setOnSendRequest(listener: OnSendRequest) {
        this.listener = listener
    }

    class Holder(val binding: ListOfAttorneyItemBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(dataItem: AttorneyProfile, requireContext: Context) {

            binding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(AppConstant.ATTORNEY_PROFILE, Gson().toJson(dataItem))
                it.findNavController().navigate(R.id.action_consumerHomeFragment_to_attorneyDetailsFragment, bundle)
            }

            binding.tvAttorneyName.text = dataItem.full_name.replaceFirstChar { it.uppercase() }

            binding.tvTypeofAttorney.text = dataItem.area_of_practice+" Attorney"

            if (dataItem.distance != null) {
                binding.tvMetersAway.text = ValidationData.formatDistance(dataItem.distance.toDouble())
            }

            if (dataItem.online_status == 1) {
                binding.showActive.visibility = View.VISIBLE
            }else{
                binding.showActive.visibility = View.INVISIBLE
            }

            val progressDrawable = CircularProgressDrawable(requireContext).apply {
                strokeWidth = 5f
                centerRadius = 30f
                setColorSchemeColors(getColor(requireContext, R.color.orange))
                start()
            }
            Glide.with(requireContext)
                .load(dataItem.profile_picture_url)
                .placeholder(progressDrawable)
                .error(R.drawable.demo_user)
                .fallback(R.drawable.demo_user)
                .into(binding.tvProfile)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ListOfAttorneyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)

    }

    override fun getItemCount(): Int {
        return attorneyList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = attorneyList[position]

       /* if (dataItem.connected == 1) {
            holder.binding.btnConnect.visibility = View.GONE
        } else {
            holder.binding.btnConnect.visibility = View.VISIBLE
        }*/

        if (dataItem.request == 0) {
            holder.binding.btnConnect.text = "Connect"
            /*holder.binding.btnConnect.setBackgroundResource(R.drawable.orange_button_identity)
            holder.binding.btnConnect.setTextColor(getColor(requireContext, R.color.white))*/
        } else {
            holder.binding.btnConnect.text = "Requested"
           /* holder.binding.btnConnect.setBackgroundResource(R.drawable.sent_bg)
            holder.binding.btnConnect.setTextColor(getColor(requireContext, R.color.black))*/
        }

        holder.binding.btnConnect.setOnClickListener {
            if (dataItem.request == 0) {
                listener?.onSendRequest(position, dataItem.id.toString(), "1")
            } else {
                Toast.makeText(requireContext,R.string.already_req,Toast.LENGTH_SHORT).show()
            }
        }

        holder.bind(dataItem, requireContext)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(filterList: List<AttorneyProfile>) {
        attorneyList = filterList
        notifyDataSetChanged()
    }

}