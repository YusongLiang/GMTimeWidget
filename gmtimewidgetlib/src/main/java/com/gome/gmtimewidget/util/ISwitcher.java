package com.gome.gmtimewidget.util;


import androidx.viewpager.widget.ViewPager;

import com.gome.gmtimewidget.widget.AbsTimeView;

/**
 * @author Felix.Liang
 */
public interface ISwitcher {

    void check(int index);

    AbsTimeView getItem(int index);

    AbsTimeView getCheckedItem();

    int getCheckedViewId();

    void setOnCheckedChangeListener(OnCheckedChangeListener listener);

    interface OnCheckedChangeListener {

        void onCheckedChanged(AbsTimeView itemView, boolean isChecked);
    }

    void bindViewPager(ViewPager pager);
}
