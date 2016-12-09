import java.util.HashMap;

/***
 * class representation of a ARP User, it uses chat box application to chat with other users in virtual sub network
 */
public class User {

    static HashMap<String, Router_Info> default_gateway;    // User has a default gateway to communicate


    /***
     * instantiates a user
     * @param args, router ip and router port number are passed as parameters to program
     */
    public static void main(String args[]) {
        default_gateway = new HashMap<>();
        if (args.length == 2) {
            default_gateway.put("192.168.1.1", new Router_Info(args[0], Integer.parseInt(args[1]), ""));
        } else if (args.length == 3) {
            default_gateway.put("192.168.1.1", new Router_Info(args[0], Integer.parseInt(args[1]), args[2]));
        }
        ChatBox c = new ChatBox();
    }


    /***
     * Class representation of Router info
     */
    static class Router_Info {
        String Router_IP;
        int Router_Port;
        String Router_Mac;

        /***
         * Constructor
         * @param router_IP, ip
         * @param router_Port, port
         * @param router_Mac, mac address
         */
        Router_Info(String router_IP, int router_Port, String router_Mac) {
            Router_IP = router_IP;
            Router_Port = router_Port;
            Router_Mac = router_Mac;
        }
    }
}
