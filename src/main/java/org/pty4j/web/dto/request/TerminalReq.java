package org.pty4j.web.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-29 11:51 <br/>
 */
@Data
public class TerminalReq implements Serializable {

    public static final String TERMINAL_COMMAND = "TERMINAL_COMMAND";
    public static final String TERMINAL_RESIZE = "TERMINAL_RESIZE";

    @NotBlank
    @Pattern(regexp = "TERMINAL_INIT|TERMINAL_READY|TERMINAL_COMMAND|TERMINAL_RESIZE")
    private String type;
    private String command;

    @Range(min = 1, max = 500)
    private Integer columns;
    @Range(min = 1)
    private Integer rows;

    public TerminalReq() {
    }

    public TerminalReq(String type, String command) {
        this.type = type;
        this.command = command;
    }

    public TerminalReq(int columns, int rows) {
        this.type = TERMINAL_RESIZE;
        this.columns = columns;
        this.rows = rows;
    }
}
