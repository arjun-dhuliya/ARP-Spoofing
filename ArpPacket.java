/***
 *
 */
public class ArpPacket {
    int hardwareType;
    int protocolType;
    int hardwareAddressLength;
    int protocolAddresslength;
    int operation;
    byte[] SHA = new byte[6];//Sender hardware addr
    byte[] SPA = new byte[4];//IP addr
    byte[] THA = new byte[6];//Target hw addr
    byte[] TPA = new byte[4];//IP addr
    
    
    public ArpPacket(byte[] SHA, byte[] SPA) {
    	this.hardwareType = 1;
    	this.protocolType = 800;
    	this.hardwareAddressLength = 6;
    	this.protocolAddresslength = 4;
    	this.operation = 0;
		this.SHA = SHA;
		this.SPA = SPA;
	}
    
    public ArpPacket() {
		// TODO Auto-generated constructor stub
	}

    /***
     *
     * @return
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
                "TPA:" + arrayToDecimalString(this.TPA) + ",\n";
    }

    /***
     *
     * @param bytes
     * @return
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
     *
     * @param bytes
     * @return
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
