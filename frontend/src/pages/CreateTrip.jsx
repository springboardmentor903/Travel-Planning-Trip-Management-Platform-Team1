import React, { useEffect, useRef, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function CreateTrip() {
  const navigate = useNavigate();

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  // Destination search states
  const [destinationSearch, setDestinationSearch] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [searchingDestinations, setSearchingDestinations] = useState(false);
  const [selectedDestination, setSelectedDestination] = useState(null);

  const searchTimeoutRef = useRef(null);

  const [form, setForm] = useState({
    startDate: "",
    endDate: "",
    travelers: 1,
    budget: "",
    status: "PLANNED",
  });

  // ==========================================
  // AUTH CONFIG
  // ==========================================

  const getAuthConfig = () => {
    const token = localStorage.getItem("token");

    return {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    };
  };

  // ==========================================
  // CHECK LOGIN
  // ==========================================

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
    }
  }, [navigate]);

  // ==========================================
  // SEARCH GOOGLE PLACES
  // ==========================================

  const searchDestinations = (query) => {
    setDestinationSearch(query);
    setSelectedDestination(null);

    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    if (!query.trim()) {
      setSuggestions([]);
      return;
    }

    searchTimeoutRef.current = setTimeout(async () => {
      try {
        setSearchingDestinations(true);
        setError("");

        const response = await axios.get(
          "http://localhost:8080/api/destinations/search",
          {
            params: {
              query: query.trim(),
            },
            ...getAuthConfig(),
          }
        );

        console.log("GOOGLE PLACES RESPONSE:", response.data);

        const places = response.data?.places || [];

        setSuggestions(places);
      } catch (err) {
        console.error("Destination search error:", err);

        if (err.response?.status === 401) {
          localStorage.removeItem("token");
          navigate("/login");
          return;
        }

        setSuggestions([]);
        setError("Unable to search destinations.");
      } finally {
        setSearchingDestinations(false);
      }
    }, 400);
  };

  // ==========================================
  // SELECT DESTINATION
  // ==========================================

  const handleSelectDestination = (place) => {
    const name =
      place?.displayName?.text ||
      place?.displayName ||
      "";

    const address =
      place?.formattedAddress ||
      "";

    setSelectedDestination({
      name,
      address,
      googlePlaceId: place?.id || "",
    });

    setDestinationSearch(name);
    setSuggestions([]);
    setError("");
  };

  // ==========================================
  // FORM CHANGE
  // ==========================================

  const handleChange = (e) => {
    const { name, value } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // ==========================================
  // CREATE TRIP
  // ==========================================

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");

    // Destination validation
    if (!selectedDestination) {
      setError("Please select a destination from the suggestions.");
      return;
    }

    if (!form.startDate || !form.endDate) {
      setError("Please select start and end dates.");
      return;
    }

    if (form.endDate < form.startDate) {
      setError("End date cannot be before start date.");
      return;
    }

    if (!form.travelers || Number(form.travelers) < 1) {
      setError("Travelers must be at least 1.");
      return;
    }

    if (!form.budget || Number(form.budget) < 0) {
      setError("Please enter a valid budget.");
      return;
    }

    try {
      setSaving(true);

      // ==========================================
      // STEP 1:
      // CREATE OR GET DESTINATION IN DATABASE
      // ==========================================

      console.log(
        "SELECTED DESTINATION:",
        selectedDestination
      );

      const destinationResponse = await axios.post(
        "http://localhost:8080/api/destinations/create-or-get",
        {
          name: selectedDestination.name,
        },
        getAuthConfig()
      );

      const destination = destinationResponse.data;

      console.log(
        "DATABASE DESTINATION:",
        destination
      );

      // ==========================================
      // STEP 2:
      // CREATE TRIP
      // ==========================================

      const tripData = {
        destination: {
          id: destination.id,
        },
        startDate: form.startDate,
        endDate: form.endDate,
        travelers: Number(form.travelers),
        budget: Number(form.budget),
        status: form.status,
      };

      console.log("CREATING TRIP:", tripData);

      await axios.post(
        "http://localhost:8080/api/trips",
        tripData,
        getAuthConfig()
      );

      alert("Trip created successfully!");

      navigate("/trips");

    } catch (err) {
      console.error("Error creating trip:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError(
        err.response?.data?.message ||
          err.response?.data ||
          "Unable to create trip. Please try again."
      );
    } finally {
      setSaving(false);
    }
  };

  // ==========================================
  // UI
  // ==========================================

  return (
    <div style={styles.page}>

      {/* Header */}
      <header style={styles.header}>
        <div>
          <h1 style={styles.logo}>
            TripNest
          </h1>

          <p style={styles.subtitle}>
            Create a new trip
          </p>
        </div>

        <button
          style={styles.backButton}
          onClick={() => navigate("/trips")}
        >
          ← My Trips
        </button>
      </header>

      {/* Main */}
      <main style={styles.container}>

        <div style={styles.card}>

          <h2 style={styles.title}>
            Plan Your Trip 🧳
          </h2>

          <p style={styles.description}>
            Enter your trip details below.
          </p>

          {/* Error */}
          {error && (
            <div style={styles.error}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>

            {/* ==========================================
                DESTINATION
            ========================================== */}

            <div style={styles.formGroup}>

              <label style={styles.label}>
                Destination
              </label>

              <div style={styles.searchContainer}>

                <input
                  type="text"
                  value={destinationSearch}
                  onChange={(e) =>
                    searchDestinations(e.target.value)
                  }
                  placeholder="Search any city, place, hotel..."
                  style={styles.input}
                  autoComplete="off"
                />

                {searchingDestinations && (
                  <div style={styles.loadingText}>
                    Searching...
                  </div>
                )}

                {/* Suggestions */}
                {suggestions.length > 0 && (
                  <div style={styles.suggestionsBox}>

                    {suggestions.map((place, index) => {

                      const name =
                        place?.displayName?.text ||
                        place?.displayName ||
                        "Unknown place";

                      const address =
                        place?.formattedAddress ||
                        "";

                      return (
                        <div
                          key={
                            place?.id ||
                            `${name}-${index}`
                          }
                          style={styles.suggestionItem}
                          onClick={() =>
                            handleSelectDestination(place)
                          }
                        >

                          <div style={styles.placeName}>
                            📍 {name}
                          </div>

                          {address && (
                            <div style={styles.placeAddress}>
                              {address}
                            </div>
                          )}

                        </div>
                      );
                    })}

                  </div>
                )}

              </div>

              {/* Selected destination */}
              {selectedDestination && (
                <div style={styles.selectedDestination}>

                  <strong>
                    ✓ Selected:
                  </strong>{" "}

                  {selectedDestination.name}

                  {selectedDestination.address && (
                    <div style={styles.selectedAddress}>
                      {selectedDestination.address}
                    </div>
                  )}

                </div>
              )}

            </div>

            {/* ==========================================
                DATES
            ========================================== */}

            <div style={styles.row}>

              <div style={styles.formGroup}>

                <label style={styles.label}>
                  Start Date
                </label>

                <input
                  type="date"
                  name="startDate"
                  value={form.startDate}
                  onChange={handleChange}
                  style={styles.input}
                  required
                />

              </div>

              <div style={styles.formGroup}>

                <label style={styles.label}>
                  End Date
                </label>

                <input
                  type="date"
                  name="endDate"
                  value={form.endDate}
                  onChange={handleChange}
                  style={styles.input}
                  required
                />

              </div>

            </div>

            {/* ==========================================
                TRAVELERS + BUDGET
            ========================================== */}

            <div style={styles.row}>

              <div style={styles.formGroup}>

                <label style={styles.label}>
                  Travelers
                </label>

                <input
                  type="number"
                  name="travelers"
                  min="1"
                  value={form.travelers}
                  onChange={handleChange}
                  style={styles.input}
                  required
                />

              </div>

              <div style={styles.formGroup}>

                <label style={styles.label}>
                  Budget (₹)
                </label>

                <input
                  type="number"
                  name="budget"
                  min="0"
                  step="0.01"
                  value={form.budget}
                  onChange={handleChange}
                  placeholder="Enter budget"
                  style={styles.input}
                  required
                />

              </div>

            </div>

            {/* ==========================================
                STATUS
            ========================================== */}

            <div style={styles.formGroup}>

              <label style={styles.label}>
                Status
              </label>

              <select
                name="status"
                value={form.status}
                onChange={handleChange}
                style={styles.input}
              >

                <option value="PLANNED">
                  Planned
                </option>

                <option value="ONGOING">
                  Ongoing
                </option>

                <option value="COMPLETED">
                  Completed
                </option>

              </select>

            </div>

            {/* ==========================================
                BUTTONS
            ========================================== */}

            <div style={styles.actions}>

              <button
                type="button"
                style={styles.cancelButton}
                onClick={() => navigate("/trips")}
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
                  ? "Creating..."
                  : "Create Trip"}
              </button>

            </div>

          </form>

        </div>

      </main>

    </div>
  );
}

