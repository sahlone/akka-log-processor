package com.m800.actorsystems.logprocessor.actors;

import akka.testkit.TestProbe;
import akka.testkit.javadsl.EventFilter;
import akka.testkit.javadsl.TestKit;
import com.m800.actorsystems.logprocessor.messages.Scan;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.AbstractActor;
import scala.concurrent.duration.Duration;

import java.io.FileNotFoundException;

public class FileScannerTest {


    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system.shutdown();
        system = null;
    }

    @Test
    public void testValidPath() {
        new TestKit(system) {{
            final Props props = Props.create(FileScanner.class);
            final ActorRef subject = system.actorOf(props);
            within(duration("3 seconds"), () -> {
                Scan scan = new Scan("../akka-log-processor-master/src/test/resources/testLogDir");
                subject.tell(scan, getRef());
                expectNoMsg();
                return null;
            });

        }};
    }

    @Test
    public void testInvalidPath() {

        new TestKit(system) {{
            final Props props = Props.create(FileScanner.class);
            final ActorRef subject = system.actorOf(props);

            subject.tell(new Scan("testLogDir"), getRef());

            final int result = new EventFilter(FileNotFoundException.class, system).intercept(() -> {
                return 1;
            });
            Assert.assertEquals(1, result);
            expectNoMsg();
        }};
    }

}