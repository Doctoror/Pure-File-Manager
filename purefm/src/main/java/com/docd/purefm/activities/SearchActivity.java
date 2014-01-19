/*
 * Copyright 2014 Yaroslav Mytkalyk
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.docd.purefm.activities;

import java.util.List;

import com.docd.purefm.Environment;
import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.adapters.BrowserGridAdapter;
import com.docd.purefm.adapters.BrowserListAdapter;
import com.docd.purefm.browser.ActionModeController;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.tasks.SearchCommandLineTask;
import com.docd.purefm.tasks.SearchJavaTask;
import com.docd.purefm.utils.PureFMFileUtils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

/**
 * Activity used for Searching files.
 * @author Doctoror
 */
public final class SearchActivity extends SuperuserActionBarMonitoredActivity {

    private ActionModeController actionModeController;

    private AbsListView list;
    private BrowserBaseAdapter adapter;
    
    private TextView input;
    private View progress;
    
    private AsyncTask<String, GenericFile, Void> searchTask;
    private String path;
    
    private int prevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search);
        this.path = getIntent().getStringExtra(Extras.EXTRA_PATH);
        this.initActionBar();
        this.initView();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (searchTask != null && searchTask.getStatus() == AsyncTask.Status.RUNNING) {
            searchTask.cancel(true);
        }
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return false;
        }
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            this.onSearchClicked();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    
    private void initActionBar() {
        final View action = this.getLayoutInflater().inflate(R.layout.activity_search_actionbar, null);
        final TextView path = (TextView) action.findViewById(android.R.id.text1);
        path.setText(this.path);
        this.progress = action.findViewById(android.R.id.progress);
        
        final ActionBar bar = this.getActionBar();
        bar.setDisplayOptions(
                ActionBar.DISPLAY_HOME_AS_UP |
                ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_CUSTOM |
                ActionBar.DISPLAY_USE_LOGO);
        bar.setCustomView(action);

        this.actionModeController = new ActionModeController(this);
    }
    
    private void initView() {
        this.initList();
        this.input = (TextView) this.findViewById(android.R.id.input);
        this.input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onSearchClicked();
                return true;
            }
        });
        
        this.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchClicked();
            }
        });
    }
    
    private void onSearchClicked() {
        final String text = input.getText().toString().trim();
        if (text.length() < 3) {
            return;
        }
        if (searchTask != null && searchTask.getStatus() == AsyncTask.Status.RUNNING) {
            searchTask.cancel(true);
        }
        this.buildSearchTask(false);
        adapter.updateData(new GenericFile[0]);
        searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, text, path);
    }
    
    private void onPreExecute() {
        this.progress.setVisibility(View.VISIBLE);
    }
    
    private void onPostExecute() {
        this.progress.setVisibility(View.INVISIBLE);
        if (this.searchTask instanceof SearchCommandLineTask) {
            final List<String> denied = ((SearchCommandLineTask) this.searchTask).getDeniedLocations();
            if (!denied.isEmpty()) {
                final AlertDialog.Builder b = new AlertDialog.Builder(this);
                final StringBuilder message = new StringBuilder(getString(R.string.search_denied_message));
                for (final String deniedItem : denied) {
                    message.append('\n').append(deniedItem);
                }
                b.setMessage(message.toString());
                b.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final Dialog dialog = b.create();
                if (!isFinishing()) {
                    dialog.show();
                }
            }
        }
    }
    
    private void onProgressUpdate(GenericFile file) {
        adapter.addFile(file);
    }
    
    private void initList() {
        if (this.list != null) {
            this.list.getEmptyView().setVisibility(View.GONE);
            this.list.setVisibility(View.GONE);
        }
        
        if (Settings.appearance == Settings.APPEARANCE_LIST) {
            View vs = findViewById(android.R.id.list);
            if (vs instanceof ViewStub) {
                vs = ((ViewStub) vs).inflate();
            }
            this.list = (AbsListView) vs;
            this.adapter = new BrowserListAdapter(this);
        } else {
            View vs = findViewById(R.id.grid);
            if (vs instanceof ViewStub) {
                vs = ((ViewStub) vs).inflate();
            }
            this.list = (AbsListView) vs;
            this.adapter = new BrowserGridAdapter(this);
        }
        
        this.list.setId(this.getNewId());
        this.list.setEmptyView(findViewById(android.R.id.empty));
        this.list.setAdapter(this.adapter);
        this.list.getEmptyView().setVisibility(View.GONE);
        this.list.setVisibility(View.GONE);

        this.actionModeController.setListView(this.list);

        this.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                final GenericFile target = (GenericFile) (av.getItemAtPosition(pos));
                PureFMFileUtils.openFile(SearchActivity.this, target.toFile());
            }
        });
    }
    
    private int getNewId() {
        this.prevId++;
        while (findViewById(this.prevId) != null){  
              this.prevId++;
        }  
        return this.prevId;  
    }
    
    
    private void buildSearchTask(boolean su) {
        if (Settings.useCommandLine && Environment.hasBusybox()) {
            searchTask = new SearchCommandLineTask(su) {
                @Override
                protected void onPreExecute() {
                    SearchActivity.this.onPreExecute();
                }
                
                @Override
                protected void onPostExecute(Void result) {
                    SearchActivity.this.onPostExecute();
                }
                
                @Override
                protected void onCancelled(Void result) {
                    SearchActivity.this.onPostExecute();
                }
                
                @Override
                protected void onProgressUpdate(GenericFile... files) {
                    SearchActivity.this.onProgressUpdate(files[0]);
                }
            };
        } else {
            searchTask = new SearchJavaTask() {
                @Override
                protected void onPreExecute() {
                    SearchActivity.this.onPreExecute();
                }
                
                @Override
                protected void onPostExecute(Void result) {
                    SearchActivity.this.onPostExecute();
                }
                
                @Override
                protected void onCancelled(Void result) {
                    SearchActivity.this.onPostExecute();
                }
                
                @Override
                protected void onProgressUpdate(GenericFile... files) {
                    SearchActivity.this.onProgressUpdate(files[0]);
                }
            };
        }
    }
}
