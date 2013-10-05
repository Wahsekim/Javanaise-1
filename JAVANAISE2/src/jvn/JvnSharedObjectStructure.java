package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class JvnSharedObjectStructure {
	private int id_jvn_object;
	private ArrayList<Integer> reader_list = new ArrayList<Integer>();
	private ArrayList<Integer> owner = new ArrayList<Integer>();
	private Integer writer = new Integer(-1);
	private JvnLocalCoord coord;
	private boolean locked = false;

	public JvnSharedObjectStructure(JvnObject obj, int id_jvm, JvnLocalCoord coord){
		try {
			id_jvn_object = obj.jvnGetObjectId();
			addOwner(new Integer(id_jvm));
			this.coord = coord;
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void getLock(){
		System.out.println("GET LOCK ON "+id_jvn_object);
		while(locked){
			try {
				System.out.println("GET LOCK WAITINBGGGGGG ON "+id_jvn_object);
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("LOCKED ON "+id_jvn_object);
		locked = true;
	}

	public synchronized void releaseLock(){
		System.out.println("RELEASE LOCK ASKED ON "+id_jvn_object);
		locked = false;
		this.notify();
	}

	/*Va invalider le(s) reader(s) pour un writer, sauf si le reader est le demandeur*/
	public synchronized void invalidateReader(int id_jvm) throws RemoteException, JvnException{
		if(reader_list.size() > 0){
			for(Integer i : reader_list){
				System.out.println("pour l'objet "+id_jvn_object+" j'invalide"+i.intValue()+"     "+System.currentTimeMillis());
				if(i.intValue() != id_jvm){
					System.out.println("pour l'objet "+id_jvn_object+" je vais vraiment invalider"+i.intValue()+"     "+System.currentTimeMillis());
					coord.jvnInvalidateReader(id_jvn_object, i.intValue());
				}
			}
			reader_list.clear();
			writer = id_jvm;	
		}
		System.out.println("pour l'objet "+id_jvn_object+" j'invalide est terminé !!!"+System.currentTimeMillis()+"     "+System.currentTimeMillis());
	}

	/*Va invalider le writer pour un reader*/
	public synchronized Serializable invalidateWriterForReader(int id_jvm) throws RemoteException, JvnException{
		Serializable ser = null;
		if(writer > 0){
			System.out.println("pour l'objet "+id_jvn_object+" j'invalide le writer"+writer+"     "+System.currentTimeMillis());
			ser = coord.jvnInvalidateWriterForReader(id_jvn_object, writer);
			reader_list.add(new Integer(id_jvm));
			writer = -1;
			System.out.println("pour l'objet "+id_jvn_object+" j'invalide le writer"+writer+" est terminé !!!!"+"     "+System.currentTimeMillis());
			return ser;
		}
		throw new JvnException("Impossible d'invalider writerForReader");
	}

	/*Va invalider le writer pour un writer*/
	public synchronized Serializable invalidateWriter(int id_jvm) throws RemoteException, JvnException{
		if(writer > 0){
			System.out.println("pour l'objet "+id_jvn_object+" j'invalide le writer"+writer+" pour un writer"+id_jvm+"     "+System.currentTimeMillis());
			Serializable ser = coord.jvnInvalidateWriter(id_jvn_object, writer);
			writer = id_jvm;
			System.out.println("pour l'objet "+id_jvn_object+" j'invalide le writer"+writer+" pour un writer"+id_jvm+" terminé !!!"+"     "+System.currentTimeMillis());
			return ser;
		}
		throw new JvnException("Impossible d'invalider writer");
	}

	public void addOwner(int id){
		owner.add(new Integer(id));
	}

	public void removeOwner(int id) throws JvnException{
		for(Integer i : owner){
			if(i.intValue() == id){
				owner.remove(i);
				return;
			}
		}
		throw new JvnException("Aucune JVM avec l'id "+id+" ne détient l'objet "+id_jvn_object);	
	}

	public synchronized void setWriter(Integer id){
		writer = id;
	}

	public synchronized boolean hasWriter(){
		return writer.intValue() != -1;
	}

	public synchronized void addReader(Integer id){
		reader_list.add(id);
	}

	public synchronized boolean hasReader(){
		return !reader_list.isEmpty();
	}

	public int getObjectId(){
		return id_jvn_object;
	}

}
