package com.ozgurcebi.istanbul_nobetci_eczaneler

import android.content.ComponentCallbacks
import android.content.Context
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import java.util.logging.Handler

class DialogManager {

    companion object{
        fun showAlert(context: Context, title: String, message: String, buttonTitle1: String, buttonTitle2: String? = null, button1Callback : ()->Unit, button2Callback: (()->Unit)? = null) {
            val builder = AlertDialog.Builder(context)

            // Set the alert dialog title
            builder.setTitle(title)

            // Display a message on alert dialog
            builder.setMessage(message)

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton(buttonTitle1){dialog, which ->
                // Do something when user press the positive button
               button1Callback.invoke()
            }

            // Display a negative button on alert dialog
            if(buttonTitle2 != null) {
                builder.setNegativeButton(buttonTitle2) { dialog, which ->
                    button2Callback?.invoke()
                }
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            android.os.Handler(Looper.getMainLooper()).post {
                dialog.show()
            }


        }
    }


}