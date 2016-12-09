/***
 * ARP Packet Analyzer class, analyzes a byte array into ARP fields and also converts the object to byte array
 * @author Arjun Dhuliya, Shailesh Vajpayee
 */
class ArpPacketAnalyzer {

    /***
     * Analyzes bytes to create an ARP PACKET Object
     * @param bytes, bytes of data
     * @return ArpPacket object
     */
    static ArpPacket analyzePacket(byte[] bytes) {
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
        current += info.protocolAddresslength;
        info.PortNumber = ((bytes[current++] & 0xff) << 8) | (bytes[current++] & 0xff);
        System.arraycopy(bytes, current, info.SLPA, 0, 4);
        return info;
    }

    /***
     * create a byte array of ArpPacket
     * @param obj, ArpPacket
     * @return byte array
     */
    static byte[] toBytes(ArpPacket obj, int mode) {
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
        data[current] = (byte) (obj.PortNumber & 0xFF);
        return data;
    }

    /***
     * store byte array inside another byte array
     * @param data, destination array
     * @param source, source array
     * @param from, from which index in destination we should start copying
     * @return index of next index that should be used
     */
    private static int storeIntoArray(byte[] data, byte[] source, int from) {
        System.arraycopy(source, 0, data, from, source.length);
        return from + source.length;
    }

}
