package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private String transitionName;
    private int currentPosition;
    public final static String EXTRA_SHARED_TRANSITION = "com.example.xyzreader.ui.EXTRA_SHARED_TRANSITION";
    public final static String EXTRA_CURRENT_POSITION = "com.example.xyzreader.ui.EXTRA_CURRENT_POSITION";
    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";

    private boolean mIsReturning = false;
    private ArticleDetailFragment mCurrentDetailsFragment;
    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mIsReturning) {
//                ImageView sharedElement = mCurrentDetailsFragment.getAlbumImage();
//                if (sharedElement == null) {
//                    // If shared element is null, then it has been scrolled off screen and
//                    // no longer visible. In this case we cancel the shared element transition by
//                    // removing the shared element from the shared elements map.
//                    names.clear();
//                    sharedElements.clear();
//                } else if (mStartingPosition != mCurrentPosition) {
//                    // If the user has swiped to a different ViewPager page, then we need to
//                    // remove the old shared element and replace it with the new shared element
//                    // that should be transitioned instead.
//                    names.clear();
//                    names.add(sharedElement.getTransitionName());
//                    sharedElements.clear();
//                    sharedElements.put(sharedElement.getTransitionName(), sharedElement);
//                }
            }
        }
    };
    private int startingPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        setContentView(R.layout.activity_article_detail);
        Intent intent = getIntent();
        transitionName = intent.getStringExtra(EXTRA_SHARED_TRANSITION);

        startingPosition = getIntent().getIntExtra(ArticleListActivity.EXTRA_STARTING_ARTICLE_POSITION, 0);
        if (savedInstanceState == null) {
            currentPosition = startingPosition;
        } else {
            currentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }


        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.setCurrentItem(currentPosition);

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
                currentPosition = position;
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, currentPosition);

    }
    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(ArticleListActivity.EXTRA_STARTING_ARTICLE_POSITION, startingPosition);
        data.putExtra(EXTRA_CURRENT_POSITION, currentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

//         Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentDetailsFragment = (ArticleDetailFragment) object;
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID)
                    ,"poster_" + position,position,startingPosition);
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
