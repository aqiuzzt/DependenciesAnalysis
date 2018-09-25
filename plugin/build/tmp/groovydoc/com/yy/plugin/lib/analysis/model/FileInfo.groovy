package com.yy.plugin.lib.analysis.model

class FileInfo {
    String id
    long size
    String type
    File file

    FileInfo(String id, long size, String type, File file) {
        this.id = id
        this.size = size
        this.type = type
        this.file = file
    }

}
