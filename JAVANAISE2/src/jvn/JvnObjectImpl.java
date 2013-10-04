package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3351923401318854408L;
	private int joi = 0;
	private Serializable obj = null;
	public enum STATE_ENUM {NL,R,W,RC,WC,RWC};
	private STATE_ENUM STATE = STATE_ENUM.NL; 
	private boolean wait_for_read = false;
	private boolean wait_for_write = false;

	public JvnObjectImpl(int id, Serializable obj){
		this.joi = id;
		this.obj = obj;
	}

	public synchronized void jvnLockRead() throws JvnException {
		System.out.println("Etat courant :"+STATE+" sur l'objet "+joi);
		if(STATE == STATE_ENUM.NL || STATE == STATE_ENUM.WC){
			obj = JvnServerImpl.jvnGetServer().jvnLockRead(joi);
			STATE = STATE_ENUM.R;
		}
		else if(STATE == STATE_ENUM.RC){
			STATE = STATE_ENUM.R;
		}
		else{
			throw new JvnException("Impossible de vérouiller l'objet"+joi+" en lecture, l'etat courant est le suivant : "+STATE);
		}
	}

	public synchronized void jvnLockWrite() throws JvnException {
		System.out.println("Etat courant :"+STATE+" sur l'objet "+joi);
		if(STATE == STATE_ENUM.NL || STATE == STATE_ENUM.RC){
			obj = JvnServerImpl.jvnGetServer().jvnLockWrite(joi);
			STATE = STATE_ENUM.W;
		}
		else if(STATE == STATE_ENUM.WC){
			STATE = STATE_ENUM.W;
		}
		else{
			throw new JvnException("Impossible de vérouiller l'objet"+joi+" en écriture, l'etat courant est le suivant : "+STATE);
		}
	}

	public synchronized void jvnUnLock() throws JvnException {
		System.out.println("STATE : "+STATE+" waitforread : "+wait_for_read+" waitforwrite : "+wait_for_write);
		if(STATE == STATE_ENUM.R){
			if(wait_for_write){
				wait_for_write = false;
				STATE = STATE_ENUM.NL;
				this.notify();
			}
			else{
				STATE = STATE_ENUM.RC;
			}
		}
		else if(STATE == STATE_ENUM.W){
			if(wait_for_write || wait_for_read){
				wait_for_write = false;
				STATE = STATE_ENUM.NL;
				this.notify();
			}
			else{
				STATE = STATE_ENUM.WC;
			}
		}
		else if(STATE == STATE_ENUM.RWC){
			if(wait_for_write || wait_for_read){
				wait_for_write = false;
				STATE = STATE_ENUM.NL;
				this.notify();
			}
			else{
				STATE = STATE_ENUM.WC;
			}
		}
		else{
			throw new JvnException("Execution de jvnUnLock() sur un jnvObject id ="+joi+" avec l'état "+STATE);
		}
	}

	public synchronized int jvnGetObjectId() throws JvnException {
		return joi;
	}

	public synchronized Serializable jvnGetObjectState() throws JvnException {
		return obj;
	}

	public synchronized void jvnInvalidateReader() throws JvnException {
		if(STATE == STATE_ENUM.RC){
			STATE = STATE_ENUM.NL;
		}
		else{
			wait_for_write = true;
			while(STATE == STATE_ENUM.R || STATE == STATE_ENUM.W || STATE == STATE_ENUM.RWC){
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		if(STATE == STATE_ENUM.WC){
			STATE = STATE_ENUM.NL;
		}
		else{
			wait_for_write = true;
			while(STATE == STATE_ENUM.R || STATE == STATE_ENUM.W || STATE == STATE_ENUM.RWC){
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return obj;
	}

	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		if(STATE == STATE_ENUM.WC){
			STATE = STATE_ENUM.NL;
		}
		else{
			wait_for_read = true;	
			if(STATE == STATE_ENUM.R || STATE == STATE_ENUM.W || STATE == STATE_ENUM.RWC){
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return obj;
	}

}
