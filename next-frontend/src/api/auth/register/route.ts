import { NextResponse } from "next/server";
const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
export async function POST(req: Request) {
  const body = await req.json();

  try {
    const backendRes = await fetch(`${backendUrl}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const data = await backendRes.json();
    return NextResponse.json(data, { status: backendRes.status });
  } catch {
    return NextResponse.json({ error: "failed to connect to backend" }, { status: 500 });
  }
}
