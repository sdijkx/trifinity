import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.{TextMessage, Message}
import play.api.libs.json.{JsString, JsObject}
import rx.lang.scala.subjects.{SerializedSubject, PublishSubject}
import rx.lang.scala.{Subject, Observable, Observer}

abstract class GameEvent
object GameEvent {

  case class JoinEvent(replyTo:ActorRef,name:String) extends GameEvent
  case class MoveEvent(replyTo:ActorRef,name:String, x:Int, y:Int) extends GameEvent
  case class LeaveEvent(replyTo:ActorRef,name:String) extends GameEvent
  case class GameInfo(replyTo:ActorRef) extends GameEvent
  case class RequestNewGame(replyTo:ActorRef) extends GameEvent
  object Failed extends GameEvent
  object Done extends GameEvent


  def unapply(msg:Message):Option[Any] =  msg match {
    case TextMessage.Strict(str) if str.matches("Join \\w+") =>
      "Join (\\w+)".r.findFirstMatchIn(str).map(m => JoinEvent(ActorRef.noSender, m.group(1)))

    case TextMessage.Strict(str) if str.matches("Move \\d+ \\d+") =>
      "Move (\\d+) (\\d+)".r.findFirstMatchIn(str).map(m => MoveEvent(ActorRef.noSender, null, m.group(1).toInt, m.group(2).toInt))

    case TextMessage.Strict(str) if str.matches("Leave \\w+") =>
      "Leave (\\w+)".r.findFirstMatchIn(str).map(m => LeaveEvent(ActorRef.noSender, m.group(1)))

    case TextMessage.Strict(str) if str.matches("GameInfo") =>
      "GameInfo".r.findFirstMatchIn(str).map(m => GameInfo(ActorRef.noSender))

    case TextMessage.Strict(str) if str.matches("NewGame") =>
      "NewGame".r.findFirstMatchIn(str).map(m => RequestNewGame(ActorRef.noSender))

    case _ => Some(Failed)
  }

}

case class GameEventSubject() {
  val subject:Subject[GameEvent] = PublishSubject[GameEvent]()
  def observer:Observer[GameEvent] = subject
  def observable:Observable[GameEvent] = subject
}

class TrifinitySubject {

  import GameEvent._

  var trifinity:Trifinity = StartTrifinity
  val subject:Subject[Trifinity] = SerializedSubject[Trifinity](PublishSubject[Trifinity]())

  def observable[Trifinity] = subject

  def observe(events: Observable[GameEvent]) = {
    events.foreach( {
      case e:JoinEvent => join(e)
      case e:LeaveEvent => leave(e)
      case e:MoveEvent => move(e)
      case GameInfo(replyTo) => replyTo ! TrifinityExt.toJson(trifinity)
      case e:RequestNewGame => newGame(e)
    })
  }

  def join(event:JoinEvent):Unit = {
    try {
      trifinity = trifinity.join(Player(event.name))
      emit()
    } catch {
      case _:Throwable => sendError(event.replyTo, "Already joined")
    }
  }

  def leave(event:LeaveEvent):Unit = {
    try {
      trifinity = trifinity.leave(Player(event.name))
      emit()
    } catch {
      case _:Throwable => sendError(event.replyTo, "Not joined")
    }
  }

  def move(event:MoveEvent):Unit = {
    try {
      if(trifinity.gameIsFinished) {
        sendError(event.replyTo, "Game finished")
      } else  if(trifinity.turn.name == event.name) {
        trifinity = trifinity.set(event.x, event.y)
        emit()
      } else {
        sendError(event.replyTo, "Not your turn")
      }
    } catch {
      case _:Throwable => sendError(event.replyTo, "Not your turn")
    }
  }

  def newGame(event:RequestNewGame): Unit = {
    if(trifinity.gameIsFinished) {
      trifinity = StartTrifinity
      emit()
    } else {
      sendError(event.replyTo, "Game not finished")
    }
  }

  def emit(): Unit = {
    subject.onNext(trifinity)
  }

  def sendError(replyTo:ActorRef, msg:String): Unit = {
    replyTo ! JsObject(Seq(("error", JsString(msg))))
  }

}