/*
 * Copyright (c) 2015. - Alexis Lecanu (alexis.lecanu@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package fr.alecanu.samplerssreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

public class ArticleDetailActivity extends ActionBarActivity {

    private static final String EXTRA_ID = "index";

    private SpannableStringBuilder mHtmlSpannable;
    private TextView mDescription;
    private ArrayList<ImageUrlTarget> mImageUrlTargetList = new ArrayList<>();

    public static Intent newInstance(Context context, int index) {
        Intent intent = new Intent(context, ArticleDetailActivity.class);
        intent.putExtra(EXTRA_ID, index);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent != null) {

            int index = intent.getIntExtra(EXTRA_ID, -1);
            Cursor cursor = getContentResolver().query(Uri.parse("content://" + ArticleProvider.PROVIDER_NAME + "/article/" +
                            index),
                    null,
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                TextView title = (TextView) findViewById(android.R.id.text1);
                title.setText(cursor.getString(cursor.getColumnIndex(ArticleProvider.KEY_TITLE)));
                TextView date = (TextView) findViewById(R.id.date);
                date.setText(cursor.getString(cursor.getColumnIndex(ArticleProvider.KEY_PUB_DATE)));

                mDescription = (TextView) findViewById(android.R.id.text2);

                Spanned htmlSpan = Html.fromHtml(cursor.getString(cursor.getColumnIndex(ArticleProvider.KEY_CONTENT)));

                mHtmlSpannable = new SpannableStringBuilder(htmlSpan);

                for (ImageSpan img : mHtmlSpannable.getSpans(0,
                        mHtmlSpannable.length(), ImageSpan.class)) {

                    ImageUrlTarget imageUrlTarget = new ImageUrlTarget(img);

                    Picasso.with(getApplicationContext()).load(img.getSource()).into(imageUrlTarget);
                    mImageUrlTargetList.add(imageUrlTarget);

                }

                mDescription.setText(htmlSpan);

                final ImageView icon = (ImageView) findViewById(android.R.id.icon);
                String imageUrl = cursor.getString(cursor.getColumnIndex(ArticleProvider.KEY_IMAGE_URL));
                if (imageUrl == null) {
                    icon.setVisibility(View.GONE);

                }
                Picasso.with(this)
                        .load(imageUrl)
                        .into(icon, new Callback() {

                            @Override
                            public void onSuccess() {
                                icon.setVisibility(View.VISIBLE);

                            }

                            @Override
                            public void onError() {
                                icon.setVisibility(View.GONE);
                            }
                        });
            }
            cursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ImageUrlTarget implements Target {
        private DisplayMetrics mMetrics = new DisplayMetrics();
        private ImageSpan mImageSpan;

        public ImageUrlTarget(ImageSpan i) {
            mImageSpan = i;
            getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            Drawable d = new BitmapDrawable(ArticleDetailActivity.this.getResources(), bitmap);
            int width, height;
            int originalWidthScaled = (int) (d.getIntrinsicWidth() * mMetrics.density);
            int originalHeightScaled = (int) (d.getIntrinsicHeight() * mMetrics.density);
            if (originalWidthScaled > mMetrics.widthPixels) {
                height = d.getIntrinsicHeight() * mMetrics.widthPixels
                        / d.getIntrinsicWidth();
                width = mMetrics.widthPixels;
            } else {
                height = originalHeightScaled;
                width = originalWidthScaled;
            }

            d.setBounds(0, 0, width, height);
            ImageSpan newImg = new ImageSpan(d, mImageSpan.getSource());

            int start = mHtmlSpannable.getSpanStart(mImageSpan);
            int end = mHtmlSpannable.getSpanEnd(mImageSpan);

            mHtmlSpannable.removeSpan(mImageSpan);

            mHtmlSpannable.setSpan(newImg, start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mDescription.setText(mHtmlSpannable);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

}
