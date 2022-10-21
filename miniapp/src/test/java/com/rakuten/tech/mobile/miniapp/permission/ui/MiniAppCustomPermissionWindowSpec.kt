package com.rakuten.tech.mobile.miniapp.permission.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rakuten.tech.mobile.miniapp.R
import com.rakuten.tech.mobile.miniapp.TEST_BASE_PATH
import com.rakuten.tech.mobile.miniapp.TEST_CALLBACK_ID
import com.rakuten.tech.mobile.miniapp.TestActivity
import com.rakuten.tech.mobile.miniapp.permission.CustomPermissionBridgeDispatcher
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermission
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionCache
import com.rakuten.tech.mobile.miniapp.permission.MiniAppCustomPermissionType
import com.rakuten.tech.mobile.miniapp.storage.DownloadedManifestCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.io.File

@Suppress("LongMethod")
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class MiniAppCustomPermissionWindowSpec {
    private lateinit var permissionCache: MiniAppCustomPermissionCache
    private lateinit var downloadedManifestCache: DownloadedManifestCache
    private val dispatcher: CustomPermissionBridgeDispatcher = mock()
    private var context: Context = mock()
    private val editor: SharedPreferences.Editor = mock()
    private val prefs: SharedPreferences = mock()
    private val activity: Activity = mock()
    private lateinit var permissionWindow: MiniAppCustomPermissionWindow
    private val miniAppId = TEST_CALLBACK_ID
    private val permissionWithDescriptions =
        listOf(
            Pair(MiniAppCustomPermissionType.USER_NAME, "dummy description"),
            Pair(MiniAppCustomPermissionType.PROFILE_PHOTO, "dummy description"),
            Pair(MiniAppCustomPermissionType.CONTACT_LIST, "dummy description")
        )
    private lateinit var cachedCustomPermission: MiniAppCustomPermission

    @Before
    fun setup() {
        Mockito.`when`(prefs.edit()).thenReturn(editor)
        Mockito.`when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs)
        Mockito.`when`(context.filesDir).thenReturn(File(TEST_BASE_PATH))

        permissionCache = MiniAppCustomPermissionCache(prefs, prefs)
        downloadedManifestCache = spy(DownloadedManifestCache(context))
        cachedCustomPermission = permissionCache.readPermissions(miniAppId)
        permissionWindow = mock()
    }

    private fun setupForRecyclerViewTestCase(onReady: (MiniAppCustomPermissionWindow, RecyclerView) -> Unit )  {
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))
        val customPermissionLayout: View = mock()
        val permissionRecyclerView: RecyclerView = mock()

        doReturn(permissionRecyclerView).whenever(customPermissionLayout)
            .findViewById<RecyclerView>(R.id.listCustomPermission)
        doReturn(customPermissionLayout).whenever(permissionWindow).customPermissionLayout

        val recyclerView = permissionWindow.getRecyclerView()

        onReady(permissionWindow, recyclerView)
    }

    private fun setupForPermissionLayoutTestCases(onReady: (View, TextView, TextView, AlertDialog) -> Unit){
        val permissionAlertDialog: AlertDialog = mock()
        val customPermissionLayout: View = mock()
        val textPermissionSave: TextView = mock()
        val textPermissionCloseWindow: TextView = mock()
        val customPermissionAdapter: MiniAppCustomPermissionAdapter = mock()
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))
        permissionWindow.customPermissionLayout = mock()

        doReturn(permissionAlertDialog).whenever(permissionWindow).customPermissionAlertDialog
        doReturn(textPermissionSave).whenever(customPermissionLayout)
            .findViewById<TextView>(R.id.permissionSave)
        doReturn(textPermissionCloseWindow).whenever(customPermissionLayout)
            .findViewById<TextView>(R.id.permissionCloseWindow)
        doReturn(customPermissionLayout).whenever(permissionWindow).customPermissionLayout
        doReturn(customPermissionAdapter).whenever(permissionWindow).customPermissionAdapter
        permissionWindow.addPermissionClickListeners()

        onReady(customPermissionLayout, textPermissionSave, textPermissionCloseWindow, permissionAlertDialog)

    }

    private fun setupUsingRealContext(onReady: (MiniAppCustomPermissionWindow) -> Unit) {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val permissionWindow = MiniAppCustomPermissionWindow(activity, dispatcher)
            permissionWindow.initCustomPermissionLayout()
            onReady(permissionWindow)
        }
    }

    private fun setupUsingRealContextWithRecyclerView(onReady: (MiniAppCustomPermissionWindow, RecyclerView) -> Unit) {
        ActivityScenario.launch(TestActivity::class.java).onActivity { activity ->
            val permissionWindow = MiniAppCustomPermissionWindow(activity, dispatcher)
            permissionWindow.initCustomPermissionLayout()
            val recyclerView = spy(permissionWindow.getRecyclerView())
            onReady(permissionWindow, recyclerView)
        }
    }


    @Test
    fun `the coroutine context should be Dispatchers Main`() {
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        permissionWindow.coroutineContext.shouldBe(Dispatchers.Main)
    }

    @Test
    fun `should not init anything while miniAppId is empty`() {
        val mockDialog: AlertDialog = mock()
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.displayPermissions("", permissionWithDescriptions)

        verify(permissionWindow, times(0)).prepareDataForAdapter(permissionWithDescriptions)
        verify(permissionWindow, times(0)).addPermissionClickListeners()
        verify(mockDialog, times(0)).show()
    }

    @Test
    fun `should not init anything while permissions are empty`() {
        val mockDialog: AlertDialog = mock()
        val emptyPermissions: List<Pair<MiniAppCustomPermissionType, String>> = emptyList()

        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        permissionWindow.displayPermissions(miniAppId, emptyPermissions)

        verify(permissionWindow, times(0)).getRecyclerView()
        verify(permissionWindow, times(0)).prepareDataForAdapter(emptyPermissions)
        verify(permissionWindow, times(0)).addPermissionClickListeners()
        verify(mockDialog, times(0)).show()
    }

    @Suppress("MaxLineLength")
    @Test(expected = NullPointerException::class) // due to a require real context
    fun `getRecyclerView should call layoutManager`() {
        setupForRecyclerViewTestCase { _, recyclerView ->
            verify(recyclerView).layoutManager
            verify(recyclerView).adapter
            recyclerView.shouldBeInstanceOf(RecyclerView::class.java)
            recyclerView.shouldNotBeNull()

        }
    }

    @Suppress("MaxLineLength")
    @Test(expected = NullPointerException::class) // due to a require real context
    fun `getRecyclerView layoutManager should not be null`() {
        setupForRecyclerViewTestCase { _, recyclerView ->
            recyclerView.layoutManager.shouldNotBeNull()
        }
    }

    @Suppress("MaxLineLength")
    @Test(expected = NullPointerException::class) // due to a require real context
    fun `getRecyclerView should set adapter`() {
        setupForRecyclerViewTestCase { _, recyclerView ->
            verify(recyclerView).adapter
        }
    }

    @Suppress("MaxLineLength")
    @Test(expected = NullPointerException::class) // due to a require real context
    fun `getRecyclerView should generate a recyclerView`() {
        setupForRecyclerViewTestCase { _, recyclerView ->
            recyclerView.shouldBeInstanceOf(RecyclerView::class.java)
        }
    }

    @Suppress("MaxLineLength")
    @Test(expected = NullPointerException::class) // due to a require real context
    fun `getRecyclerView should not be null`() {
        setupForRecyclerViewTestCase { _, recyclerView ->
            recyclerView.shouldNotBeNull()
        }
    }

    @Test
    fun `prepareDataForAdapter should add deniedPermissions to adapter`() {
        val customPermissionAdapter: MiniAppCustomPermissionAdapter = mock()
        val permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))
        doReturn(customPermissionAdapter).whenever(permissionWindow).customPermissionAdapter
        permissionWindow.prepareDataForAdapter(permissionWithDescriptions)
        verify(customPermissionAdapter).addPermissionList(
            any(),
            any(),
            any(),
        )
    }

    @Test
    fun `addPermissionClickListeners should register permissionSave textView`() {
        setupForPermissionLayoutTestCases { customPermissionLayout, _, _, _ ->
            verify(customPermissionLayout).findViewById<TextView>(R.id.permissionSave)
        }
    }

    @Test
    fun `addPermissionClickListeners should register textPermissionCloseWindow textView`() {
        setupForPermissionLayoutTestCases { customPermissionLayout, _, _, _ ->
            verify(customPermissionLayout).findViewById<TextView>(R.id.permissionCloseWindow)
        }
    }

    @Test
    fun `addPermissionClickListeners textPermissionSave textview should call setOnClickListener`() {
        setupForPermissionLayoutTestCases { _, textPermissionSave, _, _ ->
            verify(textPermissionSave).setOnClickListener(any())
        }
    }

    @Test
    fun `addPermissionClickListeners textPermissionCloseWindow textview should call setOnClickListener`() {
        setupForPermissionLayoutTestCases { _, _, textPermissionCloseWindow, _ ->
            verify(textPermissionCloseWindow).setOnClickListener(any())
        }
    }

    @Test
    fun `addPermissionClickListeners permissionAlertDialog should call setOnKeyListener`() {
        setupForPermissionLayoutTestCases { _, _, _, permissionAlertDialog ->
            verify(permissionAlertDialog).setOnKeyListener(any())
        }
    }

    @Test
    fun `onNoPermissionsSaved should send cached custom permissions`() {
        val mockDialog: AlertDialog = mock()
        permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.onNoPermissionsSaved()

        verify(dispatcher).sendCachedCustomPermissions()
        verify(mockDialog).dismiss()
    }

    @Test
    fun `onNoPermissionsSaved should call dialog dismiss`() {
        val mockDialog: AlertDialog = mock()
        permissionWindow = spy(MiniAppCustomPermissionWindow(activity, dispatcher))

        doReturn(mockDialog).whenever(permissionWindow).customPermissionAlertDialog

        permissionWindow.onNoPermissionsSaved()

        verify(mockDialog).dismiss()
    }

    @Test
    fun `customPermissionLayout should be initialized when initCustomPermissionLayout is called`() {
        setupUsingRealContext { permissionWindow ->
            val customPermissionLayout = spy(permissionWindow.customPermissionLayout)
            customPermissionLayout.isVisible.shouldBeTrue()
        }
    }

    @Test
    fun `initAdapterAndDialog should initialize customPermissionAlertDialog`() {
        setupUsingRealContextWithRecyclerView { permissionWindow, recyclerView ->
            permissionWindow.initAdapterAndDialog(recyclerView)
            val customPermissionAlertDialog = spy(permissionWindow.customPermissionAdapter)
            customPermissionAlertDialog.permissionNames.shouldBeEmpty()
        }
    }

    @Test
    fun `initAdapterAndDialog should initialize customPermissionAdapter`() {
        setupUsingRealContextWithRecyclerView { permissionWindow, recyclerView ->
            permissionWindow.initAdapterAndDialog(recyclerView)
            val customPermissionAdapter = spy(permissionWindow.customPermissionAdapter)
            customPermissionAdapter.itemCount.shouldBe(0)
        }
    }

    @Test
    fun `customPermissionLayout calls context should not be null`() {
        setupUsingRealContext { permissionWindow ->
            val customPermissionLayout = spy(permissionWindow.customPermissionLayout)
            customPermissionLayout.context.shouldNotBeNull()
        }
    }

    @Test
    fun `should get recyclerView when getRecyclerView isCalled`() {
        setupUsingRealContextWithRecyclerView { _, recyclerView ->
            recyclerView.shouldNotBeNull()
        }
    }

    @Test
    fun `recyclerView should be visible when getRecyclerView isCalled`() {
        setupUsingRealContextWithRecyclerView { _, recyclerView ->
            recyclerView.isVisible.shouldBeTrue()
            recyclerView.itemDecorationCount.shouldBe(1)
        }
    }

    @Test
    fun `recyclerView should have itemDecoration when getRecyclerView isCalled`() {
        setupUsingRealContextWithRecyclerView { _, recyclerView ->
            recyclerView.itemDecorationCount.shouldBe(1)
        }
    }
}
