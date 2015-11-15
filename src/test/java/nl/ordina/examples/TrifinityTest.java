package nl.ordina.examples;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class TrifinityTest {


    @Test
    public void testJoin() {
        Trifinity.Player player = new Trifinity.Player("player1");
        Trifinity trifinity = Trifinity.START;
        trifinity = trifinity.join(player);
        assertTrue(trifinity.size() == 2);
        assertTrue(trifinity.getTurn().equals(player));
    }

    @Test
    public void testSet() {
        Trifinity trifinity = createTrifinity2Players();
        trifinity = trifinity.set(0, 0);
        trifinity = trifinity.set(1, 0);
        assertNotSame(trifinity.get(1, 0), trifinity.get(0, 0));
        assertNotSame(Trifinity.UNASSIGNED, trifinity.get(0, 0));
        assertNotSame(Trifinity.UNASSIGNED, trifinity.get(1, 0));
        assertEquals(Trifinity.UNASSIGNED, trifinity.get(2, 0));
    }

    @Test(expected = Exception.class)
    public void testSetWithoutPlayers() {
        Trifinity trifinity = Trifinity.START;
        trifinity.set(0, 0);
    }

    @Test(expected = Exception.class)
    public void testSetFinishedGame() {
        Trifinity trifinity = createTrifinity2Players();
        trifinity = decided2PlayerGame(trifinity);
        assertTrue(trifinity.isFinished());
        assertEquals(Trifinity.UNASSIGNED, trifinity.get(2, 1));
        trifinity.set(2, 1);
    }

    @Test(expected = Exception.class)
    public void testSetOccupiedCell() {
        Trifinity trifinity = createTrifinity2Players();
        assertEquals(Trifinity.UNASSIGNED, trifinity.get(0, 0));
        trifinity = trifinity.set(0, 0);
        trifinity.set(0, 0);
    }


    @Test
    public void testSetOutsideRange() {
        Trifinity trifinity = createTrifinity2Players();
        try {
            trifinity.set(-1, 0);
            fail();
        } catch(Exception ignored) {}

        try {
            trifinity.set(-1, -1);
            fail();
        } catch(Exception ignored) {}

        try {
            trifinity.set(0, -1);
            fail();
        } catch(Exception ignored) {}

        try {
            trifinity.set(3, 0);
            fail();
        } catch(Exception ignored) {}

        try {
            trifinity.set(3, 3);
            fail();
        } catch(Exception ignored) {}

        try {
            trifinity.set(0, 3);
            fail();
        } catch(Exception ignored) {}

    }



    @Test
    public void testGet() {
        Trifinity trifinity = Trifinity.START;
        assertEquals(Trifinity.UNASSIGNED, trifinity.get(0, 0));
    }

    @Test
    public void testGetOutsideRange() {
        Trifinity trifinity = Trifinity.START;

        assertEquals(Trifinity.UNASSIGNED, trifinity.get(0,0));
        try {
            trifinity.get(-1, 0);
            fail();
        } catch (Exception ignored) {}
        try {
            trifinity.get(0, -1);
            fail();
        } catch (Exception ignored) {}

        try {
            trifinity.get(-1, -1);
            fail();
        } catch (Exception ignored) {}

        try {
            trifinity.get(1, 0);
            fail();
        } catch (Exception ignored) {}

        try {
            trifinity.get(0, 1);
            fail();
        } catch (Exception ignored) {}

        try {
            trifinity.get(1, 1);
            fail();
        } catch (Exception ignored) {}
    }

    @Test
    public void testTurn() {
        Trifinity trifinity = Trifinity.START;
        assertEquals(Trifinity.UNASSIGNED, trifinity.getTurn());
        Trifinity.Player player1 = new Trifinity.Player("player1");
        Trifinity.Player player2 = new Trifinity.Player("player2");
        trifinity = trifinity.join(player1);
        trifinity = trifinity.join(player2);
        assertTrue(trifinity.getTurn().equals(player1));
        trifinity = trifinity.set(0, 0);
        assertTrue(trifinity.getTurn().equals(player2));
        trifinity = trifinity.set(1, 0);
        assertTrue(trifinity.getTurn().equals(player1));
    }

    @Test
    public void testWinner() {

        for(int size = 3; size < 10; size++) {
            //test columns
            for(int c=0; c < size ; c++) {
                List<Trifinity.Player> players = new ArrayList<>();
                Trifinity trifinity = createGame(size - 1 , players);
                for(int r = 0; r < size; r++) {
                    for(int p = 0 ; p < players.size(); p++) {
                        if(!trifinity.isFinished()) {
                            trifinity = trifinity.set(r, (c + p) % (size -1));
                        }
                    }
                }
                final Collection<Trifinity.Player> winnners = trifinity.getWinners();
                assertTrue(winnners.size() == 1);
                assertTrue(winnners.contains(players.get(0)));
                assertTrue(IntStream.range(1, size - 1).allMatch(ip -> !winnners.contains(players.get(ip))));
            }
            //test rows
            for(int r=0; r < size ; r++) {
                List<Trifinity.Player> players = new ArrayList<>();
                Trifinity trifinity = createGame(size - 1 , players);
                for(int c = 0; c < size; c++) {
                    for(int p = 0 ; p < players.size(); p++) {
                        if(!trifinity.isFinished()) {
                            trifinity = trifinity.set((r + p) % (size -1), c);
                        }
                    }
                }
                final Collection<Trifinity.Player> winnners = trifinity.getWinners();
                assertTrue(winnners.size() == 1);
                assertTrue(winnners.contains(players.get(0)));
                assertTrue(IntStream.range(1, size - 1).allMatch(ip -> !winnners.contains(players.get(ip))));
            }

            //test LR diagonal
            {
                List<Trifinity.Player> players = new ArrayList<>();
                Trifinity trifinity = createGame(size - 1, players);
                for (int r = 0; r < size; r++) {
                    for (int c = size - 1; c >= 0 && !trifinity.isFinished(); c--) {
                        trifinity = trifinity.set(r, c);
                    }
                }
                final Collection<Trifinity.Player> winnners = trifinity.getWinners();
                assertTrue(winnners.size() == 1);
                assertTrue(winnners.contains(players.get(0)));
                assertTrue(IntStream.range(1, size - 1).allMatch(ip -> !winnners.contains(players.get(ip))));
            }
            //test RL diagonal
            {
                List<Trifinity.Player> players = new ArrayList<>();
                Trifinity trifinity = createGame(size - 1, players);
                for (int r = 0; r < size; r++) {
                    for (int c = 0; c < size && !trifinity.isFinished(); c++) {
                        trifinity = trifinity.set(r, c);
                    }
                }
                final Collection<Trifinity.Player> winnners = trifinity.getWinners();
                assertTrue(winnners.size() == 1);
                assertTrue(winnners.contains(players.get(0)));
                assertTrue(IntStream.range(1, size - 1).allMatch(ip -> !winnners.contains(players.get(ip))));
            }

        }
    }

    @Test
    public void testNoMoreMovesWithoutWinner() {
        Trifinity trifinity = createTrifinity2Players();
        assertFalse(trifinity.noMoreMoves());
        trifinity = undecided2PlayerGame(trifinity);
        assertTrue(trifinity.noMoreMoves());
    }

    @Test
    public void testNoMoreMovesWithWinner() {
        Trifinity trifinity = createTrifinity2Players();
        assertFalse(trifinity.noMoreMoves());
        trifinity = decided2PlayerGame(trifinity);
        assertFalse(trifinity.noMoreMoves());
    }

    @Test
    public void testIsFinishedWithWinner() {
        Trifinity trifinity = createTrifinity2Players();
        assertFalse(trifinity.isFinished());
        trifinity = decided2PlayerGame(trifinity);
        assertTrue(trifinity.isFinished());
    }

    @Test
    public void testIsFinishedWithoutWinner() {
        Trifinity trifinity = createTrifinity2Players();
        assertFalse(trifinity.isFinished());
        trifinity = undecided2PlayerGame(trifinity);
        assertTrue(trifinity.isFinished());
    }

    @Test
    public void testGetBoard() {
        Trifinity trifinity = createTrifinity2Players();
        assertTrue(Stream.of(trifinity.getBoard()).allMatch(r -> Stream.of(r).allMatch(p -> p == Trifinity.UNASSIGNED)));
        trifinity = undecided2PlayerGame(trifinity);
        assertTrue(Stream.of(trifinity.getBoard()).allMatch(r -> Stream.of(r).allMatch(p -> p != Trifinity.UNASSIGNED)));
        Trifinity.Player[][] board = trifinity.getBoard();
        for(int r=0; r < trifinity.size(); r ++) {
            for(int c = 0; c< trifinity.size(); c++) {
                assertEquals(trifinity.get(r,c), board[r][c]);
            }
        }
    }



    private Trifinity undecided2PlayerGame(Trifinity trifinity) {
        trifinity = trifinity.set(0,0);
        trifinity = trifinity.set(1,0);
        trifinity = trifinity.set(2,0);
        trifinity = trifinity.set(0,1);
        trifinity = trifinity.set(2,1);
        trifinity = trifinity.set(1,1);
        trifinity = trifinity.set(0,2);
        trifinity = trifinity.set(2, 2);
        trifinity = trifinity.set(1,2);
        return trifinity;
    }

    private Trifinity decided2PlayerGame(Trifinity trifinity) {
        trifinity = trifinity.set(0,0);
        trifinity = trifinity.set(1,0);
        trifinity = trifinity.set(0,1);
        trifinity = trifinity.set(1, 1);
        trifinity = trifinity.set(0,2);
        return trifinity;
    }

    private Trifinity createTrifinity2Players() {
        List<Trifinity.Player> players = new ArrayList<>();
        return createGame(2, players);
    }

    private Trifinity createGame(int numberOfPlayers, List<Trifinity.Player> players) {
        Trifinity trifinity = Trifinity.START;
        for(int p = 1; p <= numberOfPlayers; p ++) {
            Trifinity.Player player = new Trifinity.Player("player" + p);
            trifinity = trifinity.join(player);
            players.add(player);
        }
        return trifinity;
    }


}