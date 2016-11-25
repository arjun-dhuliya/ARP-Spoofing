/***
 *
 */
public class ArpPacket {
    int hardwareType;
    int protocolType;
    int hardwareAddressLength;
    int protocolAddresslength;
    int operation;
    byte[] SHA = new byte[6];
    byte[] SPA = new byte[4];
    byte[] THA = new byte[6];
    byte[] TPA = new byte[4];

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
                "SHA:" + arrayToHexString(this.SHA) + ",\n" +
                "SPA:" + arrayToDecimalString(this.SPA) + ",\n" +
                "THA:" + arrayToHexString(this.THA) + ",\n" +
                "TPA:" + arrayToDecimalString(this.TPA) + ",\n";
    }

    /***
     *
     * @param bytes
     * @return
     */
    private String arrayToDecimalString(byte[] bytes) {
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
    private String arrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int val;
            val = Integer.parseInt(String.format("%02x", b), 16);
            sb.append(Integer.toHexString(val)).append('.');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
