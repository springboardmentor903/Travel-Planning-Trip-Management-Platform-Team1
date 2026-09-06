import React from "react";
import { Link, useNavigate } from "react-router-dom";

function Home() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  return (
    <div style={styles.page}>
      {/* =================================================
          NAVBAR
      ================================================= */}
      <header style={styles.navbar}>
        <div style={styles.brand} onClick={() => navigate("/")}>
          <span style={styles.brandIcon}>✈️</span>
          <span className="tripnest-logo-text" style={styles.brandName}>
            TripNest
          </span>
        </div>

        <nav style={styles.navActions}>
          {token ? (
            <button
              style={styles.primaryButton}
              onClick={() => navigate("/dashboard")}
            >
              Go to Dashboard →
            </button>
          ) : (
            <>
              <Link to="/login" style={styles.secondaryButton}>
                Log In
              </Link>
              <Link to="/register" style={styles.primaryButton}>
                Get Started Free
              </Link>
            </>
          )}
        </nav>
      </header>

      {/* =================================================
          HERO SECTION
      ================================================= */}
      <section style={styles.heroSection}>
        <div style={styles.heroOverlay}>
          <div style={styles.heroContent}>
            <div style={styles.badge}>
              ✨ THE ULTIMATE TRAVEL MANAGEMENT PLATFORM
            </div>

            <h1 style={styles.heroTitle}>
              Welcome to <span style={styles.highlightText}>TripNest</span>
            </h1>

            <p style={styles.heroSubtitle}>
              Plan unforgettable journeys, organize daily itineraries, manage
              group expenses, collaborate with fellow travelers, and discover
              dream destinations — all in one unified place.
            </p>

            <div style={styles.heroButtons}>
              <button
                style={styles.ctaPrimary}
                onClick={() => navigate(token ? "/dashboard" : "/register")}
              >
                {token ? "Open Dashboard 🚀" : "Start Planning Your Trip →"}
              </button>

              {!token && (
                <button
                  style={styles.ctaSecondary}
                  onClick={() => navigate("/login")}
                >
                  Sign In to Account
                </button>
              )}
            </div>

            {/* QUICK STATS */}
            <div style={styles.statsRow}>
              <div style={styles.statItem}>
                <h3 style={styles.statNumber}>100%</h3>
                <p style={styles.statLabel}>Free to Plan</p>
              </div>
              <div style={styles.statDivider} />
              <div style={styles.statItem}>
                <h3 style={styles.statNumber}>👥 Collab</h3>
                <p style={styles.statLabel}>Trip Members & Roles</p>
              </div>
              <div style={styles.statDivider} />
              <div style={styles.statItem}>
                <h3 style={styles.statNumber}>💰 Real-Time</h3>
                <p style={styles.statLabel}>Budget & Expenses</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* =================================================
          KEY FEATURES SECTION
      ================================================= */}
      <section style={styles.featuresSection}>
        <div style={styles.sectionHeader}>
          <span style={styles.sectionTag}>WHAT WE OFFER</span>
          <h2 style={styles.sectionTitle}>Everything you need for seamless travel</h2>
          <p style={styles.sectionDesc}>
            TripNest takes the stress out of vacation planning with powerful,
            intuitive tools designed for solo adventurers and group travelers alike.
          </p>
        </div>

        <div style={styles.featuresGrid}>
          {/* Feature 1 */}
          <div style={styles.featureCard}>
            <div style={styles.featureIconBox}>🗓️</div>
            <h3 style={styles.featureTitle}>Day-by-Day Itineraries</h3>
            <p style={styles.featureText}>
              Organize your trip activities by day and timeline. Never miss a
              sightseeing spot, reservation, or adventure.
            </p>
          </div>

          {/* Feature 2 */}
          <div style={styles.featureCard}>
            <div style={styles.featureIconBox}>👥</div>
            <h3 style={styles.featureTitle}>Trip Members & Roles</h3>
            <p style={styles.featureText}>
              Invite travel buddies with Owner, Admin, and Member permissions.
              Collaborate and share travel details in real-time.
            </p>
          </div>

          {/* Feature 3 */}
          <div style={styles.featureCard}>
            <div style={styles.featureIconBox}>💰</div>
            <h3 style={styles.featureTitle}>Budget & Expense Tracking</h3>
            <p style={styles.featureText}>
              Set category budgets for hotels, dining, and transport. Log
              spending with visual breakdown charts.
            </p>
          </div>

          {/* Feature 4 */}
          <div style={styles.featureCard}>
            <div style={styles.featureIconBox}>🌤️</div>
            <h3 style={styles.featureTitle}>Live Weather Forecasts</h3>
            <p style={styles.featureText}>
              Check real-time weather conditions and packing tips for any city or
              destination before you take off.
            </p>
          </div>

          {/* Feature 5 */}
          <div style={styles.featureCard}>
            <div style={styles.featureIconBox}>📍</div>
            <h3 style={styles.featureTitle}>Curated Destinations</h3>
            <p style={styles.featureText}>
              Browse top trending travel destinations, popular attractions,
              and local sights to spark your wanderlust.
            </p>
          </div>

          {/* Feature 6 */}
          <div style={styles.featureCard}>
            <div style={styles.featureIconBox}>🔒</div>
            <h3 style={styles.featureTitle}>Secure & Cloud Sync</h3>
            <p style={styles.featureText}>
              Access your travel details from any device securely with JWT
              authentication and automatic updates.
            </p>
          </div>
        </div>
      </section>

      {/* =================================================
          CALL TO ACTION BANNER
      ================================================= */}
      <section style={styles.ctaBanner}>
        <div style={styles.ctaCard}>
          <h2 style={styles.ctaTitle}>Ready to begin your journey?</h2>
          <p style={styles.ctaDesc}>
            Join thousands of smart travelers planning their perfect getaways with TripNest today.
          </p>
          <div style={styles.ctaButtonGroup}>
            <button
              style={styles.ctaWhiteButton}
              onClick={() => navigate(token ? "/dashboard" : "/register")}
            >
              {token ? "Go to Dashboard →" : "Create Free Account →"}
            </button>
            {!token && (
              <button
                style={styles.ctaOutlineButton}
                onClick={() => navigate("/login")}
              >
                Log In
              </button>
            )}
          </div>
        </div>
      </section>

      {/* =================================================
          FOOTER
      ================================================= */}
      <footer style={styles.footer}>
        <div style={styles.footerTop}>
          <div style={styles.footerBrand}>
            <span style={styles.footerLogo}>✈️ TripNest</span>
            <p style={styles.footerTagline}>Travel. Plan. Explore.</p>
          </div>

          <div style={styles.footerLinks}>
            <Link to="/login" style={styles.footerLink}>
              Login
            </Link>
            <Link to="/register" style={styles.footerLink}>
              Register
            </Link>
            {token && (
              <>
                <Link to="/dashboard" style={styles.footerLink}>
                  Dashboard
                </Link>
                <Link to="/trips" style={styles.footerLink}>
                  My Trips
                </Link>
              </>
            )}
          </div>
        </div>

        <div style={styles.footerBottom}>
          <p>© 2026 TripNest Platform • All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}

