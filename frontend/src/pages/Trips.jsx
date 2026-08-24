import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function Trips() {
  const navigate = useNavigate();

  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchTrips();
  }, []);

  const fetchTrips = async () => {
    try {
      const token = localStorage.getItem("token");

      if (!token) {
        navigate("/login");
        return;
      }

      const response = await axios.get(
        "http://localhost:8080/api/trips/my",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      console.log("TRIPS RESPONSE:", response.data);

      setTrips(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Error fetching trips:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError("Unable to load your trips.");
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (date) => {
    if (!date) return "Not available";

    return new Date(date).toLocaleDateString("en-IN", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  };

  const getDestinationName = (trip) => {
    if (trip?.destination?.name) {
      return trip.destination.name;
    }

    return "Unknown destination";
  };

  if (loading) {
    return (
      <div style={styles.center}>
        <h2>Loading your trips...</h2>
      </div>
    );
  }

  return (
    <div style={styles.page}>
      {/* Header */}
      <header style={styles.header}>
        <div>
          <h1 style={styles.logo}>TripNest</h1>
          <p style={styles.subtitle}>My Trips</p>
        </div>

        <div style={styles.headerButtons}>
          <button
            style={styles.dashboardButton}
            onClick={() => navigate("/dashboard")}
          >
            Dashboard
          </button>

          <button
            style={styles.createButton}
            onClick={() => navigate("/trips/create")}
          >
            + Create Trip
          </button>
        </div>
      </header>

      <main style={styles.container}>
        {/* Page heading */}
        <div style={styles.pageHeading}>
          <div>
            <h2 style={styles.title}>Your Trips 🧳</h2>
            <p style={styles.description}>
              Manage and view all your planned trips.
            </p>
          </div>

          <div style={styles.tripCount}>
            {trips.length} {trips.length === 1 ? "Trip" : "Trips"}
          </div>
        </div>

        {/* Error */}
        {error && (
          <div style={styles.errorBox}>
            {error}

            <button
              style={styles.retryButton}
              onClick={fetchTrips}
            >
              Try Again
            </button>
          </div>
        )}

        {/* Empty state */}
        {!error && trips.length === 0 && (
          <div style={styles.emptyCard}>
            <div style={styles.emptyIcon}>🧳</div>

            <h2>No trips yet</h2>

            <p>
              You haven't created any trips yet. Start planning your
              next adventure!
            </p>

            <button
              style={styles.createButton}
              onClick={() => navigate("/trips/create")}
            >
              + Create Your First Trip
            </button>
          </div>
        )}

        {/* Trips */}
        {!error && trips.length > 0 && (
          <div style={styles.tripList}>
            {trips.map((trip) => (
              <div key={trip.id} style={styles.tripCard}>
                {/* Top */}
                <div style={styles.tripTop}>
                  <div>
                    <h3 style={styles.destination}>
                      {getDestinationName(trip)}
                    </h3>

                    <p style={styles.date}>
                      {formatDate(trip.startDate)} →{" "}
                      {formatDate(trip.endDate)}
                    </p>
                  </div>

                  <span style={styles.status}>
                    {trip.status || "PLANNED"}
                  </span>
                </div>

                {/* Details */}
                <div style={styles.details}>
                  <div style={styles.detailBox}>
                    <span style={styles.detailLabel}>
                      Travelers
                    </span>

                    <strong>
                      {trip.travelers ?? "N/A"}
                    </strong>
                  </div>

                  <div style={styles.detailBox}>
                    <span style={styles.detailLabel}>
                      Budget
                    </span>

                    <strong>
                      ₹{trip.budget ?? "N/A"}
                    </strong>
                  </div>
                </div>

                {/* Actions */}
                <div style={styles.actions}>
                  <button
                    style={styles.viewButton}
                    onClick={() =>
                      navigate(`/trips/${trip.id}`)
                    }
                  >
                    View Details
                  </button>

                  <button
                    style={styles.editButton}
                    onClick={() =>
                      navigate(`/trips/${trip.id}/edit`)
                    }
                  >
                    Edit
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    background: "#f5f7fb",
    color: "#111827",
  },

  header: {
    background: "#ffffff",
    borderBottom: "1px solid #e5e7eb",
    padding: "20px 5%",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
  },

  logo: {
    margin: 0,
    fontSize: "30px",
    fontWeight: "700",
  },

  subtitle: {
    margin: "5px 0 0",
    color: "#6b7280",
  },

  headerButtons: {
    display: "flex",
    gap: "10px",
    alignItems: "center",
  },

  dashboardButton: {
    border: "1px solid #d1d5db",
    background: "#ffffff",
    color: "#111827",
    padding: "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  createButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  container: {
    width: "100%",
    maxWidth: "1000px",
    margin: "0 auto",
    padding: "40px 25px",
  },

  pageHeading: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "25px",
    gap: "20px",
  },

  title: {
    margin: 0,
    fontSize: "30px",
  },

  description: {
    color: "#6b7280",
    marginTop: "7px",
  },

  tripCount: {
    background: "#e5e7eb",
    padding: "10px 16px",
    borderRadius: "20px",
    fontWeight: "600",
  },

  tripList: {
    display: "flex",
    flexDirection: "column",
    gap: "18px",
  },

  tripCard: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "16px",
    padding: "25px",
    boxShadow: "0 5px 20px rgba(0,0,0,0.05)",
  },

  tripTop: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "20px",
  },

  destination: {
    margin: 0,
    fontSize: "24px",
  },

  date: {
    color: "#6b7280",
    marginTop: "8px",
  },

  status: {
    background: "#e5e7eb",
    padding: "9px 14px",
    borderRadius: "20px",
    fontSize: "13px",
    fontWeight: "700",
  },

  details: {
    display: "grid",
    gridTemplateColumns: "repeat(2, 1fr)",
    gap: "15px",
    marginTop: "22px",
  },

  detailBox: {
    background: "#f9fafb",
    border: "1px solid #e5e7eb",
    borderRadius: "10px",
    padding: "15px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },

  detailLabel: {
    color: "#6b7280",
  },

  actions: {
    display: "flex",
    gap: "10px",
    marginTop: "20px",
  },

  viewButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  editButton: {
    border: "1px solid #d1d5db",
    background: "#ffffff",
    color: "#111827",
    padding: "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  emptyCard: {
    background: "#ffffff",
    borderRadius: "16px",
    padding: "60px 30px",
    textAlign: "center",
    border: "1px solid #e5e7eb",
  },

  emptyIcon: {
    fontSize: "55px",
  },

  errorBox: {
    background: "#fee2e2",
    color: "#991b1b",
    padding: "18px",
    borderRadius: "10px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "15px",
  },

  retryButton: {
    border: "none",
    background: "#991b1b",
    color: "#ffffff",
    padding: "9px 15px",
    borderRadius: "7px",
    cursor: "pointer",
  },

  center: {
    minHeight: "100vh",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
};

export default Trips;