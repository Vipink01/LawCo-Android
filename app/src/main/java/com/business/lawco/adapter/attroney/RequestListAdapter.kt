package com.business.lawco.adapter.attroney

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.business.lawco.R
import com.business.lawco.databinding.CreditsItemBinding
import com.business.lawco.model.RequestData
import com.business.lawco.utility.ValidationData

class RequestListAdapter(var datalist :  List<RequestData>, val requireActivity: FragmentActivity): RecyclerView.Adapter<RequestListAdapter.Holder>() {

    interface OnRequestAction {
        fun onRequestAction(position : Int ,requestId: String, action: String)
    }

    private var listener: OnRequestAction? = null

    fun setOnRequestAction(listener: OnRequestAction) {
        this.listener = listener
    }

    class Holder(val binding: CreditsItemBinding):RecyclerView.ViewHolder(binding.root){

       fun bind(dataItem: RequestData, requireActivity: FragmentActivity) {

           binding.tvConsumerName.text = dataItem.name?:"".replaceFirstChar { it.uppercase() }

//           binding.tvDistance.text = ValidationData.formatDistance(dataItem.distance.toDouble())
           val distanceValue = dataItem.distance?.toDoubleOrNull() ?: 0.0
           binding.tvDistance.text = ValidationData.formatDistance(distanceValue)


           if (dataItem.attorney_area_of_practice!=null) {
               binding.tvNeed.text = "Looking For "+dataItem.attorney_area_of_practice+ " Attorney"
           }

           val progressDrawable = CircularProgressDrawable(requireActivity).apply {
               strokeWidth = 5f
               centerRadius = 30f
               setColorSchemeColors(getColor(requireActivity, R.color.orange))
               start()
           }

           Glide.with(requireActivity)
               .load(/*AppConstant.BASE_URL + */dataItem.profile_picture_url)
               .placeholder(progressDrawable)
               .error(R.drawable.demo_user)
               .into(binding.tvProfile)

       }

   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = CreditsItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)

      return Holder(binding)
    }

    override fun getItemCount(): Int {
       return  datalist.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = datalist[position]

        holder.binding.btView.setOnClickListener(){
            listener?.onRequestAction(position,dataItem.request_id,"accepted")
        }

       /* holder.binding.btDecline.setOnClickListener(){
            listener?.onRequestAction(position,dataItem.request_id,"rejected")
        }
*/
        holder.bind(dataItem,requireActivity)
    }


}