package com.m800.actorsystems.logprocessor.system;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.m800.actorsystems.logprocessor.actors.FileScanner;
import com.m800.actorsystems.logprocessor.messages.Scan;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
  
  public static void main( String... args ) {

      Scanner scanner= new Scanner(System.in);
      System.out.println("Please enter valid  input directory");
      String logDir=scanner.nextLine();

      while(!Files.isDirectory(Paths.get(logDir))){
          System.out.println("Not valid input directory");

          System.out.println("Please enter  valid input directory");
          logDir=scanner.nextLine();

      }


      ActorSystem system = ActorSystem.create( "log-processor" );
      ActorRef fileScannerActor = system.actorOf( Props.create( FileScanner.class ), "file-scanner" );
      fileScannerActor.tell( new Scan(logDir ), null );

  }

}
