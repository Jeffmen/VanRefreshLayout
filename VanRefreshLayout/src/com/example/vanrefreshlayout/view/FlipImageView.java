package com.example.vanrefreshlayout.view;
 
import com.example.vanrefreshlayout.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import static java.lang.Math.abs;

public class FlipImageView extends View {
    private final int speed;
    private final Bitmap bitmap;

    private Rect clipBounds = new Rect();
    private float offset = 0;

    private boolean isStarted;
    private boolean isAdd = true; 
    private Paint mPaint;// ����
    private static final float BITMAP_MARGIN = 0.067F;
    private static final float SHADOW_MARGIN = 0.054F;
    private static final float SHADOW_HEIGHT = 0.19F;
    private float bitmapHeight, shadowMargin, shadowHeight, bitmapMargin;

    public FlipImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlipView, 0, 0);
        try {
            speed = ta.getDimensionPixelSize(R.styleable.FlipView_speed, 10);
            bitmap = BitmapFactory.decodeResource(getResources(), ta.getResourceId(R.styleable.FlipView_src, 0));
        } finally {
            ta.recycle();
        }
        bitmapHeight = bitmap.getHeight();
        bitmapMargin = bitmap.getWidth() * BITMAP_MARGIN;
        shadowMargin = bitmap.getWidth() * SHADOW_MARGIN;
        shadowHeight = bitmapHeight * SHADOW_HEIGHT;
        initPaint();
        //start();
    }
    
    private void initPaint() {  
        // ʵ��������  
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG); 
        mPaint.setStyle(Paint.Style.FILL);  
        //mPaint.setColor(Color.BLUE);
        mPaint.setColor(0xFF969696);
        // ���û��������˾�  
        mPaint.setMaskFilter(new BlurMaskFilter(6, BlurMaskFilter.Blur.NORMAL));  
        setLayerType(LAYER_TYPE_SOFTWARE, null); 
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int)(bitmap.getWidth()*(1+BITMAP_MARGIN)), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }
        
        canvas.getClipBounds(clipBounds);

        float radio = (float) (12 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * offset));  
        float top = clipBounds.height()-bitmapHeight-shadowHeight/4-bitmapMargin-offset/radio;
        RectF re = new RectF(shadowMargin+offset/radio/2, 
        		getMeasuredHeight()-shadowHeight-shadowMargin-bitmapMargin, 
        		getMeasuredWidth()-shadowMargin-offset/radio/2, 
        		getMeasuredHeight()-shadowMargin-bitmapMargin);
        
        canvas.drawOval(re, mPaint);
        canvas.drawBitmap(bitmap, bitmapMargin, top, null);
        
        if (isStarted) {
        	if(isAdd){
        		offset += abs(speed);
        	}
        	else{
                offset -= abs(speed);
        	}
            if (offset > clipBounds.height()-bitmapHeight) {
                offset = offset%clipBounds.height()-bitmapHeight;
                isAdd = false;
            }
            if (offset < 0) {
                offset = 0;
                isAdd = true;
            }
            postInvalidateOnAnimation();
        }
    }

    /**
     * Start the animation
     */
    public void start() {
        if (!isStarted) {
            isStarted = true;
            postInvalidateOnAnimation();
        }
    }

    /**
     * Stop the animation
     */
    public void stop() {
        if (isStarted) {
            isStarted = false;
            offset = 0;
            invalidate();
        }
    }
}
