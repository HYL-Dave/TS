package com.toppanidgate.idenkey;

public class TestLoadLibrary {
	public static void main(String[] args){
		// 要安裝 
		// PTKcpsdk.msi(主要是裡面有 jcryptoki.DLL, 要求裝.NET 3.5)    
		// PTKjpsdk.msi    
		// PTKnethsm.msi(先裝這，要求裝.NET) 之後才會 OK
		// 否則有 ERROR MSG:java.lang.UnsatisfiedLinkError: no jcryptoki in java.library.path
//	    System.out.println(System.getProperty("java.library.path"));
//	    System.out.println("Loading Library...");
//        System.loadLibrary("jcryptoki");
//        System.out.println("Library Loaded.");
	}
}
