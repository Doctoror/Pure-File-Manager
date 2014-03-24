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
package com.docd.purefm.utils;

import java.util.Comparator;

import com.docd.purefm.file.GenericFile;

import org.jetbrains.annotations.NotNull;

public enum FileSortType {

    NAME_ASC (new PFMFileUtils.NameComparator()),
    NAME_DESC(new PFMFileUtils.NameComparatorReverse()),
    TYPE_ASC (new PFMFileUtils.TypeComparator()),
    TYPE_DESC(new PFMFileUtils.TypeComparatorReverse()),
    SIZE_ASC (new PFMFileUtils.SizeComparatorAsc()),
    SIZE_DESC(new PFMFileUtils.SizeComparatorDesc()),
    DATE_ASC (new PFMFileUtils.DateComparatorAsc()),
    DATE_DESC(new PFMFileUtils.DateComparatorDesc());
    
    private Comparator<GenericFile> comparator;
    
    private FileSortType(Comparator<GenericFile> comparator) {
        this.comparator = comparator;
    }

    @NotNull
    public Comparator<GenericFile> getComparator() {
        return this.comparator;
    }
}
