/**
 *
 */
import Constants.Companion.operators
import Constants.Companion.scopingFunction
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.psi.Parser
import java.io.File
import java.io.BufferedReader
import java.io.FileWriter


//Adds updateMap to MutableMap class
fun MutableMap<String, Int?>.updateMap(oper: String, map: MutableMap<String, Int?>): MutableMap<String, Int?> {
    val currentValue = map.get(oper)
    if(currentValue != null) {
        map[oper] = currentValue + 1
    } else {
        map[oper] = 1
    }
    return map
}


fun main(args: Array<String>) {

    val bufferedReader: BufferedReader = File("/Users/albertinin/Documents/TCC/repo-mining/example.kt").bufferedReader()
    val inputString = bufferedReader.use { it.readText() }
    println(inputString)

    val file = Parser.parseFile(inputString)
    val map: MutableMap<String, Int?> = mutableMapOf()

    visit(file, map)
    println(map.toString())

    val newFile = File("/Users/albertinin/Documents/text.txt")
    val fileWriter = FileWriter(newFile)
    fileWriter.write(map.toString())
    fileWriter.flush()
    fileWriter.close()
}

fun visit(file: Node.File, map: MutableMap<String, Int?>){
    Visitor.visit(file) { v, _ ->
        when {
            v is Node.Expr.UnaryOp -> {
                val oper = v.oper.token
                if(oper == Node.Expr.UnaryOp.Token.NULL_DEREF) {
                    map.updateMap(oper.toString(), map)
                }
            }

            v is Node.Decl.Structured -> {
                val oper = v.form
                if(oper == Node.Decl.Structured.Form.COMPANION_OBJECT){
                    map.updateMap(oper.toString(), map)
                }
            }

            v is Node.Expr.Call.TrailLambda -> {
                val oper = "TRAIL_LAMBDA"
                map.updateMap(oper, map)
            }

            v is Node.Expr.BinaryOp -> {
                val oper = v.oper.toString()
                if(operators.contains(oper)) {
                    map.updateMap(oper, map)
                }

                if((oper == "Token(token=NEQ)") && (v.rhs.toString() == "Const(value=null, form=NULL)")){
                    map.updateMap("(!= null)", map)
                }
            }

            v is Node.Expr.Call -> {
                val scopeFunction = v.expr.toString()
                if(scopingFunction.contains(scopeFunction)){
                    map.updateMap(scopeFunction, map)
                }
            }
        }
    }
}