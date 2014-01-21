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
package com.docd.purefm.controller;

import java.io.File;
import java.math.BigInteger;

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.PureFMFileUtils;
import com.docd.purefm.utils.PureFMTextUtils;

import android.view.View;
import android.widget.TextView;

public final class FilePropertiesController {

    private final View mView;
    private final GenericFile mFile;
    
    public FilePropertiesController(View view, GenericFile file) {
        this.mView = view;
        this.mFile = file;
        this.init();
    }
    
    private void init() {
        final TextView name = (TextView) mView.findViewById(R.id.name);
        name.setText(mFile.getName());
        
        final TextView parent = (TextView) mView.findViewById(R.id.location);
        String par = mFile.toFile().getParent();
        if (par == null || par.isEmpty()) {
            par = File.listRoots()[0].getPath();
        }
        parent.setText(par);
        
        final TextView type = (TextView) mView.findViewById(R.id.type);
        type.setText(mFile.isSymlink() ? R.string.type_symlink :
                mFile.isDirectory() ? R.string.type_directory : R.string.type_file);
        
        final TextView mime = (TextView) mView.findViewById(R.id.mime);
        if (mFile.getMimeType() != null) {
           mime.setText(mFile.getMimeType());
        }
        
        final TextView size = (TextView) mView.findViewById(R.id.size);
        if (!mFile.isDirectory()) {
            size.setText(PureFMFileUtils.byteCountToDisplaySize(
                    BigInteger.valueOf(mFile.length())));
        }
        
        final TextView mod = (TextView) mView.findViewById(R.id.modified);
        mod.setText(PureFMTextUtils.humanReadableDate(mFile.lastModified()));
    }
}
