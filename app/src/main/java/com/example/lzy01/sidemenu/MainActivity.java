package com.example.lzy01.sidemenu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnTouchListener {


    private static final int PHOTO_REQUEST=1;
    private static final int FILE_REQUEST=2;

    private  File  PICTURE_FILE;
    private  File SAVE_FILE;

    private ImageView showImg;
    private Bitmap srcBitmap;
    private Bitmap cropBitmap;
    private Canvas canvas;
    private boolean isFristDraw =true;
    private boolean isDown =false;
    private  int startX=0, startY=0, endX=0, endY=0;
    private int moveX=0,moveY=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------Mine Code-----------
        showImg=findViewById(R.id.showImg);
        showImg.setOnTouchListener(this);

        //---------System Code----------DrawerLayout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try{
            switch (event.getAction()&MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN:
                        startX= (int) event.getX();
                        startY=(int) event.getY();
                        isDown=!isDown;
                    break;

                case MotionEvent.ACTION_UP:
                    isDown=!isDown;
                    endX= (int) event.getX();
                    endY=(int) event.getY();
                    drawRect(startX,startY,endX,endY);
                    System.out.println("(startX,startY)=("+startX+","+startY+")");
                    System.out.println("(endX,endY)=("+endX+","+endY+")");
                    if(isFristDraw){
                        isFristDraw =false;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    endX= (int) event.getX();
                    endY=(int) event.getY();
                    drawRect(startX,startY,endX,endY);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * *SideMenu Item点击逻辑实现
    * */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_camera:
                toCamera();
                break;
            case R.id.nav_gallery:
                toGallery();
                break;
            case R.id.nav_canny:
                
                break;
            case R.id.nav_grabCut:

                break;
            case R.id.nav_save:
                savePhoto();
                break;
            case R.id.nav_share:

                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST:
                    srcBitmap=loadBitmap(PICTURE_FILE.getPath());
                    showImg.setImageBitmap(srcBitmap);
                    try{
                        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
                    }catch (IOError error){
                        error.printStackTrace();
                    }

                    break;
                case FILE_REQUEST:
                    Uri uri = data.getData();
                    //通过uri的方式返回，部分手机uri可能为空
                    if (uri != null) {
                        try {
                            //通过uri获取到bitmap对象
                            srcBitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            showImg.setImageBitmap(srcBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        //部分手机可能直接存放在bundle中
                        Bundle bundleExtras = data.getExtras();
                        if (bundleExtras != null) {
                            srcBitmap=bundleExtras.getParcelable("data");
                            showImg.setImageBitmap(srcBitmap);
                        }
                    }
                    break;
            }
            showImg.setVisibility(View.VISIBLE);
        }else{
            showImg.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "图片获取失败", Toast.LENGTH_LONG).show();
        }
    }

    /*
    * 从文件中载入图像
    * */
    private Bitmap loadBitmap(String imgpath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bm = null;

        // 减少内存使用量，有效防止OOM
        {
            options.inJustDecodeBounds = true;
            bm = BitmapFactory.decodeFile(imgpath, options);

            // 屏幕宽
            int Wight = getWindowManager().getDefaultDisplay().getWidth();

            // 缩放比
            int ratio = options.outWidth / Wight;
            Log.e("xiangji", "options.outWidth="+options.outWidth);
            Log.e("xiangji", "rWight="+Wight);
            if (ratio <= 0){
                ratio = 1;
            }

            //InSampleSize这个参数可以调节你在decode原图时所需要的内存，有点像采样率，会丢掉一些像素，值是大于1的数，为2的幂时更利于运算。
            //举个例子：当 inSampleSize == 4 时会返回一个尺寸(长和宽)是原始尺寸1/4，像素是原来1/16的图片，由此来减少内存使用

            //options.inSampleSize = ratio;由动态的生成此数值变为手动控制
            options.inSampleSize = 1;//此数值决定显示时照片的大小
            options.inJustDecodeBounds = false;
        }

        // 加载图片,并返回
        return BitmapFactory.decodeFile(imgpath, options);
    }
    /*
    * 调用系统相机
    * */
    private void toCamera(){
        String path = Environment.getExternalStorageDirectory() + File.separator +"images"; //获取路径
        String fileName = new Date().getTime()+".jpg";//定义文件名
        PICTURE_FILE = new File(path,fileName);
        if(!PICTURE_FILE.getParentFile().exists()){//文件夹不存在
            PICTURE_FILE.getParentFile().mkdirs();
        }
        Uri imageUri = Uri.fromFile(PICTURE_FILE);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PHOTO_REQUEST);//takePhotoRequestCode是自己定义的一个请求码
        System.out.println("PHOTO --------"+PICTURE_FILE.toString());
    }
    /*
    * 调用图库
    * */
    private void toGallery(){
        Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
        //选择图片格式
        intent2.setType("image/*");
        intent2.putExtra("return-data",true);
        startActivityForResult(intent2,FILE_REQUEST);
    }
    /*
    * 保存图片
    * */
    private void savePhoto(){
        String state=Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            String savePath=Environment.getExternalStorageDirectory()+File.separator +"CUTimages";
            String saveFileName=new Date().getTime()+"lzy.jpg";
            SAVE_FILE=new File(savePath,saveFileName);
            if(!SAVE_FILE.getParentFile().exists()){//文件夹不存在
                SAVE_FILE.getParentFile().mkdirs();
            }
            Bitmap obmp = getBitmapFromImageView(showImg);
            FileOutputStream fos;
            try{
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(SAVE_FILE));
                obmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
    }
    /*
    * 获取当前显示的图片
    * */
    private Bitmap getBitmapFromImageView(ImageView imageView){
        imageView.setDrawingCacheEnabled(true);
        Bitmap obmp = Bitmap.createBitmap(imageView.getDrawingCache());
        imageView.setDrawingCacheEnabled(false);
        return obmp;
    }
    private void drawRect(int x1,int y1,int x2,int y2){
      showImg.setImageBitmap(srcBitmap);
        Bitmap bitmap=getBitmapFromImageView(showImg);


        int startX,startY,endX,endY;
        startX=x1>x2?x2:x1;
        startY=y1>y2?y2:y1;
        endX=x1>x2?x1:x2;
        endY=y1>y2?y1:y2;
        int lengthRectX=endX-startX;
        int lengthRectY=endY-startY;
        Rect desRect=new Rect(0,0,lengthRectX,lengthRectY);
        Rect srcRect=new Rect(startX,startY,endX,endY);


        canvas=new Canvas(bitmap);
        Paint paint=new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawLine(x1,y1,x2,y1,paint);
        canvas.drawLine(x1,y1,x1,y2,paint);
        canvas.drawLine(x2,y1,x2,y2,paint);
        canvas.drawLine(x1,y2,x2,y2,paint);
        showImg.setImageBitmap(bitmap);
        if(!isDown){
            canvas.drawBitmap(bitmap,srcRect,desRect,null);
            System.out.println("(lengthX,lengthY)=("+lengthRectX+","+lengthRectY+")");
            cropBitmap=Bitmap.createBitmap(bitmap,0,0,lengthRectX,lengthRectY);
            showImg.setImageBitmap(cropBitmap);
        }
    }
}
