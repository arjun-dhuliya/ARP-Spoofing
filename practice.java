import java.net.InetAddress;
import java.net.NetworkInterface;

public class practice {

	public static void main(String[] args) throws Exception{
//		System.out.println(InetAddress.getByName("127.0.0.1"));
		InetAddress addr = InetAddress.getLocalHost();
		System.out.println(addr);
		System.out.println(NetworkInterface.getByInetAddress(addr).getHardwareAddress().toString());
	}

}
