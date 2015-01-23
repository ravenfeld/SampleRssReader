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

import java.util.Date;

public class Article {
    private static final String TAG_IMG = "img";
    private static final String TAG_SRC = "src";

    private String mTitle;
    private Date mDate;
    private String mDescription;
    private String mImageUrl;
    private String mContentEncoded;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = Utils.stringToDate(date);
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    public void setEncodedContent(String encodedContent) {
        this.mContentEncoded = encodedContent;
        searchImage(encodedContent);
    }

    public String getEncodedContent() {
        return mContentEncoded;
    }

    private void searchImage(String content) {
        int start = content.indexOf(TAG_IMG);
        if (start != -1) {
            String src = content.substring(content.indexOf(TAG_SRC + "=\"", start)).replace(TAG_SRC + "=\"", "");
            int end = src.indexOf("\"");
            setImageUrl(src.substring(0, end));
        }
    }
}
