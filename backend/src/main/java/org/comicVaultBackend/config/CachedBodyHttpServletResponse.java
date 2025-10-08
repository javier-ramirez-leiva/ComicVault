package org.comicVaultBackend.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream content = new ByteArrayOutputStream();
    private final ServletOutputStream outputStream;
    private final PrintWriter writer;

    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
        this.outputStream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                content.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
        };
        this.writer = new PrintWriter(new OutputStreamWriter(content, StandardCharsets.UTF_8), true);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    public byte[] getContentAsByteArray() {
        return content.toByteArray();
    }

    public void copyBodyToResponse() throws IOException {
        ServletOutputStream responseOutputStream = super.getOutputStream();
        responseOutputStream.write(getContentAsByteArray());
        responseOutputStream.flush();
    }
}
