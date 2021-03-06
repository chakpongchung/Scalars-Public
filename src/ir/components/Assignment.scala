package ir.components

trait Assignment extends Statement with Def with Use {
  def loc: Location
}

trait AssignmentStatements extends Assignment {
  def value: Expression
}

case class AssignStatement(
    line: Int,
    col: Int,
    var loc: Location,
    var value: Expression) extends Assignment with AssignmentStatements {

  def typ: Option[Type] = loc.typ

  override def getLoc: Location = loc
  override def toString: String = s"[AssignStatement] ${typ.get}  (${line}:${col})"
}

case class CompoundAssignStatement(
    line: Int,
    col: Int,
    var loc: Location,
    var value: Expression,
    operator: ArithmeticOperator) extends Assignment with AssignmentStatements {

  override def getLoc: Location = loc
  override def toString: String = s"[CompoundAssignStatement] ${operator}  (${line}:${col})"
}

case class Increment(line: Int, col: Int, loc: Location) extends Assignment {
  override def getLoc: Location = loc
  override def toString: String = s"[Increment]  (${line}:${col})"
}

case class Decrement(line: Int, col: Int, loc: Location) extends Assignment {
  override def getLoc: Location = loc
  override def toString: String = s"[Decrement]  (${line}:${col})"
}
