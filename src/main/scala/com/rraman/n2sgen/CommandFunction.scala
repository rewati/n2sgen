package com.rraman.n2sgen

import com.rraman.n2sgen.FileOperations._

import scala.io.StdIn._
import scala.util.Try

/**
  * Created by Rewati Raman (rewati.raman@gmail.com).
  */
object CommandFunction {

  protected def projectName = Option(Configuration.projectName).filter(!_.isEmpty)
    .getOrElse(Try(currentDirectory.split('/').last).getOrElse("Project name missing."))
  def prompt = print(s"${projectName + (if(!isInitialized)" not initialized" else "")}>>> ")
  def welcomeMessage = {
    println("Welcome n2sgen. Yet another static site generator. Built in scala.")
    println(s"Project name = ${projectName} ")
  }

  protected def isInitialized: Boolean = !FileOperations.confFile.isEmpty
  def ifInitialized(x: Unit) = if (isInitialized) x else println(" Project need to be initialized")

  def initializeProject = {
    def initialize = {
      createProject
      println(s"Project ${projectName} is initialized.")
    }
    def alreadyInitialized = println(s"Error: ${projectName} is already initialized.")
    confFile .fold (initialize) ( _ => alreadyInitialized)
  }

  protected def createProject = {
    val directoriesNeeded =
      List(contentDirName,templateDirName,generatedCode,blog,tag,ccsDir)
    val filesNeeded = List(aboutMeName,css)
    directoriesNeeded foreach createDirectory
    filesNeeded foreach createFile
    createAndThenWriteToFile(confFileName,
      resourceDefaultTemplateN2SgenConf.content.replace("###project###",projectName))
    createAndThenWriteToFile(defaultTemplateFileName,resourceDefaultTemplate.content)
    createAndThenWriteToFile(defaultCssFileName,resourceDefaultTemplateCss.content)
    createAndThenWriteToFile(defaultDisqusFileName,resourceDefaultTemplateDisqus.content)
  }

  def createNewPage = {
    print("Title: ")
    Try(readLine).toOption map (createPage)
  }

  protected def createIndexHtmlPage(sourceMetas: List[MdSourceMeta], tag: Option[String], template: String) = {
    val articleListTemplate = resourceDefaultTemplateArticleList.content
    val content = sourceMetas .map (x =>
      createArticleItem(articleListTemplate,
        s"../blog/${x.date}/${x.title.trim.replace(' ','-')}.htm",x.title,x.date,x.tags)) mkString
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
    val navTagsFromConfig = Configuration.nav.map(_.toLowerCase)
    val nav = Option(tagContentMap .filter(x =>
      navTagsFromConfig.contains(x._1.toLowerCase)) .map (x => createNav(x._3,x._1)) .mkString)
      .fold("")(x => s"${homeLi}$x")
    val aboutMe = readFromFile(aboutMeName).mkString
    val template = siteTemplate.mkString.replace("###nav###",nav).replace("###SiteTitle###",projectName)
      .replace("###aboutme###",aboutMe)
    (sourceFiles map createHtmlFileForMdSourceMeta ) foreach (y => y map (x =>
      HtmlFileCreation(x._2,x._1,template.replace("###disqus###",DefaultTemplate(defaultDisqusFileName).content))))
    tagMap   .foreach (x => createIndexHtmlPage(x._2,Some(x._1),template.replace("###disqus###","")))
    createIndexHtmlPage(sourceFiles,None,template.replace("###disqus###",""))
    createAndThenWriteToFile(s"${generatedCode}/css/style.css",readFromFile(defaultCssFileName).mkString)
  }

}


