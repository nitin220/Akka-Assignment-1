import akka.actor._
import akka.util.Timeout
import org.apache.log4j.Logger
import scala.io.Source._
import akka.pattern.ask
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global

case class MsgContainingLine(string: String)

case class MsgContainingWordsCount(words: Int)

case object GetWordCountInFileMsg

class LineWordsCounterActor extends Actor {
  def receive = {
    case MsgContainingLine(line) =>
      val wordsInLine = line.split(" ").length
      sender ! MsgContainingWordsCount(wordsInLine)


  }
}


class FileWordsCounterActor(fileName: String) extends Actor {


  var wordCount = 0
  var totalLines = 0
  var linesProcessed = 0
  var messageSender: Option[ActorRef] = None

  def receive = {

    case GetWordCountInFileMsg =>

      messageSender = Some(sender)

      fromFile(fileName).getLines.foreach { line =>
        context.actorOf(Props[LineWordsCounterActor]) ! MsgContainingLine(line)
        totalLines += 1
      }

    case MsgContainingWordsCount(words) =>
      wordCount += words
      linesProcessed += 1
      if (linesProcessed == totalLines) {
        messageSender.map(_ ! wordCount) // provide result to process invoker
      }
    }


}

object FileWordsCounter extends App {


  val log = Logger.getLogger(this.getClass)
  val system = ActorSystem("System")
  val actor = system.actorOf(Props(classOf[FileWordsCounterActor], "./src/main/resources/check.txt"))
  implicit val timeout = Timeout(15 seconds)
  val count = actor ? GetWordCountInFileMsg
  count.map { result =>
    log.info("Total number of words " + result)
    system.terminate
  }
}
