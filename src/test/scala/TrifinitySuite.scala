import org.scalatest.FunSuite

class TrifinitySuite extends FunSuite {

  test("Rotate a list should put the first element to the end") {
    assert(TrifinityExt.rotate(List(1,2,3)).head == 2)
  }

  test("Rotate an empty list with one element should give a list with one element") {
    assert(TrifinityExt.rotate(List(1)).head == 1)
  }

  test("Rotate an empty list should give an empty list") {
    assert(TrifinityExt.rotate(List.empty).isEmpty)
  }

  test("A starting game should have a size of 1") {
    assert(StartTrifinity.size == 1 )
  }

  test("A starting game should have an empty player at get(0,0)") {
    assert(StartTrifinity.get(0,0) == EmptyPlayer )
  }

  test("A starting game should have an empty player at turn") {
    assert(StartTrifinity.turn == EmptyPlayer )
  }


  test("After a join the board should be size 2") {
    assert(StartTrifinity.join(Player("one")).size == 2 )
  }

  test("After a first join it is the players turn") {
    val player = Player("one")
    assert(StartTrifinity.join(player).turn == player )
  }

  test("After two joins it is the first players turn") {
    val player1 = Player("one")
    val player2 = Player("two")
    assert(StartTrifinity.join(player1).join(player2).turn == player1 )
  }

  test("After two joins and a set it is the second players turn") {
    val player1 = Player("one")
    val player2 = Player("two")
    assert(StartTrifinity.join(player1).join(player2).set(0,0).turn == player2 )
  }

  test("After two joins and two sets it is the first players turn") {
    val player1 = Player("one")
    val player2 = Player("two")
    assert(StartTrifinity.join(player1).join(player2).set(0,0).set(1,0).turn == player1 )
  }


  test("After a join and a move the get should return the player") {
    val player = Player("one")
    val start = StartTrifinity.join(player)
    val game = start.set(0, 0)
    assert(game.get(0,0) == player)
    assert(game.get(1,0) == EmptyPlayer)
    assert(game.get(0,1) == EmptyPlayer)
    assert(game.get(1,1) == EmptyPlayer)
  }

  test("After a join and 2 moves the winner should be player") {
    val player = Player("one")
    val game = StartTrifinity.join(player).set(0, 0).set(0, 1)
    assert(game.winner.size == 1)
    assert(game.winner.head == player)
  }

  test("After two joins and 2 moves there should be no winner ") {
    val playerOne = Player("one")
    val playerTwo = Player("two")
    val game = StartTrifinity.join(playerOne).join(playerTwo).set(0, 0).set(0, 1)
    assert(game.winner.size == 0)
  }

}
