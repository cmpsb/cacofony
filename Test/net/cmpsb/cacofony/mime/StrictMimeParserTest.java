package net.cmpsb.cacofony.mime;

/**
 * @author Luc Everse
 */
public class StrictMimeParserTest extends MimeParserTest<StrictMimeParser> {
    @Override
    public StrictMimeParser getParser() {
        return new StrictMimeParser();
    }
}
