package de.ur.mi.android.teamseeker.fragments.tutorial;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.ur.mi.android.teamseeker.FirstSigninActivity;
import de.ur.mi.android.teamseeker.ProfileActivity;
import de.ur.mi.android.teamseeker.R;

public class FinishSetupTutorialFragment extends Fragment {

        public static FinishSetupTutorialFragment newInstance() {

        Bundle args = new Bundle();

        FinishSetupTutorialFragment fragment = new FinishSetupTutorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial_finishsetup, container, false);
        setOnClickListener(view);
        return view;
    }
    private void setOnClickListener(View view){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                    profileIntent.putExtra(getString(R.string.intent_key_firstsetup), true);
                    startActivity(profileIntent);
                }
            });
    }
}
