package com.business.lawco.adapter.attroney

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.R
import com.business.lawco.databinding.TransactionsItemsBinding
import com.business.lawco.model.TransactionDetail
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class TransactionAdapter(
    private var transactionList: List<TransactionDetail>,
    var requireActivity: FragmentActivity
) : RecyclerView.Adapter<TransactionAdapter.Holder>() {
    class Holder(val binding: TransactionsItemsBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(transactionModel: TransactionDetail) {

           binding.tvUserName.text = capitalizeFirstLetter(transactionModel.name)
            binding.tvTransactionType.text = transactionModel.subscription_type
            if (transactionModel.formatted_date!=null) {
                binding.tvTransactionDate.text =
                    transactionModel.formatted_time + " | " + transactionModel.formatted_date//formatDateTime(transactionModel.created_at)
            }
            val price = String.format("%.2f", transactionModel.price?.toDouble())
            binding.tvTransactionAmount.text = "$"+price
        }

        private fun capitalizeFirstLetter(name: String): String {
            if (name.isEmpty()) {
                return name
            }
            return name.substring(0, 1).uppercase()
        }

        private fun formatDateTime(dateTimeString: String): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val zonedDateTime = ZonedDateTime.parse(dateTimeString)
                val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
                val formattedTime = zonedDateTime.format(timeFormatter)
                val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
                val formattedDate = zonedDateTime.format(dateFormatter)
                "$formattedTime | $formattedDate"
            } else {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.ENGLISH)
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dateTimeString)
                val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                val formattedTime = timeFormat.format(date!!)
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                val formattedDate = dateFormat.format(date)
                "$formattedTime | $formattedDate"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            TransactionsItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = transactionList[position]

        if (position % 2 == 0) {
            holder.binding.tvOvalBg.setBackgroundResource(R.drawable.yellow_oval_bg)
        } else {
            holder.binding.tvOvalBg.setBackgroundResource(R.drawable.green_oval_bg)
        }

        holder.bind(dataItem)
    }
}