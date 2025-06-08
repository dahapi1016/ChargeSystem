---
title: 充电桩
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# 充电桩

Base URLs:

# Authentication

# 充电桩控制相关

## PUT 启动/关闭充电桩

PUT /api/admin/pile/{id}/status

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|
|status|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": false
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultBoolean](#schemaresultboolean)|

## GET 获取充电桩状态

GET /api/admin/pile/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "pileCode": "",
    "pileType": 0,
    "pileTypeDesc": "",
    "status": 0,
    "statusDesc": "",
    "power": 0,
    "totalChargingTimes": 0,
    "totalChargingDuration": 0,
    "totalChargingAmount": 0,
    "queueLength": 0,
    "createTime": "",
    "updateTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultChargingPileVO](#schemaresultchargingpilevo)|

## GET 获取充电桩队列

GET /api/admin/pile/{id}/queue

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "requestId": 0,
      "userId": 0,
      "username": "",
      "batteryCapacity": 0,
      "requestAmount": 0,
      "position": 0,
      "enterTime": "",
      "waitingTime": 0
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListPileQueueItemVO](#schemaresultlistpilequeueitemvo)|

## GET 获取所有充电桩状态

GET /api/admin/pile/status

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "pileCode": "",
      "pileType": 0,
      "pileTypeDesc": "",
      "status": 0,
      "statusDesc": "",
      "power": 0,
      "totalChargingTimes": 0,
      "totalChargingDuration": 0,
      "totalChargingAmount": 0,
      "queueLength": 0,
      "createTime": "",
      "updateTime": ""
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListChargingPileVO](#schemaresultlistchargingpilevo)|

# 充电详单相关

## GET 获取用户充电详单列表

GET /api/billing/list

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "billNumber": "",
      "requestId": 0,
      "pileId": 0,
      "pileCode": "",
      "chargingAmount": 0,
      "chargingDuration": 0,
      "startTime": "",
      "endTime": "",
      "peakAmount": 0,
      "flatAmount": 0,
      "valleyAmount": 0,
      "chargingFee": 0,
      "serviceFee": 0,
      "totalFee": 0,
      "createTime": ""
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListChargingBillVO](#schemaresultlistchargingbillvo)|

## GET 获取充电详单详情

GET /api/billing/detail/{billId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|billId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "billNumber": "",
    "requestId": 0,
    "pileId": 0,
    "pileCode": "",
    "chargingAmount": 0,
    "chargingDuration": 0,
    "startTime": "",
    "endTime": "",
    "peakAmount": 0,
    "flatAmount": 0,
    "valleyAmount": 0,
    "chargingFee": 0,
    "serviceFee": 0,
    "totalFee": 0,
    "createTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultChargingBillVO](#schemaresultchargingbillvo)|

# 充电请求相关

## POST 提交充电请求

POST /api/charging/request

> Body 请求参数

```json
{
  "chargingMode": 1,
  "requestAmount": 1,
  "batteryCapacity": 1
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[ChargeReqDTO](#schemachargereqdto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "queueNumber": "",
    "chargingMode": 0,
    "chargingModeDesc": "",
    "requestAmount": 0,
    "batteryCapacity": 0,
    "status": 0,
    "statusDesc": "",
    "pileId": 0,
    "pileCode": "",
    "queuePosition": 0,
    "queueStartTime": "",
    "chargingStartTime": "",
    "chargingEndTime": "",
    "waitingTime": 0,
    "waitingCount": 0
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultChargeRespDTO](#schemaresultchargerespdto)|

## PUT 修改充电请求

PUT /api/charging/request/{requestId}

> Body 请求参数

```json
{
  "chargingMode": 1,
  "requestAmount": 1,
  "batteryCapacity": 1
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|requestId|path|integer| 是 |none|
|body|body|[ChargeReqDTO](#schemachargereqdto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "queueNumber": "",
    "chargingMode": 0,
    "chargingModeDesc": "",
    "requestAmount": 0,
    "batteryCapacity": 0,
    "status": 0,
    "statusDesc": "",
    "pileId": 0,
    "pileCode": "",
    "queuePosition": 0,
    "queueStartTime": "",
    "chargingStartTime": "",
    "chargingEndTime": "",
    "waitingTime": 0,
    "waitingCount": 0
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultChargeRespDTO](#schemaresultchargerespdto)|

## DELETE 取消充电请求

DELETE /api/charging/request/{requestId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|requestId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": false
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultBoolean](#schemaresultboolean)|

## GET 查看当前充电请求（包含排队号码）

GET /api/charging/request/current

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "queueNumber": "",
    "chargingMode": 0,
    "chargingModeDesc": "",
    "requestAmount": 0,
    "batteryCapacity": 0,
    "status": 0,
    "statusDesc": "",
    "pileId": 0,
    "pileCode": "",
    "queuePosition": 0,
    "queueStartTime": "",
    "chargingStartTime": "",
    "chargingEndTime": "",
    "waitingTime": 0,
    "waitingCount": 0
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultChargeRespDTO](#schemaresultchargerespdto)|

## GET 查看排队状态（前车等待数量等）

GET /api/charging/queue/status

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "queueNumber": "",
    "waitingCount": 0,
    "totalWaitingCount": 0,
    "estimatedWaitingTime": 0
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultQueueStatusRespDTO](#schemaresultqueuestatusrespdto)|

## POST 结束充电

POST /api/charging/end/{requestId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|requestId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": false
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultBoolean](#schemaresultboolean)|

## GET 获取充电状态

GET /api/charging/status/{requestId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|requestId|path|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "requestId": 0,
    "pileId": 0,
    "pileCode": "",
    "chargingMode": 0,
    "chargingModeDesc": "",
    "requestAmount": 0,
    "currentAmount": 0,
    "chargingDuration": 0,
    "estimatedFee": 0,
    "startTime": "",
    "estimatedEndTime": "",
    "progress": 0
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultChargingStatusVO](#schemaresultchargingstatusvo)|

# 用户鉴权相关

## POST 用户登录

POST /api/user/login

> Body 请求参数

```json
{
  "username": "string",
  "password": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[LoginReqDTO](#schemaloginreqdto)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResponseEntity?](#schemaresponseentity?)|

## GET 获取用户信息

GET /api/user/info

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResponseEntity?](#schemaresponseentity?)|

## POST 用户注册

POST /api/user/register

> Body 请求参数

```json
{
  "username": "string",
  "password": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[RegisterReqDTO](#schemaregisterreqdto)| 否 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResponseEntity?](#schemaresponseentity?)|

# 报表生成相关

## GET 获取日报表

GET /api/admin/report/daily

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|date|query|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "reportType": 0,
      "reportTypeDesc": "",
      "reportDate": "",
      "weekOfYear": 0,
      "monthOfYear": 0,
      "pileId": 0,
      "pileCode": "",
      "chargingTimes": 0,
      "chargingDuration": 0,
      "chargingAmount": 0,
      "chargingFee": 0,
      "serviceFee": 0,
      "totalFee": 0
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListChargingReportVO](#schemaresultlistchargingreportvo)|

## GET 获取周报表

GET /api/admin/report/weekly

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|weekOfYear|query|integer| 是 |none|
|year|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "reportType": 0,
      "reportTypeDesc": "",
      "reportDate": "",
      "weekOfYear": 0,
      "monthOfYear": 0,
      "pileId": 0,
      "pileCode": "",
      "chargingTimes": 0,
      "chargingDuration": 0,
      "chargingAmount": 0,
      "chargingFee": 0,
      "serviceFee": 0,
      "totalFee": 0
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListChargingReportVO](#schemaresultlistchargingreportvo)|

## GET 获取充电桩报表

GET /api/admin/report/pile/{pileId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|pileId|path|integer| 是 |none|
|reportType|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "reportType": 0,
      "reportTypeDesc": "",
      "reportDate": "",
      "weekOfYear": 0,
      "monthOfYear": 0,
      "pileId": 0,
      "pileCode": "",
      "chargingTimes": 0,
      "chargingDuration": 0,
      "chargingAmount": 0,
      "chargingFee": 0,
      "serviceFee": 0,
      "totalFee": 0
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListChargingReportVO](#schemaresultlistchargingreportvo)|

## POST 手动生成月报表

POST /api/admin/report/generate/monthly

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|month|query|integer| 是 |none|
|year|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST 手动生成周报表

POST /api/admin/report/generate/weekly

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|weekOfYear|query|integer| 是 |none|
|year|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## POST 手动生成日报表

POST /api/admin/report/generate/daily

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|date|query|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 获取月报表

GET /api/admin/report/monthly

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|month|query|integer| 是 |none|
|year|query|integer| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "reportType": 0,
      "reportTypeDesc": "",
      "reportDate": "",
      "weekOfYear": 0,
      "monthOfYear": 0,
      "pileId": 0,
      "pileCode": "",
      "chargingTimes": 0,
      "chargingDuration": 0,
      "chargingAmount": 0,
      "chargingFee": 0,
      "serviceFee": 0,
      "totalFee": 0
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListChargingReportVO](#schemaresultlistchargingreportvo)|

# 系统参数控制相关

## GET 获取所有系统参数

GET /api/admin/param/list

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": 0,
      "paramKey": "",
      "paramValue": "",
      "description": "",
      "updateTime": ""
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListSystemParamVO](#schemaresultlistsystemparamvo)|

## PUT 更新单个系统参数

PUT /api/admin/param/{key}

> Body 请求参数

```json
{
  "paramValue": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|key|path|string| 是 |none|
|body|body|[SystemParamItemUpdateDTO](#schemasystemparamitemupdatedto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "paramKey": "",
    "paramValue": "",
    "description": "",
    "updateTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultSystemParamVO](#schemaresultsystemparamvo)|

## GET 获取系统参数

GET /api/admin/param/{key}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|key|path|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": 0,
    "paramKey": "",
    "paramValue": "",
    "description": "",
    "updateTime": ""
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultSystemParamVO](#schemaresultsystemparamvo)|

## PUT 批量更新系统参数

PUT /api/admin/param/batch

> Body 请求参数

```json
{
  "params": {
    "key": "string"
  }
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[SystemParamUpdateDTO](#schemasystemparamupdatedto)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "",
  "data": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultInteger](#schemaresultinteger)|

# 数据模型

<h2 id="tocS_ResultBoolean">ResultBoolean</h2>

<a id="schemaresultboolean"></a>
<a id="schema_ResultBoolean"></a>
<a id="tocSresultboolean"></a>
<a id="tocsresultboolean"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|boolean|false|none||none|

<h2 id="tocS_ChargingPileVO">ChargingPileVO</h2>

<a id="schemachargingpilevo"></a>
<a id="schema_ChargingPileVO"></a>
<a id="tocSchargingpilevo"></a>
<a id="tocschargingpilevo"></a>

```json
{
  "id": 0,
  "pileCode": "string",
  "pileType": 0,
  "pileTypeDesc": "string",
  "status": 0,
  "statusDesc": "string",
  "power": 0,
  "totalChargingTimes": 0,
  "totalChargingDuration": 0,
  "totalChargingAmount": 0,
  "queueLength": 0,
  "createTime": "string",
  "updateTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||none|
|pileCode|string|false|none||none|
|pileType|integer|false|none||none|
|pileTypeDesc|string|false|none||none|
|status|integer|false|none||none|
|statusDesc|string|false|none||none|
|power|number|false|none||none|
|totalChargingTimes|integer|false|none||none|
|totalChargingDuration|integer|false|none||none|
|totalChargingAmount|number|false|none||none|
|queueLength|integer|false|none||none|
|createTime|string|false|none||none|
|updateTime|string|false|none||none|

<h2 id="tocS_ResultChargingPileVO">ResultChargingPileVO</h2>

<a id="schemaresultchargingpilevo"></a>
<a id="schema_ResultChargingPileVO"></a>
<a id="tocSresultchargingpilevo"></a>
<a id="tocsresultchargingpilevo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "pileCode": "string",
    "pileType": 0,
    "pileTypeDesc": "string",
    "status": 0,
    "statusDesc": "string",
    "power": 0,
    "totalChargingTimes": 0,
    "totalChargingDuration": 0,
    "totalChargingAmount": 0,
    "queueLength": 0,
    "createTime": "string",
    "updateTime": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[ChargingPileVO](#schemachargingpilevo)|false|none||none|

<h2 id="tocS_PileQueueItemVO">PileQueueItemVO</h2>

<a id="schemapilequeueitemvo"></a>
<a id="schema_PileQueueItemVO"></a>
<a id="tocSpilequeueitemvo"></a>
<a id="tocspilequeueitemvo"></a>

```json
{
  "requestId": 0,
  "userId": 0,
  "username": "string",
  "batteryCapacity": 0,
  "requestAmount": 0,
  "position": 0,
  "enterTime": "string",
  "waitingTime": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|requestId|integer(int64)|false|none||none|
|userId|integer(int64)|false|none||none|
|username|string|false|none||none|
|batteryCapacity|number|false|none||none|
|requestAmount|number|false|none||none|
|position|integer|false|none||none|
|enterTime|string|false|none||none|
|waitingTime|integer(int64)|false|none||排队时长（分钟）|

<h2 id="tocS_ResultListPileQueueItemVO">ResultListPileQueueItemVO</h2>

<a id="schemaresultlistpilequeueitemvo"></a>
<a id="schema_ResultListPileQueueItemVO"></a>
<a id="tocSresultlistpilequeueitemvo"></a>
<a id="tocsresultlistpilequeueitemvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "requestId": 0,
      "userId": 0,
      "username": "string",
      "batteryCapacity": 0,
      "requestAmount": 0,
      "position": 0,
      "enterTime": "string",
      "waitingTime": 0
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[[PileQueueItemVO](#schemapilequeueitemvo)]|false|none||none|

<h2 id="tocS_ResultListChargingPileVO">ResultListChargingPileVO</h2>

<a id="schemaresultlistchargingpilevo"></a>
<a id="schema_ResultListChargingPileVO"></a>
<a id="tocSresultlistchargingpilevo"></a>
<a id="tocsresultlistchargingpilevo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "id": 0,
      "pileCode": "string",
      "pileType": 0,
      "pileTypeDesc": "string",
      "status": 0,
      "statusDesc": "string",
      "power": 0,
      "totalChargingTimes": 0,
      "totalChargingDuration": 0,
      "totalChargingAmount": 0,
      "queueLength": 0,
      "createTime": "string",
      "updateTime": "string"
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[[ChargingPileVO](#schemachargingpilevo)]|false|none||none|

<h2 id="tocS_ResponseEntity?">ResponseEntity?</h2>

<a id="schemaresponseentity?"></a>
<a id="schema_ResponseEntity?"></a>
<a id="tocSresponseentity?"></a>
<a id="tocsresponseentity?"></a>

```json
{}

```

### 属性

*None*

<h2 id="tocS_RegisterReqDTO">RegisterReqDTO</h2>

<a id="schemaregisterreqdto"></a>
<a id="schema_RegisterReqDTO"></a>
<a id="tocSregisterreqdto"></a>
<a id="tocsregisterreqdto"></a>

```json
{
  "username": "string",
  "password": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|true|none||none|
|password|string|true|none||none|

<h2 id="tocS_LoginReqDTO">LoginReqDTO</h2>

<a id="schemaloginreqdto"></a>
<a id="schema_LoginReqDTO"></a>
<a id="tocSloginreqdto"></a>
<a id="tocsloginreqdto"></a>

```json
{
  "username": "string",
  "password": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|username|string|true|none||none|
|password|string|true|none||none|

<h2 id="tocS_ChargingReportVO">ChargingReportVO</h2>

<a id="schemachargingreportvo"></a>
<a id="schema_ChargingReportVO"></a>
<a id="tocSchargingreportvo"></a>
<a id="tocschargingreportvo"></a>

```json
{
  "id": 0,
  "reportType": 0,
  "reportTypeDesc": "string",
  "reportDate": "string",
  "weekOfYear": 0,
  "monthOfYear": 0,
  "pileId": 0,
  "pileCode": "string",
  "chargingTimes": 0,
  "chargingDuration": 0,
  "chargingAmount": 0,
  "chargingFee": 0,
  "serviceFee": 0,
  "totalFee": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||none|
|reportType|integer|false|none||none|
|reportTypeDesc|string|false|none||none|
|reportDate|string|false|none||none|
|weekOfYear|integer|false|none||none|
|monthOfYear|integer|false|none||none|
|pileId|integer(int64)|false|none||none|
|pileCode|string|false|none||none|
|chargingTimes|integer|false|none||none|
|chargingDuration|integer|false|none||none|
|chargingAmount|number|false|none||none|
|chargingFee|number|false|none||none|
|serviceFee|number|false|none||none|
|totalFee|number|false|none||none|

<h2 id="tocS_ResultListChargingReportVO">ResultListChargingReportVO</h2>

<a id="schemaresultlistchargingreportvo"></a>
<a id="schema_ResultListChargingReportVO"></a>
<a id="tocSresultlistchargingreportvo"></a>
<a id="tocsresultlistchargingreportvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "id": 0,
      "reportType": 0,
      "reportTypeDesc": "string",
      "reportDate": "string",
      "weekOfYear": 0,
      "monthOfYear": 0,
      "pileId": 0,
      "pileCode": "string",
      "chargingTimes": 0,
      "chargingDuration": 0,
      "chargingAmount": 0,
      "chargingFee": 0,
      "serviceFee": 0,
      "totalFee": 0
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[[ChargingReportVO](#schemachargingreportvo)]|false|none||none|

<h2 id="tocS_ResultVoid">ResultVoid</h2>

<a id="schemaresultvoid"></a>
<a id="schema_ResultVoid"></a>
<a id="tocSresultvoid"></a>
<a id="tocsresultvoid"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": null
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|null|false|none||none|

<h2 id="tocS_ChargingBillVO">ChargingBillVO</h2>

<a id="schemachargingbillvo"></a>
<a id="schema_ChargingBillVO"></a>
<a id="tocSchargingbillvo"></a>
<a id="tocschargingbillvo"></a>

```json
{
  "id": 0,
  "billNumber": "string",
  "requestId": 0,
  "pileId": 0,
  "pileCode": "string",
  "chargingAmount": 0,
  "chargingDuration": 0,
  "startTime": "string",
  "endTime": "string",
  "peakAmount": 0,
  "flatAmount": 0,
  "valleyAmount": 0,
  "chargingFee": 0,
  "serviceFee": 0,
  "totalFee": 0,
  "createTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||none|
|billNumber|string|false|none||none|
|requestId|integer(int64)|false|none||none|
|pileId|integer(int64)|false|none||none|
|pileCode|string|false|none||none|
|chargingAmount|number|false|none||none|
|chargingDuration|integer|false|none||none|
|startTime|string|false|none||none|
|endTime|string|false|none||none|
|peakAmount|number|false|none||none|
|flatAmount|number|false|none||none|
|valleyAmount|number|false|none||none|
|chargingFee|number|false|none||none|
|serviceFee|number|false|none||none|
|totalFee|number|false|none||none|
|createTime|string|false|none||none|

<h2 id="tocS_ResultListChargingBillVO">ResultListChargingBillVO</h2>

<a id="schemaresultlistchargingbillvo"></a>
<a id="schema_ResultListChargingBillVO"></a>
<a id="tocSresultlistchargingbillvo"></a>
<a id="tocsresultlistchargingbillvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "id": 0,
      "billNumber": "string",
      "requestId": 0,
      "pileId": 0,
      "pileCode": "string",
      "chargingAmount": 0,
      "chargingDuration": 0,
      "startTime": "string",
      "endTime": "string",
      "peakAmount": 0,
      "flatAmount": 0,
      "valleyAmount": 0,
      "chargingFee": 0,
      "serviceFee": 0,
      "totalFee": 0,
      "createTime": "string"
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[[ChargingBillVO](#schemachargingbillvo)]|false|none||none|

<h2 id="tocS_ResultChargingBillVO">ResultChargingBillVO</h2>

<a id="schemaresultchargingbillvo"></a>
<a id="schema_ResultChargingBillVO"></a>
<a id="tocSresultchargingbillvo"></a>
<a id="tocsresultchargingbillvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "billNumber": "string",
    "requestId": 0,
    "pileId": 0,
    "pileCode": "string",
    "chargingAmount": 0,
    "chargingDuration": 0,
    "startTime": "string",
    "endTime": "string",
    "peakAmount": 0,
    "flatAmount": 0,
    "valleyAmount": 0,
    "chargingFee": 0,
    "serviceFee": 0,
    "totalFee": 0,
    "createTime": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[ChargingBillVO](#schemachargingbillvo)|false|none||com.hapi.chargingsystem.dto.resp.ChargingBillVO|

<h2 id="tocS_ChargeRespDTO">ChargeRespDTO</h2>

<a id="schemachargerespdto"></a>
<a id="schema_ChargeRespDTO"></a>
<a id="tocSchargerespdto"></a>
<a id="tocschargerespdto"></a>

```json
{
  "id": 0,
  "queueNumber": "string",
  "chargingMode": 0,
  "chargingModeDesc": "string",
  "requestAmount": 0,
  "batteryCapacity": 0,
  "status": 0,
  "statusDesc": "string",
  "pileId": 0,
  "pileCode": "string",
  "queuePosition": 0,
  "queueStartTime": "string",
  "chargingStartTime": "string",
  "chargingEndTime": "string",
  "waitingTime": 0,
  "waitingCount": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||none|
|queueNumber|string|false|none||none|
|chargingMode|integer|false|none||none|
|chargingModeDesc|string|false|none||none|
|requestAmount|number|false|none||none|
|batteryCapacity|number|false|none||none|
|status|integer|false|none||none|
|statusDesc|string|false|none||none|
|pileId|integer(int64)|false|none||none|
|pileCode|string|false|none||none|
|queuePosition|integer|false|none||none|
|queueStartTime|string|false|none||none|
|chargingStartTime|string|false|none||none|
|chargingEndTime|string|false|none||none|
|waitingTime|integer(int64)|false|none||已等待时间（分钟）|
|waitingCount|integer|false|none||前车等待数量|

<h2 id="tocS_ResultChargeRespDTO">ResultChargeRespDTO</h2>

<a id="schemaresultchargerespdto"></a>
<a id="schema_ResultChargeRespDTO"></a>
<a id="tocSresultchargerespdto"></a>
<a id="tocsresultchargerespdto"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "queueNumber": "string",
    "chargingMode": 0,
    "chargingModeDesc": "string",
    "requestAmount": 0,
    "batteryCapacity": 0,
    "status": 0,
    "statusDesc": "string",
    "pileId": 0,
    "pileCode": "string",
    "queuePosition": 0,
    "queueStartTime": "string",
    "chargingStartTime": "string",
    "chargingEndTime": "string",
    "waitingTime": 0,
    "waitingCount": 0
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[ChargeRespDTO](#schemachargerespdto)|false|none||none|

<h2 id="tocS_ChargeReqDTO">ChargeReqDTO</h2>

<a id="schemachargereqdto"></a>
<a id="schema_ChargeReqDTO"></a>
<a id="tocSchargereqdto"></a>
<a id="tocschargereqdto"></a>

```json
{
  "chargingMode": 1,
  "requestAmount": 1,
  "batteryCapacity": 1
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|chargingMode|integer|true|none||none|
|requestAmount|number|true|none||none|
|batteryCapacity|number|false|none||@NotNull(message = "电池总容量不能为空")|

<h2 id="tocS_QueueStatusRespDTO">QueueStatusRespDTO</h2>

<a id="schemaqueuestatusrespdto"></a>
<a id="schema_QueueStatusRespDTO"></a>
<a id="tocSqueuestatusrespdto"></a>
<a id="tocsqueuestatusrespdto"></a>

```json
{
  "queueNumber": "string",
  "waitingCount": 0,
  "totalWaitingCount": 0,
  "estimatedWaitingTime": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|queueNumber|string|false|none||none|
|waitingCount|integer|false|none||前车等待数量|
|totalWaitingCount|integer|false|none||同类型总等待数量|
|estimatedWaitingTime|integer|false|none||预计等待时间（分钟）|

<h2 id="tocS_ResultQueueStatusRespDTO">ResultQueueStatusRespDTO</h2>

<a id="schemaresultqueuestatusrespdto"></a>
<a id="schema_ResultQueueStatusRespDTO"></a>
<a id="tocSresultqueuestatusrespdto"></a>
<a id="tocsresultqueuestatusrespdto"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "queueNumber": "string",
    "waitingCount": 0,
    "totalWaitingCount": 0,
    "estimatedWaitingTime": 0
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[QueueStatusRespDTO](#schemaqueuestatusrespdto)|false|none||none|

<h2 id="tocS_ChargingStatusVO">ChargingStatusVO</h2>

<a id="schemachargingstatusvo"></a>
<a id="schema_ChargingStatusVO"></a>
<a id="tocSchargingstatusvo"></a>
<a id="tocschargingstatusvo"></a>

```json
{
  "requestId": 0,
  "pileId": 0,
  "pileCode": "string",
  "chargingMode": 0,
  "chargingModeDesc": "string",
  "requestAmount": 0,
  "currentAmount": 0,
  "chargingDuration": 0,
  "estimatedFee": 0,
  "startTime": "string",
  "estimatedEndTime": "string",
  "progress": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|requestId|integer(int64)|false|none||none|
|pileId|integer(int64)|false|none||none|
|pileCode|string|false|none||none|
|chargingMode|integer|false|none||none|
|chargingModeDesc|string|false|none||none|
|requestAmount|number|false|none||none|
|currentAmount|number|false|none||当前已充电量|
|chargingDuration|integer|false|none||充电时长（分钟）|
|estimatedFee|number|false|none||预计费用|
|startTime|string|false|none||none|
|estimatedEndTime|string|false|none||预计结束时间|
|progress|integer|false|none||充电进度（百分比）|

<h2 id="tocS_ResultChargingStatusVO">ResultChargingStatusVO</h2>

<a id="schemaresultchargingstatusvo"></a>
<a id="schema_ResultChargingStatusVO"></a>
<a id="tocSresultchargingstatusvo"></a>
<a id="tocsresultchargingstatusvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "requestId": 0,
    "pileId": 0,
    "pileCode": "string",
    "chargingMode": 0,
    "chargingModeDesc": "string",
    "requestAmount": 0,
    "currentAmount": 0,
    "chargingDuration": 0,
    "estimatedFee": 0,
    "startTime": "string",
    "estimatedEndTime": "string",
    "progress": 0
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[ChargingStatusVO](#schemachargingstatusvo)|false|none||none|

<h2 id="tocS_SystemParamVO">SystemParamVO</h2>

<a id="schemasystemparamvo"></a>
<a id="schema_SystemParamVO"></a>
<a id="tocSsystemparamvo"></a>
<a id="tocssystemparamvo"></a>

```json
{
  "id": 0,
  "paramKey": "string",
  "paramValue": "string",
  "description": "string",
  "updateTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||none|
|paramKey|string|false|none||none|
|paramValue|string|false|none||none|
|description|string|false|none||none|
|updateTime|string|false|none||none|

<h2 id="tocS_ResultListSystemParamVO">ResultListSystemParamVO</h2>

<a id="schemaresultlistsystemparamvo"></a>
<a id="schema_ResultListSystemParamVO"></a>
<a id="tocSresultlistsystemparamvo"></a>
<a id="tocsresultlistsystemparamvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "id": 0,
      "paramKey": "string",
      "paramValue": "string",
      "description": "string",
      "updateTime": "string"
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[[SystemParamVO](#schemasystemparamvo)]|false|none||none|

<h2 id="tocS_ResultSystemParamVO">ResultSystemParamVO</h2>

<a id="schemaresultsystemparamvo"></a>
<a id="schema_ResultSystemParamVO"></a>
<a id="tocSresultsystemparamvo"></a>
<a id="tocsresultsystemparamvo"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "id": 0,
    "paramKey": "string",
    "paramValue": "string",
    "description": "string",
    "updateTime": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|[SystemParamVO](#schemasystemparamvo)|false|none||com.hapi.chargingsystem.dto.resp.SystemParamVO|

<h2 id="tocS_SystemParamItemUpdateDTO">SystemParamItemUpdateDTO</h2>

<a id="schemasystemparamitemupdatedto"></a>
<a id="schema_SystemParamItemUpdateDTO"></a>
<a id="tocSsystemparamitemupdatedto"></a>
<a id="tocssystemparamitemupdatedto"></a>

```json
{
  "paramValue": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|paramValue|string|true|none||none|

<h2 id="tocS_ResultInteger">ResultInteger</h2>

<a id="schemaresultinteger"></a>
<a id="schema_ResultInteger"></a>
<a id="tocSresultinteger"></a>
<a id="tocsresultinteger"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|message|string|false|none||none|
|data|integer|false|none||none|

<h2 id="tocS_MapString">MapString</h2>

<a id="schemamapstring"></a>
<a id="schema_MapString"></a>
<a id="tocSmapstring"></a>
<a id="tocsmapstring"></a>

```json
{
  "key": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|key|string|false|none||none|

<h2 id="tocS_SystemParamUpdateDTO">SystemParamUpdateDTO</h2>

<a id="schemasystemparamupdatedto"></a>
<a id="schema_SystemParamUpdateDTO"></a>
<a id="tocSsystemparamupdatedto"></a>
<a id="tocssystemparamupdatedto"></a>

```json
{
  "params": {
    "key": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|params|[MapString](#schemamapstring)|true|none||none|

