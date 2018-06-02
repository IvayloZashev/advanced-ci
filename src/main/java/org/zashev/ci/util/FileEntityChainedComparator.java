package org.zashev.ci.util;

import org.zashev.ci.model.FileEntity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileEntityChainedComparator implements Comparator<FileEntity> {

    List<Comparator<FileEntity>> comparators;

    @SafeVarargs
    public FileEntityChainedComparator(Comparator<FileEntity>... comparators) {
        this.comparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(FileEntity o1, FileEntity o2) {
        for (Comparator<FileEntity> comparator: comparators) {
            int result = comparator.compare(o1, o2);
            if(result != 0) {
                return result;
            }
        }
        return 0;
    }
}
