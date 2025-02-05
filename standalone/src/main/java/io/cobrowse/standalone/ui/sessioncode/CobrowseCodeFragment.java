package io.cobrowse.standalone.ui.sessioncode;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;

import io.cobrowse.Callback;
import io.cobrowse.CobrowseIO;
import io.cobrowse.Session;
import io.cobrowse.standalone.R;

import static androidx.navigation.fragment.NavHostFragment.findNavController;

/**
 * This is the sample UI we provide for generating 6 digit codes in your app.
 */
public class CobrowseCodeFragment extends Fragment {

    final static @NonNull String TAG = "CobrowseCodeFragment";

    private final @NonNull CodeDisplay codeDisplay = new CodeDisplay();
    private final @NonNull ManageSession manageView = new ManageSession();
    private final @NonNull ErrorView errorView = new ErrorView();

    private boolean wasActive = false;

    public static CobrowseCodeFragment newInstance() {
        return new CobrowseCodeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cobrowse_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void showFragment(@NonNull Fragment fragment) {
        if (isDetached()) return;
        if (isRemoving()) return;
        if (getActivity() == null) return;
        if (!isAdded()) return;
        final FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.cobrowse_fragment_container, fragment);
        transaction.commitAllowingStateLoss();
    }

    protected void createSession(final @Nullable Callback<Error, Session> callback) {
        CobrowseIO.instance().createSession(new Callback<Error, Session>() {
            public void call(@Nullable Error err, @Nullable Session session) {
                if (err != null) showError(err);
                else render(session);
                if (callback != null) callback.call(err, session);
            }
        });
    }

    protected void showError(@NonNull Error err) {
        Log.e(TAG, "Cobrowse error: " + err.getMessage());
        showFragment(errorView);
    }

    protected void render(@Nullable Session session) {
        if (session == null || session.isPending()) {
            showFragment(codeDisplay);
            if (session != null) codeDisplay.setCode(session.code());
        } else if (session.isActive()) {
            showFragment(manageView);
        }
    }

    protected void listenTo(@NonNull Session session) {
        wasActive = session.isActive();
        session.registerSessionListener(new Session.Listener() {
            @Override public void sessionDidUpdate(@NonNull Session session) {
                // automatically close fragment on session activation
                if (session.isActive() && !wasActive) close();
                else render(session);
                wasActive = session.isActive();
            }

            @Override public void sessionDidEnd(@NonNull Session session) {
                render(session);
                close();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Session current = CobrowseIO.instance().currentSession();
        if (current != null) this.listenTo(current);

        if (current == null || current.isEnded()) {
            createSession(new Callback<Error, Session>() {
                @Override
                public void call(@Nullable Error err, @Nullable Session session) {
                    if (err != null) showError(err);
                    if (session != null) listenTo(session);
                }
            });
        }

        render(current);
    }

    public void endSessionClicked() {
        Session session = CobrowseIO.instance().currentSession();
        if (session != null) {
            session.end(new Callback<Error, Session>() {
                @Override
                public void call(@Nullable Error err, @Nullable Session session) {
                    if (err != null) showError(err);
                    else close();
                }
            });
        }
    }

    private void close() {
        final NavController navigation = findNavController(this);
        final NavBackStackEntry backstack = navigation.getPreviousBackStackEntry();
        if (backstack != null && backstack.getDestination().getId() == R.id.nav_home) {
            navigation.popBackStack();
        }
    }

}
