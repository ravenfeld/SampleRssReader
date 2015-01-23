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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class Database {

    public static final String KEY_ROW_ID = BaseColumns._ID;
    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_IMAGE_URL = "imageUrl";
    public static final String KEY_PUB_DATE = "pubDate";

    private static final String DATABASE_NAME = "rss";
    private static final String DATABASE_TABLE = "article";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_LIST_TABLE = "create table " + DATABASE_TABLE + " (" +
            KEY_ROW_ID + " integer primary key autoincrement, " +
            KEY_TITLE + " , " +
            KEY_DESCRIPTION + " , " +
            KEY_CONTENT + " , " +
            KEY_IMAGE_URL + " , " +
            KEY_PUB_DATE + ");";


    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;
    private final Context context;

    public Database(Context c) {
        context = c;
    }

    public Database openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public Database openToWrite() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public boolean deleted() {
        return context.deleteDatabase(DATABASE_NAME);
    }

    public void close() {
        sqLiteHelper.close();
    }

    public class SQLiteHelper extends SQLiteOpenHelper {
        public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_LIST_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    public long insertArticle(Article article) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, article.getTitle());
        initialValues.put(KEY_DESCRIPTION, article.getDescription());
        initialValues.put(KEY_PUB_DATE, Utils.dateToString(article.getDate()));
        initialValues.put(KEY_IMAGE_URL, article.getImageUrl());
        initialValues.put(KEY_CONTENT, article.getEncodedContent());
        return sqLiteDatabase.insert(DATABASE_TABLE, null, initialValues);
    }

    public Cursor getArticles() {
        return
                sqLiteDatabase.query(true, DATABASE_TABLE, new String[]{
                                KEY_ROW_ID,
                                KEY_TITLE,
                                KEY_DESCRIPTION,
                                KEY_IMAGE_URL,
                        }, null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    public Cursor getArticle(int id) {
        return
                sqLiteDatabase.query(true, DATABASE_TABLE, new String[]{
                                KEY_ROW_ID,
                                KEY_TITLE,
                                KEY_PUB_DATE,
                                KEY_IMAGE_URL,
                                KEY_CONTENT
                        }, KEY_ROW_ID + "= '" + id + "'",
                        null,
                        null,
                        null,
                        null,
                        null);
    }
}
