package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;

public interface JvnLocalCoord {
	public Serializable jvnInvalidateWriter(int id_obj, int id_jvm) throws RemoteException, JvnException;
	public void jvnInvalidateReader(int id_obj, int id_jvm) throws RemoteException, JvnException;
	public Serializable jvnInvalidateWriterForReader(int id_obj, int id_jvm) throws RemoteException, JvnException;
}
