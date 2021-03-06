package common.widgets

import hub.common.ParseException
import ifta.common.FExpOverflowException
import org.scalajs.dom
import org.scalajs.dom.EventTarget
import org.singlespaced.d3js.Selection
import preo.common.TypeCheckException

import scala.scalajs.js.{JavaScriptException, UndefOr}


//panel boxes are the abstract entities which contain each panel displayed on the website
abstract class Box[A](title: String, dependency: List[Box[_]]){
  type Block = Selection[dom.EventTarget]

  var wrap:Block = _

  /**
    * Creates a collapsable pannel
    * */
  protected def panelBox(parent:Block,
                         visible:Boolean,
                         headerStyle: List[(String,String)] = Nil,
                         buttons:List[(Either[String,String], (()=>Unit,String) )] = Nil) : Block = {
//    val percentage=100

    var expander: Block = parent
    wrap = parent.append("div").attr("class","panel-group")
      .append("div").attr("class","panel panel-default").attr("id",title)
    expander = wrap
      .append("div").attr("class", "panel-heading my-panel-heading")
      .append("h4")
        .attr("class", "panel-title")
        .attr("style",s"padding-right: ${28*buttons.size}pt;")
//        .append("table")
//        .attr("width", "100%")
//        .append("th")
//      .attr("width", s"$percentage%")
    for ((s,v)<-headerStyle)
      expander.style(s,v)
    expander = expander
      .append("a").attr("data-toggle", "collapse")
      .attr("href", "#collapse-1" + title.hashCode)
      .attr("aria-expanded", visible.toString)
    if(!visible)
      expander.attr("class","collapsed")
    expander
      .text(title)
    val res = wrap
      .append("div").attr("id","collapse-1"+title.hashCode)
      .attr("class",if (visible) "panel-collapse collapse in" else "panel-collapse collapse")
      .attr("style",if (visible) "" else "height: 0px;")
      .attr("aria-expanded",visible.toString)
      .append("div").attr("class","panel-body my-panel-body")

    // Buttons
    for ((name,(action,title)) <- buttons.reverse) {
//      .append("button").attr("class", "btn btn-default btn-sm")
//        .style("float","right")
//        .style("margin-top","-15pt")
//        .style("display","flex")

      val button = wrap
        .select("div")
        .append("button").attr("class", "btn btn-default btn-sm")
        .style("float","right")
        .style("margin-top","-15pt")
        .style("max-height","19pt")
        .style("margin-left","2pt")
        .style("display","flex")
      if (title.nonEmpty) button.attr("title",title)
      name match {
        case Left(str) => button.append("span").html(str)
        case Right(str) => button.append("span").attr("class", "glyphicon glyphicon-refresh")
      }
      button.on("click", {(e: EventTarget, a: Int, b:UndefOr[Int])=> { action() }})
    }

    res
  }

  def isVisible: Boolean = {
    val es = dom.document.getElementsByClassName("collapsed")
    var foundId = false
    for (i <- 0 until es.length) {
      // println(es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)
      //      println("### - "+es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)
      foundId = foundId || es.item(i).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value == title
    }

    //    println("### - "+es.length)
    //    println("### - "+es.item(0).localName)
    //    println("### - "+es.item(0).parentNode.localName)
    //    println("### - "+es.item(0).parentNode.parentNode.localName)
    //    println("### - "+es.item(0).parentNode.parentNode.parentNode.attributes.getNamedItem("id").value)

    //    val res = expander.attr("aria-expander") == "true"
    //    println("--- "+expander.html().render)
    //    println("--- "+expander.classed("collapsed"))
    //    println("--- "+expander.attr("aria-expander"))
    //    println("$$$ "+ (!foundId))
    !foundId
  }

  def get: A

  /**
    * Executed once at creation time, to append the content to the inside of this box
    * @param div Placeholder that will receive the "append" with the content of the box
    * @param visible is true when this box is initially visible (i.e., expanded).
    */
  def init(div: Block, visible: Boolean): Unit

  /**
    * Block of code that should read the dependencies and:
    *  - update its output value, and
    *  - produce side-effects (e.g., redraw a diagram)
    */
  def update(): Unit

}

object Box {
  type Block = Selection[dom.EventTarget]

  /**
    * Default function that catches exceptions and produces an error message based on the type of exception.
    * @param errorBox is the placeholder where the exception will be appended to.
    * @return the function to be placed at a catch point.
    */
  def checkExceptions(errorBox: OutputArea, source:String = ""): PartialFunction[Throwable,Unit] = {
    val by = if (source.nonEmpty) s" by $source" else source
    val f: PartialFunction[Throwable,Unit] = {
      // type error
      case e: TypeCheckException =>
        errorBox.error(/*Show(result)+ */ s"Type error$by: " + e.getMessage)
      //            instanceInfo.append("p").text("-")
      case e: preo.common.GenerationException =>
        errorBox.error(/*Show(result)+ */ s"Generation failed$by: " + e.getMessage)
      case e: hub.common.GenerationException =>
        errorBox.error(/*Show(result)+ */ s"Generation failed$by: " + e.getMessage)
      case e: preo.common.TimeoutException =>
        errorBox.error(s"Timeout$by: " + e.getMessage)
      case e: ifta.common.TimeoutException =>
        errorBox.error(s"Timeout$by: " + e.getMessage)
      case e: FExpOverflowException =>
        errorBox.error(s"Overflow$by: " + e.getMessage)
      case e: hub.common.ParseException =>
        errorBox.error(s"ParseException$by: " + e.getMessage)
      case e: hprog.common.ParserException =>
        errorBox.error(s"ParserException$by: " + e.getMessage)
      case e: hprog.frontend.SageSolver.SolvingException =>
        errorBox.error(s"Failed to solve expression$by:" + e.getMessage)
      case e: JavaScriptException => {
        //      val sw = new StringWriter
        //      e.printStackTrace(new PrintWriter(sw))
        //      errorBox.error(/*Show(result)+ */ "JavaScript error : " + e + " - " + e.getClass + "\n" + sw.toString )
        errorBox.error(/*Show(result)+ */ s"JavaScript error$by: " + e + " - " + e.getClass)
      }
      //            instanceInfo.append("p").text("-")
      case e: java.lang.AssertionError => errorBox.error(e.getMessage)
      case e: Throwable => errorBox.error(s"unknown error$by: " + e + " - " + e.getClass) //+"/n - "+e.getStackTrace.mkString("\n - "))
    }
    f
  }

}

