import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// url under protect
const protectedRoutes = ["/dashboarda"];

export function middleware(req: NextRequest) {
  const token = req.cookies.get("token")?.value;

  // if no token，redirect to login page
  if (protectedRoutes.some(path => req.nextUrl.pathname.startsWith(path)) && !token) {
    const loginUrl = new URL("/login", req.url);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}
