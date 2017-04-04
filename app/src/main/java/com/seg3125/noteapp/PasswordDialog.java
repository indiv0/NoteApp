package com.seg3125.noteapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ashgangabar on 2017-04-03.
 */

public class PasswordDialog extends DialogFragment {

    private NoteEntity note;

    public PasswordDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.password_dialog, null);
        final EditText password1 = (EditText) view.findViewById(R.id.EditText_Pwd1);
        final EditText password2 = (EditText) view.findViewById(R.id.EditText_Pwd2);
        final TextView error = (TextView) view.findViewById(R.id.TextView_PwdProblem);

        password2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String strPass1 = password1.getText().toString();
                String strPass2 = password2.getText().toString();
                if (strPass1.equals(strPass2)) {
                    error.setText("Passwords Match");
                } else {
                    error.setText("Passwords Do Not Match");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter Lock");
        builder.setView(view);


        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String strPassword1 = password1.getText().toString();
                String strPassword2 = password2.getText().toString();

                if (strPassword1.equals(strPassword2)) {
                    EditNoteActivity callingActivity = (EditNoteActivity) getActivity();
                    callingActivity.savePassword(strPassword1);

                    if(strPassword1.toString().isEmpty())
                        Toast.makeText(callingActivity, "Password Lock Removed", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(callingActivity, "Password Lock Set", Toast.LENGTH_LONG).show();
                }
                else {
                    EditNoteActivity callingActivity = (EditNoteActivity) getActivity();
                    Toast.makeText(callingActivity, "Password Lock Not Set", Toast.LENGTH_LONG).show();
                }
            }
        });
        setCancelable(false);
        return builder.create();
    }
}