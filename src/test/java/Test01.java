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
                .a(" World ")
                .reset()
                .a("AAA")
                .toString();
        System.out.println(str);


        str = Ansi
                .ansi()
                .reset() // 清除颜色 \033[m

                // 清除屏幕 光标位置不会动
                .eraseScreen() // 清除屏幕(所有) \033[2J
                .eraseScreen(Ansi.Erase.BACKWARD) // 清除屏幕(前面的内容) \033[1J
                .eraseScreen(Ansi.Erase.FORWARD) // 清除屏幕(后面的内容) \033[0J

                // 清除一行 光标位置不会动
                .eraseLine() //清除一行(只清除光标后面的内容) \033[K
                .eraseLine(Ansi.Erase.BACKWARD) //清除一行(只清除光标前面的内容) \033[1K
                .eraseLine(Ansi.Erase.FORWARD) //清除一行(只清除光标后面的内容) \033[0K

                .cursorUp(5) // 光标上移
                .cursorDown(5) // 光标下移
                .cursorLeft(5) // 光标左移
                .cursorRight(5) // 光标右移

                // 上下移行 光标会到第一列
                .cursorDownLine() // 向下换行
                .cursorDownLine(5) // 向下换行
                .cursorUpLine() // 向上换行
                .cursorUpLine(5) // 向上换行

                .cursorToColumn(5) // 移动光标到第几列
                .cursor(1, 1) // 设置光标位置
                .restoreCursorPosition() // 取出保存的光标位置来使用
                .saveCursorPosition() // 保存目前的光标位置
                .newline() // 回车换行


                .render("@|red Hello|@ @|green World|@")
                .render("@|red %1$s|@ @|green %2$s|@", "Hello", "World")

                .fg(Ansi.Color.RED)
                .bg(Ansi.Color.WHITE)
                .fgBright(Ansi.Color.RED)
                .bgBright(Ansi.Color.WHITE)

                .bold()
                .boldOff()

                .scrollDown(5)
                .scrollUp(5)

                .toString();
        System.out.println(str);
    }

    @Test
    public void t01() {
        String[] str = "123\r\n456\r\n789\n000\r\n".split("\r\n|\n");
        System.out.println(str.length);
        for (String s : str) {
            System.out.println(s);
        }
    }
}
