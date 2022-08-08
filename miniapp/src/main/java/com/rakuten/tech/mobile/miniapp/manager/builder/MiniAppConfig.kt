package com.rakuten.tech.mobile.miniapp.manager.builder

import android.os.Parcelable
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.sdkExceptionForInvalidArguments
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MiniAppConfig(
    val appId: String,
    val queryParams: String,
    val miniAppSdkConfig: MiniAppSdkConfig,
    val miniAppNavigator: MiniAppNavigator?,
    val miniAppFileChooser: MiniAppFileChooser?,
    val miniAppMessageBridge: MiniAppMessageBridge
) : Parcelable {

    init {
        when {
            appId.isEmpty()  ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid baseUrl")
            ((miniAppNavigator == null) || (miniAppFileChooser == null)) ->
                throw sdkExceptionForInvalidArguments("MiniAppSdkConfig with invalid rasProjectId")
        }
    }

}
