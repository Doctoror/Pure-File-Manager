package com.docd.purefm.utils;

import java.util.Comparator;

import com.docd.purefm.file.GenericFile;

import org.jetbrains.annotations.NotNull;

public enum FileSortType {

    NAME_ASC (new PureFMFileUtils.NameComparator()),
    NAME_DESC(new PureFMFileUtils.NameComparatorReverse()),
    TYPE_ASC (new PureFMFileUtils.TypeComparator()),
    TYPE_DESC(new PureFMFileUtils.TypeComparatorReverse()),
    SIZE_ASC (new PureFMFileUtils.SizeComparatorAsc()),
    SIZE_DESC(new PureFMFileUtils.SizeComparatorDesc()),
    DATE_ASC (new PureFMFileUtils.DateComparatorAsc()),
    DATE_DESC(new PureFMFileUtils.DateComparatorDesc());
    
    private Comparator<GenericFile> comparator;
    
    private FileSortType(Comparator<GenericFile> comparator) {
        this.comparator = comparator;
    }

    @NotNull
    public Comparator<GenericFile> getComparator() {
        return this.comparator;
    }
}
