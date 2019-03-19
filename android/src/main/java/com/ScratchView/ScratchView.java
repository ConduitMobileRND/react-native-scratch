package com.ScratchView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.InputStream;
import java.net.URL;

public class ScratchView extends View implements View.OnTouchListener {
    float threshold = 0;
    float brushSize = 0;
    String imageUrl = null;
    Bitmap image;
    Path path;
    boolean[][] grid = new boolean[10][10];
    boolean cleared;
    int clearPointsCounter;
    float scratchProgress;
    int placeholderColor = -1;

    Paint imagePaint = new Paint();
    Paint pathPaint = new Paint();


    public ScratchView(Context context) {
        super(context);
        reset();
    }

    public ScratchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        reset();
    }

    public ScratchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        reset();
    }

    public void setPlaceholderColor(@Nullable String placeholderColor) {
        if (placeholderColor != null) {
            try {
                this.placeholderColor = Color.parseColor(placeholderColor);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public void setBrushSize(float brushSize) {
        this.brushSize = brushSize * 3;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        reset();
    }

    public void reset() {

        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);

        imagePaint.setAntiAlias(true);
        imagePaint.setFilterBitmap(true);

        pathPaint.setAlpha(0);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        pathPaint.setAntiAlias(true);

        setLayerType(View.LAYER_TYPE_SOFTWARE,null);

        initGrid();

        if (imageUrl != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream is = (InputStream) new URL(imageUrl).getContent();
                        image = BitmapFactory.decodeStream(is).copy(Bitmap.Config.ARGB_8888, true);
                        reportImageLoadFinished(true);
                        invalidate();

                    } catch (Exception e) {
                        reportImageLoadFinished(false);
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        threshold = threshold > 0 ? threshold : 50;
        path = null;

        reportScratchProgress();
        reportScratchState();
    }

    public void initGrid() {
        int gridSize = grid[0].length;
        for (int x = 0; x < gridSize; x++)
        {
            for (int y = 0; y < gridSize; y++)
            {
                grid[x][y] = true;
            }
        }
        clearPointsCounter = 0;
        cleared = false;
        scratchProgress = 0;
    }

    public void updateGrid (int x, int y) {
        int gridSize = grid[0].length - 1;
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        int pointInGridX = (int)((Math.max(Math.min(x, viewWidth), 0) / viewWidth) * (float)gridSize);
        int pointInGridY = (int)((Math.max(Math.min(y, viewWidth), 0) / viewHeight) * (float)gridSize);
        if (grid[pointInGridX][pointInGridY] == true) {
            grid[pointInGridX][pointInGridY] = false;
            clearPointsCounter++;
            scratchProgress = ((float)clearPointsCounter) / ((float)((gridSize+1)*(gridSize+1))) * 100;
            reportScratchProgress();
            if (!cleared && scratchProgress > threshold) {
                cleared = true;
                reportScratchState();
            }
        }
    }

    public void reportImageLoadFinished(boolean success) {
        final Context context = getContext();
        if (context instanceof ReactContext) {
            WritableMap event = Arguments.createMap();
            event.putBoolean("success", success);
            ((ReactContext) context).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), RNTScratchViewManager.EVENT_IMAGE_LOAD, event);
        }
    }

    public void reportTouchState(boolean state) {
        final Context context = getContext();
        if (context instanceof ReactContext) {
            WritableMap event = Arguments.createMap();
            event.putBoolean("touchState", state);
            ((ReactContext) context).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), RNTScratchViewManager.EVENT_TOUCH_STATE_CHANGED, event);
        }
    }

    public void reportScratchProgress() {
        final Context context = getContext();
        if (context instanceof ReactContext) {
            WritableMap event = Arguments.createMap();
            event.putDouble("progressValue", scratchProgress);
            ((ReactContext) context).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), RNTScratchViewManager.EVENT_SCRATCH_PROGRESS_CHANGED, event);
        }
    }

    public void reportScratchState() {
        final Context context = getContext();
        if (context instanceof ReactContext) {
            WritableMap event = Arguments.createMap();
            event.putBoolean("isScratchDone", cleared);
            ((ReactContext) context).getJSModule(RCTEventEmitter.class).receiveEvent(getId(), RNTScratchViewManager.EVENT_SCRATCH_DONE, event);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (image == null) {
            canvas.drawColor(this.placeholderColor != -1 ? this.placeholderColor : Color.GRAY);
            return;
        }
        canvas.drawBitmap(image, new Rect(0, 0, image.getWidth(), image.getHeight()), new Rect(0, 0, getWidth(), getHeight()), imagePaint);
        if (path != null) {
            canvas.drawPath(path, pathPaint);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                reportTouchState(true);
                float strokeWidth = brushSize > 0 ? brushSize : ((getHeight() < getWidth() ? getHeight() : getWidth()) / 10f);
                pathPaint.setStrokeWidth(strokeWidth);
                path = new Path();
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (path != null) {
                    path.lineTo(x, y);
                    updateGrid(x, y);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                reportTouchState(false);
                image = createBitmapFromView();
                path = null;
                break;
        }
        invalidate();
        return true;
    }

    public Bitmap createBitmapFromView() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        draw(c);
        return bitmap;
    }
}