import { useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";

function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e) => {
    e.preventDefault();

    try {
      setLoading(true);

      const response = await axios.post(
        "http://localhost:8080/api/auth/register",
        {
          name,
          email,
          password,
        }
      );

      console.log("Registration successful:", response.data);

      alert("Registration successful! Please login.");

      setName("");
      setEmail("");
      setPassword("");
    } catch (error) {
      console.error(error);
      alert("Registration failed!");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-page">

      {/* LEFT SIDE */}
      <div className="travel-section">
        <div className="travel-overlay">

          <div className="brand">
            <span>✈️</span>
            <span>TripNest</span>
          </div>

          <div className="travel-content">
            <p className="small-text">START YOUR ADVENTURE</p>

            <h1>
              Your next adventure
              <br />
              <span>starts here.</span>
            </h1>

            <p>
              Create your TripNest account and start planning
              unforgettable journeys with ease.
            </p>

            <div className="travel-features">
              <div>🌍 Discover places</div>
              <div>🗺️ Plan journeys</div>
              <div>✈️ Create memories</div>
            </div>
          </div>

        </div>
      </div>

      {/* RIGHT SIDE */}
      <div className="register-section">
        <div className="register-card">

          <div className="mobile-logo">
            ✈️ <span>TripNest</span>
          </div>

          <div className="register-header">
            <h2>Create your account ✨</h2>
            <p>Join TripNest and start your journey</p>
          </div>

          <form onSubmit={handleRegister}>

            {/* NAME */}
            <div className="input-group">
              <label>Full Name</label>

              <div className="input-wrapper">
                <span>👤</span>

                <input
                  type="text"
                  placeholder="Enter your name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* EMAIL */}
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

            {/* PASSWORD */}
            <div className="input-group">
              <label>Password</label>

              <div className="input-wrapper">
                <span>🔒</span>

                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="Minimum 6 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  minLength={6}
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
              className="register-btn"
              disabled={loading}
            >
              {loading ? "Creating account..." : "Create Account →"}
            </button>

          </form>

          <div className="divider">
            <span>or</span>
          </div>

          <p className="login-text">
            Already have an account?{" "}
            <Link to="/login">Login</Link>
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

        .register-page {
          min-height: 100vh;
          display: flex;
          font-family: Arial, Helvetica, sans-serif;
          background: #f5f8fc;
        }

        /* LEFT TRAVEL SECTION */

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
        }

        .brand span:first-child {
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
          max-width: 520px;

          font-size: 17px;
          line-height: 1.7;

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

        /* RIGHT SECTION */

        .register-section {
          width: 48%;
          min-height: 100vh;

          display: flex;
          justify-content: center;
          align-items: center;

          padding: 40px;
        }

        .register-card {
          width: 100%;
          max-width: 440px;
        }

        .mobile-logo {
          display: none;
        }

        .register-header {
          margin-bottom: 30px;
        }

        .register-header h2 {
          margin: 0 0 8px;

          font-size: 33px;
          color: #102a43;
        }

        .register-header p {
          margin: 0;

          color: #718096;
          font-size: 15px;
        }

        /* INPUTS */

        .input-group {
          margin-bottom: 20px;
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

          box-shadow:
            0 0 0 3px rgba(22, 138, 173, 0.12);
        }

        .input-wrapper input {
          flex: 1;

          height: 100%;

          border: none;
          outline: none;

          background: transparent;

          font-size: 15px;
          color: #243b53;
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

        /* BUTTON */

        .register-btn {
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

          box-shadow:
            0 8px 20px rgba(0, 119, 182, 0.25);

          transition: 0.25s;
        }

        .register-btn:hover {
          transform: translateY(-2px);

          box-shadow:
            0 12px 25px rgba(0, 119, 182, 0.32);
        }

        .register-btn:disabled {
          opacity: 0.7;
          cursor: not-allowed;
          transform: none;
        }

        /* DIVIDER */

        .divider {
          display: flex;
          align-items: center;

          gap: 12px;

          margin: 27px 0;

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

        /* LOGIN LINK */

        .login-text {
          text-align: center;

          color: #627d98;
          font-size: 14px;
        }

        .login-text a {
          color: #0077b6;

          font-weight: 700;

          text-decoration: none;
        }

        .login-text a:hover {
          text-decoration: underline;
        }

        .footer-text {
          text-align: center;

          margin-top: 40px;

          color: #9aa5b1;

          font-size: 12px;
        }

        /* MOBILE */

        @media (max-width: 850px) {

          .register-page {
            display: block;
          }

          .travel-section {
            display: none;
          }

          .register-section {
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

            margin-bottom: 40px;
          }

          .mobile-logo span {
            margin-left: 6px;
          }

          .register-header h2 {
            font-size: 28px;
          }
        }

        @media (max-width: 450px) {

          .register-section {
            padding: 20px;
          }

          .register-header h2 {
            font-size: 25px;
          }

          .register-card {
            max-width: 100%;
          }
        }
      `}</style>
    </div>
  );
}

export default Register;