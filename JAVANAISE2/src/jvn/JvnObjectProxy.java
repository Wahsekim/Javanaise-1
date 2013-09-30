package jvn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JvnObjectProxy implements java.lang.reflect.InvocationHandler {
	private Object obj;

	public static Object newInstance(Object obj){
		return java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(),
				obj.getClass().getInterfaces(), new JvnObjectProxy(obj));
	}

	public JvnObjectProxy (Object obj) {
		this.obj = obj;
		System.out.println("Creation du proxy");
	}

	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		Object result;
		try {
			System.out.println("before method " + m.getName());
			long start = System.nanoTime();
			result = m.invoke(obj, args);
			long end = System.nanoTime();
			System.out.println(String.format("%s took %d ns", m.getName(), (end-start)) );
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		} catch (Exception e) {
			throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
		} finally {
			System.out.println("after method " + m.getName());
		}
		return result;
	}

	public String toString(){
		return "Je suis un interceptor";
	}
}
