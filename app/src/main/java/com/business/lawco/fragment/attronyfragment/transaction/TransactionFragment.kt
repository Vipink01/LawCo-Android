package com.business.lawco.fragment.attronyfragment.transaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.attroney.TransactionAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentTransactionBinding
import com.business.lawco.model.TransactionModel
import com.business.lawco.networkModel.paymentManagement.PaymentManagementViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionFragment : BaseFragment(){
    lateinit var binding : FragmentTransactionBinding

    lateinit var sessionManager: SessionManager
    lateinit var paymentManagementViewModel : PaymentManagementViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentTransactionBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        paymentManagementViewModel  = ViewModelProvider(this)[PaymentManagementViewModel::class.java]
        binding.paymentManagementViewModel = paymentManagementViewModel

//        val callback: OnBackPressedCallback =
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    findNavController().navigate(R.id.action_transactionFragment_to_settingsFragment)
//                }
//            }
//
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        binding.arrowWhiteTransactions.setOnClickListener {
            findNavController().navigateUp()
        }

        if (!sessionManager.isNetworkAvailable()){
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        }else {
            getTransactionList()
        }
    }


    private fun getTransactionList(){
        showMe()
        lifecycleScope.launch {
            paymentManagementViewModel.getTransactionList().observe(viewLifecycleOwner){jsonObject ->
                dismissMe()
                val jsonObjectData = sessionManager.checkResponse(jsonObject)
                if (jsonObjectData != null){
                    try {
                        val transactionData = Gson().fromJson(jsonObjectData, TransactionModel::class.java)
                        if (transactionData.data!=null && transactionData.data.isNotEmpty()){
                            binding.RcvTransactions.visibility = View.VISIBLE
                            binding.textNoDataFound.visibility = View.GONE
                            val transactionList = transactionData.data.reversed()
                            binding.RcvTransactions.adapter = TransactionAdapter(transactionList,requireActivity())
                            binding.RcvTransactions.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false)
                        }else{
                            binding.RcvTransactions.visibility = View.GONE
                            binding.textNoDataFound.visibility = View.VISIBLE
                        }
                    }catch (e :Exception){
                        Log.d("Error","****"+e.message)
                        binding.RcvTransactions.visibility = View.GONE
                        binding.textNoDataFound.visibility = View.VISIBLE
                    }
                }else{
                    binding.RcvTransactions.visibility = View.GONE
                    binding.textNoDataFound.visibility = View.VISIBLE
                }
            }
        }

    }

}