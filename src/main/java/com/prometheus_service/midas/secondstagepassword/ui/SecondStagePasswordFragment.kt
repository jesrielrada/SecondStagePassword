package com.prometheus_service.midas.secondstagepassword.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import com.poovam.pinedittextfield.CirclePinField
import com.poovam.pinedittextfield.PinField
import com.prometheus_service.midas.secondstagepassword.*
import com.prometheus_service.midas.secondstagepassword.data.PinLockPrefs
import com.prometheus_service.midas.secondstagepassword.data.PinlockData
import kotlinx.android.synthetic.main.fragment_pin_lock.*

class SecondStagePasswordFragment :
    Fragment(),
    PinField.OnTextCompleteListener,
    View.OnClickListener,
    LifecycleObserver {

    companion object {
        private const val MAX_ATTEMPTS = 3
        private const val INITIAL_TRY_COUNT = 1
    }

    private var mRemainingAttemptsCount = MAX_ATTEMPTS
    private var mTryCount = INITIAL_TRY_COUNT

    private lateinit var mCancelPin: TextView
    private lateinit var mRemainingAttempts: TextView
    private lateinit var mHeaderText: TextView
    private lateinit var mCircleField: CirclePinField
    private lateinit var prefs: PinLockPrefs

    private val mViewModel: SecondStagePasswordViewModel by viewModels()

    private var userPin: String? = null
    private var pinLockState: PinLockState = PinLockState.DisplayCreatePin

    lateinit var callback: SecondStageFragmentCallback
    lateinit var translations: PinLockTranslations
    lateinit var pinlockData: PinlockData

    lateinit var operatorId: String
    lateinit var pinLockPin: String
    lateinit var clientSecret: String


    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefs = PinLockPrefs(
            context.getSharedPreferences(
                Constants.PIN_PREFERENCE,
                Context.MODE_PRIVATE
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LayoutInflater.from(context).inflate(
            R.layout.fragment_pin_lock, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set default state to pin creation
        var state: PinLockState = PinLockState.DisplayCreatePin

        // set state from intent
        if (arguments?.containsKey(Constants.EXTRA_KEY_PIN_STATE) == true) {
            state = arguments?.getSerializable(Constants.EXTRA_KEY_PIN_STATE) as PinLockState
        }

        if (arguments?.containsKey("PinlockTranslations") == true) {
            translations = arguments?.getSerializable("PinlockTranslations") as PinLockTranslations
        }

        if (arguments?.containsKey("PinlockData") == true) {
            pinlockData = arguments?.getSerializable("PinlockData") as PinlockData
        }

        initializeViews()

        operatorId = pinlockData.operatorId
        pinLockPin = pinlockData.pin
        clientSecret = pinlockData.clientSecret

        // init pin input observer
        mViewModel.pinInput.observe(viewLifecycleOwner, PinObserver())
        // get saved pin from shared preferences
        mViewModel.pinInput.value = mViewModel.getPin(operatorId, pinLockPin, clientSecret)
        // init pin lock state observer
        mViewModel.pinLockState.observe(viewLifecycleOwner, PinStateObserver())

        mViewModel.setPinLockState(state)

    }

    fun initializeChangedTranslations(pinLockTranslations: PinLockTranslations){
        translations = pinLockTranslations
    }

    private fun initializeViews() {
        mCircleField = pin_activity_pin_field
        mCircleField.showSoftInputOnFocus = false
        mCircleField.onTextCompleteListener = this
        mHeaderText = pin_activity_header
        mCancelPin = pin_activity_cancel
        mRemainingAttempts = pin_activity_attempts

        pin_activity_pin_1.setOnClickListener(this)
        pin_activity_pin_2.setOnClickListener(this)
        pin_activity_pin_3.setOnClickListener(this)
        pin_activity_pin_4.setOnClickListener(this)
        pin_activity_pin_5.setOnClickListener(this)
        pin_activity_pin_6.setOnClickListener(this)
        pin_activity_pin_7.setOnClickListener(this)
        pin_activity_pin_8.setOnClickListener(this)
        pin_activity_pin_9.setOnClickListener(this)
        pin_activity_pin_0.setOnClickListener(this)

        pin_activity_backspace.setOnClickListener(this)
        pin_activity_cancel.setOnClickListener(this)

        mRemainingAttempts.text = String.format(
            translations.pinLockAttempts + " (%1\$d)",
            mRemainingAttemptsCount
        )
        mCancelPin.text = translations.pinLockCancelButton
    }

    override fun onTextComplete(enteredText: String): Boolean {
        when (pinLockState) {
            is PinLockState.DisplayCreatePin -> {
                onCreatePinTextComplete(enteredText)
            }
            is PinLockState.DisplayConfirmPin -> {
                onConfirmPinTextComplete(enteredText)
            }
            is PinLockState.DisplayLockedScreen -> {
                onLockScreenTextComplete(enteredText)
            }
        }
        clearPinInputField()
        return true
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.pin_activity_cancel -> {
                clearPinInputField()
                callback.onCancelPinLockSetup()
                removeFragment()
            }
            else -> {
                if (view is ImageView) {
                    mCircleField.setText(mCircleField.text?.dropLast(1))
                } else if (view is TextView) {
                    // num pad clicked, append text to current pin input
                    mCircleField.text?.append(view.text)
                }
            }
        }
    }

    private fun renderProcess(renderState: PinLockState) {
        when (renderState) {
            is PinLockState.DisplayCreatePin -> {
                setHeaderCreatePin()
            }
            is PinLockState.DisplayConfirmPin -> {
                setHeaderConfirmPin()
            }
            is PinLockState.DisplayLockedScreen -> {
                setHeaderEnterOldPin()
            }
        }

        pinLockState = renderState
    }

    //-------------------- onTextComplete ----------------------//

    private fun onCreatePinTextComplete(enteredText: String) {
        // no pin is created yet, store initial pin in view model
        mViewModel.pinInput.value = enteredText

        // user is from update state
        if (!mViewModel.getPin(operatorId, pinLockPin, clientSecret).isNullOrEmpty()) {
            setHeaderVerifyNewPin()
        } else {
            setHeaderConfirmPin()
        }

        // proceed to confirm pin state
        pinLockState = PinLockState.DisplayConfirmPin
    }

    private fun onConfirmPinTextComplete(enteredText: String) {
        if (mViewModel.confirmPin(enteredText)) {
            // save pin
            mViewModel.setPin(enteredText, operatorId, clientSecret, prefs)
            removeFragment()
        } else {
            // restart pin creation
            pinLockState = PinLockState.DisplayCreatePin

            // user is from update state
            if (!mViewModel.getPin(operatorId, pinLockPin, clientSecret).isNullOrEmpty()) {
                setHeaderIncorrectPinNew()
            } else {
                setHeaderIncorrectPin()
            }
        }
    }

    private fun onLockScreenTextComplete(enteredText: String) {
        mRemainingAttemptsCount = MAX_ATTEMPTS - mTryCount

        if (mViewModel.confirmPin(enteredText)) {
            removeFragment()
        } else {
            setHeaderIncorrectPin()

            if (mRemainingAttemptsCount <= 0) {
                callback.onPinInputMaxAttempt()
                removeFragment()
            } else {
                mTryCount++
                // update remaining attempts message
                mRemainingAttempts.text = String.format(
                    translations.pinLockAttempts + " (%1\$d)",
                    mRemainingAttemptsCount
                )
            }
        }
    }

    // ----------------- SETTERS ------------------//

    private fun setHeaderCreatePin() {
        mHeaderText.text = translations.pinLockCreate
        pin_activity_cancel.visibility = View.VISIBLE
        pin_activity_attempts.visibility = View.GONE
    }

    private fun setHeaderConfirmPin() {
        mHeaderText.text = translations.pinLockConfirm
    }

    private fun setHeaderEnterOldPin() {
        mHeaderText.text = translations.pinLockEnter
        pin_activity_cancel.visibility = View.GONE
        pin_activity_attempts.visibility = View.VISIBLE
    }

    private fun setHeaderIncorrectPin() {
        mHeaderText.text = translations.pinLockIncorrect
    }

    private fun setHeaderIncorrectPinNew() {
        mHeaderText.text = translations.pinLockIncorrectNew
    }

    private fun setHeaderVerifyNewPin() {
        mHeaderText.text = translations.pinLockConfirmVerify
    }

    private fun clearPinInputField() {
        mCircleField.text?.clear()
    }

    fun removeFragment() {
        // re-init try count
        mRemainingAttemptsCount = MAX_ATTEMPTS
        mTryCount = INITIAL_TRY_COUNT
        callback.onRemoveSecondStageFragment()
    }


    inner class PinObserver : Observer<String> {
        override fun onChanged(newPin: String?) {
            userPin = newPin
        }
    }

    inner class PinStateObserver : Observer<PinLockState> {
        override fun onChanged(newState: PinLockState?) {
            when (newState) {
                is PinLockState -> renderProcess(newState)
                else -> Log.d(this::class.java.name, "$newState is an invalid state")
            }
        }
    }
}