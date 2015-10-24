import play.api.libs.json.{JsNumber, Json, JsObject}

object TrifinityExt {
  
  def range[I,O](fr: Int => I, fn: I => O):Stream[O] =Stream.iterate(0) { _ + 1} map {i => fn(fr(i))}
  def rotate[T](seq:Seq[T]):Seq[T] = seq match {
    case Nil => Seq.empty
    case x :: xs => xs :+ x
  }

  def toList(trifinity:Trifinity):Seq[(Int,Int,String)] = for {
      x <- 0 to trifinity.size - 1
      y <- 0 to trifinity.size - 1
    } yield (x, y, trifinity.get(x,y).name)


  def toJson(trifinity: Trifinity):JsObject = new JsObject(
    Seq(
      ("board",boardAsJson(trifinity)),
      ("players", Json.toJson(trifinity.players.map(_.name))),
      ("currentPlayer", Json.toJson(trifinity.turn.name)),
      ("winners", Json.toJson(trifinity.winner.map(_.name).toList))
  ))

  private def boardAsJson(trifinity:Trifinity):JsObject = new JsObject(
    Seq(
      ("cell",Json.toJson(toList(trifinity).map(_._3))),
      ("columns", JsNumber (trifinity.size)),
      ("rows",JsNumber(trifinity.size))
    ))
}
