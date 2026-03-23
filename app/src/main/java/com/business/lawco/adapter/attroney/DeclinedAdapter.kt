package com.business.lawco.adapter.attroney

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.lawco.R
import com.business.lawco.databinding.DeclinedCreditsItemsBinding

import com.business.lawco.model.RequestData
import com.business.lawco.utility.ValidationData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DeclinedAdapter(private var deniedList: ArrayList<RequestData>, var requireActivity: FragmentActivity) : RecyclerView.Adapter<DeclinedAdapter.Holder>() {

    class Holder(val binding: DeclinedCreditsItemsBinding): RecyclerView.ViewHolder(binding.root){



        @SuppressLint("SetTextI18n")
        fun bind(deniedItem: RequestData, requireActivity: FragmentActivity) {

            if (deniedItem.name!=null){
                binding.tvConsumerName.text = deniedItem.name
            }

           // val dateAndTime = giveDateAndTime(deniedItem.updated_at)
            binding.tvDeniedDate.text = deniedItem.formatted_date
            binding.tvDeniedTime.text = deniedItem.formatted_time
            binding.tvDistance.text = ValidationData.formatDistance(deniedItem.distance?.toDouble()?:0.0)

            if (deniedItem.profile_picture_url!=null){
                Glide.with(requireActivity)
                    .load(/*AppConstant.BASE_URL +*/ deniedItem.profile_picture_url)
                    .placeholder(R.drawable.demo_user)
                    .into(binding.tvProfile)
            }
        }

        private fun giveDateAndTime(inputDate: String): Pair<String, String> {
            val inputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            inputDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date: Date? = inputDateFormat.parse(inputDate)
            val outputDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            outputDateFormat.timeZone = TimeZone.getDefault()
            val outputTimeFormat = SimpleDateFormat("hh:mma", Locale.getDefault())
            outputTimeFormat.timeZone = TimeZone.getDefault()
            val formattedDate = outputDateFormat.format(date!!)
            val formattedTime = outputTimeFormat.format(date).replace("AM", "AM").replace("PM", "PM")
            return Pair(formattedDate, formattedTime)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = DeclinedCreditsItemsBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return  Holder(binding)
    }

    override fun getItemCount(): Int {
        return deniedList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val deniedItem = deniedList[position]
        holder.bind(deniedItem,requireActivity)

    }



}