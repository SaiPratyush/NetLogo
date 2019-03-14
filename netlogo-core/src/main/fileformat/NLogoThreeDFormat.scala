// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.ModelFormat
import org.nlogo.core.model.WidgetReader

class NLogoThreeDFormat
  extends ModelFormat[Array[String], NLogoThreeDFormat]
  with AbstractNLogoFormat[NLogoThreeDFormat] {
    def name: String = "nlogo3d"
    override def widgetReaders =
      Map[String, WidgetReader]()
    override def widgetThreeDReaders =
      Map[String, WidgetReader]("GRAPHICS-WINDOW" -> ThreeDViewReader)
  }
