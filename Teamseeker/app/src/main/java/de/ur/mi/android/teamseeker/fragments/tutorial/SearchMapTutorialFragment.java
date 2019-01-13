package de.ur.mi.android.teamseeker.fragments.tutorial;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.ur.mi.android.teamseeker.R;

public class SearchMapTutorialFragment extends Fragment {

        public static SearchMapTutorialFragment newInstance() {

        Bundle args = new Bundle();

        SearchMapTutorialFragment fragment = new SearchMapTutorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_map_search, container, false);
        return view;
    }
}
