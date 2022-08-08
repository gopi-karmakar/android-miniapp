package com.rakuten.tech.mobile.miniapp.manager

import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.file.MiniAppFileChooser
import com.rakuten.tech.mobile.miniapp.js.MiniAppMessageBridge
import com.rakuten.tech.mobile.miniapp.manager.builder.MiniApp
import com.rakuten.tech.mobile.miniapp.manager.builder.MiniAppConfig
import com.rakuten.tech.mobile.miniapp.navigator.MiniAppNavigator
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission

internal abstract class MiniAppManagerImpl {

    /**
     * Fetches meta data information of a mini app.
     * @return [MiniAppInfo] for the provided appId of a mini app
     * @throws [MiniAppNotFoundException] when the specified project ID does not have any mini app exist on the server.
     * @throws [MiniAppHasNoPublishedVersionException] when the specified mini app ID exists on the
     * server but has no published versions
     * @throws [MiniAppSdkException] when fetching fails from the BE server for any other reason.
     */
    @Throws(
        MiniAppNotFoundException::class,
        MiniAppHasNoPublishedVersionException::class,
        MiniAppSdkException::class
    )
    abstract suspend fun fetchInfo(appId: String): MiniAppInfo

    /**
     * Get the currently downloaded manifest information e.g. required and optional permissions.
     * @param appId mini app id.
     * @return MiniAppManifest an object contains manifest information of a miniapp.
     */
    abstract fun getDownloadedManifest(appId: String): MiniAppManifest?

    /**
     * Get custom permissions with grant results per MiniApp from this SDK.
     * @param miniAppId mini app id as the key to retrieve data from cache.
     * @return [MiniAppCustomPermission] an object contains the grant results per mini app.
     */
    abstract fun getCustomPermissions(miniAppId: String): MiniAppCustomPermission

    /**
     * Store custom permissions with grant results per MiniApp inside this SDK.
     * @param miniAppCustomPermission the supplied custom permissions to be stored in cache.
     */
    abstract fun setCustomPermissions(miniAppCustomPermission: MiniAppCustomPermission)

    /**
     * lists out the mini applications available with custom permissions in cache.
     * @return [List<MiniAppInfo>] list of MiniApp what is downloaded and containing
     * custom permissions data.
     */
    @Suppress("FunctionMaxLength")
    abstract fun listDownloadedWithCustomPermissions(): List<Pair<MiniAppInfo, MiniAppCustomPermission>>


    @Throws(MiniAppSdkException::class)
    abstract suspend fun create(
        miniAppConfig: MiniAppConfig
    ): MiniApp
}
