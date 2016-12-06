/***
 *
 */
public class ArpPacket {
    int hardwareType;               //hardware type field of Arp Packet
    int protocolType;               //protocol type field of Arp Packet
    int hardwareAddressLength;      //hardware Address length field of Arp Packet
    int protocolAddresslength;      //protocol address length field of Arp Packet
    int operation;                  // operation field of Arp packet
    byte[] SHA = new byte[6];       //Sender hardware address field of Arp packet
    byte[] SPA = new byte[4];       //Sender protocol address field of Arp packet
    byte[] THA = new byte[6];       //Sender hardware address field of Arp packet
    byte[] TPA = new byte[4];       //IP addr
    byte[] SLPA = new byte[4];       //Sender local protocol address field of Arp packet
    int PortNumber;

    /***
     * Constructor
     * @param SHA, Sender hardware address
     * @param SPA, Sender Protocol address
     */
    public ArpPacket(byte[] SHA, byte[] SPA) {
    	this.hardwareType = 1;
    	this.protocolType = 800;
    	this.hardwareAddressLength = 6;
    	this.protocolAddresslength = 4;
    	this.operation = 0;
		this.SHA = SHA;
		this.SPA = SPA;
	}

    /***
     * Constructor
     */
    public ArpPacket() {
    	this.hardwareType = 1;
    	this.protocolType = 800;
    	this.hardwareAddressLength = 6;
    	this.protocolAddresslength = 4;
    	this.operation = 0;
	}

    /***
     * String representation of Arp packet
     * @return String representation of Arp packet
     */
    @Override
    public String toString() {

        return "HwType:" + this.hardwareType + ",\n" +
                "PType:" + this.protocolType + ",\n" +
                "Hwlen:" + this.hardwareAddressLength + ",\n" +
                "Plen:" + this.protocolAddresslength + ",\n" +
                "oper:" + this.operation + ",\n" +
                "SHA:" + arrayToHexString(this.SHA, '.') + ",\n" +
                "SPA:" + arrayToDecimalString(this.SPA) + ",\n" +
                "THA:" + arrayToHexString(this.THA, '.') + ",\n" +
                "TPA:" + arrayToDecimalString(this.TPA) + ",\n" +
                "SLPA:" + arrayToDecimalString(this.SLPA) + ",\n" +
                "Port:" + this.PortNumber;
    }

    /***
     * provided with a array convert to a string with '.' separation
     * @param bytes, byte array
     * @return String value '.' separated
     */
    public static String arrayToDecimalString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int val;
            val = Integer.parseInt(String.format("%02x", b), 16);
            sb.append(val).append('.');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /***
     * provided with a array convert to a hex string with '.' separation
     * @param bytes, byte array
     * @return string value
     */
    public static String arrayToHexString(byte[] bytes, char delimeter) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int val;
            val = Integer.parseInt(String.format("%02x", b), 16);
            sb.append(Integer.toHexString(val)).append(delimeter);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
