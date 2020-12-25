package io.fluentcoding.codemanbot.util;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class CodeManArgumentSet {
    private String[] necessaryArguments = new String[0];
    private String[] optionalArguments = new String[0];
    private boolean lastArgumentVarArgs = false;

    public Optional<Map<String, String>> toMap(String input) {
        Map<String, String> result = new HashMap<>();

        String lastArgument = null;
        if (lastArgumentVarArgs) {
            int paramCount = necessaryArguments.length + optionalArguments.length;
            int currentPos = 0;
            int spaces = 0;

            while (true) {
                currentPos = input.indexOf(" ", currentPos);
                if (currentPos == -1) {
                    break;
                }

                for (int temp = currentPos+1;;temp++) {
                    if (input.length() >= temp && input.charAt(temp) == ' ') {
                        currentPos++;
                    } else {
                        break;
                    }
                }
                spaces++;

                if (spaces == paramCount) {
                    lastArgument = input.substring(currentPos+1);
                    break;
                }
            }
        }

        String[] tokens = input.split("\\s+");

        if (tokens.length <= necessaryArguments.length) {
            return Optional.empty(); /* SHOW COMMAND DESCRIPTION */
        }

        for (int i = 1; i < tokens.length; ++i) {
            int normalizedIncrement = i - 1;
            if (necessaryArguments.length > normalizedIncrement) {
                result.put(necessaryArguments[normalizedIncrement], tokens[i]);
            } else if (optionalArguments.length > normalizedIncrement - necessaryArguments.length) {
                result.put(optionalArguments[normalizedIncrement - necessaryArguments.length], tokens[i]);
            }
        }

        if (lastArgumentVarArgs) {
            if (optionalArguments.length > 0) {
                result.put(optionalArguments[optionalArguments.length-1], lastArgument);
            } else {
                result.put(necessaryArguments[necessaryArguments.length-1], lastArgument);
            }
        }

        return Optional.of(result);
    }

    public CodeManArgumentSet setNecessaryArguments(String... arguments) {
        this.necessaryArguments = arguments;
        return this;
    }

    public CodeManArgumentSet setOptionalArguments(String... arguments) {
        this.optionalArguments = arguments;
        return this;
    }

    public CodeManArgumentSet setLastArgumentVarArg() {
        this.lastArgumentVarArgs = true;
        return this;
    }
}
