import akka.actor.{Props, ActorSystem, Actor}
import akka.util.Timeout
import org.apache.log4j.Logger
import scala.concurrent.duration.DurationInt


class BookingSystem extends Actor {
  var totalSeats = 7
  val log = Logger.getLogger(this.getClass)

  override def receive: Receive = {
    case num: Int =>
      implicit val timeout = Timeout(10 seconds)
      if (num <= totalSeats) {
        log.info(s"Available seats = $totalSeats")
        totalSeats -= num
        log.info(s"Booked seats = $num ")
        log.info(s"Remaining seats = $totalSeats \n")
      }
      else {
        log.info(s"Available seats = $totalSeats")
        log.info(s"$num Seats Not Available!! \n")
      }
  }
}

class BookingCounter extends Actor {

  def receive: Receive = {
    case bookingRequest: Int =>
      val bookingSystem = context.actorSelection("../System")
      bookingSystem forward bookingRequest
  }
}


object BookingTickets extends App {

  val system = ActorSystem("BookingTickets")

  system.actorOf(Props[BookingSystem], "System")
  val bookingCounter1 = system.actorOf(Props[BookingCounter], "Counter-1")
  val bookingCounter2 = system.actorOf(Props[BookingCounter], "Counter-2")

  bookingCounter1 ! 1
  bookingCounter2 ! 2
  bookingCounter1 ! 3
  bookingCounter2 ! 4
  bookingCounter1 ! 1

}
