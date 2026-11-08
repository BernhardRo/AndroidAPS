package app.aaps.plugins.source.eversense

import android.app.Application
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.aaps.plugins.source.R
import app.aaps.plugins.source.activities.configureEversenseNavigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class EversenseNavigationTest {
    private val screens = listOf(
        R.layout.activity_eversense_status to R.string.eversense_status_title,
        R.layout.activity_eversense_calibration to R.string.eversense_calibration_action,
        R.layout.activity_eversense_placement to R.string.eversense_placement_title
    )

    @Test fun everyScreenHasClickableBackArrowBeforeOptionsMenuIsPrepared() {
        for ((layout, title) in screens) {
            val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
            val activity = controller.get()
            activity.setTheme(app.aaps.core.ui.R.style.AppTheme_NoActionBar)
            controller.create()
            try {
                activity.setContentView(layout)
                activity.configureEversenseNavigation(title)
                val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
                assertNotNull(toolbar.navigationIcon)
                val arrow = (0 until toolbar.childCount).map { toolbar.getChildAt(it) }.filterIsInstance<ImageButton>().single()
                assertTrue(arrow.performClick())
                assertTrue(activity.isFinishing)
            } finally { controller.destroy() }
        }
    }

    @Test fun systemBarsAndCutoutsKeepToolbarVisibleAndRepeatedInsetsDoNotAccumulate() {
        for ((layout, title) in screens) {
            val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
            val activity = controller.get()
            activity.setTheme(app.aaps.core.ui.R.style.AppTheme_NoActionBar)
            controller.create()
            try {
                activity.setContentView(layout)
                val root = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup
                val originalTop = root.paddingTop
                activity.configureEversenseNavigation(title)
                val insets = WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 64, 0, 48))
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(24, 0, 12, 0))
                    .build()
                repeat(2) { ViewCompat.dispatchApplyWindowInsets(root, insets) }
                assertEquals(originalTop + 64, root.paddingTop)
                assertEquals(24, root.paddingLeft)
                assertEquals(12, root.paddingRight)
                assertEquals(48, root.paddingBottom)
                root.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY))
                root.layout(0, 0, 1080, 1920)
                val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
                val bounds = Rect()
                toolbar.getDrawingRect(bounds)
                root.offsetDescendantRectToMyCoords(toolbar, bounds)
                assertTrue("Toolbar overlaps status bar in layout $layout", bounds.top >= 64)
                assertTrue(toolbar.height > 0)
            } finally { controller.destroy() }
        }
    }
}
