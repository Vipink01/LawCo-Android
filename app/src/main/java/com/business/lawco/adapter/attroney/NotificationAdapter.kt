package com.business.lawco.adapter.attroney

import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.databinding.NotificationItemsBinding
import com.business.lawco.model.NotificationData
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class NotificationAdapter(private var notificationList: ArrayList<NotificationData>,var requireContext : Activity):RecyclerView.Adapter<NotificationAdapter.Holder>() {

    class  Holder(val binding : NotificationItemsBinding):RecyclerView.ViewHolder(binding.root){


        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(dataItem: NotificationData, requireContext : Activity) {

            val progressDrawable = CircularProgressDrawable(requireContext).apply {
                strokeWidth = 5f
                centerRadius = 30f
                setColorSchemeColors(getColor(requireContext, R.color.orange))
                start()
            }

            Glide.with(requireContext)
                .load(dataItem.profile_picture)
                .placeholder(progressDrawable)
                .error(R.drawable.demo_user)
                .into(binding.profilePic)

            binding.message.text = dataItem.message

            dataItem.created_at?.let { date->
                binding.daysAgo.text = SessionManager(requireContext).formatDateTimeSafe(date)
            }


            /*if (dataItem.formatted_date!=null && dataItem.formatted_time!=null) {
                if (dataItem.formatted_date.equals("0 days ago")){
                    binding.daysAgo.text = "Today "+(dataItem.formatted_time?:"")//calculateTimeDifference(dataItem.created_at)
                }else{
                    binding.daysAgo.text = dataItem.formatted_time+" "+dataItem.formatted_time//calculateTimeDifference(dataItem.created_at)
                }
            }*/

        }


        private fun calculateTimeDifference(timeString: String): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val instant = Instant.parse(timeString)
                val givenTime =  instant.atZone(ZoneId.systemDefault())
                val currentTime = ZonedDateTime.now()

                val minutesDifference = ChronoUnit.MINUTES.between(givenTime, currentTime)

                return when {
                    minutesDifference >= 1440 -> {
                        val daysDifference = minutesDifference / 1440
                        "$daysDifference days ago ${formatTimeWithAmPm(givenTime)}"
                    }
                    minutesDifference >= 60 -> {
                        // Calculate the difference in hours
                        val hoursDifference = minutesDifference / 60
                        "$hoursDifference hours ago"
                    }
                    else -> "$minutesDifference min ago"
                }
            } else {
                return ""
            }


        }

        private fun formatTimeWithAmPm(zonedDateTime: ZonedDateTime): String? {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              val outputFormatter =  DateTimeFormatter.ofPattern("hh:mm a")
              return zonedDateTime.format(outputFormatter)
            } else {
                return  null
            }

        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = NotificationItemsBinding.inflate(LayoutInflater.from(parent.context),parent, false)

     return Holder(binding)
    }

    override fun getItemCount(): Int {
      return  notificationList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
      val dataItem = notificationList[position]
       holder.bind(dataItem ,  requireContext )
    }
}