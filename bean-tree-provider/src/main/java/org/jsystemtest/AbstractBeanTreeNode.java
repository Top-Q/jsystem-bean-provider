package org.jsystemtest;

import jsystem.framework.sut.SutValidationError;
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
            //try {
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
                    } else {
                        node.initChildren();
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

                AbstractBeanTreeNode arrayChildNode = new BeanObjectTreeNode(this, componentType.getSimpleName(), arrayElement.getClass(), arrayElement, arrayElementDefaultValue, null);

                if (componentType.isArray()) {
                    arrayChildNode.initArrayElementChildren();
                } else {
                    arrayChildNode.initChildren();
                }
                //arrayChildNode.initChildren();

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

        if(false == isTypePrimitiveOrString(componentType)) {
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

    public static boolean isTypePrimitiveOrString(Class<?> objType) {
        return (objType == String.class
                || objType == Boolean.class
                || isObjTypePrimitiveNumber(objType)
                || objType == Character.class);
    }

    public static boolean isObjTypePrimitiveNumber(Class<?> objType) {
        return(objType.isPrimitive()
                || objType == Integer.class
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
                    objInstance = cls.newInstance();
                }
                //objInstance = cls.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InstantiationException e) {
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

    private static boolean isBean(Type type) {
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
