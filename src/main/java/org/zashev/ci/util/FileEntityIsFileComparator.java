package org.zashev.ci.util;

import org.zashev.ci.model.FileEntity;

import java.util.Comparator;

public class FileEntityIsFileComparator implements Comparator<FileEntity> {
    @Override
    public int compare(FileEntity o1, FileEntity o2) {
        return new Boolean(o1.isFile()).compareTo(new Boolean(o2.isFile()));
    }
}
