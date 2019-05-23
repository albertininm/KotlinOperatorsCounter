/**
 *
 */
import kastree.ast.MutableVisitor
import kastree.ast.Node
import kastree.ast.Visitor
import kastree.ast.Writer
import kastree.ast.psi.Converter
import kastree.ast.psi.Parser

fun updateMap(oper: String, map: MutableMap<String, Int?>): MutableMap<String, Int?> {
    val currentValue = map.get(oper)
    if(currentValue != null) {
        map[oper] = currentValue + 1
    } else {
        map[oper] = 1
    }
    return map
}

fun main(args: Array<String>) {
    val code = """
package de.msal.muzei.nationalgeographic

import android.text.Html
import com.google.gson.*
import de.msal.muzei.nationalgeographic.model.Item
import java.lang.reflect.Type

class ItemDeserializer : JsonDeserializer<Item> {

   override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Item {
      json as JsonObject

      val item =
            if (json.get("image") != null) {
               // old api
               val image = json.get("image").asJsonObject
               val item: Item = context.deserialize(image, Item::class.java)
               item.internal = json.get("internal")?.asBoolean
               item.publishDate = json.get("publishDate")?.asString
               item.pageUrlPhotoOfTheDay = json.get("pageUrl")?.asString
               item.imageUrlLarge = image.get("renditions")?.asJsonArray?.last()?.asJsonObject?.get("uri")?.asString
               item
            } else {
               // new api
               val item = Gson().fromJson(json, Item::class.java)
               var x = "Hi albert"
               val z = x!!.toString()
               val y = x?.toString()
               item.imageUrlLarge = json.get("sizes")?.asJsonObject?.get("2048")?.asString
               item
            }

      // clean up photographer info
      item.photographer = stripHtml(item.photographer)
            .replace("Photograph by ", "")
            .replace(", National Geographic Your Shot", "")
            .replace(", National Geographic", "")
            .replace(", Your Shot", "")
            .replace(", My Shot", "")
            .trim()
      return item
   }

   private fun stripHtml(html: String): String {
      return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
         Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
      } else {
         @Suppress("DEPRECATION")
         Html.fromHtml(html).toString().trim { it <= ' ' }
      }
      val x = "Hi";
      val y = x?.toString()
      val z = x!!.toString()
      println(x?: y)

   }

   companion object {
        @JvmStatic  // <-- notice the @JvmStatic annotation
        val someString = "hello world"
    }


}
""".trimIndent()

    val file = Parser.parseFile(code)
    var strings = emptyList<String>()

    var map: MutableMap<String, Int?> = mutableMapOf()
    Visitor.visit(file) { v, _ ->
//        println("ITEM " + v.toString())

        if (v is Node.Expr.UnaryOp) {
            val oper = v.oper.token
            if(oper == Node.Expr.UnaryOp.Token.NULL_DEREF) {
                map = updateMap(oper.toString(), map)
            }

        }
        if(v is Node.Expr.BinaryOp){
            val oper = v.oper.toString()
            if(oper == "Token(token=DOT_SAFE)"){
                map = updateMap(oper, map)
            }
        }
        if (v is Node.Expr.BinaryOp) {
            val oper = v.oper.toString()
            if(oper == "Token(token=ELVIS)"){
                map = updateMap(oper, map)
            }
        }
        if (v is Node.Decl.Structured) {
            val oper = v.form
            if(oper == Node.Decl.Structured.Form.COMPANION_OBJECT){
                map = updateMap(oper.toString(), map)
            }
        }
    }
    println(map.toString())
}