package dev.pegasus.cmp.interfaces

/**
 * @Author: SOHAIB AHMED
 * @Date: 14-12-2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

interface OnConsentResponse {
    fun onResponse(errorMessage: String? = null)
    fun onPolicyRequired(isRequired: Boolean)
}