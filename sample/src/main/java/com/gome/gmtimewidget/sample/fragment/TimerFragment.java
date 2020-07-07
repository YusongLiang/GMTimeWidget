package com.gome.gmtimewidget.sample.fragment;

import android.view.View;
import android.widget.Button;

import com.gome.gmtimewidget.sample.R;
import com.gome.gmtimewidget.sample.activity.MainActivity;
import com.gome.gmtimewidget.widget.AbsTimeView;
import com.gome.gmtimewidget.widget.GMTimeSwitcher;
import com.gome.gmtimewidget.widget.GMTimer;

/**
 * @author Felix.Liang
 */
public class TimerFragment extends BaseFragment implements View.OnClickListener {

    private GMTimer timer;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_timer;
    }

    @Override
    protected void initView(View contentView) {
        GMTimeSwitcher switcher = ((MainActivity) getActivity()).getSwitcher();
        if (switcher != null) timer = (GMTimer) switcher.getItem(3);
        Button btTimer = (Button) contentView.findViewById(R.id.bt_timer);
        btTimer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_timer:
                if (timer != null) {
                    if (!timer.isStarted()) {
                        timer.setTotalTime(15 * 60 * 1000);
                        timer.start();
                    } else {
                        if (!timer.isPaused()) timer.pause();
                        else timer.resume();
                    }
                }
                break;
        }
    }
}
