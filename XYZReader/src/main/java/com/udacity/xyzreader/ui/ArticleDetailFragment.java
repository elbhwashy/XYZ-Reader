package com.udacity.xyzreader.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.udacity.xyzreader.R;
import com.udacity.xyzreader.data.ArticleLoader;
import com.udacity.xyzreader.service.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;

    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private boolean mIsCard = false;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    private Toolbar mToolbar;
    private String mTitle;
    private String mShareTitleString;
    private Boolean mFragmentVisibilityState = false;

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

        if (null != getArguments() && getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mToolbar = mRootView.findViewById(R.id.toolbar);
        mPhotoView = mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        bindViews();
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
        if (null == mRootView) {
            return;
        }

        TextView titleView = mRootView.findViewById(R.id.article_title);
        TextView bylineView = mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = mRootView.findViewById(R.id.article_body);

        if (mIsCard) {
            View spacer = mRootView.findViewById(R.id.spacer);
            if (0 == spacer.getHeight()) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                ViewGroup.LayoutParams params = spacer.getLayoutParams();
                TypedValue spacerRatioValue = new TypedValue();
                getResources().getValue(R.dimen.spacer_to_window_height_ratio, spacerRatioValue, true);
                params.height = Math.round(displayMetrics.heightPixels * spacerRatioValue.getFloat());
                spacer.setLayoutParams(params);
            }
        }

        if (null != mCursor) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            mTitle = mCursor.getString(ArticleLoader.Query.TITLE);
            mToolbar.setTitle(mTitle);
            enableHomeAsUp();
            titleView.setText(mTitle);
            mShareTitleString = mTitle + " by " + mCursor.getString(ArticleLoader.Query.AUTHOR);
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
                mShareTitleString += " (" + DateUtils.getRelativeTimeSpanString(
                        publishedDate.getTime(),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString() + ")";

            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
                mShareTitleString += " (" + outputFormat.format(publishedDate) + ")";

            }
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));

            if (!mIsCard) {
                Picasso.get().load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                        .noPlaceholder().error(R.drawable.ic_broken_image)
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                if (null != bitmap) {
                                    ConstraintSet constraints = new ConstraintSet();
                                    constraints.clone((ConstraintLayout) mPhotoView.getParent());
                                    constraints.setDimensionRatio(mPhotoView.getId(),
                                            (bitmap.getWidth() >= bitmap.getHeight() ? "H," : "W,")
                                                    + bitmap.getWidth() + ":"
                                                    + bitmap.getHeight());
                                    constraints.applyTo((ConstraintLayout) mPhotoView.getParent());
                                    mPhotoView.setImageBitmap(bitmap);
                                    mPhotoView.setAdjustViewBounds(true);
                                    mPhotoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    Palette p = new Palette.Builder(bitmap).generate();
                                    mMutedColor = p.getDarkMutedColor(0xFF333333);
                                    mRootView.findViewById(R.id.meta_bar).setBackgroundColor(mMutedColor);
                                }
                                scheduleSupportStartPostponedTransition(mPhotoView);
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                Log.d(TAG, "Picasso failed to load image for id " + mCursor.getInt(ArticleLoader.Query._ID));
                                scheduleSupportStartPostponedTransition(mPhotoView);
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                            }
                        });
                mRootView.findViewById(R.id.meta_bar).post(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mPhotoContainerView.getLayoutParams();
                        params.bottomMargin = mRootView.findViewById(R.id.meta_bar).getHeight();
                        mPhotoContainerView.setLayoutParams(params);
                        mPhotoContainerView.invalidate();
                        mPhotoContainerView.requestLayout();
                    }
                });
            } else {
                Picasso.get().load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                        .noPlaceholder().error(R.drawable.ic_broken_image)
                        .fit().centerCrop().into(mPhotoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) mPhotoView.getDrawable()).getBitmap();
                        if (null != bitmap) {
                            Palette p = new Palette.Builder(bitmap).generate();
                            mMutedColor = p.getDarkMutedColor(0xFF333333);
                            mRootView.findViewById(R.id.meta_bar)
                                    .setBackgroundColor(mMutedColor);
                            if (mFragmentVisibilityState) {
                                mRootView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        final View spacer = mRootView.findViewById(R.id.spacer);
                                        final int spacerHeightOriginal = spacer.getHeight();
                                        ValueAnimator shrinkAnim = Utilities.getToggleHeightAnimator(
                                                spacer,
                                                spacerHeightOriginal,
                                                (int) (spacerHeightOriginal * 0.5),
                                                450);
                                        shrinkAnim.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                ValueAnimator expandAnim = Utilities.getToggleHeightAnimator(
                                                        spacer,
                                                        spacer.getHeight(),
                                                        spacerHeightOriginal,
                                                        750);
                                                expandAnim.start();
                                            }
                                        });
                                        shrinkAnim.start();
                                    }
                                });
                            }
                        }
                        scheduleSupportStartPostponedTransition(mPhotoView);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "Picasso failed to load image for id " + mCursor.getInt(ArticleLoader.Query._ID));
                        scheduleSupportStartPostponedTransition(mPhotoView);
                    }
                });
            }
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (null != cursor)
                cursor.close();
            return;
        }

        mCursor = cursor;
        if (null != mCursor && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public String getSharedTitle() {
        return mShareTitleString;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mFragmentVisibilityState = isVisibleToUser;
        enableHomeAsUp();
    }

    private void enableHomeAsUp() {
        if (mFragmentVisibilityState && null != mToolbar) {
            AppCompatActivity hostActivity = (AppCompatActivity) getContext();
            if (null != hostActivity) {
                hostActivity.setSupportActionBar(mToolbar);
                //hostActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //hostActivity.getSupportActionBar().setHomeButtonEnabled(true);
            }
        }
    }

    private void scheduleSupportStartPostponedTransition(final View sharedElement) {
        if (mFragmentVisibilityState) {
            sharedElement.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                            getActivity().supportStartPostponedEnterTransition();
                            return true;
                        }
                    });
        }
    }
}