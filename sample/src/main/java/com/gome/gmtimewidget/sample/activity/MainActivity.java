package com.gome.gmtimewidget.sample.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.gome.gmtimewidget.sample.R;
import com.gome.gmtimewidget.sample.adapter.CustomPagerAdapter;
import com.gome.gmtimewidget.sample.fragment.AlarmFragment;
import com.gome.gmtimewidget.sample.fragment.ClockFragment;
import com.gome.gmtimewidget.sample.fragment.StopwatchFragment;
import com.gome.gmtimewidget.sample.fragment.TimerFragment;
import com.gome.gmtimewidget.widget.GMTimeSwitcher;

/**
 * @author Felix.Liang
 */
public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private ViewPager pager;
    private GMTimeSwitcher switcher;
    private RadioGroup rgPagerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switcher = (GMTimeSwitcher) findViewById(R.id.switcher);
        pager = (ViewPager) findViewById(R.id.pager);
        rgPagerController = (RadioGroup) findViewById(R.id.rg_pager_controller);
        initViewPager();
        initRadioGroup();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        checkRadioButton(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("test_a", "onDestroy: ");
    }

    private void initViewPager() {
        CustomPagerAdapter adapter = new CustomPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new AlarmFragment());
        adapter.addFragment(new ClockFragment());
        adapter.addFragment(new StopwatchFragment());
        adapter.addFragment(new TimerFragment());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(4);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.e("test_a", "onPageSelected: " + position);
                checkRadioButton(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        switcher.bindViewPager(pager);
    }

    private void initRadioGroup() {
        rgPagerController.setOnCheckedChangeListener(this);
        checkRadioButton(0);
    }

    private void checkRadioButton(int index) {
        Log.e("test_a", "checkRadioButton: " + index);
        if (!((RadioButton) rgPagerController.getChildAt(index)).isChecked())
            rgPagerController.check(rgPagerController.getChildAt(index).getId());
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton rb = (RadioButton) findViewById(checkedId);
        pager.setCurrentItem(rgPagerController.indexOfChild(rb));
    }

    public GMTimeSwitcher getSwitcher() {
        return switcher;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initWindow();
    }

    private void initWindow() {
        final Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }
}
