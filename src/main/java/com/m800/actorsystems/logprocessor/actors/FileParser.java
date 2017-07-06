package com.m800.actorsystems.logprocessor.actors;


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

import java.io.BufferedWriter;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
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
    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);
    protected Path fileToParse;
    protected ActorRef lineAggregator;
    protected long lineSequence;
    protected String line;

    private FileParser() {

        FileParser self = this;


        receive(ReceiveBuilder.
                match(Parse.class, message -> this.handle(message)).
                matchAny(o -> log.info("FileParser does not understand this message {}", o)).
                build()
        );

    }

    public void handleStartOfFile() {

        context().system().eventStream().publish(new StartOfFile(this.fileToParse));

    }

    public void handleEndOfFile() {

        context().system().eventStream().publish(new EndOfFile(this.fileToParse));

    }

    public void handleLine() {

        String line = this.line.toString();
        this.line = "";
        this.lineSequence += 1;
        context().system().eventStream().publish(
                new Line(this.fileToParse, this.lineSequence, line)
        );

    }

    public void scheduleNextByteRead() throws IOException {
        Files.readAllLines(fileToParse).forEach(content -> {
            this.line = content;
            handleLine();
        });

    }

    public void configureFrom(Parse message) {
        this.fileToParse = message.fileToParse;
        this.lineAggregator = getContext().actorOf(Props.create(Aggregator.class, this.fileToParse), "line-aggregator");
        this.lineSequence = 0;
        this.line = "";

    }


    private void handle(Parse message) {

        log.info("FileParser received Parse message: {}", message);

        this.configureFrom(message);
        this.tapFileEventStream(this.lineAggregator);

        try {

            this.handleStartOfFile();
            this.scheduleNextByteRead();
            this.handleEndOfFile();

        } catch (IOException e) {

            log.info("Message {} result : Parsing file {} caused error {}", message, this.fileToParse, e);

        }

    }

    private void tapFileEventStream(ActorRef subscriber) {

        context().system().eventStream().subscribe(subscriber, StartOfFile.class);
        context().system().eventStream().subscribe(subscriber, Line.class);
        context().system().eventStream().subscribe(subscriber, EndOfFile.class);

    }


}


