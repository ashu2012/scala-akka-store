package helloakka

import akka.actor.Status.Success
import akka.actor.{Actor, ActorRef, ActorSystem, Identify, Inbox, Props}

import scala.concurrent.duration._
import scala.collection._
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout

import scala.concurrent
import scala.concurrent.ExecutionContext.Implicits.global
import akka.event.Logging

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Random, Try}


case object product

case class WhatToBuy(productId: String)
case class Order(productId: String, itemCount:Int )
case class Booking(productId:String, itemCount:Int)
case class Event(message:String)
case class IntiateRequest(product:String)

class Buyer extends Actor with akka.actor.ActorLogging{

  implicit val timeout = Timeout(5 seconds)

  lazy val sellerActor=  Await.result(context.system.actorSelection( "akka://helloakka/user/Seller").resolveOne(), timeout.duration)

  override def preStart() = {
    log.debug("Starting")
  }
   def initiateReuest(product :String):Future[Event] ={

     (sellerActor ? WhatToBuy(product)).mapTo[Event]
   }

   def receive: Receive = {

     case Event(message)=>{
       //println(message + "<--- is received from Seller ")
       log.info(message + "<--- is received from Seller ")
     }

     case IntiateRequest(product)=>{
       val res = Await.result( sellerActor ? Order(product ,2 ), timeout.duration)
       self.tell(Event(res.toString),self)
     }
   }

}

class Seller extends Actor with akka.actor.ActorLogging {
  implicit val timeout = Timeout(5 seconds)
  var productDescription = ""
  lazy val buyerActor=   Await.result(context.system.actorSelection( "akka://helloakka/user/Buyer").resolveOne(), timeout.duration)
  def receive = {
    case WhatToBuy(productid) =>{
      productDescription = s"let me do inventory check for, $productid   . Please see full product page"
      buyerActor.tell( Event(productDescription), self)
    }
    case Order(productId , quantity)  =>{
      val orderActor = context.actorOf(Props[orderService])


      val future = orderActor ? Booking(productId, quantity)
      val result = Try(Await.result(future, timeout.duration).asInstanceOf[Event]).getOrElse(Event("timeout exception for inventory check "))
      //println(result)
      log.info(result.message)
      sender().tell(result, self )


    } // Send the current greeting back to the sender

    case Event(message)=>{
      println(message + " is received from OrderService ")
      log.info(message + " is received from OrderService ")

    }
  }
}



// prints a greeting
class orderService extends Actor with akka.actor.ActorLogging{

  var  userProductHistory = scala.collection.concurrent.TrieMap[String, Int]()

  def receive = {
    case Booking(productId, quantity) => {
     // println("checking product inventory ")
      log.info("checking product inventory ")
      Thread.sleep(Random.nextInt(5000-1000)+3000)
      //println("booking order ")
      log.info("booking order ")
      userProductHistory.put(productId,quantity )
      val bookingConfirmation = s"order booked for $productId and quantity= $quantity"
      sender() ! Event(bookingConfirmation)
    }
  }
}

object HelloAkkaScala extends App  {



  // Create the 'helloakka' actor system
  val system = ActorSystem("helloakka")


  // Create the 'buyer' actor
  val buyer = system.actorOf(Props[Buyer], "Buyer")

  println(buyer.path.toString)
  val seller = system.actorOf(Props[Seller], "Seller")
  println(seller.path.toString)

  // Create an "actor-in-a-box"
  val inbox = Inbox.create(system)

  // Tell the 'seller' to find listing for   'product' message
  buyer.tell(WhatToBuy("akka"), seller)

  // Ask the 'greeter for the latest 'greeting'
  // Reply should go to the "actor-in-a-box"
  inbox.send(seller, WhatToBuy("mobile phone"))

  // Wait 5 seconds for the reply with the 'greeting' message
  //val Event( message1) = inbox.receive(10.seconds)
  //println(s"product search: $message1")


  inbox.send(seller, Order("smart phone",1))

  // Wait 5 seconds for the reply with the 'greeting' message
  val Event( message) = inbox.receive(10.seconds)
  println(s"product order: $message")


system.scheduler.schedule(2 seconds, 0 minutes, buyer, IntiateRequest("Tablet"))
}


