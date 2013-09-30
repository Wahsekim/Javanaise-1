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
		if(STATE != STATE_ENUM.RC && STATE != STATE_ENUM.WC){
			obj = JvnServerImpl.jvnGetServer().jvnLockRead(joi);
		}
		STATE = STATE_ENUM.R;
	}

	public synchronized void jvnLockWrite() throws JvnException {
		if(STATE != STATE_ENUM.WC && STATE != STATE_ENUM.RWC){
			obj = JvnServerImpl.jvnGetServer().jvnLockWrite(joi);
		}
		STATE = STATE_ENUM.W;
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
			if(wait_for_write){
				wait_for_write = false;
				STATE = STATE_ENUM.NL;
				this.notify();
			}
			else if(wait_for_read){
				wait_for_read = false;
				STATE = STATE_ENUM.RC;
				this.notify();
			}
			else{
				STATE = STATE_ENUM.WC;
			}
		}
		else if(STATE == STATE_ENUM.RWC){
			STATE = STATE_ENUM.WC;
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
		wait_for_write = true;
		while(STATE == STATE_ENUM.R || STATE == STATE_ENUM.W){
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		wait_for_write = true;
		while(STATE == STATE_ENUM.R || STATE == STATE_ENUM.W){
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return obj;
	}

	public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		wait_for_read = true;	
		if(STATE == STATE_ENUM.R || STATE == STATE_ENUM.W){
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return obj;
	}

}
