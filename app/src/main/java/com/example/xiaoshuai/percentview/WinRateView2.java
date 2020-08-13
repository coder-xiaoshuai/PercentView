package com.example.xiaoshuai.percentview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

/**
 * Created by xiaoshuai on 2016/12/18.
 * 用于显示胜率的自定义view
 */
public class WinRateView2 extends RelativeLayout implements View.OnClickListener {
    private static final String TAG = "WinRateView2";

    /*---------默认的三种情况的颜色-----------*/
    private static final int DEFAULT_COLOR_WIN = 0Xff0000;
    private static final int DEFAULT_COLOR_LOSE = 0X0000ff;
    private static final int DEFAULT_COLOR_TIE = 0X00ff00;
    private static final float DEFAULT_PERCENT = 100f / 3;

    /*-----------自定义属性的相关变量-----------*/
    private int mColorWin;
    private int mColorLose;
    private int mColorTie;
    private float mPercentWin;
    private float mPercentLose;
    private float mPercentTie;
    private boolean isStartAnimator;

    private float angle_win_start;
    private float angle_win_end;
    private float angle_win_tmp;
    private float angle_tie_start;
    private float angle_tie_end;
    private float angle_tie_tmp;
    private float angle_lose_start;
    private float angle_lose_end;
    private float angle_lose_tmp;
    private RectF rectF;

    private Paint mPaint;
    private View rectWin;
    private View rectTie;
    private View rectLose;
    private TextView textWinRate;
    private TextView textTieRate;
    private TextView textLoseRate;
    private LinearLayout itemWinRate;
    private LinearLayout itemTieRate;
    private LinearLayout itemLoseRate;

    private static final int DEFAULT_SIZE = 200;//默认尺寸


    public WinRateView2(Context context) {
        this(context, null);
    }

