package com.elliptic.curve.crypt;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Elliptic {
	
	public static BigInteger prime; 		//The prime number recommended per SECP256K1
	public static BigInteger N;  			//The number of points in the field 
	public static BigInteger Gx; 			//The x coordinate of the generator point
	public static BigInteger Gy; 			//The y coordinate of the generator point
	
	public static BigInteger private_key; 	//This is the randomly generated secret private key
	public static BigInteger[] public_key; 	//This is generated by private key * generator point
	
	public static BigInteger dynamic_random_number; //This is for illustration purposes, I can change this to dynamic later
	public static BigInteger static_random_number;  //This is how we should use random numbers in real life ECC
	
	//Variables for signing and verification
	public static String Message; 
	public static BigInteger HashOfMessage;
	
	
	//All values recommended by NIST and SEC
	//http://www.secg.org/sec2-v2.pdf
	//Per SECp256k1, the recommended Gx is 0x79BE667E F9DCBBAC 55A06295 CE870B07 029BFCDB 2DCE28D9 59F2815B 16F81798
	//Solving for y, the recommended Gy is 0x483ada77 26a3c465 5da4fbfc 0e1108a8 fd17b448 a6855419 9c47d08f fb10d4b8L
	//The curve recommended by SEC is y = x^3 + ax + b; where a = 0, and b = 7
	public Elliptic (){
		
		BigInteger p = new BigInteger("2");
		
		//The prime number recommended by SEC
		prime = p.pow(256).subtract(p.pow(32)).subtract(p.pow(9)).subtract(p.pow(8)).subtract (p.pow(7)).subtract (p.pow(6))
				.subtract(p.pow(4)).subtract(p.pow(0));
		
		//Number of points in the field
		N  = new BigInteger("115792089237316195423570985008687907852837564279074904382605163141518161494337");
		
		//Generator point x and y coordinates
		Gx = new BigInteger("55066263022277343669578718895168534326250603453777594175500187360389116729240");
		Gy = new BigInteger("32670510020758816978083085130507043184471273380659243275938904335757337482424"); 
		
		//Randomly chosen private key, and about to be generated public key
		private_key = new BigInteger("75263518707598184987916378021939673586055614731957507592904438851787542395619");  
		public_key = null;
		
		//Static random number for illustration purposes:
		static_random_number = new BigInteger("52710924008896426492537502351405011671837583047872910411571728056743601538422");
		
		//Actually chosen random number
		dynamic_random_number = generate_random_number();
	}
	
	
	//Display all data about ECC class
	public void display_data(){
		
		System.out.println("------------------------------------------------------------------------------");
		System.out.println("The curve recommended by SEC is Y = X^3 + AX + B");
		System.out.println("A constant value= 0 \n B constant value = 7");
		System.out.println("------------------------------------------------------------------------------");
		
		System.out.println("Prime number as recommended by SEC:");
		System.out.println(prime);
		System.out.println("http://www.secg.org/sec2-v2.pdf");
		System.out.println("------------------------------------------------------------------------------");
		
		System.out.println("Generator point as recommended by SEC: \n GP-Xcoordinate: \n(" + Gx + 
							")\nGP-Ycoordinate: \n(" + Gy + ")");
		System.out.println("------------------------------------------------------------------------------");
		
		System.out.println("N: number of points in the field: as recommended by SEC:");
		System.out.println(N);
		System.out.println("------------------------------------------------------------------------------");
		
		System.out.println("The private key (randomly chosen): ");
		System.out.println(private_key);
		System.out.println("------------------------------------------------------------------------------");
		
		if(public_key==null) return;
		System.out.println("The uncompressed public key:");
		System.out.println("\n X coord: " + public_key[0]); 
		System.out.println("\n Y coord:" +  public_key[1]);
		System.out.println("------------------------------------------------------------------------------");
		
		System.out.println("The compressed public key: (only X coord, in hex)");
		System.out.println("\n" + public_key[0].toString(16)); 
		System.out.println("------------------------------------------------------------------------------");
		
		System.out.println("Static random number: ");
		System.out.println(static_random_number);
		System.out.println("------------------------------------------------------------------------------");
		
		System.out.println("Dynamic random number: ");
		System.out.println(dynamic_random_number);
		System.out.println("------------------------------------------------------------------------------");
		
	}

	
	//Calculate modular inverse 
	//Algorithm from wikipedia: https://en.wikipedia.org/wiki/Modular_multiplicative_inverse
	public BigInteger modular_inverse(BigInteger a, BigInteger p){
		
		BigInteger l = new BigInteger("1");
		BigInteger h = new BigInteger("0");

		BigInteger low = a.mod(p);
		BigInteger high = p;
		BigInteger new_low = new BigInteger("0");

		while (low.compareTo(BigInteger.ONE) > 0){ //See if low is > 1
			
			BigInteger quotient = high.divide(low);
			BigInteger n = h.subtract(l.multiply(quotient));
			
			new_low = high.subtract(low.multiply(quotient));
			
			/*System.out.println(l + "\n" +
					h + "\n" +
					high + "\n" +
					low + "\n" +
					new_low + "\n" +
					quotient + "\n" +
					n + "\n"
					);*/

			high = low;
			h = l;
			low = new_low;
			l = n;

		}

		return l.mod(p);
	}
	
	
	//Point doubling
	//Formula for doubling a point (x,y) to the doubled point (xd, yd) is given by
	// xd = lambda ^ 2 - (2 * x)
	// yd = lambda * ( x - xd ) - y
	// where lambda is the slope, given by:
	// lambda = (3 x^2 + a ) / (2 * y) 
	// Note, a = 0 here per recommendations, so lambda = 3*x^2 / 2 * y
	// Division is done with the extended euclidean algorithm, over the prime number 
	// Link to extended euclidean algorithm: 
	// https://en.wikipedia.org/wiki/Modular_multiplicative_inverse#Extended_Euclidean_algorithm
	public BigInteger[] elliptic_double(BigInteger x, BigInteger y){
		
		//Testing
		//BigInteger prime = (new BigInteger("43"));
		
		BigInteger result[] = new BigInteger[2];
		result[0] = new BigInteger("0"); //result of point doubling's x coord
		result[1] = new BigInteger("0"); //result of point doubling's y coord
		
		BigInteger lambda = new BigInteger("0");
		
		BigInteger numerator = x.pow(2);
		numerator = numerator.multiply(new BigInteger("3")); //3*x^2
		BigInteger denominator = y.multiply(new BigInteger("2")); // 2 * y
		
		BigInteger mod_denominator = modular_inverse(denominator, prime); // (2 * y) ^ -1
		
		lambda = numerator.multiply(mod_denominator);
		lambda = lambda.mod(prime);
		
		BigInteger resultX = lambda.pow(2);
		resultX = resultX.subtract(x.multiply(new BigInteger("2"))); // = lambda ^2 - 2 * x
		
		BigInteger resultY = x.subtract(resultX);
		resultY = resultY.multiply(lambda);
		resultY = resultY.subtract(y); // = lambda * ( x - xd ) - y
		
		//xd = lambda ^ 2 - (2 * x)
		result[0] = resultX.mod(prime);
		result[1] = resultY.mod(prime);
		
		return result;
	}
	
	
	//Point adding
	//(xr, yr) = (xa, ya) + (xb, yb)
	//Formula for adding two points (xa, ya) and (xb, yb) is given by:
	//xr = lambda ^ 2 - xa - xb
	//yr = lambda(xa - xr) - ya
	//where lambda is the slope given by
	//lambda = yb - ya / xb - xa
	public BigInteger[] elliptic_add(BigInteger xa, BigInteger ya, BigInteger xb, BigInteger yb){
		
		//Testing
		//BigInteger prime = (new BigInteger("43"));
		
		
		BigInteger result[] = new BigInteger[2];
		result[0] = new BigInteger("0"); //result of point adding x coord
		result[1] = new BigInteger("0"); //result of point adding's y coord
		
		//First calculate lambda
		BigInteger mod_x = modular_inverse(xb.subtract(xa), prime);
		BigInteger lambda = yb.subtract(ya); 
		lambda = lambda.multiply(mod_x); //lambda = yb - ya / xb - xa
		lambda = lambda.mod(prime);
			
		
		//Next, get the xcoord of result
		BigInteger resultX = lambda.pow(2);
		resultX = resultX.subtract(xa).subtract(xb); //xr = lambda ^ 2 - xa - xb
		resultX = resultX.mod(prime);
		
		//Next, get y
		BigInteger resultY = xa.subtract(resultX);
		resultY = resultY.multiply(lambda);
		resultY = resultY.subtract(ya);
		resultY = resultY.mod(prime); //yr = lambda(xa - xr) - ya
		
		//populate results
		result[0] = resultX;
		result[1] = resultY;
		
		return result; 
		
		
	}
	
	
	//Elliptic key multiplication is invented math is basically comprised of point doubling and point adding
	//The formula is taken from https://en.wikipedia.org/wiki/Elliptic_curve_point_multiplication
	public BigInteger[] elliptic_multiply(BigInteger x, BigInteger y, BigInteger constant){
		
		//Testing 
		//BigInteger private_key = (new BigInteger("43"));

		//First get the prime number in bit format
		String bitstring = constant.toString(2);

		BigInteger res[] = new BigInteger[2];
		res[0] = x;
		res[1] = y;
		
		//Is this right? - should the first bit be included?		
		for(int i = 1 ; i < bitstring.length(); i++){
			
			res = elliptic_double(res[0],res[1]);
			//System.out.println("double " +  res[0]);
			
			if(bitstring.charAt(i) == '1'){
				res = elliptic_add(res[0], res[1], x, y);
				//System.out.println("add " +  res[0]);
			}			
		}
		
		return res;	
	}
	
	
	//Generate public key
	//This is the amazing part of elliptic curves
	//It only takes ONE multiplication operation on an ECC 
	//to generate a public key from a private key and a 
	//generator point. The operation basically is as simple as:
	//public_key = private_key * Gp
	public BigInteger[] generate_public_key(){
		
		if(public_key == null){
			public_key = elliptic_multiply(Gx, Gy, private_key);
		}
		
		return public_key;		
	}
	
	
	//Generating a random number for digital signing
	public BigInteger generate_random_number(){
		
		dynamic_random_number = new BigInteger(256, new Random());
		
		return dynamic_random_number;
	}
	
	
	//Get message to sign
	public BigInteger get_hash_from_message(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		Message = message;
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String text = "This is some text";

		md.update(text.getBytes("UTF-8")); // Change this to "UTF-16" if needed
		byte[] digest = md.digest();
		
		HashOfMessage = (new BigInteger(digest));
		
		return HashOfMessage;
	}


	//The algorithm for digitally signing a message in ECC curves:
	//Choose a random number, multiply it with the generator point
	//to get two random coordinates. 
	//Signature = 
	//((hash of message + random_number's xcoord * private key) 
	//	*  modular_inverse(random number, number of points)) % N
	//Algorithm from: https://en.wikipedia.org/wiki/Elliptic_Curve_Digital_Signature_Algorithm
	public BigInteger[] sign_message(BigInteger hash) {
		
		BigInteger[] signature = new BigInteger[2];
		
		BigInteger[] random_coordinates = elliptic_multiply(Gx, Gy, static_random_number); 
		
		BigInteger rX = random_coordinates[0].mod(N); 
		
		BigInteger sign = hash.add(rX.multiply(private_key));
		BigInteger mod_sign = modular_inverse(static_random_number, N); 
		sign = sign.multiply(mod_sign);
		sign = sign.mod(N);
		
		
		signature[0] = rX;
		signature[1] = sign;
		
		return signature;
	}
	
	
	//The algorithm for digitally verifying a SIGNED message in ECC curves:
	//0. Find out modular inverse of the signature, over field N; Call that MOD_SIGN
	//1. First generate RESULT1 as: 
	//1. a. Multiply MOD_SIGN with hash of message
	//1. b. Multiply that with Generator point to get two coordinates on our curve
	//2. Next, generate RESULT2 as:
	//2. a. Multiply the xcoordinate of the random number (part of the signature) with MOD_SIGN 
	//2. b. Multiply result of above with public key coordinates  
	//3. Add RESULT1 and RESULT2 to get two coordinates
	//4. if the xcoordinate of 3, matches the xcoordinate of random number that was part of signature, then you're verified
	//5. If not, you're not verified. Return false
	//Algorithm from: https://en.wikipedia.org/wiki/Elliptic_Curve_Digital_Signature_Algorithm
	public Boolean verify_signature (BigInteger[] signature, BigInteger HashOfMessage){
		
		BigInteger mod_sign = modular_inverse(signature[1], N); //Corresponds to 'w' in Wikipedia
		
		BigInteger[] first_coordinates = new BigInteger[2]; //RESULT1
		BigInteger[] second_coordinates = new BigInteger[2]; //RESULT2. per wiki
		BigInteger[] result_coordinates = new BigInteger[2];
		
		first_coordinates = elliptic_multiply(Gx, Gy, HashOfMessage.multiply(mod_sign).mod(N));
		
		second_coordinates = elliptic_multiply(public_key[0], public_key[1], signature[0].multiply(mod_sign).mod(N));
		
		result_coordinates = elliptic_add(first_coordinates[0], first_coordinates[1],
										  second_coordinates[0], second_coordinates[1]);
		
		//verified if the x coordinate of the point got by adding RESULT1 and RESULT2, 
		//matches the random x-coordinate (that was part of the digital signature)
		if (result_coordinates[0].compareTo(signature[0]) == 0){
			return true;
		}
		
		//Otherwise, not verified
		System.out.println(result_coordinates[0]);
		return false;
		
	}
	
	
}


