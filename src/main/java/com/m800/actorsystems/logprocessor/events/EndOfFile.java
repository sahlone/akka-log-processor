package com.m800.actorsystems.logprocessor.events;

/**
 * ENDOFFILE
 * An Event
 * Dispatched
 * - from FileParser
 * - to Aggregator
 * - when FileParser reaches the end of a file.
 *
 * Specifies
 * - targetFile
 * - finalLineNumber
 **/

import java.nio.file.Path;

public class EndOfFile {

  public final Path targetFile;


  public EndOfFile( Path targetFile ) {

    this.targetFile = targetFile;

  }

  @Override
  public String toString( ) {

    return "EndOfFile{ targetFile = " + this.targetFile + " }";

  }

}

