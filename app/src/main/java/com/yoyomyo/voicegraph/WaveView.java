package com.yoyomyo.voicegraph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import static com.yoyomyo.voicegraph.MainActivity.RECORDER_SAMPLERATE;

/**
 * Takes in a list of short and draw them on canvas
 */

public class WaveView extends View {

    private static final int SPEED = RECORDER_SAMPLERATE/1000;
    private short[] data;

    long startTime;
    long animationDuration;
    Matrix matrix = new Matrix(); // transformation matrix

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setData(short[] data) {
        this.data = data;
        // start the animation:
        this.startTime = System.currentTimeMillis();
        this.animationDuration = data.length/RECORDER_SAMPLERATE * 1000;
        this.postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long elapsedTime = System.currentTimeMillis() - startTime;

        if (data == null || data.length == 0) {
            return;
        }

        Paint myPaint = new Paint();
        myPaint.setColor(getResources().getColor(R.color.colorAccent));

        matrix.postTranslate(- SPEED * elapsedTime/1000, 0); // move 10 pixels to the right
        canvas.concat(matrix);        // call this before drawing on the canvas!!

        float height = canvas.getHeight();
        int verticalOffset = (int) height/2;

        for (int i = 0; i < data.length -1 ; i++) {
            canvas.drawLine((float)i, data[i] + verticalOffset,
                    (float) i + 1, data[i+1] + verticalOffset, myPaint);
        }

        if (elapsedTime < animationDuration) {
            this.postInvalidateDelayed(1000 / 60);
        }
    }
}