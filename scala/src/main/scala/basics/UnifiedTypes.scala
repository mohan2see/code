package basics

object UnifiedTypes extends App {

  """
    |Any is the super type of all types.  It defines certain universal methods such as equals, hashCode, and toString.
    | Any has two direct subclasses: AnyVal and AnyRef.
    |""".stripMargin


  // AnyVal represents value types. predefined value types : Int, Float, Double, Long, Unit, Boolean, Short, Byte, Char
  // Unit is a value type which carries no meaningful information. There is exactly one instance of Unit which can be declared literally like so: ().
  // All functions must return something so sometimes Unit is a useful return type.
  // AnyRef represents reference types. All non-value types are defined as reference types. Every user-defined type in Scala is a subtype of AnyRef
   val list: List[Any] = List(
     "a string", //string
     1, //int
     1.5, //double
     'c', //character
     true, //boolean
     (x: String) => "anonymous function returns a string"
      )

  list.foreach(element => println(element))

  // Type Casting. Casting is unidirectional. i.e., cant covert the val y to Long again.
  val x: Long = 987654321
  val y: Float = x
  val face: Char = '☺'
  val number: Int = face


}
