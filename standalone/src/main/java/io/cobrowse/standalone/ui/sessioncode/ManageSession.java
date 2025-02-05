package io.cobrowse.standalone.ui.sessioncode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.cobrowse.standalone.R;

public class ManageSession extends Fragment {

   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_cobrowse_code_manage_session, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      view.findViewById(R.id.cobrowse_end_session).setOnClickListener(it -> {
         final Fragment parent = getParentFragment();
         if (parent instanceof CobrowseCodeFragment) {
            ((CobrowseCodeFragment) parent).endSessionClicked();
         }
      });
   }

}