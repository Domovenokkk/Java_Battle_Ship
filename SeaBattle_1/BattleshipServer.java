import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class BattleshipServer {

    private static final int PORT = 5000;

    private final GameService game = new GameServiceImpl();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new BattleshipServer().run();
    }

    private void run() {
        System.out.println("=== Морской бой (Сервер, Игрок 1) ===");
        System.out.println("Ожидание подключения на порту " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             Socket client = serverSocket.accept();
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

            System.out.println("Клиент подключён. Вы – Игрок 1, клиент – Игрок 2.");
            sendMessage(out, "Подключено к серверу. Вы – Игрок 2.");
            sendBoard(out, 2);

            int currentPlayer = 1;

            while (!game.isGameOver()) {
                if (currentPlayer == 1) {
                    boolean repeat = doServerTurn(out);
                    if (!repeat && !game.isGameOver()) {
                        currentPlayer = 2;
                    }
                } else {
                    boolean repeat = doClientTurn(in, out);
                    if (!repeat && !game.isGameOver()) {
                        currentPlayer = 1;
                    }
                }
            }

            int winner = game.getWinner();
            String resultText = "Игра окончена. Победил игрок " + winner + ".";
            System.out.println(resultText);
            out.println(Action.GAME_OVER.name() + ":" + winner + ":" + resultText);

        } catch (IOException e) {
            System.out.println("Ошибка сервера: " + e.getMessage());
        }
    }

    // Ход сервера (Игрок 1). Возвращает true, если игрок ходит ещё раз.
    private boolean doServerTurn(PrintWriter out) {
        System.out.println("\n--- Ваш ход (Игрок 1) ---");
        System.out.println(game.boardForPlayer(1));

        int[] rc = askCoordinateFromConsole("Введите клетку для выстрела (например A0): ", 1);
        int row = rc[0];
        int col = rc[1];

        GameService.ShotResult result = game.shoot(1, row, col);
        String coordStr = coordToString(row, col);
        System.out.println("Выстрел по " + coordStr + " -> " + resultToText(result));

        sendMessage(out, "Противник стрелял по " + coordStr + " -> " + resultToText(result));
        sendBoard(out, 2);

        return result == GameService.ShotResult.HIT || result == GameService.ShotResult.SUNK;
    }

    // Ход клиента (Игрок 2). Возвращает true, если клиент ходит ещё раз.
    private boolean doClientTurn(BufferedReader in, PrintWriter out) throws IOException {
        sendMessage(out, "\n--- Ваш ход (Игрок 2) ---");
        sendBoard(out, 2);
        out.println(Action.YOUR_TURN.name());

        while (true) {
            String line = in.readLine();
            if (line == null) {
                throw new IOException("Клиент отключился.");
            }

            line = line.trim();
            if (!line.startsWith("SHOT ")) {
                // Непонятная команда — игнорируем
                continue;
            }

            String coordStr = line.substring(5).trim();
            int[] rc = parseCoord(coordStr);
            if (rc == null) {
                sendMessage(out, "Неверный формат координаты. Используйте, например, A0.");
                out.println(Action.YOUR_TURN.name());
                continue;
            }

            int row = rc[0];
            int col = rc[1];

            if (!game.canShoot(2, row, col)) {
                sendMessage(out, "В эту клетку уже стреляли или она вне поля. Попробуйте снова.");
                out.println(Action.YOUR_TURN.name());
                continue;
            }

            GameService.ShotResult result = game.shoot(2, row, col);
            String resText = resultToText(result);
            System.out.println("Игрок 2 стреляет по " + coordToString(row, col) + " -> " + resText);

            sendMessage(out, "Вы стреляли по " + coordToString(row, col) + " -> " + resText);
            sendBoard(out, 2);

            return result == GameService.ShotResult.HIT || result == GameService.ShotResult.SUNK;
        }
    }

    private int[] askCoordinateFromConsole(String prompt, int playerId) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            int[] rc = parseCoord(input);
            if (rc == null) {
                System.out.println("Неверный формат координаты. Пример: A0");
                continue;
            }
            if (!game.canShoot(playerId, rc[0], rc[1])) {
                System.out.println("В эту клетку уже стреляли или она вне поля. Попробуйте ещё раз.");
                continue;
            }
            return rc;
        }
    }

    // Отправка текстового сообщения клиенту
    private void sendMessage(PrintWriter out, String text) {
        out.println(Action.MESSAGE.name() + ":" + text);
    }

    // Отправка доски заданного игрока
    private void sendBoard(PrintWriter out, int playerId) {
        String board = game.boardForPlayer(playerId);
        out.println(Action.BOARD.name());
        for (String line : board.split("\n")) {
            out.println(line);
        }
        out.println("END_BOARD");
    }

    // Преобразование строки типа A0 в координаты
    private static int[] parseCoord(String s) {
        if (s == null) return null;
        s = s.trim().toUpperCase();
        if (s.length() < 2 || s.length() > 3) return null;

        char rowChar = s.charAt(0);
        if (rowChar < 'A' || rowChar > 'J') return null;
        int row = rowChar - 'A';

        String colStr = s.substring(1);
        int col;
        try {
            col = Integer.parseInt(colStr);
        } catch (NumberFormatException e) {
            return null;
        }

        if (col < 0 || col > 9) return null;
        return new int[]{row, col};
    }

    private static String coordToString(int row, int col) {
        return "" + (char) ('A' + row) + col;
    }

    private static String resultToText(GameService.ShotResult result) {
        switch (result) {
            case MISS:
                return "Мимо";
            case HIT:
                return "Попадание";
            case SUNK:
                return "Корабль потоплен";
            case ALREADY_SHOT:
                return "Сюда уже стреляли";
            case INVALID:
                return "Неверный ход";
            default:
                return result.toString();
        }
    }
}
