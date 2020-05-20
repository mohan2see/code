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
    | Scala is Object oriented as well as functional programming lang. It is object oriented because every value is an object. types and behaviours of object
    | are defined by classes and traits. Scala is functional in a sense that every function is a value
    |""".stripMargin


  // Expressions vs statement
  """
    | expressions are computable statements and yields a value. For example, 1 + 1
    | Statements just perform an action. like print() or calling another function
    |""".stripMargin

  println(1 + 1)

  // storing the expression as a value. use `val` keyword
  val x = 1 + 1
  println(x)
  //can't reassign x with another value. for example x = 3 is not a valid expression.


  // Scala supports immutable data structures. It can be a value variable or a method parameter. its defined with `val` keyword
  """
    | Easier to debug
    | Avoids accidental changes
    | Helps Parallelism - since value dont change, can be shared with threads and processes without acquiring lock on the value since they dont change.
    |""".stripMargin

  // both code refers same here. val name cant be updated with another value and thus immutable. Note how scala infers the data type in the `nameInferred` val
  val name : String ="Mohan"
  val nameInferred = "Mohan"

  // variables. these are mutable data structures. Var supports type inference as well.
  var y = 10
  y = 20

  // Blocks. combining expression by surrounding them with {}
  println({
    val x = 3
    x
  })

  // functions are expressions that have parameters and takes arguments. parameter => what is defined in the function,
  // arguments => what is passed to the function
  """
    | PURE function - always produce same result  for the same input irrespective of time. example sum(5*3). Recommended more in scala.
    | IMPURE function - produce different results for same input depends on other parameters. For example "Transaction decline in card"
    | if some issue with balance.
    |""".stripMargin

  // defining anonymous functions, i.e., function with no name that returns multiplication of 2 for the given integer. left side of => is list of parameters
  //.. the right side is computation (expression) on the parameters
  (x : Int) => x * 2

  // naming a function
   val addFive = (y: Int) => y + 5
  // calling the function
  println(addFive(10))

  // function with multiple parameters
  val addTwoValues = (x: Int, y: Int) => x + y
  println(addTwoValues(4,3))

  // function without any parameter
  val noParameter = () => "Nothing Passed"
  println(noParameter())

  //function can have multi-line expression as well
  val multiLineExpression = (x: Int, y:Int) => {
    println(x + y)

  }

  multiLineExpression(5,3)

  // Methods looks and behaves similar to functions
  """
    | methods are defined with def keyword followed by name of the method, parameter list, a return type and body
    | Below, `add` is the method name, x,y are parameters and : Int is return type and x + y is the expression to return.
    |""".stripMargin

  def add(x:Int, y:Int): Int = x + y

  // a method can take multiple parameter list. Both below methods output is same, see how different the third parameter is defined in both methods.
  def addThenMultiply(x: Int, y: Int )(Multiplier: Int): Int = (x + y) * Multiplier
  def anotherWay( x: Int, y: Int, Multi: Int) = (x + y) * Multi

  println(addThenMultiply(5,4)(2))
  println(anotherWay(5,4,2))

  // a method can be defined without any parameter as well, just like functions does.
  def withoutParam() : String = System.getProperty("user.name")
  println(withoutParam())

  // methods can have multi-line expressions as well. The last expression in the body is the method’s return value.
  // Scala does have a return keyword, but it is rarely used.
  def multiLineExpr(input:Double) : String = {
    val square = input * input
    square.toString
  }
  println(multiLineExpr(5.1))


  // CLASSES
  """
    | classes can be defined with `class` keyword followed by constructor parameters.
    | Notice the return type of the method greet is Unit, which signifies that there is nothing meaningful to return. It is used similarly to void
    |  in Java and C. A difference is that, because every Scala expression must have some value, there is actually a singleton value of type Unit, written ().
    | It carries no information.
    |""".stripMargin

  class Greeter(prefix: String, suffix: String) {
    def greet(name: String): Unit = {
      println(prefix + name + suffix)
    }
  }

  // a instance of a class can be created with `new` keyword.
  val greeter = new Greeter("Hello, ", "!")
  greeter.greet("Mohan")


  // scala has special type of class called case class. By default, instances of case classes are immutable and they are compared by value (whereas in classes,
  // ... the instance are compared by ref). This makes cases classes useful in pattern matching.

  case class Fruit(name: String)

  // can instantiate without new keyword
  val orange = Fruit("Orange")
  val apple = Fruit("Apple")

  // instances of case classes are compared by value, not by reference:
  if (orange == apple) {
    println(orange + " and " + apple + " are same!")
  } else {
    println(orange + " and " + apple + " clearly not same.")
  }


  // Objects
  """
    |  Objects are single instances of their own definitions (meaning that you cant create instance of a object).
    |  You can think of them as singletons of their own classes.
    |  defined by `object` keyword
    |""".stripMargin

  object IdFactory {
    private var counter = 0
    def create() : Int = {
     counter += 1
     counter
    }
  }

  val newID = IdFactory.create() //prints 1
  val nextID = IdFactory.create() //prints 2 -- because objects are single instances, and thus persists the values.
  println(newID)
  println(nextID)

  // Comparing objects with classes that has same implementation. See how the output differs. Since objects are single instances it persists the value of
  // counter variable whereas in classes, counter variable is always set to 0 when an instance is created.

  class IdFactoryClass {
    private var counter = 0
    def create() : Int = {
      counter += 1
      counter
    }
  }



  val classNewID = new IdFactoryClass()
  val classNextID = new IdFactoryClass()

  println(classNewID.create()) //prints 1
  println(classNextID.create()) // prints 1
  println(classNextID.create())  // prints 2 -- here since the same instance(classNextID) is calling create again, it persists the counter variable value.


  // Traits
  """
    |Traits are abstract data types contains certain fields and methods. Traits can also have implementations.
    |In scala, a class can extend only one class but it can extend multiple traits.
    |""".stripMargin


  trait Calling {
    def callSomeone(name: String) : Unit
  }

  // Traits can also have implementations.
  trait CallingSomeone {
    def callSomeone(name: String) : Unit ={
      println("calling " + name)
    }
  }

  // class extending a trait. can extend multiple traits
  class CallingWithoutPrint extends Calling{
    override def callSomeone(name: String): Unit = {
      println("calling " + name) // implementing the abstract method
    }
  }

  class CallingWithPrint extends CallingSomeone {
    override def callSomeone(name: String): Unit = super.callSomeone(name) // just calling the implementation in super trait or override your base implementation
  }

  val Home = new CallingWithoutPrint()
  Home.callSomeone("Home")

  val Brother = new CallingWithPrint()
  Brother.callSomeone("Brother")


  //Higher order functions
  """
    | Higher order functions takes other functions as a parameter or return a function as a result. functions are
    | first-class values in scala. for example, map is a
    | ... higher order function that takes another function.
    |""".stripMargin

}
