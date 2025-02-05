package io.cobrowse.standalone.ui.sessioncode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.cobrowse.standalone.R;

public class CodeDisplay extends Fragment {

    @Nullable String code = null;
    @NonNull Float alpha = 0.04f;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_cobrowse_code_display, container, false);
        render(v);
        return v;
    }

    public void render(@Nullable View v) {
        if (v == null) return;
        TextView codeView = v.findViewById(R.id.cobrowse_code);
        String code = this.code != null ? this.code : "000000";
        codeView.setText(String.format("%s-%s", code.substring(0, 3), code.substring(3)));
        codeView.setAlpha(alpha);
    }

    public void setCode(@Nullable String code) {
        this.code = code;
        alpha = 1.0f;
        render(getView());
    }

}