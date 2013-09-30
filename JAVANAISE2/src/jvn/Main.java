package jvn;



public class Main {
	public static void main(String[] argv){
		try {
			MonObjet mon_objet_partage = new MonObjet("State 1");
			JvnServerImpl server = JvnServerImpl.jvnGetServer();
			JvnObject jvnO = server.jvnCreateObject(mon_objet_partage);
			server.jvnRegisterObject("objet1", jvnO);
			jvnO.jvnLockWrite();
			System.out.println("Mon objet : "+((MonObjet) jvnO.jvnGetObjectState()).getString());
			/*proxy non transparent*/
			((MonObjet) jvnO.jvnGetObjectState()).setString("State 2");
			System.out.println("Mon objet modifié pas unlock " +
					" : "+((MonObjet) jvnO.jvnGetObjectState()).getString());
			Thread.sleep(10000);
			jvnO.jvnUnLock();
			System.out.println("unlocked");
			jvnO.jvnLockRead();
			System.out.println("locked in read mode : "+((MonObjet) jvnO.jvnGetObjectState()).getString());
			Thread.sleep(10000);
			jvnO.jvnUnLock();
		} catch (JvnException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
