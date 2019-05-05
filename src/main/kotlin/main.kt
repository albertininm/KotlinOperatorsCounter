/**
 * @author Víctor Galán
 */
import kastree.ast.MutableVisitor
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.Writer
import kastree.ast.psi.Converter
import kastree.ast.psi.Parser

fun main(args: Array<String>) {
    val code = """
    package foo

    fun bar() {
        // Print hello
        println("Hello, World!")
    }

    fun baz() = println("Hello, again!")
""".trimIndent()
    val file = Parser.parseFile(code)

    val extrasMap = Converter.WithExtras()
    println(Writer.write(file, extrasMap))

    var strings = emptyList<String>()
    Visitor.visit(file) { v, _ ->
        if (v is Node.Expr.StringTmpl.Elem.Regular) strings += v.str
    }
    println(strings)

    val newFile = MutableVisitor.preVisit(file) { v, _ ->
        if (v !is Node.Expr.StringTmpl.Elem.Regular) v
        else v.copy(str = v.str.replace("Hello", "Howdy"))
    }

    println(Writer.write(newFile, extrasMap))

// Prints [Hello, World!, Hello, again!]
}