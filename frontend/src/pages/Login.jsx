import { useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";

function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();

    try {
      setLoading(true);

      const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        {
          email,
          password,
        }
      );

      console.log("Login successful:", response.data);

      if (response.data.token) {
        localStorage.setItem("token", response.data.token);
      }

      alert("Login successful!");
    } catch (error) {
      console.error(error);
      alert("Login failed!");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      {/* Left Travel Section */}
      <div className="travel-section">
        <div className="travel-overlay">
          <div className="brand">
            <span className="brand-icon">✈️</span>
            <span>TripNest</span>
          </div>

          <div className="travel-content">
            <p className="small-text">YOUR JOURNEY STARTS HERE</p>

            <h1>
              Explore the world.
              <br />
              <span>One trip at a time.</span>
            </h1>

            <p>
              Plan your perfect journey, discover amazing destinations
              and manage all your trips in one place.
            </p>

            <div className="travel-features">
              <div>🌍 Discover destinations</div>
              <div>🗺️ Plan your trips</div>
              <div>✈️ Travel smarter</div>
            </div>
          </div>
        </div>
      </div>

      {/* Login Section */}
      <div className="login-section">
        <div className="login-card">
          <div className="mobile-logo">
            ✈️ <span>TripNest</span>
          </div>

          <div className="login-header">
            <h2>Welcome back 👋</h2>
            <p>Login to continue your journey</p>
          </div>

          <form onSubmit={handleLogin}>
            <div className="input-group">
              <label>Email Address</label>

              <div className="input-wrapper">
                <span>✉️</span>

                <input
                  type="email"
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="input-group">
              <label>Password</label>

              <div className="input-wrapper">
                <span>🔒</span>

                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />

                <button
                  type="button"
                  className="password-btn"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? "🙈" : "👁️"}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="login-btn"
              disabled={loading}
            >
              {loading ? "Logging in..." : "Login →"}
            </button>
          </form>

          <div className="divider">
            <span>or</span>
          </div>

          <p className="register-text">
            Don't have an account?{" "}
            <Link to="/register">Create an account</Link>
          </p>

          <p className="footer-text">
            © 2026 TripNest • Travel. Plan. Explore.
          </p>
        </div>
      </div>

      <style>{`
        * {
          box-sizing: border-box;
        }

        .login-page {
          min-height: 100vh;
          display: flex;
          font-family: Arial, Helvetica, sans-serif;
          background: #f5f8fc;
        }

        /* LEFT SIDE */

        .travel-section {
          width: 52%;
          min-height: 100vh;

          background:
            linear-gradient(
              135deg,
              rgba(5, 35, 65, 0.88),
              rgba(0, 119, 182, 0.72)
            ),
            url("https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1400&q=80");

          background-size: cover;
          background-position: center;

          color: white;
        }

        .travel-overlay {
          min-height: 100vh;
          padding: 45px 60px;
          display: flex;
          flex-direction: column;
        }

        .brand {
          display: flex;
          align-items: center;
          gap: 10px;
          font-size: 27px;
          font-weight: 800;
          letter-spacing: 0.5px;
        }

        .brand-icon {
          font-size: 30px;
        }

        .travel-content {
          max-width: 600px;
          margin-top: auto;
          margin-bottom: auto;
        }

        .small-text {
          font-size: 13px;
          font-weight: 700;
          letter-spacing: 3px;
          margin-bottom: 18px;
          opacity: 0.85;
        }

        .travel-content h1 {
          font-size: 52px;
          line-height: 1.08;
          margin: 0 0 25px;
        }

        .travel-content h1 span {
          color: #73d2ff;
        }

        .travel-content > p {
          font-size: 17px;
          line-height: 1.7;
          max-width: 520px;
          color: #e7f5ff;
        }

        .travel-features {
          display: flex;
          flex-wrap: wrap;
          gap: 12px;
          margin-top: 30px;
        }

        .travel-features div {
          padding: 11px 16px;
          border: 1px solid rgba(255,255,255,0.25);
          border-radius: 30px;
          background: rgba(255,255,255,0.1);
          backdrop-filter: blur(8px);
          font-size: 14px;
        }

        /* RIGHT SIDE */

        .login-section {
          width: 48%;
          min-height: 100vh;
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 40px;
        }

        .login-card {
          width: 100%;
          max-width: 440px;
        }

        .mobile-logo {
          display: none;
        }

        .login-header {
          margin-bottom: 32px;
        }

        .login-header h2 {
          margin: 0 0 8px;
          font-size: 34px;
          color: #102a43;
        }

        .login-header p {
          margin: 0;
          color: #718096;
          font-size: 15px;
        }

        .input-group {
          margin-bottom: 22px;
        }

        .input-group label {
          display: block;
          margin-bottom: 8px;
          font-size: 14px;
          font-weight: 600;
          color: #243b53;
        }

        .input-wrapper {
          height: 52px;
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 0 15px;

          background: white;
          border: 1px solid #d9e2ec;
          border-radius: 12px;

          transition: 0.2s;
        }

        .input-wrapper:focus-within {
          border-color: #168aad;
          box-shadow: 0 0 0 3px rgba(22, 138, 173, 0.12);
        }

        .input-wrapper input {
          flex: 1;
          height: 100%;
          border: none;
          outline: none;
          font-size: 15px;
          color: #243b53;
          background: transparent;
        }

        .input-wrapper input::placeholder {
          color: #9aa5b1;
        }

        .password-btn {
          border: none;
          background: transparent;
          cursor: pointer;
          font-size: 16px;
          padding: 5px;
        }

        .login-btn {
          width: 100%;
          height: 54px;
          border: none;
          border-radius: 12px;

          background: linear-gradient(
            135deg,
            #0077b6,
            #00a8cc
          );

          color: white;
          font-size: 16px;
          font-weight: 700;
          cursor: pointer;

          box-shadow: 0 8px 20px rgba(0, 119, 182, 0.25);
          transition: 0.25s;
        }

        .login-btn:hover {
          transform: translateY(-2px);
          box-shadow: 0 12px 25px rgba(0, 119, 182, 0.32);
        }

        .login-btn:disabled {
          opacity: 0.7;
          cursor: not-allowed;
          transform: none;
        }

        .divider {
          display: flex;
          align-items: center;
          gap: 12px;
          margin: 28px 0;
          color: #9aa5b1;
          font-size: 13px;
        }

        .divider::before,
        .divider::after {
          content: "";
          flex: 1;
          height: 1px;
          background: #e4e7eb;
        }

        .register-text {
          text-align: center;
          color: #627d98;
          font-size: 14px;
        }

        .register-text a {
          color: #0077b6;
          font-weight: 700;
          text-decoration: none;
        }

        .register-text a:hover {
          text-decoration: underline;
        }

        .footer-text {
          text-align: center;
          margin-top: 45px;
          color: #9aa5b1;
          font-size: 12px;
        }




        /* MOBILE */

        @media (max-width: 850px) {
          .login-page {
            display: block;
          }

          .travel-section {
            display: none;
          }

          .login-section {
            width: 100%;
            min-height: 100vh;
            padding: 25px;
          }

          .mobile-logo {
            display: block;
            text-align: center;
            font-size: 26px;
            font-weight: 800;
            color: #0077b6;
            margin-bottom: 45px;
          }

          .mobile-logo span {
            margin-left: 6px;
          }

          .login-header h2 {
            font-size: 29px;
          }
        }

        @media (max-width: 450px) {
          .login-section {
            padding: 20px;
          }

          .login-header h2 {
            font-size: 26px;
          }

          .login-card {
            max-width: 100%;
          }
        }
      `}</style>
    </div>
  );
}

export default Login;