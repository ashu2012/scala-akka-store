import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._
import scala.collection._

case object product
case class WhatToBuy(productId: String)
case class Order(productId: String, itemCount:Int )
case class Booking(productId:String, itemCount:Int)


class Buyer extends Actor{


}

class Seller extends Actor {
  var productDescription = ""

  def receive = {
    case WhatToBuy(productid) => productDescription = s"let me do inventory check for, $productid   . Please see full product page"
    case Order(productId , quantity)    => sender ! orderService(productId, quantity) // Send the current greeting back to the sender
  }
}



// prints a greeting
class orderService extends Actor {

  var  userProductHistory = scala.collection.concurrent.TrieMap[String, String]()

  def receive = {
    case booking(productId, quantity) => {
      println("bokking order ")
      userProductHistory.put(productId, )
    }
  }
}

object HelloAkkaScala extends App {

  // Create the 'helloakka' actor system
  val system = ActorSystem("helloakka")

  // Create the 'greeter' actor
  val greeter = system.actorOf(Props[Greeter], "greeter")

  // Create an "actor-in-a-box"
  val inbox = Inbox.create(system)

  // Tell the 'greeter' to change its 'greeting' message
  greeter.tell(WhoToGreet("akka"), ActorRef.noSender)

  // Ask the 'greeter for the latest 'greeting'
  // Reply should go to the "actor-in-a-box"
  inbox.send(greeter, Greet)

  // Wait 5 seconds for the reply with the 'greeting' message
  val Greeting(message1) = inbox.receive(5.seconds)
  println(s"Greeting: $message1")

  // Change the greeting and ask for it again
  greeter.tell(WhoToGreet("lightbend"), ActorRef.noSender)
  inbox.send(greeter, Greet)
  val Greeting(message2) = inbox.receive(5.seconds)
  println(s"Greeting: $message2")

  val greetPrinter = system.actorOf(Props[GreetPrinter])
  // after zero seconds, send a Greet message every second to the greeter with a sender of the greetPrinter
  system.scheduler.schedule(0.seconds, 1.second, greeter, Greet)(system.dispatcher, greetPrinter)

}


