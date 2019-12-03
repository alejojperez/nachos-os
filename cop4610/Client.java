package nachos.cop4610;

import java.io.IOException;

import nachos.cop4610.KVClient;

public class Client {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		KVClient kc = new KVClient("localhost", 8080);
		try{
			String three = "3";
			String seven = "7";
			System.out.println("putting (3, 7)");
		  	kc.put(three, seven);
			System.out.println("ok");

			System.out.println("putting (3, 7) (again)");
			kc.put(three, seven);
			System.out.println("ok");
			
			System.out.println("getting key=3");			
			String value = kc.get(three);					
			System.out.println("returned: " + value);
			kc.del(three);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
