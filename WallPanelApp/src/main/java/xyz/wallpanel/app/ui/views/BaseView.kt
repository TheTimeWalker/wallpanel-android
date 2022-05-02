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

package xyz.wallpanel.app.ui.views

import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.widget.LinearLayout
import xyz.wallpanel.app.R
import xyz.wallpanel.app.databinding.DialogCodeSetBinding
//import xyz.wallpanel.app.databinding.ViewKeypadBinding

abstract class BaseView : LinearLayout {

    private lateinit var binding: DialogCodeSetBinding

    var currentCode: String = ""
    var codeComplete = false
    var enteredCode = ""

    constructor(context: Context) : super(context) {
        // let's play the sound as loud as we can
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        am.setStreamVolume(AudioManager.STREAM_ALARM, amStreamMusicMaxVol, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = DialogCodeSetBinding.bind(this)
        binding.keyPad.button0.setOnClickListener {
            addPinCode("0")
        }
        binding.keyPad.button1.setOnClickListener {
            addPinCode("1")
        }
        binding.keyPad.button2.setOnClickListener {
            addPinCode("2")
        }
        binding.keyPad.button3.setOnClickListener {
            addPinCode("3")
        }
        binding.keyPad.button4.setOnClickListener {
            addPinCode("4")
        }
        binding.keyPad.button5.setOnClickListener {
            addPinCode("5")
        }
        binding.keyPad.button6.setOnClickListener {
            addPinCode("6")
        }
        binding.keyPad.button7.setOnClickListener {
            addPinCode("7")
        }
        binding.keyPad.button8.setOnClickListener {
            addPinCode("8")
        }
        binding.keyPad.button9.setOnClickListener {
            addPinCode("9")
        }
        binding.keyPad.buttonDel.setOnClickListener {
            removePinCode()
        }
        binding.keyPad.buttonDel.setOnClickListener {
            removePinCode()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }


    fun setCode(code: String) {
        currentCode = code
    }

    abstract fun onCancel()
    abstract fun removePinCode()
    abstract fun addPinCode(code: String)
    abstract fun reset()

    protected fun showFilledPins(pinsShown: Int) {
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

    companion object {
        val MAX_CODE_LENGTH = 4
    }
}