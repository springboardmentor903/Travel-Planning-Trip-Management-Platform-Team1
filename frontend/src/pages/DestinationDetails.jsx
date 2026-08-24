import React, { useEffect, useState } from "react";
import axios from "axios";
import { useLocation, useNavigate, useParams } from "react-router-dom";

function DestinationDetails() {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams();

  const [destination, setDestination] = useState(
    location.state?.destination || null
  );

  const [places, setPlaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadDestinationPlaces();
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

  const loadDestinationPlaces = async () => {
    try {
      setLoading(true);
      setError("");

      let destinationName = destination?.name;

      // If destination name is not available from state,
      // fetch it from backend using database ID.
      if (!destinationName && id) {
        const response = await axios.get(
          `http://localhost:8080/api/destinations/${id}`,
          getAuthConfig()
        );

        destinationName = response.data?.name;
        setDestination(response.data);
      }

      if (!destinationName) {
        setError("Destination information not found.");
        return;
      }

      console.log(
        "Searching tourist places for:",
        destinationName
      );

      // Search Google Places for tourist attractions
      const response = await axios.get(
        "http://localhost:8080/api/destinations/search",
        {
          params: {
            query: `${destinationName} tourist attractions places to visit`,
          },
          ...getAuthConfig(),
        }
      );

      console.log(
        "TOURIST PLACES:",
        response.data
      );

      setPlaces(response.data?.places || []);
    } catch (err) {
      console.error(
        "Error loading destination places:",
        err
      );

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError(
        "Unable to load places for this destination."
      );
    } finally {
      setLoading(false);
    }
  };

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

  return (
    <div style={styles.page}>

      {/* Header */}
      <header style={styles.header}>
        <div>
          <h1 style={styles.logo}>TripNest</h1>

          <p style={styles.subtitle}>
            Explore places around the world
          </p>
        </div>

        <button
          style={styles.backButton}
          onClick={() => navigate("/destinations")}
        >
          ← Destinations
        </button>
      </header>


      {/* Main */}
      <main style={styles.container}>

        {/* Destination Header */}
        <div style={styles.hero}>

          <div style={styles.heroIcon}>
            📍
          </div>

          <h2 style={styles.title}>
            {destination?.name || "Destination"}
          </h2>

          <p style={styles.description}>
            Discover the best places to visit and explore
            in {destination?.name || "this destination"}.
          </p>

        </div>


        {/* Loading */}
        {loading && (
          <div style={styles.loading}>
            <div style={styles.loadingIcon}>
              🌍
            </div>

            <h3>
              Finding places to visit...
            </h3>

            <p>
              Searching Google Places for the best
              attractions.
            </p>
          </div>
        )}


        {/* Error */}
        {!loading && error && (
          <div style={styles.error}>
            {error}
          </div>
        )}


        {/* Places */}
        {!loading && !error && places.length > 0 && (
          <section style={styles.section}>

            <div style={styles.sectionHeader}>

              <div>
                <h2 style={styles.sectionTitle}>
                  Places to Visit 🗺️
                </h2>

                <p style={styles.sectionSubtitle}>
                  Popular attractions and places you
                  can explore.
                </p>
              </div>

              <span style={styles.count}>
                {places.length} places
              </span>

            </div>


            <div style={styles.grid}>

              {places.map((place) => (

                <div
                  key={place.id}
                  style={styles.card}
                  onClick={() => handlePlaceClick(place)}
                >

                  <div style={styles.cardImage}>
                    📍
                  </div>

                  <div style={styles.cardContent}>

                    <h3 style={styles.placeName}>
                      {place.displayName?.text ||
                        "Unknown Place"}
                    </h3>

                    <p style={styles.address}>
                      {place.formattedAddress ||
                        "Address not available"}
                    </p>

                    {place.rating && (
                      <div style={styles.rating}>
                        ⭐ {place.rating}

                        {place.userRatingCount && (
                          <span>
                            {" "}
                            ({place.userRatingCount} reviews)
                          </span>
                        )}
                      </div>
                    )}

                    <button
                      style={styles.viewButton}
                      onClick={(e) => {
                        e.stopPropagation();
                        handlePlaceClick(place);
                      }}
                    >
                      View Details →
                    </button>

                  </div>

                </div>

              ))}

            </div>

          </section>
        )}


        {/* No places */}
        {!loading &&
          !error &&
          places.length === 0 && (
            <div style={styles.empty}>

              <div style={styles.emptyIcon}>
                🔍
              </div>

              <h3>
                No places found
              </h3>

              <p>
                We couldn't find tourist attractions
                for this destination.
              </p>

              <button
                style={styles.backToDestinations}
                onClick={() =>
                  navigate("/destinations")
                }
              >
                ← Explore Other Destinations
              </button>

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
    padding: "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
    fontSize: "15px",
  },

  container: {
    maxWidth: "1100px",
    margin: "0 auto",
    padding: "45px 25px",
  },

  hero: {
    textAlign: "center",
    marginBottom: "40px",
  },

  heroIcon: {
    fontSize: "55px",
    marginBottom: "10px",
  },

  title: {
    margin: 0,
    fontSize: "38px",
  },

  description: {
    color: "#6b7280",
    fontSize: "17px",
    marginTop: "10px",
  },

  section: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "16px",
    padding: "28px",
    boxShadow: "0 5px 18px rgba(0,0,0,0.04)",
  },

  sectionHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "25px",
  },

  sectionTitle: {
    margin: 0,
    fontSize: "26px",
  },

  sectionSubtitle: {
    margin: "7px 0 0",
    color: "#6b7280",
  },

  count: {
    background: "#f3f4f6",
    padding: "8px 14px",
    borderRadius: "20px",
    fontSize: "14px",
    fontWeight: "600",
  },

  grid: {
    display: "grid",
    gridTemplateColumns:
      "repeat(auto-fit, minmax(280px, 1fr))",
    gap: "20px",
  },

  card: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "14px",
    overflow: "hidden",
    cursor: "pointer",
    boxShadow: "0 4px 15px rgba(0,0,0,0.05)",
    transition: "transform 0.2s",
  },

  cardImage: {
    height: "130px",
    background: "#eef2ff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "50px",
  },

  cardContent: {
    padding: "20px",
  },

  placeName: {
    margin: "0 0 10px",
    fontSize: "20px",
  },

  address: {
    color: "#6b7280",
    lineHeight: "1.5",
    minHeight: "45px",
    fontSize: "14px",
  },

  rating: {
    marginTop: "14px",
    fontWeight: "600",
    fontSize: "14px",
  },

  viewButton: {
    marginTop: "16px",
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "10px 16px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  loading: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "16px",
    padding: "60px 20px",
    textAlign: "center",
  },

  loadingIcon: {
    fontSize: "55px",
    marginBottom: "10px",
  },

  error: {
    background: "#fee2e2",
    color: "#b91c1c",
    padding: "15px",
    borderRadius: "8px",
    textAlign: "center",
  },

  empty: {
    background: "#ffffff",
    border: "1px solid #e5e7eb",
    borderRadius: "16px",
    padding: "60px 20px",
    textAlign: "center",
  },

  emptyIcon: {
    fontSize: "55px",
    marginBottom: "15px",
  },

  backToDestinations: {
    marginTop: "20px",
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "12px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },
};

export default DestinationDetails;