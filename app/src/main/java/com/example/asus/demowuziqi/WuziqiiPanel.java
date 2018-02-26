package com.example.asus.demowuziqi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2016/7/22.
 */
public class WuziqiiPanel extends View {
    private int mPanelWidth;//棋盘总宽度
    private float mLineHeight;  //一格的高度
    private int MAX_LINE = 10;
    private int MAX_COUNT_INLINE= 5;

    private Paint mPaint = new Paint();

    private Bitmap mWhitePiece; //白色旗子
    private Bitmap mBlackPiece;

    private float ratioPieceOfLineHeight = 3*1.0f/4;

    private boolean mIsWhite = true; //白棋先手或者当前该白棋下
    private ArrayList<Point> mWhiteArray = new ArrayList<>(); //白棋数组
    private ArrayList<Point> mBlackArray = new ArrayList<>();//黑棋数组

    private boolean mIsGameOver;//判断游戏是否结束
    private boolean mIsWhiteWinner;//判断赢家

    private AlertDialog dialog;//弹出小窗口

    public WuziqiiPanel(Context context, AttributeSet attrs) { //棋盘
        super(context, attrs);
        //setBackgroundColor(0x44ff0000);
        init();
    }

    private void init() {
        mPaint.setColor(0x88ffffff); //设置画笔颜色
        mPaint.setAntiAlias(true);//设置画笔锯齿效果（防边角锯齿）
        mPaint.setDither(true);//设置防抖动
        mPaint.setStyle(Paint.Style.STROKE);//设置画笔风格

        mWhitePiece = BitmapFactory.decodeResource(getResources(),R.drawable.item10);
        mBlackPiece = BitmapFactory.decodeResource(getResources(),R.drawable.item11);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {// 点击处理落子
        if(mIsGameOver) return false;

        int action = event.getAction();
        if(action == MotionEvent.ACTION_UP){

            int x= (int) event.getX();//获取当前点击处的坐标
            int y= (int) event.getY();

            Point p =getValidPoint(x,y);//打包这个坐标,这里重写方法是为了让在一定范围内的
            //坐标内点击都可以落子在一个点
            if(mWhiteArray.contains(p)||mBlackArray.contains(p)){
                return false;
            }
            if(mIsWhite){
                mWhiteArray.add(p); //坐标存入白棋数组（标记作用，防止同一点重复落子）
            }else{
                mBlackArray.add(p);
            }
            invalidate(); //重绘
            mIsWhite = !mIsWhite;
        }
       return true;
    }

    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int)(y / mLineHeight));
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  //指定我们的View在屏幕上的大小
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);//getSize取低16位的值
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//getMode取高32位的值

        int heighSize = MeasureSpec.getSize(heightMeasureSpec);
        int heighMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = Math.min(widthSize,heighSize);
        //MeasureSpec.EXACTLY  是精确尺寸
        //MeasureSpec.AT_MOST 是最大尺寸
        if(widthMode == MeasureSpec.UNSPECIFIED){ //未指定尺寸
            width = heighSize;
        }else if(heighMode == MeasureSpec.UNSPECIFIED){
            width = widthSize;
        }

        setMeasuredDimension(width,width); //传进去的值是View最终视图的大小

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) { //设置尺寸
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
        mLineHeight = mPanelWidth * 1.0f /MAX_LINE;

        int pieceWidth = (int) (mLineHeight*ratioPieceOfLineHeight);//设置棋子尺寸为一格的3/4
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece,pieceWidth,pieceWidth,false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece,pieceWidth,pieceWidth,false);

    }

    @Override
    protected void onDraw(Canvas canvas) { //绘制棋盘
        super.onDraw(canvas);

        drawBoard(canvas);  ////绘制棋盘横线和纵线
        drawPieces(canvas);//绘制棋子
        checkGameOver(); //判断游戏是否结束
    }

    private void checkGameOver() {//游戏结束判断
        boolean whiteWin = checkFiveInLine(mWhiteArray);
        boolean blackWin = checkFiveInLine(mBlackArray);

        if(whiteWin||blackWin){
            mIsGameOver = true;
            mIsWhiteWinner = whiteWin;
            String text = mIsWhiteWinner?"金刚狼胜利":"南瓜骑士胜利";
            Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            dialog1();
        }
    }

    private void dialog1() {
       DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener(){

           @Override
           public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case Dialog.BUTTON_POSITIVE:{
                        start();
                        dialog.dismiss();
                        break;
                    }
                    case Dialog.BUTTON_NEGATIVE:{
                        dialog.dismiss();
                        break;
                    }
                }
           }
       };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); //新建一个构造器
        builder.setTitle("提示");
        builder.setMessage("是否再来一局？");
        builder.setPositiveButton("确认",dialogOnclicListener);
        builder.setNegativeButton("取消", dialogOnclicListener);
        builder.create().show();
    }

    private boolean checkFiveInLine(List<Point> points) {
        for(Point p : points){ //循环语句的另一种写法
            int x=p.x;
            int y=p.y;
            boolean win =check(x,y,points);
            if(win) return true;
        }
          return false;
    }

    //判断棋子是否有横向的五个相邻一致
    private boolean check(int x, int y, List<Point> points) {
        int countHorizontal=1;
        int countVertical=1;
        int countLeftDig=1;
        int countRightDig=1;
        for(int i=1;i<MAX_COUNT_INLINE;i++){
            if(points.contains(new Point(x-i,y))){ //水平左
                countHorizontal++;
            }else countHorizontal=1;
            if (points.contains(new Point(x,y-i))){//竖直下
                countVertical++;
            }else countVertical=1;
            if (points.contains(new Point(x-i,y-i))){//左下
                countLeftDig++;
            }else countLeftDig=1;
            if (points.contains(new Point(x+i,y-i))){//右下
                countRightDig++;
            }else countLeftDig=1;
        }

        if(countHorizontal==MAX_COUNT_INLINE||countLeftDig==MAX_COUNT_INLINE
                ||countRightDig==MAX_COUNT_INLINE||countVertical==MAX_COUNT_INLINE) return true;

        for(int i=1;i<MAX_COUNT_INLINE;i++){
            if(points.contains(new Point(x+i,y))){//水平右
                countHorizontal++;
            }else countHorizontal=1;
            if (points.contains(new Point(x,y+i))){//竖直上
                countVertical++;
            } else  countVertical=1;
            if (points.contains(new Point(x-i,y+i))){//左上
                countRightDig++;
            }else  countRightDig=1;
            if (points.contains(new Point(x+i,y+i))){//右上
                countLeftDig++;
            }else  countLeftDig=1;
        }
        if(countHorizontal==MAX_COUNT_INLINE||countLeftDig==MAX_COUNT_INLINE
                ||countRightDig==MAX_COUNT_INLINE||countVertical==MAX_COUNT_INLINE) return true;

        return false;
    }

    private void drawPieces(Canvas canvas) { //绘制棋子
        for(int i=0,n=mWhiteArray.size();i<n;i++){
            Point whitePoint = mWhiteArray.get(i);
            //这只棋子的大小，并将子落下，绘制在屏幕上
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x+(1-ratioPieceOfLineHeight)/2)*mLineHeight,
                    (whitePoint.y+(1-ratioPieceOfLineHeight)/2)*mLineHeight,null);
        }

        for(int i=0,n=mBlackArray.size();i<n;i++){
            Point blackPoint = mBlackArray.get(i);
            //这只棋子的大小，并将子落下，绘制在屏幕上
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x+(1-ratioPieceOfLineHeight)/2)*mLineHeight,
                    (blackPoint.y+(1-ratioPieceOfLineHeight)/2)*mLineHeight,null);
        }
    }

    private void drawBoard(Canvas canvas) {  //绘制棋盘横线和纵线
        int w = mPanelWidth;  //棋盘总宽度
        float lineHeight = mLineHeight;

        for(int i=0;i<MAX_LINE;i++){  //一次画一行或一列，循环画
            int startX = (int) (lineHeight / 2); //起点横坐标
            int endX = (int) (w-lineHeight / 2);//终点横坐标
            int y = (int) ((0.5+i)*lineHeight);//纵坐标
            canvas.drawLine(startX,y,endX,y,mPaint);//画棋盘横线；

            canvas.drawLine(y,startX,y,endX,mPaint);//画棋盘纵线
        }

    }

    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAME_OVER = "instance_game_over";
    private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

    @Override
    protected Parcelable onSaveInstanceState() {  //保存当前棋盘上的棋局
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER,mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY,mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY,mBlackArray);
        return bundle;
    }

    public void start(){  //再来一局
        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver=false;
        mIsWhiteWinner=false;
        invalidate();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) { //重现保存的棋局
        if(state instanceof Bundle){
            Bundle bundle = (Bundle) state;
            mIsGameOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return ;
        }
        super.onRestoreInstanceState(state);
    }
}
