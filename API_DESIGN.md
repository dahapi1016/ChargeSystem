# 充电系统 API 设计文档

## 1. 用户相关 API (`UserController`)

**路径前缀**: `/api/user`

- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录  
- `GET /api/user/info` - 获取用户信息

## 2. 充电相关 API (`ChargingController`)

**路径前缀**: `/api/charging`

### 充电请求管理
- `POST /api/charging/request` - 提交充电请求
- `PUT /api/charging/request/{requestId}` - 修改充电请求
- `DELETE /api/charging/request/{requestId}` - 取消充电请求
- `GET /api/charging/request/current` - 查看当前充电请求（包含排队号码）

### 排队和状态查询
- `GET /api/charging/queue/status` - 查看排队状态（前车等待数量等）
- `GET /api/charging/status/{requestId}` - 获取充电状态

### 充电控制
- `POST /api/charging/end/{requestId}` - 结束充电

## 3. 计费详单 API (`BillingController`)

**路径前缀**: `/api/billing`

- `GET /api/billing/list` - 获取用户充电详单列表
- `GET /api/billing/detail/{billId}` - 获取充电详单详情

## 4. 管理员 - 充电桩管理 API (`AdminPileController`)

**路径前缀**: `/api/admin/pile`
**权限要求**: `ADMIN`

- `PUT /api/admin/pile/{id}/status` - 启动/关闭充电桩
- `GET /api/admin/pile/{id}` - 获取单个充电桩状态
- `GET /api/admin/pile/status` - 获取所有充电桩状态
- `GET /api/admin/pile/{id}/queue` - 获取充电桩等候车辆信息

## 5. 管理员 - 报表 API (`ReportController`)

**路径前缀**: `/api/admin/report`
**权限要求**: `ADMIN`

- `GET /api/admin/report/daily` - 获取日报表
- `GET /api/admin/report/weekly` - 获取周报表  
- `GET /api/admin/report/monthly` - 获取月报表
- `GET /api/admin/report/pile/{pileId}` - 获取充电桩报表
- `POST /api/admin/report/generate/daily` - 手动生成日报表
- `POST /api/admin/report/generate/weekly` - 手动生成周报表
- `POST /api/admin/report/generate/monthly` - 手动生成月报表

## 6. 管理员 - 系统参数 API (`SystemParamController`)

**路径前缀**: `/api/admin/param`
**权限要求**: `ADMIN`

- `GET /api/admin/param/list` - 获取所有系统参数
- `GET /api/admin/param/{key}` - 获取单个系统参数
- `PUT /api/admin/param/{key}` - 更新单个系统参数
- `PUT /api/admin/param/batch` - 批量更新系统参数