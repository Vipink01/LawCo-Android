package com.business.lawco.adapter.consumer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.business.lawco.R
import com.business.lawco.databinding.CatogeriesItemBinding
import com.business.lawco.model.consumer.AreaOfPractice
import com.business.lawco.utility.AppConstant

class CategoriesAdapter(var categoryList: ArrayList<AreaOfPractice>, var requireContext: Context, private var source : String) :
    RecyclerView.Adapter<CategoriesAdapter.Holder>() {
    class Holder(val binding: CatogeriesItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dataItem: AreaOfPractice ,  requireContext: Context) {


            val progressDrawable = CircularProgressDrawable(requireContext).apply {
                strokeWidth = 5f
                centerRadius = 30f
                setColorSchemeColors(getColor(requireContext, R.color.orange))
                start()
            }

            Glide.with(requireContext)
                .load(AppConstant.BASE_URL + dataItem.category_image)
                .placeholder(progressDrawable)
                .error(R.drawable.adoption_credit_card)
                .into(binding.ImageCatogeries)

            binding.tvCategoryName.text = dataItem.category_name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = CatogeriesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(filterList: ArrayList<AreaOfPractice>) {
        categoryList = filterList
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = categoryList[position]
        holder.bind(dataItem,requireContext)

        holder.itemView.setOnClickListener {
            val categoryList = ArrayList<String>()
            categoryList.add(dataItem.category_name)
            val addressList = ArrayList<String>()
            val bundle = Bundle()
            bundle.putStringArrayList(AppConstant.AREA_OF_PRACTICE_LIST, categoryList)
            bundle.putStringArrayList(AppConstant.ADDRESS_LIST, addressList)
            bundle.putString(AppConstant.FILTER_PAGE_NAME, dataItem.category_name + " Attorneys")
           /* if (source == "home"){
                Navigation.findNavController(holder.itemView).navigate(R.id.selectedAttorneyFragment, bundle)
            }else{*/
            holder.itemView.findNavController().navigate(R.id.selectedAttorneyFragment, bundle)
            //}
         }

    }
}