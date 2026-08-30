import React, { useEffect, useState } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

function Dashboard() {
  const navigate = useNavigate();

  const [trips, setTrips] = useState([]);
  const [popularDestinations, setPopularDestinations] = useState([]);

  const [dashboardStats, setDashboardStats] = useState({
    totalTrips: 0,
    upcomingTrips: 0,
    pastTrips: 0,
    popularDestinations: 0,
  });

  const [loadingTrips, setLoadingTrips] = useState(true);
  const [loadingDestinations, setLoadingDestinations] = useState(true);
  const [error, setError] = useState("");

  // =====================================================
  // WEATHER STATES
  // =====================================================

  const [weatherCity, setWeatherCity] = useState("Delhi");
  const [weather, setWeather] = useState(null);
  const [loadingWeather, setLoadingWeather] = useState(false);
  const [weatherError, setWeatherError] = useState("");

  const userName = localStorage.getItem("userName") || "Traveler";

  // =====================================================
  // AUTH CONFIG
  // =====================================================

  const getAuthConfig = () => {
    const currentToken = localStorage.getItem("token");

    return {
      headers: {
        Authorization: `Bearer ${currentToken}`,
        "Content-Type": "application/json",
      },
    };
  };

  // =====================================================
  // LOAD DASHBOARD DATA
  // =====================================================

  useEffect(() => {
    const currentToken = localStorage.getItem("token");

    if (!currentToken) {
      navigate("/login");
      return;
    }

    fetchTrips();
    fetchPopularDestinations();
    fetchDashboardStats();
    fetchWeather("Delhi");

    const handleFocus = () => {
      fetchTrips();
    };

    window.addEventListener("focus", handleFocus);

    return () => {
      window.removeEventListener("focus", handleFocus);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // =====================================================
  // GET USER'S TRIPS
  // =====================================================

  const fetchTrips = async () => {
    try {
      setLoadingTrips(true);

      const response = await axios.get(
        "http://localhost:8080/api/trips/my",
        getAuthConfig(),
      );

      console.log("My Trips:", response.data);

      if (Array.isArray(response.data)) {
        setTrips(response.data);
      } else {
        setTrips([]);
      }
    } catch (err) {
      console.error("Error fetching trips:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError("Unable to load your trips.");
    } finally {
      setLoadingTrips(false);
    }
  };

  // =====================================================
  // GET POPULAR DESTINATIONS
  // =====================================================

  const fetchPopularDestinations = async () => {
    try {
      setLoadingDestinations(true);

      const response = await axios.get(
        "http://localhost:8080/api/destinations/popular",
        getAuthConfig(),
      );

      console.log("Popular Destinations:", response.data);

      if (Array.isArray(response.data)) {
        setPopularDestinations(response.data);
      } else {
        setPopularDestinations([]);
      }
    } catch (err) {
      console.error("Error fetching popular destinations:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError("Unable to load popular destinations.");
    } finally {
      setLoadingDestinations(false);
    }
  };

  // =====================================================
  // GET DASHBOARD STATISTICS FROM BACKEND
  // =====================================================

  const fetchDashboardStats = async () => {
    try {
      const response = await axios.get(
        "http://localhost:8080/api/dashboard/stats",
        getAuthConfig(),
      );

      console.log("Dashboard Stats:", response.data);

      setDashboardStats({
        totalTrips: response.data.totalTrips ?? 0,
        upcomingTrips: response.data.upcomingTrips ?? 0,
        pastTrips: response.data.pastTrips ?? 0,
        popularDestinations: response.data.popularDestinations ?? 0,
      });
    } catch (err) {
      console.error("Error fetching dashboard stats:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError("Unable to load dashboard statistics.");
    }
  };

  // =====================================================
  // WEATHER API
  // =====================================================

  const fetchWeather = async (city) => {
    if (!city || city.trim() === "") {
      setWeatherError("Please enter a city name.");
      return;
    }

    try {
      setLoadingWeather(true);
      setWeatherError("");

      const response = await axios.get(
        `http://localhost:8080/api/weather/${encodeURIComponent(city.trim())}`,
        getAuthConfig(),
      );

      console.log("Weather Response:", response.data);
      setWeather(response.data);
    } catch (err) {
      console.error("Error fetching weather:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setWeather(null);
      setWeatherError("Unable to load weather. Please check the city name.");
    } finally {
      setLoadingWeather(false);
    }
  };

  // Format date helper
  const formatDate = (dateStr) => {
    if (!dateStr) return "TBD";
    return new Date(dateStr).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  // Status color helper
  const getStatusBadgeStyle = (status) => {
    switch (status?.toUpperCase()) {
      case "ACTIVE":
      case "IN_PROGRESS":
        return {
          background: "#ecfdf5",
          color: "#059669",
          border: "1px solid #a7f3d0",
        };
      case "COMPLETED":
        return {
          background: "#f1f5f9",
          color: "#475569",
          border: "1px solid #cbd5e1",
        };
      case "PLANNED":
      default:
        return {
          background: "#eff6ff",
          color: "#2563eb",
          border: "1px solid #bfdbfe",
        };
    }
  };

  return (
    <div style={styles.page}>
      {/* Shared Navbar */}
      <Navbar activePage="/dashboard" />

      <main style={styles.container}>
        {error && <div style={styles.errorAlert}>⚠️ {error}</div>}

        {/* =================================================
            HERO WELCOME BANNER
        ================================================= */}
        <section style={styles.welcomeBanner}>
          <div style={styles.welcomeContent}>
            <span style={styles.welcomeBadge}>✈️ READY FOR ADVENTURE</span>
            <h1 style={styles.welcomeTitle}>
              Welcome back, {userName.split(" ")[0]}! 👋
            </h1>
            <p style={styles.welcomeSubtitle}>
              Here is an overview of your journeys, upcoming itineraries, and
              destination ideas.
            </p>
          </div>

          <div style={styles.welcomeActions}>
            <Link to="/trips/create" style={styles.primaryActionButton}>
              <span>＋</span>
              <span>Create New Trip</span>
            </Link>
            <Link to="/destinations" style={styles.secondaryActionButton}>
              <span>🌍</span>
              <span>Explore Places</span>
            </Link>
          </div>
        </section>

        {/* =================================================
            STATS OVERVIEW
        ================================================= */}
        <section style={styles.statsGrid}>
          {/* Card 1 */}
          <div style={styles.statCard}>
            <div
              style={{
                ...styles.statIconBox,
                background: "#e0f2fe",
                color: "#0284c7",
              }}
            >
              ✈️
            </div>
            <div>
              <div style={styles.statLabel}>Total Trips</div>
              <div style={styles.statNumber}>{dashboardStats.totalTrips}</div>
            </div>
          </div>

          {/* Card 2 */}
          <div style={{ ...styles.statCard, borderLeft: "4px solid #10b981" }}>
            <div
              style={{
                ...styles.statIconBox,
                background: "#ecfdf5",
                color: "#059669",
              }}
            >
              🗓️
            </div>
            <div>
              <div style={styles.statLabel}>Upcoming Trips</div>
              <div style={styles.statNumber}>
                {dashboardStats.upcomingTrips}
              </div>
            </div>
          </div>

          {/* Card 3 */}
          <div style={styles.statCard}>
            <div
              style={{
                ...styles.statIconBox,
                background: "#f3e8ff",
                color: "#7e22ce",
              }}
            >
              🗺️
            </div>
            <div>
              <div style={styles.statLabel}>Past Trips</div>
              <div style={styles.statNumber}>{dashboardStats.pastTrips}</div>
            </div>
          </div>

          {/* Card 4 */}
          <div style={styles.statCard}>
            <div
              style={{
                ...styles.statIconBox,
                background: "#fef3c7",
                color: "#b45309",
              }}
            >
              📍
            </div>
            <div>
              <div style={styles.statLabel}>Destinations</div>
              <div style={styles.statNumber}>
                {dashboardStats.popularDestinations}
              </div>
            </div>
          </div>
        </section>

        {/* =================================================
            LIVE WEATHER WIDGET
        ================================================= */}
        <section style={styles.weatherCard}>
          <div style={styles.weatherHeader}>
            <div>
              <h2 style={styles.cardHeading}>Live Destination Weather 🌤️</h2>
              <p style={styles.cardSubheading}>
                Check real-time conditions and forecast before heading out
              </p>
            </div>

            <div style={styles.weatherSearchWrapper}>
              <input
                type="text"
                value={weatherCity}
                onChange={(e) => setWeatherCity(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && weatherCity.trim()) {
                    fetchWeather(weatherCity);
                  }
                }}
                placeholder="Search city (e.g. Paris, Goa, Tokyo)"
                style={styles.weatherInput}
              />
              <button
                style={styles.weatherBtn}
                onClick={() => fetchWeather(weatherCity)}
                disabled={loadingWeather}
              >
                {loadingWeather ? "Checking..." : "Check"}
              </button>
            </div>
          </div>

          {weatherError && (
            <div style={styles.weatherErrorAlert}>{weatherError}</div>
          )}

          {loadingWeather ? (
            <div style={styles.weatherLoadingBox}>
              <div style={styles.spinnerEmoji}>⏳</div>
              <p>Fetching latest weather data for {weatherCity}...</p>
            </div>
          ) : weather ? (
            (() => {
              // Safely extract fields — handles both raw OWM format and pre-mapped backend DTOs
              const temp =
                weather.temperature != null
                  ? weather.temperature
                  : weather.temp != null
                    ? weather.temp
                    : weather.main?.temp != null
                      ? Math.round(weather.main.temp)
                      : "--";

              const humidity =
                weather.humidity != null
                  ? weather.humidity
                  : weather.main?.humidity != null
                    ? weather.main.humidity
                    : "N/A";

              const windSpeedVal =
                weather.windSpeed != null
                  ? weather.windSpeed
                  : weather.wind != null
                    ? typeof weather.wind === "object"
                      ? (weather.wind.speed ?? "N/A")
                      : weather.wind
                    : "N/A";

              const conditionText =
                weather.condition ||
                weather.description ||
                (Array.isArray(weather.weather)
                  ? weather.weather[0]?.description
                  : null) ||
                "Clear skies";

              const iconDisplay =
                typeof weather.icon === "string" && weather.icon
                  ? weather.icon
                  : "⛅";

              const tip =
                typeof weather.tip === "string"
                  ? weather.tip
                  : "Great weather for outdoor exploration!";

              return (
                <div style={styles.weatherDetailsGrid}>
                  <div style={styles.weatherMain}>
                    <span style={styles.weatherIconLarge}>{iconDisplay}</span>
                    <div>
                      <h3 style={styles.weatherTemp}>{temp}°C</h3>
                      <div style={styles.weatherCondition}>{conditionText}</div>
                      <div style={styles.weatherCityName}>📍 {weatherCity}</div>
                    </div>
                  </div>

                  <div style={styles.weatherMetrics}>
                    <div style={styles.metricItem}>
                      <span style={styles.metricLabel}>💧 Humidity</span>
                      <strong style={styles.metricValue}>{humidity}%</strong>
                    </div>

                    <div style={styles.metricItem}>
                      <span style={styles.metricLabel}>💨 Wind</span>
                      <strong style={styles.metricValue}>
                        {windSpeedVal} km/h
                      </strong>
                    </div>

                    <div style={styles.metricItem}>
                      <span style={styles.metricLabel}>🎒 Travel Tip</span>
                      <strong style={styles.metricValueTip}>{tip}</strong>
                    </div>
                  </div>
                </div>
              );
            })()
          ) : null}
        </section>

        {/* =================================================
            MY TRIPS SECTION
        ================================================= */}
        <section style={styles.sectionBlock}>
          <div style={styles.sectionTitleRow}>
            <div>
              <h2 style={styles.sectionHeading}>My Trips 🧳</h2>
              <p style={styles.sectionSub}>Trips you own or collaborate on</p>
            </div>

            <Link to="/trips" style={styles.viewAllLink}>
              View All Trips →
            </Link>
          </div>

          {loadingTrips ? (
            <div style={styles.tripsGrid}>
              {[1, 2, 3].map((n) => (
                <div key={n} style={styles.tripSkeletonCard}>
                  <div
                    style={{
                      height: "160px",
                      background: "#f1f5f9",
                      borderRadius: "12px",
                    }}
                  />
                  <div
                    style={{
                      height: "20px",
                      background: "#f1f5f9",
                      borderRadius: "4px",
                      width: "70%",
                      marginTop: "16px",
                    }}
                  />
                  <div
                    style={{
                      height: "14px",
                      background: "#f1f5f9",
                      borderRadius: "4px",
                      width: "40%",
                      marginTop: "8px",
                    }}
                  />
                </div>
              ))}
            </div>
          ) : trips.length === 0 ? (
            <div style={styles.emptyCard}>
              <div style={styles.emptyIcon}>🌍</div>
              <h3 style={styles.emptyTitle}>No trips found</h3>
              <p style={styles.emptySubtitle}>
                You haven't created or joined any trips yet. Start planning your
                next journey now!
              </p>
              <Link to="/trips/create" style={styles.primaryActionButton}>
                ＋ Plan Your First Trip
              </Link>
            </div>
          ) : (
            <div style={styles.tripsGrid}>
              {trips.slice(0, 6).map((trip) => {
                const destName =
                  trip.destination?.name ||
                  trip.destination?.destinationName ||
                  "Dream Destination";

                const imageUrl =
                  trip.destination?.imageUrl ||
                  trip.destination?.photoUrl ||
                  "/images/tripImage.png";

                return (
                  <div
                    key={trip.id}
                    style={styles.tripCard}
                    className="trip-card-hover"
                  >
                    <div style={styles.tripImageContainer}>
                      <img
                        src={imageUrl}
                        alt={destName}
                        style={styles.tripImage}
                      />
                      <span
                        style={{
                          ...styles.statusBadge,
                          ...getStatusBadgeStyle(trip.status),
                        }}
                      >
                        {trip.status || "PLANNED"}
                      </span>
                    </div>

                    <div style={styles.tripCardBody}>
                      <h3 style={styles.tripCardTitle}>{destName}</h3>

                      <div style={styles.tripMetaRow}>
                        <span>
                          📅 {formatDate(trip.startDate)} -{" "}
                          {formatDate(trip.endDate)}
                        </span>
                      </div>

                      <div style={styles.tripMetaFooter}>
                        {trip.budget ? (
                          <div style={styles.tripBudget}>
                            <span style={styles.budgetLabel}>Budget:</span>
                            <span style={styles.budgetValue}>
                              ₹{Number(trip.budget).toLocaleString()}
                            </span>
                          </div>
                        ) : (
                          <span style={styles.budgetLabel}>
                            👥 {trip.travelers || 1} Travelers
                          </span>
                        )}

                        <button
                          style={styles.viewDetailsBtn}
                          onClick={() => navigate(`/trips/${trip.id}`)}
                        >
                          View Details →
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        {/* =================================================
            POPULAR DESTINATIONS SHOWCASE
        ================================================= */}
        <section style={styles.sectionBlock}>
          <div style={styles.sectionTitleRow}>
            <div>
              <h2 style={styles.sectionHeading}>Trending Destinations 📍</h2>
              <p style={styles.sectionSub}>
                Popular places to inspire your next travel itinerary
              </p>
            </div>

            <Link to="/destinations" style={styles.viewAllLink}>
              Browse All Places →
            </Link>
          </div>

          {loadingDestinations ? (
            <div style={styles.destGrid}>
              {[1, 2, 3, 4].map((n) => (
                <div key={n} style={styles.destSkeletonCard} />
              ))}
            </div>
          ) : popularDestinations.length === 0 ? (
            <div style={styles.emptyCardMini}>
              <p>
                Explore destinations to discover top travel recommendations.
              </p>
            </div>
          ) : (
            <div style={styles.destGrid}>
              {popularDestinations.slice(0, 4).map((dest) => (
                <div
                  key={dest.id || dest.name}
                  style={styles.destCard}
                  className="trip-card-hover"
                  onClick={() => {
                    if (dest.placeId || dest.id) {
                      navigate(
                        `/destination-details/${dest.placeId || dest.id}`,
                      );
                    }
                  }}
                >
                  <img
                    src={
                      dest.imageUrl ||
                       "/images/destinationImage.png"
                    }
                    alt={dest.name}
                    style={styles.destImage}
                  />
                  <div style={styles.destOverlay}>
                    <h4 style={styles.destName}>{dest.name}</h4>
                    {dest.description && (
                      <p style={styles.destSnippet}>
                        {dest.description.slice(0, 70)}...
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    background: "#f8fafc",
    color: "#0f172a",
    display: "flex",
    flexDirection: "column",
  },

  container: {
    maxWidth: "1240px",
    width: "92%",
    margin: "0 auto",
    padding: "32px 0 60px",
  },

  errorAlert: {
    background: "#fef2f2",
    border: "1px solid #fecaca",
    color: "#b91c1c",
    padding: "14px 18px",
    borderRadius: "12px",
    marginBottom: "24px",
    fontWeight: "600",
  },

  welcomeBanner: {
    background: "linear-gradient(135deg, #0c4a6e 0%, #0369a1 100%)",
    borderRadius: "20px",
    padding: "36px 40px",
    color: "#ffffff",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "28px",
    boxShadow: "0 10px 25px rgba(3, 105, 161, 0.15)",
    flexWrap: "wrap",
    gap: "24px",
  },

  welcomeContent: {
    maxWidth: "600px",
  },

  welcomeBadge: {
    display: "inline-block",
    background: "rgba(255, 255, 255, 0.15)",
    padding: "4px 12px",
    borderRadius: "20px",
    fontSize: "11px",
    fontWeight: "800",
    letterSpacing: "1px",
    marginBottom: "12px",
  },

  welcomeTitle: {
    margin: "0 0 8px",
    fontSize: "clamp(22px, 3.5vw, 32px)",
    fontWeight: "800",
    color: "#ffffff",
  },

  welcomeSubtitle: {
    margin: 0,
    fontSize: "15px",
    color: "#e0f2fe",
    lineHeight: "1.5",
  },

  welcomeActions: {
    display: "flex",
    gap: "12px",
    flexWrap: "wrap",
  },

  primaryActionButton: {
    display: "inline-flex",
    alignItems: "center",
    gap: "8px",
    background: "#ffffff",
    color: "#0284c7",
    padding: "12px 22px",
    borderRadius: "10px",
    fontWeight: "700",
    fontSize: "14px",
    textDecoration: "none",
    boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
    cursor: "pointer",
  },

  secondaryActionButton: {
    display: "inline-flex",
    alignItems: "center",
    gap: "8px",
    background: "rgba(255, 255, 255, 0.15)",
    color: "#ffffff",
    border: "1px solid rgba(255, 255, 255, 0.3)",
    padding: "12px 20px",
    borderRadius: "10px",
    fontWeight: "600",
    fontSize: "14px",
    textDecoration: "none",
    cursor: "pointer",
  },

  statsGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "18px",
    marginBottom: "28px",
  },

  statCard: {
    background: "#ffffff",
    borderRadius: "16px",
    padding: "22px",
    border: "1px solid #e2e8f0",
    display: "flex",
    alignItems: "center",
    gap: "16px",
    boxShadow: "0 2px 8px rgba(0,0,0,0.02)",
  },

  statIconBox: {
    width: "48px",
    height: "48px",
    borderRadius: "12px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "22px",
    flexShrink: 0,
  },

  statLabel: {
    fontSize: "13px",
    color: "#64748b",
    fontWeight: "600",
    marginBottom: "4px",
  },

  statNumber: {
    fontSize: "24px",
    fontWeight: "800",
    color: "#0f172a",
  },

  weatherCard: {
    background: "#ffffff",
    borderRadius: "18px",
    padding: "26px 30px",
    border: "1px solid #e2e8f0",
    boxShadow: "0 4px 16px rgba(0,0,0,0.02)",
    marginBottom: "36px",
  },

  weatherHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
    marginBottom: "20px",
    flexWrap: "wrap",
  },

  cardHeading: {
    margin: 0,
    fontSize: "20px",
    fontWeight: "700",
    color: "#0f172a",
  },

  cardSubheading: {
    margin: "4px 0 0",
    fontSize: "14px",
    color: "#64748b",
  },

  weatherSearchWrapper: {
    display: "flex",
    gap: "10px",
    alignItems: "center",
  },

  weatherInput: {
    padding: "10px 16px",
    borderRadius: "10px",
    border: "1px solid #cbd5e1",
    fontSize: "14px",
    outline: "none",
    width: "240px",
  },

  weatherBtn: {
    background: "#0284c7",
    color: "#ffffff",
    padding: "10px 18px",
    borderRadius: "10px",
    fontSize: "14px",
    fontWeight: "600",
    cursor: "pointer",
  },

  weatherErrorAlert: {
    background: "#fef2f2",
    color: "#b91c1c",
    padding: "10px 14px",
    borderRadius: "8px",
    fontSize: "13px",
    marginBottom: "14px",
  },

  weatherLoadingBox: {
    padding: "30px",
    textAlign: "center",
    color: "#64748b",
  },

  spinnerEmoji: {
    fontSize: "28px",
    marginBottom: "6px",
  },

  weatherDetailsGrid: {
    display: "grid",
    gridTemplateColumns: "1.2fr 2fr",
    gap: "24px",
    background: "#f0f9ff",
    border: "1px solid #bae6fd",
    borderRadius: "14px",
    padding: "22px 26px",
    alignItems: "center",
  },

  weatherMain: {
    display: "flex",
    alignItems: "center",
    gap: "18px",
  },

  weatherIconLarge: {
    fontSize: "46px",
  },

  weatherTemp: {
    margin: 0,
    fontSize: "32px",
    fontWeight: "800",
    color: "#0369a1",
  },

  weatherCondition: {
    fontSize: "15px",
    fontWeight: "600",
    color: "#0284c7",
    textTransform: "capitalize",
  },

  weatherCityName: {
    fontSize: "13px",
    color: "#64748b",
    marginTop: "2px",
  },

  weatherMetrics: {
    display: "grid",
    gridTemplateColumns: "repeat(3, 1fr)",
    gap: "14px",
    borderLeft: "1px solid #bae6fd",
    paddingLeft: "24px",
  },

  metricItem: {
    display: "flex",
    flexDirection: "column",
    gap: "4px",
  },

  metricLabel: {
    fontSize: "12px",
    color: "#64748b",
    fontWeight: "600",
  },

  metricValue: {
    fontSize: "16px",
    color: "#0f172a",
  },

  metricValueTip: {
    fontSize: "13px",
    color: "#0369a1",
    lineHeight: "1.4",
  },

  sectionBlock: {
    marginBottom: "40px",
  },

  sectionTitleRow: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-end",
    marginBottom: "20px",
    flexWrap: "wrap",
    gap: "12px",
  },

  sectionHeading: {
    margin: 0,
    fontSize: "22px",
    fontWeight: "800",
    color: "#0f172a",
  },

  sectionSub: {
    margin: "4px 0 0",
    fontSize: "14px",
    color: "#64748b",
  },

  viewAllLink: {
    fontSize: "14px",
    fontWeight: "700",
    color: "#0284c7",
    textDecoration: "none",
  },

  tripsGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
    gap: "24px",
  },

  tripCard: {
    background: "#ffffff",
    borderRadius: "16px",
    border: "1px solid #e2e8f0",
    overflow: "hidden",
    boxShadow: "0 2px 10px rgba(0,0,0,0.02)",
    display: "flex",
    flexDirection: "column",
  },

  tripImageContainer: {
    position: "relative",
    height: "170px",
    width: "100%",
  },

  tripImage: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
  },

  statusBadge: {
    position: "absolute",
    top: "12px",
    right: "12px",
    padding: "4px 10px",
    borderRadius: "20px",
    fontSize: "11px",
    fontWeight: "800",
    letterSpacing: "0.5px",
    backdropFilter: "blur(4px)",
  },

  tripCardBody: {
    padding: "20px",
    display: "flex",
    flexDirection: "column",
    flex: 1,
    justifyContent: "space-between",
  },

  tripCardTitle: {
    margin: "0 0 10px",
    fontSize: "18px",
    fontWeight: "700",
    color: "#0f172a",
  },

  tripMetaRow: {
    fontSize: "13px",
    color: "#64748b",
    marginBottom: "16px",
  },

  tripMetaFooter: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    paddingTop: "14px",
    borderTop: "1px solid #f1f5f9",
    gap: "10px",
    flexWrap: "wrap",
  },

  tripBudget: {
    display: "flex",
    flexDirection: "column",
  },

  budgetLabel: {
    fontSize: "11px",
    color: "#94a3b8",
    fontWeight: "600",
  },

  budgetValue: {
    fontSize: "15px",
    fontWeight: "700",
    color: "#0f172a",
  },

  viewDetailsBtn: {
    background: "#f0f9ff",
    color: "#0284c7",
    border: "1px solid #bae6fd",
    padding: "7px 14px",
    borderRadius: "8px",
    fontSize: "12px",
    fontWeight: "700",
    cursor: "pointer",
    transition: "background 0.15s",
  },

  emptyCard: {
    background: "#ffffff",
    border: "2px dashed #cbd5e1",
    borderRadius: "18px",
    padding: "48px 24px",
    textAlign: "center",
    maxWidth: "500px",
    margin: "0 auto",
  },

  emptyIcon: {
    fontSize: "44px",
    marginBottom: "14px",
  },

  emptyTitle: {
    margin: "0 0 8px",
    fontSize: "18px",
    fontWeight: "700",
  },

  emptySubtitle: {
    fontSize: "14px",
    color: "#64748b",
    marginBottom: "20px",
    lineHeight: "1.5",
  },

  emptyCardMini: {
    padding: "24px",
    background: "#ffffff",
    border: "1px solid #e2e8f0",
    borderRadius: "14px",
    textAlign: "center",
    color: "#64748b",
    fontSize: "14px",
  },

  destGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
    gap: "20px",
  },

  destCard: {
    position: "relative",
    height: "220px",
    borderRadius: "16px",
    overflow: "hidden",
    cursor: "pointer",
    boxShadow: "0 4px 14px rgba(0,0,0,0.04)",
  },

  destImage: {
    width: "100%",
    height: "100%",
    objectFit: "cover",
  },

  destOverlay: {
    position: "absolute",
    bottom: 0,
    left: 0,
    right: 0,
    padding: "20px 16px",
    background:
      "linear-gradient(to top, rgba(15, 23, 42, 0.85) 0%, transparent 100%)",
    color: "#ffffff",
  },

  destName: {
    margin: "0 0 4px",
    fontSize: "16px",
    fontWeight: "700",
    color: "#ffffff",
  },

  destSnippet: {
    margin: 0,
    fontSize: "12px",
    color: "#cbd5e1",
    lineHeight: "1.4",
  },

  tripSkeletonCard: {
    background: "#ffffff",
    border: "1px solid #e2e8f0",
    borderRadius: "16px",
    padding: "16px",
  },

  destSkeletonCard: {
    height: "220px",
    borderRadius: "16px",
    background: "#e2e8f0",
  },
};

export default Dashboard;
