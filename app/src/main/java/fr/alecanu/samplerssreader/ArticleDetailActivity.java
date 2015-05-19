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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ArticleDetailActivity extends ActionBarActivity {

    private static final String EXTRA_ID = "index";

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

                WebView description = (WebView) findViewById(R.id.webview);
                description.getSettings().setJavaScriptEnabled(true);

                String content = cursor.getString(cursor.getColumnIndex(ArticleProvider.KEY_CONTENT)).replaceAll("width=\"[0-9]+", "width=\"100%").replaceAll("height=\"[0-9]+\"", "");
                description.loadData(
                        content, "text/html; charset=utf-8", "UTF-8");

                if (Build.VERSION.SDK_INT >= 11) {
                    description.setBackgroundColor(0x01000000);
                } else {
                    description.setBackgroundColor(0x00000000);
                }
                final ImageView icon = (ImageView) findViewById(android.R.id.icon);
                String imageUrl = cursor.getString(cursor.getColumnIndex(ArticleProvider.KEY_IMAGE_URL));
                if (imageUrl == null) {
                    icon.setVisibility(View.GONE);

                }
                Picasso.with(this) //
                        .load(imageUrl) //
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
}
