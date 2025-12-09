import java.util.Random;
import java.util.ArrayDeque;
import java.util.Deque;

public class GameServiceImpl implements GameService {

    private static final int SIZE = 10;
    private static final int[] FLEET = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};

    private final char[][][] shipBoards = new char[2][SIZE][SIZE];
    private final char[][][] shotBoards = new char[2][SIZE][SIZE];

    private final int[] cellsLeft = new int[2];

    private final Random random = new Random();

    public GameServiceImpl() {
        initBoards();
        placeFleetRandomly(0);
        placeFleetRandomly(1);
    }

    private void initBoards() {
        for (int p = 0; p < 2; p++)
            for (int r = 0; r < SIZE; r++)
                for (int c = 0; c < SIZE; c++) {
                    shipBoards[p][r][c] = '~';
                    shotBoards[p][r][c] = '~';
                }
    }

    private void placeFleetRandomly(int playerIndex) {
        int totalCells = 0;
        for (int len : FLEET) {
            boolean placed = false;
            while (!placed) {
                boolean horizontal = random.nextBoolean();
                int row = random.nextInt(SIZE);
                int col = random.nextInt(SIZE);

                if (canPlaceShip(playerIndex, row, col, len, horizontal)) {
                    placeShip(playerIndex, row, col, len, horizontal);
                    placed = true;
                    totalCells += len;
                }
            }
        }
        cellsLeft[playerIndex] = totalCells;
    }

    private boolean canPlaceShip(int p, int row, int col, int len, boolean horiz) {
        int dr = horiz ? 0 : 1;
        int dc = horiz ? 1 : 0;

        int endRow = row + dr * (len - 1);
        int endCol = col + dc * (len - 1);

        if (endRow >= SIZE || endCol >= SIZE)
            return false;

        for (int i = 0; i < len; i++) {
            int r = row + dr * i;
            int c = col + dc * i;
            if (!cellAndNeighborsEmpty(p, r, c)) return false;
        }
        return true;
    }

    private boolean cellAndNeighborsEmpty(int p, int row, int col) {
        for (int r = row - 1; r <= row + 1; r++)
            for (int c = col - 1; c <= col + 1; c++)
                if (r >= 0 && r < SIZE && c >= 0 && c < SIZE)
                    if (shipBoards[p][r][c] == 'O')
                        return false;
        return true;
    }

    private void placeShip(int p, int row, int col, int len, boolean horiz) {
        int dr = horiz ? 0 : 1;
        int dc = horiz ? 1 : 0;
        for (int i = 0; i < len; i++)
            shipBoards[p][row + dr * i][col + dc * i] = 'O';
    }

    @Override
    public ShotResult shoot(int playerId, int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
            return ShotResult.INVALID;

        int shooter = playerId - 1;
        int victim = 1 - shooter;

        if (shotBoards[shooter][row][col] != '~')
            return ShotResult.ALREADY_SHOT;

        char target = shipBoards[victim][row][col];

        if (target == 'O') {
            shipBoards[victim][row][col] = 'X';
            shotBoards[shooter][row][col] = 'X';
            cellsLeft[victim]--;

            if (isShipSunk(victim, row, col)) return ShotResult.SUNK;
            return ShotResult.HIT;

        } else {
            shipBoards[victim][row][col] = '*';
            shotBoards[shooter][row][col] = '*';
            return ShotResult.MISS;
        }
    }

    private boolean isShipSunk(int p, int row, int col) {
        boolean[][] visited = new boolean[SIZE][SIZE];
        Deque<int[]> stack = new ArrayDeque<>();

        stack.push(new int[]{row, col});
        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            int r = cur[0], c = cur[1];

            if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) continue;
            if (visited[r][c]) continue;

            visited[r][c] = true;

            if (shipBoards[p][r][c] == 'O')
                return false;

            if (shipBoards[p][r][c] == 'X') {
                stack.push(new int[]{r + 1, c});
                stack.push(new int[]{r - 1, c});
                stack.push(new int[]{r, c + 1});
                stack.push(new int[]{r, c - 1});
            }
        }
        return true;
    }

    @Override
    public boolean canShoot(int playerId, int row, int col) {
        return shotBoards[playerId - 1][row][col] == '~';
    }

    @Override
    public boolean isGameOver() {
        return cellsLeft[0] == 0 || cellsLeft[1] == 0;
    }

    @Override
    public int getWinner() {
        if (!isGameOver()) return 0;
        return cellsLeft[0] == 0 ? 2 : 1;
    }

    @Override
    public String boardForPlayer(int playerId) {
        int idx = playerId - 1;

        StringBuilder sb = new StringBuilder();
        sb.append("Ваши корабли:\n");
        sb.append(render(shipBoards[idx], true));
        sb.append("\nВаши выстрелы:\n");
        sb.append(render(shotBoards[idx], false));
        return sb.toString();
    }

    private String render(char[][] board, boolean showShips) {
        StringBuilder sb = new StringBuilder("   0 1 2 3 4 5 6 7 8 9\n");
        for (int r = 0; r < SIZE; r++) {
            sb.append((char) ('A' + r)).append("  ");
            for (int c = 0; c < SIZE; c++) {
                char ch = board[r][c];
                if (!showShips && ch == 'O') ch = '~';
                sb.append(ch).append(' ');
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
