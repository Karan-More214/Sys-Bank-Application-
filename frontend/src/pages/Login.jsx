import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { login } from "../api/auth";
import { useAuth } from "../context/AuthContext";
import "./Login.css";

const API_BASE = import.meta.env.VITE_API_BASE_URL;

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { setUser } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const user = await login(email, password);
      setUser(user);
      const isAdmin = user.role && user.role.toLowerCase() === "admin";
      navigate(isAdmin ? "/admin" : "/dashboard");
    } catch (err) {
      const message = err.response?.data?.message || "Unable to login. Please try again.";
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

          <h3>SECURE LOGIN</h3>

          {error && (
            <div
              className="text-danger text-center mx-auto"
              style={{ background: "#f8d7da", color: "#721c24", padding: "6px 12px", borderRadius: 6, fontSize: 14, width: "fit-content", marginTop: 10 }}
            >
              {error}
            </div>
          )}

          <p>Access your Sys Bank account</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label" htmlFor="email">Email</label>
            <div className="input-group">
              <span className="input-group-text"><i className="fas fa-user"></i></span>
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
            <label className="form-label">Password</label>
            <div className="password-wrapper">
              <div className="input-group">
                <span className="input-group-text"><i className="fas fa-lock"></i></span>
                <input
                  type={showPassword ? "text" : "password"}
                  id="password"
                  className="form-control"
                  placeholder="Enter your password"
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

          <div className="d-flex justify-content-between mb-3">
            <div className="form-check">
              <input className="form-check-input" type="checkbox" id="rememberMe" />
              <label className="form-check-label" htmlFor="rememberMe">Remember me</label>
            </div>
            <a href={`${API_BASE}/main/forgot-password`} className="text-decoration-none" style={{ color: "white" }}>
              Forgot password?
            </a>
          </div>

          <div className="d-grid">
            <button type="submit" className="btn btn-login" disabled={loading}>
              {loading ? (
                <span><span className="spinner-border spinner-border-sm me-2"></span>Processing...</span>
              ) : (
                "Login"
              )}
            </button>
          </div>

          <div className="social-login">
            <div className="social-title">Or sign in with</div>
            <div className="social-buttons">
              <div className="social-btn google"><i className="fab fa-google"></i></div>
              <div className="social-btn facebook"><i className="fab fa-facebook-f"></i></div>
              <div className="social-btn twitter"><i className="fab fa-twitter"></i></div>
            </div>
          </div>

          <div className="login-footer">
            <p>Don&rsquo;t have an account? <Link to="/register">Register Now</Link></p>
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
