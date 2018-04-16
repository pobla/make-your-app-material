package com.example.xyzreader.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore.Audio.ArtistColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ArticleLoader.Query;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
  implements LoaderManager.LoaderCallbacks<Cursor> {

  private Cursor mCursor;
  private long mStartId;


  private ViewPager mPager;
  private MyPagerAdapter mPagerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_article_detail);

    mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
    mPager = findViewById(R.id.pager);
    mPager.setAdapter(mPagerAdapter);
    mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
      }

      @Override
      public void onPageSelected(int position) {
        if (mCursor != null) {
          mCursor.moveToPosition(position);
        }
      }
    });


    if (savedInstanceState == null) {
      if (getIntent() != null && getIntent().getData() != null) {
        mStartId = ItemsContract.Items.getItemId(getIntent().getData());
      }
    }
    getSupportLoaderManager().restartLoader(0, null, this);

  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return ArticleLoader.newAllArticlesInstance(this);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    mCursor = cursor;
    mPagerAdapter.notifyDataSetChanged();

//     Select the start ID
    if (mStartId > 0) {
      mCursor.moveToFirst();
//       TODO: optimize
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
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    mCursor = null;
    mPagerAdapter.notifyDataSetChanged();
  }


//  Arreglar el rotate screen
//  @Override
//  protected void onSaveInstanceState(Bundle outState) {
//    super.onSaveInstanceState(outState);
//    outState.putInt("currentItem", mPager.getCurrentItem());
//  }
//
//  @Override
//  protected void onRestoreInstanceState(Bundle savedInstanceState) {
//    super.onRestoreInstanceState(savedInstanceState);
//    int currentItem = savedInstanceState.getInt("currentItem");
//    mPager.setCurrentItem(currentItem);
//    mStartId = currentItem;
//  }

  private class MyPagerAdapter extends FragmentStatePagerAdapter {

    public MyPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      mCursor.moveToPosition(position);
      Article article = getArticleFromCursor(mCursor);
      return ArticleDetailFragment.newInstance(article);
    }

    private Article getArticleFromCursor(Cursor mCursor) {
      Article article = new Article();
      article.setId(mCursor.getLong(Query._ID));
      article.setTitle(mCursor.getString(Query.TITLE));
      article.setPublisheDate(mCursor.getString(Query.PUBLISHED_DATE));
      article.setAuthor(mCursor.getString(Query.AUTHOR));
      article.setThumbUrl(mCursor.getString(Query.THUMB_URL));
      article.setPhotoUrl(mCursor.getString(Query.PHOTO_URL));
      article.setAspectRatio(mCursor.getString(Query.ASPECT_RATIO));
      article.setBody(mCursor.getString(Query.BODY));
      return article;
    }

    @Override
    public int getCount() {
      return (mCursor != null) ? mCursor.getCount() : 0;
    }

  }
}
