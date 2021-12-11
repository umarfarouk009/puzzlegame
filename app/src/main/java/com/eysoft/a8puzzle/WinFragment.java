package com.eysoft.a8puzzle;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import view.BoardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;


public class WinFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this.getContext());
        String name;
        if(acct != null){
            if (!acct.getDisplayName().equals(null))
                name = acct.getDisplayName();
            else
                name = "";
        }else
            name = "";

        return new AlertDialog.Builder(getActivity()).setTitle("Congratulations").setMessage("Wow "+name+",you actually solved it \nScore: "+BoardView.highestScore+"")
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.shuffle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).boardView.newPuzzle("123 456 780");
                    }
                })
                .create();
    }
}