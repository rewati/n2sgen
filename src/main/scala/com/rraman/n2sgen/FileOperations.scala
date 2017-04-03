package com.rraman.n2sgen

import java.io.{File, PrintWriter}

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.pegdown.{Extensions, PegDownProcessor}

import scala.io.Source
import scala.util.Try

/**
  * Created by Rewati Raman (rewati.raman@hart.com).
  */
object FileOperations {

  /**
    * Filenames and locations
    */
  val peg = new  PegDownProcessor(Extensions.FENCED_CODE_BLOCKS)
  case class DefaultResource(url: String) {
    lazy val content = Try(readFromResource (url)).getOrElse("")
  }
  case class DefaultTemplate(url: String) {
    lazy val content = Try(readFromFile (url)).getOrElse("")
  }
  lazy val resourceTemplateDir = "template"
  lazy val resourceDefaultTemplate = DefaultResource(s"$resourceTemplateDir/siteTemplate.txt")
  lazy val resourceDefaultTemplateCss = DefaultResource(s"$resourceTemplateDir/site.css")
  lazy val resourceDefaultTemplateNewPage = DefaultResource(s"$resourceTemplateDir/md-page-template.txt")
  lazy val resourceDefaultTemplateArticleList = DefaultResource(s"$resourceTemplateDir/articleListTemplate.txt")
  lazy val resourceDefaultTemplateN2SgenConf = DefaultResource(s"$resourceTemplateDir/n2sgen.conf")
  lazy val resourceDefaultTemplateDisqus = DefaultResource(s"$resourceTemplateDir/disqus.txt")

  lazy val newPageContent = resourceDefaultTemplateNewPage.content

  lazy val confFileName = "n2sgen.conf"
  def confFile = Option(new File(confFileName)).filter(_.exists)

  lazy val contentDirName = "content"
  lazy val aboutMeName = s"${contentDirName}/aboutMe.md"
  lazy val templateDirName = "templates"
  lazy val defaultTemplateFileName = s"${templateDirName}/template"
  lazy val defaultCssFileName = s"${templateDirName}/css"
  lazy val defaultDisqusFileName = s"${templateDirName}/disqus"

  lazy val generatedCode = "generated"
  lazy val blog = s"$generatedCode/blog"
  lazy val tag = s"$generatedCode/tag"
  lazy val ccsDir = s"$generatedCode/css"
  lazy val css = s"$generatedCode/css/style.css"

  val readFromResource = (x: String) => Source.fromResource(x).mkString
  val readFromFile = (x: String) => Source.fromFile(x).mkString
  def readMd(url: String) = Try(Source.fromFile(url).getLines().drop(1).toList
    .reduceLeftOption(_+'\n'+_).mkString).getOrElse("")
  def currentDirectory = new File(".").getCanonicalPath
  def createAndThenWriteToFile(url: String, content: String) = (new PrintWriter(url) {
    write(content)
    close
  }.checkError)
  def createDirectory(dirName: String) = (new File(dirName)).mkdirs
  def createFile(fileName: String) = (new File(fileName)).createNewFile
  def listAllFilesInDirectory(dir: String): List[String] = {
    (Option(new File(dir)) filter (x => x.exists && x.isDirectory)
      map (_.listFiles .filter(_.isFile).map(_.getPath).toList) ).get
  }

  def createPage(title: String) = {
    def replace(content: String,field: String,seq: String) = content.replace(s"###${field}###",seq)
    def exists = new File(s"$contentDirName/$title.md").exists
    def create = {
      val date = now
      val url = s"blog/$title"
      val content = Option(newPageContent) .map(replace(_,"date",date))
        .map(replace(_,"url",url)) .map(replace(_,"title",title))
      val pw = new PrintWriter(new File(s"${contentDirName}/${title.trim.replace(' ','-').toLowerCase}.md"))
      pw.write(content.getOrElse(newPageContent))
      pw.close
    }
    if(!exists) create else println(s"${title} title already exists.")
  }

  def createTagIndexFile(tag: Option[String], html: String) = {
    val url = tag .fold (s"${generatedCode}/index.htm")(x => s"${generatedCode}/${x}/index.htm")
    createAndThenWriteToFile(url,html)
  }

  def mdSourceMetaOfPublishedFiles = (for {
    x <- listAllFilesInDirectory(contentDirName).filter(_.endsWith(".md"))
    y = Source.fromFile(x).getLines().take(1).mkString
    mds = MdSourceMeta(y,x)
  } yield mds).filterNot(_ == None).filter(_.get.published)

  def createHtmlFileForMdSourceMeta(mdMetaSourceData: MdSourceMeta): Option[(String,MdSourceMeta)] =
    Option(mdMetaSourceData).map(x => (readMd(x.fileUrl),x))
      .map(x => (peg.markdownToHtml(x._1),x._2))

  case class HtmlFileCreation(mdSourceMeta: MdSourceMeta, content: String, template: String) {
    val dirUrl = s"${blog}/${mdSourceMeta.date}"
    val url = s"${dirUrl}/${mdSourceMeta.title.trim.replace(' ','-').toLowerCase}.htm"
    createDirectory(dirUrl)
    val heading = s"<h3>${mdSourceMeta.title}</h3>"
    val tags = createTagLinks (mdSourceMeta.tags)
    val article =
      """<div class="row"><div class="ten columns">###heading###<div class="date-publish">###date###</div><div class="tags">Tags: ###tags###</div></div><div class="twelve columns">###article###</div></div>""".stripMargin
        .replace("###heading###",heading).replace("###article###",content).replace("###date###",articleDate(mdSourceMeta.date)).replace("###tags###",tags)
    val html = template.replace("###content###",article).replace("###pageurl###",url).replace("###pageId###",url.replace('/','-'))
    createAndThenWriteToFile(url,html)
  }

  def now: String = DateTime .now toString("yyyy/MM/dd")
  val dateFmt = DateTimeFormat.forPattern("yyyy/MM/dd")
  def articleDate(date: String) = Try(dateFmt.parseDateTime(date).toString("dd MMM yyyy")).getOrElse(date)
  val navLi = """<li class="navbar-item"><a class="navbar-link" href="###href###">###name###</a></li>"""
  def createTagLinks(tags: Set[String]) = tags . map(x => """<a class="tag-link" href="/###href###">###name###</a>"""
    .replace("###href###",x).replace("###name###",x)).reduceLeftOption(_+_).getOrElse("")
  val homeLi = createNav("/","home")
  def createNav(herf: String,name: String) = navLi.replace("###href###",herf)
    .replace("###name###",Option(name).map(string => string.substring(0, 1).toLowerCase() + string.substring(1)).getOrElse(""))
  def createArticleItem(template: String, herf: String, title: String, date: String, tagList: Set[String]) =
    template .replace("###herf###",herf) .replace("###title###",title) .replace("###date###",articleDate(date))
      .replace("###tags###",createTagLinks(tagList)).replace("###baseurl###",Configuration.baseUrl)
  def siteTemplate = Source.fromFile("templates/template")
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