package com.example.vanrefreshlayout.view; 

import com.example.vanrefreshlayout.R;
  
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;  
import android.graphics.Canvas; 
import android.graphics.PointF;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.AttributeSet;  
import android.util.Log;
import android.view.MotionEvent;  
import android.view.View;  
import android.view.ViewConfiguration; 
import android.view.ViewGroup;    
import android.widget.AbsListView;
import android.widget.RelativeLayout;  
   
public class PullToRefreshLayout extends RelativeLayout 
{  
    public static int ANIMATION_DURATION = 300;
    public static final String TAG = "PullToRefreshLayout";  
    // 下拉刷新  
    public static final int PULL_TO_REFRESH = 0;  
    public static final int RELEASE_TO_REFRESH = 1; 
    public static final int REFRESHING = 2; 
    // 当前状态  
    private int state = PULL_TO_REFRESH;  
    // 刷新回调接口  
    private OnRefreshListener mListener;  
    // 刷新成功  
    public static final int REFRESH_SUCCEED = 0;  
    public static final int REFRESH_FAIL = 1;  
    // 下拉头  
    private View headView; 
    private View contentView;  
    // 按下Y坐标，上一个事件点Y坐标  
    private PointF lastEvent = new PointF(); 
    // 下拉的距离  
    public float moveDeltaY = 0;  
    // 释放刷新的距离  
    private float mOffsetToRefresh = 200;  
    private float headViewWidth = 0;
    // 回滚速度  
    public float MOVE_SPEED = 8;   
    private int mPagingTouchSlop; 
    // 是否可以下拉  
    private boolean canPull = true; 
    private boolean mDisableWhenHorizontalMove = true; 
    private boolean mPreventForHorizontal = false;
    // 在刷新过程中滑动操作  
    //private boolean isTouchInRefreshing = false;
    private float radio = 2;  

    private ScrollingImageView scrollingBg1, scrollingBg2; 
    private FlipImageView vanView;
    private MotionEvent mDownEvent;   
  
    public PullToRefreshLayout(Context context)  
    {  
        this(context, null);
    }  
  
