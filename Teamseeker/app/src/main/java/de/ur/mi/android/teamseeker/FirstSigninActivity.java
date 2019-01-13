package de.ur.mi.android.teamseeker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

import de.ur.mi.android.teamseeker.adapters.TutorialPagerAdapter;

public class FirstSigninActivity extends AppCompatActivity {
    ViewPager viewPager;
    PagerAdapter pagerAdapter;

    private SeekBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstsignin);
        viewPager = findViewById(R.id.viewPager_container);
        pagerAdapter = new TutorialPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        setupListener();
        setupProgressBar();
    }
    private void setupProgressBar(){
        progressBar = findViewById(R.id.seekBar_tutorialProgress);
    }
    private void setupListener(){
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int progress = (int)((double)position / (double)(pagerAdapter.getCount() - 1) * 100);
                progressBar.setProgress(progress);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
