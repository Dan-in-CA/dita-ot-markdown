package com.elovirta.dita.markdown;

import static org.apache.commons.io.IOUtils.*;

import org.pegdown.Extensions;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.xml.sax.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * XMLReader implementation for Markdown.
 */
public class MarkdownReader implements XMLReader {

    private EntityResolver resolver;
    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // NOOP
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // NOOP
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return resolver;
    }

    @Override
    public void setDTDHandler(final DTDHandler handler) {
        // NOOP
    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        this.contentHandler = handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(final ErrorHandler handler) {
        this.errorHandler = handler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void parse(final InputSource input) throws IOException, SAXException {
        final char[] markdownContent = getMarkdownContent(input);
        final PegDownProcessor p = new PegDownProcessor(Extensions.ALL);
        final RootNode root = p.parseMarkdown(markdownContent);
        parseAST(root);
    }

    @Override
    public void parse(final String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }

    private char[] getMarkdownContent(final InputSource input) throws IOException {
        final CharArrayWriter out = new CharArrayWriter();
        if (input.getByteStream() != null) {
            final String encoding = input.getEncoding() != null ? input.getEncoding() : "UTF-8";
            final Reader in = new InputStreamReader(input.getByteStream(), encoding);
            try {
                copy(in, out);
            } finally {
                closeQuietly(in);
                //closeQuietly(out);
            }
        } else if (input.getCharacterStream() != null) {
            final Reader in = input.getCharacterStream();
            try {
                copy(in, out);
            } finally {
                closeQuietly(in);
                //closeQuietly(out);
            }
        } else if (input.getSystemId() != null) {
            final URL inUrl;
            try {
                inUrl = new URI(input.getSystemId()).toURL();
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
            final String encoding = input.getEncoding() != null ? input.getEncoding() : "UTF-8";
            final Reader in = new InputStreamReader(inUrl.openStream(), encoding);
            try {
                copy(in, out);
            } finally {
                closeQuietly(in);
                //closeQuietly(out);
            }
        }
        return out.toCharArray();
    }

    private void parseAST(final RootNode root) throws SAXException {
        final ToDitaSerializer s = new ToDitaSerializer(contentHandler, new LinkRenderer());//verbatimSerializerMap
        s.toHtml(root);
    }

}
