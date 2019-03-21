package common.frontend

import hub.HubAutomata
import ifta.analyse.Simplify
import ifta.{CTrue, ClockCons}
import ifta.backend.{IftaAutomata, Show}
import preo.backend._


//todo: add rectangle colision colision
object AutomataToJS {

  def apply[A<:Automata](aut: A, ext:Map[Int,Int], boxName:String, portNames:Boolean=false): String =
    generateJS(getNodes(aut), getLinks(aut,boxName), boxName)

  def toJs[A<:Automata](aut: A, boxName:String, portNames:Boolean=false): String = aut match {
    case a:IftaAutomata => generateJS(getIftaNodes(a) , getLinks(aut,boxName,portNames), boxName)
    case _              => generateJS(getNodes(aut)   , getLinks(aut,boxName,portNames), boxName)
  }




  /*todo: refactor in different methods or classes to avoid booolean virtuoso, or pass automata for
   * better customization for each type of automata, reo, ifta, hub*/
  private def generateJS(nodes: String, edges: String, name:String): String = {
    // println(nodes)
    // println(edges)
    s"""
        buildAut();
        function buildAut() {
          var svgAut = d3.select("#${name}");
          var vboxAut = svgAut.attr('viewBox').split(" ")
          var widthAut = vboxAut[2]; //svgAut.attr("widthAut");
          var heightAut = vboxAut[3];  //svgAut.attr("heightAut");
          var radiusAut = 4.75;

          var graphAut = {"nodesautomata": ${nodes}, "linksautomata": ${edges}};

          var simulationAut = d3.forceSimulation(graphAut.nodesautomata)
            .force('charge', d3.forceManyBody().strength(-200))
            .force('center', d3.forceCenter(widthAut / 2, heightAut / 2))
            .force('y', d3.forceY().y(function(d) { return 0;}))
            .force('x', d3.forceX().x(function(d) {
//              if (d.group == "1"){
//                return widthAut;
//              }
//              if (d.group == "0"){
//                return -widthAut;
//              }
              return 0;
            }))
            .force('collision', d3.forceCollide().radius(function(d) {
                         return d.radius}))
            .force("link", d3.forceLink().links(graphAut.linksautomata).id(function(d) { return d.id; }).distance(5))
            //.force("forcepos", forcepos)
            .on('tick', tickedAut);

          initAut(graphAut.nodesautomata, graphAut.linksautomata);

          function initAut(nodesAut, linksAut){
              //add nodes (nodes "circle" with group 0..2)
              var node = d3.select(".nodes${name}")
                  .selectAll("circle")
                  .data(nodesAut);
//                  .enter().append("g")
              var nd = node.enter().append("circle")
//              var nd = node.append("circle")
                  .merge(node)
                  .attr("r", function(d){
                    if(d.group == 0 || d.group == 1){
                      return radiusAut + 0.75;
                    }
                    else{
                      return 0;
                    }
                  })
                  .attr("id", function (d) {return d.id;})
                  .call(d3.drag()
                  .on("start", dragstartedAut)
                  .on("drag", draggedAut)
                  .on("end", dragendedAut))
                  .style("stroke-opacity" , "1")
                  .style("stroke-width", function(d){
                    if(d.group == 0 || d.group == 1) {
                      return "1px";
                    } else {
                      return "0px";
                    }
                  })
                  .style("stroke", "black")
                  .style("fill", function(d){
                    if(d.group == 0) {
                      return "white";
                    }
                    else if (d.group==1) {
                      return "black";
                    }
                    else{
                      return "green";
                    }
                });

                // add invariants to ifta automata nodes
//                var nodelabel = node.append("text")
//                  .style("font-size","6px")
//                  .style("fill","#00B248")
//                  .attr('dy', 9)
//                  .attr('dx', 6)
//                  .on("mouseenter", function(d) {
//                    d3.select(this).style("font-size","14px");
//                  })
//                  .on("mouseleave", function(d) {
//                    d3.select(this).style("font-size","6px");
//                  })
//                  .text(function(d) {
//                    if (d.group == 0 || d.group == 1)
//                      return d.inv;
//                    else
//                      return "";
//                    });

              node.exit().remove();


               //add links
               var link = d3.select(".links${name}")
                  .selectAll("polyline")
                  .data(linksAut);
               link.enter().append("polyline")
                  .style("stroke", "black")
                  .merge(link)
                  .attr('marker-end', function(d){
                    return 'url(#' + d.end + ')'
                  })
                  .attr('marker-start', function(d){
                    return 'url(#' + d.start + ')'
                  })
                  .style('stroke-dasharray',"1, 0");


              link.append("title")
                  .text(function (d) {return d.type;});
              link.exit().remove();

              //add labels to graphAut
              var edgepaths = svgAut.select(".paths${name}")
                  .selectAll(".edgepath")
                  .data(linksAut);
              edgepaths.enter()
                  .append('path')
                  .attr('class', 'edgepath')
                  .attr('fill-opacity', 0)
                  .attr('stroke-opacity', 0)
                  .attr('id', function (d, i) {return 'edge${name}path' + i})
                  .style("pointer-events", "none");
              edgepaths.exit().remove();

              var edgelabels = svgAut.select(".labels${name}")       // for all labels in data
                  .selectAll(".edgelabel")
                  .data(linksAut);
              edgelabels.enter()
                  .append('text')
//                  .style("pointer-events", "none")
                  .attr('class', 'edgelabel')
                  .attr('id', function (d, i) {return 'edge${name}label' + i})
                  .attr('font-size', 10)
                  .attr('fill', 'black');
              edgelabels.exit().remove();

              d3.select(".labels${name}")
                  .selectAll("textPath").remove();

              if (${name == "automata"}) {
              var textpath = d3.select(".labels${name}")
                  .selectAll(".edgelabel")
                  .append('textPath')
                  .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                  .style("text-anchor", "middle")
                  .style("fill","#3B01E9")
                  //.style("pointer-events", "none")
                  .attr("startOffset", "50%")
                  .on("mouseenter", function(d) {
                    //console.log("me: "+d.type);
                    d3.select(this).style("font-size","14px");
                    var ports = d.type.split("~");
                    ports.shift();
                    ports.forEach(function(el) {
                      var p = document.getElementById("gr_"+el);
                      //console.log("port "+el);
                      if (p!=null) {
                        p.style.backgroundColor = p.style.fill;
                        p.style.fill = "#00aaff";
                      }
                    });
                  })

                  .on("mouseleave", function(d) {
                    d3.select(this).style("font-size", "10px");
                    var ports = d.type.split("~");
                    ports.shift();
                    ports.forEach(function(el) {
                      var p = document.getElementById("gr_"+el);
                      //console.log("port "+el);
                      if (p!=null) {
                        p.style.fill = p.style.backgroundColor;
                      }
                    });
                  })

                  .text(function (d) {
                    return d.type.split("~")[0];
                  })
                  ;
            } else if (${name == "virtuosoAutomata"}) {
              var textpath = d3.select(".labels${name}")
                    .selectAll(".edgelabel")
                    .append('textPath')
                    .call(d3.drag()
                    .on("start", dragstartedAut)
                    .on("drag", draggedAut)
                    .on("end", dragendedAut))
                    .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                    .style("text-anchor", "middle")
                    // .style("pointer-events", "none")
                    .attr("startOffset", "50%")
                    .on("mouseenter", function(d) {
                      d3.select(this).style("font-size","14px");})
                    .on("mouseleave", function(d) {
                      d3.select(this).style("font-size", "10px");});
                  textpath.append("tspan")
                    .attr("class", "guards")
                    .style("fill","#008900")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      return (g != "" ) ?  "〈" + g + "〉" : "";
                    });
                  textpath.append("tspan")
                    .attr("class", "acts")
                    .style("fill","#3B01E9")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      var a = d.type.split("~")[1] ;
                      var acts = (a !== undefined) ? a : ""
                      return (g != "" && acts!= "")? ", " + acts : acts;
                    }) ;
                  textpath.append("tspan")
                    .attr("class", "updates")
                    .style("fill","#0F024F")
                    .text(function (d) {
                      var u = d.type.split("~")[2] ;
//                      return (typeof u != 'undefined') ? (", " + u) : " ";
                      return (u != "" && u!== undefined) ? ", " + u : "";
                    });
            } else { //iftaAutomata
                var textpath = d3.select(".labels${name}")
                    .selectAll(".edgelabel")
                    .append('textPath')
                    .call(d3.drag()
                    .on("start", dragstartedAut)
                    .on("drag", draggedAut)
                    .on("end", dragendedAut))
                    .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                    .style("text-anchor", "middle")
                    // .style("pointer-events", "none")
                    .style("font-size", "10px")
                    .attr("startOffset", "50%")
                    .on("mouseenter", function(d) {
                      d3.select(this).style("font-size","14px");})
                    .on("mouseleave", function(d) {
                      d3.select(this).style("font-size", "10px");});
//                  textpath.append("tspan")
//                    .attr("class", "cc")
//                    .style("fill","#00B248")
//                    .text(function (d) {
//                      var g = d.type.split("~")[0] ;
//                      return (g != "" ) ?  g : "";
//                    });
                  textpath.append("tspan")
                    .attr("class", "iftaActs")
                    .style("fill","#3B01E9")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      var a = d.type.split("~")[1] ;
                      var acts = (a !== undefined) ? ((a != "") ?a: "τ") : ""
                      return acts;
                    }) ;
//                  textpath.append("tspan")
//                    .attr("class", "fexp")
//                    .style("fill","#0F024F")
//                    .text(function (d) {
//                      var u = d.type.split("~")[2] ;
//                      return (u != "" && u!== undefined) ? ", " + u : "";
//                    });
//                  textpath.append("tspan")
//                    .attr("class", "cresets")
//                    .style("fill","#4C2EAA")
//                    .text(function (d) {
//                      var r = d.type.split("~")[3] ;
////                      return (typeof r != 'undefined') ? (", " + r) : " ";
//                      return (r != "" && r!== undefined) ? ", " + r : "";
//                    });

                  var belowTextpath = d3.select(".labels${name}")
                    .selectAll(".edgelabel")
                    .append('textPath')
                    .call(d3.drag()
                    .on("start", dragstartedAut)
                    .on("drag", draggedAut)
                    .on("end", dragendedAut))
                    .attr('xlink:href', function (d, i) {return '#edge${name}path' + i})
                    .style("text-anchor", "middle")
                      .style("font-size", "6px")
                    // .style("pointer-events", "none")
                    .attr("startOffset", "50%")
                    .on("mouseenter", function(d) {
                      d3.select(this).style("font-size","14px");})
                    .on("mouseleave", function(d) {
                      d3.select(this).style("font-size", "6px");});

                  belowTextpath.append("tspan")
                    .attr("class", "cc")
                    .style("fill","#00B248")
                    .attr("dy","1em")
                    .text(function (d) {
                      var g = d.type.split("~")[0] ;
                      return (g != "" ) ?  g : " ";
                    });
                  belowTextpath.append("tspan")
                    .attr("class", "fexp")
                    .style("fill","#0F024F")
                    .text(function (d) {
                      var cc = d.type.split("~")[0] ;
                      var u = d.type.split("~")[2] ;
                      return (u != "" && u!== undefined) ? ((cc!="") ?", " + u : u) : "";
                    });
                  belowTextpath.append("tspan")
                    .attr("class", "cresets")
                    .style("fill","#4C2EAA")
                    .text(function (d) {
                      var r = d.type.split("~")[3] ;
                      return (r != "" && r!== undefined) ? ", " + r : "";
                    });

            }
          }

          function tickedAut() {
              var node = d3.select(".nodes${name}")
                  .selectAll("circle")
                  .attr('cx', function(d) {return d.x = Math.max(radiusAut, Math.min(widthAut - radiusAut, d.x)); })
                  .attr('cy', function(d) {return d.y = Math.max(radiusAut, Math.min(heightAut - radiusAut, d.y)); });
              var label = d3.select(".nodes${name}")
                  .selectAll("text")
                  .attr('x', function(d) {return d.x = Math.max(radiusAut, Math.min(widthAut - radiusAut, d.x)); })
                  .attr('y', function(d) {return d.y = Math.max(radiusAut, Math.min(heightAut - radiusAut, d.y)); });

              var link = d3.select(".links${name}")
                  .selectAll("polyline")
                  .attr("points", function(d) {
                      return d.source.x + "," + d.source.y + " " +
                      (d.source.x + d.target.x)/2 + "," + (d.source.y + d.target.y)/2 + " " +
                      d.target.x + "," + d.target.y; });
  //                .attr("x1", function(d) { return d.source.x; })
  //                .attr("y1", function(d) { return d.source.y; })
  //                .attr("x2", function(d) { return d.target.x; })
  //                .attr("y2", function(d) { return d.target.y; });
              d3.select(".paths${name}").selectAll(".edgepath").attr('d', function (d) {
                  m = (d.target.y - d.source.y)/(d.target.x - d.source.x);
                  b = d.target.y - m*d.target.x;
                  new_source_x = d.source.x - 2000;
                  new_target_x = d.target.x + 2000;
                  new_source_y = new_source_x * m  +b;
                  new_target_y = new_target_x * m +b;
                  //return 'M ' + new_source_x +' '+ new_source_y  +' L '+ new_target_x +' '+ new_target_y;
                  return 'M ' + new_source_x +' '+ new_source_y  +' L '+ new_target_x +' '+ new_target_y;
              });
              d3.select(".labels${name}").selectAll(".edgelabel").attr('transform', function (d) {
                new_source_x = d.source.x - 2000;
                new_target_x = d.target.x + 2000;
                  if (new_target_x < new_source_x) {
                      var bbox = this.getBBox();
                      rx = bbox.x + bbox.width / 2;
                      ry = bbox.y + bbox.height / 2;
                      return 'rotate(180 ' + rx + ' ' + ry + ')';
                  }
                  else {
                      return 'rotate(0)';
                  }
              });
          }


          function dragstartedAut(d) {
            if (!d3.event.active) simulationAut.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
          }
          function draggedAut(d) {
            d.fx = d3.event.x;
            d.fy = d3.event.y;
          }
          function dragendedAut(d) {
            if (!d3.event.active) simulationAut.alphaTarget(0);
            if (d.group == 3 || d.group == 1){
              d.fx = null;
              d.fy = null;
            }
          }
      }
      """
  }

