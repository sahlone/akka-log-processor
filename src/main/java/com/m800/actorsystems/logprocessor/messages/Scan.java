package com.m800.actorsystems.logprocessor.messages;

/**
 * SCAN
 * A Message
 * Passed
 * - to FileScanner
 * - from System
 * - causes to occur: FileScanner will check if any file is in a predefined directory
 * <p>
 * Specifies
 * - name : "scan"
 **/


public class Scan {

    public final String name;
    public final String dir;

    public Scan(String logDir) {

        this.name = "scan";
        this.dir = logDir;

    }

    @Override
    public String toString() {

        return "Scan{ }";

    }

}
