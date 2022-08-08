package com.rakuten.tech.mobile.miniapp.manager.builder

import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.miniapp.*
import com.rakuten.tech.mobile.miniapp.manager.provider.Provider
import com.rakuten.tech.mobile.miniapp.storage.CachedManifest

internal class MiniApp(
    private val context: Context
) {

    private lateinit var provider: Provider

    @Throws(MiniAppSdkException::class)
    private fun getCachedMiniAppIfExist(miniAppConfig: MiniAppConfig): Pair<String, MiniAppInfo>? {
        val cachedMiniApp: Pair<String, MiniAppInfo>? = try {
            provider.miniAppDownloader.getCachedMiniApp(miniAppConfig.appId)
        } catch (e: MiniAppNotFoundException) {
            null
        }
        return cachedMiniApp
    }

    internal suspend fun create(miniAppConfig: MiniAppConfig): MiniApp {
        try {
            provider = Provider(context, miniAppConfig.miniAppSdkConfig)
            val cachedMiniApp = getCachedMiniAppIfExist(miniAppConfig)
            if (cachedMiniApp != null) {
                verifyManifest(cachedMiniApp.second)
            }
        } catch (e: MiniAppNotFoundException) {
            throw e
        }
        return this
    }

    internal fun getDownloadedManifest(appId: String): MiniAppManifest? {
        return provider.downloadedManifestCache.readDownloadedManifest(appId)?.miniAppManifest
    }

    @VisibleForTesting
    private suspend fun verifyManifest(newMiniAppInfo: MiniAppInfo) {

        val appId = newMiniAppInfo.id
        val versionId = newMiniAppInfo.version.versionId

        val cachedManifest = provider.downloadedManifestCache.readDownloadedManifest(appId)
        try {
               checkToDownloadManifest(appId, versionId, cachedManifest)
        } catch (e: MiniAppNetException) {
            Log.e("RealMiniApp", "Unable to retrieve latest manifest due to device being offline. " +
                    "Skipping manifest download.", e)
        }

        val manifestFile = provider.downloadedManifestCache.getManifestFile(appId)
        if (cachedManifest != null && provider.miniAppManifestVerifier.verify(appId, manifestFile)) {
            val customPermissions = provider.miniAppCustomPermissionCache.readPermissions(appId)
            val manifestPermissions = provider.downloadedManifestCache.getAllPermissions(customPermissions)
            provider.miniAppCustomPermissionCache.removePermissionsNotMatching(appId, manifestPermissions)

            if (provider.downloadedManifestCache.isRequiredPermissionDenied(customPermissions))
                throw RequiredPermissionsNotGrantedException(appId, versionId)
        } else {
            checkToDownloadManifest(appId, versionId, cachedManifest)
        }
    }

    @VisibleForTesting
    private suspend fun checkToDownloadManifest(appId: String, versionId: String, cachedManifest: CachedManifest?) {
        val apiManifest = getMiniAppManifest(appId, versionId, "")
        val isDifferentVersion = cachedManifest?.versionId != versionId
        val isSameVerDiffApp = !isManifestEqual(apiManifest, cachedManifest?.miniAppManifest)
        if (isDifferentVersion || isSameVerDiffApp) {
            val storableManifest = CachedManifest(versionId, apiManifest)
            provider.downloadedManifestCache.storeDownloadedManifest(appId, storableManifest)
            val manifestFile = provider.downloadedManifestCache.getManifestFile(appId)
            provider.miniAppManifestVerifier.storeHashAsync(appId, manifestFile)
        }
    }

    private suspend fun getMiniAppManifest(appId: String, versionId: String, languageCode: String): MiniAppManifest =
        provider.miniAppDownloader.fetchMiniAppManifest(appId, versionId, languageCode)


    @VisibleForTesting
    private fun isManifestEqual(apiManifest: MiniAppManifest?, downloadedManifest: MiniAppManifest?): Boolean {
        if (apiManifest != null && downloadedManifest != null) {
            val changedRequiredPermissions =
                (apiManifest.requiredPermissions + downloadedManifest.requiredPermissions).groupBy { it.first.type }
                    .filter { it.value.size == 1 }
                    .flatMap { it.value }

            val changedOptionalPermissions =
                (apiManifest.optionalPermissions + downloadedManifest.optionalPermissions).groupBy { it.first.type }
                    .filter { it.value.size == 1 }
                    .flatMap { it.value }

            return changedRequiredPermissions.isEmpty() && changedOptionalPermissions.isEmpty() &&
                    apiManifest.customMetaData == downloadedManifest.customMetaData
        }
        return false
    }
}
