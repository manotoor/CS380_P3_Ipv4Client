import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.io.BufferedReader;

/***********************************************************************************************
	
		IPV4 Packet  *************FROM WIKIPEDIA*************
		0		4		8		12	14	16		20		24		28		32
	0	|version|   IHL | 		DSCP|ECN|        Total Length           |
	32	|		Identification			| Flags	|	Fragment Offeset	|
	64	| Time To Live	|	Protocol	|		Header checkSum 		|
	96	| 						Source IP Address 						|
	128	|						Destination IP Address 					|
	160	| 							Options (Optional)					|
	192	|																|
	224	|																|
	256	|_______________________________________________________________|
	

	*************FROM WIKIPEDIA************* https://en.wikipedia.org/wiki/IPv4

	Version -
		Four Bit field, Always equal to 4 for IPv4
	IHL (Internet Header Length) -
		field has 4 bits, which is the number of 32-bit words. 
		Since an IPv4 header may contain a variable number of options, 
		this field specifies the size of the header (this also coincides with the offset to the data). 
		The minimum value for this field is 5,[11] which indicates a length of 5 × 32 bits = 160 bits = 20 bytes. 
		As a 4-bit field, the maximum value is 15 words (15 × 32 bits, or 480 bits = 60 bytes).

	Differentiated Services Code Point (DSCP)-
		Originally defined as the Type of service (ToS) field. 
		This field is now defined by RFC 2474 (updated by RFC 3168 and RFC 3260) for Differentiated services (DiffServ). 
		New technologies are emerging that require real-time data streaming and therefore make use of the DSCP field. 
		An example is Voice over IP (VoIP), which is used for interactive data voice exchange.

	Explicit Congestion Notification (ECN)-
		This field is defined in RFC 3168 and allows end-to-end notification of network congestion without dropping packets. 
		ECN is an optional feature that is only used when both endpoints support it and are willing to use it. 
		It is only effective when supported by the underlying network.

	Total Length -
		This 16-bit field defines the entire packet size in bytes, including header and data. 
		The minimum size is 20 bytes (header without data) and the maximum is 65,535 bytes. 
		All hosts are required to be able to reassemble datagrams of size up to 576 bytes, 
		but most modern hosts handle much larger packets. 
		Sometimes links impose further restrictions on the packet size, in which case datagrams must be fragmented. 
		Fragmentation in IPv4 is handled in either the host or in routers.

	Identification -
		This field is an identification field and is primarily used for uniquely identifying the group of fragments of a single IP datagram. 
		Some experimental work has suggested using the ID field for other purposes, 
		such as for adding packet-tracing information to help trace datagrams with spoofed source addresses,
		[12] but RFC 6864 now prohibits any such use.
	Flags -
		A three-bit field follows and is used to control or identify fragments. They are (in order, from most significant to least significant): 
		bit 0: Reserved; must be zero.[note 1]
		bit 1: Don't Fragment (DF)
		bit 2: More Fragments (MF)
		If the DF flag is set, and fragmentation is required to route the packet, then the packet is dropped. 
		This can be used when sending packets to a host that does not have sufficient resources to handle fragmentation. 
		It can also be used for Path MTU Discovery, either automatically by the host IP software, 
		or manually using diagnostic tools such as ping or traceroute. For unfragmented packets, the MF flag is cleared. 
		For fragmented packets, all fragments except the last have the MF flag set. 
		The last fragment has a non-zero Fragment Offset field, differentiating it from an unfragmented packet.
	Fragment Offset -
		The fragment offset field is measured in units of eight-byte blocks. 
		It is 13 bits long and specifies the offset of a particular fragment relative to the beginning of the original unfragmented IP datagram. 
		The first fragment has an offset of zero. 
		This allows a maximum offset of (213 – 1) × 8 = 65,528 bytes, 
		which would exceed the maximum IP packet length of 65,535 bytes with the header length included (65,528 + 20 = 65,548 bytes).
	Time To Live (TTL) -
		An eight-bit time to live field helps prevent datagrams from persisting (e.g. going in circles) on an internet. 
		This field limits a datagram's lifetime. It is specified in seconds, but time intervals less than 1 second are rounded up to 1. 
		In practice, the field has become a hop count—when the datagram arrives at a router, the router decrements the TTL field by one. 
		When the TTL field hits zero, the router discards the packet and typically sends an ICMP Time Exceeded message to the sender. 
		The program traceroute uses these ICMP Time Exceeded messages to print the routers used by packets to go from the source to the destination.
	Protocol -
		This field defines the protocol used in the data portion of the IP datagram. 
		The Internet Assigned Numbers Authority maintains a list of IP protocol numbers which was originally defined in RFC 790.
	Header Checksum -
		Main article: IPv4 header checksum
		The 16-bit checksum field is used for error-checking of the header. When a packet arrives at a router, the router calculates the checksum of the header and compares it to the checksum field. 
		If the values do not match, the router discards the packet. Errors in the data field must be handled by the encapsulated protocol. Both UDP and TCP have checksum fields. 
		When a packet arrives at a router, the router decreases the TTL field. Consequently, the router must calculate a new checksum. RFC 791 defines the checksum calculation:
		The checksum field is the 16-bit one's complement of the one's complement sum of all 16-bit words in the header. For purposes of computing the checksum, the value of the checksum field is zero.
		For example, consider hex 4500003044224000800600008C7C19ACAE241E2B16 (20 bytes IP header), using a machine which uses standard two's complement arithmetic:
		450016 + 003016 + 442216 + 400016 + 800616 + 000016 + 8C7C16 + 19AC16 + AE2416 + 1E2B16 = 0002BBCF (32-bit sum)
		000216 + BBCF16 = BBD116 = 10111011110100012 (1's complement 16-bit sum, formed by "end around carry" of 32-bit 2's complement sum)
		~BBD116 = 01000100001011102 = 442E16 (1's complement of 1's complement 16-bit sum)
		To validate a header's checksum the same algorithm may be used – the checksum of a header which contains a correct checksum field is a word containing all zeros (value 0):
		450016 + 003016 + 442216 + 400016 + 800616 + 442E16 + 8C7C16 + 19AC16 + AE2416 + 1E2B16 = 2FFFD16
		000216 + FFFD16 = FFFF16
		~FFFF16 = 000016
	Source address -
		This field is the IPv4 address of the sender of the packet. Note that this address may be changed in transit by a network address translation device.
	Destination address -
		This field is the IPv4 address of the receiver of the packet. As with the source address, this may be changed in transit by a network address translation device.
	Options -
		The options field is not often used. Note that the value in the IHL field must include enough extra 32-bit words to hold all the options (plus any padding needed to ensure that the header contains an integer number of 32-bit words). The list of options may be terminated with an EOL (End of Options List, 0x00) option; this is only necessary if the end of the options would not otherwise coincide with the end of the header. The possible options that can be put in the header are as follows: 
		Field
		Size (bits)
		Description
		Copied
		1
		Set to 1 if the options need to be copied into all fragments of a fragmented packet.
		Option Class
		2
		A general options category. 0 is for "control" options, and 2 is for "debugging and measurement". 1, and 3 are reserved.
		Option Number
		5
		Specifies an option.
		Option Length
		8
		Indicates the size of the entire option (including this field). This field may not exist for simple options.
		Option Data
		Variable
		Option-specific data. This field may not exist for simple options.
		Note: If the header length is greater than 5 (i.e., it is from 6 to 15) it means that the options field is present and must be considered.
		Note: Copied, Option Class, and Option Number are sometimes referred to as a single eight-bit field, the Option Type.
		Packets containing some options may be considered as dangerous by some routers and be blocked.[13]


*/

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
				//Total length without data is 20 bytes, but we add data length of 2 bytes so total for i = 0 is 22 bytes
				int totalLength = length + dataLength;
				packet = new byte[totalLength]
				//version = 4
				packet[0] = (byte)version;
				//hlen = 5 minimum
				packet[1] = (byte)
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