import React, { useState } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";

function CreateItinerary() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    dayNumber: "",
    date: "",
    title: "",
    description: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const getAuthConfig = () => {
    const token = localStorage.getItem("token");

    return {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    };
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");

    if (!formData.dayNumber) {
      setError("Please enter day number.");
      return;
    }

    if (!formData.title.trim()) {
      setError("Please enter itinerary title.");
      return;
    }

    try {
      setLoading(true);

      const payload = {
        dayNumber: Number(formData.dayNumber),
        date: formData.date || null,
        title: formData.title.trim(),
        description: formData.description.trim(),
      };

      console.log("Creating itinerary:", payload);

      await axios.post(
        `http://localhost:8080/api/trips/${id}/itineraries`,
        payload,
        getAuthConfig()
      );

      alert("Itinerary created successfully.");

      navigate(`/trips/${id}`);
    } catch (err) {
      console.error("Error creating itinerary:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError(
        err.response?.data?.message ||
          "Unable to create itinerary."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.page}>

      {/* HEADER */}
      <header style={styles.header}>

        <div>
          <h1 style={styles.logo}>TripNest</h1>
          <p style={styles.subtitle}>
            Create Itinerary
          </p>
        </div>

        <button
          style={styles.backButton}
          onClick={() => navigate(`/trips/${id}`)}
        >
          ← Back to Trip
        </button>

      </header>

      {/* MAIN */}
      <main style={styles.container}>

        <div style={styles.card}>

          <h2 style={styles.title}>
            Create New Itinerary
          </h2>

          <p style={styles.description}>
            Add a day to your trip and plan your activities.
          </p>

          {error && (
            <div style={styles.error}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>

            {/* DAY NUMBER */}
            <div style={styles.formGroup}>

              <label style={styles.label}>
                Day Number *
              </label>

              <input
                type="number"
                name="dayNumber"
                min="1"
                value={formData.dayNumber}
                onChange={handleChange}
                placeholder="Example: 1"
                style={styles.input}
              />

            </div>

            {/* DATE */}
            <div style={styles.formGroup}>

              <label style={styles.label}>
                Date
              </label>

              <input
                type="date"
                name="date"
                value={formData.date}
                onChange={handleChange}
                style={styles.input}
              />

            </div>

            {/* TITLE */}
            <div style={styles.formGroup}>

              <label style={styles.label}>
                Title *
              </label>

              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="Example: Explore Delhi"
                style={styles.input}
              />

            </div>

            {/* DESCRIPTION */}
            <div style={styles.formGroup}>

              <label style={styles.label}>
                Description
              </label>

              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Describe your plan for this day..."
                rows="5"
                style={styles.textarea}
              />

            </div>

            {/* BUTTONS */}
            <div style={styles.actions}>

              <button
                type="button"
                style={styles.cancelButton}
                onClick={() => navigate(`/trips/${id}`)}
              >
                Cancel
              </button>

              <button
                type="submit"
                style={styles.submitButton}
                disabled={loading}
              >
                {loading
                  ? "Creating..."
                  : "Create Itinerary"}
              </button>

            </div>

          </form>

        </div>

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

  backButton: {
    border: "1px solid #d1d5db",
    background: "#ffffff",
    padding: "10px 17px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  container: {
    width: "90%",
    maxWidth: "700px",
    margin: "0 auto",
    padding: "40px 0 60px",
  },

  card: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "16px",
    padding: "30px",
    boxShadow: "0 5px 20px rgba(0,0,0,0.05)",
  },

  title: {
    margin: 0,
    fontSize: "28px",
  },

  description: {
    color: "#6b7280",
    marginTop: "8px",
    marginBottom: "28px",
  },

  error: {
    background: "#fee2e2",
    color: "#991b1b",
    padding: "12px 15px",
    borderRadius: "8px",
    marginBottom: "20px",
  },

  formGroup: {
    marginBottom: "20px",
  },

  label: {
    display: "block",
    marginBottom: "8px",
    fontWeight: "600",
    fontSize: "14px",
  },

  input: {
    width: "100%",
    boxSizing: "border-box",
    padding: "12px 14px",
    border: "1px solid #d1d5db",
    borderRadius: "8px",
    fontSize: "15px",
    outline: "none",
  },

  textarea: {
    width: "100%",
    boxSizing: "border-box",
    padding: "12px 14px",
    border: "1px solid #d1d5db",
    borderRadius: "8px",
    fontSize: "15px",
    resize: "vertical",
    outline: "none",
    fontFamily: "inherit",
  },

  actions: {
    display: "flex",
    justifyContent: "flex-end",
    gap: "10px",
    marginTop: "25px",
  },

  cancelButton: {
    border: "1px solid #d1d5db",
    background: "#ffffff",
    padding: "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  submitButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "11px 20px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },
};

export default CreateItinerary;