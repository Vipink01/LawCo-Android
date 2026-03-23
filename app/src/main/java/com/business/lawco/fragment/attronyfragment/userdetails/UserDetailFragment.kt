package com.business.lawco.fragment.attronyfragment.userdetails

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.lawco.R
import com.business.lawco.adapter.ImageShowAdapter
import com.business.lawco.databinding.FragmentUserDetailBinding
import com.business.lawco.model.RequestData
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.ValidationData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserDetailFragment : Fragment()  {
    lateinit var binding: FragmentUserDetailBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserDetailBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_userDetailFragment_to_logFragment)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        val arrayListJson = requireArguments().getString(AppConstant.CONSUMER_PROFILE)
        val type = object : TypeToken<RequestData>() {}.type
        val consumerDetail: RequestData =  Gson().fromJson(arrayListJson, type)

        binding.tvConsumerName.text =
            consumerDetail.name
                ?.trim()
                ?.replaceFirstChar { it.uppercase() }
                ?: ""


        binding.tvConsumerPhone.text = consumerDetail.phone

        binding.tvConsumerEmail.text = consumerDetail.email

        val distanceValue = consumerDetail.distance?.toDoubleOrNull() ?: 0.0

        binding.tvConsumerDistance.text = ValidationData.formatDistance(distanceValue)

        if (consumerDetail.attorney_area_of_practice!=null) {
            binding.tvConsmerNeed.text = "Looking For "+consumerDetail.attorney_area_of_practice+ " Attorney"
        }

        binding.tvConsumerAddress.text = consumerDetail.address

        Glide.with(requireActivity())
            .load(consumerDetail.profile_picture_url)
            .placeholder(R.drawable.demo_user)
            .into(binding.tvConsumerProfile)

        binding.arrowWhite.setOnClickListener {
            findNavController().navigate(R.id.action_userDetailFragment_to_logFragment)
        }

        binding.btnRequest.setOnClickListener {
            showView(consumerDetail)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showView(consumerDetail: RequestData) {
        val requestDialog = Dialog(requireContext())
        requestDialog.setContentView(R.layout.request_dialog)
        requestDialog.setCancelable(false)
        requestDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val etCardNumber: EditText = requestDialog.findViewById(R.id.etCardNumber)
        val etSubject: EditText = requestDialog.findViewById(R.id.etSubject)
        val btnYes: TextView = requestDialog.findViewById(R.id.yes)
        val tvDescription: TextView = requestDialog.findViewById(R.id.tvDescription)
        val tvTitle: TextView = requestDialog.findViewById(R.id.tvTitle)
        val tvUpload: TextView = requestDialog.findViewById(R.id.tvUpload)
        val btnCancel: TextView = requestDialog.findViewById(R.id.Cancel)
        val tvInfo: TextView = requestDialog.findViewById(R.id.tvInfo)
        val tvDownload: TextView = requestDialog.findViewById(R.id.tvDownload)
        val rcyData: RecyclerView = requestDialog.findViewById(R.id.rcyData)
        val imgUpload: ImageView = requestDialog.findViewById(R.id.imgUpload)
        val imgClose: ImageView = requestDialog.findViewById(R.id.imgClose)
        val btnShow: LinearLayout = requestDialog.findViewById(R.id.btnShow)
        tvDescription.text="Legal Problem Description"
        tvTitle.text="Request Details"
        tvUpload.text="Uploaded Documents"
        imgUpload.visibility = View.GONE
        tvInfo.visibility = View.GONE
        btnShow.visibility = View.GONE
        tvUpload.visibility = View.VISIBLE
        tvDownload.visibility = View.VISIBLE
        etCardNumber.isEnabled = false
        etSubject.isEnabled = false

        consumerDetail.subject?.let {
            etSubject.setText(it)
        }
        consumerDetail.description?.let {
            etCardNumber.setText(it)
        }
        consumerDetail.documents?.let {list->
            if (list.isNotEmpty()){
                rcyData.adapter= ImageShowAdapter(requireContext(),list)
            }
        }
        btnYes.setOnClickListener {
            requestDialog.dismiss()
        }
        btnCancel.setOnClickListener {
            requestDialog.dismiss()
        }
        imgClose.setOnClickListener {
            requestDialog.dismiss()
        }
        requestDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        requestDialog.show()
    }

}