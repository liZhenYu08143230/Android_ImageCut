package com.example.lzy01.AndroidImageCut;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static java.lang.StrictMath.abs;

public class GrabCut extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {

    private ImageView showImage;
    private boolean drawRect,drawSpecialPixel;
    private TextView information;
    private Button btn_next,btn_pre;

    private Bitmap srcBitmap,cropBitmap,drawSpecialPixelBitmap;
    private Canvas canvas;

    private boolean isFristDraw;
    private boolean isDown;
    private boolean isMove;
    private  int startX=0, startY=0, endX=0, endY=0,tempX=0,tempY=0,mode;
    private int moveX=0,moveY=0;
    private static final String TAG = "GrabCut";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grab_cut);
        Init();

        Intent intent=getIntent();
        byte[] srcByte=intent.getByteArrayExtra("srcImage");
        srcBitmap=BitmapFactory.decodeByteArray(srcByte, 0, srcByte.length).copy(Bitmap.Config.ARGB_8888, true);
        showImage.setImageBitmap(srcBitmap);
    }
    private void Init(){
        showImage=findViewById(R.id.showImage);
        information =findViewById(R.id.Information);
        btn_next=findViewById(R.id.btn_next);
        btn_pre=findViewById(R.id.btn_pre);

        showImage.setOnTouchListener(this);
        btn_next.setOnClickListener(this);
        btn_pre.setOnClickListener(this);
        information.setText(this.getString(R.string.draw_rect));

        isFristDraw =true;
        isDown =false;
        isMove =false;

        drawRect=true;
        drawSpecialPixel=false;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_next:
                if(drawRect){
                    drawRect=false;
                    drawSpecialPixel=true;
                    btn_next.setText(R.string.Do_it);
                    information.setText(R.string.do_grabCut);
                    drawRect(startX,startY,endX,endY) ;
                }else if(drawSpecialPixel){
                    drawRect=false;
                    drawSpecialPixel=false;
                    btn_pre.setEnabled(false);
                    DoGrabCut();
                }
                break;
            case R.id.btn_pre:
                if(drawRect){
                    drawSpecialPixel=false;
                    drawRect=false;
                    showImage.setImageBitmap(srcBitmap);
                    information.setText(this.getString(R.string.draw_rect));
                }else if(drawSpecialPixel){
                    drawRect=true;
                    drawSpecialPixel=false;
                    information.setText(this.getString(R.string.do_grabCut));
                    showImage.setImageBitmap(cropBitmap);
                }else{
                    Intent intent=new Intent(GrabCut.this,MainActivity.class);
                    startActivity(intent);
                }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try{
            if(drawRect) {
                int tsubSX=0,tsubSY=0,tsubEX=0,tsubEY=0;
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "onTouch: clickDown");
                        isDown = !isDown;
                        if(isFristDraw){
                            startX = (int) event.getX();
                            startY = (int) event.getY();
                        }else {
                            tempX = (int) event.getX();
                            tempY = (int) event.getY();
                            tsubSX=abs(startX-tempX);
                            tsubSY=abs(startY-tempY);
                            tsubEX=abs(endX-tempX);
                            tsubEY=abs(endY-tempY);
                            if(tsubSX<=5||tsubSY<=5||tsubEX<=5||tsubEY<=5){
                                isMove=true;
                                Log.d(TAG, "onTouch: isMove == true");
                            }
                            mode=RectChange(tsubSX,tsubSY,tsubEX,tsubEY);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: clickUp");
                        isDown = !isDown;
                        if(isFristDraw){
                            endX = (int) event.getX();
                            endY = (int) event.getY();
                            drawRect(startX, startY, endX, endY);
                            System.out.println("(startX,startY)=(" + startX + "," + startY + ")");
                            System.out.println("(endX,endY)=(" + endX + "," + endY + ")");
                            isFristDraw=isFristDraw?!isFristDraw:isFristDraw;
                        }else{
                            if(isMove) {
                                moveX = (int) event.getX() - tempX;
                                moveY = (int) event.getY() - tempY;
                                int[] xy1=getXY1(moveX,moveY);
                                drawRect(xy1[0], xy1[1], xy1[2], xy1[3]);
                                startX=xy1[0];startY=xy1[1];endX=xy1[2];endY=xy1[3];
                            }
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch: clickMove");
                        if(isFristDraw){
                            endX = (int) event.getX();
                            endY = (int) event.getY();
                            drawRect(startX, startY, endX, endY);
                        } else{
                            if(isMove){
                                moveX=(int)event.getX()-tempX;
                                moveY=(int)event.getY()-tempY;
                                int[] xy1=getXY1(moveX,moveY);
                                drawRect(xy1[0], xy1[1], xy1[2], xy1[3]);
                                startX=xy1[0];startY=xy1[1];endX=xy1[2];endY=xy1[3];
                                tempX = (int) event.getX();
                                tempY = (int) event.getY();
                            }
                        }
                        break;
                }
            }else if(drawSpecialPixel){
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "onTouch: clickDown");
                        startX= (int) event.getX();
                        startY= (int) event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: clickUp");
                        endX= (int) event.getX();
                        endY= (int) event.getY();
                        drawSpecialPixel(startX,startY,endX,endY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "onTouch: clickMove");
                        endX= (int) event.getX();
                        endY= (int) event.getY();
                        drawSpecialPixel(startX,startY,endX,endY);
                        startX= (int) event.getX();
                        startY= (int) event.getY();
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    private void drawSpecialPixel(int x1, int y1, int x2, int y2) {
        if(drawSpecialPixelBitmap==null){
            drawSpecialPixelBitmap=Bitmap.createBitmap(cropBitmap);
        }
        canvas=new Canvas(drawSpecialPixelBitmap);
        Paint paint=new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5.0f);
        canvas.drawLine(x1,y1,x2,y2,paint);
        showImage.setImageBitmap(drawSpecialPixelBitmap);
    }

    private void DoGrabCut() {

    }

    private int[] getXY1(int moveX, int moveY) {
        int startX1=startX,startY1=startY,endX1=endX,endY1=endY;
        switch (mode){
            case 0:
                break;
            case 1:
                startX1=startX+moveX;
                startY1=startY+moveY;
                break;
            case 2:
                startY1=startY+moveY;
                break;
            case 3:
                startY1=startY+moveY;
                endX1=endX+moveX;
                break;
            case 4:
                startX1=startX+moveX;
                break;
            case 5:
                startX1=startX+moveX;
                startY1=startY+moveY;
                endX1=endX+moveX;
                endY1=endY+moveY;
                break;
            case 6:
                endX1=endX+moveX;
                break;
            case 7:
                startX1=startX+moveX;
                endY1=endY+moveY;
                break;
            case 8:
                endY1=endY+moveY;
                break;
            case 9:
                endX1=endX+moveX;
                endY1=endY+moveY;
                break;
        }
        int []xy1={startX1,startY1,endX1,endY1};
        return xy1;
    }

    private int RectChange(int tsubSX, int tsubSY, int tsubEX, int tsubEY) {
        int mode;
        if (tsubSX <= 5) {
            if (tsubSY <= 5) {
                mode = 1;
            } else if (tsubEY <= 5) {
                mode = 7;
            } else if(tsubSY+tsubEY== abs(startY-endY)){
                mode = 4;
            }else {
                mode=0;
            }
        } else if (tsubEX <= 5) {
            if (tsubSY <= 5) {
                mode = 3;
            } else if (tsubEY <= 5) {
                mode = 9;
            } else if(tsubSY+tsubEY==  abs(startY-endY)) {
                mode = 6;
            } else{
                mode =0;
            }
        } else if(tsubEX+tsubSX==abs(startX-endX)) {
            if (tsubSY <= 5) {
                mode = 2;
            } else if (tsubEY <= 5) {
                mode = 8;
            } else if(tsubSY+tsubEY==  abs(startY-endY)) {
                mode = 5;
            } else{
                mode=0;
            }
        }else {
            mode =0;
    }
        return mode;
    }

    private void drawRect(int x1,int y1,int x2,int y2){
        Bitmap drawRectBitmap=Bitmap.createBitmap(srcBitmap);
        showImage.setImageBitmap(drawRectBitmap);
        int startX,startY,endX,endY;
        startX=x1>x2?x2:x1;
        startY=y1>y2?y2:y1;
        endX=x1>x2?x1:x2;
        endY=y1>y2?y1:y2;
        int lengthRectX=endX-startX;
        int lengthRectY=endY-startY;
        Rect desRect=new Rect(0,0,lengthRectX,lengthRectY);
        Rect srcRect=new Rect(startX,startY,endX,endY);


        canvas=new Canvas(drawRectBitmap);
        Paint paint=new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawLine(x1,y1,x2,y1,paint);
        canvas.drawLine(x1,y1,x1,y2,paint);
        canvas.drawLine(x2,y1,x2,y2,paint);
        canvas.drawLine(x1,y2,x2,y2,paint);
        showImage.setImageBitmap(drawRectBitmap);
        if(!drawRect){
            canvas.drawBitmap(drawRectBitmap,srcRect,desRect,null);
            System.out.println("(lengthX,lengthY)=("+lengthRectX+","+lengthRectY+")");
            cropBitmap=Bitmap.createBitmap(drawRectBitmap,0,0,lengthRectX,lengthRectY);
            showImage.setImageBitmap(cropBitmap);
        }
    }
}
