package compile
import util.CLI
//import scala.util.parsing.input.Reader
//import scala.util.parsing.input.StreamReader
import scala.collection.immutable.PagedSeq
import java.io._
import scala.io.Source
import scala.collection.mutable.{StringBuilder, ListBuffer}
import scala.Console

// Begin parser/scanner imports
import antlr.CommonAST
import antlr.collections.AST
import antlr.Token
import edu.mit.compilers.grammar.{ DecafParser, DecafParserTokenTypes, DecafScanner, DecafScannerTokenTypes }

object Compiler {
  val tokenMap = Map(
    DecafScannerTokenTypes.CHAR_LITERAL -> "CHARLITERAL",
    DecafScannerTokenTypes.DECIMAL -> "INTLITERAL",
    DecafScannerTokenTypes.HEXADECIMAL -> "INTLITERAL",
    DecafScannerTokenTypes.IDENTIFIER -> "IDENTIFIER",
    DecafScannerTokenTypes.STR_LITERAL -> "STRINGLITERAL",
    DecafScannerTokenTypes.TK_false -> "BOOLEANLITERAL",
    DecafScannerTokenTypes.TK_true -> "BOOLEANLITERAL"
  )
  var outFile = if (CLI.outfile == null) Console.out else (new java.io.PrintStream(
    new java.io.FileOutputStream(CLI.outfile)))

  def main(args: Array[String]): Unit = {
    CLI.parse(args, Array[String]());
    if (CLI.target == CLI.Action.SCAN) {
      scan(CLI.infile)
      System.exit(0)
    } else if (CLI.target == CLI.Action.PARSE) {
        if(parse(CLI.infile) == null) {
          System.exit(1)
        }
        System.exit(0)
    }
  }

  def scan(fileName: String) {
    try {
      val inputStream: FileInputStream = new java.io.FileInputStream(fileName)
      val scanner = new DecafScanner(new DataInputStream(inputStream))
      scanner.setTrace(CLI.debug)
      var done = false
      while (!done) {
        try {
          val head = scanner.nextToken()
          if (head.getType() == DecafScannerTokenTypes.EOF) {
            done = true
          } else {
            val tokenType = tokenMap.getOrElse(head.getType(), "")
            outFile.println(head.getLine() + (if (tokenType ==  "") "" else " ") + tokenType + " " + head.getText())
          }
        } catch {
          case ex: Exception => {
            Console.err.println(CLI.infile + " " + ex)
            scanner.consume();
          }
        }
      }
    } catch {
      case ex: Exception => Console.err.println(ex)
    }
  }

  def parse(fileName: String): CommonAST  = {
    /**
    Parse the file specified by the filename. Eventually, this method
    may return a type specific to your compiler.
    */
    var inputStream : java.io.FileInputStream = null
    try {
      inputStream = new java.io.FileInputStream(fileName)
    } catch {
      case f: FileNotFoundException => { Console.err.println("File " + fileName + " does not exist"); return null }
    }
    try {
      val scanner = new DecafScanner(new DataInputStream(inputStream))
      val parser = new DecafParser(scanner);

      parser.setTrace(CLI.debug)
      parser.program()
      val t = parser.getAST().asInstanceOf[CommonAST]
      if (parser.getError()) {
        println("[ERROR] Parse failed")
        println(t.toStringList)
        return null
      } else if (CLI.debug){
        print(t.toStringList())
      }
      prettyPrint(Option(t))
      t
    } catch {
      case e: Exception => Console.err.println(CLI.infile + " " + e)
      null
    }
  }

  def prettyPrint(tree: Option[AST], numSpaces: Int = 0): Unit = {
    // siblings, including tree
    val siblings = ListBuffer[(AST, Int)]()

    var siblingOpt = tree
    while (! siblingOpt.isEmpty) {
      val sibling = siblingOpt.get
      siblings.append((sibling, numSpaces))
      siblingOpt = Option(sibling.getNextSibling)
    }

    siblings foreach {
      case (sibling, throwaway) => {
        val indent = Vector.range(0, numSpaces).map(_ => "  ").mkString("")
        println(s"${indent}${sibling.toString}")

        val childOpt = Option(sibling.getFirstChild)
        prettyPrint(childOpt, numSpaces + 1)
      }
    }
  }

}