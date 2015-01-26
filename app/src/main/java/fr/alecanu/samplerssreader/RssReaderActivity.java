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

import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class RssReaderActivity extends ActionBarActivity implements RssService.RssDataRetriever,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {
    private static final String BLOG_URL = "http://www.trails-endurance.com/feed/";
    private static final int LOADER_RSS = 1;
    private RssService mRssService;
    private MenuItem mRefreshItem;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mEmptyTextView;
    private boolean mListViewEnabled;
    private ArticleAdapter mArticleAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss_reader);

        mListViewEnabled = true;

        mArticleAdapter = new ArticleAdapter(this);
        initUI();
        Utils.showProgressDialog(this, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mRssService != null)
                    mRssService.cancel(true);
            }
        });
        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isOnline(this)) {
            mSwipeLayout.setEnabled(true);
        } else {
            mSwipeLayout.setEnabled(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    @Override
    protected void onDestroy() {
        Utils.dismissProgressDialog();
        if (mRssService != null)

            mRssService.cancel(true);
        getLoaderManager().destroyLoader(LOADER_RSS);
        super.onDestroy();
    }

    @Override
    public void onPreparedRss() {
        mListViewEnabled = false;
    }

    @Override
    public void onDataReceivedRss() {
        getLoaderManager().restartLoader(LOADER_RSS, null, this);
        mListViewEnabled = true;
    }

    @Override
    public void onDataReceivedRssError() {
        refreshItemAnimationFinished();
        mSwipeLayout.setRefreshing(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rss_reader, menu);
        mRefreshItem = menu.findItem(R.id.menu_refresh);
        if (!mListViewEnabled) {
            refreshItemAnimationStarted();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (mListViewEnabled) {
                    refreshItemAnimationStarted();
                    refreshList();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        setContentView(R.layout.activity_rss_reader);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mEmptyTextView = (TextView) findViewById(android.R.id.empty);
        ListView listView = (ListView) findViewById(android.R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListViewEnabled) {
                    mArticleAdapter.getCursor().moveToPosition(position);
                    int index = mArticleAdapter.getCursor().getInt(mArticleAdapter.getCursor().getColumnIndex(Database.KEY_ROW_ID));
                    startActivity(ArticleDetailActivity.newInstance(getApplicationContext(), index));
                }
            }
        });
        listView.setAdapter(mArticleAdapter);
        if (Utils.isOnline(this)) {
            mSwipeLayout.setEnabled(true);
        } else {
            mSwipeLayout.setEnabled(false);
        }

    }

    private void refreshList() {
        if (Utils.isOnline(this)) {
            mRssService = new RssService(getApplicationContext(), this);
            mRssService.execute(BLOG_URL);

        } else {
            Utils.showSimpleInformationDialogBox(this, R.string.no_internet, R.string.ok);
            getLoaderManager().restartLoader(LOADER_RSS, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new ArticleLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mArticleAdapter.swapCursor(data);
        mSwipeLayout.setRefreshing(false);
        refreshItemAnimationFinished();
        Utils.dismissProgressDialog();
        mEmptyTextView.setVisibility(mArticleAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mArticleAdapter.swapCursor(null);
    }

    private void refreshItemAnimationStarted() {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout iv = (LinearLayout) inflater.inflate(R.layout.action_view_refresh, null);
        final ImageView iconRefresh = (ImageView) iv.findViewById(R.id.action_view_refresh);
        final Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh);

        if (rotation != null) {
            iconRefresh.startAnimation(rotation);
            mRefreshItem.setActionView(iv);
        }
    }

    private void refreshItemAnimationFinished() {
        if (mRefreshItem != null && mRefreshItem.getActionView() != null) {
            mRefreshItem.getActionView().clearAnimation();
            mRefreshItem.setActionView(null);
        }
    }

    @Override
    public void onRefresh() {
        if (mListViewEnabled) {
            refreshItemAnimationStarted();
            refreshList();
        }
    }
}
