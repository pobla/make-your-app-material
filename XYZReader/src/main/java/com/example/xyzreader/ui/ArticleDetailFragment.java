package com.example.xyzreader.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements LoaderCallbacks<Cursor>{
  private static final String TAG = "ArticleDetailFragment";

  public static final String ARG_ITEM_ID = "item_id";
  private static final float PARALLAX_FACTOR = 1.25f;

  private Cursor mCursor;
  private long mItemId;
  private View mRootView;
  private Toolbar mToolbar;
  private CollapsingToolbarLayout collapsingToolbarLayout;
  //    private ObservableScrollView mScrollView;
//    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
  private ColorDrawable mStatusBarColorDrawable;

  private int mTopInset;
  //    private View mPhotoContainerView;
  private FloatingActionButton shareButton;
  private ImageView mPhotoView;
  private FrameLayout metaBar;
  private int mScrollY;
  private boolean mIsCard = false;
//  private int mStatusBarFullOpacityBottom;

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
  private SimpleDateFormat outputFormat = new SimpleDateFormat();
  // Most time functions can only handle 1902 - 2037
  private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
  private TextView bylineView;
  private TextView bodyView;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ArticleDetailFragment() {
  }

  public static ArticleDetailFragment newInstance(long itemId) {
    Bundle arguments = new Bundle();
    arguments.putLong(ARG_ITEM_ID, itemId);
    ArticleDetailFragment fragment = new ArticleDetailFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments().containsKey(ARG_ITEM_ID)) {
      mItemId = getArguments().getLong(ARG_ITEM_ID);
    }

    mIsCard = getResources().getBoolean(R.bool.detail_is_card);
    setHasOptionsMenu(true);
    getLoaderManager().restartLoader((int)mItemId, null, this);
  }

  public ArticleDetailActivity getActivityCast() {
    return (ArticleDetailActivity) getActivity();
  }

  @Override
  public void onResume() {
    super.onResume();
//    getActivity().findViewById(R.id.app_bar).setVisibility(View.GONE);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(mToolbar);
    activity.getSupportActionBar().setHomeButtonEnabled(true);
    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(getActivity());
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
    mRootView.setVisibility(View.INVISIBLE);
    mToolbar = mRootView.findViewById(R.id.toolbar);
    collapsingToolbarLayout = mRootView.findViewById(R.id.toolbar_layout);
    mPhotoView = mRootView.findViewById(R.id.photo);
    metaBar = mRootView.findViewById(R.id.meta_bar);
    mStatusBarColorDrawable = new ColorDrawable(0);

    shareButton = mRootView.findViewById(R.id.share_fab);
    shareButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                                             .setType("text/plain")
                                             .setText("Some sample text")
                                             .getIntent(), getString(R.string.action_share)));
      }
    });

    bylineView = mRootView.findViewById(R.id.article_byline);
    bylineView.setMovementMethod(new LinkMovementMethod());
    bodyView = mRootView.findViewById(R.id.article_body);
    bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

//    bindViews();
    return mRootView;
  }

  private Date parsePublishedDate() {
    try {
      String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
      return dateFormat.parse(date);
    } catch (ParseException ex) {
      Log.e(TAG, ex.getMessage());
      Log.i(TAG, "passing today's date");
      return new Date();
    }
  }

  private void bindViews() {
    if (mRootView == null) {
      return;
    }
    mRootView.setVisibility(View.VISIBLE);

    if (mCursor != null) {
      mRootView.setAlpha(0);
      mRootView.setVisibility(View.VISIBLE);
      mRootView.animate().alpha(1);
      collapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
      Date publishedDate = parsePublishedDate();
      if (!publishedDate.before(START_OF_EPOCH.getTime())) {
        bylineView.setText(Html.fromHtml(
          DateUtils.getRelativeTimeSpanString(
            publishedDate.getTime(),
            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_ALL).toString()
            + " by <font color='#ffffff'>"
            + mCursor.getString(ArticleLoader.Query.AUTHOR)
            + "</font>"));

      } else {
        // If date is before 1902, just show the string
        bylineView.setText(Html.fromHtml(
          outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
            + mCursor.getString(ArticleLoader.Query.AUTHOR)
            + "</font>"));

      }

      bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));
      ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
        .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
          @Override
          public void onResponse(final ImageLoader.ImageContainer imageContainer, boolean b) {
            Bitmap bitmap = imageContainer.getBitmap();
            if (bitmap != null) {
              Palette.from(bitmap).generate(new PaletteAsyncListener() {
                @Override
                public void onGenerated(@NonNull Palette palette) {
                  int darkMutedColor = palette.getDarkMutedColor(getActivity().getResources().getColor(R.color.theme_primary_dark));
                  mPhotoView.setImageBitmap(imageContainer.getBitmap());
                  metaBar.setBackgroundColor(darkMutedColor);
                  collapsingToolbarLayout.setContentScrimColor(darkMutedColor);
                  int vibrantColor = palette.getVibrantColor(getActivity().getResources().getColor(R.color.theme_accent));
                  shareButton.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
                }
              });

            }
          }

          @Override
          public void onErrorResponse(VolleyError volleyError) {

          }
        });
    } else {
      mRootView.setVisibility(View.GONE);
//      titleView.setText("N/A");
//      bylineView.setText("N/A");
//      bodyView.setText("N/A");
    }
  }

  @Override
  public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
  }

  @Override
  public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
    if (!isAdded()) {
      if (cursor != null) {
        cursor.close();
      }
      return;
    }

    mCursor = cursor;
    if (mCursor != null && !mCursor.moveToFirst()) {
      Log.e(TAG, "Error reading item detail cursor");
      mCursor.close();
      mCursor = null;
    }

    bindViews();
  }

  @Override
  public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
    mCursor = null;
    bindViews();
  }

}
