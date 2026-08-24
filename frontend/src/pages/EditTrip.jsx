import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";

function EditTrip() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [trip, setTrip] = useState(null);

  const [formData, setFormData] = useState({
    startDate: "",
    endDate: "",
    travelers: "",
    budget: "",
    status: "PLANNED",
  });

  const getAuthConfig = () => {
    const token = localStorage.getItem("token");

    return {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    };
  };

  // ================= FETCH TRIP =================

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    fetchTrip();
  }, [id]);

  const fetchTrip = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await axios.get(
        `http://localhost:8080/api/trips/${id}`,
        getAuthConfig()
      );

      console.log("EDIT TRIP DATA:", response.data);

      const data = response.data;

      setTrip(data);

      setFormData({
        startDate: data.startDate || "",
        endDate: data.endDate || "",
        travelers: data.travelers ?? "",
        budget: data.budget ?? "",
        status: data.status || "PLANNED",
      });
    } catch (err) {
      console.error("Error fetching trip:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError(
        err.response?.data?.message ||
          "Unable to load trip details."
      );
    } finally {
      setLoading(false);
    }
  };

  // ================= INPUT CHANGE =================

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // ================= UPDATE TRIP =================

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.startDate) {
      alert("Please select start date.");
      return;
    }

    if (!formData.endDate) {
      alert("Please select end date.");
      return;
    }

    if (formData.travelers === "") {
      alert("Please enter number of travelers.");
      return;
    }

    if (formData.budget === "") {
      alert("Please enter budget.");
      return;
    }

    if (new Date(formData.endDate) < new Date(formData.startDate)) {
      alert("End date cannot be before start date.");
      return;
    }

    try {
      setSaving(true);

      const payload = {
        destination: trip.destination,

        startDate: formData.startDate,
        endDate: formData.endDate,

        travelers: Number(formData.travelers),

        budget: Number(formData.budget),

        status: formData.status,
      };

      console.log("UPDATING TRIP:", payload);

      await axios.put(
        `http://localhost:8080/api/trips/${id}`,
        payload,
        getAuthConfig()
      );

      alert("Trip updated successfully! ✅");

      navigate(`/trips/${id}`);
    } catch (err) {
      console.error("Error updating trip:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      alert(
        err.response?.data?.message ||
          "Unable to update trip. Please try again."
      );
    } finally {
      setSaving(false);
    }
  };

  // ================= LOADING =================

  if (loading) {
    return (
      <div style={styles.center}>
        <div style={styles.loadingIcon}>✈️</div>
        <h2>Loading trip...</h2>
        <p style={styles.muted}>
          Please wait a moment.
        </p>
      </div>
    );
  }

  // ================= ERROR =================

  if (error || !trip) {
    return (
      <div style={styles.center}>
        <div style={styles.errorIcon}>⚠️</div>

        <h2>Unable to load trip</h2>

        <p style={styles.error}>
          {error || "Trip not found."}
        </p>

        <button
          style={styles.primaryButton}
          onClick={() => navigate("/trips")}
        >
          ← Back to My Trips
        </button>
      </div>
    );
  }

  const destinationName =
    trip.destination?.name ||
    trip.destination?.destinationName ||
    "Unknown Destination";

  return (
    <div style={styles.page}>

      {/* ================= HEADER ================= */}

      <header style={styles.header}>
        <div>
          <h1 style={styles.logo}>TripNest</h1>

          <p style={styles.headerSubtitle}>
            Edit Trip
          </p>
        </div>

        <div style={styles.headerButtons}>
          <button
            style={styles.secondaryButton}
            onClick={() => navigate(`/trips/${id}`)}
          >
            ← Trip Details
          </button>

          <button
            style={styles.secondaryButton}
            onClick={() => navigate("/trips")}
          >
            🧳 My Trips
          </button>
        </div>
      </header>

      {/* ================= MAIN ================= */}

      <main style={styles.container}>

        <div style={styles.card}>

          <div style={styles.titleSection}>
            <div style={styles.icon}>
              ✈️
            </div>

            <div>
              <h2 style={styles.title}>
                Edit Trip
              </h2>

              <p style={styles.subtitle}>
                Update your trip information
              </p>
            </div>
          </div>

          <div style={styles.divider}></div>

          {/* DESTINATION */}

          <div style={styles.destinationBox}>
            <span style={styles.label}>
              📍 Destination
            </span>

            <strong style={styles.destination}>
              {destinationName}
            </strong>

            <small style={styles.muted}>
              Destination cannot be changed from this page.
            </small>
          </div>

          {/* FORM */}

          <form onSubmit={handleSubmit}>

            {/* DATE ROW */}

            <div style={styles.formRow}>

              <div style={styles.formGroup}>
                <label style={styles.label}>
                  📅 Start Date
                </label>

                <input
                  type="date"
                  name="startDate"
                  value={formData.startDate}
                  onChange={handleChange}
                  style={styles.input}
                  required
                />
              </div>

              <div style={styles.formGroup}>
                <label style={styles.label}>
                  📅 End Date
                </label>

                <input
                  type="date"
                  name="endDate"
                  value={formData.endDate}
                  onChange={handleChange}
                  style={styles.input}
                  required
                />
              </div>

            </div>

            {/* TRAVELERS + BUDGET */}

            <div style={styles.formRow}>

              <div style={styles.formGroup}>
                <label style={styles.label}>
                  👥 Travelers
                </label>

                <input
                  type="number"
                  name="travelers"
                  min="1"
                  value={formData.travelers}
                  onChange={handleChange}
                  style={styles.input}
                  placeholder="Number of travelers"
                  required
                />
              </div>

              <div style={styles.formGroup}>
                <label style={styles.label}>
                  💰 Budget
                </label>

                <input
                  type="number"
                  name="budget"
                  min="0"
                  value={formData.budget}
                  onChange={handleChange}
                  style={styles.input}
                  placeholder="Enter budget"
                  required
                />
              </div>

            </div>

            {/* STATUS */}

            <div style={styles.formGroup}>
              <label style={styles.label}>
                📌 Status
              </label>

              <select
                name="status"
                value={formData.status}
                onChange={handleChange}
                style={styles.input}
              >
                <option value="PLANNED">
                  PLANNED
                </option>

                <option value="ONGOING">
                  ONGOING
                </option>

                <option value="COMPLETED">
                  COMPLETED
                </option>

                <option value="CANCELLED">
                  CANCELLED
                </option>
              </select>
            </div>

            {/* BUTTONS */}

            <div style={styles.actions}>

              <button
                type="button"
                style={styles.cancelButton}
                onClick={() => navigate(`/trips/${id}`)}
                disabled={saving}
              >
                Cancel
              </button>

              <button
                type="submit"
                style={styles.saveButton}
                disabled={saving}
              >
                {saving
                  ? "Saving..."
                  : "✓ Save Changes"}
              </button>

            </div>

          </form>

        </div>

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
    flexWrap: "wrap",
  },

  logo: {
    margin: 0,
    fontSize: "30px",
    fontWeight: "700",
  },

  headerSubtitle: {
    margin: "5px 0 0",
    color: "#6b7280",
  },

  headerButtons: {
    display: "flex",
    gap: "10px",
    flexWrap: "wrap",
  },

  secondaryButton: {
    border: "1px solid #d1d5db",
    background: "#ffffff",
    color: "#111827",
    padding: "10px 17px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  container: {
    width: "90%",
    maxWidth: "850px",
    margin: "0 auto",
    padding: "45px 0 60px",
  },

  card: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "16px",
    padding: "30px",
    boxShadow: "0 5px 20px rgba(0,0,0,0.05)",
  },

  titleSection: {
    display: "flex",
    alignItems: "center",
    gap: "18px",
  },

  icon: {
    width: "60px",
    height: "60px",
    borderRadius: "14px",
    background: "#eef2ff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "30px",
  },

  title: {
    margin: 0,
    fontSize: "28px",
  },

  subtitle: {
    margin: "6px 0 0",
    color: "#6b7280",
  },

  divider: {
    height: "1px",
    background: "#e5e7eb",
    margin: "28px 0",
  },

  destinationBox: {
    background: "#f9fafb",
    border: "1px solid #e5e7eb",
    borderRadius: "12px",
    padding: "18px",
    marginBottom: "25px",
    display: "flex",
    flexDirection: "column",
    gap: "7px",
  },

  destination: {
    fontSize: "19px",
  },

  formRow: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: "18px",
  },

  formGroup: {
    marginBottom: "20px",
  },

  label: {
    display: "block",
    marginBottom: "8px",
    color: "#374151",
    fontSize: "14px",
    fontWeight: "600",
  },

  input: {
    width: "100%",
    boxSizing: "border-box",
    border: "1px solid #d1d5db",
    borderRadius: "9px",
    padding: "12px 13px",
    fontSize: "14px",
    outline: "none",
    background: "#ffffff",
  },

  actions: {
    display: "flex",
    justifyContent: "flex-end",
    gap: "12px",
    marginTop: "10px",
    flexWrap: "wrap",
  },

  cancelButton: {
    border: "1px solid #d1d5db",
    background: "#ffffff",
    color: "#374151",
    padding: "12px 20px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  saveButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "12px 22px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  primaryButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  center: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    flexDirection: "column",
    gap: "10px",
    padding: "20px",
  },

  loadingIcon: {
    fontSize: "45px",
  },

  errorIcon: {
    fontSize: "45px",
  },

  error: {
    color: "#b91c1c",
    textAlign: "center",
  },

  muted: {
    color: "#6b7280",
    fontSize: "13px",
  },
};

export default EditTrip;