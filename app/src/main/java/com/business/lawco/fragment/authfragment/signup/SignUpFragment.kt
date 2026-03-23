package com.business.lawco.fragment.authfragment.signup

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.activity.consumer.ConsumerHomeActivity
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentSignUpBinding
import com.business.lawco.networkModel.signUp.SignUpViewModel
import com.business.lawco.utility.AppConstant
import com.business.lawco.utility.ValidationData
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.withpersona.sdk2.inquiry.Environment
import com.withpersona.sdk2.inquiry.Fields
import com.withpersona.sdk2.inquiry.Inquiry
import com.withpersona.sdk2.inquiry.InquiryResponse
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

@AndroidEntryPoint
class SignUpFragment : BaseFragment(), View.OnClickListener {

    lateinit var sessionManager: SessionManager
    lateinit var binding: FragmentSignUpBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var signViewModel: SignUpViewModel
    var type:String = ""
    private lateinit var callbackManager: CallbackManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentSignUpBinding.inflate(LayoutInflater.from(requireActivity()), container, false)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = context?.let { SessionManager(it) }!!
        signViewModel = ViewModelProvider(this)[SignUpViewModel::class.java]

        binding.signUpViewModel = signViewModel
        callbackManager = CallbackManager.Factory.create()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.data != null) {
                    try {
                        if (result.resultCode == Activity.RESULT_OK) {
                            Log.e("Result Code ", result.toString())
                            val task: Task<GoogleSignInAccount> =
                                GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            val account: GoogleSignInAccount? =
                                task.getResult(ApiException::class.java)

                            if (account != null) {
                                if (sessionManager.isNetworkAvailable()) {
                                    Log.e("Google Sign In Email", account.email.toString())

                                    //getDeviceToken(account.displayName!!,  account.email!!,account.id!!)

                                    val userType =
                                        if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                                            "0"
                                        } else {
                                            "1"
                                        }
                                    socialLogin(account.displayName!!, account.email!!, userType, account.id!!)

                                } else {
                                    sessionManager.alertErrorDialog(getString(R.string.no_internet))
                                }
                            }
                        } else if (result.resultCode == Activity.RESULT_CANCELED) {
                            Log.e("Social Login", "User canceled autocomplete")
                        }
                    } catch (e: Exception) {
                        sessionManager.alertErrorDialog(e.toString())
                        Log.e("Auth Error", result.resultCode.toString())
                    }
                } else {
                    sessionManager.alertErrorDialog("Gmail Not Found !")
                }
            }

        try {
            if (requireArguments().getString(AppConstant.NAME) != null) {
                binding.etName.setText(requireArguments().getString(AppConstant.NAME).toString())
            }

            if (requireArguments().getString(AppConstant.EMAIL) != null) {
                binding.etEmail.setText(requireArguments().getString(AppConstant.EMAIL).toString())
            }

            if (requireArguments().getString(AppConstant.PASSWORD) != null) {
                binding.etPassword.setText(requireArguments().getString(AppConstant.PASSWORD).toString())
            }

        } catch (e: Exception) {
            Log.e("No Argument", e.toString())
        }

        binding.btSignUp.setOnClickListener(this)
        binding.textTermAndCondition.setOnClickListener(this)
        binding.imgShowPass.setOnClickListener(this)
        binding.imgHidePass.setOnClickListener(this)
        binding.btGoogleSignIn.setOnClickListener(this)

        binding.rlFBox.setOnClickListener {
            initFacebook()
        }


        if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
            binding.btnClaim.visibility = View.VISIBLE
        } else {
            binding.btnClaim.visibility = View.GONE
        }


        binding.btnClaim.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_claimProfileFragment)
        }


        signInButtonClickable()
        termAndConditionClickable()

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


    private fun initFacebook() {
        val loginManager = LoginManager.getInstance()
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val request = GraphRequest.newMeRequest(result.accessToken) { `object`, _ ->
                    try {
                        val email = `object`?.optString("email") ?: ""
                        val name = `object`?.optString("name") ?: ""
                        val personId = `object`?.optString("id") ?: ""
                        val finalEmail = email.ifEmpty { "$personId@facebook.com" }
                        Log.d("FB_LOGIN", "Email: $finalEmail, Name: $name")
                        Log.d("FB_LOGIN", "fbId: $personId, Name: $name")
                        facebookLogOut()
                        if (sessionManager.isNetworkAvailable()) {
                            Log.e("Google Sign In Email", finalEmail)
                            val userType = if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                                "0"
                            } else {
                                "1"
                            }
                            socialLogin(name, finalEmail, userType, personId)
                        } else {
                            sessionManager.alertErrorDialog(getString(R.string.no_internet))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Error ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                val parameters = Bundle().apply {
                    putString("fields", "id,name,email,gender,birthday,picture.type(large)")
                }
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Log.d("Error :-", "Login Cancel")
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }
        })

        // Set permissions directly without trying to access loginBehavior
        loginManager.logInWithReadPermissions(
            this,
            listOf("email", "user_photos", "public_profile")
        )
    }

    private fun facebookLogOut() {
        val accessToken = AccessToken.getCurrentAccessToken()
        GraphRequest(
            accessToken,
            "/me/permissions/",
            null,
            HttpMethod.DELETE,
            { response ->
                // Step 2: Local logout
                LoginManager.getInstance().logOut()
                AccessToken.setCurrentAccessToken(null)
                // Step 3: Clear cookies completely
                clearFacebookCookies()
                Log.d("FB_LOGOUT", "Facebook logout completed successfully.")
            }
        ).executeAsync()
    }
    private fun clearFacebookCookies() {
        try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            cookieManager.flush()
            android.webkit.WebStorage.getInstance().deleteAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // This function is used for term and condition up button Clickable
    private fun termAndConditionClickable() {
        val spannedString = SpannableString("By Signing up you agree to our Terms & Conditions.")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val bundle = Bundle()
                bundle.putString(AppConstant.SOURCE_FRAGMENT , AppConstant.SIGN_UP)
                findNavController().navigate(R.id.action_signUpFragment_to_termAndConditionFragment,bundle)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.orange)
                ds.isUnderlineText = true // Remove the underline
            }

        }
        val startIndex = spannedString.toString().indexOf("Terms & Conditions.")
        val endIndex = startIndex + "Terms & Conditions.".length
        spannedString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textTermAndCondition.text = spannedString
        binding.textTermAndCondition.movementMethod = LinkMovementMethod.getInstance()

    }

    // This function is used for sign in button Clickable
    private fun signInButtonClickable() {
        val spannedString = SpannableString(getString(R.string.already_have_an_account))
        val signUpStart = object : ClickableSpan() {
            override fun onClick(p0: View) {
                findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.orange)
                ds.isUnderlineText = false
            }
        }
        val signUpStartIndex = spannedString.toString().indexOf("Sign In")
        val signUpEndIndex = signUpStartIndex + "Sign In".length
        spannedString.setSpan(
            signUpStart,
            signUpStartIndex,
            signUpEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textAlreadyHaveAccount.text = spannedString
        binding.textAlreadyHaveAccount.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.btSignUp -> {
                if (binding.etName.text.isEmpty()) {
                  //  sessionManager.alertErrorDialog(getString(R.string.fill_name))
                    sessionManager.alertErrorDialog(getString(R.string.fill_name))
                } else if (binding.etEmail.text.isEmpty()) {
                   // sessionManager.alertErrorDialog(getString(R.string.fill_email))
                    sessionManager.alertErrorDialog(getString(R.string.fill_email))
                }else if (!validateEmailPhone()!!){
                    sessionManager.alertErrorDialog(getString(R.string.fill_valid_email))
                } else if (binding.etPassword.text.isEmpty()) {
                    sessionManager.alertErrorDialog(getString(R.string.fill_password))
                } else if (!ValidationData.passCheck(binding.etPassword.text.toString())) {
                    sessionManager.alertErrorDialog(getString(R.string.password_validation_text))
                }
                else if (!binding.checkbox.isChecked) {
                    sessionManager.alertErrorDialog(getString(R.string.accept_term_condition))
                } else if (!sessionManager.isNetworkAvailable()) {
                    sessionManager.alertErrorDialog(getString(R.string.no_internet))
                } else {
                    sendVerificationOtp(binding.etEmail.text.toString())
                }
            }
            R.id.imgHidePass -> {
                binding.imgShowPass.visibility = View.VISIBLE
                binding.imgHidePass.visibility = View.GONE
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.etPassword.setSelection(binding.etPassword.text.length)
            }
            R.id.imgShowPass -> {
                binding.imgShowPass.visibility = View.GONE
                binding.imgHidePass.visibility = View.VISIBLE
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.etPassword.setSelection(binding.etPassword.text.length)
            }
            R.id.btGoogleSignIn -> {
                val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(),
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build())
                mGoogleSignInClient.signOut()
                val signInIntent: Intent = mGoogleSignInClient.signInIntent
                activityResultLauncher.launch(signInIntent)
            }
        }
    }

    private fun validateEmailPhone(): Boolean? {
        val input: String = binding.etEmail.text.toString().trim()
        if (emailValidator(input)) {
            Patterns.EMAIL_ADDRESS.matcher(input).matches()
            type = "email"
            return true
        } else if (validateNumber(input)) {
            type = "mobile"
            return true
        }
        return false
    }

    private fun emailValidator(email: String?): Boolean {
        val pattern: Pattern
        val EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun validateNumber(mobNumber: String): Boolean {
        //validates phone numbers having 10 digits (9998887776)
        return if (mobNumber.matches("\\d{10}".toRegex())) true else if (mobNumber.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}".toRegex())) true else if (mobNumber.matches(
                "\\d{4}[-\\.\\s]\\d{3}[-\\.\\s]\\d{3}".toRegex()
            )
        ) true else if (mobNumber.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}".toRegex())) true else if (mobNumber.matches(
                "\\(\\d{3}\\)-\\d{3}-\\d{4}".toRegex()
            )
        ) true else if (mobNumber.matches("\\(\\d{5}\\)-\\d{3}-\\d{3}".toRegex())) true else if (mobNumber.matches(
                "\\(\\d{4}\\)-\\d{3}-\\d{3}".toRegex()
            )
        ) true else false
    }

    // This function is used for send Sign Up verification Otp and open  Sign Up verification Screen
    private fun sendVerificationOtp(email: String) {
        showMe()
        lifecycleScope.launch {
            val userType =
                if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                    "0"
                } else {
                    "1"
                }
            signViewModel.signUpOtp(email,userType)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            val bundle = Bundle()
                            bundle.putString(AppConstant.NAME, binding.etName.text.toString())
                            bundle.putString(AppConstant.EMAIL, binding.etEmail.text.toString())
                            bundle.putString(AppConstant.PASSWORD, binding.etPassword.text.toString())
                            bundle.putString(AppConstant.OTP, jsonObjectData["otp"].asString)
                            findNavController().navigate(R.id.action_signUpFragment_to_signUpVerificationFragment, bundle)
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }
    }

    // This function is used for social gmail or facebook login
    private fun socialLogin(displayName: String, email: String, userType: String, deviceToken: String, ) {
        showMe()
        Log.e("displayName", displayName)
        Log.e("email", email)
        Log.e("userType", userType)
        Log.e("deviceToken", deviceToken)
        lifecycleScope.launch {
            signViewModel.socialLogin(displayName, email, userType, deviceToken)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            sessionManager.setBearerToken(jsonObjectData["token"].asString)
                            Log.e("success", jsonObjectData.toString())
                            Log.e("User Type", sessionManager.getUserType())
                            Log.e("Bearer Token", sessionManager.getBearerToken())
                            openHomeScreen(jsonObjectData)
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }
    }

    private fun openHomeScreen(jsonObjectData: JsonObject) {
        if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
            if (jsonObjectData.has("is_profile_complete") && !jsonObjectData["is_profile_complete"].asBoolean ) {
                findNavController().navigate(R.id.action_signUpFragment_to_profileFragment)
            }else{
                sessionManager.setIsLogin(true)
                val intent = Intent(requireActivity(), AttronyHomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        } else {
            if (sessionManager.getUserType() == AppConstant.CONSUMER) {
                sessionManager.setIsLogin(true)
                val consumerIntent = Intent(requireActivity(), ConsumerHomeActivity::class.java)
                startActivity(consumerIntent)
                requireActivity().finish()
            } else {
                Log.e("success failed", "User type is not found")
                sessionManager.sessionEndDialog(requireActivity(), "User type is not found")
            }
        }
    }

}
