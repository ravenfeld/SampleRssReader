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

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ArticleAdapter extends CursorAdapter {

    private int mIconIndex;
    private int mTitleIndex;
    private int mDescriptionIndex;

    static class ViewHolder {
        private TextView mTitleTextView;
        private TextView mDescriptionTextView;
        private ImageView mIconImageView;
    }

    public ArticleAdapter(Activity activity) {
        super(activity, null, FLAG_REGISTER_CONTENT_OBSERVER);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_article, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.mIconImageView = (ImageView) view.findViewById(android.R.id.icon);

        holder.mTitleTextView = (TextView) view.findViewById(android.R.id.text1);
        holder.mDescriptionTextView = (TextView) view.findViewById(android.R.id.text2);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        final String image = cursor.getString(mIconIndex);
        final String title = cursor.getString(mTitleIndex);
        final String description = cursor.getString(mDescriptionIndex);
        holder.mTitleTextView.setText(title);
        holder.mDescriptionTextView.setText(Html.fromHtml(description));
        if (image == null) {
            holder.mIconImageView.setVisibility(View.GONE);

        }
        Picasso.with(context)
                .load(image)
                .into(holder.mIconImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.mIconImageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        holder.mIconImageView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        if (cursor != null) {
            mIconIndex = cursor.getColumnIndex(Database.KEY_IMAGE_URL);
            mTitleIndex = cursor.getColumnIndex(Database.KEY_TITLE);
            mDescriptionIndex = cursor.getColumnIndex(Database.KEY_DESCRIPTION);
        }
        return super.swapCursor(cursor);
    }
}
