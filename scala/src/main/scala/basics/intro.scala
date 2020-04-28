package basics

object intro extends App{
  """WHY SCALA
     ---------
    | Function + object oriented
    | TypeInference
    | Immutable data structures
    | Better Concurrency Support
    | Rich Library Collections
    |""".stripMargin


  """
    |Scala is Object oriented as well as functional programming lang. It is object oriented because every value is an object. types and behaviours of object
    |are defined by classes and traits. Scala is functional in a sense that every function is a value
    |""".stripMargin


  // Expressions vs statement
  """
    |expressions are computable statements and yields a value. For example, 1 + 1
    |Statements just perform an action. like print() or calling another function
    |""".stripMargin

  1 + 1


  // Scala supports immutable data structures. It can be a value variable or a method parameter. its defined with `val` keyword
  """
    | Easier to debug
    | Avoids accidental changes
    | Helps Parallelism - since value dont change, can be shared with threads and processes without acquiring lock on the value since they dont change.
    |""".stripMargin

    // both code refers same here. val name cant be updated with another value and thus immutable. Note how scala infers the data type in the `nameinferred` val
    val name : String ="Mohan"
    val nameInferred = "Mohan"



  // functions
  """
    |PURE function - always produce same result  for the same input irrespective of time. example sum(5*3). Recommended more in scala.
    |IMPURE function - produce different results for same input depends on other parameters. For example "Transaction decline in card" if some issue with balance.
    |""".stripMargin

  //Higher order functions
  """
    |Higher order functions takes other functions as a parameter or return a function as a result. functions are first-class values in scala. for example, map is a
    |... higher order function that takes another function.
    |""".stripMargin

}
