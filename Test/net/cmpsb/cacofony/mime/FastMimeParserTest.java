package net.cmpsb.cacofony.mime;

/**
 * @author Luc Everse
 */
public class FastMimeParserTest extends MimeParserTest<FastMimeParser> {
    @Override
    public FastMimeParser getParser() {
        return new FastMimeParser();
    }
}