  private def getNodes[A<:Automata](aut: A): String =
    aut.getTrans().flatMap(processNode(aut.getInit, _)).mkString("[",",","]")

  private def getIftaNodes(aut:IftaAutomata):String =
    aut.getTrans().flatMap(processNode(aut.getInit, _, aut.ifta.cInv)).mkString("[",",","]")

  private def getLinks[A<:Automata](aut: A,name:String,portNames:Boolean=false): String =
    aut.getTrans(portNames).flatMap(t => processEdge(t,name)).mkString("[",",","]")

  private def processNode(initAut:Int,trans:(Int,Any,String,Int),nodeInvariant:Map[Int,ClockCons]=Map()): Set[String] = trans match{
    case (from,lbl,id,to) =>
      val toInv   = Show(Simplify(nodeInvariant.getOrElse(to,CTrue)))
      val fromInv = Show(Simplify(nodeInvariant.getOrElse(from,CTrue)))
      val (gfrom,gto,gp1,gp2) = nodeGroups(initAut,from,to)
      Set(s"""{"id": "$from", "group": $gfrom ,"inv":"${if (fromInv == "true") "" else fromInv}"}""",
        s"""{"id": "$to", "group": $gto ,"inv":"${if (toInv == "true") "" else toInv }"}""",
        s"""{"id": "$from-1-$to-$id", "group": "$gp1","inv":""}""",
        s"""{"id": "$to-2-$from-$id", "group": "$gp2" ,"inv":""}""")
  }

