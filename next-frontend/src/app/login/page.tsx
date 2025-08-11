"use client";

import { useState } from "react";
import { apiLogin } from "../../lib/apiClient";
import Cookies from "js-cookie";

export default function LoginPage() {
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setError("");

    try {
      const data = await apiLogin(userName, password);
      localStorage.setItem("token", data.token);
      alert("Login success!");
      Cookies.set("token", data.token, { expires: 7 });
      window.location.href = "/dashboard";
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <div style={{ maxWidth: 400, margin: "50px auto", padding: 20, border: "1px solid #ccc" }}>
      <h2>Login</h2>
      <form onSubmit={handleLogin}>
        <div>
          <label>Username</label>
          <input
            value={userName}
            onChange={(e) => setUserName(e.target.value)}
            style={{ width: "100%", padding: "8px", margin: "5px 0" }}
          />
        </div>
        <div>
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ width: "100%", padding: "8px", margin: "5px 0" }}
          />
        </div>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <button type="submit" style={{ padding: "10px 15px", marginTop: 10 }}>Login</button>
      </form>
    </div>
  );
}
