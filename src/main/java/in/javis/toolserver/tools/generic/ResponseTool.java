package in.javis.toolserver.tools.generic;

import com.javis.universaltoolbridge.tools.AbstractTool;
import org.graalvm.polyglot.Value;

import static in.javis.toolserver.constants.ToolArgumentsConstants.RESPONSE_TOOL_ARGS;
import static in.javis.toolserver.helpers.PolyglotObjectMapper.convertPolyglotValue;

public class ResponseTool extends AbstractTool {

    public ResponseTool() {
        this.argumentNames = RESPONSE_TOOL_ARGS.getArgs();
    }

    @Override
    public Object execute(Value... args) {
        return convertPolyglotValue(args[0]);
    }
}
