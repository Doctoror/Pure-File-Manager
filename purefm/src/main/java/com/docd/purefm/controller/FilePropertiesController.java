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

import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;

import android.view.View;
import android.widget.TextView;

public final class FilePropertiesController {

    private final View view;
    private final GenericFile file;
    
    public FilePropertiesController(View view, GenericFile file) {
        this.view = view;
        this.file = file;
        this.init();
    }
    
    private void init() {
        final TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(file.getName());
        
        final TextView parent = (TextView) view.findViewById(R.id.location);
        String par = file.toFile().getParent();
        if (par == null || par.isEmpty()) {
            par = File.listRoots()[0].getPath();
        }
        parent.setText(par);
        
        final TextView type = (TextView) view.findViewById(R.id.type);
        type.setText(file.isSymlink() ? R.string.type_symlink :
                file.isDirectory() ? R.string.type_directory : R.string.type_file);
        
        final TextView mime = (TextView) view.findViewById(R.id.mime);
        if (file.getMimeType() != null) {
           mime.setText(file.getMimeType());
        }
        
        final TextView size = (TextView) view.findViewById(R.id.size);
        if (!file.isDirectory()) {
            size.setText(file.humanReadableLength());
        }
        
        final TextView mod = (TextView) view.findViewById(R.id.modified);
        mod.setText(file.humanReadableLastModified());
    }
}
