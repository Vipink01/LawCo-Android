package com.business.lawco.fragment.authfragment.signin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
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
import android.view.View.OnFocusChangeListener
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.business.lawco.R
import com.business.lawco.SessionManager
import com.business.lawco.activity.IdentityActivity
import com.business.lawco.activity.attroney.AttronyHomeActivity
import com.business.lawco.activity.consumer.ConsumerHomeActivity
import com.business.lawco.adapter.consumer.RememberMeAdapter
import com.business.lawco.adapter.consumer.RememberSelect
import com.business.lawco.base.BaseFragment
import com.business.lawco.databinding.FragmentSignInBinding
import com.business.lawco.model.consumer.RememberMe
import com.business.lawco.networkModel.login.LoginViewModel
import com.business.lawco.utility.AppConstant
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Matcher
import java.util.regex.Pattern

@AndroidEntryPoint
class  SignInFragment : BaseFragment(), View.OnClickListener {

    lateinit var binding: FragmentSignInBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var type:String = ""
    private var token: String = ""
    private lateinit var callbackManager: CallbackManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentSignInBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = context?.let { SessionManager(it) }!!
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        binding.loginViewModel = loginViewModel
        callbackManager = CallbackManager.Factory.create()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                @SuppressLint("SuspiciousIndentation")
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
                                    //   getDeviceToken(account.displayName!!,  account.email!!)
                                    val userType = if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
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
//                            sessionManager.alertErrorDialog("Sign in failed !")
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

      /*  binding.etMail.onFocusChangeListener = OnFocusChangeListener { view, b ->
            showRememberDialog()
        }*/

        sessionManager.getRememberMe()?.let {
            if (!it.equals("",true)){
                val data: String = it
                val rememberMe = Gson().fromJson(data, RememberMe::class.java)
                binding.etMail.setText(rememberMe.email)
                binding.etPassword.setText(rememberMe.pass)
            }
        }

