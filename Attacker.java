import java.io.IOException;
import java.net.*;
import java.util.HashMap;

/***
 *
 */
public class Attacker {
    static HashMap<String, User.Router_Info> default_gateway = new HashMap<>();
    private static DatagramSocket sendingSocket;
    private static DatagramSocket socket;
    private static String Router_IP;
    private static int Router_Port;
    private static String ip;
    private static int port;
    HashMap<String, Victim> victims = new HashMap<>();

    /***
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            sendingSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (args.length == 2) {
            default_gateway.put("192.168.1.1", new User.Router_Info(args[0], Integer.parseInt(args[1]), ""));
        } else if (args.length == 3) {
            default_gateway.put("192.168.1.1", new User.Router_Info(args[0], Integer.parseInt(args[1]), args[2]));
        }
        UserListeningThread thread = new UserListeningThread();
        new Thread(thread).start();
    }

    private static class UserListeningThread implements Runnable {
        byte[] bytes;
        DatagramPacket p;

        UserListeningThread() {
            bytes = new byte[1024];
            p = new DatagramPacket(bytes, 0, bytes.length);
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        /***
         *
         * @param text
         */
        private void sendMessage(String text) {
            byte[] bytes = new byte[text.length() + 1];
            bytes = text.getBytes();
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(ip);
                DatagramPacket p = new DatagramPacket(bytes, bytes.length, inetAddress, port);
                sendingSocket.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /***
         * 0:initial_msg 1:send_arp_msg 2:send_usr_msg
         *
         * @param arp_init
         * @throws Exception
         */
        void send_arp_init_msg(DatagramSocket arp_init) throws Exception {
            Router_IP = Attacker.default_gateway.get("192.168.1.1").Router_IP;
            Router_Port = Attacker.default_gateway.get("192.168.1.1").Router_Port;
            InetAddress inetAddress = InetAddress.getByName(Router_IP);
            ArpPacket init_pkt = new ArpPacket(
                    NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress(),
                    InetAddress.getLocalHost().getAddress());
            byte[] byte_stream = ArpPacketAnalyzer.toBytes(init_pkt, 0);
            DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Router_Port);
            arp_init.send(p);
            p = new DatagramPacket(byte_stream, byte_stream.length);
            arp_init.receive(p);
            ArpPacket pkt = ArpPacketAnalyzer.analyzePacket(p.getData(), p.getLength());
            User.Router_Info router_info = Attacker.default_gateway.get("192.168.1.1");
            router_info.Router_Port = pkt.PortNumber;
            router_info.Router_IP = ArpPacket.arrayToDecimalString(pkt.TPA);
            router_info.Router_Mac = ArpPacket.arrayToHexString(pkt.THA, ':');
            System.out.println("Received:\n" + pkt);
        }

        @Override
        public void run() {
            try {
                String s = "Attacker Listening at Ip, Port :" + InetAddress.getLocalHost().getHostAddress() + ", "
                        + socket.getLocalPort();
                System.out.println(s);
                try {
                    send_arp_init_msg(socket);
                    getTable(socket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (true) {
                    p = new DatagramPacket(bytes, 0, bytes.length);
                    socket.receive(p);
                    byte[] data = p.getData();
                    if (data[0] == 0) {
                        ArpPacket arpPacket = ArpPacketAnalyzer.analyzePacket(data, p.getLength());
                        System.out.println(arpPacket);
                    } else {
                        String msg = "\nfriend:";
                        msg += new String(data, 0, p.getLength());
                        System.out.println(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /***
         *
         * @param socket
         */
        private void getTable(DatagramSocket socket) {
            Router_IP = Attacker.default_gateway.get("192.168.1.1").Router_IP;
            Router_Port = Attacker.default_gateway.get("192.168.1.1").Router_Port;
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getByName(Router_IP);

                ArpPacket init_pkt = new ArpPacket(
                        NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress(),
                        InetAddress.getLocalHost().getAddress());
                byte[] byte_stream = ArpPacketAnalyzer.toBytes(init_pkt, 2);
                DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Router_Port);
                socket.send(p);
                ArpPacket pkt = ArpPacketAnalyzer.analyzePacket(p.getData(), p.getLength());
                User.Router_Info router_info = Attacker.default_gateway.get("192.168.1.1");
                router_info.Router_Port = pkt.PortNumber;
                router_info.Router_IP = ArpPacket.arrayToDecimalString(pkt.TPA);
                router_info.Router_Mac = ArpPacket.arrayToHexString(pkt.THA, ':');
                System.out.println("Received:\n" + pkt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /***
     *
     */
    static class Victim {
        String ip;
        int port;

        Victim(String ip, String port) {
            this.ip = ip;
            this.port = Integer.parseInt(port);
        }

        @Override
        public String toString() {
            return "Victim{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    '}';
        }
    }
}
