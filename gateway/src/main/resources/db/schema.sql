CREATE TABLE `t_token` (
  `id` INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
  `user_id` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户id', 
  `token` VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'token信息',
  `expire_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'token失效时间',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(50) NOT NULL DEFAULT '' COMMENT '备注'
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='登录令牌表';

CREATE TABLE `t_tenant_db` (
  `id` INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
  `tenant_id` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '租户id',
  `app_dbname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'appDB名字', 
  `cust_dbname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'custDB名字', 
  `hg_dbname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'hgDB名字', 
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(50) NOT NULL DEFAULT '' COMMENT '备注'
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='租户数据库关系表';

CREATE TABLE `t_api_route` (
  `id` INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
  `tenant_id` VARCHAR(50) NOT NULL COMMENT '租户id',
  `path` VARCHAR(20) NOT NULL DEFAULT '/**' COMMENT '路由path',
  `url` VARCHAR(120) NOT NULL DEFAULT '' COMMENT 'host:port',
  `strip_prefix` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否去前缀',
  `retryable` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否重试',
  `service_id` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '服务id',
  `api_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'api名称',
  `enabled` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否开启',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(50) NOT NULL DEFAULT '' COMMENT '备注'
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='API网关规则';


INSERT INTO t_api_route (tenant_id, path, strip_prefix, url, enabled, remark) 
						VALUES ('initialization_id', '/checkout/**', 0, 'http://localhost:8091', 1, "初始化匹配路径");
INSERT INTO t_api_route (tenant_id, path, strip_prefix, url, enabled, remark) 
						VALUES ('initialization_id', '/available/**', 0, 'http://localhost:8091', 1, "初始化匹配路径");
						
INSERT INTO t_api_route (tenant_id, path, strip_prefix, url, enabled) 
						VALUES ('pppp', '/checkout/**', 0, 'http://localhost:8090', 1);
INSERT INTO t_api_route (tenant_id, path, strip_prefix, url, enabled) 
						VALUES ('xxxx', '/available/**', 0, 'http://localhost:8091', 1);