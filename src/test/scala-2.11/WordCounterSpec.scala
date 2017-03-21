import akka.actor.{Props, ActorSystem}
import akka.testkit.TestKit
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import org.scalatest.{MustMatchers, BeforeAndAfterAll, WordSpecLike}


import scala.concurrent.Await

class WordCounterSpec extends TestKit(ActorSystem("test-system")) with WordSpecLike
  with BeforeAndAfterAll with MustMatchers {

  override protected def afterAll(): Unit = {
    system.terminate()
  }

  "LineWordsCounterActor" must {
    "Return Word Count in a line" in {
      val ref = system.actorOf(Props[LineWordsCounterActor])
      ref tell(MsgContainingLine("Hello this is test"), testActor)

      expectMsgPF() {
        case MsgContainingWordsCount(words) =>
          words must be(4)
      }
    }
  }

  "FileWordsCounterActor" must {
    "Return Word Count in a File" in {

      val actor = system.actorOf(Props(classOf[FileWordsCounterActor], "./src/main/resources/check.txt"))
      implicit val timeout = Timeout(15 seconds)
      val future = actor ? GetWordCountInFileMsg
      Await.result(future, Duration.Inf) must be(19)

    }
  }

}
