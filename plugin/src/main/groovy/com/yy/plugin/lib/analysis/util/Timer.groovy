package com.yy.plugin.lib.analysis.util;


class Timer {

    long start

    Timer() {
        start = System.currentTimeMillis()
    }

    void mark(Logger logger, String message) {
        long now = System.currentTimeMillis()
        logger?.log "${message ?: ""} cost ${now - start}ms"
        start = now
    }

}
