package com.business.lawco.fragment.consumerfragment.category

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.consumer.CategoriesAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentConsumerCategoryBinding
import com.business.lawco.model.consumer.AreaOfPractice
import com.business.lawco.model.consumer.CategoryModel
import com.business.lawco.networkModel.homeScreen.consumer.ConsumerHomeScreenViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConsumerCategoryFragment : BaseFragment(), View.OnClickListener{

    lateinit var binding: FragmentConsumerCategoryBinding
    lateinit var sessionManager: SessionManager

    private var categoriesPageList: ArrayList<AreaOfPractice> = arrayListOf()
    private lateinit var consumerHomeScreenViewModel: ConsumerHomeScreenViewModel
    private var adapter:CategoriesAdapter?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConsumerCategoryBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        consumerHomeScreenViewModel = ViewModelProvider(this)[ConsumerHomeScreenViewModel::class.java]
        binding.consumerHomeScreenViewModel = consumerHomeScreenViewModel

        adapter = CategoriesAdapter(categoriesPageList, requireContext(), "home")
        binding.rcvCategoriesList.adapter = adapter
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        binding.arrowWhite.setOnClickListener(this)
        binding.categorypLayoutRefresh.setOnRefreshListener{
            getAreaOfPractice()
        }
        binding.EtSearchForCategories.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                val resultList: List<AreaOfPractice> = when {
                    query.isEmpty() -> {
                        categoriesPageList
                    }else -> {
                        categoriesPageList.filter {
                            it.category_name.startsWith(query, ignoreCase = true)
                        }
                    }
                }
                updateUI(resultList.takeIf { it.isNotEmpty() })
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        getAreaOfPractice()

    }
    override fun onResume() {
        super.onResume()
        binding.EtSearchForCategories.text.clear()
    }
    private fun updateUI(list: List<AreaOfPractice>?) {
        if (!list.isNullOrEmpty()) {
            binding.rcvCategoriesList.visibility = View.VISIBLE
            binding.textNoDataFound.visibility = View.GONE
            adapter?.updateData(list as ArrayList<AreaOfPractice>)
        } else {
            binding.rcvCategoriesList.visibility = View.GONE
            binding.textNoDataFound.visibility = View.VISIBLE
        }
    }
    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.arrowWhite -> {
//                findNavController().navigate(R.id.action_consumerCategoryFragment_to_consumerHomeFragment)
                findNavController().navigateUp()
            }
        }
    }
    private fun getAreaOfPractice() {
        showMe()
        lifecycleScope.launch {
            consumerHomeScreenViewModel.getAreaOfPractice()
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    binding.categorypLayoutRefresh.isRefreshing = false
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            val categoryList = Gson().fromJson(jsonObjectData, CategoryModel::class.java)
                            categoriesPageList.clear()
                            categoryList.data.let {
                                categoriesPageList.addAll(it)
                            }
                            updateUI(categoriesPageList)
                        } catch (e: Exception) {
                            binding.rcvCategoriesList.visibility = View.GONE
                            binding.textNoDataFound.visibility = View.VISIBLE
                            Log.e("Error","******"+e.message)
                        }
                    }else{
                        binding.rcvCategoriesList.visibility = View.GONE
                        binding.textNoDataFound.visibility = View.VISIBLE
                    }
                }
        }
    }
}