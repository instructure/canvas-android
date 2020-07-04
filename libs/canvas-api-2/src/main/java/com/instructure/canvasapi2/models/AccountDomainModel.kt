package com.instructure.canvasapi2.models

import android.os.Parcelable

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

import java.util.Date

@Parcelize
data class AccountDomainModel(
        var domain: String? = null,
        var name: String? = null,
        @SerializedName("client_id")
        val clientId: String? = null,
        @SerializedName("client_secret")
        val clientSecret: String? = null,
        @SerializedName("authentication_provider")
        val authenticationProvider: String? = null

) : CanvasModel<AccountDomainModel>(), Parcelable{
    override val comparisonString: String? get() = domain
    override fun toString(): String = "$name --- $domain"
}
