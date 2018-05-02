package com.example.lzy01.sidemenu;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class SelectRectDraw {


    private Bitmap srcBitmap,bitmap,cropBitmap;
    private Rect srcRect,desRect;
    private Canvas canvas;
    private Paint paint;
    private int lengthRectX,lengthRectY;

    public SelectRectDraw(){
        paint=new Paint();
        paint.setColor(Color.WHITE);
    }

    public void setSrcBitmap(Bitmap srcBitmap) {
        this.srcBitmap=srcBitmap;
        canvas=new Canvas(bitmap=Bitmap .createBitmap(bitmap));
    }
    public Bitmap getBitmap(){
        return bitmap;
    }

    public void drawRect(int x1, int y1, int x2, int y2){

        int startX,startY,endX,endY;
        startX=x1>x2?x2:x1;
        startY=y1>y2?y2:y1;
        endX=x1>x2?x1:x2;
        endY=y1>y2?y1:y2;

        lengthRectX=endX-startX;
        lengthRectY=endY-startY;

        desRect=new Rect(0,0,lengthRectX,lengthRectY);
        srcRect=new Rect(startX,startY,endX,endY);

        canvas.drawLine(x1,y1,x2,y1,paint);
        canvas.drawLine(x1,y1,x1,y2,paint);
        canvas.drawLine(x2,y1,x2,y2,paint);
        canvas.drawLine(x1,y2,x2,y2,paint);
    }

    public void cropImageByRect(){
        canvas.drawBitmap(srcBitmap,srcRect,desRect,null);
        cropBitmap=Bitmap.createBitmap(srcBitmap,0,0,lengthRectX,lengthRectY);
    }
    public Bitmap getCropBitmap() {
        return cropBitmap;
    }
}
