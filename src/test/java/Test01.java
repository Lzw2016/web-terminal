import org.fusesource.jansi.Ansi;
import org.junit.Test;

/**
 * 作者： lzw<br/>
 * 创建时间：2018-01-26 23:20 <br/>
 */
public class Test01 {


    @Test
    public void t() {
//        AnsiConsole.systemInstall();
        Ansi.setEnabled(true);
        String str = Ansi
                .ansi()
                .eraseScreen()
                .fg(Ansi.Color.RED)
                .a("Hello")
                .fg(Ansi.Color.GREEN)
                .a(" World")
                .reset()
                .toString();
        System.out.println(str);


        str = Ansi
                .ansi()
                .eraseScreen()
                .a("11111111111111111111111111111111\r\n")
                .a(" 222222222222222222222\r\n")
                .cursorUp(2)
                .a("55")
                .reset()
                .a("33")
                .toString();
        System.out.println(str);
    }
}
