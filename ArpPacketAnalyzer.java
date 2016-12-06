/***
 *
 */
public class ArpPacketAnalyzer {

    /***
     *
     * @param bytes
     * @param length
     * @return
     */
    public static ArpPacket analyzePacket(byte[] bytes, int length) {
        ArpPacket info = new ArpPacket();
        int current = 1;
        info.hardwareType = ((bytes[current++] & 0xff) << 8) | (bytes[current++] & 0xff);
        info.protocolType = ((bytes[current++] & 0xff) << 8) | (bytes[current++] & 0xff);
        info.hardwareAddressLength = Integer.parseInt(bytes[current++] + "", 16);
        info.protocolAddresslength = Integer.parseInt(bytes[current++] + "", 16);
        info.operation = ((bytes[current++] & 0xff) << 8) | (bytes[current++] & 0xff);
        System.arraycopy(bytes, current, info.SHA, 0, 6);
        current += 6;
        System.arraycopy(bytes, current, info.SPA, 0, 4);
        current += 4;
        System.arraycopy(bytes, current, info.THA, 0, 6);
        current += 6;
        System.arraycopy(bytes, current, info.TPA, 0, 4);
        current+=info.protocolAddresslength;
        info.PortNumber = ((bytes[current++] & 0xff) << 8) | (bytes[current++] & 0xff);
        System.arraycopy(bytes, current, info.SLPA, 0, 4);
        return info;
    }

    /***
     *
     * @param obj
     * @return
     */
    public static byte[] toBytes(ArpPacket obj, int mode) {
        byte[] data = new byte[35];
        int current = 0;
        data[current++] = (byte) mode;
        data[current++] = (byte) ((obj.hardwareType >> 8) & 0xFF);
        data[current++] = (byte) (obj.hardwareType & 0xFF);
        data[current++] = (byte) ((obj.protocolType >> 8) & 0xFF);
        data[current++] = (byte) (obj.protocolType & 0xFF);
        data[current++] = (byte) obj.hardwareAddressLength;
        data[current++] = (byte) obj.protocolAddresslength;
        data[current++] = (byte) ((obj.operation >> 8) & 0xFF);
        data[current++] = (byte) (obj.operation & 0xFF);
        current = storeIntoArray(data, obj.SHA, current);
        current = storeIntoArray(data, obj.SPA, current);
        current = storeIntoArray(data, obj.THA, current);
        current = storeIntoArray(data, obj.TPA, current);
        data[current++] = (byte) ((obj.PortNumber >> 8) & 0xFF);
        data[current++] = (byte) (obj.PortNumber & 0xFF);
        current = storeIntoArray(data,obj.SLPA,current);
        return data;
    }

    /***
     *
     * @param data
     * @param source
     * @param from
     * @return
     */
    private static int storeIntoArray(byte[] data, byte[] source, int from) {
        System.arraycopy(source, 0, data, from, source.length);
        return from + source.length;
    }

    /***
     *
     * @param args
     */
    public static void main(String args[]) {
        ArpPacket p = new ArpPacket();
        p.hardwareType = 1;
        p.protocolType = 800;
        p.hardwareAddressLength = 6;
        p.protocolAddresslength = 4;
        p.operation = 1;
        p.SHA = new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255};
        p.SPA = new byte[]{(byte) 192, (byte) 168, (byte) 1, (byte) 6};
        p.THA = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
        p.TPA = new byte[]{(byte) 192, (byte) 168, (byte) 1, (byte) 0};
        p.PortNumber = 8888;
        byte[] bytes = ArpPacketAnalyzer.toBytes(p,0);
        ArpPacket arpPacket = ArpPacketAnalyzer.analyzePacket(bytes, bytes.length);
        System.out.println(arpPacket);
    }

}
