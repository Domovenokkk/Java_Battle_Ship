import javax.xml.ws.Endpoint;

public class BattleshipServicePublisher {
    public static void main(String[] args) {
        String url = "http://localhost:8080/battleship";
        System.out.println("SOAP сервер запущен: " + url);
        Endpoint.publish(url, new BattleshipWebServiceImpl());
    }
}
