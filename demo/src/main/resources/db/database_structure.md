# 危化品管理系统数据库结构

## 表结构

### 1. 化学品表 (chemical)
| 字段名 | 类型 | 说明 | 约束 |
|-------|------|------|------|
| id | int(11) | 化学品ID | 主键，自增 |
| name | varchar(100) | 化学品名称 | 非空，唯一 |
| category | varchar(50) | 类别 | 非空 |
| danger_level | varchar(50) | 危险等级 | 非空 |
| storage_condition | varchar(100) | 存储条件 | 非空 |
| warning_threshold | decimal(10,2) | 预警阈值 | 非空 |
| description | varchar(500) | 描述 | 可空 |

### 2. 登录用户表 (user_account)
| 字段名 | 类型 | 说明 | 约束 |
|-------|------|------|------|
| id | int(11) | 用户ID | 主键，自增 |
| email | varchar(100) | 登录邮箱 | 非空，唯一 |
| name | varchar(50) | 昵称/姓名 | 非空 |
| password | varchar(100) | BCrypt加密密码 | 非空 |
| user_type | tinyint | 用户类型：0-普通，1-管理员 | 非空，默认0 |
| status | tinyint | 状态：0-禁用，1-启用 | 非空，默认1 |
| last_login_time | datetime | 最后登录时间 | 可空 |
| create_time | datetime | 创建时间 | 非空，默认当前时间 |
| update_time | datetime | 更新时间 | 非空，自动更新 |

### 3. 注册流水表 (user_register_record)
| 字段名 | 类型 | 说明 | 约束 |
|-------|------|------|------|
| id | int(11) | 记录ID | 主键，自增 |
| email | varchar(100) | 注册邮箱 | 非空 |
| name | varchar(50) | 注册姓名 | 非空 |
| user_type | tinyint | 注册用户类型 | 非空，默认0 |
| register_ip | varchar(64) | 注册IP | 可空 |
| register_channel | varchar(32) | 渠道：web、app等 | 默认web |
| create_time | datetime | 注册时间 | 非空，默认当前时间 |

### 4. 人员表 (man)
| 字段名 | 类型 | 说明 | 约束 |
|-------|------|------|------|
| id | int(11) | 人员ID | 主键，自增 |
| name | varchar(50) | 姓名 | 非空 |
| gender | varchar(10) | 性别 | 可空 |
| phone | varchar(20) | 电话 | 可空 |
| email | varchar(100) | 邮箱 | 可空 |
| department | varchar(50) | 部门 | 可空 |
| position | varchar(50) | 职位 | 可空 |
| create_time | datetime | 创建时间 | 非空，默认当前时间 |

### 5. 库存表 (inventory)
| 字段名 | 类型 | 说明 | 约束 |
|-------|------|------|------|
| id | int(11) | 库存ID | 主键，自增 |
| chemical_id | int(11) | 化学品ID | 非空，外键 |
| current_amount | decimal(10,2) | 当前数量 | 非空 |
| unit | varchar(20) | 单位 | 非空 |
| location | varchar(100) | 存储位置 | 可空 |
| last_check_time | datetime | 最后检查时间 | 可空 |
| create_time | datetime | 创建时间 | 非空，默认当前时间 |

### 6. 预警记录表 (warning_record)
| 字段名 | 类型 | 说明 | 约束 |
|-------|------|------|------|
| id | int(11) | 预警记录ID | 主键，自增 |
| chemical_id | int(11) | 化学品ID | 非空，外键 |
| warning_type | varchar(50) | 预警类型 | 非空 |
| warning_level | varchar(50) | 预警等级 | 非空 |
| warning_content | varchar(500) | 预警内容 | 非空 |
| status | varchar(50) | 处理状态 | 非空，默认'unprocessed' |
| warning_time | datetime | 预警时间 | 非空 |
| handle_time | datetime | 处理时间 | 可空 |
| handler | varchar(100) | 处理人 | 可空 |
| handle_result | varchar(500) | 处理结果 | 可空 |
| create_time | datetime | 创建时间 | 非空，默认当前时间 |

### 7. 使用记录表 (usage_record)
| 字段名 | 类型 | 说明 | 约束 |
|-------|------|------|------|
| id | int(11) | 使用记录ID | 主键，自增 |
| chemical_id | int(11) | 化学品ID | 非空，外键 |
| user_id | int(11) | 使用人ID | 非空，外键 |
| amount | decimal(10,2) | 使用数量 | 非空 |
| unit | varchar(20) | 单位 | 非空 |
| usage_time | datetime | 使用时间 | 非空 |
| usage_purpose | varchar(200) | 使用目的 | 非空 |
| notes | varchar(500) | 备注 | 可空 |
| create_time | datetime | 创建时间 | 非空，默认当前时间 |

## 关系说明

1. **登录用户(user_account) 与 人员(man)**: 可选一对一/一对多
   - 登录用户为系统访问账号，人员表记录具体员工资料
   - 业务需要时可通过邮箱或外键进行关联

2. **化学品(chemical) 与 库存(inventory)**: 一对多关系
   - 一种化学品可以有多个库存记录
   - 每个库存记录只对应一种化学品

3. **化学品(chemical) 与 预警记录(warning_record)**: 一对多关系
   - 一种化学品可以有多个预警记录
   - 每个预警记录只对应一种化学品

4. **化学品(chemical) 与 使用记录(usage_record)**: 一对多关系
   - 一种化学品可以有多个使用记录
   - 每个使用记录只对应一种化学品

5. **人员(man) 与 使用记录(usage_record)**: 一对多关系
   - 一个人员可以有多个使用记录
   - 每个使用记录只对应一个使用人

## 索引说明

1. **user_account表**:
   - 主键索引: `id`
   - 唯一索引: `email`

2. **user_register_record表**:
   - 主键索引: `id`
   - 普通索引: `email`

3. **chemical表**:
   - 主键索引: `id`
   - 唯一索引: `name`

4. **man表**:
   - 主键索引: `id`

5. **inventory表**:
   - 主键索引: `id`
   - 普通索引: `chemical_id`

6. **warning_record表**:
   - 主键索引: `id`
   - 普通索引: `chemical_id`, `warning_type`, `status`, `warning_time`

7. **usage_record表**:
   - 主键索引: `id`
   - 普通索引: `chemical_id`, `user_id`, `usage_time`

## 登录/注册流程

1. **注册**
   - `AuthServiceImpl.register` 对 `RegisterRequest` 做字段校验。
   - 通过 `UserMapper.selectByEmail` 校验唯一性，落库到 `user_account`。
   - 注册信息同步写入 `user_register_record`，记录IP与渠道，便于审计。

2. **登录**
   - `AuthServiceImpl.login` 读取 `user_account`，校验状态/类型/密码。
   - 成功后更新 `last_login_time` 并生成 JWT，返回给前端。

3. **令牌校验**
   - `AuthServiceImpl.validateToken` 解码 JWT，查询 `user_account` 并验证是否过期，提供受保护接口的统一鉴权入口。