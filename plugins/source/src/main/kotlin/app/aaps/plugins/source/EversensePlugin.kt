package app.aaps.plugins.source

import android.content.Context
import app.aaps.core.data.plugin.PluginType
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.plugin.PluginDescription
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.source.BgSource
import app.aaps.plugins.source.fragments.EversenseFragment
import com.nightscout.eversense.EversenseCGMPlugin
import javax.inject.Inject

class EversensePlugin @Inject constructor(
    rh: ResourceHelper,
    private val context: Context,
    aapsLogger: AAPSLogger
) : AbstractBgSourceWithSensorInsertLogPlugin(
    PluginDescription()
        .mainType(PluginType.BGSOURCE)
        .fragmentClass(EversenseFragment::class.java.name)
        // Fix: Explicitly point to the core object R file
        .pluginIcon(app.aaps.core.objects.R.drawable.ic_blooddrop_48)
        .preferencesId(R.xml.eversense_preferences)
        .pluginName(R.string.source_eversense)
        .preferencesVisibleInSimpleMode(false)
        .description(R.string.description_source_eversense),
    aapsLogger, rh
), BgSource {
    // No extra overrides needed; the abstract class handles defaults.
    init {
        EversenseCGMPlugin.instance.setContext(context)
    }
}