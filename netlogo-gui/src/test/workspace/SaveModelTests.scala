// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.URI

import org.nlogo.core.Model
import org.nlogo.fileformat.NLogoFormat
import org.nlogo.api.{ ConfigurableModelLoader, ModelType, Version }

import org.scalatest.FunSuite

import scala.util.{ Success, Try }

class SaveModelTests extends FunSuite {
  val model = Model()

  def testSave(withModel: Model => Model = identity,
    controller: MockController = new MockController,
    modelType: ModelType = ModelType.New,
    error: Option[Exception] = None,
    existingPath: Option[String] = None,
    forcePathSelect: Boolean = false)(
    assertion: (Option[Try[URI]], MockController) => Unit): Unit = {
      val model = withModel(Model())
      val format = new NLogoFormat
      val loader = new ConfigurableModelLoader().addFormat[Array[String], NLogoFormat](format)
      val modelTracker = new ModelTracker {
        override def getModelType = modelType
        override def getModelPath: String = existingPath.orNull
        override def compiler = null
        override def getExtensionManager() = null
      }
      val res =
        if (forcePathSelect)
          SaveModelAs(model, loader, controller, modelTracker, Version).map(_.apply())
        else
          SaveModel(model, loader, controller, modelTracker, Version).map(_.apply())
      assertion(res, controller)
  }

  test("if workspace fileMode is Normal, saves at the workspace model path") {
    testSave(modelType = ModelType.Normal, existingPath = Some("/tmp/save.test")) { (result, _) =>
      assert(Some(Try(new URI("file:///tmp/save.test"))) == result)
    }
  }

  test("if workspace fileMode is Normal, but the user is forced to select a path, use the chosen path") {
    testSave(modelType = ModelType.Normal,
      existingPath = Some("/existing/save.test"),
      forcePathSelect = true) { (result, _) =>
      assert(Some(Try(new URI("file:///tmp/nlogo.test"))) == result)
    }
  }

  test("if ModelType is New, prompts for path and saves as specified path") {
    testSave() { (result, _) =>
      assert(Some(Try(new URI("file:///tmp/nlogo.test"))) == result)
    }
  }

  test("if user doesn't select a path when prompted, returns None") {
    testSave(controller = new MockController(chosenFilePaths = Seq())) { (result, _) =>
      assert(result == None)
    }
  }

  test("if user elects not to save when warned of differing version, returns None") {
    testSave(withModel = _.copy(version = "NetLogo 2.0"), controller = new MockController()) { (result, _) =>
      assert(result == None)
    }
  }

  test("if workspace fileMode is LIBRARY, prompts for path and saves as specified path") {
    testSave(modelType = ModelType.Library) { (result, _) =>
      assert(Some(Try(new URI("file:///tmp/nlogo.test"))) == result)
    }
  }

  test("if the original version of the model doesn't match the current version of NetLogo, checks before saving") {
    testSave(withModel = _.copy(version = "NetLogo 100"),
      controller = new MockController(continueSavingModel = true)) { (_, controller) =>
        assert(controller.warnedOfDiffereringVersion == "NetLogo 100")
      }
  }

  test("if user tries to save the file in a format not understood by NetLogo, reprompts") {
    val filePaths = Seq(new URI("file:///tmp/valid.test"), new URI("file:///tmp/invalid.invalid"))
    testSave(controller = new MockController(chosenFilePaths = filePaths)) { (result, controller) =>
      assert(result == Some(Try(new URI("file:///tmp/valid.test"))))
      assert(controller.warnedOfInvalidFileFormat == "")
    }
  }

  test("if the file saves to a different format, succeed") {
    val error = new Exception("couldn't save file")
    testSave(error = Some(error)) { (result, controller) =>
      assert(result.get == Success(new java.net.URI("file:///tmp/nlogo.test")))
    }
  }

  class MockController(
    var chosenFilePaths: Seq[URI] = Seq(new URI("file:///tmp/nlogo.test")),
    continueSavingModel: Boolean = false)
    extends SaveModel.Controller {
      var warnedOfDiffereringVersion = ""
      var warnedOfInvalidFileFormat = ""

      def chooseFilePath(modelType: ModelType): Option[URI] = {
        val r = chosenFilePaths.headOption
        if (chosenFilePaths.nonEmpty)
          chosenFilePaths = chosenFilePaths.tail
        r
      }

      def warnInvalidFileFormat(extension: String): Unit = {
        warnedOfInvalidFileFormat = extension
      }

      def shouldSaveModelOfDifferingVersion(version: String): Boolean = {
        warnedOfDiffereringVersion = version
        continueSavingModel
      }
    }

}
