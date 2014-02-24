package org.jsystemtest;

import jsystem.framework.sut.SutValidationError;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * User: Moshe Goldyan (mgoldyan)
 *
 * Class which represents a TreeNode that contains a bean object.
 * The TreeNode's children represent (recursively) the bean's properties (i.e. its public "get" or "is" methods).
 */
public abstract class AbstractBeanTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 1L;

	public enum NodeType {
		ROOT, BEAN, PRIMITIVE
	}

	/**
	 * Type of the node (enum).
	 */
	private NodeType nodeType;
    protected Vector<AbstractBeanTreeNode> hiddenChildren;

    protected Class<?> objType;
	private String name;
    protected Object defaultValue;
    protected String getMethodNameInParent;

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

    public String getGetMethodNameInParent() {
        return getMethodNameInParent;
    }

    public void setGetMethodNameInParent(String getMethodNameInParent) {
        this.getMethodNameInParent = getMethodNameInParent;
    }

    public AbstractBeanTreeNode(MutableTreeNode parent, NodeType nodeType, String name, Class<?> objType, Object userObject, Object defaultValue, String getMethodNameInParent) {
		super(userObject);
		this.nodeType = nodeType;
        this.objType = objType;
		this.name = name;
		this.parent = parent;
        this.defaultValue = defaultValue;
        this.getMethodNameInParent = getMethodNameInParent;
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
                    // TODO 3 - We need to make sure that methods that start with "is" are NOT named like: "isolateFromList" or "issueToLog"

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

    /**
     * Initializing the array's elements using the array's get method.
     * Supports multi-dim arrays.
     */
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

    /**
     * Generates a new AbstractBeanTreeNode which contains an array element.
     * Note: The newly generated element may also be an array by itself.
     * @return A new AbstractBeanTreeNode which contains an array element.
     */
    public AbstractBeanTreeNode generateNewDefaultArrayElementNode() {
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

    /**
     * @param objType The object's class type
     * @return true iff type is of number (primitive or non-primitive), boolean, character or string.
     */
    public static boolean isTypeNumberOrString(Class<?> objType) {
        return (objType == String.class
                || objType == Boolean.class
                || isObjTypeNumber(objType)
                || objType == Character.class);
    }

    /**
     * @param objType The object's class type
     * @return true iff type is of a number (primitive or non-primitive).
     */
    public static boolean isObjTypeNumber(Class<?> objType) {
        return(objType.isPrimitive() || isObjTypeObjectNumber(objType));
    }

    /**
     * @param objType The object's class type
     * @return true iff type is of a number (non-primitive only).
     */
    public static boolean isObjTypeObjectNumber(Class<?> objType) {
        return (objType == Integer.class
                || objType == Long.class
                || objType == Float.class
                || objType == Double.class
                || objType == Byte.class
                || objType == Short.class);
    }

    /**
     * Generates a new default value for the given object-type.
     *  Also supports multi-dim arrays.
     * @param cls The object's class type
     * @return A new default (object) value for the given object-type.
     */
    public Object getObjectDefaultValueInstance(Class<?> cls) {
        if (cls == Boolean.TYPE || cls == boolean.class || cls == Boolean.class) {
            return Boolean.FALSE;
        } else if (cls == Byte.TYPE || cls == byte.class || cls == Byte.class) {
            return (byte) 0;
        } else if (cls == Short.TYPE || cls == short.class || cls == Short.class) {
            return (short) 0;
        } else if (cls == Integer.TYPE || cls == int.class || cls == Integer.class) {
            return 0;
        } else if (cls == Long.TYPE || cls == long.class || cls == Long.class) {
            return (long) 0;
        }  else if (cls == Float.TYPE || cls == float.class || cls == Float.class) {
            return 0.0F;
        }  else if (cls == Double.TYPE || cls == double.class || cls == Double.class) {
            return 0.0;
        }  else if (cls == Character.TYPE || cls == char.class || cls == Character.class) {
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
                AbstractBeanTreeNode parentNode = childNode.getBeanParent();
                return (parentNode.objType.isArray());
            }
        }

        return false;
    }

    /**
     * @return The string representation of the contained user-object (toString method).
     *  For null user-object, an empty String is returned.
     */
	public Object getValue() {
		if (userObject != null) {
			return userObject.toString();
		}
		return "";
	}

    /**
     * Sets a new value to the contained user-object, using the parent's "set" method (when possible).
     * @param value The new value to set.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void setValue(Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object valueToSet = value;
        if(false == objType.isArray()) {
            valueToSet = parseObjectFromString(value.toString());

            if(null == valueToSet) {
                valueToSet = value;
            }
        }

        if(valueToSet != null) {
            this.setValueUsingSetMethod(valueToSet);
            this.setUserObject(valueToSet);
        }
    }

    /**
     * Sets the user-object's value with a new value using the parent's "set" method.
     * @param value The new value to set to the node's user-object.
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void setValueUsingSetMethod(Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // TODO 3 - beware of setting the root node (no parent) - possible??
        AbstractBeanTreeNode parentNode = this.getBeanParent();
        if(parentNode != null && parentNode.getUserObject() == null) {
            parentNode.setValueUsingSetMethod(parentNode.getObjectDefaultValueInstance(parentNode.objType));
        }
        Object objInstance = parentNode.getUserObject();

        if(parentNode.objType.isArray()) {
            int indexInParent = parentNode.getIndex(this);
            Array.set(parentNode.userObject, indexInParent, value);
        } else {
            // Checking also for "is" (not only for "get")
            String setMethodNameInParent = "set" + (getGetMethodNameInParent().startsWith("get") ? getGetMethodNameInParent().substring(3) : getGetMethodNameInParent().substring(2));
            Method setMethodInParent = objInstance.getClass().getMethod(setMethodNameInParent, new Class[]{this.objType});
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

    /**
     * Inserts a new array element at a specific position.
     * @param arrayNode
     * @param newArrayElementObj
     * @param position Inserting the new object element at a specific position in the array object.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void insertNewArrayElementAt(AbstractBeanTreeNode arrayNode, Object newArrayElementObj, int position) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object arrayObj = arrayNode.getUserObject();
        int arrayOriginalLength = Array.getLength(arrayObj);
        Class componentType = arrayNode.objType.getComponentType();
        Object newArrayObj = Array.newInstance(componentType, arrayOriginalLength + 1);

        int i = 0;
        for (; i < position; i++) {
            Object arrayElement = Array.get(arrayObj, i);
            Array.set(newArrayObj, i, arrayElement);
        }

        Array.set(newArrayObj, i, newArrayElementObj);

        for (; i < arrayOriginalLength; i++) {
            Object arrayElement = Array.get(arrayObj, i);
            Array.set(newArrayObj, (i+1), arrayElement);
        }

        // If grandparent is also an array : set its relevant element with the new array
        setArrayObjectToAncestryArrayNodes(arrayNode, newArrayObj);
    }


    /**
     * Removing an array-element in the original user-object (actually replacing with a new array)
     * @param parentNode The relevant TreeNode that holds the original array object.
     * @param nodeIndex Index of the element (to be removed) in the array
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
        setArrayObjectToAncestryArrayNodes(parentNode, newArrayObj);
    }

    /**
     *Sets the user-object to the array node. When grandparent is also an array - also sets its relevant element with the new array.
     * @param arrayNode The relevant TreeNode that holds the original array object.
     * @param newArrayObj The new array object to replace the original array object.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void setArrayObjectToAncestryArrayNodes(AbstractBeanTreeNode arrayNode, Object newArrayObj) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        AbstractBeanTreeNode grandParentNode = arrayNode.getBeanParent();
        if(null != grandParentNode) {
            if(grandParentNode.objType.isArray()) {
                int parentIndexInGrandParent = grandParentNode.getIndex(arrayNode);
                Array.set(grandParentNode.userObject, parentIndexInGrandParent, newArrayObj);
            } else {
                arrayNode.setValueUsingSetMethod(newArrayObj);
            }
        }

        arrayNode.setUserObject(newArrayObj);
    }


    /**
     * Parses the text-value of String, char, enum, boolean or number into its actual value by its object type.
     * @param value The given text.
     * @return Parsed object from the given text, or null when object type is unsupported.
     */
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

    private void instantiateAncestry() {
        AbstractBeanTreeNode parentNode = this.getBeanParent();
        while(parentNode != null && parentNode.getUserObject() == null) {
            try {
                parentNode.setValueUsingSetMethod(parentNode.getObjectDefaultValueInstance(parentNode.objType));
                parentNode = parentNode.getBeanParent();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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


	public String getClassName() {
		if (userObject != null) {
			return userObject.getClass().getSimpleName();
		}
		return "";
	}

    public Vector<AbstractBeanTreeNode> getVisibleChildren() {
        return  this.children;
    }

	public Vector<AbstractBeanTreeNode> getHiddenChildren() {
		return this.hiddenChildren;
	}


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
