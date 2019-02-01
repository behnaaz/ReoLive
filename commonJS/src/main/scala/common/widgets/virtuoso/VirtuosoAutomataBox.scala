package common.widgets.virtuoso

import common.frontend.AutomataToJS
import common.widgets.{Box, GraphBox, OutputArea}
import hub.HubAutomata
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import preo.ast.CoreConnector
import preo.backend.{Automata, PortAutomata, ReoGraph}
import preo.frontend.Show

/**
  * Created by guille on 31/01/2019
  */


class VirtuosoAutomataBox(dependency: Box[CoreConnector], errorBox: OutputArea)
  extends Box[Automata]("Automaton of the instance", List(dependency)) {
  private var svg: Block = _
  private var automaton: Automata = _


  private val widthAutRatio = 7
  private val heightAutRatio = 3
  private val densityAut = 0.2 // nodes per 100x100 px

  override def get: Automata = automaton

  override def init(div: Block, visible: Boolean): Unit = {
    svg= GraphBox.appendSvg(panelBox(div, visible),"automata")
    dom.document.getElementById("Automaton of the instance").firstChild.firstChild.firstChild.asInstanceOf[html.Element]
      .onclick = {e: MouseEvent => if(!isVisible) drawAutomata() else deleteAutomaton()}

  }

  override def update(): Unit = if(isVisible) drawAutomata()


  private def drawAutomata(): Unit =
    try{
//      automaton = Automata.fromOneToOneSimple[HubAutomata](dependency.get)//
      automaton = Automata[HubAutomata](dependency.get).serialize.simplify
      val sizeAut = automaton.getStates.size
      //              println("########")
      //              println(aut)
      //              println("++++++++")
      val factorAut = Math.sqrt(sizeAut * 10000 / (densityAut * widthAutRatio * heightAutRatio))
      val width = (widthAutRatio * factorAut).toInt
      val height = (heightAutRatio * factorAut).toInt
      svg.attr("viewBox", s"00 00 $width $height")

      scalajs.js.eval(AutomataToJS(automaton))
    }
    catch Box.checkExceptions(errorBox)

  private def deleteAutomaton(): Unit = {
    svg.selectAll("g").html("")
  }
}