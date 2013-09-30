package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class JvnSharedObjectStructure {
	private int id_jvn_object;
	private ArrayList<Integer> reader_list = new ArrayList<Integer>();
	private ArrayList<Integer> owner = new ArrayList<Integer>();
	private Integer writer = new Integer(-1);
	private JvnCoordImpl coord;
	
	public JvnSharedObjectStructure(JvnObject obj, int id_jvm, JvnCoordImpl coord){
		try {
			id_jvn_object = obj.jvnGetObjectId();
			addOwner(new Integer(id_jvm));
			this.coord = coord;
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Integer> getReader(){
		return reader_list;
	}
	
	/*Va invalider le(s) reader(s) pour un writer*/
	public synchronized void invalidateReader(int id_jvm) throws RemoteException, JvnException{
		if(reader_list.size() > 0){
			for(Integer i : reader_list){
				coord.jvnInvalidateReader(id_jvn_object, i.intValue());
			}
			writer = id_jvm;
		}
	}
	
	/*Va invalider le writer pour un reader*/
	public synchronized Serializable invalidateWriterForReader(int id_jvm) throws RemoteException, JvnException{
		if(writer > 0){
			Serializable ser = coord.jvnInvalidateWriterForReader(id_jvn_object, writer);
			reader_list.add(new Integer(id_jvm));
			return ser;
		}
		throw new JvnException("Impossible d'invalider writerForReader");
	}
	
	/*Va invalider le writer pour un writer*/
	public synchronized Serializable invalidateWriter(int id_jvm) throws RemoteException, JvnException{
		if(writer > 0){
			Serializable ser = coord.jvnInvalidateWriter(id_jvn_object, writer);
			writer = id_jvm;
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
	
	public boolean canBeRemoved(){
		return owner.size() == 0;
	}
	
	public int getWriter(){
		return writer;
	}
	public void setWriter(Integer id){
		writer = id;
	}
	
	public boolean hasWriter(){
		return writer.intValue() != -1;
	}
	
	public void setNoWriter(){
		writer = -1;
	}
	
	public void addReader(Integer id){
		reader_list.add(id);
	}
	
	public void removeReader(Integer id){
		reader_list.remove(id);
	}
	
	public boolean hasReader(){
		return !reader_list.isEmpty();
	}
	
	public int getObjectId(){
		return id_jvn_object;
	}
	
}
