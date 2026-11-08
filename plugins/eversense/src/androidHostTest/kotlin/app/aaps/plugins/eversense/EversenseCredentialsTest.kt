package app.aaps.plugins.eversense

import android.bluetooth.BluetoothManager
import android.content.Context
import app.aaps.plugins.eversense.models.EversenseSecureState
import app.aaps.plugins.eversense.storage.EversenseStore
import app.aaps.plugins.eversense.util.StorageKeys
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class EversenseCredentialsTest {
    private val store: EversenseStore = mock()
    private val editor: EversenseStore.Editor = mock()
    private val context: Context = mock()
    private val bluetooth: BluetoothManager = mock()
    private val json = Json { ignoreUnknownKeys = true }
    private fun backend(): EversenseCGMPlugin {
        whenever(store.edit()).thenReturn(editor)
        whenever(editor.commit()).thenReturn(true)
        whenever(store.getString(StorageKeys.SECURE_STATE, null)).thenReturn(
            json.encodeToString(EversenseSecureState().apply { username = "user"; password = "secret"; canUseShortcut = true }))
        return EversenseCGMPlugin(context, bluetooth, store)
    }
    @Test fun clearCredentialsRemovesCachedLoginAndDisablesShortcut() {
        val backend = backend()
        backend.clearCredentials()
        val state = argumentCaptor<String>()
        verify(editor).putString(eq(StorageKeys.SECURE_STATE), state.capture())
        val saved = json.decodeFromString<EversenseSecureState>(state.firstValue)
        assertEquals("", saved.username)
        assertEquals("", saved.password)
        assertFalse(saved.canUseShortcut)
        assertEquals("", backend.username)
        assertEquals("", backend.password)
        verify(editor).remove(StorageKeys.ACCESS_TOKEN)
        verify(editor).remove(StorageKeys.ACCESS_TOKEN_EXPIRY)
        verify(editor).commit()
        verifyNoInteractions(bluetooth)
    }
    @Test fun regionChangeInvalidatesTokenAndShortcutBeforeDriverIsStarted() {
        val backend = backend()
        backend.setCredentials("user", "secret", europeanRegion = true)
        val state = argumentCaptor<String>()
        verify(editor).putString(eq(StorageKeys.SECURE_STATE), state.capture())
        val saved = json.decodeFromString<EversenseSecureState>(state.firstValue)
        assertTrue(saved.isEuropeanRegion)
        assertFalse(saved.canUseShortcut)
        verify(editor).remove(StorageKeys.ACCESS_TOKEN)
        verify(editor).remove(StorageKeys.ACCESS_TOKEN_EXPIRY)
        verifyNoInteractions(bluetooth)
    }
}
