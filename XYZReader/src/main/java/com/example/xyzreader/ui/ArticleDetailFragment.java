package com.example.xyzreader.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xyzreader.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements Callback, PaletteAsyncListener {
  private static final String TAG = "ArticleDetailFragment";

  public static final String ARG_ITEM = "item";
  private static final float PARALLAX_FACTOR = 1.25f;

  private Article article;
  private View mRootView;
  private Toolbar mToolbar;
  private CollapsingToolbarLayout collapsingToolbarLayout;
  private ColorDrawable mStatusBarColorDrawable;

  private int mTopInset;
  //    private View mPhotoContainerView;
  private FloatingActionButton shareButton;
  private ImageView mPhotoView;
  private LinearLayout metaBar;
  private int mScrollY;
  private boolean mIsCard = false;
//  private int mStatusBarFullOpacityBottom;

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
  private SimpleDateFormat outputFormat = new SimpleDateFormat();
  // Most time functions can only handle 1902 - 2037
  private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
  private TextView articleAuthor;
  private TextView articleDate;
  private TextView bodyView;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ArticleDetailFragment() {
  }

  public static ArticleDetailFragment newInstance(Article article) {
    Bundle arguments = new Bundle();
    arguments.putParcelable(ARG_ITEM, article);
    ArticleDetailFragment fragment = new ArticleDetailFragment();
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments().containsKey(ARG_ITEM)) {
      article = getArguments().getParcelable(ARG_ITEM);
    }

    mIsCard = getResources().getBoolean(R.bool.detail_is_card);
    setHasOptionsMenu(true);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (getUserVisibleHint()) {
      setActionBar();
    }
  }

  private void setActionBar() {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(mToolbar);
    activity.getSupportActionBar().setHomeButtonEnabled(true);
    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isVisibleToUser && getActivity() != null) {
      setActionBar();
    }
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

    articleAuthor = mRootView.findViewById(R.id.article_byline);
    articleDate = mRootView.findViewById(R.id.article_date);
    articleAuthor.setMovementMethod(new LinkMovementMethod());
    bodyView = mRootView.findViewById(R.id.article_body);
    bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
    bodyView.setMovementMethod(new ScrollingMovementMethod());

    return mRootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    bindViews();
  }

  private Date parsePublishedDate() {
    try {
      return dateFormat.parse(article.getPublisheDate());
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

    if (article != null) {
      mRootView.setAlpha(0);
      mRootView.setVisibility(View.VISIBLE);
      mRootView.animate().alpha(1);
      collapsingToolbarLayout.setTitle(article.getTitle());
      String stringPublishedDate = getPublishDate();

      articleDate.setText(stringPublishedDate);
      articleAuthor.setText(getString(R.string.space_author_by, article.getAuthor()));
      bodyView.setText(Html.fromHtml(article.getBody().replaceAll("(\r\n|\n)", "<br />")));
      bodyView.setText(article.getBody());

      final FragmentActivity activity = getActivity();
      Picasso.with(activity)
        .load(article.getPhotoUrl())
        .into(mPhotoView, this);

    } else {
      mRootView.setVisibility(View.GONE);
    }
  }

  private String getPublishDate() {
    Date publishedDate = parsePublishedDate();
    String stringPublishedDate;
    if (!publishedDate.before(START_OF_EPOCH.getTime())) {
      stringPublishedDate = DateUtils.getRelativeTimeSpanString(
        publishedDate.getTime(),
        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_ALL).toString();
    } else {
      stringPublishedDate = outputFormat.format(publishedDate);
    }
    return stringPublishedDate;
  }

  @Override
  public void onSuccess() {
    Bitmap bitmap = ((BitmapDrawable) mPhotoView.getDrawable()).getBitmap();
    if (bitmap != null) {
      Palette.from(bitmap).generate(this);
    }


  }

  @Override
  public void onError() {
    Toast.makeText(getActivity(), "Error loading image", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onGenerated(@NonNull Palette palette) {
    Resources resources = metaBar.getResources();
    int darkMutedColor = palette.getDarkMutedColor(resources.getColor(R.color.theme_primary_dark));
    metaBar.setBackgroundColor(darkMutedColor);
    collapsingToolbarLayout.setContentScrimColor(darkMutedColor);
    int vibrantColor = palette.getVibrantColor(resources.getColor(R.color.theme_accent));
    shareButton.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
  }
}
