import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    static int MAX_X = 35;
    static int MAX_Y = 20;
    Scanner in;
    int gameRound;
    int opponentCount;
    Opponent[] opponents;
    Cell[][] grid;
    int myX;
    int myY;
    Cell myCell;
    int backInTimeLeft;
    Cell currentTarget;
    Target target;

    List<Cell> freeCells;

    Player(Scanner in) {
        this.in = in;
        this.opponentCount = in.nextInt(); // Opponent count
        debug(opponentCount);
        this.freeCells = new ArrayList<>();
    }

    static void debug(Object o, boolean print) {
        if (print)
            System.err.println(o);
    }

    static void debug(Object o) {
        debug(o, false);
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Player player = new Player(in);

        player.initOpponents();
        player.initGrid();

        // game loop
        while (true) {
            player.readInfo();

            player.readOpponents();

            player.readGrid();

            Cell target = player.computeTarget();
            // action: "x y" to move or "BACK rounds" to go back in time
            System.out.println(target);
        }
    }

    private Cell computeTarget() {
        if (this.target == null || currentTarget == null) {
            List<Cell> targets = new ArrayList<>();
            Cell topLeft = findLargeRectangle(-1, -1);
            Cell topRight = findLargeRectangle(1, -1);
            Cell bottomLeft = findLargeRectangle(-1, 1);
            Cell bottomRight = findLargeRectangle(1, 1);
            targets.add(topLeft);
            targets.add(topRight);
            targets.add(bottomLeft);
            targets.add(bottomRight);
            Cell cell = targets.get(0);
            for (int i = 1; i < targets.size(); i++) {
                if (myCell.distance(cell) < myCell.distance(targets.get(i))) {
                    cell = targets.get(i);
                }
            }
            this.target = new Target(grid[cell.y][myX]);
            this.target.next = new Target(cell);
            this.target.next.next = new Target(grid[myY][myX]);
            this.target.next.next.next = new Target(grid[myY][cell.x]);
            currentTarget = this.target.cell();
            this.target = this.target.next;
            currentTarget.message = "Reach target";
        } else {
            if (myCell.equals(currentTarget)) {
                while (this.target != null && !this.target.cell().isFree()) {
                    this.target = this.target.next;
                }
                if (target == null) {
                    return findNextFree();
                }
                currentTarget = this.target.cell();
                debug(target, true);
                this.target = this.target.next;
                currentTarget.message = "Reach target";
            } else {
                currentTarget.message = "Continue";
            }
        }
        return currentTarget;
    }

    private Cell findLargeRectangle(int dx, int dy) {
        int x = myX;
        int y = myY;
        main:
        while (isMovePossible(x, y, dx, dy)) {
            int k = Math.abs(x - myX);
            for (int i = 0; i <= k; i++) {
                if (!grid[myY + i * dy][x + dx].isFree()) {
                    return grid[y][x];
                }
            }
            for (int i = 0; i <= k; i++) {
                if (!grid[y + dy][myX + i * dx].isFree()) {
                    return grid[y][x];
                }
            }
            x += dx;
            y += dy;
        }
        debug(String.format("x - myX = %s, y - myY = %s", x - myX, y - myY));
        debug(grid[y][x]);
        if (myCell.equals(grid[y][x])) {
            Cell cell = findNextFree();
            if (cell != null) {
                return cell;
            }
        }
        return grid[y][x];
    }

    private Cell findNextFree() {
        for (int i = 1, j = 1; i < MAX_X || j < MAX_Y; i++, j++) {
            if (isInBound(myX + i, myY)) {
                if (grid[myY][myX + i].isFree()) {
                    return grid[myY][myX + i];
                }
            }
            if (isInBound(myX - i, myY)) {
                if (grid[myY][myX - i].isFree()) {
                    return grid[myY][myX - i];
                }
            }

            if (isInBound(myX, myY + j)) {
                if (grid[myY + j][myX].isFree()) {
                    return grid[myY + j][myX];
                }
            }

            if (isInBound(myX, myY - j)) {
                if (grid[myY - j][myX].isFree()) {
                    return grid[myY - j][myX];
                }
            }

            if (isInBound(myX + i, myY + j)) {
                if (grid[myY + j][myX + i].isFree()) {
                    return grid[myY + j][myX + i];
                }
            }

            if (isInBound(myX - i, myY + j)) {
                if (grid[myY + j][myX - i].isFree()) {
                    return grid[myY + j][myX - i];
                }
            }

            if (isInBound(myX + i, myY - j)) {
                if (grid[myY - j][myX + i].isFree()) {
                    return grid[myY - j][myX + i];
                }
            }

            if (isInBound(myX - i, myY - j)) {
                if (grid[myY - j][myX - i].isFree()) {
                    return grid[myY - j][myX - i];
                }
            }

        }
        return null;
    }

    private boolean isInBound(int x, int y) {
        return x >= 0 && x < MAX_X && y >= 0 && y < MAX_Y;
    }

    private boolean isMovePossible(int x, int y, int dx, int dy) {
        if (x + dx >= MAX_X) {
            return false;
        }
        if (x + dx < 0) {
            return false;
        }
        if (y + dy >= MAX_Y) {
            return false;
        }
        if (y + dy < 0) {
            return false;
        }
        return grid[y + dy][x + dx].owner == -1;
    }

    private void sort() {
        Collections.sort(freeCells, new Comparator<Cell>() {
            @Override
            public int compare(Cell o1, Cell o2) {
                double d1 = o1.distance(myCell);
                double d2 = o2.distance(myCell);
                return d1 < d2 ? -1 : (d1 == d2 ? 0 : 1);
            }
        });
    }

    private void readGrid() {
        for (int y = 0; y < MAX_Y; y++) {
            String line = in.next(); // One line of the map ('.' = free, '0' = you, otherwise the id of the opponent)
            //debug(line);
            for (int x = 0; x < MAX_X; x++) {
                Cell cell = new Cell(x, y);
                cell.owner = -1;
                grid[y][x] = cell;
                if (line.charAt(x) != '.') {
                    cell.owner = line.charAt(x) - '0';
                } else {
                    freeCells.add(cell);
                }
            }
        }
        myCell = grid[myY][myX];
    }

    private void readInfo() {
        gameRound = in.nextInt();
        myX = in.nextInt(); // Your x position
        myY = in.nextInt(); // Your y position
        backInTimeLeft = in.nextInt(); // Remaining back in time
        debug(gameRound);
        debug(myX);
        debug(myY);
        debug(backInTimeLeft);
    }

    private void readOpponents() {
        for (int id = 1; id <= opponentCount; id++) {
            opponents[id - 1] = new Opponent(in.nextInt(), in.nextInt(), id);
            opponents[id - 1].opponentBackInTimeLeft = in.nextInt(); // Remaining back in time of the opponent
            debug(opponents[id - 1].opponentX + " " + opponents[id - 1].opponentY + " " + opponents[id - 1].opponentBackInTimeLeft);
        }

    }

    private void initGrid() {
        this.grid = new Cell[MAX_Y][MAX_X];
    }

    private void initOpponents() {
        this.opponents = new Opponent[opponentCount];
    }

    class Opponent {
        int id;
        int opponentX;
        int opponentY;
        int opponentBackInTimeLeft;

        public Opponent(int opponentX, int opponentY, int id) {
            this.opponentX = opponentX;
            this.opponentY = opponentY;
            this.id = id;
        }
    }

    class Cell {
        int x;
        int y;
        int owner;
        String message = "";

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        double distance(Cell cell) {
            return Math.hypot(x - cell.x, y - cell.y);
        }

        @Override
        public String toString() {
            return String.format("%d %d", x, y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cell cell = (Cell) o;

            if (x != cell.x) return false;
            return y == cell.y;

        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        public boolean isFree() {
            return owner == -1;
        }
    }

    class Target {
        int x, y;
        Target next;

        public Target(Cell cell) {
            this.x = cell.x;
            this.y = cell.y;
        }

        Cell cell() {
            return grid[y][x];
        }

        @Override
        public String toString() {
            return cell() + "";
        }
    }
}