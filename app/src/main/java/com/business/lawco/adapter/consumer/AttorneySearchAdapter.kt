package com.business.lawco.adapter.consumer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.databinding.AttorneyPreviousListBinding
import com.business.lawco.model.consumer.AttorneyProfile
import com.business.lawco.utility.AppConstant

class AttorneySearchAdapter(private var previousAttorneyList: List<AttorneyProfile>, var requireContext: Context, private val source: String) : RecyclerView.Adapter<AttorneySearchAdapter.Holder>() {
    private var cardView:CardView?=null
    private var etSearch:EditText?=null
    class Holder(val binding: AttorneyPreviousListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dataItem: AttorneyProfile) {
            binding.TextPreviousAttorneyData.text = dataItem.full_name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            AttorneyPreviousListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return previousAttorneyList.size
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = previousAttorneyList[position]
        holder.bind(dataItem)
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(AppConstant.ATTORNEY_PROFILE, Gson().toJson(dataItem))
            if (cardView!=null){
                cardView?.visibility = View.GONE
            }
            if (etSearch!=null){
                etSearch?.text?.clear()
            }
            if (source == AppConstant.HOME) {
                Navigation.findNavController(it).navigate(R.id.action_consumerHomeFragment_to_attorneyDetailsFragment, bundle)
            }

            if (source == AppConstant.CONNECTED) {
                Navigation.findNavController(it).navigate(R.id.action_connectionsFragment_to_attorneyDetailsFragment, bundle)
            }

            if (source == AppConstant.FILTER) {
                Navigation.findNavController(it).navigate(R.id.action_selectedAttorneyFragment_to_attorneyDetailsFragment, bundle)
            }

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(filterList: List<AttorneyProfile>, cardViewforAttorneyPrevious: CardView, etSearch: EditText) {
        previousAttorneyList = filterList
        cardView = cardViewforAttorneyPrevious
        this.etSearch = etSearch
        notifyDataSetChanged()
    }

}