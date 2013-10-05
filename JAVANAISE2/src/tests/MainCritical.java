package tests;

import java.util.ArrayList;

import jvn.JvnException;
import jvn.JvnObjectImpl;
import jvn.JvnServerImpl;
import jvn.MonObjet;

public class MainCritical {
	public static void main(String[] args){
		try {
			String name = "main";
			int num_rand;
			ArrayList<JvnObjectImpl> array = new ArrayList<JvnObjectImpl>();
			MonObjet o1 = new MonObjet("objet1");
			JvnObjectImpl shared_object_1 = (JvnObjectImpl) JvnServerImpl.jvnGetServer().jvnCreateObject(o1);
			JvnServerImpl.jvnGetServer().jvnRegisterObject("objet1", shared_object_1);
			Thread.sleep(5000);
			JvnObjectImpl shared_object_2 = (JvnObjectImpl) JvnServerImpl.jvnGetServer().jvnLookupObject("objet2");
			JvnObjectImpl shared_object_3 = (JvnObjectImpl) JvnServerImpl.jvnGetServer().jvnLookupObject("objet3");
			array.add(shared_object_1);
			array.add(shared_object_2);
			array.add(shared_object_3);
			for(int i = 0; i < 100; i++){
				for(JvnObjectImpl obj : array){
					num_rand = (int) Math.round(Math.random());
					switch(num_rand){
					case 0 :
						System.out.println("Lock write on "+obj.jvnGetObjectId()+" by "+name);
						obj.jvnLockWrite();
						System.out.println("Lock acquire");
						((MonObjet)obj.jvnGetObjectState()).setString("lock by"+name);
						Thread.sleep(1000*(num_rand+1));
						obj.jvnUnLock();
						System.out.println("Unlock done");
						break;
					case 1 : 
						System.out.println("Lock read on "+obj.jvnGetObjectId()+" by "+name);
						obj.jvnLockRead();
						System.out.println("Actual value : "+((MonObjet)obj.jvnGetObjectState()).getString());
						Thread.sleep(1000*(num_rand+1));
						obj.jvnUnLock();
						System.out.println("Unlock done");
						break; 
					}		
				}
			}
			System.out.println("YES fin du test pour "+name);
			System.exit(0);
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
