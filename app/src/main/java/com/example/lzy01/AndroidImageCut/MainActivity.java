package com.example.lzy01.AndroidImageCut;

import android.app.Activity;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final int PHOTO_REQUEST=1;
    private static final int FILE_REQUEST=2;

    private  File  PICTURE_FILE;
    private  File SAVE_FILE;

    private ImageView showImg;
    private Bitmap srcBitmap;

    private boolean isGrabCut=false;



    private  Uri imageUri;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------Mine Code-----------
        showImg=findViewById(R.id.showImg);


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
                Log.d(TAG, "onNavigationItemSelected: openCamera");
                toCamera();
                break;
            case R.id.nav_gallery:
                Log.d(TAG, "onNavigationItemSelected:openGallery");
                toGallery();
                break;
            case R.id.nav_canny:
                Log.d(TAG, "onNavigationItemSelected:canny");
                isGrabCut=false;
                cutImage(isGrabCut);
                break;
            case R.id.nav_grabCut:
                Log.d(TAG, "onNavigationItemSelected:grabcut");
                isGrabCut=true;
                cutImage(isGrabCut);
                break;
            case R.id.nav_save:
                Log.d(TAG, "onNavigationItemSelected:save");
                savePhoto();
                break;
            case R.id.nav_share:
                Log.d(TAG, "onNavigationItemSelected:share");
                shareImg("Share","AppTheme","this is picture",imageUri);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void cutImage(boolean isGrabCut) {
        Bitmap bitmap=getBitmapFromImageView(showImg);
        if(isGrabCut){
            Intent grabCutIntent=new Intent(MainActivity.this,GrabCut.class);
            grabCutIntent.putExtra("srcImage",Bitmap2Bytes(srcBitmap));
            startActivity(grabCutIntent);
            Log.d(TAG, "cutImage: grabcut");
        }else{
            Intent cannyIntent=new Intent(MainActivity.this,CannyActivity.class);
            cannyIntent.putExtra("srcImage",Bitmap2Bytes(srcBitmap));
            startActivity(cannyIntent);
            Log.d(TAG, "cutImage: canny");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST:
                    srcBitmap=loadBitmap(PICTURE_FILE.getPath());
                    showImg.setImageBitmap(srcBitmap);
                    Log.d(TAG, "onActivityResult: getPictureFromcameraSuccessful");
                    break;
                case FILE_REQUEST:
                    Uri uri = data.getData();
                    imageUri=uri;
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
        imageUri = Uri.fromFile(PICTURE_FILE);
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
    private void shareImg(String dlgTitle, String subject, String content, Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }
    private byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