    public PullToRefreshLayout(Context context, AttributeSet attrs)  
    {  
        this(context, attrs, 0);
    }  
  
    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle)  
    {  
        super(context, attrs, defStyle);
        initView();
    }  
    
	private void initView()  
	{    
        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
//        mHeaderView = header;
//        addView(header);
	}
    
    public void setOnRefreshListener(OnRefreshListener listener)  
    {  
        mListener = listener;  
    }     
    
    public void disableWhenHorizontalMove(boolean disable) {
        mDisableWhenHorizontalMove = disable;
    }

    @Override
    protected void onFinishInflate() {
        final int childCount = getChildCount();
        
        if (childCount != 2) {
            throw new IllegalStateException("PtrFrameLayout only can host 2 elements");
        } else if (childCount == 2) {
	        scrollingBg1 = (ScrollingImageView) findViewById(R.id.scrolling_bg1);
	        scrollingBg2 = (ScrollingImageView) findViewById(R.id.scrolling_bg2);
	        vanView = (FlipImageView) findViewById(R.id.van);
	        headView = getChildAt(0);  
	        contentView = getChildAt(1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        if (headView != null) {
            measureChildWithMargins(headView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mOffsetToRefresh = ((ViewGroup) headView).getMeasuredHeight(); 
            headViewWidth = ((ViewGroup) headView).getMeasuredWidth();
        }

        if (contentView != null) {
            measureContentView(contentView, widthMeasureSpec, heightMeasureSpec);
        }
    }
    
    private void measureContentView(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
		final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
		
		final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
		getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
		final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
		getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);
		
		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
	}
    
    @Override  
    protected void dispatchDraw(Canvas canvas)  
    {  
        super.dispatchDraw(canvas);  
    }   

    @Override  
    protected void onLayout(boolean changed, int l, int t, int r, int b)  
    {  
        if (canPull)  
        {  
            // 改变子控件的布局  
            headView.layout(0, (int) moveDeltaY - headView.getMeasuredHeight(), headView.getMeasuredWidth(), (int) moveDeltaY);  
            contentView.layout(0, (int) moveDeltaY, contentView.getMeasuredWidth(), (int) moveDeltaY + contentView.getMeasuredHeight());  
        }
        else{ 
        	super.onLayout(changed, l, t, r, b);
        }  
    }  
    
    private void hideHeadView(final float to){
    	ValueAnimator animator = ValueAnimator.ofFloat(moveDeltaY, to); 
	    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {  
	        @Override  
	        public void onAnimationUpdate(ValueAnimator valueAnimator) { 
	        	moveDeltaY = (Float) valueAnimator.getAnimatedValue();
                requestLayout();
	        }  
	    });          
	    animator.addListener(new AnimatorListenerAdapter() {  
        	@Override  
            public void onAnimationStart(Animator animation) {
        		isAnimationMoving = true;
        	}
        	
	        @Override  
	        public void onAnimationEnd(Animator animation) { 
	        	isAnimationMoving = false;
	            if(to <= 0){
		            vanView.setTranslationX(0);
		            changeState(PULL_TO_REFRESH);
	            }
	        }  
	    });
	    animator.setDuration(ANIMATION_DURATION);
	    animator.start();
    }

    private boolean isAnimationMoving = false;
    private void showHeadView(){
    	ValueAnimator animator = ValueAnimator.ofInt(0, (int)mOffsetToRefresh); 
	    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {  
	        @Override  
	        public void onAnimationUpdate(ValueAnimator valueAnimator) { 
	        	moveDeltaY = (Integer) valueAnimator.getAnimatedValue();
	            vanView.setTranslationX(moveDeltaY*(headViewWidth-vanView.getWidth())/2/mOffsetToRefresh);
                requestLayout();
	        }  
	    });          
	    animator.addListener(new AnimatorListenerAdapter() {  
        	@Override  
            public void onAnimationStart(Animator animation) {
        		isAnimationMoving = true;
        	}
        	
	        @Override  
	        public void onAnimationEnd(Animator animation) {
	            changeState(REFRESHING);
	            isAnimationMoving = false;
	        }  
	    });
	    animator.setDuration(ANIMATION_DURATION);
	    animator.start();
    }
    
    /** 
     * 完成刷新操作，显示刷新结果 
     */  
    public void refreshFinish(int refreshResult)  
    {  
    	float to; 
        switch (refreshResult)  
        {  
	        case REFRESH_SUCCEED:
	        	to = headViewWidth;
	            break;  
	        case REFRESH_FAIL:
	        	to = 0;
	            break;  
	        default:  
	        	to = 0;
	            break;  
        }  
        ObjectAnimator animator = ObjectAnimator.ofFloat(vanView, "translationX", to);
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter() {
        	@Override  
            public void onAnimationStart(Animator animation) {
        		isAnimationMoving = true;
            	scrollingBg1.stop();
            	scrollingBg2.stop(); 
            	vanView.stop();
        	}
        	
	        @Override  
	        public void onAnimationEnd(Animator animation) {
	            isAnimationMoving = false;  
                hideHeadView(0);
	        }  
        });
        animator.start();
    }  
  
    private void changeState(int to)  
    {  
        state = to;  
        switch (state)  
        {  
	        case PULL_TO_REFRESH:  
	        case RELEASE_TO_REFRESH:
	        	float mv = moveDeltaY*(headViewWidth-vanView.getWidth())/2/mOffsetToRefresh;
	        	if(state == PULL_TO_REFRESH && mv <= (headViewWidth-vanView.getWidth())/2){
	        		vanView.setTranslationX(mv);
	        	}
	            break;  
	        case REFRESHING:
	        	scrollingBg1.start();
	        	scrollingBg2.start();
	        	vanView.start();
	            break;  
	        default:  
	            break;  
        }  
    }  
  
  
    @Override  
    public boolean dispatchTouchEvent(MotionEvent ev)  
    {
    	if(isAnimationMoving){
    		return super.dispatchTouchEvent(ev);
    	}
        switch (ev.getActionMasked())  
        {  
	        case MotionEvent.ACTION_DOWN: 
                mPreventForHorizontal = false;
	            mDownEvent = ev; 
	            lastEvent.set(ev.getX(), ev.getY());
	            break;  
	        case MotionEvent.ACTION_MOVE:  
	            float offsetY = ev.getY() - lastEvent.y;
                float offsetX = ev.getX() - lastEvent.x;
	            boolean moveDown = offsetY > 0;
                if (mDisableWhenHorizontalMove && !mPreventForHorizontal && (Math.abs(offsetX) > mPagingTouchSlop || Math.abs(offsetX) > 3 * Math.abs(offsetY))) {
                    //header not moved
                	if (moveDeltaY == 0) {
                        mPreventForHorizontal = true;
                    }
                }
                if (mPreventForHorizontal) {
                    break;
                }
	        	if ((moveDown || state == REFRESHING) && checkContentCanBePulledDown(contentView, headView)){
	        		
	                // 对实际滑动距离做缩小，造成用力拉的感觉  
	                moveDeltaY += offsetY / radio;  
	                if (moveDeltaY < 0)  
	                    moveDeltaY = 0;  
	                if (moveDeltaY > getMeasuredHeight())  
	                    moveDeltaY = getMeasuredHeight();  
		            lastEvent.set(ev.getX(), ev.getY());
		            // 根据下拉距离改变比例  
		            radio = (float) (2 + Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));  
		            requestLayout();
	                if (state == REFRESHING)  
	                {  
	                    // 正在刷新的时候触摸移动  
	                    //isTouchInRefreshing = true;
	                }
	                else{
			            if (moveDeltaY <= mOffsetToRefresh)  
			            {
			                changeState(PULL_TO_REFRESH);  
			            }
			            else 
			            {  
			                changeState(RELEASE_TO_REFRESH);  
			            }
	                }  
	            	sendCancelEvent(); 
		            return true; 
	            }
	            break;  
	        case MotionEvent.ACTION_UP:  
	            if (moveDeltaY > mOffsetToRefresh) 
	            {
	                // 正在刷新时往下拉释放后下拉头不隐藏  
	                //isTouchInRefreshing = false;  
		            if (state == RELEASE_TO_REFRESH)  
		            {  
		                changeState(REFRESHING);  
		                // 刷新操作  
		                if (mListener != null)  
		                    mListener.onRefresh();  
		            }  
		            hideHeadView(mOffsetToRefresh);  
	                return true;
	            }
	            else if(moveDeltaY > 0 && state == PULL_TO_REFRESH){
	            	hideHeadView(0);  
	                return true;
	            }
	            //这是如果return ture，则会消费MOVE_UP事件，子控件接受不到事件
	        default:  
	            break;  
        }
        return super.dispatchTouchEvent(ev);  
    }  
  
    private void sendCancelEvent() {
        MotionEvent e = MotionEvent.obtain(mDownEvent.getDownTime(), 
        		mDownEvent.getEventTime() + ViewConfiguration.getLongPressTimeout(), 
        		MotionEvent.ACTION_CANCEL, 
        		mDownEvent.getX(), 
        		mDownEvent.getY(), 
        		mDownEvent.getMetaState());
        super.dispatchTouchEvent(e);
    }     
    
    public void setRefreshing(boolean isRefreshing){
        //isTouchInRefreshing = isRefreshing;  
        if(isRefreshing){ 
        	if(!isAnimationMoving && state != REFRESHING){
        		showHeadView();
        	}
        }
        else{	    	
        	refreshFinish(REFRESH_SUCCEED); 
        }
    }    
    
    public static boolean canChildScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }

    public static boolean checkContentCanBePulledDown(View content, View header) {
        return !canChildScrollUp(content);
    }
    
    /*public static boolean checkContentCanBePulledDown(View content, View header) {
        if (!(content instanceof ViewGroup)) {
            return true;
        }

        ViewGroup viewGroup = (ViewGroup) content;
        if (viewGroup.getChildCount() == 0) {
            return true;
        }

        if (viewGroup instanceof AdapterView) {
        	@SuppressWarnings("rawtypes")
			AdapterView listView = (AdapterView) viewGroup;
            if (listView.getFirstVisiblePosition() > 0) {
                return false;
            }
        }

//        if (Build.VERSION.SDK_INT >= 14) {
//            return !content.canScrollVertically(-1);
//        } else {
//            if (viewGroup instanceof ScrollView || viewGroup instanceof AbsListView) {
//                return viewGroup.getScrollY() == 0;
//            }
//        }

        View child = viewGroup.getChildAt(0);
        ViewGroup.LayoutParams glp = child.getLayoutParams();
        int top = child.getTop();
        if (glp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) glp;
            return top == mlp.topMargin + viewGroup.getPaddingTop();
        } else {
            return top == viewGroup.getPaddingTop();
        }
    }*/
}  
