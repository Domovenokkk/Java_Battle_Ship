public interface GameService {

    enum ShotResult {
        MISS,
        HIT,
        SUNK,
        ALREADY_SHOT,
        INVALID
    }

    /**
     * Выстрел игрока playerId (1 или 2) по клетке row, col.
     */
    ShotResult shoot(int playerId, int row, int col);

    /**
     * Можно ли стрелять в эту клетку (игрок сюда ещё не стрелял и координата валидна).
     */
    boolean canShoot(int playerId, int row, int col);

    /**
     * Игра закончена?
     */
    boolean isGameOver();

    /**
     * Победитель: 0 – ещё нет, 1 или 2 – игрок.
     */
    int getWinner();

    /**
     * Строковое представление досок для игрока:
     *  - его поле с кораблями и попаданиями/промахами противника
     *  - его выстрелы по противнику.
     */
    String boardForPlayer(int playerId);
}
