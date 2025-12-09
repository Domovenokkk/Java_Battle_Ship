import java.util.Random;
import java.util.ArrayDeque;
import java.util.Deque;

public class GameServiceImpl implements GameService {

    private static final int SIZE = 10;
    // 4-палубный, 2×3-палубных, 3×2-палубных, 4×1-палубных
    private static final int[] FLEET = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};

    // [playerIndex][row][col]
    private final char[][][] shipBoards = new char[2][SIZE][SIZE]; // свои корабли (+ попадания/промахи врага)
    private final char[][][] shotBoards = new char[2][SIZE][SIZE]; // выстрелы по врагу

    private final int[] cellsLeft = new int[2]; // сколько палуб осталось у каждого игрока

    private final Random random = new Random();

    public GameServiceImpl() {
        initBoards();
        placeFleetRandomly(0); // Игрок 1
        placeFleetRandomly(1); // Игрок 2
    }

    private void initBoards() {
        for (int p = 0; p < 2; p++) {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    shipBoards[p][r][c] = '~';
                    shotBoards[p][r][c] = '~';
                }
            }
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

        if (endRow < 0 || endRow >= SIZE || endCol < 0 || endCol >= SIZE) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            int r = row + dr * i;
            int c = col + dc * i;
            if (!cellAndNeighborsEmpty(p, r, c)) {
                return false;
            }
        }
        return true;
    }

    // Проверяем, что в этой клетке и вокруг неё нет кораблей
    private boolean cellAndNeighborsEmpty(int p, int row, int col) {
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r >= 0 && r < SIZE && c >= 0 && c < SIZE) {
                    if (shipBoards[p][r][c] == 'O') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placeShip(int p, int row, int col, int len, boolean horiz) {
        int dr = horiz ? 0 : 1;
        int dc = horiz ? 1 : 0;
        for (int i = 0; i < len; i++) {
            int r = row + dr * i;
            int c = col + dc * i;
            shipBoards[p][r][c] = 'O';
        }
    }

    @Override
    public ShotResult shoot(int playerId, int row, int col) {
        if (playerId != 1 && playerId != 2) return ShotResult.INVALID;
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return ShotResult.INVALID;

        int shooter = playerId - 1;
        int victim = 1 - shooter;

        // проверяем только по своей матрице выстрелов
        if (shotBoards[shooter][row][col] == 'X' || shotBoards[shooter][row][col] == '*') {
            return ShotResult.ALREADY_SHOT;
        }

        char target = shipBoards[victim][row][col];

        if (target == 'O') {
            shipBoards[victim][row][col] = 'X'; // попадание на поле жертвы
            shotBoards[shooter][row][col] = 'X'; // попадание в своей таблице выстрелов
            cellsLeft[victim]--;

            if (isShipSunk(victim, row, col)) {
                return ShotResult.SUNK;
            }
            return ShotResult.HIT;
        } else {
            // промах: отмечаем только для красоты на поле жертвы
            if (shipBoards[victim][row][col] == '~') {
                shipBoards[victim][row][col] = '*'; // промах врага отображается на твоём поле
            }
            shotBoards[shooter][row][col] = '*';
            return ShotResult.MISS;
        }
    }

    // Проверяем, есть ли ещё неповреждённые клетки этого корабля
    private boolean isShipSunk(int p, int row, int col) {
        boolean[][] visited = new boolean[SIZE][SIZE];
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{row, col});

        while (!stack.isEmpty()) {
            int[] cur = stack.pop();
            int r = cur[0];
            int c = cur[1];

            if (r < 0 || r >= SIZE || c < 0 || c >= SIZE) continue;
            if (visited[r][c]) continue;

            char ch = shipBoards[p][r][c];
            if (ch != 'X' && ch != 'O') continue;

            visited[r][c] = true;

            if (ch == 'O') {
                // есть ещё живая палуба – корабль не потоплен
                return false;
            }

            // продолжаем обход
            stack.push(new int[]{r + 1, c});
            stack.push(new int[]{r - 1, c});
            stack.push(new int[]{r, c + 1});
            stack.push(new int[]{r, c - 1});
        }
        return true;
    }

    @Override
    public boolean canShoot(int playerId, int row, int col) {
        if (playerId != 1 && playerId != 2) return false;
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return false;
        int shooter = playerId - 1;
        char v = shotBoards[shooter][row][col];
        return v != 'X' && v != '*';
    }

    @Override
    public boolean isGameOver() {
        return cellsLeft[0] == 0 || cellsLeft[1] == 0;
    }

    @Override
    public int getWinner() {
        if (!isGameOver()) return 0;
        if (cellsLeft[0] == 0 && cellsLeft[1] == 0) return 0; // теоретическая ничья
        return cellsLeft[0] == 0 ? 2 : 1;
    }

    @Override
    public String boardForPlayer(int playerId) {
        int idx = playerId - 1;

        StringBuilder sb = new StringBuilder();
        sb.append("====================================\n");
        sb.append("Ваши корабли:\n");
        sb.append(renderBoard(shipBoards[idx], true));
        sb.append("\nВаши выстрелы по противнику:\n");
        sb.append(renderBoard(shotBoards[idx], false));
        sb.append("\nЛегенда: O – корабль, X – попадание, * – промах, ~ – вода\n");
        sb.append("====================================\n");
        return sb.toString();
    }

    private String renderBoard(char[][] board, boolean showShips) {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        for (int c = 0; c < SIZE; c++) {
            sb.append(c).append(" ");
        }
        sb.append("\n");

        for (int r = 0; r < SIZE; r++) {
            sb.append((char) ('A' + r)).append("  ");
            for (int c = 0; c < SIZE; c++) {
                char ch = board[r][c];
                if (!showShips && ch == 'O') {
                    ch = '~';
                }
                sb.append(ch).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
