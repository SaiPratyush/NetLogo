// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.FunSuite

import java.net.URI

import org.nlogo.core.{ Model, View, WorldDimensions }
import org.nlogo.api.WorldDimensions3D
import org.nlogo.fileformat.{ defaultConverter, ModelConversion, NLogoFormat }
import org.nlogo.api.{ ConfigurableModelLoader, Version }
import scala.util.Try

class OpenModelTests extends FunSuite {
  val testURI = new URI("file:///foo.test")

  trait OpenTest {
    val uri: URI = testURI
    def modelChanges: Model => Model = identity
    def currentVersion = "NetLogo 6.0"
    def autoconverter: ModelConversion = defaultConverter

    def format = new NLogoFormat
    object VersionInfo extends Version {
      override def is3D = currentVersion.contains("3D")
      override def knownVersion(v: String) = v == currentVersion || super.knownVersion(v)
    }
    lazy val loader = new ConfigurableModelLoader().addFormat[Array[String], NLogoFormat](format)
  }

  test("serializes various version in the model") { new OpenTest {
    assert(format.version.serialize(new Model) === Array[String]("NetLogo 6.0"))
    assert(format.version.serialize(new Model(version = "NetLogo 3D 6.0")) ===
      Array[String]("NetLogo 3D 6.0"))
  } }

  test("serializes various dimensions in the model") { new OpenTest {
    assert((format.interfaceComponent.serialize(new Model)) ===
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "0", "1", "1", "ticks", "30.0", ""))
    assert(format.interfaceComponent.serialize(new Model(widgets =
      List(View(dimensions = new WorldDimensions(0,0,0,0,12.0,true,true))))) ===
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "0", "1", "1", "ticks", "30.0", ""))
    assert(format.interfaceComponent.serialize(new Model(widgets =
      List(View(dimensions = new WorldDimensions3D(0,0,0,0,0,0,12.0,true,true))))) ===
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "0", "0", "1", "1", "1", "ticks", "30.0", ""))
  } }

  test("deserializes various dimensions for models") { new OpenTest {
    val tryModel: Try[Model] = (format.interfaceComponent.deserialize(
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "1", "1", "1", "ticks", "30.0", ""))(new Model))
    val tryModel3d: Try[Model] = (format.interfaceComponent.deserialize(
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "0", "0", "1", "1", "1", "ticks", "30.0", ""))(new Model))

    /* Determines if view is correctly configured */
    assert(tryModel.get.widgets(0) === View())
    assert(tryModel.get.widgets(0) ===
      View(dimensions = new WorldDimensions(0,0,0,0,12.0,true,true)))
    assert(tryModel3d.get.widgets(0) ===
      View(dimensions = new WorldDimensions3D(0,0,0,0,0,0,12.0,true,true)))
  } }

  test("parses sections from provided string") { new OpenTest {
    val sectionString =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val sectionStringWith3D =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 3D 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val section3dString =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 3D 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val section3dStringWith2D =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val model: Try[Model] = format.load(sectionString,Seq())
    val model3d: Try[Model] = format.load(section3dString,Seq())
    val modelWith3dHeader: Try[Model] = format.load(sectionStringWith3D,Seq())
    val model3dWith2dHeader: Try[Model] = format.load(section3dStringWith2D,Seq())

    /* Model's dimensions after loading */
    assert(model3d.get.widgets(0) ===
      View(dimensions = new WorldDimensions3D(-16,16,-16,16,-16,16,12.0,true,true)))
    assert(model.get.widgets(0) ===
      View(dimensions = new WorldDimensions(-16,16,-16,16,12.0,true,true)))
    assert(model3dWith2dHeader.get.widgets(0) ===
      View(dimensions = new WorldDimensions3D(-16,16,-16,16,-16,16,12.0,true,true)))
    assert(modelWith3dHeader.get.widgets(0) ===
      View(dimensions = new WorldDimensions(-16,16,-16,16,12.0,true,true)))

    /* Model's version after loading */
    assert(model.get.version == "NetLogo 6.0.4")
    assert(modelWith3dHeader.get.version == "NetLogo 3D 6.0.4")
    assert(model3d.get.version == "NetLogo 3D 6.0.4")
    assert(model3dWith2dHeader.get.version == "NetLogo 6.0.4")
  } }

  test("determines valid path extensions") { new OpenTest {
    val acceptedExtNLogo: Option[org.nlogo.api.FormatterPair[_, _]] = loader.uriFormat(new java.net.URI("test.nlogo"))
    val acceptedExtNLogo3d: Option[org.nlogo.api.FormatterPair[_, _]] = loader.uriFormat(new java.net.URI("test.nlogo3d"))
    val acceptedExtTest: Option[org.nlogo.api.FormatterPair[_, _]] = loader.uriFormat(new java.net.URI("test.test"))

    /* Compares the format name for equality */
    assert(acceptedExtNLogo.get.name == format.name)
    assert(acceptedExtNLogo3d.get.name == format.name)
    assert(acceptedExtTest.get.name == format.name)
  } }
}
