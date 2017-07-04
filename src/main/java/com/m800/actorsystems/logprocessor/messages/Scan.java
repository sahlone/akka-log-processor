package com.m800.actorsystems.logprocessor.messages;

/**
 * SCAN
 * A Message 
 * Passed 
 * - to FileScanner 
 * - from System
 * - causes to occur: FileScanner will check if any file is in a predefined directory
 *
 * Specifies
 * - name : "scan"
 **/


public class Scan {

  public final String name;

  public Scan( ) {
    
    this.name = "scan";

  }

  @Override
  public String toString( ) {
    
    return "Scan{ }";

  }

}
