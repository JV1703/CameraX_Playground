package com.example.intermediatesubmission.presentation.ui.custom_view

import android.content.Context
import android.graphics.Canvas
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.util.Patterns
import android.view.View.OnFocusChangeListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener
import com.example.intermediatesubmission.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    companion object {
        const val TEXT_PASSWORD = 129
        const val TEXT_EMAIL_ADDRESS = 33
    }

    init {
        validator(500L)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        validator(500L)
    }

    private fun isFieldEmpty(isEmpty: Boolean) {
        error = if (isEmpty) {
            context.getString(R.string.empty_field_error)
        } else {
            null
        }
    }

    private suspend fun validatePassword(password: String, delay: Long) {
        error = if (password.isEmpty() || password.length > 5) {
            null
        } else {
            delay(delay)
            context.getString(R.string.password_error)
        }
    }

    private suspend fun validateEmail(email: String, delay: Long) {
        error = if (email.isEmpty()) {
            null
        } else if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            null
        } else {
            delay(delay)
            context.getString(R.string.email_error)
        }
    }

    private fun validator(delay: Long) {
        var job: Job? = null
        Log.i("custom_edit_text", "input type: $inputType")
        InputType.TYPE_CLASS_TEXT
        if (inputType == TEXT_PASSWORD || inputType == TEXT_EMAIL_ADDRESS) {
            addTextChangedListener { editable ->
                job?.cancel()
                job = MainScope().launch {
                    this@CustomEditText.let {
                        if (it.inputType == TEXT_PASSWORD) {
                            validatePassword(editable.toString(), delay)
                        } else if (it.inputType == TEXT_EMAIL_ADDRESS) {
                            validateEmail(editable.toString(), delay)
                        }
                    }
                }
            }
        } else {
            onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    isFieldEmpty(text.toString().isEmpty())
                }
            }
        }
    }

}