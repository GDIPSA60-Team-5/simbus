// Helper functions for JWT token management

export interface DecodedToken {
  sub?: string;
  username?: string;
  exp?: number;
  iat?: number;
  [key: string]: any;
}

/**
 * Decode JWT token payload
 */
export const decodeJWT = (token: string): DecodedToken | null => {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      throw new Error('Invalid token format');
    }

    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('Error decoding JWT:', error);
    return null;
  }
};

/**
 * Check if token is expired
 */
export const isTokenExpired = (token: string): boolean => {
  const decoded = decodeJWT(token);
  if (!decoded || !decoded.exp) {
    return true;
  }
  
  // Add 30 second buffer to account for clock skew
  const now = Math.floor(Date.now() / 1000);
  return now >= (decoded.exp - 30);
};

/**
 * Get time until token expires (in milliseconds)
 */
export const getTokenExpirationTime = (token: string): number | null => {
  const decoded = decodeJWT(token);
  if (!decoded || !decoded.exp) {
    return null;
  }
  
  const now = Date.now();
  const expiry = decoded.exp * 1000;
  
  return Math.max(0, expiry - now);
};

/**
 * Check if token needs refresh (expires within 5 minutes)
 */
export const shouldRefreshToken = (token: string): boolean => {
  const timeUntilExpiry = getTokenExpirationTime(token);
  if (timeUntilExpiry === null) {
    return false;
  }
  
  // Refresh if token expires within 5 minutes (300,000 ms)
  return timeUntilExpiry < 300000;
};

/**
 * Extract username from token
 */
export const getUsernameFromToken = (token: string): string | null => {
  const decoded = decodeJWT(token);
  return decoded?.sub || decoded?.username || null;
};

/**
 * Validate token format
 */
export const isValidTokenFormat = (token: string): boolean => {
  if (!token || typeof token !== 'string') {
    return false;
  }
  
  // JWT should have 3 parts separated by dots
  const parts = token.split('.');
  return parts.length === 3;
};