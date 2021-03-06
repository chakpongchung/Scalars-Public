package optimization.reg_alloc

import codegen._
import ir.components.{Def, Location, Use}
import optimization.GlobalCP.isArray
import optimization.Labeling
import optimization.Labeling.{StmtId, getStmt}
import optimization.loop_opt.LoopConstruction

import scala.collection.mutable
import scala.collection.mutable.{Queue, Set, Map}
import math.max

/**
  * apply this function to a CFGProgram.
  * would find all DU chains in the program, and store them in duChainSet.
  * note that this function does not take function calls into account.
  * and also, this function only find du chains in vaiables, not arrays.
  */
object DUChainConstruct {
  val cfgs = Set[CFG]()
  var duChainSet = Set[DefUseChain]()

  def findDuChain(): Unit = {
    val (calls, graph, revGraph) = Labeling(cfgs.toVector)
    graph.keySet filter (id => {
      val stmt = getStmt(id)
      lazy val defn = stmt.get.asInstanceOf[Def]

      stmt.isDefined &&
        stmt.get.isInstanceOf[Def] &&
        !isArray(defn.getLoc)
    }) foreach (id => {
      val defn = getStmt(id).get.asInstanceOf[Def]
      val defLoc = defn.getLoc
      val q = Queue() ++ graph(id)
      val vis = Set[StmtId]() ++ graph(id)

      //System.err.println(s"\n\ntrying to find defs for ${defLoc}")

      while (q.nonEmpty) {
        val h = q.dequeue()
        h._1 match {
          case call: CFGMethodCall => {
            if (h._2 == -1) {
              call.params foreach ({
                case useLoc: Location => {
                  useLoc.getUse foreach (loc => {
                    if (loc.field == defLoc.field) {
                      duChainSet += DefUseChain(id, h, defLoc, loc)
                    }
                  })
                }
                case _ =>
              })
            }
          }

          case block: CFGBlock => {
            if (h._2 >= 0) {
              val uses = getStmt(h).get.asInstanceOf[Use].getUse
              // PrintCFG.prtStmt(getStmt(h).get)
              //System.err.println(s"trying to find uses ${uses}")
              uses filter (defLoc.field == _.field) foreach (useLoc => {
                duChainSet += DefUseChain(id, h, defLoc, useLoc)
              })
            }
          }

          case cond: CFGConditional => {
            if (h._2 == -1) {
              cond.condition match {
                case useLoc: Location => {
                  if (useLoc.field == defLoc.field) {
                    duChainSet += DefUseChain(id, h, defLoc, useLoc)
                  }
                }
                case _ =>
              }
            }
          }

          case _ =>
        }
        val toAdd = {
          getStmt(h) match {
            case Some(ir) => {
              if (ir.isInstanceOf[Def] && ir.asInstanceOf[Def].getLoc.field == defLoc.field) {
                Vector()
              }
              else {
                graph(h) diff vis
              }
            }
            case None => graph(h) diff vis
          }
        }

        q ++= toAdd
        vis ++= toAdd
      }
    })


  }

  def testOutput(): Unit = {
    System.err.println(s"Duchain Count ${duChainSet.size}")
    duChainSet foreach( chain => {
//      System.err.println(s"defPos:${(chain.DefPos,chain.DefLoc.line, chain.DefLoc.col)}, " +
//        s"usePos:${(chain.UsePos,chain.UseLoc.line, chain.UseLoc.col)}")
      System.err.println(chain.toString)
//      System.err.println(s"defPos:${()}, " +
//              s"usePos:${()}")
    })
  }

  /**
    * this function collect following info for each du chain
    * 1. depth in loops. which is defined by the deepest position in the convex set of du chain.
    * 2. set of function calls in the chain's convex set.
    */
  def collectInfo(): Unit = {
    // at the beginning of the function, cfgs are not cleared.
    LoopConstruction.construct(cfgs.toVector)
    val dom = LoopConstruction.dom
    val loops = LoopConstruction.loops
    val graph = LoopConstruction.graph
    val revGraph = LoopConstruction.revGraph
    val calls = LoopConstruction.calls
    val depth = Map[StmtId, Int]()

    graph.keySet foreach (depth(_) = 0)
    loops foreach (_.stmts foreach (depth(_) += 1))

    def calcConvexSet(defUseChain: DefUseChain): Set[StmtId] = {
      val forwardReach = reachable(defUseChain.defPos, defUseChain.usePos, graph)
      val backwardReach = reachable(defUseChain.usePos, defUseChain.defPos, revGraph)
      forwardReach intersect backwardReach
    }

    //only those du inside the function are considered.
    duChainSet filter (du => graph.keySet.contains(du.defPos)) foreach (duChain => {
      val convexSet = calcConvexSet(duChain)
      duChain.convexSet = convexSet
      duChain.functionCalls = (Set() ++ (convexSet map (_._1))) intersect calls
      duChain.defDepth = depth(duChain.defPos)
      duChain.useDepth = depth(duChain.usePos)
    })

  }


  /**
    * reachable stmt id. include itself only incase of loop
    * @param Def
    * @param graph
    * @return
    */
  def reachable(Def:StmtId, Use:StmtId, graph: Map[StmtId, Set[StmtId]]): Set[StmtId] = {
    val q = mutable.Queue(Def)
    val vis = Set(Def)
    //System.err.println(graph.keySet)
    while (q.nonEmpty) {
      val head = q.dequeue()
      //if (head != Use) {
        val nxt = graph(head) diff vis
        q ++= nxt
        vis ++= nxt
      //}
    }
    //System.err.println("finish")
    vis
  }

  def apply(cfg: CFG): Unit = {
    if (cfgs.contains(cfg)) return
    cfgs += cfg

    cfg match {
      case program: CFGProgram => {
        duChainSet.clear()
        program.methods foreach (DUChainConstruct(_))
        cfgs.clear()
      }

      // we collect all blocks of a function.
      case method: CFGMethod => {
        if (method.block.isDefined) {
          cfgs.clear
          DUChainConstruct(method.block.get)
          findDuChain()
          duChainSet = duChainSet filter (duc => {
            !method.params.contains(duc.defLoc.field.get) &&
            !duc.defLoc.field.get.isGlobal
          })
          collectInfo()
        }
      }

      case cond: CFGConditional => {
        if (cond.next.isDefined) {
          DUChainConstruct(cond.next.get)
        }
        if (cond.ifFalse.isDefined) {
          DUChainConstruct(cond.ifFalse.get)
        }
      }

      case other => {
        if (other.next.isDefined) {
          DUChainConstruct(other.next.get)
        }
      }
    }
  }
}
