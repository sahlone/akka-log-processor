package com.m800.actorsystems.logprocessor.actors;

/**
 * This class is an Actor
 * that receives events to do with
 * file parsing and reports
 * on them in some cases.
 * <p>
 * If an instance receives an EndOfFile event
 * it prints to the console the number of words
 * in the file as specified in the EndOfFile event.
 **/

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

import java.nio.file.Path;

import com.m800.actorsystems.logprocessor.events.StartOfFile;
import com.m800.actorsystems.logprocessor.events.Line;
import com.m800.actorsystems.logprocessor.events.EndOfFile;

public class Aggregator extends AbstractActor {

    protected final LoggingAdapter log = Logging.getLogger(context().system(), this);
    public final Path fileToAggregate;
    public int noOfWords = 0;

    private String DELIMITER = " ";

    private Aggregator(Path fileToAggregate) {

        this.fileToAggregate = fileToAggregate;

        receive(ReceiveBuilder.

                match(StartOfFile.class, event -> {

                    if (this.fileToAggregate.equals(event.targetFile))
                        log.info("Aggregator received StartOfFile event: {}", event);

                }).
                match(Line.class, event -> {

                    if (this.fileToAggregate.equals(event.targetFile)) {
                        log.info("Aggregator received Line event: {}", event);
                        noOfWords += event.read.split(DELIMITER).length;
                    }

                }).
                match(EndOfFile.class, event -> this.handle(event)).
                matchAny(o -> log.info("Aggregator does not understand this event {}", o)).
                build()

        );

    }

    private void handle(EndOfFile event) {

        if (this.fileToAggregate.equals(event.targetFile)) {
            log.info("Aggregator received EndOfFile event: {}", event);

            this.outputLineCount(event);
        }

    }

    private void handle(StartOfFile event) {

        if (this.fileToAggregate.equals(event.targetFile)) {
            log.info("Aggregator received StartOfFile event: {}", event);
        }

    }

    private void handle(Line event) {

        if (this.fileToAggregate.equals(event.targetFile)) {
            log.info("Aggregator received Line event: {}", event);
            noOfWords++;
        }

    }

    private void outputLineCount(EndOfFile event) {
        log.info("File " + event.targetFile + " has " + noOfWords + " words");
        if(null!=System.console()){
            System.console().writer().println("File " + event.targetFile + " has " + noOfWords + " words");
            System.console().writer().flush();
        }
    }

}


