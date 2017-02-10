package com.rraman.n2sgen.common

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.io.Source
import scala.util.Try

/**
  * Created by Rewati Raman (rewati.raman@gmail.com).
  */
object Utils {

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
      .replace("###tags###",createTagLinks(tagList))
  def template = Source.fromFile("templates/template")
  def disqus: String = Option(Source.fromFile("templates/disqus").mkString ).fold("")(x => x)
  def readFromFile(url: String) = Source.fromFile(url)
  def readFromResource(url: String) = Source.fromResource(url).mkString
  def readMd(url: String) = {
    val a = Source.fromFile(url).getLines().drop(1).toList
    a match {
    case x if (x.size>1) => x.reduce(_+'\n'+_)
    case x if (x.size==1) => x.mkString
    case x if (x.size==0) => ""
  }}
}
