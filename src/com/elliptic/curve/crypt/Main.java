package com.elliptic.curve.crypt;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class Main {
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		
		//First, create our elliptic object
		//TODO: Assign private key here
		Elliptic e = new Elliptic();
		
		//Generate public key
		e.generate_public_key();
		
		//Display all information generated so far
		e.display_data();
		
		//Generate hash of message to sign
		BigInteger hash = e.get_hash_from_message("The quick brown fox jumped over the lazy dog");
		System.out.println("Hash: \n" + hash);
		
		//Digitally sign the message using ECC
		BigInteger[] signature = e.sign_message(hash);
		System.out.println("Signature: \n" + signature[0] + ",\n" + signature[1] );
		
		//Verify the signature at our end to make sure
		if(e.verify_signature(signature, hash)){
			System.out.println("Signature verification tested: PASSED!");
		}
		else{
			System.out.println("Signature verification tested: FAILED!");
		}
		
		
	}

}

