package com.jd.workflow.console.serializer;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.ClassHelper;

import java.io.*;

public class DefaultSerializer<T> {
    public DefaultSerializer() {
    }

    
    public byte[] serialize(T o)  {
        if (o == null) {
            String msg = "argument cannot be null.";
            throw new IllegalArgumentException(msg);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);

            try {
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(o);
                oos.close();
                return baos.toByteArray();
            } catch (IOException ex) {
                String msg = "Unable to serialize object [" + o + "].  " + "In order for the DefaultSerializer to serialize this object, the [" + o.getClass().getName() + "] " + "class must implement java.io.Serializable.";
                throw StdException.adapt(ex);
            }
        }
    }
    
    public T deserialize(byte[] serialized)  {
        if (serialized == null) {
            String msg = "argument cannot be null.";
            throw new IllegalArgumentException(msg);
        } else {
            ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
            BufferedInputStream bis = new BufferedInputStream(bais);

            try {
                ObjectInputStream ois = new ClassResolvingObjectInputStream(bis);
                T deserialized = (T) ois.readObject();
                ois.close();
                return deserialized;
            } catch (Exception ex) {
                String msg = "Unable to deserialze argument byte array.";
                throw StdException.adapt(ex);
            }
        }
    }
    public static class ClassResolvingObjectInputStream extends ObjectInputStream {
        public ClassResolvingObjectInputStream(InputStream inputStream) throws IOException {
            super(inputStream);
        }

        protected Class<?> resolveClass(ObjectStreamClass osc) {
            return ClassHelper.forName(osc.getName());
        }
    }
}