        binding.textBtSignIn.setOnClickListener(this)
        binding.textForgotPass.setOnClickListener(this)
        binding.imgShowPass.setOnClickListener(this)
        binding.imgHidePass.setOnClickListener(this)
        binding.btGmailLogin.setOnClickListener(this)
        binding.btFacebookLogin.setOnClickListener(this)
        signUpButtonClickable()
    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.textForgotPass -> {
                findNavController().navigate(R.id.action_signInFragment_to_forgotPasswordFragment)
            }
            R.id.textBtSignIn -> {

                if (binding.etMail.text.isEmpty()) {
                    sessionManager.alertErrorDialog(getString(R.string.fill_email))
//                    binding.etMail.requestFocus()
                }
                else if (!validateEmailPhone()!!){
                    sessionManager.alertErrorDialog(getString(R.string.fill_valid_email_phone))
                }
                /* else if (!ValidationData.emailValidate(binding.etMail.text.toString())) {
                    sessionManager.alertErrorDialog(getString(R.string.fill_valid_email))
                }*/ else if (binding.etPassword.text.isEmpty()) {
                    sessionManager.alertErrorDialog(getString(R.string.fill_password))
//                    binding.etPassword.requestFocus()
                } else if (!sessionManager.isNetworkAvailable()) {
                    sessionManager.alertErrorDialog(getString(R.string.no_internet))
                } else {
                    if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                        login(binding.etMail.text.toString(), binding.etPassword.text.toString(), "0")
                    } else {
                        if (sessionManager.getUserType() == AppConstant.CONSUMER) {
                            login(binding.etMail.text.toString(), binding.etPassword.text.toString(), "1")
                        } else {
                            sessionManager.sessionEndDialog(requireActivity(), getString(R.string.user_type_not_found))
                        }
                    }
                }
            }
            R.id.imgHidePass -> {
                binding.imgShowPass.visibility = View.VISIBLE
                binding.imgHidePass.visibility = View.GONE
                binding.etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.etPassword.setSelection(binding.etPassword.text.length)

            }

            R.id.imgShowPass -> {
                binding.imgShowPass.visibility = View.GONE
                binding.imgHidePass.visibility = View.VISIBLE

                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.etPassword.setSelection(binding.etPassword.text.length)
            }

            R.id.btGmailLogin -> {
                val mGoogleSignInClient = GoogleSignIn.getClient(
                    requireActivity(),
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build()
                )
                mGoogleSignInClient.signOut()
                val signInIntent: Intent = mGoogleSignInClient.signInIntent
                activityResultLauncher.launch(signInIntent)
            }

            R.id.btFacebookLogin -> {
                initFacebook()
            }
        }
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
                    putString("fields", "id,name,email")
                }
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                facebookLogOut()
                Log.d("Error :-", "Login Cancel")
            }

            override fun onError(error: FacebookException) {
                facebookLogOut()
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


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun validateEmailPhone(): Boolean? {
        val input: String = binding.etMail.text.toString().trim()

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
        val matcher: Matcher
        val EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email)
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

    // This function is used for sign up button Clickable
    private fun signUpButtonClickable() {

        val spannedString = SpannableString(getString(R.string.create_an_account))

        val signUpStart = object : ClickableSpan() {

            override fun onClick(p0: View) {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.orange)
                ds.isUnderlineText = false
            }
        }

        val signUpStartIndex = spannedString.toString().indexOf("Sign Up")
        val signUpEndIndex = signUpStartIndex + "Sign Up".length
        spannedString.setSpan(
            signUpStart,
            signUpStartIndex,
            signUpEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.textDoNotHaveAccount.text = spannedString
        binding.textDoNotHaveAccount.movementMethod = LinkMovementMethod.getInstance()
    }


    // This function is used  by email  and password login
    private fun login(emailOrPhone: String, password: String, userType: String) {
       showMe()
        lifecycleScope.launch {
            loginViewModel.login(emailOrPhone, password, userType)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            sessionManager.setBearerToken(jsonObjectData["token"].asString)

                            Log.e("success", jsonObjectData.toString())

                            if (binding.checkbox.isChecked) {
                                saveRemember(emailOrPhone,password,userType)
                            }
                            if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                                if (!jsonObjectData["is_profile_complete"].asBoolean) {
                                    findNavController().navigate(R.id.action_signInFragment_to_completeProfile)
                                } else {
                                    sessionManager.setIsLogin(true)
                                    val intent = Intent(requireActivity(), AttronyHomeActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    startActivity(intent)
                                    requireActivity().finish()
                                }
                            } else {
                                if (sessionManager.getUserType() == AppConstant.CONSUMER) {
                                    sessionManager.setIsLogin(true)
                                    val consumerIntent = Intent(requireActivity(), ConsumerHomeActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    startActivity(consumerIntent)
                                    requireActivity().finish()
                                } else {
                                    Log.e("success failed", "User type is not found")
                                    sessionManager.sessionEndDialog(requireActivity(), "User type is not found")
                                }
                            }
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }
    }

    private fun saveRemember(emailOrPhone: String, password: String, userType: String) {
        /*val data: String = sessionManager.getRememberMe()
        var checkDuplicate:Boolean = false
        val list: MutableList<RememberMe> = ArrayList()
        var mutableList: MutableList<RememberMe>?=ArrayList()
        if (data!=null && data != "") {
            val objectList:List<RememberMe> = Gson().fromJson(data, Array<RememberMe>::class.java).asList()
            mutableList= objectList.toMutableList()
            Log.e("*****", objectList.toString())
            for (item in objectList) {
                if (item.email == emailOrPhone){
                    checkDuplicate = true
                    break
                }
            }
        }else {
            mutableList?.add(RememberMe(emailOrPhone, password, userType))
        }
        if (mutableList!=null) {
            if (!checkDuplicate){
                mutableList.add(RememberMe(emailOrPhone, password, userType))
            }
            sessionManager.setRememberMe(mutableList)
        }*/

        sessionManager.setRememberMe(RememberMe(emailOrPhone,password,userType))

    }

    // This function is used for social gmail or facebook login
    private fun socialLogin(
        displayName: String,
        email: String,
        userType: String,
        deviceToken: String,
    ) {
        showMe()

        Log.e("displayName", displayName)
        Log.e("email", email)
        Log.e("userType", userType)
        Log.e("deviceToken", deviceToken)

        lifecycleScope.launch {
            loginViewModel.socialLogin(displayName, email, userType, deviceToken)
                .observe(viewLifecycleOwner) { jsonObject ->
                    dismissMe()
                    val jsonObjectData = sessionManager.checkResponseShowMsg(jsonObject)
                    if (jsonObjectData != null) {
                        try {
                            sessionManager.setBearerToken(jsonObjectData["token"].asString)
                            Log.e("success", jsonObjectData.toString())
                            Log.e("User Type", sessionManager.getUserType())
                            Log.e("Bearer Token", sessionManager.getBearerToken())
                            if (sessionManager.getUserType() == AppConstant.ATTORNEY) {
                                if (!jsonObjectData["is_profile_complete"].asBoolean) {
                                    findNavController().navigate(R.id.action_signInFragment_to_completeProfile)
                                } else {
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
                        } catch (e: Exception) {
                            sessionManager.alertErrorDialog(e.toString())
                        }
                    }
                }
        }
    }


    override fun onStart() {
        super.onStart()
        getFcmToken()
    }
    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    token=task.result
                    Log.d("FCM", "FCM Token: ${task.result}")
                } else {
                    token="Fetching FCM token failed"
                    Log.e("FCM", "Fetching FCM token failed", task.exception)
                }
            }
    }
}