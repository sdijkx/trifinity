import TrifinityExt._

case class Trifinity(players:Seq[Player],get:(Int,Int) => Player) {

  def set(x:Int, y: Int):Trifinity = {
    require(players.size > 0)
    require(x >= 0 && x < size && y >= 0 && y < size)
    require(get(x,y) == EmptyPlayer)
    val player = turn
    Trifinity(rotate(players) , (mx,my) => (mx,my) match {
      case (nx,ny) if nx == x && ny == y => player
      case _ => get(mx,my)
    })
  }
  def join(player:Player):Trifinity = Trifinity(players :+ player, get)
  def turn:Player = if(players.isEmpty) EmptyPlayer else players.head
  def size:Int = players.size + 1
  def winner:Seq[Player] = solutions.map(seq => playerHasAWinningCombo(seq)).filter( _ != EmptyPlayer)

  def playerHasAWinningCombo(seq:Seq[Player]):Player = seq.distinct match {
    case xs if xs.size == 1 => xs.head
    case _ => EmptyPlayer
  }

  def solutions:Seq[Seq[Player]] =
    (for(i <- 0 to size ) yield  range( (i, _) )) ++ //rows
    (for(i <- 0 to size ) yield  range( (_, i) )) :+ //columns
    range(i => (i , i)) :+ //diagonal
    range(i => (size - 1 - i , i)) //reverse diagonal


  def range(f:Int => (Int, Int)) = TrifinityExt.range[(Int,Int),Player](f, (c:(Int,Int)) => get(c._1,c._2) ).take(size)
}

object StartTrifinity extends Trifinity(Seq.empty, (x:Int, y:Int) => EmptyPlayer )