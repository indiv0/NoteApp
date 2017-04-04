package com.seg3125.noteapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Ashgangabar on 2017-04-03.
 */

public class EnterPassword extends DialogFragment {

    private NoteEntity note;

    public EnterPassword() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.enter_password, null);
        final EditText password1 = (EditText) view.findViewById(R.id.EditText_Pwd1);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String strPassword = password1.getText().toString();

                EditNoteActivity callingActivity = (EditNoteActivity) getActivity();
                callingActivity.checkPassword(strPassword);
            }
        });
        setCancelable(false);
        return builder.create();
    }
}