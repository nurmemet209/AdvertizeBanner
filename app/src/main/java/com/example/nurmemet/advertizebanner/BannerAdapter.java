package com.example.nurmemet.advertizebanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

/**
 * Created by nurmemet on 2015/12/13.
 */
public class BannerAdapter extends AdverTizeBanner.BannerAdapter {
    private Context mCotext;
    private ArrayList<String> url;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public BannerAdapter(Context mCotext, ArrayList<String> url) {

        this.mCotext = mCotext;
        this.url = url;
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(mCotext));

        options = new DisplayImageOptions.Builder()
                .showStubImage(R.mipmap.ic_launcher) // 设置图片下载期间显示的图片
                //.showImageForEmptyUri(R.drawable.meinv) // 设置图片Uri为空或是错误的时候显示的图片
                //.showImageOnFail(R.drawable.meinv) // 设置图片加载或解码过程中发生错误显示的图片
                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
                .build();
    }

    @Override
    public int getCount() {
        if (url != null) {
            return url.size();
        }
        return 0;
    }

    @Override
    public View getView(int position, View mainView, int chilIndex) {

        if (mainView == null) {
            mainView = LayoutInflater.from(mCotext).inflate(R.layout.banner_item, null);
        }
        mainView.setTag(position);


        ImageView img = (ImageView) mainView.findViewById(R.id.test);
        TextView tv = (TextView) mainView.findViewById(R.id.index);
        tv.setText("" + chilIndex);
//        AQuery aq = new AQuery(mainView);
//
//        aq.id(R.id.test).image(url.get(position),true,true);

        imageLoader.displayImage(
                url.get(position),
                img, options);
        return mainView;
    }
}
