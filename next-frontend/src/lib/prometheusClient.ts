export interface PrometheusQueryResult {
  metric: Record<string, string>;
  value?: [number, string];
  values?: [number, string][];
}

export interface PrometheusResponse {
  status: string;
  data: {
    resultType: 'matrix' | 'vector' | 'scalar' | 'string';
    result: PrometheusQueryResult[];
  };
}

export interface PrometheusRangeQuery {
  query: string;
  start: string;
  end: string;
  step: string;
}

export interface PrometheusInstantQuery {
  query: string;
  time?: string;
}

class PrometheusClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl.replace(/\/$/, ''); // Remove trailing slash
  }

  async instantQuery(params: PrometheusInstantQuery): Promise<PrometheusResponse> {
    const searchParams = new URLSearchParams({
      query: params.query,
      ...(params.time && { time: params.time }),
    });

    const response = await fetch(`${this.baseUrl}?${searchParams}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Prometheus query failed: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  }

  async rangeQuery(params: PrometheusRangeQuery): Promise<PrometheusResponse> {
    const searchParams = new URLSearchParams({
      query: params.query,
      start: params.start,
      end: params.end,
      step: params.step,
    });

    const url = `${this.baseUrl}_range?${searchParams}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`Prometheus range query failed: ${response.status} ${response.statusText}`);
    }

    return await response.json();
  }

  // Helper methods for common queries
  async getCurrentValue(query: string): Promise<number | null> {
    try {
      const result = await this.instantQuery({ query });
      if (result.data.result.length > 0 && result.data.result[0].value) {
        return parseFloat(result.data.result[0].value[1]);
      }
      return null;
    } catch (error) {
      console.error('Failed to get current value:', error);
      return null;
    }
  }

  async getTimeSeriesData(query: string, hours: number = 24): Promise<[number, number][]> {
    try {
      const end = new Date();
      const start = new Date(end.getTime() - hours * 60 * 60 * 1000);
      
      const result = await this.rangeQuery({
        query,
        start: Math.floor(start.getTime() / 1000).toString(),
        end: Math.floor(end.getTime() / 1000).toString(),
        step: `${Math.max(Math.floor(hours * 60 / 100), 1)}m`, // Adaptive step size
      });

      if (result.data.result.length > 0 && result.data.result[0].values) {
        return result.data.result[0].values.map(([timestamp, value]) => [
          timestamp * 1000, // Convert to milliseconds
          parseFloat(value)
        ]);
      }
      return [];
    } catch (error) {
      console.error('Failed to get time series data:', error);
      return [];
    }
  }
}

// Create singleton instance
export const prometheusClient = new PrometheusClient('http://175.41.171.167/prometheus/query');