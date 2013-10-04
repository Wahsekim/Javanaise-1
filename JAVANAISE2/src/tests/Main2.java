package tests;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;
import jvn.MonObjet;

public class Main2 {
	
	public static void main(String[] argv){
		try {
			JvnServerImpl server = JvnServerImpl.jvnGetServer();
			JvnObject jvnO = server.jvnLookupObject("objet1");
			/*Si fixé avant unlock (5sec par exemple) interblocage voir pourquoi !!*/
			Thread.sleep(5000);
			jvnO.jvnLockRead();
			System.out.println("L'objet partagé 'objet1' récupéré et lock apres 10 secondes : "+((MonObjet)jvnO.jvnGetObjectState()).getString());
			jvnO.jvnUnLock();
			jvnO.jvnLockRead();
			System.out.println("locked in read mode : "+((MonObjet) jvnO.jvnGetObjectState()).getString());
			Thread.sleep(10000);
			jvnO.jvnUnLock();
		} catch (JvnException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}	
}
