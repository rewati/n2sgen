package com.rraman.n2sgen.procedure

import com.rraman.n2sgen.procedure.FileOperations._
import java.io.{File, FileOutputStream, PrintWriter}

import com.rraman.n2sgen.common.{Configuration, Utils}
import org.pegdown.PegDownProcessor

import scala.io.Source
import scala.io.StdIn._
import scala.util.Try

/**
  * Created by Rewati Raman (rewati.raman@gmail.com).
  */
object CommandFunction {

  def projectName = Option(Configuration.projectName).getOrElse("Project name missing.")
  def prompt = print(s"${projectName + (if(!isInitialized)" not initialized" else "")}>>> ")
  def welcomeMessage = {
    println("Welcome n2sgen. Yet another static site generator. Built in scala.")
    println(s"Project name = ${projectName} ")
  }
  def isInitialized: Boolean = !confFile.isEmpty
  def ifInitialized(x: Unit) = if (isInitialized) x

  def initializeProject = {
    def initialize = {
      createProject
      println(s"Project ${projectName} is initialized.")
    }
    def alreadyInitialized = println(s"Error: ${projectName} is already initialized.")
    confFile .fold (initialize) ( _ => alreadyInitialized)
  }

  def createNewPage = {
    print("Title: ")
    Try(readLine).toOption map (createPage)
  }

  def createTagIndexPage(sourceMetas: List[MdSourceMeta], tag: String, template: String) = {
    val content = sourceMetas .map (x => Utils.createIndexPage(x.fileUrl,x.title,x.date)) mkString
    val html = template replace("###content###",content)
    createTagIndexFile(tag,html)
  }

  def compile = {
    val sourceFiles = mdSourceMetaOfPublishedFiles  .flatten   .sortBy(x => x.date)
    val tagList = sourceFiles .map(x => x.tags).flatten
    val tagMap = scala.collection.mutable.Map[String,List[MdSourceMeta]]()
    tagList.foreach(tagMap.put(_,List.empty))
    sourceFiles.foreach(x => x.tags.foreach(y => tagMap.put(y,tagMap.apply(y):::List(x))))
    val tags = tagMap .keySet
    tags map (x => createDirectory(s"${generatedCode}/${x}"))
    val tagContentMap = tags .map  (x => (x,tagMap.get(x).map(_.size).get,s"/${x}/index.htm")) .toSet
    val nav = tagContentMap .filter(x => Configuration.nav.contains(x._1)) .map (x => Utils.createNav(x._3,x._1)) .mkString
    val template = Utils.template.mkString.replace("###nav###",nav).replace("###SiteTitle###",projectName)
    (sourceFiles map createHtmlFileForMdSourceMeta ) foreach (y => y map (x => HtmlFileCreation(x._2,x._1,template)))
    tagMap   .foreach (x => createTagIndexPage(x._2,x._1,template))
  }

}

object FileOperations {

  val peg = new  PegDownProcessor
  val confFileName = "n2sgen.conf"
  val contentDirName = "content"
  val aboutMe = s"${contentDirName}/aboutMe.md"
  val templateDirName = "templates"
  val generatedCode = "generated"
  val blog = s"${generatedCode}/blog"
  val tag = s"${generatedCode}/tag"
  def confFilePath = s"${currentDirectory}/${confFileName}"
  def confFile = Option(new File(confFileName)).filter(_.exists)
  def currentDirectory = new File(".").getCanonicalPath
  def createN2SGenConfFile = new FileOutputStream(confFilePath, true).close()
  def createDirectory(dirName: String) = (new File(dirName)).mkdirs
  def createFile(fileName: String) = (new File(fileName)).createNewFile
  val newPageContent = Source.fromResource("template/md-page-template.txt").mkString

  def createPage(title: String) = {
    def replace(content: String,field: String,seq: String) = content.replace(s"###${field}###",seq)
    val exists = new File(s"${contentDirName}/${title}.md").exists
    if(!exists) {
      val date = Utils.now
      val url = s"blog/${title}"
      val content = Option(newPageContent) .map(replace(_,"date",date))
        .map(replace(_,"url",url)) .map(replace(_,"title",title))
      val pw = new PrintWriter(new File(s"${contentDirName}/${title.trim.replace(' ','-')}.md"))
      pw.write(content.getOrElse(newPageContent))
      pw.close
    } else {
      println(s"${title} title already exists.")
    }
  }

  case class HtmlFileCreation(mdSourceMeta: MdSourceMeta, content: String, template: String) {
    val dirUrl = s"${blog}/${mdSourceMeta.date}"
    val url = s"${dirUrl}/${mdSourceMeta.title.trim.replace(' ','-')}.hmt"
    createDirectory(dirUrl)
    val heading = s"<h1>${mdSourceMeta.title}</h1>"
    val html = template.replace("###content###",heading+content)
    (new PrintWriter(url) {
      write(html)
      close
    }.checkError)
  }

  def createTagIndexFile(tag: String, html: String) = {
    val url = s"${generatedCode}/${tag}/index.htm"
    (new PrintWriter(url) {
      write(html)
      close
    }.checkError)
  }

  def createProject = {
    createN2SGenConfFile
    val directoriesNeeded =
      List(contentDirName,templateDirName,generatedCode,blog,tag)
    val filesNeeded = List(aboutMe)
    directoriesNeeded foreach createDirectory
    filesNeeded foreach createFile
  }

  def listAllFilesInDirectory(dir: String): List[String] = {
    (Option(new File(dir)) filter (x => x.exists && x.isDirectory)
      map (_.listFiles .filter(_.isFile).map(_.getPath).toList) ).get
  }

  def mdSourceMetaOfPublishedFiles = (for {
    x <- listAllFilesInDirectory(contentDirName).filter(_.endsWith(".md"))
    y = Source.fromFile(x).getLines().take(1).mkString
    mds = MdSourceMeta(y,x)
  } yield mds).filterNot(_ == None).filter(_.get.published)

  def createHtmlFileForMdSourceMeta(mdMetaSourceData: MdSourceMeta): Option[(String,MdSourceMeta)] =
    Option(mdMetaSourceData).map(x => (Source.fromFile(x.fileUrl).getLines().drop(1).reduce(_+'\n'+_),x))
      .map(x => (peg.markdownToHtml(x._1),x._2))

}

case class MdSourceMeta(title: String,published: Boolean,date: String,fileUrl: String, tags: Set[String])
object MdSourceMeta {
  def apply(meta: String,fileUrl: String): Option[MdSourceMeta] = {
    val start = "<<<?"
    val end = "?>>>"
    val metaMap: Map[String,String] = (for {
      x <- Try(meta.substring(meta.indexOf(start) + start.length, meta.indexOf(end))).getOrElse("").split("""\|\|""")
      segment = x.split("=")
      k = Try(segment(0)).getOrElse("")
      v = Try(segment(1)).getOrElse("")
    } yield (k -> v)).toMap
    Try(MdSourceMeta(metaMap("title"),metaMap("published").equals("true"),metaMap("date"),fileUrl,Try(metaMap("tags").split(',').toSet).getOrElse(Set.empty))).toOption
  }
}
