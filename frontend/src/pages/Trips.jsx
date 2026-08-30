import React, { useEffect, useState } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

function Trips() {
  const navigate = useNavigate();

  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  useEffect(() => {
    fetchTrips();
  }, []);

  const fetchTrips = async () => {
    try {
      setLoading(true);
      setError("");
      const token = localStorage.getItem("token");

      if (!token) {
        navigate("/login");
        return;
      }

      const response = await axios.get("http://localhost:8080/api/trips/my", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      console.log("TRIPS RESPONSE:", response.data);
      setTrips(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Error fetching trips:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError("Unable to load your trips. Please check your connection.");
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (date) => {
    if (!date) return "TBD";
    return new Date(date).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  const getDestinationName = (trip) => {
    if (trip?.destination?.name) {
      return trip.destination.name;
    }
    return "Dream Destination";
  };

  const getStatusBadgeStyle = (status) => {
    switch (status?.toUpperCase()) {
      case "ACTIVE":
      case "IN_PROGRESS":
        return { background: "#ecfdf5", color: "#059669", border: "1px solid #a7f3d0" };
      case "COMPLETED":
        return { background: "#f1f5f9", color: "#475569", border: "1px solid #cbd5e1" };
      case "PLANNED":
      default:
        return { background: "#eff6ff", color: "#2563eb", border: "1px solid #bfdbfe" };
    }
  };

  // Filtered trips
  const filteredTrips = trips.filter((trip) => {
    const destName = getDestinationName(trip).toLowerCase();
    const matchesSearch = destName.includes(searchQuery.toLowerCase().trim());
    const matchesStatus =
      statusFilter === "ALL" ||
      (trip.status && trip.status.toUpperCase() === statusFilter);

    return matchesSearch && matchesStatus;
  });

  return (
    <div style={styles.page}>
      <Navbar activePage="/trips" />

      <main style={styles.container}>
        {/* Top Header */}
        <div style={styles.headerRow}>
          <div>
            <span style={styles.pageBadge}>🧳 TRIP COLLECTION</span>
            <h1 style={styles.pageTitle}>My Trips</h1>
            <p style={styles.pageSubtitle}>
              Explore, manage, and collaborate on your planned and past travel itineraries.
            </p>
          </div>

          <Link to="/trips/create" style={styles.createTripBtn}>
            <span>＋</span>
            <span>Plan New Trip</span>
          </Link>
        </div>

        {/* Filter / Search Bar */}
        <div style={styles.filterBar}>
          <div style={styles.searchWrapper}>
            <span style={styles.searchIcon}>🔍</span>
            <input
              type="text"
              placeholder="Search by destination..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={styles.searchInput}
            />
          </div>

          <div style={styles.statusButtonGroup}>
            {["ALL", "PLANNED", "ACTIVE", "COMPLETED"].map((status) => (
              <button
                key={status}
                onClick={() => setStatusFilter(status)}
                style={{
                  ...styles.statusTab,
                  ...(statusFilter === status ? styles.statusTabActive : {}),
                }}
              >
                {status.charAt(0) + status.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </div>

        {/* Error Alert */}
        {error && (
          <div style={styles.errorBox}>
            <span>⚠️ {error}</span>
            <button style={styles.retryBtn} onClick={fetchTrips}>
              Retry
            </button>
          </div>
        )}

        {/* Content */}
        {loading ? (
          <div style={styles.grid}>
            {[1, 2, 3, 4].map((n) => (
              <div key={n} style={styles.skeletonCard}>
                <div style={{ height: "180px", background: "#f1f5f9", borderRadius: "12px" }} />
                <div style={{ height: "22px", background: "#f1f5f9", borderRadius: "4px", width: "65%", marginTop: "16px" }} />
                <div style={{ height: "14px", background: "#f1f5f9", borderRadius: "4px", width: "45%", marginTop: "10px" }} />
              </div>
            ))}
          </div>
        ) : filteredTrips.length === 0 ? (
          <div style={styles.emptyState}>
            <div style={styles.emptyIcon}>✈️</div>
            <h2 style={styles.emptyTitle}>
              {searchQuery || statusFilter !== "ALL"
                ? "No matching trips found"
                : "No trips created yet"}
            </h2>
            <p style={styles.emptyDesc}>
              {searchQuery || statusFilter !== "ALL"
                ? "Try adjusting your search terms or filters to find what you are looking for."
                : "Start organizing your upcoming adventures, day-by-day itineraries, and group expenses."}
            </p>
            <Link to="/trips/create" style={styles.emptyActionBtn}>
              ＋ Create a Trip
            </Link>
          </div>
        ) : (
          <div style={styles.grid}>
            {filteredTrips.map((trip) => {
              const destName = getDestinationName(trip);
              const imageUrl = "/images/tripImage.png";

              return (
                <div key={trip.id} style={styles.card} className="trip-card-hover">
                  <div style={styles.imageBox}>
                    <img src={imageUrl} alt={destName} style={styles.image} />
                    <span
                      style={{
                        ...styles.statusBadge,
                        ...getStatusBadgeStyle(trip.status),
                      }}
                    >
                      {trip.status || "PLANNED"}
                    </span>
                  </div>

                  <div style={styles.cardContent}>
                    <h3 style={styles.cardTitle}>{destName}</h3>

                    <div style={styles.metaRow}>
                      <span style={styles.metaIcon}>📅</span>
                      <span>
                        {formatDate(trip.startDate)} → {formatDate(trip.endDate)}
                      </span>
                    </div>

                    <div style={styles.infoPillsRow}>
                      <div style={styles.pill}>
                        <span style={styles.pillLabel}>Travelers</span>
                        <strong>{trip.travelers || 1}</strong>
                      </div>

                      <div style={styles.pill}>
                        <span style={styles.pillLabel}>Budget</span>
                        <strong>
                          {trip.budget ? `₹${Number(trip.budget).toLocaleString()}` : "Not Set"}
                        </strong>
                      </div>
                    </div>

                    <div style={styles.cardActions}>
                      <button
                        style={styles.viewBtn}
                        onClick={() => navigate(`/trips/${trip.id}`)}
                      >
                        View Details →
                      </button>

                      <button
                        style={styles.editBtn}
                        onClick={() => navigate(`/trips/${trip.id}/edit`)}
                        title="Edit Trip Settings"
                      >
                        ✏️ Edit
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
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
    padding: "36px 0 60px",
  },

  headerRow: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-end",
    marginBottom: "28px",
    flexWrap: "wrap",
    gap: "18px",
  },

  pageBadge: {
    display: "inline-block",
    color: "#0284c7",
    fontWeight: "800",
    fontSize: "12px",
    letterSpacing: "1.2px",
    marginBottom: "6px",
  },

  pageTitle: {
    margin: "0 0 6px",
    fontSize: "32px",
    fontWeight: "800",
    letterSpacing: "-0.5px",
    color: "#0f172a",
  },

  pageSubtitle: {
    margin: 0,
    fontSize: "15px",
    color: "#64748b",
  },

  createTripBtn: {
    display: "inline-flex",
    alignItems: "center",
    gap: "8px",
    background: "#0284c7",
    color: "#ffffff",
    padding: "12px 24px",
    borderRadius: "10px",
    fontWeight: "700",
    fontSize: "14px",
    textDecoration: "none",
    boxShadow: "0 4px 12px rgba(2, 132, 199, 0.25)",
    transition: "transform 0.15s, background 0.15s",
  },

  filterBar: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "16px",
    marginBottom: "32px",
    flexWrap: "wrap",
  },

  searchWrapper: {
    display: "flex",
    alignItems: "center",
    background: "#ffffff",
    border: "1px solid #cbd5e1",
    borderRadius: "10px",
    padding: "0 14px",
    width: "320px",
    maxWidth: "100%",
  },

  searchIcon: {
    fontSize: "14px",
    color: "#94a3b8",
    marginRight: "8px",
  },

  searchInput: {
    border: "none",
    outline: "none",
    padding: "11px 0",
    width: "100%",
    fontSize: "14px",
    background: "transparent",
  },

  statusButtonGroup: {
    display: "flex",
    gap: "6px",
    background: "#ffffff",
    padding: "4px",
    borderRadius: "10px",
    border: "1px solid #e2e8f0",
  },

  statusTab: {
    padding: "8px 14px",
    borderRadius: "8px",
    border: "none",
    background: "transparent",
    color: "#64748b",
    fontSize: "13px",
    fontWeight: "600",
    cursor: "pointer",
    transition: "all 0.15s",
  },

  statusTabActive: {
    background: "#0284c7",
    color: "#ffffff",
    fontWeight: "700",
  },

  errorBox: {
    background: "#fef2f2",
    border: "1px solid #fecaca",
    color: "#b91c1c",
    padding: "14px 18px",
    borderRadius: "12px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "24px",
  },

  retryBtn: {
    background: "#b91c1c",
    color: "#ffffff",
    padding: "6px 14px",
    borderRadius: "6px",
    fontSize: "12px",
    fontWeight: "600",
  },

  grid: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))",
    gap: "24px",
  },

  card: {
    background: "#ffffff",
    borderRadius: "16px",
    border: "1px solid #e2e8f0",
    overflow: "hidden",
    boxShadow: "0 2px 8px rgba(0,0,0,0.02)",
    display: "flex",
    flexDirection: "column",
  },

  imageBox: {
    position: "relative",
    height: "180px",
    width: "100%",
  },

  image: {
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

  cardContent: {
    padding: "20px",
    display: "flex",
    flexDirection: "column",
    flex: 1,
    justifyContent: "space-between",
  },

  cardTitle: {
    margin: "0 0 8px",
    fontSize: "20px",
    fontWeight: "700",
    color: "#0f172a",
  },

  metaRow: {
    display: "flex",
    alignItems: "center",
    gap: "6px",
    fontSize: "13px",
    color: "#64748b",
    marginBottom: "16px",
  },

  metaIcon: {
    fontSize: "14px",
  },

  infoPillsRow: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: "10px",
    marginBottom: "18px",
  },

  pill: {
    background: "#f8fafc",
    border: "1px solid #e2e8f0",
    borderRadius: "10px",
    padding: "10px 12px",
    display: "flex",
    flexDirection: "column",
    gap: "2px",
  },

  pillLabel: {
    fontSize: "11px",
    color: "#94a3b8",
    fontWeight: "600",
  },

  cardActions: {
    display: "flex",
    gap: "10px",
    paddingTop: "14px",
    borderTop: "1px solid #f1f5f9",
  },

  viewBtn: {
    flex: 1,
    background: "linear-gradient(135deg, #0284c7 0%, #0369a1 100%)",
    color: "#ffffff",
    border: "none",
    padding: "10px 16px",
    borderRadius: "9px",
    fontSize: "13px",
    fontWeight: "700",
    cursor: "pointer",
    boxShadow: "0 2px 6px rgba(2, 132, 199, 0.2)",
  },

  editBtn: {
    background: "#ffffff",
    color: "#475569",
    border: "1px solid #cbd5e1",
    padding: "10px 14px",
    borderRadius: "9px",
    fontSize: "13px",
    fontWeight: "600",
    cursor: "pointer",
  },

  emptyState: {
    background: "#ffffff",
    border: "2px dashed #cbd5e1",
    borderRadius: "20px",
    padding: "60px 24px",
    textAlign: "center",
    maxWidth: "540px",
    margin: "40px auto 0",
  },

  emptyIcon: {
    fontSize: "52px",
    marginBottom: "16px",
  },

  emptyTitle: {
    margin: "0 0 8px",
    fontSize: "22px",
    fontWeight: "700",
    color: "#0f172a",
  },

  emptyDesc: {
    fontSize: "15px",
    color: "#64748b",
    marginBottom: "24px",
    lineHeight: "1.5",
  },

  emptyActionBtn: {
    display: "inline-block",
    background: "#0284c7",
    color: "#ffffff",
    padding: "12px 26px",
    borderRadius: "10px",
    fontWeight: "700",
    fontSize: "14px",
    textDecoration: "none",
    boxShadow: "0 4px 12px rgba(2, 132, 199, 0.25)",
  },

  skeletonCard: {
    background: "#ffffff",
    border: "1px solid #e2e8f0",
    borderRadius: "16px",
    padding: "16px",
  },
};

export default Trips;