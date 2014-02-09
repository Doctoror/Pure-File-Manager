package com.docd.purefm.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.ui.view.BreadCrumbTextView;
import com.docd.purefm.utils.MimeTypes;

public final class PickerActivity extends AbstractBrowserActivity {

    private BreadCrumbTextView mBreadCrumbView;

    /**
     * GET_CONTENT mime type
     */
    private String mGetContentMimeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Settings.theme == R.style.ThemeDark) {
            setTheme(R.style.ThemeDark_Overlay);
        } else {
            setTheme(R.style.ThemeLight_Overlay);
        }
        super.onCreate(savedInstanceState);
        final ViewGroup content = (ViewGroup) findViewById(android.R.id.content);
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        content.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (metrics.widthPixels * 0.9f),
                (int) (metrics.heightPixels * 0.8f)
        ));

        this.setContentView(R.layout.activity_picker);
        this.checkIntentAction(getIntent());
        this.initActionBar();
    }

    @Override
    protected void setCurrentlyDisplayedFragment(BrowserFragment fragment) {
        //do nothing
    }

    @Override
    protected void setOnSequenceClickListener(BreadCrumbTextView.OnSequenceClickListener sequenceListener) {
        if (mBreadCrumbView != null) {
            mBreadCrumbView.setOnSequenceClickListener(sequenceListener);
        }
    }

    @Override
    protected boolean shouldShowBrowserFragmentMenu() {
        return false;
    }

    @Override
    public void invalidateList() {
        //do nothing
    }

    @Override
    protected boolean setThemeInOnCreate() {
        return false;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        checkIntentAction(intent);
    }

    private void checkIntentAction(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_GET_CONTENT.equals(action)) {
            mGetContentMimeType = intent.getType();
            if (mGetContentMimeType == null || mGetContentMimeType.isEmpty()) {
                mGetContentMimeType = MimeTypes.ALL_MIME_TYPES;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mBreadCrumbView.fullScrollRight();
    }

    private void initActionBar() {
        mBreadCrumbView = (BreadCrumbTextView) findViewById(R.id.bread_crumb_view);
        final View home = findViewById(android.R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected String getGetContentMimeType() {
        return mGetContentMimeType;
    }

    @Override
    public void onNavigationCompleted(GenericFile path) {
        mBreadCrumbView.setFile(path.toFile());
    }

    @Override
    protected void setActionBarIcon(final Drawable icon) {
        ((ImageView) findViewById(R.id.home_icon)).setImageDrawable(icon);
    }

    @Override
    protected BrowserFragment getCurrentlyDisplayedFragment() {
        return (BrowserFragment) getFragmentManager().findFragmentById(
                R.id.activity_picker_fragment);
    }

}