    public WinRateView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WinRateView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WinRateView);
        mColorWin = array.getColor(R.styleable.WinRateView_color_win, DEFAULT_COLOR_WIN);
        mColorLose = array.getColor(R.styleable.WinRateView_color_lose, DEFAULT_COLOR_LOSE);
        mColorTie = array.getColor(R.styleable.WinRateView_color_tie, DEFAULT_COLOR_TIE);

        mPercentWin = array.getFloat(R.styleable.WinRateView_win_percent, DEFAULT_PERCENT);
        mPercentLose = array.getFloat(R.styleable.WinRateView_lose_percent, DEFAULT_PERCENT);
        mPercentTie = array.getFloat(R.styleable.WinRateView_tie_percent, DEFAULT_PERCENT);

        isStartAnimator = array.getBoolean(R.styleable.WinRateView_is_startAnimator, false);

        checkIsLegal();
        calculateEachAngle();
        //是否开启动画
        if (isStartAnimator) {
            startAnimation();
        }
        array.recycle();
        View centerView = LayoutInflater.from(context).inflate(R.layout.layout_center_win_rate, this, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(centerView,layoutParams);
        initPaintAndView();
        initEvent();
    }

    /***
     * 进行一些初始化工作
     */
    private void initPaintAndView() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        rectWin = findViewById(R.id.rect_rate_win);
        rectWin.setBackgroundColor(mColorWin);
        rectTie = findViewById(R.id.rect_rate_tie);
        rectTie.setBackgroundColor(mColorTie);
        rectLose = findViewById(R.id.rect_rate_lose);
        rectLose.setBackgroundColor(mColorLose);
        textWinRate = findViewById(R.id.text_rate_win);
        textWinRate.setText("胜率" + mPercentWin + "%");
        textTieRate = findViewById(R.id.text_rate_tie);
        textTieRate.setText("平局率" + mPercentTie + "%");
        textLoseRate = findViewById(R.id.text_rate_lose);
        textLoseRate.setText("失败率" + mPercentLose + "%");
        itemWinRate = findViewById(R.id.item_rate_win);
        itemTieRate = findViewById(R.id.item_rate_tie);
        itemLoseRate = findViewById(R.id.item_rate_lose);
    }

    private void initEvent(){
        itemWinRate.setOnClickListener(this);
        itemTieRate.setOnClickListener(this);
        itemLoseRate.setOnClickListener(this);
    }

    /**
     * 检查是否合法
     */
    private void checkIsLegal() {
        if ((mPercentWin + mPercentLose + mPercentTie) != 100) {
            try {
                throw new Exception("三种情况之和必须是一百");
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPercentWin = mPercentLose = mPercentTie = DEFAULT_PERCENT;
        }
    }

    /**
     * 根据角度计算每种情况的 起始结束角度
     */
    private void calculateEachAngle() {
        //+2是为了连接处留一点空隙
        angle_win_start = 270 + 2;
        angle_win_tmp = 0;
        angle_win_end = mPercentWin == 0 ? 0 : 360 * mPercentWin / 100 - 2;


        angle_tie_start = (270 + 360 * mPercentWin / 100) % 360 + 2;
        angle_tie_tmp = 0;
        angle_tie_end = mPercentTie == 0 ? 0 : 360 * mPercentTie / 100 - 2;

        angle_lose_start = (270 + 360 * (mPercentWin / 100 + mPercentTie / 100)) % 360 + 2;
        angle_lose_tmp = 0;
        angle_lose_end = mPercentLose == 0 ? 0 : 360 * mPercentLose / 100 - 2;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        /*------------先绘制外层弧线------------*/
        float lineWidth = 20;
        mPaint.setStrokeWidth(20);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        if (rectF == null) {
            rectF = new RectF(getPaddingLeft() + lineWidth / 2, getPaddingTop() + lineWidth / 2, getWidth() - 2 * getPaddingLeft() - lineWidth / 2, getHeight() - 2 * getPaddingTop() - lineWidth / 2);
        }
        //起始角度为270度
        if (isStartAnimator) {
            mPaint.setColor(mColorWin);
            canvas.drawArc(rectF, angle_win_start, angle_win_tmp, false, mPaint);
            mPaint.setColor(mColorTie);
            canvas.drawArc(rectF, angle_tie_start, angle_tie_tmp, false, mPaint);
            mPaint.setColor(mColorLose);
            canvas.drawArc(rectF, angle_lose_start, angle_lose_tmp, false, mPaint);
            Log.d(TAG, "-----angle_win_tmp" + angle_win_tmp);
        } else {
            mPaint.setColor(mColorWin);
            canvas.drawArc(rectF, angle_win_start, angle_win_end, false, mPaint);
            mPaint.setColor(mColorTie);
            canvas.drawArc(rectF, angle_tie_start, angle_tie_end, false, mPaint);
            mPaint.setColor(mColorLose);
            canvas.drawArc(rectF, angle_lose_start, angle_lose_end, false, mPaint);
        }
    }


    /**
     * 对自定义view进行测量
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            //宽高都是wrap_content的时候 我们指定默认大小
            setMeasuredDimension(DEFAULT_SIZE, DEFAULT_SIZE);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_SIZE, DEFAULT_SIZE);
        } else if (heightMeasureSpec == MeasureSpec.AT_MOST) {
            setMeasuredDimension(DEFAULT_SIZE, DEFAULT_SIZE);
        } else {
            int minSize = Math.min(widthSpecSize, heightSpecSize);
            setMeasuredDimension(minSize, minSize);
        }

    }

    /**
     * 设置各种情况的百分比
     *
     * @param percentWin
     * @param percentTie
     * @param percentLose
     */
    public void setEachPercent(float percentWin, float percentTie, float percentLose) {
        this.mPercentWin = percentWin;
        this.mPercentTie = percentTie;
        this.mPercentLose = percentLose;
        textWinRate.setText("胜率" + mPercentWin + "%");
        textTieRate.setText("平局率" + mPercentTie + "%");
        textLoseRate.setText("失败率" + mPercentLose + "%");
        //需要重新检验和计算
        checkIsLegal();
        calculateEachAngle();
    }


    /**
     * 开启动画效果
     */
    private void startAnimation() {
        ValueAnimator animator_win = ValueAnimator.ofFloat(0, angle_win_end);
        animator_win.setDuration(500);
        animator_win.setInterpolator(new LinearInterpolator());
        animator_win.start();
        final ValueAnimator animator_dogfall = ValueAnimator.ofFloat(0, angle_tie_end);
        animator_dogfall.setDuration(500);
        animator_dogfall.setInterpolator(new LinearInterpolator());
        final ValueAnimator animator_lose = ValueAnimator.ofFloat(0, angle_lose_end);
        animator_lose.setDuration(500);
        animator_lose.setInterpolator(new LinearInterpolator());

        animator_win.start();
        animator_dogfall.start();
        animator_lose.start();
        animator_win.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //进行重绘形成动画效果
                angle_win_tmp = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator_lose.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //进行重绘形成动画效果
                angle_lose_tmp = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator_dogfall.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                angle_tie_tmp = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_rate_win:
                Toast.makeText(this.getContext(), "查看胜率", Toast.LENGTH_SHORT).show();
                break;
            case R.id.item_rate_tie:
                Toast.makeText(this.getContext(), "查看平局率", Toast.LENGTH_SHORT).show();
                break;
            case R.id.item_rate_lose:
                Toast.makeText(this.getContext(), "查看失败率", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
