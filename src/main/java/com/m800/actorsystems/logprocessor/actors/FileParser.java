package com.m800.actorsystems.logprocessor.actors;

/**
 * AsynchronousFileParser
 *
 * The class is an Actor
 * Which parses a file
 * Passed to it in a Parse message.
 * 
 * It performs the parsing asynchronously
 * using AsynchronousFileChannel
 * so that multiple such AsynchronousFileParser instances
 * can parse multiple files
 * concurrently.
 *
 * Currently, it asynchronously reads one byte at a time
 * and emits a Line event to the Akka Event Stream
 * when it reaches a line delmiter ( here set as newline ).
 *
 * When it reaches the end of the file it emits an EndOfFile
 * event to the event stream recording the total number of lines
 * the parser counted for this file. 
 * 
 * The AsynchronousFileParser also emits a StartOfFile event,
 * and also creates a LineAggregator, which it subscribes to 
 * the event stream.
 *
 * In the event of errors, this Actor catches them and reports, 
 * emitting and EndOfFile event. No further implementation was made
 * of error handling or Actor shutdown logic in this version.
 * 
 * In the case that this Actor receives a new Parse event
 * it will abandon any other work it is doing, and begin 
 * a new parse of the file specified in the latest Parse event.
 *
 **/

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

import com.m800.actorsystems.logprocessor.messages.Parse;

import com.m800.actorsystems.logprocessor.events.StartOfFile;
import com.m800.actorsystems.logprocessor.events.Line;
import com.m800.actorsystems.logprocessor.events.EndOfFile;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import scala.concurrent.duration.FiniteDuration;
import java.lang.Runnable;

public class FileParser extends AbstractActor {

  protected final byte DELIMETER = (byte) '\n';
  protected final LoggingAdapter log = Logging.getLogger( context( ).system( ), this );
  protected final CompletionHandler< Integer, ByteBuffer > readComplete;
  protected Path fileToParse;
  protected ActorRef lineAggregator;
  protected long pos;
  protected long lineSequence;
  protected ByteArrayOutputStream line;
  protected AsynchronousFileChannel pipe;
  protected FileLock lock;
  protected ByteBuffer octet = ByteBuffer.allocate( 1 );
  
  private FileParser( ) {

    FileParser self = this;

    this.readComplete = makeCompletionHandler( );

    receive( ReceiveBuilder.
      match( Parse.class, message -> this.handle( message ) ).
      matchAny( o -> log.info( "FileParser does not understand this message {}", o ) ).
      build( )
    );

  }

  public void handleStartOfFile( ) {

    context( ).system( ).eventStream( ).publish( new StartOfFile( this.fileToParse ) );

  }

  public void handleEndOfFile( ) {

    context( ).system( ).eventStream( ).publish( new EndOfFile( this.fileToParse) );

  }

  public void handleLine( ) { 

    String line = this.line.toString( );
    this.line.reset( );
    this.lineSequence += 1;
    context( ).system( ).eventStream( ).publish( 
      new Line( this.fileToParse, this.lineSequence, line ) 
    );

  }

  public void scheduleNextByteRead( ) throws IOException {

    this.lock = this.pipe.tryLock( this.pos, 1, true );

    if ( this.lock == null ) 
      this.handleLockBusy( );

    else {
      this.octet = ByteBuffer.allocate( 1 );
      this.pipe.read( this.octet, this.pos, null, this.readComplete );
    }

  }

  public void configureFrom( Parse message ) {



    this.fileToParse = message.fileToParse;
    this.lineAggregator = getContext( ).actorOf( Props.create( Aggregator.class, this.fileToParse ), "line-aggregator" );
    this.pos = 0;
    this.lineSequence = 0; 
    this.line = new ByteArrayOutputStream( );

  }

  public void openAsyncChannel( Path fileToParse ) throws IOException {

    this.pipe = AsynchronousFileChannel.open( fileToParse, StandardOpenOption.READ );

  }

  private void handle( Parse message ) {
        
    log.info( "FileParser received Parse message: {}", message );

    this.configureFrom( message );
    this.tapFileEventStream( this.lineAggregator );

    try {

      this.openAsyncChannel( this.fileToParse );
      this.handleStartOfFile( );
      this.scheduleNextByteRead( );

    } catch( IOException e ) {
      
      log.info( "Message {} result : Parsing file {} caused error {}", message, this.fileToParse, e );

    } 
      
  }

  private void tapFileEventStream( ActorRef subscriber ) {

    context( ).system( ).eventStream( ).subscribe( subscriber, StartOfFile.class );
    context( ).system( ).eventStream( ).subscribe( subscriber, Line.class );
    context( ).system( ).eventStream( ).subscribe( subscriber, EndOfFile.class );

  }

  private CompletionHandler< Integer, ByteBuffer > makeCompletionHandler( ) {

    FileParser self = this;

    return new CompletionHandler< Integer, ByteBuffer >( ) {

      public void completed( Integer result, ByteBuffer target ) {

        try {
          self.lock.release( );
          //log.info( "Read complete {}", result );
          self.processNextByte( result );
        } catch( IOException e ) {
          self.log.info( "Error on complete {}", e );
        }

      }

      public void failed( Throwable exception, ByteBuffer target ) {

        try { 
          self.lock.release( );
          self.log.info( "Read failed {}", exception );      
          self.handleEndOfFile( );
        } catch ( IOException e ) {
          self.log.info( "Error on complete {}", e );
        }

      }

    };

  }
  
  private Runnable makeNextReadScheduler( ) {

    FileParser self = this;

    return new Runnable( ) {
      public void run( ) {

        try {
          self.scheduleNextByteRead( );
        } catch( IOException e ) {
          self.log.info( "Error on run {}", e ); 
        }

      }
    };

  }

  private void handleLockBusy( ) {

    this.log.info( "Could not acquire lock" );

    context( ).system( ).scheduler( ).scheduleOnce( 
      FiniteDuration.create( 5, "milliseconds" ),
      this.makeNextReadScheduler( ),
      context( ).system( ).dispatcher( )
    );

  }

  private void processNextByte( Integer numRead ) throws IOException {
    
    if ( numRead <= 0 ) {
      this.handleEndOfFile( );
      return;
    } 

    this.line.write( this.octet.get( 0 ) );

    if ( this.octet.get( 0 ) == this.DELIMETER ) {
      this.handleLine( );    
    }

    this.pos += 1;
    this.scheduleNextByteRead( );

  }

}


