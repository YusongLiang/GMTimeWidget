package com.gome.gmtimewidget.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.gome.gmtimewidget.R;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Felix.Liang
 */
public class GMAlarm extends AbsTimeView {

    private boolean mAttached;
    private Paint mAxlePaint;
    private Paint mSecondHandPaint;
    private Paint mMinuteHandPaint;
    private Paint mHourHandPaint;
    private Paint mSubDialPaint;
    private Paint mScaleTextPaint;
    private int mColorAxle;
    private int mColorSecondHand;
    private int mColorMinuteHand;
    private int mColorHourHand;
    private int mColorShadow;
    private int mColorSubDial;
    private int mColorDialStart;
    private int mColorDialEnd;
    private int mColorUncheckedDial;
    private float mDialWidth;
    private float mDialLength;
    private float mAxleRadius;
    private float mSecondHandLength;
    private float mSecondHandWidth;
    private float mMinuteHandLength;
    private float mMinuteHandWidth;
    private float mHourHandLength;
    private float mHourHandWidth;
    private float mSubDialLength;
    private float mSubDialWidth;
    private int mScaleTextColor;
    private int mScaleTextSize;
    private float mDialRadius;
    private float mSubDialRadius;
    private int mScaleTextMargin;
    private float mSecondDegree;
    private float mMinuteDegree;
    private float mHourDegree;
    private int[] mDialColors = new int[3];
    private float[] mDialColorPositions = {0.5f, 0.75f, 1f};
    private float[] mDash = new float[2];
    private SweepGradient mSweepGradient;
    private String mTimeZone;
    private Calendar mTime;
    private Matrix mMatrix = new Matrix();
    private float mHourOffsetDegree;
    private float mMinuteOffsetDegree;
    private float mSecondOffsetDegree;
    private int mHourAlpha = 255;
    private int mMinuteAlpha = 255;
    private int mSecondAlpha = 255;
    private int mAxleAlpha = 255;
    private int mScaleTextAlpha = 255;
    private int mDialAlpha = 255;
    private int mSubDialAlpha = 255;
    private float mHourScale = 1;
    private float mMinuteScale = 1;
    private float mSecondScale = 1;
    private float mInitDialRadius;
    private float mInitSubDialRadius;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                final String timeZone = intent.getStringExtra("time-zone");
                createTime(timeZone);
            }
            onTimeChanged();
        }
    };

    public GMAlarm(Context context) {
        this(context, null);
    }

    public GMAlarm(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GMAlarm);
        mTimeZone = array.getString(R.styleable.GMAlarm_alarmTimeZone);
        mDialWidth = array.getDimensionPixelSize(R.styleable.GMAlarm_alarmDialWidth, dp2px(1f));
        mDialLength = array.getDimensionPixelSize(R.styleable.GMAlarm_alarmDialLength, dp2px(8));
        mSubDialWidth = array.getDimensionPixelSize(R.styleable.GMAlarm_alarmSubDialWidth, dp2px(1.4f));
        mSubDialLength = array.getDimensionPixelSize(R.styleable.GMAlarm_alarmSubDialLength, dp2px(12));
        mAxleRadius = array.getDimensionPixelSize(R.styleable.GMAlarm_alarmAxleRadius, dp2px(5));
        mColorAxle = array.getColor(R.styleable.GMAlarm_alarmAxleColor, 0xFF3492E6);
        mColorSecondHand = array.getColor(R.styleable.GMAlarm_alarmSecondHandColor, 0xFF3492E6);
        mColorMinuteHand = array.getColor(R.styleable.GMAlarm_alarmMinuteHandColor, 0xCC000000);
        mColorHourHand = array.getColor(R.styleable.GMAlarm_alarmHourHandColor, 0xCC000000);
        mColorShadow = array.getColor(R.styleable.GMAlarm_alarmShadowColor, 0x30000000);
        mColorSubDial = array.getColor(R.styleable.GMAlarm_alarmSubDialColor, 0xFF4A4A4A);
        mSecondHandWidth = array.getColor(R.styleable.GMAlarm_alarmSecondHandWidth, dp2px(1));
        mMinuteHandWidth = array.getColor(R.styleable.GMAlarm_alarmMinuteHandWidth, dp2px(3.8f));
        mHourHandWidth = array.getColor(R.styleable.GMAlarm_alarmSecondHandWidth, dp2px(4.5f));
        mColorDialStart = array.getColor(R.styleable.GMAlarm_alarmDialStartColor, 0xFF538EFF);
        mColorDialEnd = array.getColor(R.styleable.GMAlarm_alarmDialEndColor, 0xFF0754FF);
        mColorUncheckedDial = array.getColor(R.styleable.GMAlarm_alarmDialUncheckedColor, 0xFF8C8C8C);
        mScaleTextSize = array.getDimensionPixelSize(R.styleable.GMAlarm_alarmScaleTextSize, sp2px(16));
        mScaleTextColor = array.getDimensionPixelSize(R.styleable.GMAlarm_alarmScaleTextColor, 0xFF4A4A4A);
        mScaleTextMargin = array.getDimensionPixelSize(R.styleable.GMAlarm_alarmScaleTextMargin, dp2px(8));
        updateDialColors();
        array.recycle();
        setStarted(true);
        initPaints();
    }

    private void updateDialColors() {
        mDialColors[0] = mColorUncheckedDial;
        mDialColors[1] = mColorDialStart;
        mDialColors[2] = mColorDialEnd;
    }

    private void initPaints() {
        mDialPaint.setStrokeWidth(mDialLength);
        mAxlePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mAxlePaint.setColor(mColorAxle);
        mAxlePaint.setStyle(Paint.Style.FILL);
        mAxlePaint.setShadowLayer(mShadowRadius, 0, mShadowOffset, mColorShadow);
        mSecondHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSecondHandPaint.setColor(mColorSecondHand);
        mSecondHandPaint.setStyle(Paint.Style.STROKE);
        mSecondHandPaint.setStrokeCap(Paint.Cap.ROUND);
        mSecondHandPaint.setStrokeWidth(mSecondHandWidth);
        mMinuteHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mMinuteHandPaint.setColor(mColorMinuteHand);
        mMinuteHandPaint.setStyle(Paint.Style.STROKE);
        mMinuteHandPaint.setStrokeCap(Paint.Cap.ROUND);
        mMinuteHandPaint.setStrokeWidth(mMinuteHandWidth);
        mHourHandPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mHourHandPaint.setColor(mColorHourHand);
        mHourHandPaint.setStyle(Paint.Style.STROKE);
        mHourHandPaint.setStrokeCap(Paint.Cap.ROUND);
        mHourHandPaint.setStrokeWidth(mHourHandWidth);
        mSubDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSubDialPaint.setStyle(Paint.Style.STROKE);
        mSubDialPaint.setStrokeWidth(mSubDialLength);
        mSubDialPaint.setColor(mColorSubDial);
        mSweepGradient = new SweepGradient(0, 0, mDialColors, mDialColorPositions);
        mDialPaint.setShader(mSweepGradient);
        mScaleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mScaleTextPaint.setTextAlign(Paint.Align.CENTER);
        mScaleTextPaint.setTextSize(mScaleTextSize);
        mScaleTextPaint.setColor(mScaleTextColor);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            registerReceiver();
            createTime(mTimeZone);
        }
    }

    private void createTime(String timeZone) {
        if (timeZone != null) {
            mTime = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            mTime = Calendar.getInstance();
        }
    }

    private void registerReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            mAttached = false;
            unregisterReceiver();
        }
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(mIntentReceiver);
    }

    @Override
    protected void onTimeChanged() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        long millis = mTime.get(Calendar.MILLISECOND);
        float second = mTime.get(Calendar.SECOND) + millis / 1000f;
        float minute = mTime.get(Calendar.MINUTE);
        float hour = mTime.get(Calendar.HOUR);
        mSecondDegree = second * 6;
        mSecondDegree %= 360;
        mMinuteDegree = minute * 6;
        mMinuteDegree %= 360;
        mHourDegree = (hour + minute / 60f) * 30;
        mHourDegree %= 360;
        updateView();
    }

    private void updateView() {
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mInitDialRadius = mBoundRadius / 1.08f - mDialLength * 0.5f;
        setDialRadius(mInitDialRadius);
        mInitSubDialRadius = mBoundRadius / 1.08f - mSubDialLength * 0.5f;
        setSubDialRadius(mInitSubDialRadius);
    }

    private void setDialRadius(float radius) {
        if (mDialRadius != radius) {
            mDialRadius = radius;
            radius += (mDialLength * 0.5f);
            mSecondHandLength = radius;
            mMinuteHandLength = radius * 0.73f;
            mHourHandLength = radius * 0.55f;
            updateDialPaint();
        }
    }

    public void setSubDialRadius(float radius) {
        if (mSubDialRadius != radius) {
            mSubDialRadius = radius;
            updateSubDialPaint();
        }
    }

    private void updateDialPaint() {
        float unitDial = (float) (2 * Math.PI * mDialRadius / 240);
        mDash[0] = mDialWidth;
        mDash[1] = unitDial - mDialWidth;
        mDialPaint.setPathEffect(new DashPathEffect(mDash, 0));
        mDialPaint.setColor(0xff000000);
    }

    private void updateSubDialPaint() {
        float unitDial = (float) (2 * Math.PI * mSubDialRadius / 12);
        mDash[0] = mSubDialWidth;
        mDash[1] = unitDial - mSubDialWidth;
        mSubDialPaint.setPathEffect(new DashPathEffect(mDash, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mXOffset, mYOffset);
        drawDials(canvas);
        drawScales(canvas);
        drawHands(canvas);
    }

    private void drawScales(Canvas canvas) {
        canvas.save();
        canvas.translate(0, dp2px(6));
        mScaleTextPaint.setAlpha(mScaleTextAlpha);
        float r = mDialRadius - mSubDialLength - mScaleTextMargin;
        for (int i = 0; i < 12; i++) {
            float x = (float) (r * Math.sin(Math.toRadians(i * 30)));
            float y = -(float) (r * Math.cos(Math.toRadians(i * 30)));
            int scale = i == 0 ? 12 : i;
            canvas.drawText(String.valueOf(scale), x, y, mScaleTextPaint);
        }
        canvas.restore();
    }

    private void drawDials(Canvas canvas) {
        mMatrix.reset();
        mMatrix.preRotate(mSecondDegree - 90);
        mSweepGradient.setLocalMatrix(mMatrix);
        mDialPaint.setShader(mSweepGradient);
        mDialPaint.setAlpha(mDialAlpha);
        mSubDialPaint.setAlpha(mSubDialAlpha);
        canvas.drawCircle(0, 0, mDialRadius, mDialPaint);
        canvas.drawCircle(0, 0, mSubDialRadius, mSubDialPaint);
    }

    private void drawHands(Canvas canvas) {
        drawHourHand(canvas);
        drawMinuteHand(canvas);
        drawSecondHand(canvas);
        drawAxle(canvas);
    }

    private void drawSecondHand(Canvas canvas) {
        canvas.save();
        canvas.scale(mSecondScale, mSecondScale);
        mSecondHandPaint.setAlpha(mSecondAlpha);
        float degree = mSecondDegree - 90 + mSecondOffsetDegree;
        setPaintShadow(degree, mSecondHandPaint, mColorShadow);
        canvas.rotate(degree);
        canvas.drawLine(0, 0, mSecondHandLength, 0, mSecondHandPaint);
        canvas.restore();
    }

    private void drawMinuteHand(Canvas canvas) {
        canvas.save();
        canvas.scale(mMinuteScale, mMinuteScale);
        mMinuteHandPaint.setAlpha(mMinuteAlpha);
        float degree = mMinuteDegree - 90 + mMinuteOffsetDegree;
        setPaintShadow(degree, mMinuteHandPaint, mColorShadow);
        canvas.rotate(degree);
        canvas.drawLine(0, 0, mMinuteHandLength, 0, mMinuteHandPaint);
        canvas.restore();
    }

    private void drawHourHand(Canvas canvas) {
        canvas.save();
        canvas.scale(mHourScale, mHourScale);
        mHourHandPaint.setAlpha(mHourAlpha);
        float degree = mHourDegree - 90 + mHourOffsetDegree;
        setPaintShadow(degree, mHourHandPaint, mColorShadow);
        canvas.rotate(degree);
        canvas.drawLine(0, 0, mHourHandLength, 0, mHourHandPaint);
        canvas.restore();
    }

    private void drawAxle(Canvas canvas) {
        mAxlePaint.setAlpha(mAxleAlpha);
        canvas.drawCircle(0, 0, mAxleRadius, mAxlePaint);
    }

    @Override
    protected void onInitShowAnimState() {
        mMinuteAlpha = 0;
        mHourAlpha = 0;
        mSecondAlpha = 0;
        mDialAlpha = 0;
        mSubDialAlpha = 0;
        mScaleTextAlpha = 0;
    }

    @Override
    protected AnimatorSet getShowAnimSet() {
        AnimatorSet animSet = new AnimatorSet();
        ValueAnimator anim1_1 = ValueAnimator.ofFloat(-30, 0);
        anim1_1.setDuration(300);
        anim1_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHourOffsetDegree = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_2 = ValueAnimator.ofInt(0, 255);
        anim1_2.setDuration(300);
        anim1_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHourAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_3 = ValueAnimator.ofFloat(0.2f, 1);
        anim1_3.setDuration(450);
        anim1_3.setInterpolator(new OvershootInterpolator(1.5f));
        anim1_3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHourScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim2_1 = ValueAnimator.ofFloat(-30, 0);
        anim2_1.setStartDelay(90);
        anim2_1.setDuration(300);
        anim2_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim2_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMinuteOffsetDegree = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_2 = ValueAnimator.ofInt(0, 255);
        anim2_2.setStartDelay(90);
        anim2_2.setDuration(300);
        anim2_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMinuteAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_3 = ValueAnimator.ofFloat(0.2f, 1);
        anim2_3.setDuration(390);
        anim2_3.setInterpolator(new OvershootInterpolator(1.5f));
        anim2_3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMinuteScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim3_1 = ValueAnimator.ofFloat(-30, 0);
        anim3_1.setStartDelay(150);
        anim3_1.setDuration(300);
        anim3_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSecondOffsetDegree = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim3_2 = ValueAnimator.ofInt(0, 255);
        anim3_2.setStartDelay(150);
        anim3_2.setDuration(300);
        anim3_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSecondAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim3_3 = ValueAnimator.ofFloat(0.2f, 1);
        anim3_3.setStartDelay(150);
        anim3_3.setDuration(330);
        anim3_3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSecondScale = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim4_1 = ValueAnimator.ofInt(0, 255);
        anim4_1.setDuration(150);
        anim4_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAxleAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim5_1 = ValueAnimator.ofInt(0, 255);
        anim5_1.setStartDelay(450);
        anim5_1.setDuration(300);
        anim5_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScaleTextAlpha = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim6_1 = ValueAnimator.ofInt(0, 255);
        anim6_1.setDuration(300);
        anim6_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim6_2 = ValueAnimator.ofFloat(0.2f, 1);
        anim6_2.setDuration(450);
        anim6_2.setInterpolator(new OvershootInterpolator(1.5f));
        anim6_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setDialRadius(((float) animation.getAnimatedValue()) * mInitDialRadius);
            }
        });
        ValueAnimator anim7_1 = ValueAnimator.ofInt(0, 255);
        anim7_1.setStartDelay(150);
        anim7_1.setDuration(300);
        anim7_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSubDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim7_2 = ValueAnimator.ofFloat(0.2f, 1);
        anim7_2.setDuration(450);
        anim7_2.setStartDelay(150);
        anim7_2.setInterpolator(new OvershootInterpolator());
        anim7_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setSubDialRadius(((float) animation.getAnimatedValue()) * mInitSubDialRadius);
                invalidate();
            }
        });
        animSet.playTogether(anim1_1, anim1_2, anim1_3,
                anim2_1, anim2_2, anim2_3,
                anim3_1, anim3_2, anim3_3,
                anim4_1, anim5_1, anim6_1,
                anim6_2, anim7_1, anim7_2
        );
        return animSet;
    }

    @Override
    protected AnimatorSet getHideAnimSet() {
        AnimatorSet animSet = new AnimatorSet();
        ValueAnimator anim1_1 = ValueAnimator.ofFloat(0, -30);
        anim1_1.setDuration(240);
        anim1_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim1_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHourOffsetDegree = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim1_2 = ValueAnimator.ofInt(255, 0);
        anim1_2.setDuration(240);
        anim1_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHourAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim1_3 = ValueAnimator.ofFloat(1, 1.04f);
        anim1_3.setStartDelay(180);
        anim1_3.setDuration(60);
        anim1_3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHourScale = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_1 = ValueAnimator.ofFloat(0, -30);
        anim2_1.setStartDelay(60);
        anim2_1.setDuration(240);
        anim2_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim2_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMinuteOffsetDegree = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim2_2 = ValueAnimator.ofInt(255, 0);
        anim2_2.setStartDelay(60);
        anim2_2.setDuration(240);
        anim2_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMinuteAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim2_3 = ValueAnimator.ofFloat(1, 1.04f, 0.945f);
        anim2_3.setStartDelay(60);
        anim2_3.setDuration(120);
        anim2_3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMinuteScale = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim3_1 = ValueAnimator.ofFloat(0, -30);
        anim3_1.setStartDelay(120);
        anim3_1.setDuration(240);
        anim3_1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim3_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSecondOffsetDegree = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim3_2 = ValueAnimator.ofInt(255, 0);
        anim3_2.setStartDelay(120);
        anim3_2.setDuration(240);
        anim3_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSecondAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim3_3 = ValueAnimator.ofFloat(1, 1.04f, 0.654f);
        anim3_3.setStartDelay(120);
        anim3_3.setDuration(240);
        anim3_3.setInterpolator(new DecelerateInterpolator());
        anim3_3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSecondScale = (float) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim4_1 = ValueAnimator.ofInt(255, 0);
        anim4_1.setStartDelay(390);
        anim4_1.setDuration(150);
        anim4_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAxleAlpha = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim4_1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAxlePaint.setShadowLayer(dp2px(2), dp2px(2), dp2px(2), mColorShadow);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        ValueAnimator anim5_1 = ValueAnimator.ofInt(255, 0);
        anim5_1.setDuration(150);
        anim5_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScaleTextAlpha = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator anim6_1 = ValueAnimator.ofInt(255, 0);
        anim6_1.setStartDelay(180);
        anim6_1.setDuration(300);
        anim6_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim6_2 = ValueAnimator.ofFloat(1, 0.2f);
        anim6_2.setStartDelay(180);
        anim6_2.setDuration(360);
        anim6_2.setInterpolator(new AnticipateInterpolator());
        anim6_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setDialRadius(mInitDialRadius * ((float) animation.getAnimatedValue()));
                invalidate();
            }
        });
        ValueAnimator anim7_1 = ValueAnimator.ofInt(255, 0);
        anim7_1.setStartDelay(90);
        anim7_1.setDuration(300);
        anim7_1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSubDialAlpha = (int) animation.getAnimatedValue();
            }
        });
        ValueAnimator anim7_2 = ValueAnimator.ofFloat(1, 0.2f);
        anim7_2.setStartDelay(90);
        anim7_2.setDuration(300);
        anim7_2.setInterpolator(new AnticipateInterpolator());
        anim7_2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setSubDialRadius(mInitSubDialRadius * ((float) animation.getAnimatedValue()));
                invalidate();
            }
        });
        animSet.playTogether(anim1_1, anim1_2, anim1_3,
                anim2_1, anim2_2, anim2_3,
                anim3_1, anim3_2, anim3_3,
                anim4_1, anim5_1, anim6_1,
                anim6_2, anim7_1, anim7_2);
        return animSet;
    }
}
