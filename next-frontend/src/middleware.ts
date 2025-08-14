import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// Protected routes that require authentication
const protectedRoutes = ["/dashboard"];

// Public routes that should redirect to dashboard if authenticated
const publicRoutes = ["/login", "/register"];

export function middleware(req: NextRequest) {
  const token = req.cookies.get("authToken")?.value || req.cookies.get("token")?.value;
  const { pathname } = req.nextUrl;
  
  // Check if accessing protected route without token
  if (protectedRoutes.some(path => pathname.startsWith(path))) {
    if (!token) {
      const loginUrl = new URL("/login", req.url);
      loginUrl.searchParams.set("redirect", pathname);
      return NextResponse.redirect(loginUrl);
    }
  }
  
  // Check if accessing public routes with token (redirect to dashboard)
  if (publicRoutes.some(path => pathname.startsWith(path))) {
    if (token) {
      const dashboardUrl = new URL("/dashboard", req.url);
      return NextResponse.redirect(dashboardUrl);
    }
  }

  // If accessing root and authenticated, redirect to dashboard
  if (pathname === "/" && token) {
    const dashboardUrl = new URL("/dashboard", req.url);
    return NextResponse.redirect(dashboardUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};
