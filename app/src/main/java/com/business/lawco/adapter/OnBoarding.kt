package com.business.lawco.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.databinding.LayoutItemOnbaordingBinding
import com.business.lawco.model.OnBordingModel

class OnBoarding( var dataList: ArrayList<OnBordingModel>) : RecyclerView.Adapter<OnBoarding.ViewHolder>() {
    class ViewHolder(val binding: LayoutItemOnbaordingBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(textList: OnBordingModel) {
            binding.tvItem.text = textList.tvItem
            binding.tvTitle.text = textList.tvTitle
            binding.ivItem.setImageResource(textList.ivItem)
        }

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
      val binding =  LayoutItemOnbaordingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() : Int {
        return 3
    }
}


