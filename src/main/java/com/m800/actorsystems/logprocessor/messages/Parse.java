package com.m800.actorsystems.logprocessor.messages;

/**
 * PARSE
 * A Message
 * Passed
 * - to FileParser
 * - from FileScanner
 * - causes to occur: FileParser initiates the parsing
 *
 * Details not specified and inferred:
 * - FileParser parses one file at a time
 * - FileParser parses a file when it receives a parse message
 * - The PARSE message must contain the file name and path
 * 
 * Specifies
 * - name : "parse"
 * - fileToParse : the Path of the file the FileParser will parse
 *
 **/

import java.nio.file.Path;

public class Parse {

  public final String name;
  public final Path fileToParse;

  public Parse( Path fileToParse ) {

    this.name = "parse";
    this.fileToParse = fileToParse;

  }

  @Override
  public String toString( ) {
  
    return "Parse{ fileToParse : " + this.fileToParse + "}";
    
  }

}
