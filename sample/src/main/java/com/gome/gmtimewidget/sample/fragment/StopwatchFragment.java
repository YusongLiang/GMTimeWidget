package com.gome.gmtimewidget.sample.fragment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gome.gmtimewidget.sample.R;
import com.gome.gmtimewidget.sample.activity.MainActivity;
import com.gome.gmtimewidget.util.IStopwatch;
import com.gome.gmtimewidget.util.TimeFormatter;
import com.gome.gmtimewidget.widget.GMStopwatch;
import com.gome.gmtimewidget.widget.GMTimeSwitcher;

/**
 * @author Felix.Liang
 */
public class StopwatchFragment extends BaseFragment implements View.OnClickListener {

    private GMStopwatch stopwatch;
    private TextView tvStopwatchTime;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stopwatch;
    }

    @Override
    protected void initView(View contentView) {
        tvStopwatchTime = (TextView) contentView.findViewById(R.id.tv_stopwatch_time);
        GMTimeSwitcher switcher = ((MainActivity) getActivity()).getSwitcher();
        if (switcher != null) {
            stopwatch = (GMStopwatch) switcher.getItem(2);
            stopwatch.setStopwatchWatcher(new IStopwatch.StopwatchListener() {
                @Override
                public void onRecord(long recordTime) {
                    Toast.makeText(getContext(), TimeFormatter.getFormatStopwatchTime(recordTime), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUpdate(long stopwatchTime) {
                    tvStopwatchTime.setText(TimeFormatter.getFormatStopwatchTime(stopwatchTime));
                }
            });
        }
        Button btStopwatch = (Button) contentView.findViewById(R.id.bt_stopwatch);
        Button btSetTime = (Button) contentView.findViewById(R.id.bt_set_time);
        btStopwatch.setOnClickListener(this);
        btSetTime.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_stopwatch:
                if (stopwatch != null) {
                    if (!stopwatch.isStarted()) {
                        stopwatch.start();
                    } else {
                        if (!stopwatch.isPaused()) stopwatch.pause();
                        else stopwatch.resume();
                    }
                }
                break;
            case R.id.bt_set_time:
                if (!stopwatch.isStarted()) {
                    stopwatch.setElapsedTime(24130);
                }
                break;
        }
    }
}
