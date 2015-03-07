package cn.edu.sjtu.omnilab.tpca.pattern.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjPersistence {
	/**
	 * Serialize an object to binary file.
	 * @param o
	 * @param file
	 * @throws IOException
	 */
	public static void toBinary(Object o, String file) throws IOException{
		// Write to disk with FileOutputStream
		FileOutputStream f_out = new FileOutputStream(file);
		// Write object with ObjectOutputStream
		ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
		// Write object out to disk
		obj_out.writeObject ( o );
		obj_out.close();
	}
	
	/**
	 * Restore an object from a binary file.
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object fromBinary(String file) throws IOException, ClassNotFoundException{
		FileInputStream f_in;
		// Read from disk using FileInputStream
		f_in = new FileInputStream(file);
		// Read object using ObjectInputStream
		ObjectInputStream obj_in = new ObjectInputStream (f_in);
		// Read an object
		Object res = obj_in.readObject();
		obj_in.close();
		return res;
	}
}
