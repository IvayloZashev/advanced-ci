package org.zashev.ci.model;

public class FileEntity implements Comparable<FileEntity> {
    private String path;
    private String name;
    private boolean isFile;
    private long sizeInBytes;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    public int compareTo(FileEntity fileEntity) {
        int result = new Boolean(this.isFile).compareTo(new Boolean(fileEntity.isFile));
        if (result == 0) {
            result = new Long(this.getSizeInBytes()).compareTo(new Long(fileEntity.getSizeInBytes()));
        }
        return result;
    }
}
