import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService(
        targetNamespace = "http://battleship/"
)
public interface BattleshipWebService {

    @WebMethod
    int registerPlayer();

    @WebMethod
    String getBoard(int playerId);

    @WebMethod
    String shoot(int playerId, String coord);

    @WebMethod
    boolean isMyTurn(int playerId);

    @WebMethod
    boolean isGameOver();

    @WebMethod
    int getWinner();
}
