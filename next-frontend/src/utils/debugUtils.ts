// Debugging utilities for development

export const testBackendConnection = async () => {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080/api';
  const testUrls = [
    `${backendUrl}/stats`,
    `${backendUrl}/user/feedbacks`,
    'http://localhost:8080/api/stats',
    'http://localhost:8080/stats'
  ];

  console.log('🔍 Testing backend connections...');
  
  for (const url of testUrls) {
    try {
      console.log(`📡 Testing: ${url}`);
      
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('authToken') || 'no-token'}`
        }
      });
      
      console.log(`📊 ${url}: ${response.status} ${response.statusText}`);
      
      if (response.status === 200) {
        try {
          const data = await response.json();
          console.log(`✅ ${url}: Data received`, data);
        } catch (e) {
          console.log(`⚠️ ${url}: Response not JSON`);
        }
      } else {
        const text = await response.text();
        console.log(`❌ ${url}: ${text}`);
      }
    } catch (error) {
      console.log(`💥 ${url}: Connection failed`, error);
    }
  }
};

export const debugAuthToken = () => {
  const token = localStorage.getItem('authToken');
  console.log('🔑 Auth Token Debug:');
  console.log('Token exists:', !!token);
  
  if (token) {
    try {
      const parts = token.split('.');
      console.log('Token parts count:', parts.length);
      
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1]));
        console.log('Token payload:', payload);
        console.log('Token expires:', payload.exp ? new Date(payload.exp * 1000) : 'No expiration');
        console.log('Token expired:', payload.exp ? Date.now() >= payload.exp * 1000 : 'Cannot determine');
      }
    } catch (e) {
      console.error('Error decoding token:', e);
    }
  }
};

// Add to window for manual testing
if (typeof window !== 'undefined') {
  (window as any).testBackend = testBackendConnection;
  (window as any).debugAuth = debugAuthToken;
}