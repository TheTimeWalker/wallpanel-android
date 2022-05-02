/*
 * Copyright (c) 2022 Wallpanel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.wallpanel.app.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.wallpanel.app.R
import xyz.wallpanel.app.databinding.FragmentCodeBottomSheetBinding

class CodeBottomSheetFragment (private val alarmListener: OnAlarmCodeFragmentListener) : BottomSheetDialogFragment() {

    private var codeComplete = false
    private var enteredCode = ""
    private lateinit var binding: FragmentCodeBottomSheetBinding
    private val handler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val delayRunnable = object : Runnable {
        override fun run() {
            handler.removeCallbacks(this)
            if(codeComplete) {
                onComplete(enteredCode)
            } else {
                onError()
            }
        }
    }

    interface OnAlarmCodeFragmentListener {
        fun onComplete(code: String)
        fun onCodeError()
        fun onCancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeCodeButton.setOnClickListener {
            onCancel()
        }
        binding.include.button0.setOnClickListener {
            addPinCode("0")
        }
        binding.include.button1.setOnClickListener {
            addPinCode("1")
        }
        binding.include.button2.setOnClickListener {
            addPinCode("2")
        }
        binding.include.button3.setOnClickListener {
            addPinCode("3")
        }
        binding.include.button4.setOnClickListener {
            addPinCode("4")
        }
        binding.include.button5.setOnClickListener {
            addPinCode("5")
        }
        binding.include.button6.setOnClickListener {
            addPinCode("6")
        }
        binding.include.button7.setOnClickListener {
            addPinCode("7")
        }
        binding.include.button8.setOnClickListener {
            addPinCode("8")
        }
        binding.include.button9.setOnClickListener {
            addPinCode("9")
        }
        binding.include.buttonDel.setOnClickListener {
            removePinCode()
        }
        binding.include.buttonDel.setOnClickListener {
            removePinCode()
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog
                val bottomSheetResId = com.google.android.material.R.id.design_bottom_sheet
                val bottomSheet = dialog.findViewById<View>(bottomSheetResId) as FrameLayout?
                val behavior = BottomSheetBehavior.from(bottomSheet!!)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        })
    }

    @Nullable
    override fun onCreateView(@NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        binding = FragmentCodeBottomSheetBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    private fun onCancel() {
        enteredCode = ""
        showFilledPins(0)
        alarmListener.onCancel()
        dismiss()
    }

    private fun onComplete(code: String) {
        alarmListener.onComplete(code)
        dismiss()
    }

    private fun onError() {
        codeComplete = false
        alarmListener.onCodeError()
        enteredCode = ""
        showFilledPins(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(delayRunnable)
    }

    private fun addPinCode(code: String) {
        if (codeComplete) return
        enteredCode += code
        showFilledPins(enteredCode.length)
        if (enteredCode.length == MAX_CODE_LENGTH && enteredCode == currentCode) {
            codeComplete = true
            handler.postDelayed(delayRunnable, 500)
        } else if (enteredCode.length == MAX_CODE_LENGTH) {
            handler.postDelayed(delayRunnable, 500)
        }
    }

    private fun removePinCode() {
        if (codeComplete) return
        if (enteredCode.isNotEmpty()) {
            enteredCode = enteredCode.substring(0, enteredCode.length - 1)
            showFilledPins(enteredCode.length)
        }
    }

    private fun showFilledPins(pinsShown: Int) {
        if (binding.pinCode1 != null && binding.pinCode2 != null && binding.pinCode3 != null && binding.pinCode4 != null) {
            when (pinsShown) {
                1 -> {
                    binding.pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode2.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    binding.pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    binding.pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                }
                2 -> {
                    binding.pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    binding.pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                }
                3 -> {
                    binding.pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode3.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                }
                4 -> {
                    binding.pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode3.setImageResource(R.drawable.ic_radio_button_checked_black)
                    binding.pinCode4.setImageResource(R.drawable.ic_radio_button_checked_black)
                }
                else -> {
                    binding.pinCode1.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    binding.pinCode2.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    binding.pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                    binding.pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black)
                }
            }
        }
    }

    companion object {
        private  val MAX_CODE_LENGTH = 4
        private var currentCode: String = ""

        fun newInstance(code: String, listener : OnAlarmCodeFragmentListener): CodeBottomSheetFragment {
            currentCode = code
            return CodeBottomSheetFragment(listener)
        }
    }
}