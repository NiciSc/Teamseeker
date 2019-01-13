package de.ur.mi.android.teamseeker.fragments.tutorial;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.ur.mi.android.teamseeker.R;

public class JoinEventTutorialFragment extends Fragment {

        public static JoinEventTutorialFragment newInstance() {

        Bundle args = new Bundle();

        JoinEventTutorialFragment fragment = new JoinEventTutorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_event_join, container, false);
        return view;
    }
}
