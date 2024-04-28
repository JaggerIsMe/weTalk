package com.weTalk;

import com.weTalk.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

@Component("initRun")
public class InitRun implements ApplicationRunner {

    public static final Logger logger = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void run(ApplicationArguments args) {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            logger.info("主人！服务已启动成功！");
        }catch (SQLException e){
            logger.error("数据库连接失败，请检查数据库配置");
        }catch (RedisConnectionFailureException e){
            logger.error("Redis连接失败，请检查Redis配置");
        }catch (Exception e){
            logger.error("主人！抱歉！服务启动失败！", e);
        }
    }
}
