package com.business.lawco.fragment.settingFragment.privacypolicy

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
import com.business.lawco.databinding.FragmentPrivacyPolicyBinding
import com.business.lawco.networkModel.common.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PrivacyPolicyFragment : BaseFragment() ,View.OnClickListener{
    lateinit var binding: FragmentPrivacyPolicyBinding
    private lateinit var sessionManager: SessionManager
    lateinit var commonViewModel: CommonViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPrivacyPolicyBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]
        binding.commonViewModel = commonViewModel
        sessionManager = SessionManager(requireContext())

//        val callback: OnBackPressedCallback =
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    findNavController().navigate(R.id.action_privacyPolicyFragment_to_settingsFragment)
//                }
//            }
//
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        startGetPrivacyAndPolicy()

        binding.arrowWhite.setOnClickListener(this)

        binding.privacyRefresh.setOnRefreshListener{
            startGetPrivacyAndPolicy()
        }

    }

    override fun onClick(item: View?) {

        when(item!!.id){
            R.id.arrowWhite->{
              //  findNavController().navigate(R.id.action_privacyPolicyFragment_to_settingsFragment)
                findNavController().navigateUp()
            }

        }
    }

    private fun startGetPrivacyAndPolicy(){
        if (!sessionManager.isNetworkAvailable()){
            binding.privacyRefresh.isRefreshing = false
            sessionManager.alertErrorDialog(getString(R.string.no_internet))
        }else{
            getPrivacyAndPolicy()
        }
    }

    // This function is used for get Privacy and Policy for database
    @SuppressLint("SetJavaScriptEnabled")
    private fun getPrivacyAndPolicy(){
        showMe()
        lifecycleScope.launch {
            commonViewModel.getContent("2")
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    binding.privacyRefresh.isRefreshing = false
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null){
                        try {
                           /* val spanned = Html.fromHtml(jsonObjectData["content"].asString, Html.FROM_HTML_MODE_LEGACY)
                            binding.privacyPolicyText.text = spanned*/

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

                        }catch (e:Exception){
                            Log.d("@Error","***"+e.message)
                        }
                    }
                }
        }


    }


}