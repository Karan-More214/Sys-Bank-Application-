import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/auth";
import "./Login.css";

export default function Register() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);
    try {
      await register(fullName, email, username, password);
      navigate("/login");
    } catch (err) {
      const message = err.response?.data?.message || "Unable to register. Please try again.";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-container animate__animated animate__fadeInRight">
        <div className="login-header">
          <svg xmlns="http://www.w3.org/2000/svg" width="50" height="50" viewBox="0 0 24 24" fill="none" stroke="#00b3ff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mb-3">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
          </svg>

          <h3>CREATE ACCOUNT</h3>

          {error && (
            <div
              className="text-danger text-center mx-auto"
              style={{ background: "#f8d7da", color: "#721c24", padding: "6px 12px", borderRadius: 6, fontSize: 14, width: "fit-content", marginTop: 10 }}
            >
              {error}
            </div>
          )}

          <p>Join Sys Bank today</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label" htmlFor="fullName">Full Name</label>
            <div className="input-group">
              <span className="input-group-text"><i className="fas fa-user"></i></span>
              <input
                type="text"
                className="form-control"
                id="fullName"
                placeholder="Enter your full name"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="email">Email</label>
            <div className="input-group">
              <span className="input-group-text"><i className="fas fa-envelope"></i></span>
              <input
                type="email"
                className="form-control"
                id="email"
                placeholder="Enter your email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label" htmlFor="username">Username</label>
            <div className="input-group">
              <span className="input-group-text"><i className="fas fa-id-badge"></i></span>
              <input
                type="text"
                className="form-control"
                id="username"
                placeholder="Choose a username"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Password</label>
            <div className="password-wrapper">
              <div className="input-group">
                <span className="input-group-text"><i className="fas fa-lock"></i></span>
                <input
                  type={showPassword ? "text" : "password"}
                  id="password"
                  className="form-control"
                  placeholder="Create a password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
              <i
                className={`fas ${showPassword ? "fa-eye-slash" : "fa-eye"} password-toggle`}
                title={showPassword ? "Hide Password" : "Show/Hide Password"}
                onClick={() => setShowPassword((v) => !v)}
              ></i>
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Confirm Password</label>
            <div className="password-wrapper">
              <div className="input-group">
                <span className="input-group-text"><i className="fas fa-lock"></i></span>
                <input
                  type={showConfirmPassword ? "text" : "password"}
                  id="confirmPassword"
                  className="form-control"
                  placeholder="Confirm your password"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
              </div>
              <i
                className={`fas ${showConfirmPassword ? "fa-eye-slash" : "fa-eye"} password-toggle`}
                title={showConfirmPassword ? "Hide Password" : "Show/Hide Password"}
                onClick={() => setShowConfirmPassword((v) => !v)}
              ></i>
            </div>
          </div>

          <div className="d-grid">
            <button type="submit" className="btn btn-login" disabled={loading}>
              {loading ? (
                <span><span className="spinner-border spinner-border-sm me-2"></span>Processing...</span>
              ) : (
                "Register"
              )}
            </button>
          </div>

          <div className="social-login">
            <div className="social-title">Or sign up with</div>
            <div className="social-buttons">
              <div className="social-btn google"><i className="fab fa-google"></i></div>
              <div className="social-btn facebook"><i className="fab fa-facebook-f"></i></div>
              <div className="social-btn twitter"><i className="fab fa-twitter"></i></div>
            </div>
          </div>

          <div className="login-footer">
            <p>Already have an account? <Link to="/login">Login Now</Link></p>
            <div className="mt-2">
              <a href="#" className="me-2"><i className="fas fa-shield-alt me-1"></i> Security</a>
              <a href="#"><i className="fas fa-question-circle me-1"></i> Help</a>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
