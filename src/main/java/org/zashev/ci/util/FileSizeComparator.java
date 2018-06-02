package org.zashev.ci.util;

import org.zashev.ci.model.FileEntity;

import java.util.Comparator;

public class FileSizeComparator implements Comparator<FileEntity> {
    @Override
    public int compare(FileEntity o1, FileEntity o2) {
        return new Long(o2.getSizeInBytes()).compareTo(new Long(o1.getSizeInBytes()));
    }
}
