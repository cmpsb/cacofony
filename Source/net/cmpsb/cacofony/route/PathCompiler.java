package net.cmpsb.cacofony.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A compiler that translates the routing path format into a regular expression.
 *
 * @author Luc Everse
 */
public class PathCompiler {
    private static final Logger logger = LoggerFactory.getLogger(PathCompiler.class);

    /**
     * The regex all parameter names should comply with.
     */
    private static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]+");

    /**
     * The default regex parameters should comply with.
     */
    public static final String PARAMETER_VALUE_PATTERN = "[^/?#]+";

    /**
     * Compile the path into a regex pattern with named matches.
     *
     * @param path         the path to compile
     * @param requirements a set of regex patterns some routing parameters should comply with
     *
     * @return the path compiled as a regex
     */
    public CompiledPath compile(final String path,
                                final Map<String, String> requirements) {
        final StringBuilder patternBuilder = new StringBuilder();

        // All arguments present in the path.
        final List<String> parameters = new ArrayList<>();

        // See if there's a parameter in there somewhere.
        int parameterStartIndex = path.indexOf('{');
        int parameterEndIndex = -1;
        while (parameterStartIndex != -1) {
            // There is. Copy the part up to now to the regex.
            final int pathPortionStart = parameterEndIndex + 1;
            final int pathPortionEnd = parameterStartIndex;
            patternBuilder.append(path, pathPortionStart, pathPortionEnd);

            // Find where the regex ends. The part in between is the name of the parameter.
            final int nameStartIdx = parameterStartIndex + 1;
            parameterEndIndex = path.indexOf('}', parameterStartIndex + 1);

            if (parameterEndIndex == -1) {
                throw new BadRoutePathException("Route parameter block is not closed.");
            }

            // Get the actual parameter name.
            final int nameEndIdx = parameterEndIndex;
            final String parameter = path.substring(nameStartIdx, nameEndIdx).toLowerCase();

            if (!PARAMETER_NAME_PATTERN.matcher(parameter).matches()) {
                throw new BadRoutePathException(
                        "Route parameter \"" + parameter + "\" is not a valid parameter name."
                );
            }

            // Remember this argument.
            parameters.add(parameter);

            // Get the requirements regex or use ".*" if there is none.
            final String regex = requirements.getOrDefault(parameter, PARAMETER_VALUE_PATTERN);

            // Insert it into the built regex.
            patternBuilder.append("(?<");
            patternBuilder.append(parameter);
            patternBuilder.append(">");
            patternBuilder.append(regex);
            patternBuilder.append(")");

            // Find the next parameter.
            parameterStartIndex = path.indexOf('{', parameterEndIndex + 1);
        }

        // Copy over the remaining part.
        patternBuilder.append(path, parameterEndIndex + 1, path.length());

        // Finish the pattern by ignoring any suffix.
        // Make the trailing slash optional.
        if (path.endsWith("/")) {
            patternBuilder.append("?");
        } else {
            patternBuilder.append("/?");
        }

        // Ignore the query string and fragment identifier.
        patternBuilder.append("(?<QUERY>\\?[^#]*)?(?<FRAGMENT>#.*)?");

        final String regex = patternBuilder.toString();

        logger.debug("Compiled \"{}\" -> \"{}\".", path, regex);

        final Pattern pattern = Pattern.compile(regex);

        return new CompiledPath(path, pattern, parameters);
    }
}
