import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

/***
 *
 */
public class Router {

    static final HashMap<IP, MacAddress> ARP_TABLE = new HashMap<>();

    /***
     *
     * @param args, command line argument ignored
     */
    public static void main(String[] args) {
        ListeningThread listeningThread = new ListeningThread();
        new Thread(listeningThread).start();
    }

    /***
     *
     */
    public static class ListeningThread implements Runnable {
        private DatagramSocket socket;

        ListeningThread() {
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
                    } else {
                        forwardMessage(p.getData(), p.getLength());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /***
         *
         * @param data
         * @param length
         */
        private void forwardMessage(byte[] data, int length) {

        }
    }

}
