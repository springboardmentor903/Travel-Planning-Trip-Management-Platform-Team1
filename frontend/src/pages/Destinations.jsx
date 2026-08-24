import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function Destinations() {
  const navigate = useNavigate();

  const [query, setQuery] = useState("");
  const [places, setPlaces] = useState([]);

  const [destinations, setDestinations] = useState([]);
  const [popularDestinations, setPopularDestinations] = useState([]);

  const [loading, setLoading] = useState(false);
  const [loadingDestinations, setLoadingDestinations] = useState(true);
  const [error, setError] = useState("");

  // ==========================================
  // LOAD DESTINATIONS
  // ==========================================

  useEffect(() => {
    fetchDestinations();
  }, []);

  const getAuthConfig = () => {
    const token = localStorage.getItem("token");

    return token
      ? {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      : {};
  };

  const fetchDestinations = async () => {
    try {
      setLoadingDestinations(true);
      setError("");

      const [allResponse, popularResponse] = await Promise.all([
        axios.get(
          "http://localhost:8080/api/destinations",
          getAuthConfig()
        ),

        axios.get(
          "http://localhost:8080/api/destinations/popular",
          getAuthConfig()
        ),
      ]);

      console.log("ALL DESTINATIONS:", allResponse.data);
      console.log("POPULAR DESTINATIONS:", popularResponse.data);

      setDestinations(
        Array.isArray(allResponse.data)
          ? allResponse.data
          : []
      );

      setPopularDestinations(
        Array.isArray(popularResponse.data)
          ? popularResponse.data
          : []
      );
    } catch (err) {
      console.error("Error loading destinations:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError("Unable to load destinations.");
    } finally {
      setLoadingDestinations(false);
    }
  };

  // ==========================================
  // GOOGLE PLACES SEARCH
  // ==========================================

  const searchDestinations = async () => {
    if (!query.trim()) {
      setError("Please enter a destination.");
      return;
    }

    try {
      setLoading(true);
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

      setPlaces(response.data?.places || []);
    } catch (err) {
      console.error("Destination search error:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError("Unable to search destinations.");
      setPlaces([]);
    } finally {
      setLoading(false);
    }
  };

  // ==========================================
  // GOOGLE PLACE CLICK
  // ==========================================

  const handlePlaceClick = (place) => {
    if (!place?.id) return;

    navigate(
      `/destination-details/${encodeURIComponent(place.id)}`,
      {
        state: {
          place,
        },
      }
    );
  };

  // ==========================================
  // DATABASE DESTINATION CLICK
  // ==========================================

  const handleDestinationClick = (destination) => {
    if (!destination?.id) return;

    navigate(`/destination-details/${destination.id}`, {
      state: {
        destination,
      },
    });
  };

  // ==========================================
  // DESTINATION CARD
  // ==========================================

  const DestinationCard = ({ destination }) => (
    <div
      style={styles.destinationCard}
      onClick={() => handleDestinationClick(destination)}
    >
      <div style={styles.destinationIcon}>📍</div>

      <h3 style={styles.destinationName}>
        {destination.name}
      </h3>

      <p style={styles.destinationText}>
        Explore {destination.name}
      </p>

      <button style={styles.exploreButton}>
        Explore →
      </button>
    </div>
  );

  return (
    <div style={styles.page}>

      {/* ==========================================
          HEADER
      ========================================== */}

      <header style={styles.header}>
        <div>
          <h1 style={styles.logo}>TripNest</h1>

          <p style={styles.subtitle}>
            Explore destinations around the world
          </p>
        </div>

        <button
          style={styles.backButton}
          onClick={() => navigate("/dashboard")}
        >
          Dashboard
        </button>
      </header>


      {/* ==========================================
          MAIN
      ========================================== */}

      <main style={styles.container}>

        <h2 style={styles.title}>
          Explore Destinations 🌍
        </h2>

        <p style={styles.description}>
          Search for destinations using Google Places.
        </p>


        {/* ==========================================
            SEARCH
        ========================================== */}

        <div style={styles.searchBox}>

          <input
            type="text"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setError("");
            }}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                searchDestinations();
              }
            }}
            placeholder="Search any city, place, landmark..."
            style={styles.input}
          />

          <button
            style={styles.searchButton}
            onClick={searchDestinations}
            disabled={loading}
          >
            {loading ? "Searching..." : "Search"}
          </button>

        </div>


        {/* ==========================================
            ERROR
        ========================================== */}

        {error && (
          <div style={styles.error}>
            {error}
          </div>
        )}


        {/* ==========================================
            GOOGLE SEARCH RESULTS
        ========================================== */}

        {places.length > 0 && (
          <section style={styles.section}>

            <div style={styles.sectionHeader}>
              <h2 style={styles.sectionTitle}>
                Search Results
              </h2>

              <button
                style={styles.clearButton}
                onClick={() => {
                  setPlaces([]);
                  setQuery("");
                }}
              >
                Clear
              </button>
            </div>

            <div style={styles.grid}>

              {places.map((place) => (

                <div
                  key={place.id}
                  style={styles.card}
                  onClick={() => handlePlaceClick(place)}
                >

                  <div style={styles.cardIcon}>
                    📍
                  </div>

                  <h3 style={styles.placeName}>
                    {place.displayName?.text ||
                      "Unknown Destination"}
                  </h3>

                  <p style={styles.address}>
                    {place.formattedAddress ||
                      "Address not available"}
                  </p>

                  {place.rating && (
                    <p style={styles.rating}>
                      ⭐ {place.rating}

                      {place.userRatingCount && (
                        <span>
                          {" "}
                          ({place.userRatingCount})
                        </span>
                      )}
                    </p>
                  )}

                  <button style={styles.detailsButton}>
                    View Details →
                  </button>

                </div>

              ))}

            </div>

          </section>
        )}


        {/* ==========================================
            POPULAR DESTINATIONS
        ========================================== */}

        {!loadingDestinations &&
          popularDestinations.length > 0 && (
            <section style={styles.section}>

              <div style={styles.sectionHeader}>
                <div>
                  <h2 style={styles.sectionTitle}>
                    Popular Destinations 🔥
                  </h2>

                  <p style={styles.sectionSubtitle}>
                    Discover some of the most popular
                    destinations.
                  </p>
                </div>
              </div>

              <div style={styles.grid}>

                {popularDestinations.map(
                  (destination) => (
                    <DestinationCard
                      key={destination.id}
                      destination={destination}
                    />
                  )
                )}

              </div>

            </section>
          )}


        {/* ==========================================
            ALL DESTINATIONS
        ========================================== */}

        {!loadingDestinations &&
          destinations.length > 0 && (
            <section style={styles.section}>

              <div style={styles.sectionHeader}>
                <div>
                  <h2 style={styles.sectionTitle}>
                    List of Destinations 🌎
                  </h2>

                  <p style={styles.sectionSubtitle}>
                    Browse all available destinations.
                  </p>
                </div>
              </div>

              <div style={styles.grid}>

                {destinations.map(
                  (destination) => (
                    <DestinationCard
                      key={destination.id}
                      destination={destination}
                    />
                  )
                )}

              </div>

            </section>
          )}


        {/* ==========================================
            LOADING
        ========================================== */}

        {loadingDestinations && (
          <div style={styles.loading}>
            <div style={styles.loadingIcon}>
              🌍
            </div>

            <h3>Loading destinations...</h3>

            <p>
              Please wait while we load destinations.
            </p>
          </div>
        )}


        {/* ==========================================
            NO DESTINATIONS
        ========================================== */}

        {!loadingDestinations &&
          destinations.length === 0 &&
          places.length === 0 &&
          !error && (
            <div style={styles.empty}>

              <div style={styles.emptyIcon}>
                🌍
              </div>

              <h3>
                Find your next destination
              </h3>

              <p>
                Search for cities, tourist attractions,
                landmarks and places.
              </p>

            </div>
          )}

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
    padding: "20px 6%",
    background: "#ffffff",
    borderBottom: "1px solid #e5e7eb",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
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
    padding: "11px 20px",
    borderRadius: "9px",
    cursor: "pointer",
    fontWeight: "600",
    fontSize: "15px",
  },

  container: {
    maxWidth: "1100px",
    margin: "0 auto",
    padding: "45px 25px",
  },

  title: {
    textAlign: "center",
    margin: 0,
    fontSize: "34px",
  },

  description: {
    textAlign: "center",
    color: "#6b7280",
    marginBottom: "30px",
    fontSize: "17px",
  },

  searchBox: {
    display: "flex",
    gap: "12px",
    maxWidth: "850px",
    margin: "0 auto 45px",
  },

  input: {
    flex: 1,
    padding: "15px 18px",
    border: "1px solid #d1d5db",
    borderRadius: "10px",
    fontSize: "16px",
    outline: "none",
    background: "#ffffff",
  },

  searchButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "0 28px",
    borderRadius: "10px",
    cursor: "pointer",
    fontWeight: "600",
    fontSize: "15px",
  },

  error: {
    maxWidth: "850px",
    margin: "0 auto 30px",
    padding: "14px",
    background: "#fee2e2",
    color: "#b91c1c",
    borderRadius: "8px",
    textAlign: "center",
  },

  section: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "16px",
    padding: "28px",
    marginBottom: "30px",
    boxShadow: "0 5px 18px rgba(0,0,0,0.04)",
  },

  sectionHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "22px",
  },

  sectionTitle: {
    margin: 0,
    fontSize: "25px",
  },

  sectionSubtitle: {
    margin: "7px 0 0",
    color: "#6b7280",
  },

  clearButton: {
    border: "1px solid #d1d5db",
    background: "#ffffff",
    padding: "9px 15px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  grid: {
    display: "grid",
    gridTemplateColumns:
      "repeat(auto-fit, minmax(250px, 1fr))",
    gap: "20px",
  },

  destinationCard: {
    background: "#f9fafb",
    border: "1px solid #e5e7eb",
    borderRadius: "14px",
    padding: "22px",
    cursor: "pointer",
    transition: "transform 0.2s",
  },

  destinationIcon: {
    fontSize: "35px",
    marginBottom: "10px",
  },

  destinationName: {
    margin: "0 0 8px",
    fontSize: "20px",
  },

  destinationText: {
    color: "#6b7280",
    marginBottom: "18px",
  },

  exploreButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "10px 15px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  card: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "14px",
    padding: "22px",
    cursor: "pointer",
    boxShadow: "0 5px 18px rgba(0,0,0,0.05)",
    transition: "transform 0.2s",
  },

  cardIcon: {
    fontSize: "35px",
    marginBottom: "10px",
  },

  placeName: {
    margin: "0 0 10px",
    fontSize: "20px",
  },

  address: {
    color: "#6b7280",
    lineHeight: "1.5",
    minHeight: "45px",
  },

  rating: {
    marginTop: "15px",
    fontWeight: "600",
  },

  detailsButton: {
    marginTop: "15px",
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "10px 15px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  loading: {
    textAlign: "center",
    background: "#ffffff",
    borderRadius: "15px",
    padding: "60px 20px",
    border: "1px solid #e5e7eb",
  },

  loadingIcon: {
    fontSize: "50px",
    marginBottom: "10px",
  },

  empty: {
    textAlign: "center",
    background: "#ffffff",
    borderRadius: "15px",
    padding: "60px 20px",
    border: "1px solid #e5e7eb",
  },

  emptyIcon: {
    fontSize: "55px",
    marginBottom: "15px",
  },
};

export default Destinations;