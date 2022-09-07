package com.rakuten.tech.mobile.miniapp.manager

import android.annotation.SuppressLint
import android.content.Context
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.manager.builder.MiniAppView
import com.rakuten.tech.mobile.miniapp.manager.builder.MiniAppConfig
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.sdkExceptionForInvalidArguments

internal class MiniAppManager private constructor(): MiniAppManagerImpl() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var instance: MiniAppManager? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: MiniAppManager().also { instance = it }
        }
    }

    private lateinit var context: Context
    private lateinit var defaultConfig: MiniAppSdkConfig
    private lateinit var miniAppsMap: LinkedHashMap<String, MiniAppView>

    internal fun init(context: Context, config: MiniAppSdkConfig) {
        this.context = context
        this.defaultConfig = config
        this.miniAppsMap = LinkedHashMap()
    }

    internal fun getDefaultConfig(): MiniAppSdkConfig {
        return defaultConfig
    }

    override suspend fun fetchInfo(appId: String): MiniAppInfo {
        TODO("Not yet implemented")
    }

    override fun getDownloadedManifest(appId: String): MiniAppManifest? {
        return miniAppsMap[appId]?.getDownloadedManifest(appId)
    }

    override fun getCustomPermissions(miniAppId: String): MiniAppCustomPermission {
        TODO("Not yet implemented")
    }

    override fun setCustomPermissions(miniAppCustomPermission: MiniAppCustomPermission) {
        TODO("Not yet implemented")
    }

    override fun listDownloadedWithCustomPermissions(): List<Pair<MiniAppInfo, MiniAppCustomPermission>> {
        TODO("Not yet implemented")
    }

    @Throws(MiniAppSdkException::class)
    override suspend fun create(miniAppConfig: MiniAppConfig): MiniAppView {

        if (miniAppConfig.appId.isNullOrEmpty())
            throw sdkExceptionForInvalidArguments()

        // TODO: Verify if the MiniAppSdkConfig isn't present then assign the default config

        val miniApp = MiniAppView(context).create(miniAppConfig)
        miniAppsMap[miniAppConfig.appId] = miniApp

        return miniApp
    }
}
