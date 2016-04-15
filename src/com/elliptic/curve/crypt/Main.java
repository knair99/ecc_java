package com.elliptic.curve.crypt;

import java.math.BigInteger;

public class Main {
	
	public static void main(String[] args){
		
		Elliptic e = new Elliptic();
		
		BigInteger[] public_key = e.elliptic_multiply(e.Gx, e.Gy, e.private_key);
		
		//BigInteger[] result = e.elliptic_add(new BigInteger("13"), new BigInteger("233"),new BigInteger("14"),new BigInteger("400"));
		
		//BigInteger result = e.modular_inverse(new BigInteger("17"), new BigInteger("43"));
		
		System.out.println(public_key[0] + ", \n" +  public_key[1]);
	}

}
