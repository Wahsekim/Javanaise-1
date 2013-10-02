/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;


public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord, JvnLocalCoord{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1164487591124280323L;
	private HashMap<Integer, JvnRemoteServer> id_server = new HashMap<Integer, JvnRemoteServer>();
	private HashMap<String, Integer> name_objectid = new HashMap<String, Integer>();
	private ArrayList<JvnSharedObjectStructure> struct_list = new ArrayList<JvnSharedObjectStructure>();
	private HashMap<Integer, JvnObject> id_object = new HashMap<Integer, JvnObject>();
	private int NEXT_ID = 1;
	private int NEXT_ID_JVM = 1;
	/**
	 * Default constructor
	 * @throws JvnException
	 **/
	private JvnCoordImpl() throws Exception {
		LocateRegistry.createRegistry(5555);
		Naming.rebind("//localhost:5555/coordinator",  this);
		System.out.println("Coordinateur enregistré dans rmiregistry, pret à fonctionner");
	}

	/**
	 *  Allocate a NEW JVN object id (usually allocated to a 
	 *  newly created JVN object)
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public synchronized int jvnGetObjectId() throws java.rmi.RemoteException,jvn.JvnException {
		NEXT_ID += 1;
		return NEXT_ID - 1;
	}

	public synchronized int jvnGetServerId() throws RemoteException, JvnException {
		NEXT_ID_JVM++;
		return NEXT_ID_JVM - 1;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException,jvn.JvnException{
		/*Si le nom symbolique est déjà utilisé, cela pose problème!*/
		if(name_objectid.containsKey(jon)){
			throw new JvnException("Un objet existe déjà avec ce nom symbolique, impossible d'en créer un autre");
		}
		/*Sinon on l'enregistre*/
		else{
			/*Récupération du serveur pour appels distants si pas déjà présent*/
			if(!id_server.containsKey(new Integer(js.jvnGetId()))){
				id_server.put(js.jvnGetId(), js);
			}
			/*Si l'objet n'est pas connu du coordinateur on va créer une structure de controle d'acces*/
			if(!name_objectid.containsValue(new Integer(jo.jvnGetObjectId()))){
				struct_list.add(new JvnSharedObjectStructure(jo,js.jvnGetId(),this));
				id_object.put(new Integer(jo.jvnGetObjectId()), jo);
			}
			/*Mapping nom symbolique / identifiant objet*/
			name_objectid.put(jon, new Integer(jo.jvnGetObjectId()));
		}
	}

	/*Sending a jvnInvalidateWriter message to a remote jvm for the id_obj object*/
	public Serializable jvnInvalidateWriter(int id_obj, int id_jvm) throws RemoteException, JvnException{
		return id_server.get(id_obj).jvnInvalidateWriter(id_obj);
	}

	/*Sending a jvnInvalidateReader message to a remote jvm for the id_obj object*/
	public void jvnInvalidateReader(int id_obj, int id_jvm) throws RemoteException, JvnException{
		id_server.get(id_obj).jvnInvalidateReader(id_obj);
	}

	/*Sending a jvnInvalidateWriterForReader message to a remote jvm for the id_obj object*/
	public Serializable jvnInvalidateWriterForReader(int id_obj, int id_jvm) throws RemoteException, JvnException{
		return id_server.get(id_obj).jvnInvalidateWriterForReader(id_obj);
	}


	private JvnSharedObjectStructure getStruct(Integer id) throws JvnException{
		for(JvnSharedObjectStructure str : struct_list){
			if(str.getObjectId() == id.intValue()){
				return str;
			}
		}
		throw new JvnException("Aucune structure pour l'objet [id="+id+"] n'a été trouvé.");
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public JvnObject jvnLookupObject(String jon,JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		/*Récupération du serveur pour appels distants si pas déjà présent*/
		if(!id_server.containsKey(new Integer(js.jvnGetId()))){
			id_server.put(new Integer(js.jvnGetId()), js);
		}
		Integer id = name_objectid.get(jon);
		/*Si l'objet existe , on ajoute une duplication et retourne l'objet*/
		if(id != null){
			JvnSharedObjectStructure o = getStruct(id);
			o.addOwner(js.jvnGetId());
			return new JvnObjectImpl(id.intValue(),id_object.get(id).jvnGetObjectState());
		}
		else{
			throw new JvnException("Impossible de trouver l'objet JVN associé " +
					"à ce nom symbolique : "+jon);
		}
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		JvnSharedObjectStructure str = getStruct(joi);
		if(str.hasWriter()){
			Serializable ser = str.invalidateWriterForReader(js.jvnGetId());
			updateObject(joi,ser);
			return ser;
		}
		else{
			str.addReader(js.jvnGetId());
			return id_object.get(str.getObjectId()).jvnGetObjectState();
		}
	}

	private void updateObject(int id, Serializable ser){
		id_object.remove(id);
		id_object.put(new Integer(id), new JvnObjectImpl(id, ser));
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		JvnSharedObjectStructure str = getStruct(joi);
		if(str.hasWriter()){
			Serializable ser = str.invalidateWriter(js.jvnGetId());
			updateObject(joi,ser);
			return ser;
		}
		else if(str.hasReader()){
			str.invalidateReader(js.jvnGetId());
		}
		else{
			str.setWriter(new Integer(js.jvnGetId()));
		}
		return id_object.get(str.getObjectId()).jvnGetObjectState();
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js)
			throws java.rmi.RemoteException, JvnException {
		// to be completed
	}



	public static void main(String[] args){
		try {
			new JvnCoordImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Remove a distributed object
	 * @param joi : the JVN object identification
	 * @param js : the remote reference of the server
	 * @throws java.rmi.RemoteException 
	 * @throws JvnException
	 */
	public void jvnRemoveObject(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		/*TODO*/
	}
}


