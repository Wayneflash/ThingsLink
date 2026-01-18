<template>
  <div class="mobile-prototypes-page">
    <div class="page-header">
      <h1 class="page-title">移动端原型展示</h1>
      <p class="page-subtitle">所有原型页面预览 - 使用模拟数据</p>
    </div>

    <!-- 原型选择器 -->
    <div class="prototype-selector">
      <div
        v-for="prototype in prototypes"
        :key="prototype.id"
        class="selector-item"
        :class="{ active: activePrototype === prototype.id }"
        @click="activePrototype = prototype.id"
      >
        <el-icon :size="18">
          <component :is="prototype.icon" />
        </el-icon>
        <span>{{ prototype.label }}</span>
      </div>
    </div>

    <!-- 移动端模拟容器 -->
    <div class="mobile-container">
      <div class="mobile-frame">
        <!-- 设备边框装饰 -->
        <div class="phone-notch"></div>

        <!-- 原型内容区域 -->
        <div class="prototype-content">
          <!-- 1. 登录页原型 -->
          <div v-show="activePrototype === 'login'" class="prototype-view login-view">
            <div class="mobile-header-placeholder"></div>
            <div class="login-content">
              <div class="logo-section">
                <div class="logo-icon">
                  <el-icon :size="56"><Connection /></el-icon>
                </div>
                <h1 class="logo-title">ThingsLink</h1>
                <p class="logo-subtitle">智慧物联网平台</p>
              </div>
              <div class="login-form">
                <div class="input-group">
                  <el-icon class="input-icon"><User /></el-icon>
                  <input type="text" placeholder="请输入用户名" value="admin" class="form-input" />
                </div>
                <div class="input-group">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <input type="password" placeholder="请输入密码" value="admin123456" class="form-input" />
                </div>
                <button class="login-button">登 录</button>
              </div>
            </div>
          </div>

          <!-- 2. 概览页原型 - 重新设计 -->
          <div v-show="activePrototype === 'overview'" class="prototype-view overview-view">
            <div class="mobile-header-bar">
              <h2 class="header-title">设备概览</h2>
              <el-icon class="header-icon refresh-btn"><Refresh /></el-icon>
            </div>
            <div class="mobile-content-area">
              <!-- 核心指标卡片 - 2x2布局 -->
              <div class="stats-grid-v2">
                <div class="stat-card-v2 stat-primary" @click="() => {}">
                  <div class="stat-icon-wrapper-v2">
                    <div class="stat-icon-v2" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                      <el-icon :size="32"><Monitor /></el-icon>
                    </div>
                    <div class="stat-trend-badge" v-if="true">
                      <span class="trend-up">↑ 5</span>
                    </div>
                  </div>
                  <div class="stat-content-v2">
                    <div class="stat-label-v2">设备总数</div>
                    <div class="stat-value-v2">128</div>
                    <div class="stat-detail-v2">
                      <span class="detail-item">
                        <span class="status-dot online"></span>
                        在线 96
                      </span>
                      <span class="detail-item">
                        <span class="status-dot offline"></span>
                        离线 32
                      </span>
                    </div>
                  </div>
                </div>

                <div class="stat-card-v2 stat-success" @click="() => {}">
                  <div class="stat-icon-wrapper-v2">
                    <div class="stat-icon-v2" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
                      <el-icon :size="32"><CircleCheck /></el-icon>
                    </div>
                    <div class="stat-trend-badge">
                      <span class="trend-up">75%</span>
                    </div>
                  </div>
                  <div class="stat-content-v2">
                    <div class="stat-label-v2">在线设备</div>
                    <div class="stat-value-v2">96</div>
                    <div class="stat-detail-v2">
                      <span class="detail-text">在线率 75%</span>
                    </div>
                  </div>
                </div>

                <div class="stat-card-v2 stat-danger" @click="() => {}">
                  <div class="stat-icon-wrapper-v2">
                    <div class="stat-icon-v2" style="background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);">
                      <el-icon :size="32"><BellFilled /></el-icon>
                    </div>
                  </div>
                  <div class="stat-content-v2">
                    <div class="stat-label-v2">今日报警</div>
                    <div class="stat-value-v2">12</div>
                    <div class="stat-detail-v2">
                      <span class="detail-item critical">严重 3</span>
                      <span class="detail-item warning">警告 5</span>
                    </div>
                  </div>
                </div>

                <div class="stat-card-v2 stat-info" @click="() => {}">
                  <div class="stat-icon-wrapper-v2">
                    <div class="stat-icon-v2" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                      <el-icon :size="32"><DataLine /></el-icon>
                    </div>
                    <div class="stat-trend-badge">
                      <span class="trend-up">↑ 12%</span>
                    </div>
                  </div>
                  <div class="stat-content-v2">
                    <div class="stat-label-v2">今日数据量</div>
                    <div class="stat-value-v2">8.5K</div>
                    <div class="stat-detail-v2">
                      <span class="detail-text">较昨日 +12%</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 设备状态分布图表卡片 -->
              <div class="chart-card-mobile">
                <div class="chart-header-mobile">
                  <div class="chart-title-mobile">
                    <el-icon :size="18" color="#667eea"><DataAnalysis /></el-icon>
                    <span>设备状态分布</span>
                  </div>
                </div>
                <div class="chart-content-mobile">
                  <div class="status-chart-mini">
                    <div class="status-item">
                      <div class="status-bar">
                        <div class="status-fill online" style="width: 75%;"></div>
                      </div>
                      <div class="status-info">
                        <span class="status-label">在线</span>
                        <span class="status-value">96 台 (75%)</span>
                      </div>
                    </div>
                    <div class="status-item">
                      <div class="status-bar">
                        <div class="status-fill offline" style="width: 25%;"></div>
                      </div>
                      <div class="status-info">
                        <span class="status-label">离线</span>
                        <span class="status-value">32 台 (25%)</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 最近活跃设备列表 -->
              <div class="section-card-v2">
                <div class="section-header-v2">
                  <div class="section-title-v2">
                    <el-icon :size="18" color="#667eea"><Clock /></el-icon>
                    <span>最近活跃设备</span>
                  </div>
                  <span class="section-more-v2">查看全部 →</span>
                </div>
                <div class="device-list-v2">
                  <div class="device-item-v2" @click="() => {}">
                    <div class="device-icon-wrapper-v2">
                      <div class="device-icon-bg-v2">
                        <el-icon :size="20"><Monitor /></el-icon>
                      </div>
                      <span class="status-dot online"></span>
                    </div>
                    <div class="device-content-v2">
                      <div class="device-header-v2">
                        <span class="device-name-v2">智能电表-001</span>
                        <el-tag type="success" size="small" effect="dark">在线</el-tag>
                      </div>
                      <div class="device-meta-v2">智能电表 · 最后上线：刚刚</div>
                    </div>
                  </div>
                  <div class="device-item-v2" @click="() => {}">
                    <div class="device-icon-wrapper-v2">
                      <div class="device-icon-bg-v2">
                        <el-icon :size="20"><Monitor /></el-icon>
                      </div>
                      <span class="status-dot online"></span>
                    </div>
                    <div class="device-content-v2">
                      <div class="device-header-v2">
                        <span class="device-name-v2">智能水表-002</span>
                        <el-tag type="success" size="small" effect="dark">在线</el-tag>
                      </div>
                      <div class="device-meta-v2">智能水表 · 最后上线：5分钟前</div>
                    </div>
                  </div>
                  <div class="device-item-v2" @click="() => {}">
                    <div class="device-icon-wrapper-v2">
                      <div class="device-icon-bg-v2">
                        <el-icon :size="20"><Monitor /></el-icon>
                      </div>
                      <span class="status-dot offline"></span>
                    </div>
                    <div class="device-content-v2">
                      <div class="device-header-v2">
                        <span class="device-name-v2">智能气表-003</span>
                        <el-tag type="warning" size="small" effect="dark">离线</el-tag>
                      </div>
                      <div class="device-meta-v2">智能气表 · 最后上线：1小时前</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="mobile-bottom-nav">
              <div class="nav-item active"><el-icon><HomeFilled /></el-icon><span>概览</span></div>
              <div class="nav-item"><el-icon><Monitor /></el-icon><span>设备</span></div>
              <div class="nav-item"><el-icon><Bell /></el-icon><span>报警</span></div>
              <div class="nav-item"><el-icon><User /></el-icon><span>我的</span></div>
            </div>
          </div>

          <!-- 3. 设备列表页原型 -->
          <div v-show="activePrototype === 'device-list'" class="prototype-view device-list-view">
            <div class="mobile-header-bar">
              <div class="search-wrapper">
                <el-input placeholder="搜索设备名称/编码" size="small" class="search-input">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-select placeholder="型号" size="small" class="model-filter" style="width: 90px;">
                  <el-option label="全部" value="" />
                  <el-option label="ELEC" value="ELEC" />
                  <el-option label="WATER" value="WATER" />
                  <el-option label="GAS" value="GAS" />
                </el-select>
                <el-select placeholder="状态" size="small" class="status-filter" style="width: 80px;">
                  <el-option label="全部" value="" />
                  <el-option label="在线" value="online" />
                  <el-option label="离线" value="offline" />
                </el-select>
              </div>
            </div>
            <div class="mobile-content-area">
              <div class="device-list">
                <div class="device-card">
                  <div class="device-status-indicator online"></div>
                  <div class="device-info">
                    <div class="device-name">智能电表-001</div>
                    <div class="device-meta">DEV001 · 智能电表 · ELEC</div>
                    <div class="device-time">最后上线：2026-01-13 10:30:45</div>
                  </div>
                  <el-tag type="success" size="small">在线</el-tag>
                </div>
                <div class="device-card">
                  <div class="device-status-indicator online"></div>
                  <div class="device-info">
                    <div class="device-name">智能水表-002</div>
                    <div class="device-meta">DEV002 · 智能水表 · WATER</div>
                    <div class="device-time">最后上线：2026-01-13 10:25:30</div>
                  </div>
                  <el-tag type="success" size="small">在线</el-tag>
                </div>
                <div class="device-card">
                  <div class="device-status-indicator offline"></div>
                  <div class="device-info">
                    <div class="device-name">智能气表-003</div>
                    <div class="device-meta">DEV003 · 智能气表 · GAS</div>
                    <div class="device-time">最后上线：2026-01-13 09:30:15</div>
                  </div>
                  <el-tag type="warning" size="small">离线</el-tag>
                </div>
                <div class="device-card">
                  <div class="device-status-indicator online"></div>
                  <div class="device-info">
                    <div class="device-name">智能电表-004</div>
                    <div class="device-meta">DEV004 · 智能电表 · ELEC</div>
                    <div class="device-time">最后上线：2026-01-13 10:32:20</div>
                  </div>
                  <el-tag type="success" size="small">在线</el-tag>
                </div>
              </div>
            </div>
            <div class="mobile-bottom-nav">
              <div class="nav-item"><el-icon><HomeFilled /></el-icon><span>概览</span></div>
              <div class="nav-item active"><el-icon><Monitor /></el-icon><span>设备</span></div>
              <div class="nav-item"><el-icon><Bell /></el-icon><span>报警</span></div>
              <div class="nav-item"><el-icon><User /></el-icon><span>我的</span></div>
            </div>
          </div>

          <!-- 4. 设备详情页原型 -->
          <div v-show="activePrototype === 'device-detail'" class="prototype-view device-detail-view">
            <div class="mobile-header-bar">
              <el-icon class="back-icon"><ArrowLeft /></el-icon>
              <h2 class="header-title">设备详情</h2>
            </div>
            <div class="mobile-content-area">
              <div class="device-info-card">
                <div class="device-name-row">
                  <h2 class="device-name">智能电表-001</h2>
                  <el-tag type="success" size="large">在线</el-tag>
                </div>
                <div class="device-code">DEV001</div>
                <div class="device-meta-grid">
                  <div class="meta-item">
                    <div class="meta-label">产品类型</div>
                    <div class="meta-value">智能电表</div>
                  </div>
                  <div class="meta-item">
                    <div class="meta-label">所属分组</div>
                    <div class="meta-value">生产车间A</div>
                  </div>
                  <div class="meta-item">
                    <div class="meta-label">最后上线</div>
                    <div class="meta-value">刚刚</div>
                  </div>
                </div>
              </div>

              <div class="tab-nav">
                <div class="tab-item active">实时数据</div>
                <div class="tab-item">历史数据</div>
                <div class="tab-item">命令控制</div>
              </div>

              <div class="data-grid">
                <div class="data-card">
                  <div class="data-label">电压</div>
                  <div class="data-value-row">
                    <span class="data-value">220.5</span>
                    <span class="data-unit">V</span>
                  </div>
                  <div class="data-time">
                    <el-icon :size="12"><Clock /></el-icon>
                    2026-01-13 10:30:45
                  </div>
                </div>
                <div class="data-card">
                  <div class="data-label">电流</div>
                  <div class="data-value-row">
                    <span class="data-value">15.8</span>
                    <span class="data-unit">A</span>
                  </div>
                  <div class="data-time">
                    <el-icon :size="12"><Clock /></el-icon>
                    2026-01-13 10:30:45
                  </div>
                </div>
                <div class="data-card">
                  <div class="data-label">功率</div>
                  <div class="data-value-row">
                    <span class="data-value">3.48</span>
                    <span class="data-unit">kW</span>
                  </div>
                  <div class="data-time">
                    <el-icon :size="12"><Clock /></el-icon>
                    2026-01-13 10:30:45
                  </div>
                </div>
                <div class="data-card">
                  <div class="data-label">累计电量</div>
                  <div class="data-value-row">
                    <span class="data-value">1250.8</span>
                    <span class="data-unit">kWh</span>
                  </div>
                  <div class="data-time">
                    <el-icon :size="12"><Clock /></el-icon>
                    2026-01-13 10:30:45
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 5. 报警页原型 - 重新设计 -->
          <div v-show="activePrototype === 'alarm'" class="prototype-view alarm-view">
            <div class="mobile-header-bar">
              <h2 class="header-title">报警管理</h2>
              <el-icon class="header-icon refresh-btn"><Refresh /></el-icon>
            </div>
            <div class="mobile-content-area">
              <!-- 筛选栏 -->
              <div class="filter-bar-mobile">
                <el-select placeholder="报警级别" size="small" clearable class="filter-select">
                  <el-option label="全部" value="" />
                  <el-option label="严重" value="critical" />
                  <el-option label="警告" value="warning" />
                  <el-option label="提示" value="info" />
                </el-select>
                <el-select placeholder="处理状态" size="small" clearable class="filter-select">
                  <el-option label="全部" value="" />
                  <el-option label="未处理" :value="0" />
                  <el-option label="已处理" :value="1" />
                </el-select>
              </div>

              <!-- 报警列表 -->
              <div class="alarm-list-v3">
                <div class="alarm-card-v3 alarm-critical" @click="() => {}">
                  <div class="alarm-indicator critical"></div>
                  <div class="alarm-icon-wrapper-v3">
                    <div class="alarm-icon-bg-v3 critical">
                      <el-icon :size="22"><WarningFilled /></el-icon>
                    </div>
                  </div>
                  <div class="alarm-content-v3">
                    <div class="alarm-header-v3">
                      <div class="alarm-device-v3">智能电表-001</div>
                      <el-tag type="danger" size="small" effect="dark">未处理</el-tag>
                    </div>
                    <div class="alarm-message-v3">电压异常，超过阈值 250V，请立即处理</div>
                    <div class="alarm-footer-v3">
                      <div class="alarm-time-v3">
                        <el-icon :size="12"><Clock /></el-icon>
                        2026-01-13 10:25:30
                      </div>
                      <div class="alarm-level-badge critical">严重</div>
                    </div>
                  </div>
                </div>

                <div class="alarm-card-v3 alarm-warning" @click="() => {}">
                  <div class="alarm-indicator warning"></div>
                  <div class="alarm-icon-wrapper-v3">
                    <div class="alarm-icon-bg-v3 warning">
                      <el-icon :size="22"><Warning /></el-icon>
                    </div>
                  </div>
                  <div class="alarm-content-v3">
                    <div class="alarm-header-v3">
                      <div class="alarm-device-v3">智能水表-002</div>
                      <el-tag type="warning" size="small" effect="dark">未处理</el-tag>
                    </div>
                    <div class="alarm-message-v3">流量异常，请检查管道是否泄漏</div>
                    <div class="alarm-footer-v3">
                      <div class="alarm-time-v3">
                        <el-icon :size="12"><Clock /></el-icon>
                        2026-01-13 10:15:20
                      </div>
                      <div class="alarm-level-badge warning">警告</div>
                    </div>
                  </div>
                </div>

                <div class="alarm-card-v3 alarm-info" @click="() => {}">
                  <div class="alarm-indicator info"></div>
                  <div class="alarm-icon-wrapper-v3">
                    <div class="alarm-icon-bg-v3 info">
                      <el-icon :size="22"><InfoFilled /></el-icon>
                    </div>
                  </div>
                  <div class="alarm-content-v3">
                    <div class="alarm-header-v3">
                      <div class="alarm-device-v3">智能气表-003</div>
                      <el-tag type="success" size="small" effect="dark">已处理</el-tag>
                    </div>
                    <div class="alarm-message-v3">设备离线提醒，已重新连接</div>
                    <div class="alarm-footer-v3">
                      <div class="alarm-time-v3">
                        <el-icon :size="12"><Clock /></el-icon>
                        2026-01-13 09:30:15
                      </div>
                      <div class="alarm-level-badge info">提示</div>
                    </div>
                  </div>
                </div>

                <div class="alarm-card-v3 alarm-critical" @click="() => {}">
                  <div class="alarm-indicator critical"></div>
                  <div class="alarm-icon-wrapper-v3">
                    <div class="alarm-icon-bg-v3 critical">
                      <el-icon :size="22"><WarningFilled /></el-icon>
                    </div>
                  </div>
                  <div class="alarm-content-v3">
                    <div class="alarm-header-v3">
                      <div class="alarm-device-v3">智能电表-005</div>
                      <el-tag type="danger" size="small" effect="dark">未处理</el-tag>
                    </div>
                    <div class="alarm-message-v3">温度过高，当前温度 85°C，超过安全阈值</div>
                    <div class="alarm-footer-v3">
                      <div class="alarm-time-v3">
                        <el-icon :size="12"><Clock /></el-icon>
                        2026-01-13 09:15:10
                      </div>
                      <div class="alarm-level-badge critical">严重</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="mobile-bottom-nav">
              <div class="nav-item"><el-icon><HomeFilled /></el-icon><span>概览</span></div>
              <div class="nav-item"><el-icon><Monitor /></el-icon><span>设备</span></div>
              <div class="nav-item active"><el-icon><Bell /></el-icon><span>报警</span></div>
              <div class="nav-item"><el-icon><User /></el-icon><span>我的</span></div>
            </div>
          </div>

          <!-- 6. 设备历史数据页原型 -->
          <div v-show="activePrototype === 'device-history'" class="prototype-view device-history-view">
            <div class="mobile-header-bar">
              <el-icon class="back-icon"><ArrowLeft /></el-icon>
              <h2 class="header-title">历史数据</h2>
            </div>
            <div class="mobile-content-area">
              <div class="filter-section-history">
                <el-select placeholder="选择属性" size="small" class="filter-select-history" style="flex: 1;">
                  <el-option label="全部属性" value="" />
                  <el-option label="电压" value="voltage" />
                  <el-option label="电流" value="current" />
                  <el-option label="功率" value="power" />
                </el-select>
                <el-date-picker
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  size="small"
                  style="flex: 2;"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
                <el-button type="primary" size="small" class="query-btn-history">查询</el-button>
              </div>

              <div class="history-chart-card">
                <div class="chart-title-bar">数据趋势</div>
                <div class="history-chart-placeholder">
                  <div class="chart-line-sample">
                    <div class="chart-line" style="background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); height: 3px; width: 100%; margin-bottom: 20px;"></div>
                    <div class="chart-line" style="background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%); height: 3px; width: 85%; margin-bottom: 20px;"></div>
                    <div class="chart-line" style="background: linear-gradient(90deg, #f56c6c 0%, #ff8a80 100%); height: 3px; width: 70%;"></div>
                  </div>
                  <div class="chart-legend">
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #667eea;"></span>
                      <span>电压 (V)</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #38ef7d;"></span>
                      <span>电流 (A)</span>
                    </div>
                    <div class="legend-item">
                      <span class="legend-dot" style="background: #ff8a80;"></span>
                      <span>功率 (kW)</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="history-table-card">
                <div class="table-title-bar">数据列表</div>
                <div class="history-table">
                  <div class="table-row table-header">
                    <div class="table-cell">时间</div>
                    <div class="table-cell">电压(V)</div>
                    <div class="table-cell">电流(A)</div>
                    <div class="table-cell">功率(kW)</div>
                  </div>
                  <div class="table-row">
                    <div class="table-cell">10:30</div>
                    <div class="table-cell">220.5</div>
                    <div class="table-cell">15.8</div>
                    <div class="table-cell">3.48</div>
                  </div>
                  <div class="table-row">
                    <div class="table-cell">10:25</div>
                    <div class="table-cell">219.8</div>
                    <div class="table-cell">15.5</div>
                    <div class="table-cell">3.41</div>
                  </div>
                  <div class="table-row">
                    <div class="table-cell">10:20</div>
                    <div class="table-cell">220.2</div>
                    <div class="table-cell">15.7</div>
                    <div class="table-cell">3.45</div>
                  </div>
                  <div class="table-row">
                    <div class="table-cell">10:15</div>
                    <div class="table-cell">220.0</div>
                    <div class="table-cell">15.6</div>
                    <div class="table-cell">3.43</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 7. 命令控制页原型 -->
          <div v-show="activePrototype === 'device-command'" class="prototype-view device-command-view">
            <div class="mobile-header-bar">
              <el-icon class="back-icon"><ArrowLeft /></el-icon>
              <h2 class="header-title">命令控制</h2>
            </div>
            <div class="mobile-content-area">
              <div class="device-info-bar">
                <div class="device-name-bar">智能电表-001</div>
                <el-tag type="success" size="small" style="margin-left: 8px;">在线</el-tag>
              </div>

              <div class="command-list-v2">
                <div class="command-card-v2" @click="() => {}">
                  <div class="command-icon-v2">
                    <el-icon :size="24" color="#667eea"><Setting /></el-icon>
                  </div>
                  <div class="command-content-v2">
                    <div class="command-name-v2">开启设备</div>
                    <div class="command-desc-v2">开启设备运行，启动所有功能模块</div>
                  </div>
                  <el-button type="primary" size="small">执行</el-button>
                </div>

                <div class="command-card-v2" @click="() => {}">
                  <div class="command-icon-v2">
                    <el-icon :size="24" color="#11998e"><Setting /></el-icon>
                  </div>
                  <div class="command-content-v2">
                    <div class="command-name-v2">关闭设备</div>
                    <div class="command-desc-v2">关闭设备运行，停止所有功能模块</div>
                  </div>
                  <el-button type="primary" size="small">执行</el-button>
                </div>

                <div class="command-card-v2" @click="() => {}">
                  <div class="command-icon-v2">
                    <el-icon :size="24" color="#f56c6c"><Refresh /></el-icon>
                  </div>
                  <div class="command-content-v2">
                    <div class="command-name-v2">重启设备</div>
                    <div class="command-desc-v2">重启设备系统，恢复默认设置</div>
                  </div>
                  <el-button type="primary" size="small">执行</el-button>
                </div>

                <div class="command-card-v2" @click="() => {}">
                  <div class="command-icon-v2">
                    <el-icon :size="24" color="#ff9500"><Setting /></el-icon>
                  </div>
                  <div class="command-content-v2">
                    <div class="command-name-v2">调整参数</div>
                    <div class="command-desc-v2">调整设备运行参数配置</div>
                  </div>
                  <el-button type="primary" size="small">执行</el-button>
                </div>
              </div>

              <div class="command-tip">
                <el-icon :size="16" color="#86868b"><InfoFilled /></el-icon>
                <span>设备在线时才能执行控制命令</span>
              </div>
            </div>
          </div>

          <!-- 8. 设备日志页原型 -->
          <div v-show="activePrototype === 'device-log'" class="prototype-view device-log-view">
            <div class="mobile-header-bar">
              <el-icon class="back-icon"><ArrowLeft /></el-icon>
              <h2 class="header-title">设备日志</h2>
            </div>
            <div class="mobile-content-area">
              <div class="filter-section-log">
                <el-select placeholder="日志类型" size="small" clearable class="filter-select-log" style="flex: 1;">
                  <el-option label="全部" value="" />
                  <el-option label="设备上线" value="online" />
                  <el-option label="设备离线" value="offline" />
                  <el-option label="命令下发" value="command" />
                </el-select>
                <el-date-picker
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始"
                  end-placeholder="结束"
                  size="small"
                  style="flex: 2;"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
                <el-button type="primary" size="small" class="query-btn-log">查询</el-button>
              </div>

              <div class="log-list-card">
                <div class="log-item">
                  <div class="log-icon-wrapper">
                    <div class="log-icon-bg success">
                      <el-icon :size="18"><CircleCheck /></el-icon>
                    </div>
                  </div>
                  <div class="log-content">
                    <div class="log-header">
                      <span class="log-type success">设备上线</span>
                      <span class="log-time">2026-01-13 10:30:45</span>
                    </div>
                    <div class="log-detail">设备智能电表-001成功上线，连接MQTT服务器</div>
                  </div>
                </div>

                <div class="log-item">
                  <div class="log-icon-wrapper">
                    <div class="log-icon-bg command">
                      <el-icon :size="18"><Setting /></el-icon>
                    </div>
                  </div>
                  <div class="log-content">
                    <div class="log-header">
                      <span class="log-type command">命令下发</span>
                      <span class="log-time">2026-01-13 10:25:30</span>
                    </div>
                    <div class="log-detail">下发命令：开启设备，命令值：1</div>
                  </div>
                </div>

                <div class="log-item">
                  <div class="log-icon-wrapper">
                    <div class="log-icon-bg danger">
                      <el-icon :size="18"><WarningFilled /></el-icon>
                    </div>
                  </div>
                  <div class="log-content">
                    <div class="log-header">
                      <span class="log-type danger">设备离线</span>
                      <span class="log-time">2026-01-13 09:30:15</span>
                    </div>
                    <div class="log-detail">设备智能电表-001离线，连接超时</div>
                  </div>
                </div>

                <div class="log-item">
                  <div class="log-icon-wrapper">
                    <div class="log-icon-bg success">
                      <el-icon :size="18"><CircleCheck /></el-icon>
                    </div>
                  </div>
                  <div class="log-content">
                    <div class="log-header">
                      <span class="log-type success">设备上线</span>
                      <span class="log-time">2026-01-13 09:15:10</span>
                    </div>
                    <div class="log-detail">设备智能电表-001成功上线，连接MQTT服务器</div>
                  </div>
                </div>

                <div class="log-item">
                  <div class="log-icon-wrapper">
                    <div class="log-icon-bg command">
                      <el-icon :size="18"><Setting /></el-icon>
                    </div>
                  </div>
                  <div class="log-content">
                    <div class="log-header">
                      <span class="log-type command">命令下发</span>
                      <span class="log-time">2026-01-13 08:45:20</span>
                    </div>
                    <div class="log-detail">下发命令：调整参数，命令值：100</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 9. 我的页原型 -->
          <div v-show="activePrototype === 'profile'" class="prototype-view profile-view">
            <div class="profile-card">
              <div class="user-avatar">
                <el-icon :size="48"><User /></el-icon>
              </div>
              <div class="user-info">
                <div class="user-name">张三</div>
                <div class="user-role">管理员</div>
              </div>
            </div>
            <div class="menu-section">
              <div class="menu-item">
                <el-icon :size="20"><User /></el-icon>
                <span>个人资料</span>
                <el-icon :size="16" class="arrow-icon"><ArrowRight /></el-icon>
              </div>
              <div class="menu-item">
                <el-icon :size="20"><Lock /></el-icon>
                <span>修改密码</span>
                <el-icon :size="16" class="arrow-icon"><ArrowRight /></el-icon>
              </div>
            </div>
            <div class="logout-section">
              <el-button type="danger" size="large" class="logout-button">退出登录</el-button>
            </div>
            <div class="mobile-bottom-nav">
              <div class="nav-item"><el-icon><HomeFilled /></el-icon><span>概览</span></div>
              <div class="nav-item"><el-icon><Monitor /></el-icon><span>设备</span></div>
              <div class="nav-item"><el-icon><Bell /></el-icon><span>报警</span></div>
              <div class="nav-item active"><el-icon><User /></el-icon><span>我的</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 说明文字 -->
    <div class="prototype-info">
      <div class="info-card">
        <h3>原型说明</h3>
        <ul>
          <li>所有数据均为模拟数据，用于展示页面效果</li>
          <li>页面尺寸：375px × 812px（iPhone X 标准尺寸）</li>
          <li>设计风格：Apple风格，遵循UI规范</li>
          <li>点击上方标签切换不同原型页面</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElIcon, ElInput, ElSelect, ElOption, ElTag, ElButton } from 'element-plus'
