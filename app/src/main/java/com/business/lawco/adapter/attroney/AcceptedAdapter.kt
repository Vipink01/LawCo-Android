package com.business.lawco.adapter.attroney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.databinding.AcceptedCreditsItemsBinding
import com.business.lawco.model.RequestData
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.ValidationData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.navigation.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.business.lawco.utility.OnItemSelectListener
import kotlinx.coroutines.CoroutineScope

class AcceptedAdapter(
    private val acceptedList: ArrayList<RequestData>,
    var requireActivity: FragmentActivity,
    var type: String,
    var OnItemSelectListener: OnItemSelectListener
) :
    RecyclerView.Adapter<AcceptedAdapter.Holder>() {

    class Holder(val binding: AcceptedCreditsItemsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(acceptedItem: RequestData, requireActivity: FragmentActivity) {

            if (acceptedItem.name!=null){
                binding.tvConsumerName.text = acceptedItem.name?:"".replaceFirstChar { it.uppercase() }
            }

          //  val dateAndTime = giveDateAndTime(acceptedItem.updated_at)
            binding.tvAppliedDate.text = acceptedItem.formatted_date
            binding.tvAppliedTime.text =  acceptedItem.formatted_time

            val distanceValue = acceptedItem.distance
                ?.toDoubleOrNull()
                ?: 0.0
            binding.tvDistance.text = ValidationData.formatDistance(distanceValue)
          //  binding.tvDistance.text =  ValidationData.formatDistance(acceptedItem.distance.toDouble())


            val progressDrawable = CircularProgressDrawable(requireActivity).apply {
                strokeWidth = 5f
                centerRadius = 30f
                setColorSchemeColors(getColor(requireActivity, R.color.orange))
                start()
            }

            Glide.with(requireActivity)
                .load(/*AppConstant.BASE_URL + */acceptedItem.profile_picture_url)
                .placeholder(progressDrawable)
                .error(R.drawable.demo_user)
                .into(binding.tvProfile)

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
        val binding = AcceptedCreditsItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)

    }

    override fun getItemCount(): Int {
        return acceptedList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val acceptedItem = acceptedList[position]




        if (type.equals("1",true)){
            holder.itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(AppConstant.CONSUMER_PROFILE,Gson().toJson(acceptedItem))
                it.findNavController().navigate(R.id.action_logFragment_to_userDetailFragment,bundle)
            }
            holder.binding.btOpenProfile.visibility = View.VISIBLE
        }else{
            holder.binding.btOpenProfile.visibility = View.GONE
        }

        holder.binding.btnRequest.setOnClickListener {
            OnItemSelectListener.itemSelect(position,acceptedItem.request_id,type)
        }

        holder.bind(acceptedItem, requireActivity)

    }

}



