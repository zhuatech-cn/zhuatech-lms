# 学习路径先修条件核验

> 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/
> 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。

## 实现与使用

验证课程先修图、已完成记录和循环依赖，计算可学习课程、锁定原因及剩余学习时长，不自动授予资格。

最多200课程，每课程最多100先修，时长1–100000分钟。先修必须是已声明课程，环路和自引用拒绝。已完成课程若缺失先修完成证据会标记不一致，不偷偷抹除历史。剩余时长为未完成课程加总，不是并行日历计划，也不代表考试合格、法定资质或自动发证。

- 入口：真实账号登录后，右下角“业务计算工具”。默认沿用当前登录JWT；演示模式的demo-token不可执行。
- 接口：`POST /api/workbench/learning-path-prerequisites`，`Content-Type: application/json`，`Authorization: Bearer <登录令牌>`。
- 返回：200含`success:true,data`；身份缺失/失效401；输入错误400含可读message。独立计算只允许调用者提交数据，不读取数据库、其他用户数据、文件或URL。
- 结果在内存中保留，刷新或退出丢失；可导出完整JSON。列表仅预览100行，导出不会截断。
- 请求限制：数组最多500项（各业务数组有更小上限），嵌套12层，10000节点，文本总量128000字符，单文本64000字符。通用数值最多30位有效数字、尺度绝对值≤12；金额最多2位小数。日期1900–2200年；本地时间精确到整分钟。
- 页面限制请求128 KiB、超时15秒。服务端字段限制在JSON解码后执行；生产反向代理仍须配置请求体上限、速率限制和部署级访问控制，不将这些校验当作网络层防护。

## 请求示例（合成数据）

```json
{
  "completed": [
    "BASIC"
  ],
  "courses": [
    {
      "id": "BASIC",
      "minutes": 30,
      "prerequisites": []
    },
    {
      "id": "ADV",
      "minutes": 60,
      "prerequisites": [
        "BASIC"
      ]
    },
    {
      "id": "CERT",
      "minutes": 20,
      "prerequisites": [
        "ADV"
      ]
    }
  ]
}
```

## 验证

后端Java21：`cd backend && mvn test`；前端：`cd frontend && npm ci && npm run build`。

新增测试覆盖算法样例、特定边界、异常输入、零分母或缺失证据、重复执行不修改输入、过深/过长请求、过高数字精度；MockMvc覆盖匿名401、无效JWT401、有效JWT200和参数错误400。用例位于`backend/src/test/java/cn/zhuatech/lms/workbench/`。实际结果以本批交付测试报告为准，不以文档替代测试执行。

## 能力边界 / Scope

This is an authenticated, deterministic calculation over caller-supplied data. Validates course prerequisite graphs and completed-course evidence, identifying ready and locked courses and remaining study time without automatically issuing qualifications. It does not run external inference, retrieve protected records, persist workflows or prove production readiness of the whole application. Results require business review; do not use them as automatic approval, regulatory certification or payment authorization.

原有业务模块、数据模式与许可不变；没有引入数据库迁移。该功能是可使用的辅助工具，不代表整个项目所有历史演示页面已改造成生产业务系统。
