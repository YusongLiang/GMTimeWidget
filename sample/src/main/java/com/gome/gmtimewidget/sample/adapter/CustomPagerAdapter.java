package com.gome.gmtimewidget.sample.adapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Felix.Liang
 */
public class CustomPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments = new ArrayList<>();

    public CustomPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public void addFragment(Fragment element) {
        mFragments.add(element);
    }

    public void addFragment(int index, Fragment element) {
        mFragments.add(index, element);
    }

    @Override
    public @NonNull Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
