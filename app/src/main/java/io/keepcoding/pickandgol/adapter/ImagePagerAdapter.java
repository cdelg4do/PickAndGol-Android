package io.keepcoding.pickandgol.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.manager.image.ImageManager;


/**
 * Created by Carlos on 31/03/2017.
 */
public class ImagePagerAdapter extends PagerAdapter {

    private Context context;
    private List<String> imageUrlList;
    private LayoutInflater layoutInflater;
    private ImageManager im;


    public ImagePagerAdapter(Context context, List<String> imageUrlList) {

        this.context = context;
        this.layoutInflater = (LayoutInflater) this.context.getSystemService(this.context.LAYOUT_INFLATER_SERVICE);
        this.imageUrlList = imageUrlList;

        im = ImageManager.getInstance(context);
    }

    @Override
    public int getCount() {
        return imageUrlList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View view = layoutInflater.inflate(R.layout.pager_image_element, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.pager_image_element_holder);
        im.loadImage(imageUrlList.get(position), imageView, R.drawable.error_placeholder);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
