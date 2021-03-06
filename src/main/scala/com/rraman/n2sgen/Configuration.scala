package com.rraman.n2sgen

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

import scala.io.Source
import scala.util.Try

/**
  * Created by Rewati Raman (rewati.raman@gmail.com).
  */
object Configuration {

  def currentDirectory = new File(".").getCanonicalPath

  case class InternalConfig(val config: Config) {
    def apply[T](property: String,default: T) = Try(default match {
      case x: Int => config.getInt(property)
      case x: Boolean => config.getBoolean(property)
      case _ => Option(System.getProperties.getProperty(property)) getOrElse config.getString(property)
    }).getOrElse(default).asInstanceOf[T]
  }

  var config = loadN2SgenConf
  def loadN2SgenConf = {
    val file = new File("./n2sgen.conf").getCanonicalFile
    Try(Source.fromFile(file).getLines().toList.foreach(x => Try{
      val prop = x.split('=').toList
      System.setProperty(prop(0),prop(1))
    }))
    new InternalConfig(ConfigFactory.load())
  }

  def reload = {config = loadN2SgenConf}

  def projectName = config ("project-name","")
  def nav = config ("nav","").split(',').toList
  def baseUrl = config("base-url","")
}
