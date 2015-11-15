/**
 * Created by steven on 12-11-15.
 */

import java.awt.image.{DataBufferByte, BufferedImage}

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorRef, ActorSystem, Actor}
import com.github.sarxos.webcam.Webcam
import play.api.libs.json.{JsNumber, JsArray, JsObject}

class WebcamStream extends Actor {

  val webcam:Webcam = Webcam.getDefault
  webcam.open

  def receive:Receive = {
    case GetImage => sender ! convert(webcam.getImage)
  }

  def convert(image:BufferedImage):Image = {
    Image(image.getWidth, image.getHeight, image.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData)
  }

  override def postStop:Unit = {
    webcam.close
  }

}

object GetImage
case class Image(width:Int, height:Int, data:Array[Byte]) {
  def toJson(): String = JsObject(Seq(
    ("width",JsNumber(width)),
    ("height",JsNumber(height)),
    ("data",JsArray(data.map(b => JsNumber(b.toInt)).toSeq)))).toString()
}

object Main extends App {
  implicit val system = ActorSystem("webcam")

  val webcam:ActorRef = system.actorOf(Props[WebcamStream], "webcam")

  val init:ActorRef = system.actorOf(Props(new Actor {
    def receive:Receive = {
      case "ok" => webcam ! GetImage
      case image:Image => println("receive image " + image)
    }
  }))

  init ! "ok"
}