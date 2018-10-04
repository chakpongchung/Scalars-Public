package ir.typed

import ir.untyped.UntypedIR

trait IR extends UntypedIR {
  def typ: Type
}

case class Program(line: Int, col: Int, imports: Vector[Import], fields: Vector[FieldDeclaration], methods: Vector[MethodDeclaration]) extends IR {
  def typ: Type = VoidType
  override def toString: String = s"Program ${line}:${col}"
}

case class Import(line: Int, col: Int, method: MethodDeclaration) extends IR {
  def typ: Type = VoidType
  override def toString: String = s"Import ${line}:${col}"
}

case class Block(line: Int, col: Int, declarations: Vector[FieldDeclaration], statements: Vector[Statement]) extends IR {
  def typ: Type = VoidType
  override def toString: String = s"Block ${line}:${col}"
}