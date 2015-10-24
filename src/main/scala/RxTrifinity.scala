import rx.lang.scala.subjects.{SerializedSubject, PublishSubject}
import rx.lang.scala.{Subject, Observable}

abstract class GameEvent(id:String)
case class JoinEvent(id:String, name:String) extends GameEvent(id)
case class MoveEvent(id:String,x:Int, y:Int) extends GameEvent(id)

case class Gamer(id:String, name:String) {

  val subject:Subject[GameEvent] = SerializedSubject[GameEvent](PublishSubject[GameEvent]())

  def move(x:Int, y:Int): Unit = {
    subject.onNext(MoveEvent(id,x,y))
  }

  def join(): Unit = {
    subject.onNext(JoinEvent(id, name))
  }

  def observable:Observable[GameEvent] = subject

}

class TrifinitySubject {

  var trifinity:Trifinity = StartTrifinity
  val subject:Subject[Trifinity] = SerializedSubject[Trifinity](PublishSubject[Trifinity]())

  def observe(events: Observable[GameEvent]) = {
    events.foreach( {
      case e:JoinEvent => join(e.name)
      case e:MoveEvent => move(e.x, e.y)
    })
  }

  def join(name:String):Unit = {
    try {
      trifinity = trifinity.join(Player(name))
      emit()
    } catch {
      case _ =>
    }
  }

  def move(x:Int, y:Int):Unit = {
    try {
      trifinity = trifinity.set(x,y)
      emit()
    } catch {
      case _ =>
    }
  }

  def emit(): Unit = {
    if(trifinity.winner.isEmpty) {
      subject.onNext(trifinity)
    } else {
      subject.onCompleted()
    }
  }
}