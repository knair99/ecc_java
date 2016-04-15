package com.elliptic.curve.crypt;


import java.math.BigInteger;

public class Elliptic {
	
	//The curve recommended by SEC is y = x^3 + ax + b; where a = 0, and b = 7
	//http://www.secg.org/sec2-v2.pdf
	
	public static BigInteger prime; //The prime number recommended per SECP256K1
	public static BigInteger N;  //The number of points in the field 
	public static BigInteger Gx; //The x coordinate of the generator point
	public static BigInteger Gy; //The y coordinate of the generator point
	
	public static BigInteger private_key;
	
	
	public Elliptic (){
		
		BigInteger p = new BigInteger("2");
		
		//Value recommended by NIST and SEC
		//http://www.secg.org/sec2-v2.pdf
		prime = p.pow(256).subtract(p.pow(32)).subtract(p.pow(9)).subtract(p.pow(8)).subtract (p.pow(7)).subtract (p.pow(6))
				.subtract(p.pow(4)).subtract(p.pow(0));
		
		//System.out.println("Prime number:");
		//System.out.println(prime);
		
		N  = new BigInteger("115792089237316195423570985008687907852837564279074904382605163141518161494337");
		//System.out.println("N: number of points in the field:");
		//System.out.println(N);
		
		//The generator point, Gp = (Gx, Gy)
		//Per SECp256k1, the recommended Gx is 0x79BE667E F9DCBBAC 55A06295 CE870B07 029BFCDB 2DCE28D9 59F2815B 16F81798
		//Solving for y, the recommended Gy is 0x483ada77 26a3c465 5da4fbfc 0e1108a8 fd17b448 a6855419 9c47d08f fb10d4b8L
		
		
		Gx = new BigInteger("55066263022277343669578718895168534326250603453777594175500187360389116729240");
		Gy = new BigInteger("32670510020758816978083085130507043184471273380659243275938904335757337482424"); //y = x^3 + 0*x + 7
		
		//System.out.println("Generator point (x,y): \n (" + Gx + ", \n" + Gy + ")");
		
		private_key = new BigInteger("75263518707598184987916378021939673586055614731957507592904438851787542395619"); //randomly chosen 
		
		
	}
	
	//Calculate modular inverse 
	//Algorithm from wikipedia: https://en.wikipedia.org/wiki/Modular_multiplicative_inverse
	public static BigInteger modular_inverse(BigInteger a, BigInteger p){
		
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
	public static BigInteger[] elliptic_double(BigInteger x, BigInteger y){
		
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
	public static BigInteger[] elliptic_add(BigInteger xa, BigInteger ya, BigInteger xb, BigInteger yb){
		
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
	public static BigInteger[] elliptic_multiply(BigInteger x, BigInteger y, BigInteger constant){
		
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
	
}
