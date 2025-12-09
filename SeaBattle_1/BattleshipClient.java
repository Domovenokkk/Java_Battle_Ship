import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class BattleshipClient {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new BattleshipClient().run();
    }

    private void run() {
        System.out.println("=== Морской бой (Клиент, Игрок 2) ===");
        System.out.println("Подключение к " + HOST + ":" + PORT + "...");

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Подключено. Ожидание инструкций от сервера...");

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(Action.MESSAGE.name() + ":")) {
                    String msg = line.substring((Action.MESSAGE.name() + ":").length());
                    System.out.println(msg);
                } else if (line.equals(Action.BOARD.name())) {
                    readAndPrintBoard(in);
                } else if (line.equals(Action.YOUR_TURN.name())) {
                    handleMyTurn(out);
                } else if (line.startsWith(Action.GAME_OVER.name() + ":")) {
                    handleGameOver(line);
                    break;
                } else {
                    // на всякий случай печатаем всё непонятное
                    System.out.println(line);
                }
            }

        } catch (IOException e) {
            System.out.println("Ошибка клиента: " + e.getMessage());
        }
    }

    private void readAndPrintBoard(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            if ("END_BOARD".equals(line)) break;
            sb.append(line).append("\n");
        }
        System.out.print(sb.toString());
    }

    private void handleMyTurn(PrintWriter out) {
        while (true) {
            System.out.print("Введите клетку для выстрела (например A0): ");
            String input = scanner.nextLine();
            int[] rc = parseCoord(input);
            if (rc == null) {
                System.out.println("Неверный формат координаты. Пример: A0");
                continue;
            }
            out.println("SHOT " + input.trim().toUpperCase());
            break; // дальше ждём ответов от сервера
        }
    }

    private void handleGameOver(String line) {
        // формат: GAME_OVER:winner:текст
        String[] parts = line.split(":", 3);
        int winner = 0;
        if (parts.length >= 2) {
            try {
                winner = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        String text = parts.length == 3 ? parts[2] : "Игра окончена.";

        System.out.println(text);
        if (winner == 2) {
            System.out.println("Вы победили! 🎉");
        } else if (winner == 1) {
            System.out.println("Вы проиграли.");
        }
    }

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
}
