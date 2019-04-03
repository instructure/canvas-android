/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.ktmodels.User
import com.instructure.androidfoosball.utils.bind
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk21.listeners.onClick

class ConfirmPinDialog(
        context: Context,
        val user: User,
        var createNewPin: Boolean = false,
        var onConfirmed: (User) -> Unit
) : Dialog(context) {

    private enum class State {
        NEW_PIN,
        CONFIRM_NEW,
        CONFIRM_EXISTING
    }

    private var mNewPinHash = ""
    private var mPin = ""
    private var mState = State.CONFIRM_EXISTING

    private val mMessage by bind<TextView>(R.id.pin_message)
    private val mPinDisplay by bind<TextView>(R.id.pin_display)
    private val mPinPad by bind<PinPad>(R.id.pin_pad)
    private val mBackspace by bind<View>(R.id.backspace)
    private val mUserName by bind<TextView>(R.id.userName)

    private var onPinHashCreated: (String) -> Unit = { updateUserPinHash(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_confirm_pin)
        setupListeners()
        mUserName.text = user.name
        if (createNewPin) createPin() else confirmPin()
    }

    private fun setupListeners() {
        mPinPad.onButtonTapped = onButtonTapped
        mBackspace.onClick { updatePin(mPin.dropLast(1)) }
    }

    private val onButtonTapped = { number: String ->
        updatePin(mPin + number)
        if (mPin.length == 4) {
            val hash = mPin.hashCode().toString()
            when (mState) {
                ConfirmPinDialog.State.NEW_PIN -> {
                    mNewPinHash = hash
                    setMessage(R.string.confirm_pin, R.color.confirm_blue)
                    mState = State.CONFIRM_NEW
                }
                ConfirmPinDialog.State.CONFIRM_NEW -> {
                    if (hash != mNewPinHash) {
                        setMessage(R.string.pin_mismatch, R.color.error_red)
                        mNewPinHash = ""
                        mState = State.NEW_PIN
                    } else {
                        onPinHashCreated(hash)
                    }
                }
                ConfirmPinDialog.State.CONFIRM_EXISTING -> {
                    if (hash != user.pinHash) {
                        setMessage(R.string.incorrect_pin, R.color.error_red)
                    } else {
                        dismiss()
                        onConfirmed(user)
                    }
                }
            }
            updatePin("")
        }
    }

    private fun updatePin(newPin: String) {
        mPin = newPin
        mPinDisplay.text = "•".repeat(mPin.length)
    }

    fun overrideOnPinHashCreated(block: (String) -> Unit): ConfirmPinDialog {
        onPinHashCreated = {
            block(it)
            dismiss()
        }
        return this
    }

    override fun show() {
        if ("true" == user.pinDisabled) {
            onConfirmed(user)
        } else  if (user.pinHash.isNullOrBlank()) {
            showInfoDialog()
        } else {
            super.show()
        }
    }

    private fun showInfoDialog() {
        AlertDialog.Builder(context)
                .setTitle(R.string.create_pin)
                .setMessage(R.string.pin_dialog_info_message)
                .setPositiveButton(R.string.create_pin) { dialog, which ->
                    createNewPin = true
                    super.show()
                }
                .setNegativeButton(R.string.maybe_later) { dialog, which ->
                    onConfirmed(user)
                }
                .setNeutralButton(R.string.disable_pin) { dialog, which ->
                    showConfirmDisablePinDialog()
                }
                .show()
    }

    private fun showConfirmDisablePinDialog() {
        AlertDialog.Builder(context)
                .setMessage(R.string.disable_pin_confirmation_message)
                .setPositiveButton(android.R.string.yes) { dialog, which ->
                    FirebaseDatabase.getInstance().reference.child("users").child(user.id).child("pinDisabled").setValue("true")
                    Toast.makeText(context, R.string.pin_has_been_disabled, Toast.LENGTH_SHORT).show()
                    onConfirmed(user)
                }
                .setNegativeButton(android.R.string.no) { dialog, which ->
                    showInfoDialog()
                }
                .show()
    }

    private fun createPin() {
        mState = State.NEW_PIN
        setMessage(R.string.enter_pin)
    }

    private fun confirmPin() {
        mState = State.CONFIRM_EXISTING
        setMessage(R.string.confirm_pin)
    }

    private fun setMessage(messageResId: Int, colorResId: Int? = null) {
        mMessage.setText(messageResId)
        if (colorResId == null) {
            mMessage.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            mMessage.setBackgroundColor(Color.TRANSPARENT)
        } else {
            mMessage.setTextColor(Color.WHITE)
            mMessage.backgroundColor = ContextCompat.getColor(context, colorResId)
        }
    }

    private fun updateUserPinHash(pinHash: String) {
        FirebaseDatabase.getInstance().reference.child("users").child(user.id).child("pinHash").setValue(pinHash)
        Toast.makeText(context, R.string.pin_saved, Toast.LENGTH_SHORT).show()
        dismiss()
        onConfirmed(user)
    }
}
