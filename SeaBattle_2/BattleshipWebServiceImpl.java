import javax.jws.WebService;

@WebService(
        endpointInterface = "BattleshipWebService",
        serviceName = "BattleshipService",
        portName = "BattleshipPort",
        targetNamespace = "http://battleship/"
)
public class BattleshipWebServiceImpl implements BattleshipWebService {

    private final GameService game = new GameServiceImpl();

    private int players = 0; 
    private int currentPlayer = 1;

    @Override
    public synchronized int registerPlayer() {
        if (players >= 2) return -1;
        players++;
        System.out.println("Игрок " + players + " подключён");
        return players;
    }

    @Override
    public synchronized String getBoard(int playerId) {
        return game.boardForPlayer(playerId);
    }

    @Override
    public synchronized String shoot(int playerId, String coord) {

        if (playerId != currentPlayer)
            return "Сейчас ход другого игрока!";

        int[] rc = parse(coord);
        if (rc == null) return "Неверный формат. Пример: A5";

        int r = rc[0], c = rc[1];

        if (!game.canShoot(playerId, r, c))
            return "В эту клетку уже стреляли!";

        GameService.ShotResult res = game.shoot(playerId, r, c);

        String txt = toText(res);

        // смена хода только при промахе
        if (res == GameService.ShotResult.MISS)
            currentPlayer = (currentPlayer == 1 ? 2 : 1);

        if (game.isGameOver())
            return txt + " Игра окончена! Победил игрок " + game.getWinner();

        return txt;
    }

    @Override
    public synchronized boolean isMyTurn(int playerId) {
        return currentPlayer == playerId;
    }

    @Override
    public synchronized boolean isGameOver() {
        return game.isGameOver();
    }

    @Override
    public synchronized int getWinner() {
        return game.getWinner();
    }

    private int[] parse(String coord) {
        if (coord == null) return null;
        coord = coord.trim().toUpperCase();
        if (coord.length() < 2 || coord.length() > 3) return null;

        char rowChar = coord.charAt(0);
        if (rowChar < 'A' || rowChar > 'J') return null;
        int row = rowChar - 'A';

        int col;
        try {
            col = Integer.parseInt(coord.substring(1));
        } catch (Exception e) {
            return null;
        }
        if (col < 0 || col > 9) return null;

        return new int[]{row, col};
    }

    private String toText(GameService.ShotResult res) {
        switch (res) {
            case MISS: return "Мимо";
            case HIT: return "Попадание";
            case SUNK: return "Корабль потоплен";
            case ALREADY_SHOT: return "Уже стреляли";
            default: return res.toString();
        }
    }
}
