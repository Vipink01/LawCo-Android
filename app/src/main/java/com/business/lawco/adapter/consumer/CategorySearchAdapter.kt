package com.business.lawco.adapter.consumer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.R
import com.business.lawco.databinding.ItemViewBinding
import com.business.lawco.model.consumer.AreaOfPractice
import com.business.lawco.utility.AppConstant

class CategorySearchAdapter(private var previousList:ArrayList<AreaOfPractice>, var reaquireContext: Context):RecyclerView.Adapter<CategorySearchAdapter.Holder>() {
    class  Holder(val binding:ItemViewBinding):RecyclerView.ViewHolder(binding.root){

        fun bind(dataItem: AreaOfPractice) {
           binding.TextPreviousData.text = dataItem.category_name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
       return  previousList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
       val dataItem = previousList[position]
        holder.bind(dataItem)

        holder.itemView.setOnClickListener{
          //  val bundle = Bundle()
          //  bundle.putString("category", dataItem.category_name)
          //  Navigation.findNavController(holder.itemView).navigate(R.id.action_consumerCategoryFragment_to_selectedAttorneyFragment, bundle)
            val categoryList = ArrayList<String>()
            categoryList.add(dataItem.category_name)
            val addressList = ArrayList<String>()
            val bundle = Bundle()
            bundle.putStringArrayList(AppConstant.AREA_OF_PRACTICE_LIST, categoryList)
            bundle.putStringArrayList(AppConstant.ADDRESS_LIST, addressList)
            bundle.putString(AppConstant.FILTER_PAGE_NAME, dataItem.category_name + " Attorneys")
            Navigation.findNavController(holder.itemView).navigate(R.id.action_consumerCategoryFragment_to_selectedAttorneyFragment, bundle)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(filterList: ArrayList<AreaOfPractice>) {
        previousList = filterList
        notifyDataSetChanged()
    }
}