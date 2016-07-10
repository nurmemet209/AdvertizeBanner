package com.example.nurmemet.advertizebanner;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;


import java.util.ArrayList;

/**
 * Created by nurmemet on 2015/12/13.
 */
public class BannerActivity extends Activity {
    AdverTizeBanner banner;
    BannerAdapter mAdapter;
    ArrayList<String> imageUrlList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner_activity);
        banner = (AdverTizeBanner) findViewById(R.id.banner_view);

        initDatas();
        init();
        mAdapter = new BannerAdapter(this, imageUrlList);
        banner.setAdapter(mAdapter);
        banner.start();
        banner.setOnPageChangedListener(new AdverTizeBanner.OnPageChangedListener() {
            @Override
            public void OnPageChange(int position) {
                System.out.println("banner" + position);
            }
        });

        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUrlList.remove(1);
                mAdapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.click_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUrlList
                        .add("http://g.hiphotos.baidu.com/image/pic/item/b3119313b07eca80131de3e6932397dda1448393.jpg");
                mAdapter.notifyDataSetChanged();
            }
        });
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


    }


    private void init() {
        int a = 0;
        System.out.println(a++);
        System.out.println(a);


    }


}
