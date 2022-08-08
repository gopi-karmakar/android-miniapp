package com.rakuten.tech.mobile.miniapp.manager.provider

import android.content.Context
import com.rakuten.tech.mobile.miniapp.MiniAppDownloader
import com.rakuten.tech.mobile.miniapp.MiniAppInfoFetcher
import com.rakuten.tech.mobile.miniapp.MiniAppSdkConfig
import com.rakuten.tech.mobile.miniapp.analytics.MiniAppAnalytics
import com.rakuten.tech.mobile.miniapp.api.ApiClient
import com.rakuten.tech.mobile.miniapp.api.ApiClientRepository
import com.rakuten.tech.mobile.miniapp.api.ManifestApiCache
import com.rakuten.tech.mobile.miniapp.display.Displayer
import com.rakuten.tech.mobile.miniapp.js.MessageBridgeRatDispatcher
import com.rakuten.tech.mobile.miniapp.js.MiniAppSecureStorageDispatcher
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.signatureverifier.SignatureVerifier
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import com.rakuten.tech.mobile.miniapp.storage.FileWriter
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStatus
import com.rakuten.tech.mobile.miniapp.storage.MiniAppStorage
import com.rakuten.tech.mobile.miniapp.storage.verifier.CachedMiniAppVerifier
import com.rakuten.tech.mobile.miniapp.storage.verifier.MiniAppManifestVerifier

class Provider(
    val context: Context,
    val config: MiniAppSdkConfig
) {

    internal var enableH5Ads: Boolean
    internal var apiClient: ApiClient
    internal var displayer: Displayer
    internal var miniAppAnalytics: MiniAppAnalytics
    internal var miniAppDownloader: MiniAppDownloader
    internal var signatureVerifier: SignatureVerifier
    internal var miniAppInfoFetcher: MiniAppInfoFetcher
    internal var apiClientRepository: ApiClientRepository
    internal var ratDispatcher: MessageBridgeRatDispatcher
    internal var miniAppManifestVerifier: MiniAppManifestVerifier
    internal var downloadedManifestCache: DownloadedManifestCache
    internal var secureStorageDispatcher: MiniAppSecureStorageDispatcher
    internal var miniAppCustomPermissionCache: MiniAppCustomPermissionCache

    private fun initApiClient() = ApiClient(
        baseUrl = config.baseUrl,
        rasProjectId = config.rasProjectId,
        subscriptionKey = config.subscriptionKey,
        isPreviewMode = config.isPreviewMode,
        sslPublicKeyList = config.sslPinningPublicKeyList
    )

    private fun initMiniAppDownloader() = MiniAppDownloader(
        apiClient = apiClient,
        miniAppAnalytics = miniAppAnalytics,
        requireSignatureVerification = config.requireSignatureVerification,
        initStorage = { MiniAppStorage(FileWriter(), context.filesDir) },
        initStatus = { MiniAppStatus(context) },
        initVerifier = { CachedMiniAppVerifier(context) },
        initManifestApiCache = { ManifestApiCache(context) },
        initSignatureVerifier = { signatureVerifier }
    )

    init {

        enableH5Ads = config.enableH5Ads
        apiClient = initApiClient()
        displayer = Displayer(config.hostAppUserAgentInfo)
        miniAppInfoFetcher = MiniAppInfoFetcher(apiClient)
        miniAppDownloader = initMiniAppDownloader()
        downloadedManifestCache = DownloadedManifestCache(context)
        miniAppManifestVerifier = MiniAppManifestVerifier(context)
        miniAppCustomPermissionCache = MiniAppCustomPermissionCache(context)

        apiClientRepository = ApiClientRepository().apply {
            registerApiClient(config, apiClient)
        }
        signatureVerifier = SignatureVerifier.init(
            context = context,
            baseUrl = config.baseUrl + "keys/",
            subscriptionKey = config.subscriptionKey
        )!!
        miniAppAnalytics = MiniAppAnalytics(
            config.rasProjectId,
            config.miniAppAnalyticsConfigList
        )
        ratDispatcher = MessageBridgeRatDispatcher(miniAppAnalytics)
        secureStorageDispatcher = MiniAppSecureStorageDispatcher(
            context,
            config.maxStorageSizeLimitInMB
        )
    }
}
