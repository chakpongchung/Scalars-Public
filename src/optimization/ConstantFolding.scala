package optimization

import codegen._
import ir.components._

object ConstantFolding extends Optimization {

  override def toString(): String = "ConstantFolding"

  def apply(cfg: CFG, isInit: Boolean=true): Unit = {
   if (isInit) { init }
   if (cfg.isOptimized(ConstantFolding)) {
     return
   }
   cfg.setOptimized(ConstantFolding)

   cfg match {
     case program: CFGProgram => {
       program.methods foreach (ConstantFolding(_, isInit = false))
     }

     // we collect all blocks of a function.
     case method: CFGMethod => {
       if (method.block.isDefined) {
         ConstantFolding(method.block.get, isInit = false)
       }
     }

     case cond: CFGConditional => {
       if (cond.next.isDefined) {
         ConstantFolding(cond.next.get, isInit = false)
       }
       if (cond.ifFalse.isDefined) {
         ConstantFolding(cond.ifFalse.get, isInit = false)
       }
     }

     case block: CFGBlock => {
       for (idx <- block.statements.indices) {
         val stmt = block.statements(idx)
         stmt match {
           case unary: UnaryOperation => {
             unary.expression match {
               case lit: IntLiteral => {
                 val calculatedValue = unary match {
                   case _:Not => IntLiteral(lit.line, lit.col, ~lit.value)
                   case _:Negate => IntLiteral(lit.line, lit.col, -lit.value)
                 }

                 block.statements(idx) = AssignStatement(unary.line, unary.col, unary.eval.get, calculatedValue)
                 setChanged()
               }
               case _ =>
             }
           }

           case binary: BinaryOperation => {
             if (binary.lhs.isInstanceOf[Literal] && binary.rhs.isInstanceOf[Literal]) {
               lazy val lhsBool = binary.lhs.asInstanceOf[BoolLiteral]
               lazy val rhsBool = binary.rhs.asInstanceOf[BoolLiteral]
               lazy val lhsInt = binary.lhs.asInstanceOf[IntLiteral]
               lazy val rhsInt = binary.rhs.asInstanceOf[IntLiteral]

               val calculatedValue = binary match {
                 case logical: LogicalOperation =>
                   logical.operator match {
                     case And => BoolLiteral(binary.line, binary.col, lhsBool.value && rhsBool.value)
                     case Or => BoolLiteral(binary.line, binary.col, lhsBool.value || rhsBool.value)
                     case Equal => binary.lhs match {
                       case _: BoolLiteral => BoolLiteral(binary.line, binary.col, lhsBool.value == rhsBool.value)
                       case _: IntLiteral => BoolLiteral(binary.line, binary.col, lhsInt.value == rhsInt.value)
                     }
                     case NotEqual => binary.lhs match {
                       case _: BoolLiteral => BoolLiteral(binary.line, binary.col, lhsBool.value == rhsBool.value)
                       case _: IntLiteral => BoolLiteral(binary.line, binary.col, lhsInt.value == rhsInt.value)
                     }
                     case GreaterThan => BoolLiteral(binary.line, binary.col, lhsInt.value > rhsInt.value)
                     case GreaterThanOrEqual => BoolLiteral(binary.line, binary.col, lhsInt.value >= rhsInt.value)
                     case LessThan => BoolLiteral(binary.line, binary.col, lhsInt.value < rhsInt.value)
                     case LessThanOrEqual => BoolLiteral(binary.line, binary.col, lhsInt.value <= rhsInt.value)
                   }
                 case arith: ArithmeticOperation =>
                   arith.operator match {
                     case Add => IntLiteral(binary.line, binary.col, lhsInt.value + rhsInt.value)
                     case Divide => IntLiteral(binary.line, binary.col, lhsInt.value / rhsInt.value) // TODO divide by zero would be an error.
                     case Modulo => IntLiteral(binary.line, binary.col, lhsInt.value % rhsInt.value)
                     case Multiply => IntLiteral(binary.line, binary.col, lhsInt.value * rhsInt.value)
                     case Subtract => IntLiteral(binary.line, binary.col, lhsInt.value - rhsInt.value)
                   }
               }

               block.statements(idx) = AssignStatement(binary.line, binary.col, binary.eval.get, calculatedValue)
               setChanged()
             }
           }

           case _ =>
         }
       }

       if (block.next.isDefined) {
         ConstantFolding(block.next.get, isInit=false)
       }
     }

     case other => {
       if (other.next.isDefined) {
         ConstantFolding(other.next.get, isInit=false)
       }
     }
   }
 }
}
