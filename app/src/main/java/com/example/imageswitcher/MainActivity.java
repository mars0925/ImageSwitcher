package com.example.imageswitcher;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements ViewSwitcher.ViewFactory, View.OnTouchListener {

    /**
     * ImagaSwitcher 的引用
     */
    private ImageSwitcher mImageSwitcher;
    /**
     * 图片id数组
     */
    private String[] imgIds;
    /**
     * 当前选中的图片id序号
     */
    private int currentPosition;
    /**
     * 按下点的X坐标
     */
    private float downX;
    /**
     * 装载点点的容器
     */
    private LinearLayout linearLayout;
    /**
     * 点点数组
     */
    private ImageView[] tips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*網路圖片資料*/
        String testurl = "https://pic4.zhimg.com/80/v2-31954457a93bb3918f874748c3ba094e_hd.jpg";
        String testurl2 = "http://pic2.zhimg.com/80/v2-9670af7e55ee97e05c5d8efd72331da4_hd.jpg";
        String testurl3 = "https://pic1.zhimg.com/80/v2-c569b9fac24eaaf81bd7b5335d955ae6_hd.jpg";
        String testurl4 = "https://pic3.zhimg.com/80/v2-0d935c5f96e1a31b96e244cd0487f143_hd.jpg";
        String testurl5 = "https://img.shouji.com.cn/simg/20170821/7642564207.jpg";
        String testurl6 = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRMzES-iC2EXUDoWL68SewFNOQf5F9V8UORV1EV5tfRZcHLmqrn";
        imgIds = new String[]{testurl, testurl2, testurl3, testurl4, testurl5, testurl6};

        new GetImageTask().execute(testurl);

        //实例化ImageSwitcher
        mImageSwitcher  = findViewById(R.id.imageSwitcher1);
        //设置Factory
        mImageSwitcher.setFactory(this);
        //设置OnTouchListener，我们通过Touch事件来切换图片
        mImageSwitcher.setOnTouchListener(this);

        linearLayout = findViewById(R.id.viewGroup);

        tips = new ImageView[imgIds.length];

        for(int i=0; i<imgIds.length; i++){
            ImageView mImageView = new ImageView(this);
            tips[i] = mImageView;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            layoutParams.rightMargin = 3;
            layoutParams.leftMargin = 3;

            mImageView.setBackgroundResource(R.drawable.ic_launcher_background);
            linearLayout.addView(mImageView, layoutParams);
        }

        //这个我是从上一个界面传过来的，上一个界面是一个GridView
        currentPosition = getIntent().getIntExtra("position", 0);
        mImageSwitcher.setImageResource(R.drawable.item01);

        setImageBackground(currentPosition);

    }

    /**
     * 设置选中的tip的背景
     */
    private void setImageBackground(int selectItems){
        for(int i=0; i<tips.length; i++){
            if(i == selectItems){
                tips[i].setBackgroundResource(R.drawable.page_indicator_focused);
            }else{
                tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public View makeView() {
        final ImageView i = new ImageView(this);
        i.setBackgroundColor(0xff000000);
        i.setScaleType(ImageView.ScaleType.CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return i ;
    }

    /**滑動換圖片*/
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:{
                //手指按下的X座標
                downX = event.getX();
                break;
            }
            case MotionEvent.ACTION_UP:{
                float lastX = event.getX();
                //抬起的时候的X坐标大于按下的时候就显示上一张图片
                if(lastX > downX){
                    if(currentPosition > 0){

                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.left_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.right_out));
                        currentPosition --;

                        new GetImageTask().execute(imgIds[currentPosition % imgIds.length]);
                        setImageBackground(currentPosition);
                    }else{
                        Toast.makeText(getApplication(), "已經是第一張", Toast.LENGTH_SHORT).show();
                    }
                }

                if(lastX < downX){
                    if(currentPosition < imgIds.length - 1){
                        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.right_in));
                        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.left_out));
                        currentPosition ++ ;

                        new GetImageTask().execute(imgIds[currentPosition % imgIds.length]);
                        setImageBackground(currentPosition);
                    }else{
                        Toast.makeText(getApplication(), "到最後一張了", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            break;
        }

        return true;
    }

    class GetImageTask extends AsyncTask<String, Void, Uri>{
        private final String tag = GetImageTask.class.getSimpleName();

        @Override
        protected Uri doInBackground(String... strings) {
            Bitmap bitmap = null;
            HttpURLConnection urlConnection = null;
            BufferedInputStream in = null;
            try {
                URL url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
                bitmap = BitmapFactory.decodeStream(in);

                Log.e(tag, "doInBackground: "+ "download ok");
            } catch (Exception e) {
                Log.e(tag, "doInBackground: " + e );
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                try {

                    if (in != null) {
                        in.close();
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            storeImage(bitmap);


            return storeImage(bitmap);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            mImageSwitcher.setImageURI(uri);
        }
    }

    private Uri storeImage(Bitmap image) {
        Uri uri;
        try {
            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            String mImageName = "MI_" + timeStamp + ".jpg";

            FileOutputStream fos = openFileOutput(mImageName, MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Log.e("storeImage", "doInBackground: " + "save ok");

            System.out.println(getFilesDir().getPath());

            String dataPath = getFilesDir().getPath() + "//" + mImageName;
            uri = Uri.fromFile(new File(dataPath));

            return uri;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
