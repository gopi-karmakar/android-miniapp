[miniapp](../../index.md) / [com.rakuten.tech.mobile.miniapp](../index.md) / [MiniApp](index.md) / [getCustomPermissions](./get-custom-permissions.md)

# getCustomPermissions

`abstract fun getCustomPermissions(miniAppId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`MiniAppCustomPermission`](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission/index.md)

Get custom permissions with grant results per MiniApp from this SDK.

### Parameters

`miniAppId` - mini app id as the key to retrieve data from cache.

**Return**
[MiniAppCustomPermission](../../com.rakuten.tech.mobile.miniapp.permission/-mini-app-custom-permission/index.md) an object contains the grant results per mini app.
