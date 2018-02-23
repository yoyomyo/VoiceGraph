package com.yoyomyo.voicegraph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

/**
 * Takes in a list of short and draw them on canvas
 */

public class WaveView extends View {

    public float[] data;
    private int width;
    private int height;

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //setData();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = widthMeasureSpec;
        height = heightMeasureSpec;
    }

    public void setData(float[] data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.length == 0) {
            return;
        }

        Paint myPaint = new Paint();
        myPaint.setColor(getResources().getColor(R.color.colorAccent));

        for (int i =0; i < data.length -1 ; i++) {
            //android.util.Log.d("Yun", i + " : " + data[i]);
            canvas.drawLine((float)i, data[i] + 200, (float) i + 1, data[i+1] + 200, myPaint);
        }
    }
}
