package com.business.lawco.adapter.attroney

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.databinding.LayoutItemSubscriptionsBinding
import com.business.lawco.model.SubcriptionData
import java.lang.Integer.min
import android.graphics.Paint
import kotlin.collections.set


class SubscriptionAdpter (private var dataList: ArrayList<SubcriptionData>, private var requireContext : Context): RecyclerView.Adapter<SubscriptionAdpter.SubViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubViewHolder {
        val binding = LayoutItemSubscriptionsBinding.inflate(LayoutInflater.from(parent.context),parent, false)

        return SubViewHolder(binding)
    }
    override fun onBindViewHolder(holder: SubViewHolder, position: Int) {
        holder.bind(dataList[position] , requireContext ,position)

    }

    override fun getItemCount(): Int {
        return min(dataList.size, 3)

    }

    class SubViewHolder(val binding : LayoutItemSubscriptionsBinding): RecyclerView.ViewHolder(binding.root){


        private lateinit var adapter1: SubscriptionGreenTickAdapter



        @SuppressLint("SetTextI18n")
        fun bind(
            subcriptionData: SubcriptionData,
            requireContext: Context,
            position: Int
        ){
            binding.tvSubcriptionName.text =subcriptionData.title
            binding.tvSubcriptionPrice.text = "$ ${subcriptionData.price}/"

            if (subcriptionData.price_show== 1 && position==0){
                binding.tvSubcriptionPrice1.visibility = View.VISIBLE
                binding.tvSubcriptionPrice1.text = "$ ${subcriptionData.price}/"

                // 🔥 STRIKE THROUGH
                binding.tvSubcriptionPrice1.paintFlags =
                    binding.tvSubcriptionPrice1.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvSubcriptionPrice.text = "$ 49.00/"

            }else {
                binding.tvSubcriptionPrice1.visibility = View.GONE
                binding.tvSubcriptionPrice.text = "$ ${subcriptionData.price}/"
            }

            adapter1 = SubscriptionGreenTickAdapter(subcriptionData.description, requireContext)
            binding.rcyGreenTick.adapter = adapter1
            binding.rcyGreenTick.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
        }




    }
    fun updateList(updatedList: ArrayList<SubcriptionData>) {
        dataList = updatedList
        notifyDataSetChanged()
    }

}