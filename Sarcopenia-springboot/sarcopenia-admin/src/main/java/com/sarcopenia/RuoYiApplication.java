package com.sarcopenia;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println("\n" +
                "  年少未觉行路远，步步深陷幻梦中。\n" +
                "  一失再失藏胸臆，几度惊醒又沉沦。\n" +
                "  长成先尝世事深，连连艰难易辙时。\n" +
                "  若危若安不会意，何年何月破茧飞。\n");
    }
}