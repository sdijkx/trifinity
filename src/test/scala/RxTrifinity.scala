import org.scalatest.FunSuite

/**
 * Created by steven on 24-10-15.
 */
class RxTrifinity extends FunSuite {

  test("a test") {
    val gamer1 = Gamer("id1","one")
    val gamer2 = Gamer("id2","two")



    val trifinity = new TrifinitySubject
    trifinity.observe(gamer1.observable)
    trifinity.observe(gamer2.observable)

    gamer1.observable.foreach(println(_))

    trifinity.subject.foreach(
      trifinity => {
        println(TrifinityExt.toJson(trifinity).toString())
        println(trifinity.get(0,0))
      },
      e => {},
      () => {
        println("completed")
      }

    )


    gamer1.join()
    gamer2.join()

    gamer1.move(0,0)
    gamer2.move(0,1)

    gamer1.move(1,0)
    gamer2.move(1,1)

    gamer1.move(2,0)
    gamer1.move(2,1)

  }

}
