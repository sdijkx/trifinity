import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, Message, UpgradeToWebsocket}
import akka.http.scaladsl.model.{HttpMethods, Uri, HttpResponse, HttpRequest}
import akka.stream.{ActorMaterializer}
import akka.stream.actor.{ActorPublisher}
import akka.stream.scaladsl.{Source, Sink, Flow}
import play.api.libs.json.{Json, JsString, JsObject}
import rx.lang.scala.Observer
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global



object WebserviceApp extends App {

  implicit val system = ActorSystem("trifinity")
  implicit val fm = ActorMaterializer()

  val trifinity:TrifinitySubject = new TrifinitySubject
  val binding = Http().bindAndHandleSync(handler, interface = "localhost", port = 9001)

  //Webservice handler
  def handler(request:HttpRequest):HttpResponse = request match {
    case HttpRequest(HttpMethods.GET,Uri.Path("/trifinity"),_,_,_)
      if request.header[UpgradeToWebsocket].isDefined =>
        request.header[UpgradeToWebsocket].get.handleMessages(flow)

    case HttpRequest(HttpMethods.GET,Uri.Path("/webcam"),_,_,_)
      if request.header[UpgradeToWebsocket].isDefined =>
      request.header[UpgradeToWebsocket].get.handleMessages(webcamFlow)

    case _ => HttpResponse(200, entity = "Hallo")
  }

  //Webcam flow
  def webcamFlow:Flow[Message, Message, Unit] = {
    val publisher = system.actorOf(Props[WsWebcamPublisher])
    val source = Source(ActorPublisher[Message](publisher))
    val sink = Sink.ignore
    Flow.wrap(sink,source)((_,_) => ())
  }

  //Websocket flow
  def flow: Flow[Message, Message, Unit] = {

    val publisher = system.actorOf(Props(classOf[WsPublisher]))

    val gamer = GameEventSubject()
    trifinity.observe(gamer.observable)

    trifinity.observable.subscribe(
      trifinity => publisher ! TrifinityExt.toJson(trifinity),
      e => publisher ! JsObject(Seq(("error", JsString(e.getMessage)))),
      () => publisher ! JsObject(Seq(("complete", TrifinityExt.toJson(trifinity.trifinity))))
    )
    val session = system.actorOf(Props(classOf[WsSessionActor], gamer.observer, publisher))

    val source =  Source(ActorPublisher[Message](publisher))
    val sink = Sink.actorRef(session, GameEvent.Done)
    Flow.wrap(sink, source )((_, _) => ())
  }
}


class WsSessionActor(observer:Observer[GameEvent], publisher:ActorRef) extends Actor {

  import GameEvent._

  override def  aroundReceive(receive: Actor.Receive, msg: Any):Unit = msg match {
    case GameEvent(cmd) => super.aroundReceive(receive, cmd)
    case Done => super.aroundReceive(receive, Done)
    case _ => super.aroundReceive(receive, msg)
  }

  def next(name:String):Receive = {
    case MoveEvent(_, _, x, y) => observer.onNext(MoveEvent(publisher, name, x, y))
    case RequestNewGame(_) => observer.onNext(RequestNewGame(publisher));
    case LeaveEvent(_, _) => observer.onNext(LeaveEvent(publisher, name));
    case Done => observer.onCompleted();
    case JoinEvent(_, _ ) => observer.onNext(JoinEvent(publisher,name)); context.become(next(name))
    case GameInfo(_) =>  observer.onNext(GameInfo(publisher))
  }

  def initial:Receive = {
    case JoinEvent(_, name) => observer.onNext(JoinEvent(publisher, name)); context.become(next(name))
    case GameInfo(_) =>  observer.onNext(GameInfo(publisher))
  }

  def receive:Receive = initial
}


class WsPublisher extends ActorPublisher[Message] {
  override def receive: Receive = {
    case msg:JsObject => if(isActive && totalDemand > 0) {
      onNext(TextMessage.Strict(msg.toString()))
    }
  }
}

class WsWebcamPublisher extends ActorPublisher[Message] {

  implicit val system = ActorSystem("trifinity")
  val webcam = system.actorOf(Props[WebcamStream])
  context.system.scheduler.schedule(0 seconds, 200 milli, webcam, GetImage)

  override def receive: Receive = {
    case image:Image => if(isActive && totalDemand > 0) {
      onNext(TextMessage.Strict(image.toJson()))
    }
  }
}

