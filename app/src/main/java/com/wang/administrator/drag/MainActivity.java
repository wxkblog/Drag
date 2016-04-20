package com.wang.administrator.drag;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import static java.lang.Math.sqrt;

public class MainActivity extends Activity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image);

        imageView.setOnTouchListener(new TouchListener());
        Log.e("IMAGEVIEW", "iii");
    }

    private final class TouchListener implements View.OnTouchListener {

        //记录是拖拉照片模式还是放大缩小照片模式
        private int mode = 0;//初始状态
        //拖拉模式、缩放和旋转模式
        private static final int MODE_DRAG = 1;
        private static final int MODE_ZOOM = 2;

        //记录开始的坐标位置
        private PointF startPoint = new PointF();
        //记录拖拉图片移动的坐标位置
        private Matrix matrix = new Matrix();
        //记录图片要进行拖拉时候的坐标位置
        private Matrix currentMatrix = new Matrix();

        //两个手指的开始距离
        private float startDis;
        //两个手指的中间点
        private PointF midPoint;
        //初始角度
        float oldRotation = 0f;

        //计算两个手指间的距离
        private float distance(MotionEvent event) {
            double dx = event.getX(1) - event.getX(0);
            double dy = event.getY(1) - event.getY(0);
            //勾股定理计算距离
            return (float) sqrt(dx * dx + dy * dy);
        }

        // 取旋转角度
        private float rotation(MotionEvent event) {
            double delta_x = (event.getX(0) - event.getX(1));
            double delta_y = (event.getY(0) - event.getY(1));
            double radians = Math.atan2(delta_y, delta_x);
            return (float) Math.toDegrees(radians);
        }

        //计算中间坐标
        private PointF getMidPoint(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //通过运算保留最后八位
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                //手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    Log.e("IMAGEVIEW", "ACTION_DOWN");
                    mode = MODE_DRAG;
                    //记录ImageView当前的移动位置
                    currentMatrix.set(imageView.getImageMatrix());
                    matrix.set(currentMatrix);
                    startPoint.set(event.getX(), event.getY());
                    break;

                //手指在屏幕上移动，该事件会不断被触发
                case MotionEvent.ACTION_MOVE:
                    Log.e("IMAGEVIEW", "ACTION_MOVE");
                    //拖拉图片
                    if (mode == MODE_DRAG) {
                        Log.e("IMAGEVIEW", " MODE_DRAG");
                        float dx = event.getX() - startPoint.x;
                        float dy = event.getY() - startPoint.y;
                        //在没有移动之前的位置上进行移动
                        matrix.set(currentMatrix);
                        matrix.postTranslate(dx, dy);
                    } else if (mode == MODE_ZOOM) {
                        Log.e("IMAGEVIEW", " MODE_ZOOM");
                        float endDis = distance(event);
                        if (endDis > 10f) {//两个手指并拢在一起的时候像素大于10
                            float scale = endDis / startDis;//得到缩放倍数
                            matrix.set(currentMatrix);
                            //sx,sy控制x,y方向上的缩放比例，设置maxtrix以px,py为轴心进行缩放
                            matrix.postScale(scale, scale, midPoint.x, midPoint.y);

                            float rotation = rotation(event) - oldRotation;//获取旋转角度
                            matrix.postRotate(rotation, midPoint.x, midPoint.y);
                        }
                    }
                    break;
                //手指离开屏幕
                case MotionEvent.ACTION_UP:
                    //当触电离开屏幕，但是屏幕上还有触电（手指）
                case MotionEvent.ACTION_POINTER_UP:
                    Log.e("IMAGEVIEW", " ACTION_POINTER_UP");
                    mode = 0;
                    break;
                //当屏幕上已经有触点，再有一个触点压下屏幕
                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.e("IMAGEVIEW", "ACTION_POINTER_DOWN");
                    mode = MODE_ZOOM;
                    startDis = distance(event);
                    oldRotation = rotation(event);
                    midPoint = getMidPoint(event);
                    currentMatrix.set(imageView.getImageMatrix());
                    break;
            }
            //设置图片
            imageView.setImageMatrix(matrix);
            return true;
        }
    }
}