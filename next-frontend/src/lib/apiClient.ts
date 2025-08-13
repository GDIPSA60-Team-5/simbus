interface ApiOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  body?: any;
  headers?: Record<string, string>;
  requireAuth?: boolean;
}

// Helper function to get auth token
const getAuthToken = (): string | null => {
  if (typeof window === 'undefined') {
    return null;
  }
  return localStorage.getItem('authToken');
};

// Helper function to get default headers with auth
const getHeaders = (customHeaders: Record<string, string> = {}, requireAuth: boolean = true): Record<string, string> => {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...customHeaders,
  };

  if (requireAuth) {
    const token = getAuthToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }

  return headers;
};

// Generic API client
export async function apiRequest<T>(url: string, options: ApiOptions = {}): Promise<T> {
  const { method = 'GET', body, headers = {}, requireAuth = true } = options;

  const finalHeaders = getHeaders(headers, requireAuth);

  const config: RequestInit = {
    method,
    headers: finalHeaders,
  };

  if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
    config.body = JSON.stringify(body);
  }

  try {
    const response = await fetch(url, config);

    // Handle 401 Unauthorized - token might be expired
    if (response.status === 401 && requireAuth) {
      // Clear the token and redirect to login
      localStorage.removeItem('authToken');
      document.cookie = 'authToken=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
      
      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
      
      throw new Error('Authentication failed. Please login again.');
    }

    if (!response.ok) {
      let errorMessage = `HTTP Error: ${response.status}`;
      
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch {
        // If JSON parsing fails, use the status text
        errorMessage = response.statusText || errorMessage;
      }
      
      throw new Error(errorMessage);
    }

    // Handle empty responses
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return await response.json();
    }

    // Return empty object for non-JSON responses
    return {} as T;
  } catch (error) {
    if (error instanceof Error) {
      throw error;
    }
    throw new Error('Network error occurred');
  }
}

// Convenience methods
export async function apiGet<T>(url: string, headers?: Record<string, string>): Promise<T> {
  return apiRequest<T>(url, { method: 'GET', headers });
}

export async function apiPost<T>(url: string, body: any, headers?: Record<string, string>): Promise<T> {
  return apiRequest<T>(url, { method: 'POST', body, headers });
}

export async function apiPut<T>(url: string, body: any, headers?: Record<string, string>): Promise<T> {
  return apiRequest<T>(url, { method: 'PUT', body, headers });
}

export async function apiDelete<T>(url: string, headers?: Record<string, string>): Promise<T> {
  return apiRequest<T>(url, { method: 'DELETE', headers });
}

// Public API methods (no auth required)
export async function apiPostPublic<T>(url: string, body: any, headers?: Record<string, string>): Promise<T> {
  return apiRequest<T>(url, { method: 'POST', body, headers, requireAuth: false });
}

export async function apiGetPublic<T>(url: string, headers?: Record<string, string>): Promise<T> {
  return apiRequest<T>(url, { method: 'GET', headers, requireAuth: false });
}
