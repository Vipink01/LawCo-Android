package com.business.lawco.fragment.attronyfragment.subscription

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.attroney.SubscriptionAdpter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentSubscriptionsBinding
import com.business.lawco.model.SubcriptionData
import com.business.lawco.model.SubscriptionsModel
import com.business.lawco.networkModel.paymentManagement.PaymentManagementViewModel
import com.business.lawco.utility.AppConstant
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class SubscriptionsFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentSubscriptionsBinding
    private var subcriptionList: ArrayList<SubcriptionData> = arrayListOf()
    private lateinit var paymentViewModel: PaymentManagementViewModel
    lateinit var sessionManager: SessionManager
    private var subcriptionId: String? = null
    private var price: String? = null
    private var price_show: Int? = 0
    private lateinit var subscriptionAdapter: SubscriptionAdpter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSubscriptionsBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscriptionAdapter = SubscriptionAdpter(subcriptionList, requireContext())
        sessionManager = SessionManager(requireContext())
        paymentViewModel = ViewModelProvider(this)[PaymentManagementViewModel::class.java]
        binding.paymentManagementViewModel = paymentViewModel


        binding.viewpager.apply {
            clipChildren = false  // No clipping the left and right items
            clipToPadding = false  // Show the viewpager in full width without clipping the padding
            offscreenPageLimit = 3  // Render the left and right items
            (getChildAt(0) as RecyclerView).overScrollMode =
                RecyclerView.OVER_SCROLL_NEVER // Remove the scroll effect
        }

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(
            MarginPageTransformer(
                (7 * Resources.getSystem()
                    .displayMetrics.density).toInt()
            )
        )

        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = (0.80f + r * 0.20f)
        }
        binding.viewpager.setPageTransformer(compositePageTransformer)

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0 || position == 1 || position == 2) {
                    subcriptionId = subcriptionList[position].id.toString()

                    price_show = subcriptionList[position].price_show
                    price = if (price_show==1 && position == 0){
                        "49.00"
                    }else{
                        subcriptionList[position].price
                    }

                    updateButtonState(position)
                }
            }
        })

        binding.btnBuyOrNext.setOnClickListener(this)
        binding.arrowWhite.setOnClickListener(this)
        binding.btnCancel.setOnClickListener {

            val currentPosition = binding.viewpager.currentItem

            val currentSubscriptionId = subcriptionList[currentPosition].id.toString()
            //subcriptionId
            cancelSubAlertBox(currentPosition,currentSubscriptionId)

        }


    }

    override fun onClick(item: View?) {

        when (item!!.id) {
            R.id.arrowWhite -> {
                findNavController().navigateUp()
            }

            R.id.btnBuyOrNext -> {
               /* val currentPosition = binding.viewpager.currentItem
                val activePlanPosition = getActivePlanPosition()
                val purchaseCount=subcriptionList.count() { it.is_active }
                if (purchaseCount >0) {
                    Toast.makeText(requireActivity(), "Kindly cancel the previous plan and proceed with the upgrade to the latest plan.", Toast.LENGTH_SHORT).show()
                    return
                }*/
                openLogoutInBrowser(requireContext())
            }
        }
    }


    fun openLogoutInBrowser(context: Context) {
        val logoutUrl = "${AppConstant.BASE_URL}/web-payment?token=${sessionManager.getBearerToken()}&plan_id=${subcriptionId}"
        Log.d("openLogoutInBrowser", logoutUrl)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, logoutUrl.toUri())
    }




    private fun getSubscriptionList() {
        showMe()
        lifecycleScope.launch {

            paymentViewModel.getSubcription().observe(viewLifecycleOwner) { jsonObject ->

                dismissMe()

                val jsonObjectData = sessionManager.checkResponse(jsonObject)

                if (jsonObjectData != null) {
                    try {

                        val subcriptionData = Gson().fromJson(jsonObjectData, SubscriptionsModel::class.java)
                        subcriptionList = subcriptionData.data

                        subcriptionId = subcriptionList[0].id.toString()
                        price = subcriptionList[0].price
                        price_show = subcriptionList[0].price_show
                        if (price_show==1 ){
                            price = "49.00"
                        }else{
                            price = subcriptionList[0].price
                        }

                        updateButtonState(0)

                        binding.viewpager.adapter = subscriptionAdapter
                        binding.viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
                        subscriptionAdapter.updateList(subcriptionList)
                        /* 🔥 TabLayout Indicator attach */
                        TabLayoutMediator(
                            binding.tabLayoutIndicator,
                            binding.viewpager
                        ) { tab, position ->

                            tab.text = ""
                        }.attach()
                    } catch (e: Exception) {
                        sessionManager.alertErrorDialog(e.message.toString())
                    }

                }
            }

        }

    }

    // This function is used for remove card from save card list
    @SuppressLint("NotifyDataSetChanged")
    private fun cancelSubscription(position : Int ,subcriptionId: String) {
        showMe()
        lifecycleScope.launch {
            paymentViewModel.cancelSubscription(subcriptionId)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)

                    if (jsonObjectData != null) {
                        try {
                            val message = jsonObjectData["message"].asString
                            val status = jsonObjectData["status"].asBoolean

                            sessionManager.alertErrorDialog(message)

                            if (status) {
                                subcriptionList[position].is_active = false
                                subscriptionAdapter.notifyItemChanged(position)

                                updateButtonState(position)
                                // ✅ Update buttons immediately
                                binding.btnCancel.visibility = View.GONE
                                binding.btnBuyOrNext.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }
    }
    private fun updateButtonState(position: Int) {
        if (position < 0 || position >= subcriptionList.size) return

        if (subcriptionList[position].is_active) {
            binding.btnBuyOrNext.visibility = View.GONE
            binding.btnCancel.visibility = View.VISIBLE
        } else {
            binding.btnBuyOrNext.visibility = View.VISIBLE
            binding.btnCancel.visibility = View.GONE
        }
    }

    private fun cancelSubAlertBox(position : Int ,subcriptionId: String) {
        val cancelSubAccountDialog = Dialog(requireContext())
        cancelSubAccountDialog.setContentView(R.layout.cancel_subscription_alert_dialog)
        cancelSubAccountDialog.setCancelable(true)
        cancelSubAccountDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val btYes: TextView = cancelSubAccountDialog.findViewById(R.id.yes)
        val btNo: TextView = cancelSubAccountDialog.findViewById(R.id.no)

        btNo.setOnClickListener {
            cancelSubAccountDialog.dismiss()
        }

        btYes.setOnClickListener {
            cancelSubAccountDialog.dismiss()
            if (!sessionManager.isNetworkAvailable()){
                sessionManager.alertErrorDialog(getString(R.string.no_internet))
            }else{
                cancelSubscription(position,subcriptionId)
            }

        }

        cancelSubAccountDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        cancelSubAccountDialog.show()
    }

    private fun getActivePlanPosition(): Int {
        for (i in subcriptionList.indices.reversed()) {
            if (subcriptionList[i].is_active) {
                return i
            }
        }
        return -1
    }

    override fun onResume() {
        super.onResume()
        getSubscriptionList()
    }

}