import {
  Connection,
  User,
  Lock,
  Monitor,
  CircleCheck,
  Bell,
  BellFilled,
  DataLine,
  WarningFilled,
  Warning,
  InfoFilled,
  Search,
  Refresh,
  HomeFilled,
  ArrowLeft,
  ArrowRight,
  Setting,
  Clock,
  DataAnalysis,
  Document
} from '@element-plus/icons-vue'

const activePrototype = ref('overview')

const prototypes = [
  { id: 'login', label: '登录页', icon: Lock },
  { id: 'overview', label: '概览页', icon: HomeFilled },
  { id: 'device-list', label: '设备列表', icon: Monitor },
  { id: 'device-detail', label: '设备详情', icon: DataLine },
  { id: 'device-history', label: '设备历史', icon: DataAnalysis },
  { id: 'device-command', label: '命令控制', icon: Setting },
  { id: 'device-log', label: '设备日志', icon: Document },
  { id: 'alarm', label: '报警页', icon: Bell },
  { id: 'profile', label: '我的', icon: User }
]
</script>

<style scoped>
.mobile-prototypes-page {
  min-height: 100vh;
  background: #f5f5f7;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 'Helvetica Neue', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.page-header {
  text-align: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #86868b;
  margin: 0;
}

/* 原型选择器 */
.prototype-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding: 4px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.selector-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  color: #86868b;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.selector-item:hover {
  background: #f9f9f9;
  color: #667eea;
}

