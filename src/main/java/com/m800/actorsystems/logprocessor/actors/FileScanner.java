package com.m800.actorsystems.logprocessor.actors;

/**
 * 
 * FileScanner
 * 
 * This class is an Actor
 * Which scans a predefined
 * directory for files
 * and creates Actors to 
 * process those files.
 *
 *
 * The FileScanner creates
 * a different FileParser
 * for each file
 * so that
 * Many files may be read
 * and processed
 * concurrently.
 *
 **/

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

import com.m800.actorsystems.logprocessor.messages.Scan;
import com.m800.actorsystems.logprocessor.messages.Parse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileScanner extends AbstractActor {

  protected final LoggingAdapter log = Logging.getLogger( context( ).system( ), this );
  protected final Map<Parse,ActorRef> fileParserRefs = new HashMap<Parse,ActorRef>( );
  protected final Path logDirectory;

  private FileScanner( String logDirectoryName ) {

    this.logDirectory = Paths.get( logDirectoryName ).toAbsolutePath( );

    log.info( "Log directory : {} ", this.logDirectory );

    receive( 
      ReceiveBuilder.
        match( Scan.class, message -> this.handle( message ) ).
        matchAny( o -> log.info( "FileScanner does not understand this message {}", o ) ).
        build( )
    );

  }

  private void handle( Scan message ) throws IOException {

    log.info( "FileScanner received Scan message: {}", message );


    Files.list( this.logDirectory )
      .filter( Files::isRegularFile )
      .forEach( filePath -> this.startNewLineAggregatorFor( filePath ) );

  }

  private void startNewLineAggregatorFor( Path filePath ) {

    log.info( "File scanner sees this file {} and will make a parser for it...", filePath );

    ActorRef fileParserRef = getContext( ).actorOf(
        Props.create( FileParser.class ), "file-parser-" + UUID.randomUUID( )
      );

    Parse parseMessage = new Parse( filePath );

    fileParserRef.tell( parseMessage, null );

    this.fileParserRefs.put( parseMessage, fileParserRef );

  }

}


