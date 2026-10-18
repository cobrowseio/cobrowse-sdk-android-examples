package io.cobrowse.standalone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;

import io.cobrowse.CobrowseIO;
import io.cobrowse.Session;

public class CustomRemoteControlConsentDialogFragment extends DialogFragment {

    public CustomRemoteControlConsentDialogFragment() {
        super();
    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        this.setCancelable(false);
        return new AlertDialog.Builder(getActivity())
                .setTitle("Custom: Allow remote control?")
                .setMessage("A support agent would like to control this app. Do you accept?")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int w) {
                        Session session = CobrowseIO.instance().currentSession();
                        if (session != null) session.setRemoteControl(Session.RemoteControlState.On, null);
                    }})
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int w) {
                        Session session = CobrowseIO.instance().currentSession();
                        if (session != null) session.setRemoteControl(Session.RemoteControlState.Rejected, null);
                    }
                })
                .create();
    }
}