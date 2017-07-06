package com.m800.actorsystems.logprocessor.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.EventFilter;
import akka.testkit.javadsl.TestKit;
import com.m800.actorsystems.logprocessor.messages.Parse;
import com.m800.actorsystems.logprocessor.messages.Scan;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileParserTest {


    static ActorSystem system;
    static String logPath = "../akka-log-processor-master/src/test/resources/testLogDir/log.txt";

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system.shutdown();
        system= null;
    }

    @Test
    public void testValidRecieve()
    {
        Parse parse = new Parse(Paths.get(logPath));
        final Props props = Props.create(FileParser.class);
        final TestActorRef<FileParser> ref = TestActorRef.create(system, props, "testValidRecieve");
        final FileParser actor = ref.underlyingActor();
        ref.tell(parse, ref);
        Assert.assertEquals(parse.fileToParse.toAbsolutePath(), actor.fileToParse.toAbsolutePath());

    }

    @Test
    public void testNoOfLinestoParse()
    {
        Parse parse = new Parse(Paths.get(logPath));
        final Props props = Props.create(FileParser.class);
        final TestActorRef<FileParser> ref = TestActorRef.create(system, props, "testNoOfLinestoParse");
        final FileParser actor = ref.underlyingActor();
        ref.tell(parse, ref);
        new TestKit(system) {{
            within(duration("5 seconds"), () -> {
                Assert.assertEquals(2, actor.lineSequence);

                return null;
            });

        }};

    }


}