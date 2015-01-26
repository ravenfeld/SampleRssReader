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


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class ArticleProvider extends ContentProvider {
    static final String PROVIDER_NAME = "fr.alecanu.samplerssreader.provider.article";
    static final String URL = "content://" + PROVIDER_NAME + "/articles";
    static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String KEY_ROW_ID = BaseColumns._ID;
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_IMAGE_URL = "imageUrl";
    public static final String KEY_PUB_DATE = "pubDate";

    static final int ARTICLES = 1;
    static final int ARTICLE_ID = 2;
    static final UriMatcher URI_MATCHER;
    SQLiteHelper mSQLiteHelper;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(PROVIDER_NAME, "articles", ARTICLES);
        URI_MATCHER.addURI(PROVIDER_NAME, "article/#", ARTICLE_ID);
    }

    private SQLiteDatabase mSQLiteDatabase;
    static final String DATABASE_NAME = "rss";
    static final String TABLE_NAME = "articleTable";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_TABLE =
            " CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ROW_ID + " integer primary key autoincrement, " +
                    KEY_TITLE + " , " +
                    KEY_DESCRIPTION + " , " +
                    KEY_CONTENT + " , " +
                    KEY_IMAGE_URL + " , " +
                    KEY_PUB_DATE + ");";


    public class SQLiteHelper extends SQLiteOpenHelper {
        public SQLiteHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mSQLiteHelper = new SQLiteHelper(context);
        mSQLiteDatabase = mSQLiteHelper.getWritableDatabase();

        if (mSQLiteDatabase == null)
            return false;
        else
            return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_NAME);

        switch (URI_MATCHER.match(uri)) {
            case ARTICLES:
                break;
            case ARTICLE_ID:
                queryBuilder.appendWhere(KEY_ROW_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder == "") {
            sortOrder = KEY_PUB_DATE;
        }
        Cursor cursor = queryBuilder.query(mSQLiteDatabase, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ARTICLES:
                return "vnd.android.cursor.dir/fr.alecanu.samplerssreader.articleTable";
            case ARTICLE_ID:
                return "vnd.android.cursor.item/fr.alecanu.samplerssreader.articleTable";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = mSQLiteDatabase.insert(TABLE_NAME, "", values);
        if (row > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, row);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Fail to add a new record into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (URI_MATCHER.match(uri)) {
            case ARTICLES:
                count = mSQLiteDatabase.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case ARTICLE_ID:
                String id = uri.getLastPathSegment();
                count = mSQLiteDatabase.delete(TABLE_NAME, KEY_ROW_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (URI_MATCHER.match(uri)) {
            case ARTICLES:
                count = mSQLiteDatabase.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case ARTICLE_ID:
                count = mSQLiteDatabase.update(TABLE_NAME, values, KEY_ROW_ID +
                        " = " + uri.getLastPathSegment() +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
