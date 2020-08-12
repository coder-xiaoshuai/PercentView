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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

/**
 * Created by xiaoshuai on 2016/12/18.
 * 用于显示胜率的自定义view
 */
public class WinRateView extends View {
    private static final String TAG = "PercentView";

    /*---------默认的三种情况的颜色-----------*/

    private static final int DEFAULT_COLOR_WIN = 0Xff0000;
    private static final int DEFAULT_COLOR_LOSE = 0X0000ff;
    private static final int DEFAULT_COLOR_DOGFALL = 0X00ff00;
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
    private Rect rectWin;
    private Rect rectTie;
    private Rect rectLose;

    private Paint mPaint;

    private static final int DEFAULT_SIZE = 200;//默认尺寸


    public WinRateView(Context context) {
        this(context, null);
    }

    public WinRateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WinRateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WinRateView);
        mColorWin = array.getColor(R.styleable.WinRateView_color_win, DEFAULT_COLOR_WIN);
        mColorLose = array.getColor(R.styleable.WinRateView_color_lose, DEFAULT_COLOR_LOSE);
        mColorTie = array.getColor(R.styleable.WinRateView_color_tie, DEFAULT_COLOR_DOGFALL);

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
        init();
    }

    /***
     * 进行一些初始化工作
     */
    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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

        /*------------画矩形和文字------------*/

        //因为需要让这些内容居中  我们需要先画平局的  以它为基准来画别的
        int textSize = 36;
        mPaint.setTextSize(textSize);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        int textHeight = (int) Math.ceil(fm.descent - fm.ascent);

        mPaint.setStyle(Paint.Style.FILL);
        float textLength = mPaint.measureText("胜利60%");
        //矩形的边长
        int side_size = 20;
        //矩形与文字之间的间隙
        int text_padding = 10;
        int space_raw = 80;
        mPaint.setColor(mColorTie);
        if (rectTie == null) {
            rectTie = new Rect((getWidth() - (int) textLength - side_size - text_padding) / 2, getHeight() / 2 - textHeight / 2, (getWidth() - (int) textLength - side_size - text_padding) / 2 + side_size + text_padding + (int) textLength, getHeight() / 2 - textHeight / 2 + textHeight);
        }
        canvas.drawRect((getWidth() - textLength - side_size - text_padding) / 2, getHeight() / 2 - side_size / 2, (getWidth() - textLength - side_size - text_padding) / 2 + side_size, getHeight() / 2 - side_size / 2 + side_size, mPaint);
        mPaint.setColor(Color.BLACK);
        canvas.drawText("平局" + (int) mPercentTie + "%", (getWidth() - textLength - side_size) / 2 + side_size + text_padding, getHeight() / 2 + textHeight / 2, mPaint);

        mPaint.setColor(mColorWin);
        if (rectWin == null) {
            rectWin = new Rect((getWidth() - (int) textLength - side_size - text_padding) / 2, getHeight() / 2 - textHeight / 2 - space_raw, (getWidth() - (int) textLength - side_size - text_padding) / 2 + side_size + text_padding + (int) textLength, getHeight() / 2 - textHeight / 2 - space_raw + textHeight);
        }
        canvas.drawRect((getWidth() - textLength - side_size - text_padding) / 2, getHeight() / 2 - side_size / 2 - space_raw, (getWidth() - textLength - side_size - text_padding) / 2 + side_size, getHeight() / 2 - side_size / 2 - space_raw + side_size, mPaint);
        mPaint.setColor(Color.BLACK);
        canvas.drawText("胜利" + (int) mPercentWin + "%", (getWidth() - textLength - side_size) / 2 + side_size + text_padding, getHeight() / 2 + textHeight / 2 - space_raw, mPaint);

        mPaint.setColor(mColorLose);
        if (rectLose == null) {
            rectLose = new Rect((getWidth() - (int) textLength - side_size - text_padding) / 2, getHeight() / 2 - textHeight / 2 + space_raw, (getWidth() - (int) textLength - side_size - text_padding) / 2 + side_size + text_padding + (int) textLength, getHeight() / 2 - textHeight / 2 + textHeight + space_raw);
        }
        canvas.drawRect((getWidth() - textLength - side_size - text_padding) / 2, getHeight() / 2 - side_size / 2 + side_size / 2 + space_raw, (getWidth() - textLength - side_size - text_padding) / 2 + side_size, getHeight() / 2 - side_size / 2 + side_size / 2 + space_raw + side_size, mPaint);
        mPaint.setColor(Color.BLACK);
        canvas.drawText("失败" + (int) mPercentLose + "%", (getWidth() - textLength - side_size) / 2 + side_size + text_padding, getHeight() / 2 + textHeight / 2 + space_raw, mPaint);

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
        //需要重新检验和计算
        checkIsLegal();
        calculateEachAngle();
        invalidate();
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

//        //依次进行动画
//        AnimatorSet set = new AnimatorSet();
////        set.play(animator_lose).after(animator_dogfall).after(animator_win);
//        set.playSequentially(animator_win, animator_dogfall, animator_lose);
//        //开始动画
//        set.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int currentX = (int) event.getX();
        int currentY = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //在这里可以根据自己的需求向外部暴露自己的接口
            if (rectWin.contains(currentX, currentY)) {
                Toast.makeText(this.getContext(), "胜率" + mPercentWin + "%", Toast.LENGTH_SHORT).show();
            } else if (rectTie.contains(currentX, currentY)) {
                Toast.makeText(this.getContext(), "平局率" + mPercentTie + "%", Toast.LENGTH_SHORT).show();
            } else if (rectLose.contains(currentX, currentY)) {
                Toast.makeText(this.getContext(), "失败率" + mPercentLose + "%", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

}
