package nodegraph.nodes.logic;

import com.google.gson.JsonObject;
import compiler.FunctionDirective;
import graphics.GLDataType;
import nodegraph.nodes.TemplateNode;

public class NotNode extends TemplateNode {

    public NotNode(JsonObject initialization_data) {
        super("Equal", FunctionDirective.EQUALS, GLDataType.BOOL, initialization_data);
    }
}
