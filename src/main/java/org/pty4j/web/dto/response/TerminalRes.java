package org.pty4j.web.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-29 12:28 <br/>
 */
@Data
public class TerminalRes implements Serializable {
    public static final String TERMINAL_PRINT = "TERMINAL_PRINT";

    private String type;
    private String text;

    public TerminalRes() {
    }

    public TerminalRes(String text) {
        this.type = TERMINAL_PRINT;
        this.text = text;
    }
}
