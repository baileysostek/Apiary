package nodegraph;

import com.google.gson.JsonObject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NodeRegistry {
    private static NodeRegistry instance;

    private HashSet<Class<? extends Node>> templates = new HashSet<>();

    private NodeRegistry() {
        // This is some wild reflection code adapted from here:
        // https://www.edureka.co/community/93462/how-to-find-all-the-classes-of-a-package-in-java
        String NODEGRAPH_PACKAGE = "nodegraph"; // All nodes live in this class.
        Reflections reflections = new Reflections((new ConfigurationBuilder()).setScanners(new Scanners[]{Scanners.SubTypes, Scanners.TypesAnnotated}).setUrls(ClasspathHelper.forPackage(NODEGRAPH_PACKAGE, new ClassLoader[0])).filterInputsBy((new FilterBuilder()).includePackage(NODEGRAPH_PACKAGE)));
        Set classes = reflections.getSubTypesOf(Node.class);
        Class[] node_child_classes = (Class[])classes.toArray(new Class[classes.size()]);
        for(int i = 0; i < node_child_classes.length; i++){
            Class node_child_class = node_child_classes[i];
            // Prevent instantiation of new Abstract class instance. Always need to instantiate the child-class not parent abstract class.
            if(!Modifier.isAbstract(node_child_class.getModifiers())){
                templates.add(node_child_class); // Populate our templates based on reflection.
            }
        }
    }

    public static void initialize() {
        if (instance == null) {
            instance = new NodeRegistry();
        }
    }

    public static NodeRegistry getInstance() {
        return instance;
    }

    public Node getNodeFromClass(JsonObject save_data) {
        // First thing we need to do is get the class object from save_data.
        if(!save_data.has("class")){
            System.out.println("Error: Cannot instantiate an instance of a node from a save object without a class member.");
            return null;
        }

        // We are going to try to resolve an instance of this class from the name.
        try {
            Class<? extends Node> node_class = (Class<? extends Node>) Class.forName(save_data.get("class").getAsString());
            Node node_instance = getNodeFromClass(node_class, save_data);
            return node_instance;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Node getNodeFromClass(Class<? extends Node> node_class, JsonObject initialization_data) {
        // Prevent instantiation of new Abstract class instance. Always need to instantiate the child-class not parent abstract class.
        if(Modifier.isAbstract(node_class.getModifiers())){
            System.out.println(String.format("Error: Cannot instantiate new instance of abstract class:%s.", node_class));
            return null;
        }

        // Check if we have an entry for this type of object.
        if(templates.contains(node_class)){
            // Instantiate a new instance of this class.
            for(Constructor constructor : node_class.getConstructors()){
                if(constructor.getParameterCount() == 1){
                    try {
                        Node node = (Node) constructor.newInstance(initialization_data);
                        return node;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // No node found.
        return null;
    }

    public Collection<Class<? extends Node>> getRegisteredNodes() {
        return this.templates;
    }
}
