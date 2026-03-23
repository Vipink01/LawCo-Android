package com.business.lawco.utility

import java.util.Calendar

object StripPaymentUtility {

    fun isValidCardNumber(cardNumber: String): Boolean {
        val sanitizedCardNumber = cardNumber.replace(" ", "")
        if (sanitizedCardNumber.length < 13 || sanitizedCardNumber.length > 19) return false

        var sum = 0
        var alternate = false
        for (i in sanitizedCardNumber.length - 1 downTo 0) {
            var n = sanitizedCardNumber[i].toString().toInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    fun getCardType(cardNumber: String): String {
        return when {
            cardNumber.startsWith("4") -> "visa"
            cardNumber.matches(Regex("^5[1-5]")) || cardNumber.matches(Regex("^2(22[1-9]|2[3-9][0-9]|[3-6][0-9]{2}|7([01][0-9]|20))")) -> "mastercard"
            cardNumber.matches(Regex("^3[47]")) -> "amex"
            cardNumber.matches(Regex("^6(?:011|5)")) -> "discover"
            else -> "unknown"
        }
    }

    fun isValidExpiryDate(expiryMonth: Int, expiryYear: Int): Boolean {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        return if (expiryYear > currentYear) {
            true
        } else if (expiryYear == currentYear) {
            expiryMonth >= currentMonth
        } else {
            false
        }
    }

    fun isValidCVV(cvv: String, cardType: String): Boolean {
        val cvvPattern = Regex("^[0-9]{3,4}\$")
        if (!cvv.matches(cvvPattern)) return false

        return when (cardType) {
            "amex" -> cvv.length == 4
            "visa", "mastercard", "discover" -> cvv.length == 3
            else -> false
        }
    }

   /* fun getCardToken(cardNumber :String ,expiryMonth: Int ,expiryYear: Int , etCvv :String, etCardHolderName :String , requireActivity : Activity) : String? {
        val stripe = Stripe(requireActivity, AppConstant.STRIP_PUBLIC_KEY)
        var token : String? = "nocard"

        val cardParams = CardParams(cardNumber, expiryMonth,expiryYear,etCvv,etCardHolderName,null)

        stripe.createCardToken(
            cardParams, null, null,
            object : ApiResultCallback<Token> {
                override fun onSuccess(result: Token) {
                    token = result.id
                }
                override fun onError(e: Exception) {
                    token = null
                    SessionManager(requireActivity).alertErrorDialog(e.message.toString())
                }
            }
        )

        return  token
    }
*/
}