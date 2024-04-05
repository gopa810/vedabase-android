package com.gopalapriyadasa.bhaktivedanta_textabase;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MessageBox extends DialogFragment {

    public static final int MB_OK = 0x1;
    public static final int MB_OKCANCEL = 0x2;
    public static final int MB_YESNO = 0x4;


    public static String getPositiveButtonText(int buttons) {
        switch (buttons) {
            case MB_OK: return "OK";
            case MB_OKCANCEL: return "OK";
            case MB_YESNO: return "Yes";
            default: return "OK";
        }
    }

    public static String getNegativeButtonText(int buttons) {
        switch (buttons) {
            case MB_OKCANCEL: return "Cancel";
            case MB_YESNO: return "No";
            default: return  null;
        }
    }

    public static void showMessage(final MessageBoxDelegate del, String messageText, int boxButtons, final HashMap<String, String> Data) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
        builder.setMessage(messageText);

        String buttonText;

        buttonText = MessageBox.getPositiveButtonText(boxButtons);
        if (buttonText != null) {
            builder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // FIRE ZE MISSILES!
                    if (del != null) {
                        del.messageBoxAnswerOK(Data);
                    }
                }
            });
        }

        buttonText = MessageBox.getNegativeButtonText(boxButtons);
        if (buttonText != null) {
            builder.setNegativeButton(buttonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (del != null) {
                        del.messageBoxAnswerCancel(Data);
                    }
                }
            });
        }
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();

        dialog.show();
    }
}
