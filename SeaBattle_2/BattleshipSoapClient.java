import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Scanner;

public class BattleshipSoapClient {

    public static void main(String[] args) throws Exception {

        URL wsdl = new URL("http://localhost:8080/battleship?wsdl");

        // Должно совпадать с targetNamespace и serviceName в @WebService
        QName qname = new QName("http://battleship/", "BattleshipService");
        Service serv = Service.create(wsdl, qname);

        BattleshipWebService port = serv.getPort(BattleshipWebService.class);

        Scanner sc = new Scanner(System.in);

        int playerId = port.registerPlayer();
        if (playerId == -1) {
            System.out.println("Уже есть 2 игрока!");
            return;
        }

        System.out.println("Вы игрок " + playerId);

        while (!port.isGameOver()) {

            while (!port.isMyTurn(playerId)) {
                System.out.print("\rЖдём ход противника");
                for (int i = 0; i < 3; i++) {
                    System.out.print(".");
                    Thread.sleep(500);
                    if (port.isMyTurn(playerId))
                        break;
                    }
                System.out.print("\r                             \r"); // Очистка строки
            }

            System.out.println("\n--- Ваш ход ---");
            System.out.println(port.getBoard(playerId));

            System.out.print("Введите клетку (например A5): ");
            String cell = sc.nextLine();

            System.out.println(port.shoot(playerId, cell));
        }

        System.out.println("Игра окончена! Победитель: игрок " + port.getWinner());
    }
}
