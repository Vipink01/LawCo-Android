package com.business.lawco.fragment.settingFragment.termscondition

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentTermAndConditionBinding
import com.business.lawco.networkModel.common.CommonViewModel
import com.business.lawco.utility.AppConstant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TermAndConditionFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentTermAndConditionBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var commonViewModel: CommonViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTermAndConditionBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]
        binding.commonViewModel = commonViewModel

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getString(AppConstant.SOURCE_FRAGMENT ) ==  AppConstant.SETTING)
                    {
                     //   findNavController().navigate(R.id.action_termAndConditionFragment_to_settingsFragment)
                        findNavController().navigateUp()
                    }else{
                        findNavController().navigate(R.id.action_termAndConditionFragment_to_signUpFragment)
                    }

                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        startGetTermAndCondition()

        binding.arrowWhite.setOnClickListener(this)

        binding.termRefresh.setOnRefreshListener {
            startGetTermAndCondition()
        }

    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.arrowWhite -> {
                if (requireArguments().getString(AppConstant.SOURCE_FRAGMENT ) ==  AppConstant.SETTING)
                {
                    findNavController().navigateUp()
                 //   findNavController().navigate(R.id.action_termAndConditionFragment_to_settingsFragment)
                }else{
                    findNavController().navigate(R.id.action_termAndConditionFragment_to_signUpFragment)
                }
            }
        }
    }

    // This function is used for Start get  Term and Condition
    private fun startGetTermAndCondition() {
        if (!sessionManager.isNetworkAvailable()) {
            binding.termRefresh.isRefreshing = false
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        } else {
            getTermAndCondition()
        }
    }

    // This function is used for get Term and Condition for database
    @SuppressLint("SetJavaScriptEnabled")
    private fun getTermAndCondition() {
        showMe()
        lifecycleScope.launch {
            commonViewModel.getContent("1")
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    binding.termRefresh.isRefreshing = false

                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                          /*  val spanned = Html.fromHtml(
                                jsonObjectData["content"].asString,
                                Html.FROM_HTML_MODE_LEGACY
                            )
                            binding.termAndConditionText.text = spanned*/
                            jsonObjectData.get("content")?.asString
                                ?.takeIf { it.isNotBlank() }
                                ?.let { htmlContent ->

                                    binding.webView.apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        webViewClient = WebViewClient()

                                        loadDataWithBaseURL(
                                            null,
                                            htmlContent,
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                    }
                                }
                        } catch (e: Exception) {
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
        }
    }
}