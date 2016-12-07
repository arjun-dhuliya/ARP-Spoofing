import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;

/***
 * Class Representation of a ARP SPOOF attack
 */
public class Attacker {
    private static final HashMap<String, User.Router_Info> default_gateway = new HashMap<>();
    private static DatagramSocket sendingSocket;
    private static DatagramSocket socket;
    private static String Router_IP;
    private static int Router_Port;
    HashMap<String, Victim> victims = new HashMap<>();
    private static byte[] ownIP;
    private static byte[] ownMAC;
    private static int ownPort;

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

    /***
     *
     */
    private static class UserListeningThread implements Runnable {
        final byte[] bytes;
        DatagramPacket p;
        private String ip;
        private int port;
        private static Victim mainVictim;

        /***
         *
         */
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
            byte[] bytes;
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
            ownMAC = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            ownIP = InetAddress.getLocalHost().getAddress();
            ArpPacket init_pkt = new ArpPacket(ownMAC, ownIP);
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

        /***
         *
         */
        @Override
        public void run() {
            try {
                ownPort = socket.getLocalPort();
                String s = "Attacker Listening at Ip, Port :" + InetAddress.getLocalHost().getHostAddress() + ", "
                        + ownPort;
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
                        System.out.println("Recieved ARP:\n" + arpPacket);
                    } else {
                        String msg;
                        msg = bytesToIp(data, 1)+": ";
                        msg += new String(data, 5, p.getLength()-5)+"\n";
                        System.out.println(msg);
                        String recivedFrom = p.getAddress().getHostAddress();
                        String victimIp = bytesToIp(mainVictim.ip, 0);
                        if(recivedFrom.equals(victimIp) ){
                            System.out.println("Forwarding to Router");
                            User.Router_Info router_info = default_gateway.get("192.168.1.1");
                            p.setAddress(InetAddress.getByName(router_info.Router_IP));
                            p.setPort(router_info.Router_Port);
                        }else {
                            System.out.println("Forwarding to " + mainVictim);
                            p.setAddress(InetAddress.getByAddress(mainVictim.ip));
                            p.setPort(mainVictim.port);
                        }
                        socket.send(p);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /***
         *
         * @param data
         * @param start
         * @return
         */
        private String bytesToIp(byte[] data, int start) {
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < start + 4; i++) {
                if (data[i] >= 0) sb.append(data[i]);
                else sb.append(data[i] + 256);
                sb.append('.');
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }

        /***
         * Request Router for ARP table info
         * @param socket, listening soclet of the attacker
         */
        private void getTable(DatagramSocket socket) {
            Router_IP = Attacker.default_gateway.get("192.168.1.1").Router_IP;
            Router_Port = Attacker.default_gateway.get("192.168.1.1").Router_Port;
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(Router_IP);
                ArpPacket init_pkt = new ArpPacket(
                        NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress(),
                        InetAddress.getLocalHost().getAddress());
                byte[] byte_stream = ArpPacketAnalyzer.toBytes(init_pkt, 2);
                DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Router_Port);
                socket.send(p);
                p = new DatagramPacket(byte_stream, byte_stream.length);
                socket.receive(p);
                int len = Integer.parseInt(String.valueOf(p.getData()[0]));
//                System.out.println(new String(p.getData()) + "," + p.getLength());
                Victim[] victims = new Victim[len];
                System.out.println("ARP TABLE: ");
                System.out.println("---------------------------------------------------------------------------------------");
                for (int i = 0; i < len; i++) {
//                    System.out.println("Waiting for more: " + (len - i));
                    p = new DatagramPacket(byte_stream, byte_stream.length);
                    socket.receive(p);
                    ArpPacket arpPacket = ArpPacketAnalyzer.analyzePacket(p.getData(), p.getLength());
//                    System.out.println("table entry " + (i + 1) + ":\n" + arpPacket);
                    victims[i] = new Victim(arpPacket.TPA,arpPacket.PortNumber,arpPacket.SLPA);
                    printEntry(arpPacket,i);
                }
                System.out.println("Got above table");
                Scanner scanner = new Scanner(System.in);
                System.out.println("Enter the id of your target:");
                init_pkt = new ArpPacket(
                        NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress(),
                        InetAddress.getLocalHost().getAddress());
                int id = Integer.parseInt(scanner.nextLine());
                init_pkt.SLPA = victims[id].SLPA;
                mainVictim = victims[id];
                byte_stream = ArpPacketAnalyzer.toBytes(init_pkt, 1);
                p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress, Router_Port);
                socket.send(p);
                String k = "192.168.1.";
                ArpPacket response = new ArpPacket();
                response.THA = ownMAC;
                response.TPA = ownIP;
                response.PortNumber = Integer.parseInt(String.valueOf(ownPort));
                sendArpMessage(response, bytesToIp(mainVictim.ip,0), mainVictim.port+"", 0);
                System.out.println("Sent spoofed messages");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /***
         *
         * @param text
         */
        private void sendArpMessage(ArpPacket pkt, String IP, String Port, int mode) {
            byte[] byte_stream = ArpPacketAnalyzer.toBytes(pkt, mode);
            InetAddress inetAddress;
            try {
                inetAddress = InetAddress.getByName(IP);
                DatagramPacket p = new DatagramPacket(byte_stream, byte_stream.length, inetAddress,
                        Integer.parseInt(Port));
                socket.send(p);
                System.out.println("ARP packet sent to " + IP + " " + Port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void printEntry(ArpPacket arpPacket, int i) {
            System.out.println(i+" "+arpPacket.arrayToDecimalString(arpPacket.SLPA)+" "
                    +arpPacket.arrayToHexString(arpPacket.THA,':')+" "+arpPacket.PortNumber);
            System.out.println("---------------------------------------------------------------------------------------");
        }

        /***
         *
         * @param ip
         * @return
         */
        byte[] ipToBytes(String ip) {
            String[] splits = ip.split("\\.");
            byte[] data = new byte[splits.length];
            for (int i = 0; i < splits.length; i++) {
                int v = Integer.parseInt(splits[i]);
                if (v <= 127)
                    data[i] = (byte) v;
                else
                    data[i] = (byte) (v - 256);

            }
            return data;
        }
    }


    /***
     *
     */
    static class Victim {
        final byte[] ip;
        final int port;
        final byte[] SLPA;

        Victim(byte[] ip, int port, byte[] slpa) {
            this.ip = ip;
            this.port = port;
            this.SLPA = slpa;
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