.selector-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
}

/* 移动端容器 */
.mobile-container {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}

.mobile-frame {
  width: 375px;
  height: 812px;
  background: #1d1d1f;
  border-radius: 32px;
  padding: 8px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
}

.phone-notch {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 150px;
  height: 30px;
  background: #1d1d1f;
  border-radius: 0 0 16px 16px;
  z-index: 10;
}

.prototype-content {
  width: 100%;
  height: 100%;
  background: #f5f5f7;
  border-radius: 24px;
  overflow: hidden;
  position: relative;
}

.prototype-view {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 通用样式 */
.mobile-header-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
  flex: 1;
}

.back-icon {
  font-size: 24px;
  color: #1d1d1f;
  cursor: pointer;
  margin-right: 12px;
}

.mobile-content-area {
  padding: 16px;
  padding-bottom: 80px;
}

.mobile-bottom-nav {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  color: #86868b;
  font-size: 11px;
  flex: 1;
}

.nav-item.active {
  color: #667eea;
}

.nav-item span {
  font-size: 11px;
}

/* 登录页样式 */
.login-content {
  padding: 40px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: center;
}

.logo-section {
  text-align: center;
  margin-bottom: 40px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.logo-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 8px;
}

.logo-subtitle {
  font-size: 14px;
  color: #86868b;
  margin: 0;
}

