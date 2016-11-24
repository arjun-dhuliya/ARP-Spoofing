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
}
