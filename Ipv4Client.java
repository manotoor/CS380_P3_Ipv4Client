import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.io.BufferedReader;

public class Ipv4Client{
	public static void main(String[] args){
		try(Socket socket = new Socket("18.221.102.182", 38003)){
			System.out.println("Connected to Server.");

			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(isr);

			//get the dest address of server
			byte[] destAddress = socket.getInetAddress().getAddress();
			//Personal IP address (can use local host)
			byte[] srcAddress = {127,0,0,1};


			// Initializing variables for array
			//IPv4 always uses version 4
			int version = 4;
			//Minimum Value for this field is 5( can be variable in size but 5lines * 32 bits = 160 bits or 20 bytes)
			byte hlen = 5;
			//Start at 2 increment by x2 each time
			int dataLength = 2;
			//length of header (20 bytes)
			int length = 20;
			//Array for each field of IPv4
			byte[] packet;

			for(int i =0; i < 12;i++){
				//version = 4
				packet[0] = (byte)version;
				//hlen = 5 minimum
				//TOS(Do not Implement)
				//Length
				//Ident(Do not Implement)
				//Flags(Implement assuming no fragmentation)
				//Offset(Do not Implement)
				//TTL(Implement assuming every packet has a TTL of 50)
				//Protocol(Implement assuming TCP for all packets)
				//Checksum
				//Source Address(Implement with an IP address of your choice)
				//Destination Address( Implement using the IP address of the server )
				//Options/Pad(Ignore (do not put in header)
				//Data(Implement using zeros or random data)
			}
		}catch(Exception e){
			System.out.println("Uh oh! Looks like something went wrong!");
		}
		System.out.println("Disconnected from Server.");
	}
	private static short checksum(byte[] b){
		//Initialize Sum
		int sum = 0;
		//int i = 0;
		//Loop through bytes
		for(int i =0; i < b.length-1; i= i+2){
			//take upper shift 4 bits AND wit
			byte upper = b[i];
			byte lower = b[i+1];
			//upper = (byte)(upper << 8 & 0xFF00);
			//lower = (byte)(lower & 0xFF);
			int result = ((upper << 8 & 0xFF00) + (lower & 0x00FF));
			//sum = sum + ((firstHalf << 8 & 0xFF00) + (secondHalf & 0xFF));
			//add to sum
			sum = sum + (result);
			//check to make sure no overflow
			if ((sum & 0xFFFF0000) > 0) {
				sum &= 0xFFFF;
				sum++;
			}
		}
		//For Odd
		if(b.length %2 == 1){
			//add odd bit to sum
			sum = sum + ((b[b.length-1] << 8) & 0xFF00);
			
			// Check overflow
			if((sum & 0xFFFF0000) > 0) {
				sum &= 0xFFFF;
				sum++;
			}
		}
		//return sum
		return (short)~(sum & 0xFFFF);
	}
}