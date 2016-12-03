import java.util.HashMap;

/***
 *
 */
public class User {

    static HashMap<String, Router_Info> default_gateway;


    /***
     *
     * @param args, command line arguments are ignored
     */
    public static void main(String args[]) {
        default_gateway = new HashMap<>();
        if(args.length == 2) {
            default_gateway.put("192.168.1.1", new Router_Info(args[0], Integer.parseInt(args[1]), ""));
        }else if (args.length == 3){
            default_gateway.put("192.168.1.1", new Router_Info(args[0], Integer.parseInt(args[1]), args[2]));
        }
        ChatBox c = new ChatBox();
    }


    /***
     *
     */
    static class Router_Info{
        String Router_IP;
        int Router_Port;
        String Router_Mac;

        /***
         *
         * @param router_IP
         * @param router_Port
         * @param router_Mac
         */
        public Router_Info(String router_IP, int router_Port, String router_Mac) {
            Router_IP = router_IP;
            Router_Port = router_Port;
            Router_Mac = router_Mac;
        }
    }
}