  //  private def processNode(initAut:Int,trans:(Int,(Int,Set[Int],Set[Edge]))): Set[String] = trans match{
  //    case (from,(to,fire,es)) =>
  //      val (gfrom,gto,gp1,gp2) = nodeGroups(initAut,from,to)
  //      Set(s"""{"id": "$from", "group": $gfrom }""",
  //          s"""{"id": "$to", "group": $gto }""",
  //          s"""{"id": "$from-1-$to-${fire.mkString(".")}", "group": $gp1}""",
  //          s"""{"id": "$to-2-$from-${fire.mkString(".")}", "group": $gp2 }""")
  //  }

  /**
    * Select the right group:
    *  - 0: initial state
    *  - 1: normal state
    *  - otherwise: connection dot
    */
  private def nodeGroups(initAut:Int,from:Int,to:Int):(String,String,String,String) =
    (   if(from==initAut) "0" else "1"
      , if(to==initAut) "0" else "1"
      , "2" , "2"
    )

  private def processEdge(trans:(Int,Any,String,Int), name:String): Set[String] = trans match {
    case (from, lbl,id, to) => {
      Set(s"""{"id": "${id}" , "source": "$from", "target": "$from-1-$to-$id", "type":"", "start":"start", "end": "end"}""",
        s"""{"id": "${id}" , "source": "$from-1-$to-$id", "target": "$to-2-$from-$id", "type":"$lbl", "start":"start", "end": "end"}""",
        s"""{"id": "${id}" , "source": "$to-2-$from-$id", "target": "$to", "type":"", "start":"start", "end": "endarrowout${name}"}""")
    }
  }

}
