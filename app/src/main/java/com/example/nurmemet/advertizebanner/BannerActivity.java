package com.example.nurmemet.advertizebanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by nurmemet on 2015/12/13.
 */
public class BannerActivity extends Activity {
    AdverTizeBanner banner;
    BannerAdapter mAdapter;
    ArrayList<String> imageUrlList = new ArrayList<String>();
    Button refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner_activity);
        banner = (AdverTizeBanner) findViewById(R.id.banner_view);
        refresh=(Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUrlList.remove(0);
                banner.requestLayout();
            }
        });
        initDatas();
        mAdapter = new BannerAdapter(this, imageUrlList);
        banner.setAdapter(mAdapter);
        banner.statrtAutoPlay();

    }

    private void initDatas() {
        imageUrlList
                .add("http://b.hiphotos.baidu.com/image/pic/item/d01373f082025aaf95bdf7e4f8edab64034f1a15.jpg");
        imageUrlList
                .add("http://g.hiphotos.baidu.com/image/pic/item/6159252dd42a2834da6660c459b5c9ea14cebf39.jpg");
        imageUrlList
                .add("http://d.hiphotos.baidu.com/image/pic/item/adaf2edda3cc7cd976427f6c3901213fb80e911c.jpg");
        imageUrlList
                .add("http://g.hiphotos.baidu.com/image/pic/item/b3119313b07eca80131de3e6932397dda1448393.jpg");
//        imageUrlList.add("http://www.oschina.net/uploads/img/201207/16151042_50i0.jpg");
//        imageUrlList.add("http://img2.3lian.com/2014/f5/158/d/86.jpg");
//        imageUrlList.add("http://images.99pet.com/InfoImages/wm600_450/1d770941f8d44c6e85ba4c0eb736ef69.jpg");
//        imageUrlList.add("http://img2.3lian.com/2014/f5/158/d/87.jpg");
//        imageUrlList.add("http://images.99pet.com/InfoImages/wm600_450/ef48d0d8e8f64172a28b9451fc5a941d.jpg");




    }







}
