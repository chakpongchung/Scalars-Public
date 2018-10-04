package ir.typed

import ir.untyped.UntypedExpression

trait Expression extends IR with UntypedExpression {
  def typ: Type
}

case class Length(line: Int, col: Int, typ: Type, array: ArrayDeclaration) extends Expression {
  override def toString: String = s"Length ${line}:${col}"
}

case class Location(line: Int, col: Int, typ: Type, field: FieldDeclaration, index: Expression) extends Expression {
  override def toString: String = s"Location ${line}:${col}"
}
