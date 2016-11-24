import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/***
 *
 */
public class ListeningThread implements Runnable {
    private static DatagramSocket socket;

    /*

     */
    static {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /***
     *
     */
    @Override
    public void run() {
        try {
            System.out.println("Router Listening at Ip, Port :"
                    + InetAddress.getLocalHost().getHostAddress() + ", " + socket.getLocalPort());
            while (true) {
                byte[] bytes = new byte[1024];
                DatagramPacket p = new DatagramPacket(bytes, bytes.length);
                socket.receive(p);
                byte[] data = p.getData();
                if (data[0] == 1) {
                    ArpPacketAnalyzer.analyzePacket(bytes, p.getLength());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
