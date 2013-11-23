package com.docd.purefm.activities;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.adapters.BookmarksAdapter;
import com.docd.purefm.settings.Settings;

import android.app.ActionBar;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public final class BookmarksActivity extends ListActivity {
        
    private BookmarksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_bookmarks);
        this.initActionBar();
        this.initView();
    }

    private void initActionBar() {
        this.getActionBar().setDisplayOptions(
                ActionBar.DISPLAY_HOME_AS_UP |
                ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_TITLE |
                ActionBar.DISPLAY_USE_LOGO);
    }

    private void initView() {
        this.adapter = new BookmarksAdapter(this,
                Settings.getBookmarks(getApplicationContext()));
        this.getListView().setAdapter(this.adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.adapter.isModified()) {
            Settings.saveBookmarks(getApplicationContext(), adapter.getData());
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_bookmarks, menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
                
            case R.id.menu_bookmarks_new:
                final String path = getIntent().getStringExtra(Extras.EXTRA_PATH);
                if (path != null) {
                    this.adapter.addItem(path);
                }
                return true;
                
            default:
                return false;
        }
    }

}