.login-form {
  width: 100%;
  max-width: 400px;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #ffffff;
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.input-icon {
  color: #667eea;
}

.form-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #1d1d1f;
  background: transparent;
}

.login-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  margin-bottom: 16px;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}


/* 概览页样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-footer {
  font-size: 12px;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.online-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #34c759;
}

.trend-up {
  color: #34c759;
}

.section-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.section-more {
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
}

.alarm-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 10px;
  background: #f9f9f9;
  margin-bottom: 12px;
  border-left: 3px solid transparent;
}

.alarm-item.alarm-critical {
  border-left-color: #ff3b30;
}

.alarm-item.alarm-warning {
  border-left-color: #ff9500;
}

.alarm-item.alarm-info {
  border-left-color: #34c759;
}

.alarm-title {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.alarm-message {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 6px;
}

.alarm-time {
  font-size: 12px;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 4px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.action-label {
  font-size: 12px;
  color: #1d1d1f;
  text-align: center;
}

/* 设备列表页样式 */
.search-wrapper {
  display: flex;
  gap: 8px;
  width: 100%;
}

.search-input {
  flex: 1;
}

.device-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.device-status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.device-status-indicator.online {
  background: #34c759;
  box-shadow: 0 0 8px rgba(52, 199, 89, 0.4);
}