// =====================================================
// INLINE STYLES
// =====================================================
const styles = {
  page: {
    minHeight: "100vh",
    fontFamily:
      '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, Cantarell, "Helvetica Neue", sans-serif',
    background: "transparent",
    color: "#0f172a",
    display: "flex",
    flexDirection: "column",
  },

  navbar: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "18px 6%",
    background: "#ffffff",
    borderBottom: "1px solid #e2e8f0",
    position: "sticky",
    top: 0,
    zIndex: 100,
    boxShadow: "0 2px 8px rgba(0,0,0,0.03)",
  },

  brand: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    cursor: "pointer",
  },

  brandIcon: {
    fontSize: "26px",
  },

  brandName: {
    fontSize: "22px",
    fontWeight: "800",
    letterSpacing: "-0.5px",
  },

  navActions: {
    display: "flex",
    alignItems: "center",
    gap: "14px",
  },

  secondaryButton: {
    padding: "9px 18px",
    borderRadius: "8px",
    border: "1px solid #cbd5e1",
    background: "#ffffff",
    color: "#0f172a",
    fontWeight: "600",
    fontSize: "14px",
    textDecoration: "none",
    cursor: "pointer",
    transition: "all 0.2s ease",
  },

  primaryButton: {
    padding: "9px 20px",
    borderRadius: "8px",
    border: "none",
    background: "linear-gradient(135deg, #0284c7 0%, #0369a1 100%)",
    color: "#ffffff",
    fontWeight: "600",
    fontSize: "14px",
    textDecoration: "none",
    cursor: "pointer",
    boxShadow: "0 4px 12px rgba(2, 132, 199, 0.25)",
    transition: "all 0.2s ease",
  },

  heroSection: {
    position: "relative",
    background:
      'linear-gradient(135deg, rgba(5, 35, 65, 0.88), rgba(0, 119, 182, 0.75)), url("https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1800&q=80")',
    backgroundSize: "cover",
    backgroundPosition: "center",
    color: "#ffffff",
    padding: "90px 6% 100px",
  },

  heroOverlay: {
    maxWidth: "1100px",
    margin: "0 auto",
  },

  heroContent: {
    maxWidth: "760px",
  },

  badge: {
    display: "inline-block",
    background: "rgba(255, 255, 255, 0.15)",
    backdropFilter: "blur(8px)",
    padding: "6px 14px",
    borderRadius: "20px",
    fontSize: "12px",
    fontWeight: "700",
    letterSpacing: "1px",
    marginBottom: "20px",
    border: "1px solid rgba(255, 255, 255, 0.2)",
  },

  heroTitle: {
    fontSize: "clamp(34px, 5vw, 56px)",
    fontWeight: "800",
    lineHeight: "1.15",
    letterSpacing: "-1px",
    margin: "0 0 20px",
  },

  highlightText: {
    background: "linear-gradient(135deg, #38bdf8 0%, #a5f3fc 100%)",
    WebkitBackgroundClip: "text",
    WebkitTextFillColor: "transparent",
  },

  heroSubtitle: {
    fontSize: "clamp(16px, 2vw, 19px)",
    lineHeight: "1.6",
    color: "#e2e8f0",
    marginBottom: "36px",
    maxWidth: "680px",
  },

  heroButtons: {
    display: "flex",
    gap: "16px",
    flexWrap: "wrap",
    marginBottom: "50px",
  },

  ctaPrimary: {
    padding: "14px 28px",
    borderRadius: "10px",
    border: "none",
    background: "linear-gradient(135deg, #38bdf8 0%, #0284c7 100%)",
    color: "#042f2e",
    fontWeight: "700",
    fontSize: "16px",
    cursor: "pointer",
    boxShadow: "0 6px 20px rgba(56, 189, 248, 0.4)",
    transition: "transform 0.2s",
  },

  ctaSecondary: {
    padding: "14px 26px",
    borderRadius: "10px",
    border: "1px solid rgba(255, 255, 255, 0.4)",
    background: "rgba(255, 255, 255, 0.1)",
    backdropFilter: "blur(6px)",
    color: "#ffffff",
    fontWeight: "600",
    fontSize: "16px",
    cursor: "pointer",
  },

  statsRow: {
    display: "flex",
    alignItems: "center",
    gap: "28px",
    paddingTop: "24px",
    borderTop: "1px solid rgba(255, 255, 255, 0.15)",
    flexWrap: "wrap",
  },

  statItem: {
    display: "flex",
    flexDirection: "column",
  },

  statNumber: {
    margin: 0,
    fontSize: "20px",
    fontWeight: "800",
    color: "#38bdf8",
  },

  statLabel: {
    margin: "4px 0 0",
    fontSize: "13px",
    color: "#cbd5e1",
  },

  statDivider: {
    width: "1px",
    height: "30px",
    background: "rgba(255, 255, 255, 0.2)",
  },

  featuresSection: {
    maxWidth: "1160px",
    margin: "0 auto",
    padding: "80px 6%",
    width: "100%",
  },

  sectionHeader: {
    textAlign: "center",
    maxWidth: "680px",
    margin: "0 auto 50px",
  },

  sectionTag: {
    display: "inline-block",
    color: "#0284c7",
    fontWeight: "800",
    fontSize: "12px",
    letterSpacing: "1.2px",
    marginBottom: "10px",
  },

  sectionTitle: {
    fontSize: "32px",
    fontWeight: "800",
    letterSpacing: "-0.5px",
    margin: "0 0 16px",
    color: "#0f172a",
  },

  sectionDesc: {
    fontSize: "16px",
    color: "#64748b",
    lineHeight: "1.6",
    margin: 0,
  },

  featuresGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
    gap: "24px",
  },

  featureCard: {
    background: "#ffffff",
    borderRadius: "16px",
    padding: "32px 28px",
    border: "1px solid #e2e8f0",
    boxShadow: "0 4px 16px rgba(0,0,0,0.03)",
    transition: "transform 0.2s, box-shadow 0.2s",
  },

  featureIconBox: {
    width: "52px",
    height: "52px",
    borderRadius: "12px",
    background: "#f0f9ff",
    border: "1px solid #bae6fd",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "26px",
    marginBottom: "18px",
  },

  featureTitle: {
    fontSize: "19px",
    fontWeight: "700",
    margin: "0 0 10px",
    color: "#0f172a",
  },

  featureText: {
    fontSize: "14px",
    color: "#64748b",
    lineHeight: "1.6",
    margin: 0,
  },

  ctaBanner: {
    maxWidth: "1160px",
    margin: "0 auto 80px",
    padding: "0 6%",
    width: "100%",
  },

  ctaCard: {
    background: "linear-gradient(135deg, #0369a1 0%, #0c4a6e 100%)",
    borderRadius: "20px",
    padding: "50px 40px",
    textAlign: "center",
    color: "#ffffff",
    boxShadow: "0 10px 30px rgba(3, 105, 161, 0.25)",
  },

  ctaTitle: {
    fontSize: "30px",
    fontWeight: "800",
    margin: "0 0 12px",
    letterSpacing: "-0.5px",
  },

  ctaDesc: {
    fontSize: "16px",
    color: "#e0f2fe",
    maxWidth: "540px",
    margin: "0 auto 28px",
    lineHeight: "1.5",
  },

  ctaButtonGroup: {
    display: "flex",
    justifyContent: "center",
    gap: "14px",
    flexWrap: "wrap",
  },

  ctaWhiteButton: {
    padding: "13px 28px",
    borderRadius: "10px",
    border: "none",
    background: "#ffffff",
    color: "#0369a1",
    fontWeight: "700",
    fontSize: "15px",
    cursor: "pointer",
    boxShadow: "0 4px 12px rgba(0,0,0,0.15)",
  },

  ctaOutlineButton: {
    padding: "13px 26px",
    borderRadius: "10px",
    border: "1px solid rgba(255, 255, 255, 0.4)",
    background: "rgba(255, 255, 255, 0.1)",
    color: "#ffffff",
    fontWeight: "600",
    fontSize: "15px",
    cursor: "pointer",
  },

  footer: {
    background: "#0f172a",
    color: "#94a3b8",
    padding: "50px 6% 30px",
    marginTop: "auto",
  },

  footerTop: {
    maxWidth: "1160px",
    margin: "0 auto",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    paddingBottom: "30px",
    borderBottom: "1px solid #1e293b",
    flexWrap: "wrap",
    gap: "20px",
  },

  footerBrand: {
    display: "flex",
    flexDirection: "column",
    gap: "4px",
  },

  footerLogo: {
    color: "#ffffff",
    fontSize: "20px",
    fontWeight: "800",
  },

  footerTagline: {
    margin: 0,
    fontSize: "13px",
    color: "#64748b",
  },

  footerLinks: {
    display: "flex",
    gap: "20px",
  },

  footerLink: {
    color: "#94a3b8",
    textDecoration: "none",
    fontSize: "14px",
    fontWeight: "500",
    transition: "color 0.2s",
  },

  footerBottom: {
    maxWidth: "1160px",
    margin: "24px auto 0",
    textAlign: "center",
    fontSize: "13px",
    color: "#64748b",
  },
};

export default Home;
