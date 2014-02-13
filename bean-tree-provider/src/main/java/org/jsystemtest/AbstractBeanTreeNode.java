package org.jsystemtest;

import jsystem.framework.sut.SutValidationError;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
//import org.apache.commons.lang.ClassUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public abstract class AbstractBeanTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	public enum NodeType {
		ROOT, BEAN, PRIMITIVE
	}

	/**
	 * Type of the node.
	 */
	private NodeType nodeType;
    protected Vector<AbstractBeanTreeNode> hiddenChildren;

    protected Class<?> objType;
	private String name;
    protected Object defaultValue;
    protected String getMethodInParent;

    public Class<?> getObjType() {
        return objType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public AbstractBeanTreeNode getBeanParent() {
        return (AbstractBeanTreeNode) this.getParent();
    }

    public String getGetMethodInParent() {
        return getMethodInParent;
    }

    public void setGetMethodInParent(String getMethodInParent) {
        this.getMethodInParent = getMethodInParent;
    }

    public AbstractBeanTreeNode(MutableTreeNode parent, NodeType nodeType, String name, Class<?> objType, Object userObject, Object defaultValue, String getMethodInParent) {
		super(userObject);
		this.nodeType = nodeType;
        this.objType = objType;
		this.name = name;
		this.parent = parent;
        this.defaultValue = defaultValue;
        this.getMethodInParent = getMethodInParent;
	}

    @SuppressWarnings("unchecked")
	protected void initChildren() {
		if (null == children) {
			children = new Vector<AbstractBeanTreeNode>();
		}
		if (null == hiddenChildren){
			hiddenChildren = new Vector<AbstractBeanTreeNode>();
		}

        Object objInstance = userObject;
        if (null == userObject) {
            if(objType.isArray()) {
                objInstance = Array.newInstance(objType, 0);
            } else {
                objInstance = this.getObjectDefaultValueInstance(objType);
            }
        }
		Method[] methods = objInstance.getClass().getMethods();
		for (Method method : methods) {
            String methodName = method.getName();
			if ((methodName.startsWith("get") || methodName.startsWith("is"))
					&& method.getParameterTypes().length == 0) {
				if (method.getName().equals("getClass")) {
					continue;
				}
				if (objInstance.getClass().getName().equals("java.lang.String")) {
                    return;
				}
				try {
					Class<?> type = method.getReturnType();
					Object childObj = method.invoke(objInstance, new Object[]{});

                    // Works only when there's a default constructor...
                    Object newInstance = objInstance.getClass().newInstance();
                    //Object newInstance = objInstance != null ? objInstance.getClass().newInstance() : Class.forName(this.objType.getCanonicalName()).newInstance();
					//TODO : Add factory
                    // TODO 3 - We need to make sure that methods that start with "is" are not named like: "isolateFromList" or "issueToLog"
                    // TODO 3 - Handle Arrays & Lists

                    String name = methodName.startsWith("get") ? methodName.replaceFirst("get", "") : methodName.replaceFirst("is", "");
                    Object defaultValue = method.invoke(newInstance, new Object[] {});
                    /*final */BeanObjectTreeNode node = new BeanObjectTreeNode(this, name, type, childObj, defaultValue, methodName);

                    if (type.isArray()) {
                        node.initArrayElementChildren();
                    } else if (false == type.isEnum()) {
                        node.initChildren();
                    } else {
                        System.out.println(childObj);
                    }

                    if (childObj == null || childObj.equals(defaultValue)) {
                    //It is important to set the parent to null unless we want null pointer exception later on
                        node.setParent(null);
                        hiddenChildren.add(node);
                    }else {
                        children.add(node);

                    }

				} catch (Exception e) {
					System.out.println("Failed to invoke method " + methodName);
                    e.printStackTrace();
				}
			}
		}

	}

    public void initArrayElementChildren() {
        Object userObj = this.userObject;
        if(userObj != null) {
            if (null == this.children) {
                this.children = new Vector<AbstractBeanTreeNode>();
            }

            int length = Array.getLength(userObj);
            for (int i = 0; i < length; i ++) {
                Object arrayElement = Array.get(userObj, i);
                Class<?> componentType = this.getObjType().getComponentType();
                Object arrayElementDefaultValue = this.getObjectDefaultValueInstance(componentType);

                AbstractBeanTreeNode arrayChildNode = new BeanObjectTreeNode(this, componentType.getSimpleName(), componentType/*arrayElement.getClass()*/, arrayElement, arrayElementDefaultValue, null);

                if (componentType.isArray()) {
                    arrayChildNode.initArrayElementChildren();
                } else {
                    arrayChildNode.initChildren();
                }

                this.children.add(arrayChildNode);
            }
        }
    }

    public AbstractBeanTreeNode addNewDefaultArrayElementChild() {
        if (null == this.children) {
            this.children = new Vector<AbstractBeanTreeNode>();
        }

        AbstractBeanTreeNode arrayChildNode = null;
        Class<?> componentType = this.getObjType().getComponentType();
        Object arrayElement = null;
        String nodeName = componentType.getSimpleName();
        String methodName = null;

        if(false == isTypeNumberOrString(componentType)) {
            Object parentObj = this.userObject;
            Method method = findGetMethodInParentByName(parentObj, nodeName);
            if(method != null) {
                try {
                    arrayElement = method.invoke(parentObj, new Object[]{});
                    methodName = method.getName();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InvocationTargetException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        if(arrayElement == null) {
            arrayElement = this.getObjectDefaultValueInstance(componentType);
        }

        Object arrayElementDefaultValue = this.getObjectDefaultValueInstance(componentType);
        arrayChildNode = new BeanObjectTreeNode(this, componentType.getSimpleName(), arrayElement.getClass(), arrayElement, arrayElementDefaultValue, methodName);
        this.children.add(arrayChildNode);

        if(false == componentType.isPrimitive()) {
            arrayChildNode.initChildren();
        }

        return arrayChildNode;
    }

    private static Method findGetMethodInParentByName(Object parentObj, String nodeName) {
        Method method = null;
        try {
            method = parentObj.getClass().getMethod("get"+nodeName, new Class[]{});
        } catch (NoSuchMethodException e) {
            try {
                method = parentObj.getClass().getMethod("is"+nodeName, new Class[]{});
            } catch (NoSuchMethodException e1) {
                System.out.println("No getter method found for : " + nodeName);
            }
        }

        return method;
    }

    public static boolean isTypeNumberOrString(Class<?> objType) {
        return (objType == String.class
                || objType == Boolean.class
                || isObjTypeNumber(objType)
                || objType == Character.class);
    }

    public static boolean isObjTypeNumber(Class<?> objType) {
        return(objType.isPrimitive() || isObjTypeObjectNumber(objType));
    }

    public static boolean isObjTypeObjectNumber(Class<?> objType) {
        return (objType == Integer.class
                || objType == Long.class
                || objType == Float.class
                || objType == Double.class
                || objType == Byte.class
                || objType == Short.class);
    }

    public Object getObjectDefaultValueInstance(Class<?> cls) {
        if (cls == Boolean.TYPE) {
            return Boolean.FALSE;
        } else if (cls == Byte.TYPE) {
            return (byte) 0;
        } else if (cls == Short.TYPE) {
            return (short) 0;
        } else if (cls == Integer.TYPE) {
            return 0;
        } else if (cls == Long.TYPE) {
            return (long) 0;
        }  else if (cls == Float.TYPE) {
            return 0.0F;
        }  else if (cls == Double.TYPE) {
            return 0.0;
        }  else if (cls == Character.TYPE) {
            return ((char) 0);
        }  else {
            Object objInstance = null;
            try {
                if(cls.isArray()) {
                    objInstance = Array.newInstance(cls.getComponentType(), 0);
                } else {
                    if(isObjTypeObjectNumber(cls)) {
                        objInstance = cls.getConstructor(String.class).newInstance("0");
                    } else {
                        objInstance = cls.newInstance();
                    }
                }
                //objInstance = cls.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return objInstance;
        }
    }


    public static boolean isParentAnArray(AbstractBeanTreeNode childNode) {
        if(null != childNode && childNode.getParent() != null) {
            if(childNode.getParent() instanceof AbstractBeanTreeNode) {
                AbstractBeanTreeNode parentNode = (AbstractBeanTreeNode) childNode.getParent();
                return (parentNode.objType.isArray());
            }
        }

        return false;
    }


    public String toString() {
        return name;
    }

    public boolean isLeaf() {
        return this.getChildCount() == 0;
    }
	
	public Object getName() {
		return name;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void getValidationErrors(ArrayList<SutValidationError> nodeErrors) {
		// TODO Auto-generated method stub

	}

	public Object getValue() {
		if (userObject != null) {
			return userObject.toString();
		}
		return "";
	}

    public void setValue(Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object parsedString = parseObjectFromString(value.toString());
        if(parsedString != null) {
            this.setValueUsingSetMethod(parsedString);
            this.setUserObject(parsedString);
        }
    }

    /**
     * Switching the positions of 2 array elements in the original user-object.
     * @param parentNode - The array itself
     * @param nodeIndex1 - The 1st array's-value index to be switched
     * @param nodeIndex2 - The 2nd array's-value index to be switched
     */
    public void substitueArrayElementsOrder(AbstractBeanTreeNode parentNode,  int nodeIndex1, int nodeIndex2) {
        Object arrayObj = parentNode.getUserObject();
        Object arrayElement1 = Array.get(arrayObj, nodeIndex1);
        Object arrayElement2 = Array.get(arrayObj, nodeIndex2);

        Array.set(arrayObj, nodeIndex1, arrayElement2);
        Array.set(arrayObj, nodeIndex2, arrayElement1);
    }


    public void insertNewArrayElement() {
         // TODO 4 - Support add element
    }


    /**
     * Removing an array-element in the original user-object (actually replacing with a new array)
     * @param parentNode - The array itself
     * @param nodeIndex - Index of the element (to be removed) in the array
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void removeArrayElement(AbstractBeanTreeNode parentNode, int nodeIndex) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object arrayObj = parentNode.getUserObject();
        int arrayLength = Array.getLength(arrayObj);
        Class componentType = parentNode.userObject.getClass().getComponentType();
        Object newArrayObj = Array.newInstance(componentType, arrayLength - 1);

        int i = 0;
        for (; i < nodeIndex; i++) {
            Object arrayElement = Array.get(arrayObj, i);
            Array.set(newArrayObj, i, arrayElement);
        }

        i = nodeIndex + 1; // Skipping the removed element
        for (; i < arrayLength; i++) {
            Object arrayElement = Array.get(arrayObj, i);
            Array.set(newArrayObj, (i-1), arrayElement);
        }

        // If grandparent is also an array : set its relevant element with the new array
        AbstractBeanTreeNode grandParentNode = parentNode.getBeanParent();
        if(null != grandParentNode) {
            if(grandParentNode.objType.isArray()) {
                int parentIndexInGrandParent = grandParentNode.getIndex(parentNode);
                Array.set(grandParentNode.userObject, parentIndexInGrandParent, newArrayObj);
            } else {
                parentNode.setValueUsingSetMethod(newArrayObj);
            }
        }

        parentNode.setUserObject(newArrayObj);
    }

    public Object parseObjectFromString(String value) {
        if(objType == String.class) {
            return value;
        }

        try {
            if (objType == Boolean.class) {
                if(value != null) {
                    if(value.equalsIgnoreCase("true")) {
                        return true;
                    } else if(value.equalsIgnoreCase("false")) {
                        return false;
                    }
                    //return (value.equalsIgnoreCase("true") ? true : (value.equalsIgnoreCase("false") ? false : null));
                }
            } else if (objType.isEnum()) {
                //return objType.getDeclaredField(value);
                return Enum.valueOf((Class<Enum>)objType, value);
            } else if (objType == Byte.class || objType == byte.class) {
                return Byte.parseByte(value);
            } else if (objType == Short.class || objType == short.class) {
                return Short.parseShort(value);
            } else if (objType == Integer.class || objType == int.class) {
                return Integer.parseInt(value);
            } else if (objType == Long.class || objType == long.class) {
                return Long.parseLong(value);
            }  else if (objType == Float.class || objType == float.class) {
                return Float.parseFloat(value);
            }  else if (objType == Double.class || objType == double.class) {
                return Double.parseDouble(value);
            }  else if (objType == Character.class) {
                return (value.length() == 1 ? value.charAt(0) : null);
            } else {
                System.out.println("Unsupported object type to parse from String : " + getObjType().getName());
            }
        } catch (NumberFormatException nfe) {
            // TODO 3 - Highlight the parsing problem to user
        }

        return null;
    }

    private void setValueUsingSetMethod(Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // TODO 3 - beware of setting the root node (no parent) - possible??
        AbstractBeanTreeNode parentNode = (AbstractBeanTreeNode)this.parent;
        Object objInstance = parentNode.getUserObject();

        if(parentNode.objType.isArray()) {
            int indexInParent = parentNode.getIndex(this);
            Array.set(parentNode.userObject, indexInParent, value);
        } else {
            // Checking also for "is" (not only for "get")
            String setMethodNameInParent = "set" + (getGetMethodInParent().startsWith("get") ? getGetMethodInParent().substring(3) : getGetMethodInParent().substring(2));
            Method setMethodInParent = objInstance.getClass().getMethod(setMethodNameInParent, new Class[]{userObject.getClass()});
            if(objType.isEnum()) {
                setMethodInParent.invoke(objInstance, new Object[] {Enum.valueOf((Class<Enum>) userObject.getClass(), value.toString())});
            } else {
                Object valueToSet = value;
                if(value.getClass().isArray()) {
                    Class valueComponentType = value.getClass().getComponentType();
                    Class setMethodComponentType = setMethodInParent.getParameterTypes()[0].getComponentType();
                    if(setMethodComponentType.isPrimitive() && false == valueComponentType/*userObject.getClass()*/.isPrimitive()) {
                        valueToSet = this.convertObjectArrayToPrimitiveArray(value, setMethodComponentType);
                    } else if(false == setMethodComponentType.isPrimitive() && valueComponentType/*userObject.getClass()*/.isPrimitive()) {

                        valueToSet = this.convertPrimitiveArrayToObjectArray(value, setMethodComponentType);
                    }
                }
                this.userObject = valueToSet;
                setMethodInParent.invoke(objInstance, new Object[] { valueToSet });
            }
        }
    }


    private Object convertObjectArrayToPrimitiveArray(Object objArray, Class destComponentType) {
        if (destComponentType == byte.class) {
            return ArrayUtils.toPrimitive(((Byte[]) (objArray)));
        } else if (destComponentType == short.class) {
            return ArrayUtils.toPrimitive(((Short[]) (objArray)));
        } else if (destComponentType == int.class) {
            return ArrayUtils.toPrimitive(((Integer[]) (objArray)));
        } else if (destComponentType == long.class) {
            return ArrayUtils.toPrimitive(((Long[]) (objArray)));
        }  else if (destComponentType == float.class) {
            return ArrayUtils.toPrimitive(((Float[])(objArray)));
        }  else if (destComponentType == double.class) {
            return ArrayUtils.toPrimitive(((Double[])(objArray)));
        }  else if (destComponentType == char.class) {
            return ArrayUtils.toPrimitive(((Character[])(objArray)));
        } else {
            System.out.println("Unsupported object array type to covert to : " + destComponentType.getName());
            return objArray;
        }
    }


    private Object convertPrimitiveArrayToObjectArray(Object primitiveArray, Class destComponentType) {
        if (destComponentType == Byte.class) {
            return ArrayUtils.toObject(((byte[])(primitiveArray)));
        } else if (destComponentType == Short.class) {
            return ArrayUtils.toObject(((short[]) (primitiveArray)));
        } else if (destComponentType == Integer.class) {
            return ArrayUtils.toObject(((int[]) (primitiveArray)));
        } else if (destComponentType == Long.class) {
            return ArrayUtils.toObject(((long[]) (primitiveArray)));
        }  else if (destComponentType == Float.class) {
            return ArrayUtils.toObject(((float[]) (primitiveArray)));
        }  else if (destComponentType == Double.class) {
            return ArrayUtils.toObject(((double[]) (primitiveArray)));
        }  else if (destComponentType == Character.class) {
            return ArrayUtils.toObject(((char[])(primitiveArray)));
        } else {
            System.out.println("Unsupported primitive array type to covert to : " + destComponentType.getName());
            return primitiveArray;
        }
    }


	public String getClassName() {
		if (userObject != null) {
			return userObject.getClass().getSimpleName();
		}
		return "";
	}

	public String getArraySuperClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<AbstractBeanTreeNode> getHiddenChildren() {
		return hiddenChildren;
	}

    /*private static boolean isBean(Type type) {
        // TODO 3 - might contain "int[]" or other strings that contain "int", "short" etc.
        // Beans should have getters and setters methods, default constructor and implement Serializable
        if (type.toString().contains("java.lang.String")) {
            return false;
        }
        if (type.toString().contains("int")) {
            return false;
        }
        if (type.toString().contains("boolean")) {
            return false;
        }
        if (type.toString().contains("float")) {
            return false;
        }
        if (type.toString().contains("double")) {
            return false;
        }
        if (type.toString().contains("long")) {
            return false;
        }
        if (type.toString().contains("byte")) {
            return false;
        }
        if (type.toString().contains("short")) {
            return false;
        }
        if (type.toString().contains("char")) {
            return false;
        }

        return true;
    }*/


    private <T> T instantiate(Class<T> cls, Map<String, ? extends Object> args) throws Exception
    {
        // Create instance of the given class
        final Constructor<T> constr = (Constructor<T>) cls.getConstructors()[0];
        final List<Object> params = new ArrayList<Object>();
        for (Class<?> pType : constr.getParameterTypes())
        {
            params.add((pType.isPrimitive()) ? ClassUtils.primitiveToWrapper(pType).newInstance() : null);
        }
        final T instance = constr.newInstance(params.toArray());

        // Set separate fields
        for (Map.Entry<String, ? extends Object> arg : args.entrySet()) {
            Field f = cls.getDeclaredField(arg.getKey());
            f.setAccessible(true);
            f.set(instance, arg.getValue());
        }

        return instance;
    }

}
