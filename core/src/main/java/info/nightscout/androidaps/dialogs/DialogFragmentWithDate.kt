package info.nightscout.androidaps.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import dagger.android.support.DaggerDialogFragment
import info.nightscout.androidaps.core.R
import info.nightscout.shared.logging.AAPSLogger
import info.nightscout.shared.logging.LTag
import info.nightscout.androidaps.utils.DateUtil
import info.nightscout.androidaps.extensions.toVisibility
import info.nightscout.shared.sharedPreferences.SP
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

abstract class DialogFragmentWithDate : DaggerDialogFragment() {

    @Inject lateinit var aapsLogger: AAPSLogger
    @Inject lateinit var sp: SP
    @Inject lateinit var dateUtil: DateUtil

    fun interface OnValueChangedListener {
        fun onValueChanged(value: Long)
    }

    var eventTime: Long = 0
    var eventTimeOriginal: Long = 0
    val eventTimeChanged: Boolean
        get() = eventTime != eventTimeOriginal

    private var eventDateView: TextView? = null
    private var eventTimeView: TextView? = null
    private var mOnValueChangedListener: OnValueChangedListener? = null

    //one shot guards
    private var okClicked: AtomicBoolean = AtomicBoolean(false)

    companion object {

        private var seconds: Int = (Math.random() * 59.0).toInt()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        aapsLogger.debug(LTag.APS, "Dialog opened: ${this.javaClass.name}")
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putLong("eventTime", eventTime)
        savedInstanceState.putLong("eventTimeOriginal", eventTimeOriginal)
    }

    fun onCreateViewGeneral() {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        isCancelable = true
        dialog?.setCanceledOnTouchOutside(false)
    }

    fun updateDateTime(timeMs: Long) {
        eventTime = timeMs
        eventDateView?.text = dateUtil.dateString(eventTime)
        eventTimeView?.text = dateUtil.timeString(eventTime)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        eventDateView = view.findViewById(R.id.eventdate) as TextView?
        eventTimeView = view.findViewById(R.id.eventtime) as TextView?

        eventTimeOriginal = savedInstanceState?.getLong("eventTimeOriginal") ?: dateUtil.nowWithoutMilliseconds()
        eventTime = savedInstanceState?.getLong("eventTime") ?: eventTimeOriginal

        eventDateView?.text = dateUtil.dateString(eventTime)
        eventTimeView?.text = dateUtil.timeString(eventTime)

        // create an OnDateSetListener
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = eventTime
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                eventTime = cal.timeInMillis
                eventDateView?.text = dateUtil.dateString(eventTime)
                callValueChangedListener()
            }

        eventDateView?.setOnClickListener {
            context?.let {
                val cal = Calendar.getInstance()
                cal.timeInMillis = eventTime
                DatePickerDialog(
                    it, R.style.MaterialPickerTheme,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        // create an OnTimeSetListener
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = eventTime
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(
                Calendar.SECOND,
                seconds++
            ) // randomize seconds to prevent creating record of the same time, if user choose time manually
            eventTime = cal.timeInMillis
            eventTimeView?.text = dateUtil.timeString(eventTime)
            callValueChangedListener()
        }

        eventTimeView?.setOnClickListener {
            context?.let {
                val cal = Calendar.getInstance()
                cal.timeInMillis = eventTime
                TimePickerDialog(
                    it, R.style.MaterialPickerTheme,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(context)
                ).show()
            }
        }

        (view.findViewById(R.id.notes_layout) as View?)?.visibility =
            sp.getBoolean(R.string.key_show_notes_entry_dialogs, false).toVisibility()

        (view.findViewById(R.id.ok) as Button?)?.setOnClickListener {
            synchronized(okClicked) {
                if (okClicked.get()) {
                    aapsLogger.warn(LTag.UI, "guarding: ok already clicked for dialog: ${this.javaClass.name}")
                } else {
                    okClicked.set(true)
                    if (submit()) {
                        aapsLogger.debug(LTag.APS, "Submit pressed for Dialog: ${this.javaClass.name}")
                        dismiss()
                    } else {
                        aapsLogger.debug(LTag.APS, "Submit returned false for Dialog: ${this.javaClass.name}")
                        okClicked.set(false)
                    }
                }
            }
        }
        (view.findViewById(R.id.cancel) as Button?)?.setOnClickListener {
            aapsLogger.debug(LTag.APS, "Cancel pressed for dialog: ${this.javaClass.name}")
            dismiss()
        }

    }

    private fun callValueChangedListener() {
        mOnValueChangedListener?.onValueChanged(eventTime)
    }

    fun setOnValueChangedListener(onValueChangedListener: OnValueChangedListener?) {
        mOnValueChangedListener = onValueChangedListener
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            manager.beginTransaction().let {
                it.add(this, tag)
                it.commitAllowingStateLoss()
            }
        } catch (e: IllegalStateException) {
            aapsLogger.debug(e.localizedMessage ?: "")
        }
    }

    abstract fun submit(): Boolean
}