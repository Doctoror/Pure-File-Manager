package com.docd.purefm.ui.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.docd.purefm.Environment;
import com.docd.purefm.R;
import com.docd.purefm.browser.Browser;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.ui.view.BreadCrumbTextView;
import com.docd.purefm.utils.MimeTypes;
import com.docd.purefm.utils.ThemeUtils;

public final class PickerActivity extends AbstractBrowserActivity {

    private BreadCrumbTextView mBreadCrumbView;

    /**
     * GET_CONTENT mime type
     */
    private String mGetContentMimeType;

    private View mUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Settings.theme == R.style.ThemeDark) {
            setTheme(R.style.ThemeDark_Overlay);
        } else {
            setTheme(R.style.ThemeLight_Overlay);
        }
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_picker);
        this.checkIntentAction(getIntent());
        this.initActionBar();
        this.setWindowParams();
    }

    private void setWindowParams() {
        final Resources res = getResources();
        final Window window = getWindow();
        final WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.width = (int) res.getDimension(R.dimen.picker_dialog_width);
        windowAttributes.height = (int) res.getDimension(R.dimen.picker_dialog_height);
        window.setAttributes(windowAttributes);

        final View content = findViewById(R.id.activity_picker_content);
        final ViewGroup.LayoutParams contentParams = content.getLayoutParams();
        if (contentParams != null) {
            contentParams.width = windowAttributes.width;
            contentParams.height = windowAttributes.height;
            content.setLayoutParams(contentParams);
        }
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
    public boolean isHistoryEnabled() {
        return false;
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
        setWindowParams();

        final int actionBarSize = (int) ThemeUtils.getDimension(getTheme(), android.R.attr.actionBarSize);
        final View actionBar = findViewById(R.id.activity_picker_actionbar);
        final ViewGroup.LayoutParams actionBarParams = actionBar.getLayoutParams();
        if (actionBarParams != null) {
            actionBarParams.height = actionBarSize;
            actionBar.setLayoutParams(actionBarParams);
            mBreadCrumbView.fullScrollRight();
        }
    }

    private void initActionBar() {
        mBreadCrumbView = (BreadCrumbTextView) findViewById(R.id.bread_crumb_view);
        mUp = findViewById(R.id.up);
        final View home = findViewById(android.R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BrowserFragment fragment = getCurrentlyDisplayedFragment();
                if (fragment != null) {
                    final Browser browser = fragment.getBrowser();
                    if (browser != null) {
                        browser.up();
                    }
                }
            }
        });
    }

    @Override
    protected String getGetContentMimeType() {
        return mGetContentMimeType;
    }

    @Override
    public void onNavigationCompleted(final GenericFile path) {
        mBreadCrumbView.setFile(path.toFile());
        mUp.setVisibility(path.getAbsolutePath().equals(Environment.sRootDirectory.getAbsolutePath()) ? View.INVISIBLE : View.VISIBLE);
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
