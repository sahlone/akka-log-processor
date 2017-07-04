package com.m800.actorsystems.logprocessor.events;

/**
 * LINE
 * An Event
 * Dispatched
 * - from FileParser
 * - to Aggregator
 * - when FileParser has read a line from a file
 * 
 * Specifies
 * - targetFile
 * - sequenceNumber : the line number, which allows lines to be collated even if they arrive out of sequence
 * - read : the String representation of the line that was just read
 * 
 **/

import java.nio.file.Path;

public class Line {

  public final Path targetFile;
  public final long sequenceNumber;
  public final String read;

  public Line( Path targetFile, long sequenceNumber, String read ) {

    this.targetFile = targetFile;
    this.sequenceNumber = sequenceNumber;
    this.read = read;

  }

  @Override
  public String toString( ) {

    return "Line{ " +
      "targetFile = " + this.targetFile +
      ", sequenceNumber = " + this.sequenceNumber +
      ", read = " + this.read +
      " }";

  }

}

