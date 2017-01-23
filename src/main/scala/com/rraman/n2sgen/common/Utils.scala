package com.rraman.n2sgen.common

import org.joda.time.DateTime

import scala.io.Source

/**
  * Created by Rewati Raman (rewati.raman@hart.com).
  */
object Utils {

  def now: String = DateTime .now toString("yyyy/MM/dd")
  val navLi = """<li><a href="###href###">###name###</a></li>"""
  val article = """<article><span class="date">###date###</span><a href="###herf###">###title###</a></article>"""
  def createNav(herf: String,name: String) = navLi.replace("###href###",herf).replace("###name###",name)
  def createIndexPage(herf: String,title: String, date: String) =
    article .replace("###herf###",herf) .replace("###title###",title) .replace("###date###",date)
  def template = Source.fromFile("templates/template")
}
