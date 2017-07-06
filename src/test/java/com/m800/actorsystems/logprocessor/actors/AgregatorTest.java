package com.m800.actorsystems.logprocessor.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.EventFilter;
import akka.testkit.javadsl.TestKit;
import com.m800.actorsystems.logprocessor.events.Line;
import com.m800.actorsystems.logprocessor.events.StartOfFile;
import com.m800.actorsystems.logprocessor.messages.Parse;
import com.m800.actorsystems.logprocessor.messages.Scan;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AgregatorTest {


    static ActorSystem system;
    static String logPath = "../akka-log-processor-master/src/test/resources/testLogDir/log.txt";
    static Path path = Paths.get(logPath);

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
    public void testValidRecieve() {
        StartOfFile startOfFile = new StartOfFile(Paths.get(logPath));
        final Props props = Props.create(Aggregator.class, path);
        final TestActorRef<Aggregator> ref = TestActorRef.create(system, props, "testValidRecieve");
        final Aggregator actor = ref.underlyingActor();
        ref.tell(startOfFile, ref);
        Assert.assertEquals(startOfFile.targetFile.toAbsolutePath(), actor.fileToAggregate.toAbsolutePath());

    }

    @Test
    public void testWordsIncrement() {
        Line line = new Line(Paths.get(logPath), 1, "two words");
        final Props props = Props.create(Aggregator.class, path);
        final TestActorRef<Aggregator> ref = TestActorRef.create(system, props, "testWordsIncrement");
        final Aggregator actor = ref.underlyingActor();
        ref.tell(line, ref);
        Assert.assertEquals(2, actor.noOfWords);

    }

    @Test
    public void testTotalNoOWords() throws IOException {
        final Props props = Props.create(Aggregator.class, path);
        final TestActorRef<Aggregator> ref = TestActorRef.create(system, props, "testTotalNoOWords");
        final Aggregator actor = ref.underlyingActor();
        Files.readAllLines(path).forEach(content -> {
            Line line = new Line(Paths.get(logPath), 1, content);
            ref.tell(line, ref);

        });

        Assert.assertEquals(14, actor.noOfWords);
    }


    @Test
    public void testOutput() {

        Line line = new Line(Paths.get(logPath), 1, "two words");
        final Props props = Props.create(Aggregator.class, path);
        final TestActorRef<Aggregator> ref = TestActorRef.create(system, props, "testOutput");
        final Aggregator actor = ref.underlyingActor();
        ref.tell(line, ref);

        final int result = new EventFilter(IOException.class, system).intercept(() -> {
            return 1;
        });
        Assert.assertEquals(1, result);
    }

}