package com.yy.plugin.lib.analysis.util;



class Logger {

    static Logger D = new Logger()
    static Logger W = new Logger()

    private Logger() {
    }

    void log(def message) {
        print "LibReport "
        println message
    }
}
