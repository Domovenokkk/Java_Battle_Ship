public interface GameService {

    enum ShotResult {
        MISS,
        HIT,
        SUNK,
        ALREADY_SHOT,
        INVALID
    }

    ShotResult shoot(int playerId, int row, int col);

    boolean canShoot(int playerId, int row, int col);

    boolean isGameOver();

    int getWinner();

    String boardForPlayer(int playerId);
}
