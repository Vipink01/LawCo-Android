package com.business.lawco.adapter.attroney

import android.annotation.SuppressLint
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.business.lawco.R
import com.business.lawco.databinding.SavedAddressItemBinding
import com.business.lawco.model.Address

class SavedAddressAdapter(
    private var addressList: List<Address>,
    private val requireActivity: FragmentActivity,
    private val selectaddress: SelectAddress
) : RecyclerView.Adapter<SavedAddressAdapter.Holder>() {

    interface OnMenuItemClick {
        fun editAddress(addressId: String, latitude: String, longitude: String)
        fun deleteAddress(addressId: String)
        fun defaultAddress(addressId: String)
    }

    private var listener: OnMenuItemClick? = null

    fun setOnMenuItemClick(listener: OnMenuItemClick) {
        this.listener = listener
    }

    class Holder(val binding: SavedAddressItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dataItem: Address) {
            binding.tvAddressType.text = dataItem.address_type
            binding.tvAddress.text = dataItem.address

            if (dataItem.address_type.uppercase() == "HOME"){
                binding.tvOfficeTypeIcon.setImageResource(R.drawable.home_address_icon)
            }else{
                binding.tvOfficeTypeIcon.setImageResource(R.drawable.office_building_blackvector)
            }


            if (dataItem.address_type == "default"){
                binding.tvDefault.visibility = View.VISIBLE
            }else{
                binding.tvDefault.visibility = View.GONE
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            SavedAddressItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val dataItem = addressList[position]
        holder.bind(dataItem)

        holder.binding.btMenuIcon.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireActivity, view)
            popupMenu.menuInflater.inflate(R.menu.edit_delete_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> {
                        listener?.editAddress(dataItem.id, dataItem.latitude.toString(), dataItem.longitude.toString())
                    }

                    R.id.default_address -> {
                        if (dataItem.address_type != "default"){
                            Log.e("Address Type",dataItem.address_type)
                            listener?.defaultAddress(dataItem.id)
                        }
                    }

                    R.id.delete -> {
                        listener?.deleteAddress(dataItem.id)
                    }
                }

                return@setOnMenuItemClickListener true
            }

            popupMenu.gravity = Gravity.END

            popupMenu.show()
        }

        holder.binding.selectLocation.setOnClickListener {
            selectaddress.select(dataItem.latitude.toString(),
                dataItem.longitude.toString())

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAddressList(newAddressList: ArrayList<Address>) {
        addressList = newAddressList
        notifyDataSetChanged()
    }



}

 interface SelectAddress {
        fun select(lat:String,lng:String)
}
