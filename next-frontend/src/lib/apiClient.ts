export async function apiLogin(userName: string, password: string) {
  const res = await fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      username: userName,   //  AuthRequest.username()
      password              //  AuthRequest.password()
    })
  });

  if (!res.ok) {
    throw new Error("Login failed");
  }

  return res.json(); // { token: "xxx" }
}