.device-status-indicator.offline {
  background: #ff3b30;
}

.device-info {
  flex: 1;
  min-width: 0;
}

.device-name {
  font-size: 16px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 6px;
}

.device-meta {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 6px;
}

.device-time {
  font-size: 12px;
  color: #86868b;
}

/* 设备详情页样式 */
.device-info-card {
  background: #ffffff;
  margin-bottom: 16px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.device-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.device-name {
  font-size: 20px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
}

.device-code {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 16px;
}

.device-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.meta-item {
}

.meta-label {
  font-size: 12px;
  color: #86868b;
  margin-bottom: 4px;
}

.meta-value {
  font-size: 14px;
  color: #1d1d1f;
  font-weight: 500;
}

.tab-nav {
  display: flex;
  background: #ffffff;
  margin-bottom: 16px;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px;
  font-size: 14px;
  color: #86868b;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 500;
}

.data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.data-card {
  background: #ffffff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.data-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
}

.data-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.data-value {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
}

.data-unit {
  font-size: 14px;
  color: #86868b;
}

.data-time {
  font-size: 11px;
  color: #86868b;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== 概览页重新设计样式 ========== */

/* 核心指标卡片 - 2x2布局 */
.stats-grid-v2 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-card-v2 {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.stat-card-v2:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon-wrapper-v2 {
  position: relative;
  margin-bottom: 12px;
}

.stat-icon-v2 {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-trend-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ffffff;
  border-radius: 12px;
  padding: 4px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  font-size: 11px;
  font-weight: 600;
}

.trend-up {
  color: #34c759;
}

.stat-content-v2 {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label-v2 {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
}

.stat-value-v2 {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  line-height: 1.2;
}

.stat-detail-v2 {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86868b;
}

.detail-item.critical {
  color: #ff3b30;
}

.detail-item.warning {
  color: #ff9500;
}

.detail-text {
  font-size: 12px;
  color: #86868b;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}

.status-dot.online {
  background: #34c759;
}

.status-dot.offline {
  background: #ff3b30;
}

/* 设备状态分布图表卡片 */
.chart-card-mobile {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.chart-header-mobile {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.chart-title-mobile {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.chart-content-mobile {
  padding: 8px 0;
}

.status-chart-mini {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-bar {
  height: 8px;
  background: #f5f5f7;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.status-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
}

.status-fill.online {
  background: linear-gradient(90deg, #11998e 0%, #38ef7d 100%);
}

.status-fill.offline {
  background: linear-gradient(90deg, #f56c6c 0%, #ff8a80 100%);
}

.status-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-label {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
}

.status-value {
  font-size: 13px;
  color: #1d1d1f;
  font-weight: 600;
}

/* 最近报警列表 - 重新设计 */
.section-card-v2 {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.section-header-v2 {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title-v2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.section-more-v2 {
  font-size: 13px;
  color: #667eea;
  cursor: pointer;
  font-weight: 500;
}

.alarm-list-v2 {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alarm-item-v2 {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: #f9f9f9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.alarm-item-v2:hover {
  background: #f0f0f0;
  transform: translateX(2px);
}

.alarm-item-v2.alarm-critical {
  border-left-color: #ff3b30;
}

.alarm-item-v2.alarm-warning {
  border-left-color: #ff9500;
}

.alarm-item-v2.alarm-info {
  border-left-color: #34c759;
}

.alarm-icon-wrapper-v2 {
  flex-shrink: 0;
}

.alarm-icon-bg {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.alarm-icon-bg.critical {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);
}

.alarm-icon-bg.warning {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.alarm-icon-bg.info {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.alarm-content-v2 {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.alarm-header-v2 {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.alarm-device-v2 {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.alarm-message-v2 {
  font-size: 13px;
  color: #6e6e73;
  line-height: 1.5;
}

.alarm-time-v2 {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #86868b;
}

.refresh-btn {
  cursor: pointer;
  color: #667eea;
  transition: transform 0.3s ease;
}

.refresh-btn:hover {
  transform: rotate(180deg);
}

/* ========== 报警页重新设计样式 ========== */

/* 统计卡片网格 - 5个指标 */
.alarm-stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.alarm-stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.alarm-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.alarm-stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.alarm-stat-card.stat-total .alarm-stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.alarm-stat-card.stat-pending .alarm-stat-icon {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.alarm-stat-card.stat-critical .alarm-stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);
}

.alarm-stat-card.stat-warning .alarm-stat-icon {
  background: linear-gradient(135deg, #ff9500 0%, #ffb74d 100%);
}

.alarm-stat-card.stat-info .alarm-stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.alarm-stat-content {
  text-align: center;
}

.alarm-stat-label {
  font-size: 11px;
  color: #86868b;
  margin-bottom: 4px;
  font-weight: 500;
}

.alarm-stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1d1d1f;
}

/* 筛选栏 */
.filter-bar-mobile {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.filter-select {
  flex: 1;
}

/* 报警列表 - 重新设计 */
.alarm-list-v3 {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alarm-card-v3 {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.alarm-card-v3:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.alarm-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.alarm-indicator.critical {
  background: #ff3b30;
}

.alarm-indicator.warning {
  background: #ff9500;
}

.alarm-indicator.info {
  background: #34c759;
}

.alarm-icon-wrapper-v3 {
  flex-shrink: 0;
}

.alarm-icon-bg-v3 {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.alarm-icon-bg-v3.critical {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);
}

.alarm-icon-bg-v3.warning {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
}

.alarm-icon-bg-v3.info {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.alarm-content-v3 {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alarm-header-v3 {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.alarm-device-v3 {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.alarm-message-v3 {
  font-size: 14px;
  color: #6e6e73;
  line-height: 1.5;
}

.alarm-footer-v3 {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.alarm-time-v3 {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86868b;
}

.alarm-level-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.alarm-level-badge.critical {
  background: #ff3b30;
}

.alarm-level-badge.warning {
  background: #ff9500;
}

.alarm-level-badge.info {
  background: #34c759;
}

/* ========== 最近活跃设备列表样式 ========== */
.device-list-v2 {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.device-item-v2 {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: #f9f9f9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.device-item-v2:hover {
  background: #f0f0f0;
  transform: translateX(2px);
}

.device-icon-wrapper-v2 {
  position: relative;
  flex-shrink: 0;
}

.device-icon-bg-v2 {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.device-item-v2 .status-dot {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  border: 2px solid white;
}

.device-content-v2 {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.device-header-v2 {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.device-name-v2 {
  font-size: 15px;
  font-weight: 600;
  color: #1d1d1f;
}

.device-meta-v2 {
  font-size: 13px;
  color: #6e6e73;
}

/* ========== 设备历史数据页样式 ========== */
.device-info-bar {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.device-name-bar {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.device-code-bar {
  font-size: 13px;
  color: #86868b;
}

.filter-section-history {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}

.filter-select-history {
  flex: 1;
}

.query-btn-history {
  flex-shrink: 0;
}

.history-chart-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.chart-title-bar {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 16px;
}

.history-chart-placeholder {
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px 0;
}

.chart-line-sample {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.chart-line {
  border-radius: 2px;
  transition: width 0.6s ease;
}

.chart-legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #86868b;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.history-table-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.table-title-bar {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 12px;
}

.history-table {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 8px;
  padding: 10px;
  border-radius: 8px;
}

.table-row.table-header {
  background: #f5f5f7;
  font-weight: 600;
  font-size: 13px;
  color: #86868b;
}

.table-row:not(.table-header) {
  background: #f9f9f9;
  transition: background 0.3s ease;
}

.table-row:not(.table-header):hover {
  background: #f0f0f0;
}

.table-cell {
  font-size: 13px;
  color: #1d1d1f;
  text-align: center;
}

.table-row.table-header .table-cell {
  color: #86868b;
}

/* ========== 命令控制页样式 ========== */
.command-list-v2 {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.command-card-v2 {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.command-card-v2:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.command-icon-v2 {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #f5f5f7;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.command-content-v2 {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.command-name-v2 {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.command-desc-v2 {
  font-size: 13px;
  color: #86868b;
}

.command-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px;
  background: #f5f5f7;
  border-radius: 12px;
  font-size: 12px;
  color: #86868b;
  text-align: center;
  justify-content: center;
}

/* ========== 设备日志页样式 ========== */
.filter-section-log {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}

.filter-select-log {
  flex: 1;
}

.query-btn-log {
  flex-shrink: 0;
}

.log-list-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-left: 4px solid transparent;
  transition: all 0.3s ease;
}

.log-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.log-item .log-icon-wrapper {
  flex-shrink: 0;
}

.log-icon-bg {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.log-icon-bg.success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.log-icon-bg.danger {
  background: linear-gradient(135deg, #f56c6c 0%, #ff8a80 100%);
}

.log-icon-bg.command {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.log-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.log-type {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.log-type.success {
  background: #34c759;
}

.log-type.danger {
  background: #ff3b30;
}

.log-type.command {
  background: #667eea;
}

.log-time {
  font-size: 12px;
  color: #86868b;
}

.log-detail {
  font-size: 14px;
  color: #6e6e73;
  line-height: 1.5;
}

/* 我的页样式 */
.profile-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px 30px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.user-role {
  font-size: 14px;
  opacity: 0.8;
}

.menu-section {
  margin: 16px;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: #f9f9f9;
}

.menu-item span {
  flex: 1;
  font-size: 15px;
  color: #1d1d1f;
}

.menu-item :deep(.el-icon) {
  color: #667eea;
}

.arrow-icon {
  margin-left: auto;
  color: #86868b;
}

.logout-section {
  padding: 0 16px;
  margin-top: 24px;
}

.logout-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
}

/* 说明卡片 */
.prototype-info {
  max-width: 800px;
  margin: 0 auto;
}

.info-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.info-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 16px;
}

.info-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-card li {
  font-size: 14px;
  color: #86868b;
  line-height: 1.8;
  padding-left: 20px;
  position: relative;
}

.info-card li:before {
  content: '•';
  position: absolute;
  left: 0;
  color: #667eea;
  font-weight: bold;
}

/* 响应式 */
@media (max-width: 768px) {
  .mobile-container {
    padding: 0;
  }

  .mobile-frame {
    width: 100%;
    max-width: 375px;
    height: 812px;
  }
}
</style>