// ==========================================
// STYLES
// ==========================================

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
    color: "#111827",
    padding: "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  container: {
    width: "100%",
    maxWidth: "850px",
    margin: "0 auto",
    padding: "45px 25px",
    boxSizing: "border-box",
  },

  card: {
    background: "#ffffff",
    borderRadius: "18px",
    padding: "35px",
    boxShadow: "0 8px 30px rgba(0, 0, 0, 0.07)",
    border: "1px solid #e5e7eb",
  },

  title: {
    margin: 0,
    fontSize: "28px",
  },

  description: {
    color: "#6b7280",
    margin: "8px 0 30px",
  },

  formGroup: {
    marginBottom: "20px",
    flex: 1,
  },

  row: {
    display: "flex",
    gap: "18px",
  },

  label: {
    display: "block",
    marginBottom: "8px",
    fontSize: "14px",
    fontWeight: "600",
    color: "#374151",
  },

  searchContainer: {
    position: "relative",
    width: "100%",
  },

  input: {
    width: "100%",
    boxSizing: "border-box",
    padding: "12px 13px",
    border: "1px solid #d1d5db",
    borderRadius: "9px",
    background: "#ffffff",
    color: "#111827",
    fontSize: "15px",
    outline: "none",
  },

  loadingText: {
    position: "absolute",
    right: "12px",
    top: "12px",
    color: "#6b7280",
    fontSize: "13px",
    background: "#ffffff",
    paddingLeft: "5px",
  },

  suggestionsBox: {
    position: "absolute",
    top: "100%",
    left: 0,
    right: 0,
    background: "#ffffff",
    border: "1px solid #d1d5db",
    borderRadius: "0 0 10px 10px",
    boxShadow: "0 8px 20px rgba(0,0,0,0.12)",
    zIndex: 1000,
    maxHeight: "320px",
    overflowY: "auto",
  },

  suggestionItem: {
    padding: "13px 15px",
    cursor: "pointer",
    borderBottom: "1px solid #f0f0f0",
    background: "#ffffff",
  },

  placeName: {
    fontSize: "15px",
    fontWeight: "600",
    color: "#111827",
  },

  placeAddress: {
    fontSize: "13px",
    color: "#6b7280",
    marginTop: "4px",
    paddingLeft: "22px",
  },

  selectedDestination: {
    marginTop: "10px",
    padding: "11px 13px",
    borderRadius: "8px",
    background: "#ecfdf5",
    color: "#065f46",
    fontSize: "14px",
  },

  selectedAddress: {
    marginTop: "4px",
    color: "#047857",
    fontSize: "13px",
  },

  error: {
    background: "#fee2e2",
    color: "#991b1b",
    padding: "13px 15px",
    borderRadius: "8px",
    marginBottom: "20px",
    fontSize: "14px",
  },

  actions: {
    display: "flex",
    justifyContent: "flex-end",
    gap: "12px",
    marginTop: "30px",
  },

  cancelButton: {
    border: "1px solid #d1d5db",
    background: "#ffffff",
    color: "#111827",
    padding: "12px 22px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  saveButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "12px 24px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },
};

export default CreateTrip;