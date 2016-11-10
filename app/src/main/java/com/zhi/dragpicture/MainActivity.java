package com.zhi.dragpicture;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {


    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mImageView.setOnTouchListener(new TouchListener());
    }


    /**
     * 1、查看图片，ACTION_DOWN, ACTION_MOVE
     * 2、缩放图片, ACTION_DOWN, ACTION_MOVE, ACTION_POINT_DOWN, ACTION_UP, ACTION_POINTER_UP
     */
    public final class TouchListener implements View.OnTouchListener {
        private static final int DRAG = 0x1;  // 拖动
        private static final int ZOOM = 0x2;  // 缩放

        private PointF start = new PointF(); // 记录开始位置
        private Matrix matrix = new Matrix();  // 记录矩阵
        private Matrix currentMatrix = new Matrix();  // 记录没移动前的矩阵(如果不记录矩阵初始位置，会不按照意向移动的地方移动)
        private int mode = 0;
        private float startDistance = 0.0f;
        private PointF centerPoint; // 缩放的中心点


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 触摸事件在 int类型的低8位，...000000 11111111
                case MotionEvent.ACTION_DOWN:
                    mode = DRAG;
                    start.set(event.getX(), event.getY());
                    currentMatrix.set(mImageView.getImageMatrix());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = ZOOM;
                    // 计算两点之间的距离保存
                    startDistance = distance(event);
                    if(startDistance > 10f){
                        centerPoint =  point(event);  // 得到原始位置的中心点
                        currentMatrix.set(mImageView.getImageMatrix()); // 记录imageView的当前缩放倍数
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    if(mode == DRAG){
                        float dx = event.getX() - start.x;
                        float dy = event.getY() - start.y;
                        matrix.set(currentMatrix); // 在当前图片的位置进行移动
                        matrix.postTranslate(dx, dy);
                    } else if(mode == ZOOM) { // 初始距离与结束距离的比值决定缩放倍数，在当前缩放倍数开始缩放
                        float endDistance = distance(event);
                        if(endDistance > 10f) {
                            float scale = endDistance / startDistance;  // 得到放大倍数
                            matrix.set(currentMatrix);
                            matrix.postScale(scale, scale, centerPoint.x, centerPoint.y);
                        }
                    }

                    break;
            }
            mImageView.setImageMatrix(matrix);
            return true;
        }
    }

    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float)Math.sqrt(dx*dx+dy*dy);
    }

    private PointF point(MotionEvent event){
        float dx = (event.getX(1)+event.getX(0)) / 2;
        float dy = (event.getY(1)+event.getY(0)) / 2;
        return new PointF(dx, dy);
    }
}