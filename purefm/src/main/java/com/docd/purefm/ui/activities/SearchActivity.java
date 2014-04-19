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
package com.docd.purefm.ui.activities;

import java.util.List;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.adapters.BrowserGridAdapter;
import com.docd.purefm.adapters.BrowserListAdapter;
import com.docd.purefm.controller.ActionModeController;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.settings.Settings;
import com.docd.purefm.tasks.AbstractSearchTask;
import com.docd.purefm.utils.PFMFileUtils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import android.support.annotation.NonNull;

/**
 * Activity used for Searching files.
 * @author Doctoror
 */
public final class SearchActivity extends ActionBarIconThemableActivity
        implements AbstractSearchTask.SearchTaskListener {

    private ActionModeController mActionModeController;

    private InputMethodManager mInputMethodManager;

    private AbsListView mList;
    private BrowserBaseAdapter mAdapter;

    private TextView mInput;
    private View mProgress;

    private AbstractSearchTask mSearchTask;
    private GenericFile mStartDirectory;

    private int mPrevId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search);
        mStartDirectory = (GenericFile) getIntent().getSerializableExtra(Extras.EXTRA_FILE);
        mInputMethodManager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        initActionBar();
        initView();
    }

    @Override
    protected void setActionBarIcon(final Drawable icon) {
        getActionBar().setIcon(icon);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter.isEmpty() && mInput.requestFocus()) {
            mInputMethodManager.showSoftInput(mInput, InputMethodManager.SHOW_IMPLICIT);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSearchTask != null && mSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSearchTask.cancel(true);
        }
    }

    @Override
    public boolean onKeyUp(final int keyCode, @NonNull final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            this.onSearchClicked();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void initActionBar() {
        final View actionBarCustom = this.getLayoutInflater().inflate(R.layout.activity_search_actionbar, null);
        final TextView path = (TextView) actionBarCustom.findViewById(android.R.id.text1);
        path.setText(mStartDirectory.getAbsolutePath());

        final ActionBar bar = this.getActionBar();
        bar.setDisplayOptions(
                ActionBar.DISPLAY_HOME_AS_UP |
                ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_SHOW_CUSTOM |
                ActionBar.DISPLAY_USE_LOGO);
        bar.setCustomView(actionBarCustom);

        mActionModeController = new ActionModeController(this);
    }

    private void initView() {
        this.initList();
        this.mProgress = findViewById(android.R.id.progress);
        this.mInput = (TextView) this.findViewById(android.R.id.input);
        this.mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

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
        final String text = mInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        if (mSearchTask != null && mSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSearchTask.cancel(true);
        }
        mAdapter.updateData(new GenericFile[0]);
        mSearchTask = AbstractSearchTask.create(mStartDirectory, this);
        mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, text);
    }

    @Override
    public void onPreExecute(@NonNull AbstractSearchTask task) {
        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProgressUpdate(@NonNull AbstractSearchTask task, GenericFile... files) {
        mAdapter.addFiles(files);
        mInputMethodManager.hideSoftInputFromWindow(mInput.getWindowToken(),
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public void onCancelled(@NonNull AbstractSearchTask task) {
        onPostExecute(task);
    }

    @Override
    public void onPostExecute(@NonNull AbstractSearchTask task) {
        mProgress.setVisibility(View.INVISIBLE);
            final List<String> denied = mSearchTask.getDeniedLocations();
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

    private void initList() {
        if (this.mList != null) {
            this.mList.getEmptyView().setVisibility(View.GONE);
            this.mList.setVisibility(View.GONE);
        }

        if (Settings.appearance == Settings.APPEARANCE_LIST) {
            View vs = findViewById(android.R.id.list);
            if (vs instanceof ViewStub) {
                vs = ((ViewStub) vs).inflate();
            }
            this.mList = (AbsListView) vs;
            this.mAdapter = new BrowserListAdapter(this);
        } else {
            View vs = findViewById(R.id.grid);
            if (vs instanceof ViewStub) {
                vs = ((ViewStub) vs).inflate();
            }
            this.mList = (AbsListView) vs;
            this.mAdapter = new BrowserGridAdapter(this);
        }

        this.mList.setId(this.getNewId());
        this.mList.setEmptyView(findViewById(android.R.id.empty));
        this.mList.setAdapter(this.mAdapter);
        this.mList.getEmptyView().setVisibility(View.GONE);
        this.mList.setVisibility(View.GONE);

        this.mActionModeController.setListView(this.mList);

        this.mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                final GenericFile target = (GenericFile) (av.getItemAtPosition(pos));
                PFMFileUtils.openFile(SearchActivity.this, target.toFile());
            }
        });
    }

    private int getNewId() {
        this.mPrevId++;
        while (findViewById(this.mPrevId) != null){
              this.mPrevId++;
        }
        return this.mPrevId;
    }
}
