/***
 *
 */
public class IP {
    private final String ip;
    private final int[] octets;
    private final boolean[] binary;
    private boolean binaryString[];

    /***
     *
     * @param ip
     */
    public IP(String ip) {
        this.ip = ip;
        String[] splits = ip.split("\\.");
        octets = new int[splits.length];
        for (int i = 0; i < octets.length; i++) {
            octets[i] = Integer.parseInt(splits[i]);
        }
        binary = toBooleanArray(ip);
    }

    public static void main(String[] args) {
        IP obj = new IP("192.168.1.1");
        boolean[] binary = obj.getBinary();
        String sVal = obj.getStringVal();
        int[] octets = obj.getOctets();
        System.out.println(obj.toBinaryString());
    }

    /***
     *
     * @param ip
     * @return
     */
    private boolean[] toBooleanArray(String ip) {
        String[] splits = ip.split("\\.");
        boolean[] bits = new boolean[splits.length * 8];
        for (int i = 0; i < bits.length; ) {
            String binaryString = Integer.toBinaryString(Integer.parseInt(splits[(i + 1) / 8]));
            i += 8 - binaryString.length();
            for (int j = 0; j < binaryString.length(); j++) {
                if (binaryString.charAt(j) == '1')
                    bits[i] = true;
                i++;
            }
        }
        return bits;
    }

    /***
     *
     */
    public String toBinaryString() {
        StringBuilder sb = new StringBuilder(32);
        for (boolean aBinary : binary) {
            sb.append(aBinary ? '1' : '0');
        }
        return sb.toString();
    }

    /***
     *
     * @return
     */
    public String getStringVal() {
        return ip;
    }

    /***
     *
     * @return
     */
    public int[] getOctets() {
        return octets;
    }

    /***
     *
     * @return
     */
    public boolean[] getBinary() {
        return binary;
    }
}
