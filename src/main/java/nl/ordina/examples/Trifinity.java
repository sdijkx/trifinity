package nl.ordina.examples;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Trifinity {

    public static class Coordinate {
        public final int x, y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Coordinate{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    public static class Player {
        private final String name;

        public Player(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Player{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static final Player UNASSIGNED = new Player(null);
    public static final Trifinity START = new Trifinity(new LinkedList<>(), c -> UNASSIGNED);

    private final LinkedList<Player> players;
    private final Function<Coordinate,Player> get;

    public Trifinity(LinkedList<Player> players, Function<Coordinate, Player> get) {
        this.players = players;
        this.get = get;
    }

    public Player get(int x, int y) {
        require(x >= 0 && x <size() && y>=0 && y<size(), "Coordinate not inside board");
        return get.apply(new Coordinate(x, y));
    }

    public Trifinity set(int x, int y) {
        require(!players.isEmpty(), "No players");
        require(!isFinished(), "Game is finished");
        require((x>=0 && x<size() && y>=0 && y<size()), "Invalid coordinate("+x+","+y+")");
        require(get(x,y).equals(UNASSIGNED), "Already occupied coordinate(" + x + "," + y + ")");
        Trifinity self = this;
        Player setter = getTurn();
        return new Trifinity( rotate(players), (c) -> {
            if(c.x == x && c.y == y) {
                return setter;
            }
            return self.get.apply(c);
        });
    }

    private void require(boolean b, String msg) {
        if(!b) {
            throw new RuntimeException(msg);
        }
    }

    public Trifinity join(Player player) {
        LinkedList<Player> newPlayerList = new LinkedList<>(players);
        newPlayerList.add(player);
        return new Trifinity(newPlayerList, this.get);
    }

    public Player getTurn() {
        if(players.isEmpty()) {
            return UNASSIGNED;
        } else {
            return players.getFirst();
        }
    }

    public int size() {
        return players.size() + 1;
    }

    public boolean isFinished() {
        return hasWinner() || noMoreMoves();
    }

    public boolean noMoreMoves() {
        return IntStream.range(0, size())
                .mapToObj( r -> IntStream.range(0, size())
                        .mapToObj( c -> get(r,c) )
                )
                .flatMap(player -> player)
                .allMatch(player -> !UNASSIGNED.equals(player));
    }

    public boolean hasWinner() {
        return !getWinners().isEmpty();
    }

    public Collection<Player> getWinners() {
        return getStreamOfAllCoordinateRanges()
                .map(r -> r.distinct().collect(Collectors.toList()))
                .filter( r -> r.size() == 1)
                .filter( r -> {
                    Optional<Player> winner = r.stream().findFirst();
                    return winner.isPresent() && !winner.get().equals(UNASSIGNED);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public Player[][] getBoard() {
        Player[][] board = new Player[size()][size()];
        for(int r = 0; r< size(); r++) {
            for (int c = 0; c < size(); c++) {
                board[r][c] = get(r,c);
            }
        }
        return board;
    }

    private Stream<Stream<Player>> getStreamOfAllCoordinateRanges() {
        Stream<Stream<Player>> rows = IntStream.range(0, size()).mapToObj( r -> coordinateRange(i -> new Coordinate(i, r)));
        Stream<Stream<Player>> columns = IntStream.range(0, size()).mapToObj(c -> coordinateRange(i -> new Coordinate(c, i)));
        Stream<Player> diagonal = coordinateRange(i -> new Coordinate(i, i));
        Stream<Player> inverseDiagonal = coordinateRange(i -> new Coordinate(size() - i - 1, i));
        Stream<Stream<Player>> columnAndRows = Stream.concat(rows, columns);
        return Stream.concat(columnAndRows, Stream.of(diagonal, inverseDiagonal));
    }

    private Stream<Player> coordinateRange(Function<Integer, Coordinate> fIndex) {
        return range(fIndex, get).limit(size());
    }

    private static <I,O> Stream<O> range(Function<Integer, I> fIndex, Function<I, O> fValue) {
        return IntStream.iterate(0, i -> i + 1).mapToObj( i -> fValue.apply(fIndex.apply(i)));
    }

    private LinkedList<Player> rotate(LinkedList<Player> players) {
        LinkedList<Player> rotated = new LinkedList<>(players);
        if(!rotated.isEmpty()) {
            rotated.addLast(rotated.removeFirst());
        }
        return rotated;
    }

}
