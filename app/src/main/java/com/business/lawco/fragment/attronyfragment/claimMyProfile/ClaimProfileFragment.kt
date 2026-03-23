package com.business.lawco.fragment.attronyfragment.claimMyProfile

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.attroney.AttorneyProfileAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentClaimProfileBinding
import com.business.lawco.model.claimprofilemodellist.AttorneyListModel
import com.business.lawco.model.claimprofilemodellist.Data
import com.business.lawco.networkModel.claimProfile.ClaimProfileViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ClaimProfileFragment : BaseFragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var binding: FragmentClaimProfileBinding
    private lateinit var claimProfileViewModel: ClaimProfileViewModel
    private lateinit var attorneyProfileAdapter: AttorneyProfileAdapter
    private lateinit var textListener: TextWatcher
    private var textChangedJob: Job? = null

    private var dataList: MutableList<Data> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentClaimProfileBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        claimProfileViewModel = ViewModelProvider(requireActivity())[ClaimProfileViewModel::class.java]
        setupRecyclerView()
        setupSearchListener()
        claimProfileViewModel.dataList.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()){
                attorneyProfileAdapter.updateProfileList(dataList)
                showEmptyState(true)
            }else{
                showEmptyState(false)
            }
        }
        binding.btBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    private fun setupRecyclerView() {
        attorneyProfileAdapter = AttorneyProfileAdapter(dataList, requireActivity())
        binding.rvSearchResults.adapter = attorneyProfileAdapter
        attorneyProfileAdapter.setOnClaimProfile(object : AttorneyProfileAdapter.OnClaimProfile {
            override fun onClaimProfile(position: Int, profileId: String) {
                showClaimConfirmationDialog( profileId)
            }
        })
    }
    private fun setupSearchListener() {
        textListener = object : TextWatcher {
            private var searchFor = "" // Or view.editText.text.toString()
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (searchText != searchFor) {
                    searchFor = searchText
                    textChangedJob?.cancel()
                    // Launch a new coroutine in the lifecycle scope
                    textChangedJob = lifecycleScope.launch {
                        delay(1000)  // Debounce time
                        if (searchText.equals(searchFor,true)) {
                            searchApi(searchText)
                        }else{
                            showEmptyState(false)
                        }
                    }
                }else{
                    showEmptyState(false)
                }
            }
        }
    }
    private fun searchApi(value:String){
        if (!sessionManager.isNetworkAvailable()) {
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        } else {
            binding.loader.visibility= View.VISIBLE
            lifecycleScope.launch {
                // Log the final JSON data
                Log.d("final data", "******$value")
                claimProfileViewModel.searchAttorneyList(value).observe(viewLifecycleOwner) { jsonObject ->
                    binding.loader.visibility= View.GONE
                    Log.d("API_RESPONSE", jsonObject.toString())
                    val jsonObjectData = sessionManager.checkResponseHidemessage(jsonObject)
                    Log.e("Get Profile", "True")
                    if (jsonObjectData != null) {
                        try {
                            val apiModel = Gson().fromJson(jsonObject.response, AttorneyListModel::class.java)
                            dataList.clear()
                            apiModel.data?.let {
                                dataList.addAll(apiModel.data)
                            }
                            claimProfileViewModel.setDataList(dataList)
                            if (dataList.isNotEmpty()){
                                attorneyProfileAdapter.updateProfileList(dataList)
                                showEmptyState(true)
                            }else{
                                showEmptyState(false)
                            }
                        } catch (e: Exception) {
                            showEmptyState(false)
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
            }
        }
    }

    private fun showEmptyState(status: Boolean) {
        if (status){
            binding.rvSearchResults.visibility = View.VISIBLE
            binding.emptyStateContainer.visibility = View.GONE
        }else{
            binding.rvSearchResults.visibility = View.GONE
            binding.emptyStateContainer.visibility = View.VISIBLE
        }
    }

    private fun showClaimConfirmationDialog( profileId: String) {
        val confirmDialog = Dialog(requireContext())
        confirmDialog.setContentView(R.layout.alert_claim_confirmation_dialog)
        confirmDialog.setCancelable(false)
        confirmDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val textCancel = confirmDialog.findViewById<TextView>(R.id.textCancel)
        val textProceed = confirmDialog.findViewById<TextView>(R.id.textProceed)

        textCancel.setOnClickListener {
            confirmDialog.dismiss()
        }

        textProceed.setOnClickListener {
            confirmDialog.dismiss()
            val dataItem = dataList.find { it.id == profileId.toInt() }
            dataItem?.let {
                claimProfileViewModel.selectAttorney(dataItem)
                claimProfileViewModel.updatePhoneStatus(false)
                claimProfileViewModel.updateEmailStatus(false)
                findNavController().navigate(R.id.action_claimProfileFragment_to_claimProfileDetailsFragment)
            }
        }

        confirmDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        confirmDialog.show()
    }

    override fun onResume() {
        super.onResume()
        binding.etSearch.addTextChangedListener(textListener)
    }

    override fun onPause() {
        binding.etSearch.removeTextChangedListener(textListener)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        claimProfileViewModel.clearAllData()
    }

}

