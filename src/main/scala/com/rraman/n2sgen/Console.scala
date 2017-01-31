package com.rraman.n2sgen

import com.rraman.n2sgen.common.Configuration
import com.rraman.n2sgen.procedure.CommandFunction._
import com.rraman.n2sgen.procedure.FileOperations
import org.eclipse.jetty.server.{Handler, Server}
import org.eclipse.jetty.server.handler.{DefaultHandler, HandlerList, ResourceHandler}

import scala.concurrent.Future
import scala.io.StdIn._
import scala.util.{Failure, Success, Try}

/**
  * Created by Rewati Raman (rewati.raman@gmail.com).
  */
object Console extends App {
  welcomeMessage

  def waitAndGetCommand: Unit = {
    prompt
    val command = Try(readLine .trim .toLowerCase).getOrElse("interrupt")
    Option(command) .map(_.split(' ')) .map(_.apply(0)) .map (Command(_))
    waitAndGetCommand
  }
  waitAndGetCommand
}


object Command {
  sealed case class Command(command: String,help: String)
  val INIT = Command("init","Initialize project. This will create basic structure.")
  val EXIT = Command("exit", "Will exit n2sgen shell.")
  val INTERRUPT = Command("interrupt","")
  val COMPILE = Command("compile","Compile and generate static html file.")
  val RSYNC = Command("rsync","Rsync to remote server. Or can be rsync to HTTP server serving content location.")
  val FTP = Command("ftp","Ftp to remote server. Or can be ftp to HTTP server serving content location.")
  val EMPTY = Command("","")
  val NEW_PAGE = Command("new","Will create new page.")
  val HELP = Command("help","Will print this help.")
  val CONFIGURE = Command("conf", "Will reload the configuration.")
  val SERVE = Command("serve", "Will launch a local server with the generate content.")
  protected val allCommands = List(INIT,EXIT,EMPTY,INTERRUPT,NEW_PAGE,HELP,COMPILE,RSYNC,FTP,CONFIGURE,SERVE)
  protected val validCommands = List(INIT,NEW_PAGE,HELP,COMPILE,RSYNC,FTP,CONFIGURE,SERVE)
  protected def space(c: Command) = " "*(18-c.command.length)
  protected def printHelp = validCommands foreach (x => println(s"  ${x.command}${space(x)}${x.help}"))
  def apply (command: String): Unit = allCommands.find(_.command == command) match {
    case Some(INIT) => initializeProject
    case Some(EXIT) => System.exit(0)
    case Some(INTERRUPT) => System.exit(1)
    case Some(HELP) => printHelp
    case Some(COMPILE) => compile
    case Some(EMPTY) =>
    case Some(NEW_PAGE) => ifInitialized (createNewPage)
    case Some(CONFIGURE) => Configuration.reload
    case Some(SERVE) => Try(Server.start) match {
      case Failure(x) => println("Was not able to start server."+x.getMessage )
      case _ =>
    }
    case _ => {
      println("Unknown command.")
      printHelp
    }
  }
}

object Server {
  var runing: Option[Server] = None
  import scala.concurrent.ExecutionContext.Implicits.global
  def start: Unit = if (!runing.isDefined) {
    print("Please enter the Port: ")
    val port = Try(readInt()).getOrElse(8080)
    val server = new Server(port)
    val resource_handler = new ResourceHandler()
    resource_handler.setDirectoriesListed(true)
    resource_handler.setWelcomeFiles( Array("index.htm"))
    resource_handler.setResourceBase(s"./${FileOperations.generatedCode}")
    val handlers = new HandlerList()
    handlers.setHandlers(Array( resource_handler, new DefaultHandler() ))
    server.setHandler(handlers)
    Future {
      server.start()
      server.join()
      runing = Option(server)
    }
    println("Server started press Enter.")
  } else {
    println(" Server is already running. Restarting...")
    runing.get.stop()
    runing = None
    start
  }
}
