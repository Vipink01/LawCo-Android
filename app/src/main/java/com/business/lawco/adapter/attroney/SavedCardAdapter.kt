package com.business.lawco.adapter.attroney

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.databinding.PaymentCardItemBinding
import com.business.lawco.model.CardDetails


class SavedCardAdapter(
    private var cardList: ArrayList<CardDetails>,
    var requireActivity: FragmentActivity,
    var payEnable : Int
) :
    RecyclerView.Adapter<SavedCardAdapter.Holder>() {

    interface OnRemoveCard {
        fun onRemoveCard(position : Int ,cardId: String)
    }

    private var listener: OnRemoveCard? = null

    fun setOnRemoveCard(listener: OnRemoveCard) {
        this.listener = listener
    }

    class Holder(val binding: PaymentCardItemBinding) : RecyclerView.ViewHolder(binding.root) {


        @SuppressLint("SetTextI18n")
        fun bind(dataItem: CardDetails) {

            binding.cardHolderName.text = dataItem.cardholdername
            binding.CardNumber.text = "**** **** **** ${dataItem.last4}"
            binding.expireDate.text = "${dataItem.exp_month}/${dataItem.exp_year.takeLast(2)}"


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = PaymentCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)


        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = cardList[position]

        holder.binding.btSelect.setOnCheckedChangeListener(null) // Clear existing listener
        holder.binding.btSelect.isChecked = dataItem.selectCard

        holder.binding.btSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cardList.forEachIndexed { index, dataItem ->
                    dataItem.selectCard = (index == position)
                }
            } else {
                dataItem.selectCard = false
            }
            notifyDataSetChanged()
        }

        holder.binding.btRemoveCard.setOnClickListener {
                listener?.onRemoveCard(position,dataItem.id.toString())
        }

        if (payEnable == 1){
            holder.binding.btSelect.visibility = View.VISIBLE
        }else{
            holder.binding.btSelect.visibility = View.GONE
        }

        holder.bind(dataItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCardList(newList: ArrayList<CardDetails>){
        cardList = newList
        notifyDataSetChanged()
    }

}