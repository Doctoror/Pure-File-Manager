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
package com.docd.purefm.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public final class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private boolean isChecked;
    
    public CheckableRelativeLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableRelativeLayout(Context context) {
        super(context);
    }

    @Override
    public boolean isChecked() {
        return this.isChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    @Override
    public void toggle() {
        this.isChecked = !this.isChecked;
    }

}
