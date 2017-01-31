package com.rraman.n2sgen.common

import org.joda.time.DateTime

import scala.io.Source

/**
  * Created by Rewati Raman (rewati.raman@gmail.com).
  */
object Utils {

  def now: String = DateTime .now toString("yyyy/MM/dd")
  val navLi = """<li><a href="###href###">###name###</a></li>"""
  val article = """<article><span class="date">###date###</span><a href="###herf###">###title###</a></article>"""
  def createNav(herf: String,name: String) = navLi.replace("###href###",herf)
    .replace("###name###",Option(name).map(string => string.substring(0, 1).toLowerCase() + string.substring(1)).getOrElse(""))
  def createIndexPage(herf: String,title: String, date: String) =
    article .replace("###herf###",herf) .replace("###title###",title) .replace("###date###",date)
  def template = Source.fromFile("templates/template")
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
