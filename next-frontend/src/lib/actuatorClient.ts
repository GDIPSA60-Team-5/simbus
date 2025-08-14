export interface ActuatorMetric {
  name: string;
  description?: string;
  baseUnit?: string;
  measurements: Array<{
    statistic: string;
    value: number;
  }>;
  availableTags?: Array<{
    tag: string;
    values: string[];
  }>;
}

export interface ActuatorMetricsResponse {
  names: string[];
}

class ActuatorClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl.replace(/\/$/, '');
  }

  async getAvailableMetrics(): Promise<string[]> {
    const response = await fetch(`${this.baseUrl}/actuator/metrics`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Actuator metrics failed: ${response.status} ${response.statusText}`);
    }

    const data: ActuatorMetricsResponse = await response.json();
    return data.names;
  }

  async getMetric(metricName: string, tags?: Record<string, string>): Promise<ActuatorMetric> {
    let url = `${this.baseUrl}/actuator/metrics/${metricName}`;
    
    if (tags) {
      const tagParams = Object.entries(tags)
        .map(([key, value]) => `tag=${key}:${value}`)
        .join('&');
      url += `?${tagParams}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Actuator metric query failed: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  }

  async getCurrentValue(metricName: string, statistic: string = 'VALUE', tags?: Record<string, string>): Promise<number | null> {
    try {
      console.log(`Fetching metric: ${metricName} with statistic: ${statistic}`);
      const metric = await this.getMetric(metricName, tags);
      console.log(`Metric response:`, metric);
      const measurement = metric.measurements.find(m => m.statistic === statistic);
      const value = measurement ? measurement.value : null;
      console.log(`Extracted value: ${value}`);
      return value;
    } catch (error) {
      console.error(`Failed to get current value for ${metricName}:`, error);
      return null;
    }
  }

  async getHealthStatus(): Promise<any> {
    const response = await fetch(`${this.baseUrl}/actuator/health`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Health check failed: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  }

  // Helper methods for common system metrics
  async getJvmMemoryUsed(area: 'heap' | 'nonheap' = 'heap'): Promise<number | null> {
    return this.getCurrentValue('jvm.memory.used', 'VALUE', { area });
  }

  async getSystemCpuUsage(): Promise<number | null> {
    return this.getCurrentValue('system.cpu.usage');
  }

  async getProcessCpuUsage(): Promise<number | null> {
    return this.getCurrentValue('process.cpu.usage');
  }

  async getHttpRequestsActive(): Promise<number | null> {
    return this.getCurrentValue('http.server.requests.active', 'ACTIVE_TASKS');
  }

  async getHttpRequestCount(status?: string, method?: string): Promise<number | null> {
    const tags: Record<string, string> = {};
    if (status) tags.status = status;
    if (method) tags.method = method;
    
    return this.getCurrentValue('http.server.requests', 'COUNT', tags);
  }

  async getDatabaseConnectionsActive(): Promise<number | null> {
    try {
      return this.getCurrentValue('mongodb.driver.pool.checkedout');
    } catch {
      return null;
    }
  }
}

// Create singleton instance
export const actuatorClient = new ActuatorClient(
  process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080'
);