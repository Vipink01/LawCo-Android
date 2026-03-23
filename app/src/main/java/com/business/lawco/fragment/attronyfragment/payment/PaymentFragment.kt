package com.business.lawco.fragment.attronyfragment.payment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.braintreepayments.cardform.view.CardEditText
import com.braintreepayments.cardform.view.CvvEditText
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.santalu.maskara.widget.MaskEditText
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.Token
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.adapter.attroney.SavedCardAdapter
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentPaymentBinding
import com.business.lawco.model.CardDetails
import com.business.lawco.model.SavedCardModel
import com.business.lawco.networkModel.paymentManagement.PaymentManagementViewModel
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.StripPaymentUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaymentFragment : BaseFragment(), View.OnClickListener {
    lateinit var sessionManager: SessionManager
    lateinit var binding: FragmentPaymentBinding

    private lateinit var adapterSaveCard: SavedCardAdapter
    private var savedCardList: ArrayList<CardDetails> = arrayListOf()
    private lateinit var paymentManagementViewModel: PaymentManagementViewModel
    private var subcriptionPrice: String? = null
    private var subcriptionId: String? = null
    private lateinit var stripe: Stripe

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            FragmentPaymentBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        return binding.root
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        paymentManagementViewModel = ViewModelProvider(this)[PaymentManagementViewModel::class.java]
        binding.paymentManagementViewModel = paymentManagementViewModel

        stripe = Stripe(requireContext(), AppConstant.STRIP_PUBLIC_KEY)

        binding.cardListBox.visibility = View.GONE
        binding.noCardBox.visibility = View.GONE
        binding.directPaymentBox.visibility = View.GONE

//        val callback: OnBackPressedCallback =
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    findNavController().navigate(R.id.action_paymentFragment_to_settingsFragment)
//                }
//            }
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        when (requireArguments().getString(AppConstant.SOURCE_FRAGMENT)) {
            AppConstant.SETTING -> {
                makePayEnable(0)
            }

            AppConstant.SUBCRIPTION -> {
                subcriptionPrice =
                    requireArguments().getString(AppConstant.SUBCRIPTION_PRICE).toString()
                subcriptionId = requireArguments().getString(AppConstant.SUBCRIPTION_ID).toString()
                makePayEnable(1)
            }
        }

        adapterSaveCard.setOnRemoveCard(object : SavedCardAdapter.OnRemoveCard {
            override fun onRemoveCard(position: Int, cardId: String) {
                removeCard(position, cardId)
            }
        })

        getCardList()

        binding.btPaymentSubcription.setOnClickListener(this)
        binding.btPayNow.setOnClickListener(this)
        binding.btBack.setOnClickListener(this)
        binding.btAddCard.setOnClickListener(this)
        binding.btAddCard1.setOnClickListener(this)

    }

    override fun onClick(item: View?) {
        when (item!!.id) {

            R.id.btPaymentSubcription -> {

                if (binding.etCardHolderName.text.toString() == "") {
                    sessionManager.alertErrorDialog("Enter name as it appears on your card")
                } else if (binding.etCardNumber.text.toString().trim() == "") {
                    sessionManager.alertErrorDialog("Enter card number")
                } else if (binding.etCardNumber.text.toString().length < 16) {
                    sessionManager.alertErrorDialog("Enter valid card number.")
                } else if (binding.etExpireDate.text.toString().length < 5 || !expiryValidator(
                        binding.etExpireDate.text.toString()
                    )
                ) {
                    sessionManager.alertErrorDialog("Enter correct expiry date.")
                } else if (binding.etCvv.text.toString().length < 3) {
                    sessionManager.alertErrorDialog("Enter 3 digit cvv number.")
                } else {
                    binding.btPaymentSubcription.isClickable=false
                    val expire: String = binding.etExpireDate.text.toString()
                    var month = 0
                    var year = 0
                    try {
                        month = expire.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[0].toInt()
                        year = expire.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[1].toInt()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    /* if (validateCardDetails(
                             binding.etCardHolderName.text.toString(),
                             binding.etCardNumber.text.toString().replace("\\s".toRegex(), ""),
                             binding.etExpireDate.month.toString().toInt(),
                             binding.etExpireDate.year.toString().toInt(),
                             binding.etCvv.text.toString(),
                         )) {*/

                    val cardParams = CardParams(
                        binding.etCardNumber.text.toString(),
                        month,
                        year,
                        binding.etCvv.text.toString(),
                        binding.etCardHolderName.text.toString(),
                        null
                    )

                    stripe.createCardToken(
                        cardParams, null, null,
                        object : ApiResultCallback<Token> {
                            override fun onSuccess(result: Token) {
                                Log.e("Card Token", result.id)
                                if (binding.checkbox.isChecked) {
                                    paymentStart("1", "", result.id)
                                } else {
                                    paymentStart("0", "", result.id)
                                }
                            }

                            override fun onError(e: Exception) {
                                binding.btPaymentSubcription.isClickable = true

                                sessionManager.alertErrorDialog(e.message.toString())
                                Log.d("1512",e.message.toString())
                            }
                        }
                    )
                }
            }

            R.id.btAddCard -> {
                openAddCardDialog()
            }

            R.id.btAddCard1 -> {
                openAddCardDialog()
            }

            R.id.btBack -> {
                findNavController().navigateUp()
            }

            R.id.btPayNow -> {
               /* var selectCardPosition: Int? = 0
                savedCardList.forEachIndexed { index, dataItem ->
                    if (dataItem.selectCard) {
                        selectCardPosition = index
                    }
                }

                if (selectCardPosition != null) {
                    bottomSheetPayment(
                        savedCardList[selectCardPosition].customer_id,
                        savedCardList[selectCardPosition].cardholdername,
                        savedCardList[selectCardPosition].last4,
                        savedCardList[selectCardPosition].exp_month,
                        savedCardList[selectCardPosition].exp_year,
                    )
                } else {
                    sessionManager.alertErrorDialog("Please Select A Card !")
                }*/
            }
        }
    }

    private fun makePayEnable(enable: Int) {
        adapterSaveCard = SavedCardAdapter(savedCardList, requireActivity(), enable)
        binding.cardRcv.adapter = adapterSaveCard

        binding.cardRcv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

    }

    // This function is used for get all saved card list
    @SuppressLint("NotifyDataSetChanged")
    private fun getCardList() {
        showMe()
        lifecycleScope.launch {
            paymentManagementViewModel.getAllCard().observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
                val jsonObjectData = sessionManager.checkResponseHidemessage(jsonObject)

                if (jsonObjectData != null) {
                    try {
                        Log.e("Json response", jsonObjectData.toString())
                        val cardData = Gson().fromJson(jsonObjectData, SavedCardModel::class.java)
                        savedCardList = cardData.data
                        adapterSaveCard.updateCardList(savedCardList)
                        Log.e("Card List", savedCardList.toString())
                        checkCardList()
                    } catch (e: Exception) {
                        sessionManager.alertErrorDialog(e.toString())
                    }
                } else {
                    checkCardList()
                }

            }
        }

    }

    // This function is used for check card list is empty or not
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun checkCardList() {
        if (savedCardList.isEmpty()) {
            when (requireArguments().getString(AppConstant.SOURCE_FRAGMENT)) {
                AppConstant.SETTING -> {
                    binding.directPaymentBox.visibility = View.GONE
                    binding.noCardBox.visibility = View.VISIBLE
                    binding.cardListBox.visibility = View.GONE
                }

                AppConstant.SUBCRIPTION -> {
                    binding.btPaymentSubcription.text = "Pay $$subcriptionPrice"
                    binding.directPaymentBox.visibility = View.VISIBLE
                    binding.noCardBox.visibility = View.GONE
                    binding.cardListBox.visibility = View.GONE
                }
            }
        } else {
            when (requireArguments().getString(AppConstant.SOURCE_FRAGMENT)) {
                AppConstant.SETTING -> {
                    binding.directPaymentBox.visibility = View.GONE
                    binding.noCardBox.visibility = View.GONE
                    binding.cardListBox.visibility = View.VISIBLE
                    binding.btPayNow.visibility = View.GONE
                }

                AppConstant.SUBCRIPTION -> {
                    binding.directPaymentBox.visibility = View.GONE
                    binding.noCardBox.visibility = View.GONE
                    binding.cardListBox.visibility = View.VISIBLE
                    binding.btPayNow.visibility = View.VISIBLE
                }
            }
        }
    }

    // This function is used for remove card from save card list
    @SuppressLint("NotifyDataSetChanged")
    private fun removeCard(position: Int, cardId: String) {
        showMe()
        lifecycleScope.launch {
            paymentManagementViewModel.removeCard(cardId)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)

                    if (jsonObjectData != null) {
                        try {
                            savedCardList.removeAt(position)
                            adapterSaveCard.updateCardList(savedCardList)
                            checkCardList()
                            sessionManager.alertErrorDialog(jsonObjectData["message"].asString)
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }
    }

    // This function is used for display add card dialog
    private fun openAddCardDialog() {
        val addCardDialog = Dialog(requireContext())
        addCardDialog.setContentView(R.layout.alert_add_card_dialog)

        val btAddCard:TextView = addCardDialog.findViewById(R.id.btAddCard)
        val cross: ImageView = addCardDialog.findViewById(R.id.crossAddCard)

        val etCardHolderName: EditText = addCardDialog.findViewById(R.id.etCardHolderName)
        val etCardNumber: CardEditText = addCardDialog.findViewById(R.id.etCardNumber)
        val etExpireDate: MaskEditText = addCardDialog.findViewById(R.id.etExpireDate)
        val etCvv: CvvEditText = addCardDialog.findViewById(R.id.etCvv)

        cross.setOnClickListener {
            addCardDialog.dismiss()
        }

        btAddCard.setOnClickListener {
            if (etCardHolderName.text.toString() == "") {
                sessionManager.alertErrorDialog("Enter name as it appears on your card")
            } else if (etCardNumber.text.toString().trim() == "") {
                sessionManager.alertErrorDialog("Enter card number")
            } else if (etCardNumber.text.toString().length < 16) {
                sessionManager.alertErrorDialog("Enter valid card number.")
            } else if (etExpireDate.text.toString().length < 5 || !expiryValidator(etExpireDate.text.toString())) {
                sessionManager.alertErrorDialog("Enter correct expiry date.")
            } else if (etCvv.text.toString().length < 3) {
                sessionManager.alertErrorDialog("Enter 3 digit cvv number.")
            } else {
                btAddCard.isClickable=false
                val expire: String = etExpireDate.text.toString()
                var month = 0
                var year = 0
                try {
                    month = expire.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0].toInt()
                    year = expire.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1].toInt()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                val cardParams = CardParams(
                    etCardNumber.text.toString(),
                    month, year, etCvv.text.toString(),
                    etCardHolderName.text.toString()
                )

                stripe.createCardToken(
                    cardParams, null, null,
                    object : ApiResultCallback<Token> {
                        override fun onSuccess(result: Token) {
                            Log.e("Card Token", result.id)
                            saveCard(result.id, addCardDialog)
                        }

                        override fun onError(e: Exception) {
                            btAddCard.isClickable=true
                            sessionManager.alertErrorDialog(e.message.toString())
//                            addCardDialog.dismiss()
                        }
                    }
                )

            }
        }

        addCardDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addCardDialog.show()
    }


    // This function is used for check card is valid or not
    private fun validateCardDetails(
        cardHolderName: String, cardNumber: String, expiryMonth: Int, expiryYear: Int, cvv: String
    ): Boolean {
        if (cardHolderName.isEmpty()) {
            return false
        }
        if (!StripPaymentUtility.isValidCardNumber(cardNumber)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.valid_card_number),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        val cardType = StripPaymentUtility.getCardType(cardNumber)
        if (cardType == "unknown") {
            Toast.makeText(
                requireContext(),
                getString(R.string.valid_card_type),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (!StripPaymentUtility.isValidExpiryDate(expiryMonth, expiryYear)) {
            Toast.makeText(
                requireContext(),
                getString(R.string.valid_expireDate),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (!StripPaymentUtility.isValidCVV(cvv, cardType)) {
            Toast.makeText(requireContext(), getString(R.string.enter_cvv), Toast.LENGTH_SHORT)
                .show()
            return false
        }

        return true
    }

    // This function is used for save card in database
    private fun saveCard(cardToken: String, addCardDialog: Dialog) {
        showMe()
        lifecycleScope.launch {
            paymentManagementViewModel.saveCard(
                cardToken,
            ).observe(viewLifecycleOwner) { jsonObject ->
                dismissMe()
                val jsonObjectData = sessionManager.checkResponse(jsonObject)
                if (jsonObjectData != null) {
                    try {
                        addCardDialog.dismiss()
                        addCardSuccessFullDialog()
                    } catch (e: Exception) {
                        sessionManager.alertErrorDialog(e.toString())
                    }

                }

            }
        }

    }

    // This function is used for display card successfully added dialog
    @SuppressLint("SuspiciousIndentation")
    private fun addCardSuccessFullDialog() {
        val addCardDialogFirst = Dialog(requireContext())
        addCardDialogFirst.setContentView(R.layout.add_card_successfully_alert_box)
        val okButton = addCardDialogFirst.findViewById<TextView>(R.id.btn_okay1)

        okButton.setOnClickListener {
            addCardDialogFirst.dismiss()

            getCardList()
        }

        addCardDialogFirst.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addCardDialogFirst.show()

    }


    // This function is used for open dialog for payment
    @SuppressLint("SetTextI18n")
    private fun bottomSheetPayment(
        customerId: String,
        cardHolderName: String,
        cardNumber: String,
        expMonth: String,
        expYear: String,
    ) {

        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.BottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_amount_payable)
        bottomSheetDialog.show()

        val btPayCancel = bottomSheetDialog.findViewById<ImageView>(R.id.btPayCancel) as ImageView
        val btPayNow = bottomSheetDialog.findViewById<TextView>(R.id.btPayNow) as TextView
        val tvCardNumber = bottomSheetDialog.findViewById<TextView>(R.id.tvCardNumber) as TextView
        val tvCardHolderName =
            bottomSheetDialog.findViewById<TextView>(R.id.tvCardHolderName) as TextView
        val tvExpireDate = bottomSheetDialog.findViewById<TextView>(R.id.tvExpireDate) as TextView
        val etCvvVerification =
            bottomSheetDialog.findViewById<EditText>(R.id.etCvvVerification) as EditText
        val tvAmountPayable =
            bottomSheetDialog.findViewById<TextView>(R.id.tvAmountPayable) as TextView

        tvAmountPayable.text = "Amount Payable $$subcriptionPrice"
        tvCardHolderName.text = cardHolderName
        tvCardNumber.text = "**** **** **** ${cardNumber.takeLast(4)}"
        tvExpireDate.text = "Exp date:$expMonth/$expYear"

        btPayCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        btPayNow.setOnClickListener {
            if (etCvvVerification.text.toString().isEmpty()) {
                sessionManager.alertErrorDialog(getString(R.string.enter_cvv))
            } else if (etCvvVerification.text.toString().length != 3) {
                sessionManager.alertErrorDialog(getString(R.string.valid_cvv))
            } else {
                bottomSheetDialog.dismiss()
                paymentStart("0", customerId, "")
            }
        }

    }

    // This function is used for start payment process
    private fun paymentStart(status: String, coutmerID: String, token: String) {
        showMe()
        lifecycleScope.launch {
            paymentManagementViewModel.paymentForSubcription(
                subcriptionPrice!!,
                subcriptionId!!,
                status,
                coutmerID,
                token
            )
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponse(jsonObject)

                    if (jsonObjectData != null) {
                        try {
                            paymentSuccessFullDialog()
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.message.toString())
                        }
                    }
                }
        }
    }

    // This function is used for display payment successfully done dialog
    private fun paymentSuccessFullDialog() {

        val postDialog = Dialog(requireContext())
        postDialog.setContentView(R.layout.alert_dialog_payment_successful)
        postDialog.setCancelable(true)
        val submit: TextView = postDialog.findViewById(R.id.pass_btn_okay1)

        submit.setOnClickListener {
            postDialog.dismiss()
            binding.btPaymentSubcription.isClickable=true
            findNavController().navigate(R.id.subscriptionsFragment)
            /*getCardList()*/

        }
        postDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        postDialog.show()


    }

}