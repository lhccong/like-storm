# Like-Storm 高并发点赞系统

## 项目介绍

Like-Storm 是一个基于 Spring Boot 3 和 Java 21 开发的高性能点赞系统，专为社交平台和内容社区设计。本项目完整覆盖了点赞系统的核心技术，从基础功能开发到高并发优化，再到企业级高可用架构，为开发者提供了一个完整的点赞系统解决方案。

## 技术栈

- **核心框架**：Spring Boot 3
- **开发语言**：Java 21
- **数据库**：TiDB（分布式数据库）
- **缓存**：Redis + Caffeine（多级缓存）
- **消息队列**：Apache Pulsar
- **容器化**：Docker
- **监控**：Prometheus + Grafana

## 核心特性

- 🚀 **高性能设计**
  - 多级缓存策略（Caffeine + Redis）
  - 消息队列削峰
  - HeavyKeeper 热点数据识别
  - 分布式数据库支持

- 🔄 **高可用架构**
  - 分布式系统设计
  - 服务容错机制
  - 数据一致性保证

- 📊 **可观测性**
  - Prometheus 指标监控
  - Grafana 可视化面板
  - 系统性能分析

- 🛡️ **企业级特性**
  - 完整的监控告警
  - 系统容错机制
  - 数据一致性保证

## 项目结构

```
like-storm/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/likestorm/
│   │   │       ├── api/        # API 接口
│   │   │       ├── service/    # 业务逻辑
│   │   │       ├── model/      # 数据模型
│   │   │       └── config/     # 配置类
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
├── docker/                      # Docker 相关配置
├── docs/                        # 项目文档
└── scripts/                     # 部署脚本
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose
- TiDB
- Redis
- Apache Pulsar

### 本地开发

1. 克隆项目
```bash
git clone https://github.com/yourusername/like-storm.git
cd like-storm
```

2. 启动依赖服务
```bash
docker-compose up -d
```

3. 运行项目
```bash
mvn spring-boot:run
```

## 性能优化

- 使用 Caffeine 本地缓存减少 Redis 访问
- 采用 Pulsar 消息队列进行流量削峰
- 通过 HeavyKeeper 算法识别并优化热点数据
- 分布式数据库 TiDB 提供水平扩展能力

## 监控指标

- 系统吞吐量
- 响应时间
- 缓存命中率
- 消息队列积压情况
- 数据库性能指标

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进项目。

## 许可证

本项目采用 MIT 许可证，详情请参见 [LICENSE](LICENSE) 文件。

## 联系方式

如有任何问题或建议，请通过以下方式联系我们：

- 提交 Issue
- 发送邮件至：[your-email@example.com]

## 致谢

感谢所有为本项目做出贡献的开发者。 