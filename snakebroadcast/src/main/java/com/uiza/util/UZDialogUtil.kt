package com.uiza.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import com.uiza.R

class UZDialogUtil {
    companion object {

        fun showDialog1(
            context: Context,
            title: String? = null,
            msg: String? = null,
            button1: String = context.getString(R.string.confirm),
            onClickButton1: ((Unit) -> Unit)? = null
        ): AlertDialog {
            val builder = AlertDialog.Builder(
                ContextThemeWrapper(
                    context,
                    android.R.style.Theme_Material_Dialog
                )
            )
            if (title.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setTitle(title)
            }
            if (msg.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setMessage(msg)
            }

            builder.setPositiveButton(button1) { _, _ ->
                onClickButton1?.invoke(Unit)
            }
            val dialog = builder.create()
            dialog.show()
            return dialog
        }

        fun showDialog2(
            context: Context,
            title: String? = null,
            msg: String? = null,
            button1: String = context.getString(R.string.confirm),
            button2: String = context.getString(R.string.cancel),
            onClickButton1: ((Unit) -> Unit)? = null,
            onClickButton2: ((Unit) -> Unit)? = null
        ): AlertDialog {
            val builder = AlertDialog.Builder(
                ContextThemeWrapper(
                    context,
                    android.R.style.Theme_Material_Dialog
                )
            )

            if (title.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setTitle(title)
            }
            if (msg.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setMessage(msg)
            }
            builder.setNegativeButton(button1) { _, _ ->
                onClickButton1?.invoke(Unit)
            }
            builder.setPositiveButton(button2) { _, _ ->
                onClickButton2?.invoke(Unit)
            }
            val dialog = builder.create()
            dialog.show()
            return dialog
        }

        fun showDialog3(
            context: Context,
            title: String? = null,
            msg: String? = null,
            button1: String? = null,
            button2: String? = null,
            button3: String? = null,
            onClickButton1: ((Unit) -> Unit)? = null,
            onClickButton2: ((Unit) -> Unit)? = null,
            onClickButton3: ((Unit) -> Unit)? = null
        ): AlertDialog {
            val builder = AlertDialog.Builder(
                ContextThemeWrapper(
                    context,
                    android.R.style.Theme_Material_Dialog
                )
            )
            if (title.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setTitle(title)
            }
            if (msg.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setMessage(msg)
            }
            if (button1.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setNegativeButton(button1) { _, _ ->
                    onClickButton1?.invoke(Unit)
                }
            }
            if (button2.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setPositiveButton(button2) { _, _ ->
                    onClickButton2?.invoke(Unit)
                }
            }
            if (button3.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setNeutralButton(button3) { _, _ ->
                    onClickButton3?.invoke(Unit)
                }
            }

            val dialog = builder.create()
            dialog.show()
            return dialog
        }

        fun showDialogList(
            context: Context,
            title: String? = null,
            arr: Array<String?>,
            onClick: ((Int) -> Unit)? = null
        ): AlertDialog {
            val builder = AlertDialog.Builder(
                ContextThemeWrapper(
                    context,
                    android.R.style.Theme_Material_Dialog
                )
            )
            if (title.isNullOrEmpty()) {
                //do nothing
            } else {
                builder.setTitle(title)
            }
            builder.setItems(arr) { _, which ->
                onClick?.invoke(which)
            }
            val dialog = builder.create()
            dialog.show()
            return dialog
        }

        fun show(dialog: Dialog?) {
            if (dialog != null && !dialog.isShowing) {
                dialog.show()
            }
        }

        fun hide(dialog: Dialog?) {
            if (dialog != null && dialog.isShowing) {
                dialog.cancel()
            }
        }
    }
}
