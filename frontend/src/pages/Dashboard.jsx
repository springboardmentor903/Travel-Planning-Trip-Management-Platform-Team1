import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

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
  // WEATHER STATES - NEW
  // =====================================================

  const [weatherCity, setWeatherCity] = useState("Delhi");
  const [weather, setWeather] = useState(null);
  const [loadingWeather, setLoadingWeather] = useState(false);
  const [weatherError, setWeatherError] = useState("");

  // =====================================================
  // AUTH CONFIG
  // =====================================================

  // OLD CODE - kept for reference
  // const token = localStorage.getItem("token");
  // const authConfig = {
  //   headers: {
  //     Authorization: `Bearer ${token}`,
  //     "Content-Type": "application/json",
  //   },
  // };

  // FIX - dynamically reads latest JWT token
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
  // WEATHER API - NEW
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

  // =====================================================
  // LOGOUT
  // =====================================================

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  // =====================================================
  // CALCULATE UPCOMING TRIPS
  // =====================================================

  const upcomingTrips = trips.filter((trip) => {
    if (!trip.startDate) return false;

    const startDate = new Date(trip.startDate);
    const today = new Date();

    return startDate >= today;
  });

  // =====================================================
  // RECENT TRIPS
  // =====================================================

  const recentTrips = trips.filter((trip) => {
    if (!trip.startDate) return false;

    const startDate = new Date(trip.startDate);
    const today = new Date();

    return startDate < today;
  });

  return (
    <div style={styles.page}>
      {/* ================= HEADER ================= */}

      <header style={styles.header}>
        <div>
          <h1 style={styles.logo}>TripNest</h1>
          <p style={styles.subtitle}>Your travel dashboard</p>
        </div>

        <div style={styles.headerButtons}>
          <button
            style={styles.profileButton}
            onClick={() => navigate("/profile")}
          >
            Profile
          </button>

          <button style={styles.logoutButton} onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      {/* ================= MAIN ================= */}

      <main style={styles.container}>
        {error && <div style={styles.error}>{error}</div>}

        {/* ================= WELCOME ================= */}

        <section style={styles.welcomeCard}>
          <div>
            <h2>Welcome to TripNest 👋</h2>

            <p>
              Plan your trips, explore destinations and manage your travel plans
              from one place.
            </p>
          </div>

          <button
            style={styles.primaryButton}
            onClick={() => navigate("/trips/create")}
          >
            + Create New Trip
          </button>
        </section>

        {/* ================= STAT CARDS ================= */}

        <section style={styles.statsGrid}>
          <div style={styles.statCard}>
            <div style={styles.statIcon}>✈️</div>

            <div>
              <h3>{dashboardStats.totalTrips}</h3>
              <p>Total Trips</p>
            </div>
          </div>

          <div style={styles.statCard}>
            <div style={styles.statIcon}>📅</div>

            <div>
              <h3>{dashboardStats.upcomingTrips}</h3>
              <p>Upcoming Trips</p>
            </div>
          </div>

          <div style={styles.statCard}>
            <div style={styles.statIcon}>🗺️</div>

            <div>
              <h3>{dashboardStats.pastTrips}</h3>
              <p>Past Trips</p>
            </div>
          </div>

          <div style={styles.statCard}>
            <div style={styles.statIcon}>🌎</div>

            <div>
              <h3>{dashboardStats.popularDestinations}</h3>
              <p>Popular Destinations</p>
            </div>
          </div>
        </section>

        {/* =====================================================
            WEATHER - NEW
        ===================================================== */}

        <section style={styles.section}>
          <div style={styles.sectionHeader}>
            <div>
              <h2>Weather 🌤️</h2>

              <p>Check live weather for your destination</p>
            </div>
          </div>

          <div style={styles.weatherSearch}>
            <input
              type="text"
              value={weatherCity}
              onChange={(e) => setWeatherCity(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter" && weatherCity.trim()) {
                  fetchWeather(weatherCity);
                }
              }}
              placeholder="Enter city name"
              style={styles.weatherInput}
            />

            <button
              style={styles.weatherButton}
              onClick={() => fetchWeather(weatherCity)}
              disabled={loadingWeather}
            >
              {loadingWeather ? "Loading..." : "Check Weather"}
            </button>
          </div>

          {weatherError && (
            <div style={styles.weatherError}>{weatherError}</div>
          )}

          {loadingWeather && (
            <div style={styles.weatherLoading}>
              <div style={styles.weatherEmoji}>🌤️</div>

              <h3>Loading weather...</h3>

              <p>Getting live weather information for {weatherCity}</p>
            </div>
          )}

          {weather &&
            !loadingWeather &&
            (() => {
              // Backend String JSON ko object me convert karo
              let weatherData = weather;

              try {
                if (typeof weather === "string") {
                  weatherData = JSON.parse(weather);
                }
              } catch (error) {
                console.error("Unable to parse weather response:", error);
                weatherData = null;
              }

              // Agar response valid nahi hai
              if (!weatherData) {
                return (
                  <div style={styles.weatherError}>
                    Unable to display weather information.
                  </div>
                );
              }

              const temperature = weatherData?.main?.temp;
              const feelsLike = weatherData?.main?.feels_like;
              const humidity = weatherData?.main?.humidity;
              const pressure = weatherData?.main?.pressure;
              const windSpeed = weatherData?.wind?.speed;
              const windDegree = weatherData?.wind?.deg;
              const visibility = weatherData?.visibility;
              const condition = weatherData?.weather?.[0]?.description || "N/A";

              const weatherIcon = weatherData?.weather?.[0]?.icon;

              const cityName = weatherData?.name || weatherCity;

              const country = weatherData?.sys?.country || "";

              return (
                <div style={styles.weatherCard}>
                  {/* Header */}
                  <div style={styles.weatherCardHeader}>
                    <div>
                      <p style={styles.weatherLabel}>Current Weather</p>

                      <h2 style={styles.weatherCity}>
                        {cityName}
                        {country ? `, ${country}` : ""}
                      </h2>

                      <p
                        style={{
                          marginTop: "6px",
                          textTransform: "capitalize",
                          color: "#555",
                        }}
                      >
                        {condition}
                      </p>
                    </div>

                    <div
                      style={{
                        textAlign: "center",
                      }}
                    >
                      {weatherIcon ? (
                        <img
                          src={`https://openweathermap.org/img/wn/${weatherIcon}@2x.png`}
                          alt={condition}
                          style={{
                            width: "90px",
                            height: "90px",
                          }}
                        />
                      ) : (
                        <div style={styles.weatherBigIcon}>🌤️</div>
                      )}
                    </div>
                  </div>

                  {/* Main Temperature */}
                  <div
                    style={{
                      textAlign: "center",
                      marginTop: "20px",
                      marginBottom: "30px",
                    }}
                  >
                    <div
                      style={{
                        fontSize: "64px",
                        fontWeight: "700",
                        lineHeight: "1.1",
                        color: "#0f172a",
                        marginBottom: "8px",
                      }}
                    >
                      {weather.main?.temp}°C
                    </div>

                    <div
                      style={{
                        fontSize: "18px",
                        color: "#64748b",
                        lineHeight: "1.4",
                      }}
                    >
                      Feels like {weather.main?.feels_like}°C
                    </div>
                  </div>

                  {/* Weather Information */}
                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns:
                        "repeat(auto-fit, minmax(150px, 1fr))",
                      gap: "15px",
                    }}
                  >
                    {/* Humidity */}
                    <div
                      style={{
                        padding: "18px",
                        borderRadius: "12px",
                        background: "#f8fafc",
                        border: "1px solid #e5e7eb",
                        textAlign: "center",
                      }}
                    >
                      <div style={{ fontSize: "28px" }}>💧</div>

                      <div
                        style={{
                          fontSize: "20px",
                          fontWeight: "600",
                          marginTop: "5px",
                        }}
                      >
                        {humidity !== undefined ? `${humidity}%` : "N/A"}
                      </div>

                      <div
                        style={{
                          color: "#6b7280",
                          fontSize: "14px",
                        }}
                      >
                        Humidity
                      </div>
                    </div>

                    {/* Wind */}
                    <div
                      style={{
                        padding: "18px",
                        borderRadius: "12px",
                        background: "#f8fafc",
                        border: "1px solid #e5e7eb",
                        textAlign: "center",
                      }}
                    >
                      <div style={{ fontSize: "28px" }}>💨</div>

                      <div
                        style={{
                          fontSize: "20px",
                          fontWeight: "600",
                          marginTop: "5px",
                        }}
                      >
                        {windSpeed !== undefined ? `${windSpeed} m/s` : "N/A"}
                      </div>

                      <div
                        style={{
                          color: "#6b7280",
                          fontSize: "14px",
                        }}
                      >
                        Wind Speed
                      </div>
                    </div>

                    {/* Pressure */}
                    <div
                      style={{
                        padding: "18px",
                        borderRadius: "12px",
                        background: "#f8fafc",
                        border: "1px solid #e5e7eb",
                        textAlign: "center",
                      }}
                    >
                      <div style={{ fontSize: "28px" }}>🌡️</div>

                      <div
                        style={{
                          fontSize: "20px",
                          fontWeight: "600",
                          marginTop: "5px",
                        }}
                      >
                        {pressure !== undefined ? `${pressure} hPa` : "N/A"}
                      </div>

                      <div
                        style={{
                          color: "#6b7280",
                          fontSize: "14px",
                        }}
                      >
                        Pressure
                      </div>
                    </div>

                    {/* Visibility */}
                    <div
                      style={{
                        padding: "18px",
                        borderRadius: "12px",
                        background: "#f8fafc",
                        border: "1px solid #e5e7eb",
                        textAlign: "center",
                      }}
                    >
                      <div style={{ fontSize: "28px" }}>👁️</div>

                      <div
                        style={{
                          fontSize: "20px",
                          fontWeight: "600",
                          marginTop: "5px",
                        }}
                      >
                        {visibility !== undefined
                          ? `${(visibility / 1000).toFixed(1)} km`
                          : "N/A"}
                      </div>

                      <div
                        style={{
                          color: "#6b7280",
                          fontSize: "14px",
                        }}
                      >
                        Visibility
                      </div>
                    </div>

                    {/* Wind Direction */}
                    <div
                      style={{
                        padding: "18px",
                        borderRadius: "12px",
                        background: "#f8fafc",
                        border: "1px solid #e5e7eb",
                        textAlign: "center",
                      }}
                    >
                      <div style={{ fontSize: "28px" }}>🧭</div>

                      <div
                        style={{
                          fontSize: "20px",
                          fontWeight: "600",
                          marginTop: "5px",
                        }}
                      >
                        {windDegree !== undefined ? `${windDegree}°` : "N/A"}
                      </div>

                      <div
                        style={{
                          color: "#6b7280",
                          fontSize: "14px",
                        }}
                      >
                        Wind Direction
                      </div>
                    </div>
                  </div>
                </div>
              );
            })()}
        </section>

        {/* ================= UPCOMING TRIPS ================= */}

        <section style={styles.section}>
          <div style={styles.sectionHeader}>
            <div>
              <h2>Upcoming Trips</h2>

              <p>Your upcoming travel plans</p>
            </div>

            <button
              style={styles.viewButton}
              onClick={() => navigate("/travel-history")}
            >
              View All
            </button>
          </div>

          {loadingTrips ? (
            <div style={styles.loading}>Loading your trips...</div>
          ) : upcomingTrips.length === 0 ? (
            <div style={styles.emptyCard}>
              <div style={styles.emptyIcon}>✈️</div>

              <h3>No upcoming trips</h3>

              <p>Start planning your next adventure.</p>

              <button
                style={styles.primaryButton}
                onClick={() => navigate("/create-trip")}
              >
                Create Trip
              </button>
            </div>
          ) : (
            <div style={styles.cardsGrid}>
              {upcomingTrips.slice(0, 4).map((trip) => (
                <div style={styles.tripCard} key={trip.id}>
                  <div style={styles.tripIcon}>🌴</div>

                  <h3>{trip.tripName || "My Trip"}</h3>

                  <p>
                    📍{" "}
                    {trip.destination?.name ||
                      trip.destination?.destinationName ||
                      "Destination"}
                  </p>

                  <p>
                    📅 {trip.startDate || "N/A"} → {trip.endDate || "N/A"}
                  </p>

                  {trip.description && (
                    <p style={styles.description}>{trip.description}</p>
                  )}

                  <button
                    style={styles.detailsButton}
                    onClick={() => navigate(`/trips/${trip.id}`)}
                  >
                    View Trip
                  </button>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* ================= MY TRIPS ================= */}

        <section style={styles.section}>
          <div style={styles.sectionHeader}>
            <div>
              <h2>My Trips 🧳</h2>

              <p>View and manage all your trips</p>
            </div>

            <button
              style={styles.viewButton}
              onClick={() => navigate("/trips")}
            >
              View All
            </button>
          </div>

          {loadingTrips ? (
            <div style={styles.loading}>Loading your trips...</div>
          ) : trips.length === 0 ? (
            <div style={styles.emptyCard}>
              <div style={styles.emptyIcon}>🧳</div>

              <h3>No trips yet</h3>

              <p>You haven't created any trips yet.</p>

              <button
                style={styles.primaryButton}
                onClick={() => navigate("/trips/create")}
              >
                Create Trip
              </button>
            </div>
          ) : (
            <div style={styles.cardsGrid}>
              {trips.slice(0, 4).map((trip) => (
                <div style={styles.tripCard} key={trip.id}>
                  <div style={styles.tripIcon}>🧳</div>

                  <h3>{trip.destination?.name || "My Trip"}</h3>

                  <p>📍 {trip.destination?.name || "Destination"}</p>

                  <p>
                    📅 {trip.startDate || "N/A"} → {trip.endDate || "N/A"}
                  </p>

                  <p>👥 {trip.travelers || 0} Travelers</p>

                  <p>💰 ₹{trip.budget ?? "N/A"}</p>

                  <button
                    style={styles.detailsButton}
                    onClick={() => navigate(`/trips/${trip.id}`)}
                  >
                    View Trip
                  </button>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* ================= POPULAR DESTINATIONS ================= */}

        <section style={styles.section}>
          <div style={styles.sectionHeader}>
            <div>
              <h2>Popular Destinations</h2>

              <p>Explore destinations loved by travelers</p>
            </div>

            <button
              style={styles.viewButton}
              onClick={() => navigate("/destinations")}
            >
              Explore All
            </button>
          </div>

          {loadingDestinations ? (
            <div style={styles.loading}>Loading popular destinations...</div>
          ) : popularDestinations.length === 0 ? (
            <div style={styles.emptyCard}>
              <div style={styles.emptyIcon}>🌎</div>

              <h3>No popular destinations available</h3>

              <p>Check back later for popular destinations.</p>
            </div>
          ) : (
            <div style={styles.destinationGrid}>
              {popularDestinations.slice(0, 6).map((destination) => (
                <div style={styles.destinationCard} key={destination.id}>
                  <div style={styles.destinationImage}>🌄</div>

                  <div style={styles.destinationContent}>
                    <h3>
                      {destination.name ||
                        destination.destinationName ||
                        "Destination"}
                    </h3>

                    {destination.country && <p>📍 {destination.country}</p>}

                    {destination.description && (
                      <p style={styles.description}>
                        {destination.description}
                      </p>
                    )}

                    <button
                      style={styles.detailsButton}
                      onClick={() =>
                        navigate(
                          `/destinations?query=${encodeURIComponent(
                            destination.name ||
                              destination.destinationName ||
                              "",
                          )}`,
                        )
                      }
                    >
                      Explore
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* ================= QUICK ACTIONS ================= */}

        <section style={styles.section}>
          <div style={styles.sectionHeader}>
            <div>
              <h2>Quick Actions</h2>

              <p>Manage your travel easily</p>
            </div>
          </div>

          <div style={styles.quickGrid}>
            <button
              style={styles.quickCard}
              onClick={() => navigate("/trips/create")}
            >
              <span>➕</span>
              <strong>Create Trip</strong>
              <small>Plan your next journey</small>
            </button>

            <button
              style={styles.quickCard}
              onClick={() => navigate("/travel-history")}
            >
              <span>📚</span>
              <strong>Travel History</strong>
              <small>View your previous trips</small>
            </button>

            <button
              style={styles.quickCard}
              onClick={() => navigate("/profile")}
            >
              <span>👤</span>
              <strong>My Profile</strong>
              <small>Manage your profile</small>
            </button>

            <button
              style={styles.quickCard}
              onClick={() => navigate("/settings")}
            >
              <span>⚙️</span>
              <strong>Settings</strong>
              <small>Manage account settings</small>
            </button>
          </div>
        </section>
      </main>
    </div>
  );
}

/* =====================================================
   STYLES
===================================================== */

const styles = {
  page: {
    minHeight: "100vh",
    background: "#f5f7fb",
    color: "#1f2937",
    fontFamily:
      "Inter, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, sans-serif",
  },

  header: {
    background: "#ffffff",
    borderBottom: "1px solid #e5e7eb",
    padding: "20px 6%",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
  },

  logo: {
    margin: 0,
    fontSize: "28px",
    fontWeight: "800",
  },

  subtitle: {
    margin: "4px 0 0",
    color: "#6b7280",
  },

  headerButtons: {
    display: "flex",
    gap: "10px",
  },

  profileButton: {
    padding: "10px 18px",
    border: "1px solid #d1d5db",
    borderRadius: "8px",
    background: "#ffffff",
    cursor: "pointer",
  },

  logoutButton: {
    padding: "10px 18px",
    border: "none",
    borderRadius: "8px",
    background: "#ef4444",
    color: "#ffffff",
    cursor: "pointer",
  },

  container: {
    width: "88%",
    maxWidth: "1200px",
    margin: "0 auto",
    padding: "30px 0 60px",
  },

  welcomeCard: {
    background: "#09296d",
    color: "#ffffff",
    borderRadius: "18px",
    padding: "30px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
    marginBottom: "25px",
  },

  primaryButton: {
    border: "none",
    background: "#ffffff",
    color: "#111827",
    padding: "12px 20px",
    borderRadius: "9px",
    fontWeight: "700",
    cursor: "pointer",
    whiteSpace: "nowrap",
  },

  statsGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(210px, 1fr))",
    gap: "18px",
    marginBottom: "35px",
  },

  statCard: {
    background: "#ffffff",
    borderRadius: "14px",
    padding: "22px",
    display: "flex",
    alignItems: "center",
    gap: "15px",
    border: "1px solid #e5e7eb",
  },

  statIcon: {
    fontSize: "30px",
  },

  section: {
    marginTop: "35px",
  },

  sectionHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "15px",
    marginBottom: "18px",
  },

  viewButton: {
    border: "none",
    background: "transparent",
    cursor: "pointer",
    fontWeight: "700",
  },

  loading: {
    background: "#ffffff",
    borderRadius: "14px",
    padding: "35px",
    textAlign: "center",
    color: "#6b7280",
  },

  emptyCard: {
    background: "#ffffff",
    border: "1px dashed #d1d5db",
    borderRadius: "14px",
    padding: "45px 20px",
    textAlign: "center",
  },

  emptyIcon: {
    fontSize: "40px",
    marginBottom: "10px",
  },

  cardsGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
    gap: "20px",
  },

  tripCard: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "15px",
    padding: "22px",
    boxShadow: "0 4px 12px rgba(0,0,0,0.04)",
  },

  tripIcon: {
    fontSize: "35px",
    marginBottom: "8px",
  },

  description: {
    color: "#6b7280",
    fontSize: "14px",
  },

  detailsButton: {
    marginTop: "10px",
    padding: "9px 15px",
    border: "none",
    borderRadius: "7px",
    background: "#111827",
    color: "#ffffff",
    cursor: "pointer",
  },

  destinationGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
    gap: "20px",
  },

  destinationCard: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "15px",
    overflow: "hidden",
  },

  destinationImage: {
    height: "130px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "55px",
    background: "#eef2ff",
  },

  destinationContent: {
    padding: "18px",
  },

  quickGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "15px",
  },

  quickCard: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "14px",
    padding: "22px",
    textAlign: "left",
    cursor: "pointer",
    display: "flex",
    flexDirection: "column",
    gap: "7px",
  },

  error: {
    background: "#fee2e2",
    color: "#991b1b",
    padding: "14px",
    borderRadius: "10px",
    marginBottom: "20px",
  },

  // =====================================================
  // WEATHER STYLES - NEW
  // =====================================================

  weatherSearch: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "14px",
    padding: "15px",
    display: "flex",
    gap: "10px",
    marginBottom: "18px",
  },

  weatherInput: {
    flex: 1,
    minWidth: 0,
    padding: "12px 14px",
    border: "1px solid #d1d5db",
    borderRadius: "8px",
    fontSize: "15px",
    outline: "none",
  },

  weatherButton: {
    padding: "12px 20px",
    border: "none",
    borderRadius: "8px",
    background: "#111827",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600",
  },

  weatherLoading: {
    background: "#ffffff",
    borderRadius: "14px",
    padding: "40px",
    textAlign: "center",
    border: "1px solid #e5e7eb",
  },

  weatherEmoji: {
    fontSize: "50px",
  },

  weatherError: {
    background: "#fee2e2",
    color: "#991b1b",
    padding: "14px",
    borderRadius: "10px",
    marginBottom: "18px",
  },

  weatherCard: {
    background: "linear-gradient(135deg, #dbeafe, #eff6ff)",
    borderRadius: "18px",
    padding: "25px",
    border: "1px solid #bfdbfe",
  },

  weatherCardHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
    marginBottom: "20px",
  },

  weatherLabel: {
    margin: 0,
    color: "#6b7280",
    fontSize: "14px",
  },

  weatherCity: {
    margin: "5px 0 0",
  },

  weatherBigIcon: {
    fontSize: "55px",
  },

  weatherDataBox: {
    background: "#ffffff",
    borderRadius: "12px",
    padding: "18px",
    overflowX: "auto",
  },

  weatherRaw: {
    margin: 0,
    whiteSpace: "pre-wrap",
    wordBreak: "break-word",
    fontSize: "14px",
    lineHeight: "1.6",
    color: "#374151",
  },
};

export default Dashboard;
