package com.example.nurmemet.advertizebanner;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;

/**
 * Created by nurmemet on 2015/12/12.
 */
public class AdverTizeBanner extends ViewGroup {


    private BannerAdapter mAdapter;
    int mTouchSlop;
    int mMaximumVelocity;
    private int mDuration = 2000;
    private Handler handler;
    private int mCurrentScreen;
    private int mAdapterPosition = 0;

    //布局孩子节点的空间的大小
    private Rect availableSpace = new Rect();


    private ArrayList<View> viewList = new ArrayList<View>();


    public AdverTizeBanner(Context context) {
        super(context);
        init();
    }

    public AdverTizeBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AdverTizeBanner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        handler = new Handler();


    }

    public void setDuration(int duration) {
        mDuration = duration;
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean b = super.dispatchTouchEvent(ev);
        System.out.println("dispatch");
        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int maskAction = event.getActionMasked();


        switch (maskAction) {
            case MotionEvent.ACTION_DOWN:
                //Toast.makeText(this.getContext(),"第一次按下",Toast.LENGTH_SHORT).show();
                System.out.println("第一次按下");
                break;
            case MotionEvent.ACTION_UP:
                //Toast.makeText(this.getContext(),"手指松开",Toast.LENGTH_SHORT).show();
                System.out.println("手指松开");
                break;
            case MotionEvent.ACTION_MOVE:
                //Toast.makeText(this.getContext(),"手指一动",Toast.LENGTH_SHORT).show();
                System.out.println("手指一动");
                break;
            case MotionEvent.ACTION_CANCEL:
                //Toast.makeText(this.getContext(),"取消",Toast.LENGTH_SHORT).show();
                System.out.println("取消");
                break;
            case MotionEvent.ACTION_OUTSIDE:
                //Toast.makeText(this.getContext(),"ui之外的触摸事件",Toast.LENGTH_SHORT).show();
                System.out.println("ui之外的触摸事件");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // Toast.makeText(this.getContext(),"单手指按下",Toast.LENGTH_SHORT).show();
                System.out.println("单手指按下");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //Toast.makeText(this.getContext(),"单手指松开",Toast.LENGTH_SHORT).show();
                System.out.println("单手指松开");
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                //Toast.makeText(this.getContext(),"滑过",Toast.LENGTH_SHORT).show();
                System.out.println("滑过");
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                //Toast.makeText(this.getContext(),"滑过离开",Toast.LENGTH_SHORT).show();
                System.out.println("滑过离开");
                break;

            case MotionEvent.ACTION_HOVER_ENTER:
                //Toast.makeText(this.getContext(),"滑过进入",Toast.LENGTH_SHORT).show();
                System.out.println("滑过进入");
                break;
            case MotionEvent.ACTION_POINTER_INDEX_SHIFT:
                // Toast.makeText(this.getContext(),"手指移动",Toast.LENGTH_SHORT).show();
                System.out.println("手指移动");
                break;

        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        dispatchLayout();
    }

    private void dispatchLayout() {
        fill();
    }

    private void fill() {
        if (mAdapter == null) {
            throw new IllegalStateException("adapter can not be null");
        }
        //此操作不会导致requestLayout
        //detachAllViewsFromParent();
        //更新布局空间
        updateAvailableSpace();
        //mCurrentLayoutPosition = -1;
        int count = mAdapter.getCount();
        for (int i = 0; i < 3; i++) {
            View temp = getViewFromPool(i);
            if (temp == null) {
                View view = mAdapter.getView(i, temp, i);
                viewList.add(i, view);
                addView(view);
                measureChildWithMargins(view, 0, 0);
                layoutChild(view, i);
            } else {
                layoutChild(viewList.get(i), i);
            }

        }
    }

    private void updateAvailableSpace() {
        availableSpace.left = getLeft() + getPaddingLeft();
        availableSpace.top = getTop() + getPaddingTop();
        availableSpace.right = getRight() - getPaddingRight();
        availableSpace.bottom = getBottom() - getPaddingBottom();
    }


    private View getViewFromPool(int position) {
        if (position < viewList.size()) {
            return viewList.get(position);
        }
        return null;
    }

    private void layoutChild(View view, int position) {
        Rect rf = new Rect(availableSpace);
        int width = rf.width();
        rf.left = rf.left + position * (rf.right - rf.left);
        rf.right = rf.left + width;
        view.layout(rf.left, rf.top, rf.right, rf.bottom);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final int widthSpec = getChildMeasureSpec(getWidth(),
                getPaddingLeft() + getPaddingRight());
        final int heightSpec = getChildMeasureSpec(getHeight(),
                getPaddingTop() + getPaddingBottom());
        child.measure(widthSpec, heightSpec);
    }

    public static int getChildMeasureSpec(int parentSize, int padding) {
        int resultSize = Math.max(0, parentSize - padding);
        int resultMode = MeasureSpec.EXACTLY;
        return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }


    public void setAdapter(BannerAdapter adapter) {
        setAdapterInternal(adapter);
        requestLayout();
    }

    private void setAdapterInternal(BannerAdapter adapter) {
        if (adapter != null) {
            mAdapter = adapter;
        } else {
            throw new IllegalStateException("adapter can not be null");
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }


    public static abstract class BannerAdapter {
        public BannerAdapter() {
        }

        abstract public int getCount();

        abstract public View getView(int position, View mainView, int childIndex);

        public void notifyDataSetChannged() {
        }

    }

    public void start() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                int fromX = mCurrentScreen * getWidth();
                int toX = (mCurrentScreen + 1) * getWidth();
                ValueAnimator animator = ValueAnimator.ofInt(fromX, toX);
                animator.setDuration(700);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (Integer) animation.getAnimatedValue();
                        scrollTo(value, getScrollY());
                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mAdapterPosition++;
                        if (mAdapterPosition == mAdapter.getCount()) {
                            mAdapterPosition = 0;
                        }
                        mCurrentScreen++;
                        mAdapter.getView(mAdapterPosition, viewList.get(mCurrentScreen), mCurrentScreen);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        if (mCurrentScreen == 2) {
                            mCurrentScreen = 0;
                            mAdapter.getView(mAdapterPosition, viewList.get(mCurrentScreen), mCurrentScreen);
                            scrollTo(0, getScrollY());
                        }
                        start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            }
        }, 3000);


    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }
}
