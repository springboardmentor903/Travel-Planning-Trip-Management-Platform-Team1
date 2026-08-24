import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function Profile() {
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [trips, setTrips] = useState([]);
const [tripsLoading, setTripsLoading] = useState(true);

  const [preferences, setPreferences] = useState(null);
const [preferencesLoading, setPreferencesLoading] = useState(true);

const [editingPreferences, setEditingPreferences] = useState(false);
const [savingPreferences, setSavingPreferences] = useState(false);
const [preferenceMessage, setPreferenceMessage] = useState("");

const [travelType, setTravelType] = useState("");
const [preferredDestinationId, setPreferredDestinationId] = useState("");
const [favouriteDestinationId, setFavouriteDestinationId] = useState("");

const [editing, setEditing] = useState(false);
const [editName, setEditName] = useState("");
const [saving, setSaving] = useState(false);
const [success, setSuccess] = useState("");

const [destinations, setDestinations] = useState([]);

  const fetchProfile = async () => {
    try {
      const token = localStorage.getItem("token");

      if (!token) {
        navigate("/login");
        return;
      }

      const response = await axios.get(
        "http://localhost:8080/api/users/profile",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      console.log("PROFILE RESPONSE:", response.data);

      setProfile(response.data);
    } catch (err) {
      console.error("Error fetching profile:", err);

      if (err.response?.status === 401) {
        localStorage.removeItem("token");
        navigate("/login");
        return;
      }

      setError("Unable to load profile.");
    } finally {
      setLoading(false);
    }
  };


  const fetchPreferences = async () => {
  try {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    const response = await axios.get(
      "http://localhost:8080/api/users/preferences",
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    console.log("PREFERENCES RESPONSE:", response.data);

    setPreferences(response.data);

   setTravelType(response.data?.preferredTravelType || "");

setPreferredDestinationId(
  response.data?.preferredDestination?.id
    ? String(response.data.preferredDestination.id)
    : ""
);

setFavouriteDestinationId(
  response.data?.favouriteDestination?.id
    ? String(response.data.favouriteDestination.id)
    : ""
);

  } catch (err) {
    console.error("Error fetching preferences:", err);

    if (err.response?.status === 401) {
      localStorage.removeItem("token");
      navigate("/login");
      return;
    }

    setPreferences(null);
  } finally {
    setPreferencesLoading(false);
  }
};


const fetchDestinations = async () => {
  try {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    const response = await axios.get(
      "http://localhost:8080/api/destinations",
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    setDestinations(
      Array.isArray(response.data) ? response.data : []
    );
  } catch (err) {
    console.error("Error fetching destinations:", err);
  }
};




const savePreferences = async () => {
  try {
    setSavingPreferences(true);
    setPreferenceMessage("");

    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    const response = await axios.put(
      "http://localhost:8080/api/users/preferences",
      {
        preferredTravelType: travelType,
        preferredDestinationId:
          preferredDestinationId
            ? Number(preferredDestinationId)
            : null,
        favouriteDestinationId:
          favouriteDestinationId
            ? Number(favouriteDestinationId)
            : null,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    setPreferenceMessage(
      "Travel preferences updated successfully."
    );

    setEditingPreferences(false);

    // Refresh displayed preferences
    await fetchPreferences();

  } catch (err) {
    console.error("Error updating preferences:", err);

    if (err.response?.status === 401) {
      localStorage.removeItem("token");
      navigate("/login");
      return;
    }

    setPreferenceMessage(
      err.response?.data?.message ||
      "Unable to update travel preferences."
    );
  } finally {
    setSavingPreferences(false);
  }
};



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

    setTrips([]);
  } finally {
    setTripsLoading(false);
  }
};

useEffect(() => {
  fetchProfile();
  fetchPreferences();
  fetchTrips();
  fetchDestinations();
}, []);


  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };



  const handleSaveProfile = async () => {
  try {
    setSaving(true);
    setError("");
    setSuccess("");

    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    const response = await axios.put(
      "http://localhost:8080/api/users/profile",
      {
        name: editName,
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    console.log("PROFILE UPDATE RESPONSE:", response.data);

    setProfile(response.data);
    setSuccess("Profile updated successfully.");
    setEditing(false);

  } catch (err) {
    console.error("Error updating profile:", err);

    if (err.response?.status === 401) {
      localStorage.removeItem("token");
      navigate("/login");
      return;
    }

    setError(
      err.response?.data?.message || "Unable to update profile."
    );
  } finally {
    setSaving(false);
  }
};

  if (loading) {
    return (
      <div style={styles.center}>
        <p>Loading profile...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={styles.center}>
        <p style={{ color: "red" }}>{error}</p>
        <button style={styles.button} onClick={fetchProfile}>
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div style={styles.page}>
      {/* Header */}
      <header style={styles.header}>
        <div>
          <h1 style={styles.logo}>TripNest</h1>
          <p style={styles.subtitle}>Your travel profile</p>
        </div>

        <div style={styles.headerButtons}>
          <button
            style={styles.backButton}
            onClick={() => navigate("/dashboard")}
          >
            Dashboard
          </button>

          <button style={styles.logoutButton} onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      {/* Main */}
      <main style={styles.container}>
        <div style={styles.profileCard}>
          <div style={styles.avatar}>
            {profile?.name?.charAt(0)?.toUpperCase() || "U"}
          </div>

          <h2 style={styles.name}>{profile?.name || "User"}</h2>

          <p style={styles.email}>{profile?.email || "No email available"}</p>

          {success && (
  <p
    style={{
      color: "#16a34a",
      textAlign: "center",
      fontWeight: "600",
      marginTop: "15px",
    }}
  >
    {success}
  </p>
)}

{editing && (
  <div
    style={{
      marginTop: "25px",
      padding: "25px",
      border: "1px solid #e5e7eb",
      borderRadius: "12px",
      background: "#fafafa",
    }}
  >
    <h3 style={styles.sectionTitle}>Edit Profile</h3>

    <div style={{ marginBottom: "18px" }}>
      <label
        style={{
          display: "block",
          marginBottom: "8px",
          fontWeight: "600",
        }}
      >
        Full Name
      </label>

      <input
        type="text"
        value={editName}
        onChange={(e) => setEditName(e.target.value)}
        style={{
          width: "100%",
          padding: "12px",
          border: "1px solid #d1d5db",
          borderRadius: "8px",
          boxSizing: "border-box",
          fontSize: "16px",
        }}
      />
    </div>

    <div style={{ marginBottom: "18px" }}>
      <label
        style={{
          display: "block",
          marginBottom: "8px",
          fontWeight: "600",
        }}
      >
        Email Address
      </label>

      <input
        type="email"
        value={profile?.email || ""}
        disabled
        style={{
          width: "100%",
          padding: "12px",
          border: "1px solid #d1d5db",
          borderRadius: "8px",
          boxSizing: "border-box",
          fontSize: "16px",
          background: "#e5e7eb",
          cursor: "not-allowed",
        }}
      />
    </div>

    <div style={{ display: "flex", gap: "12px" }}>
      <button
        style={styles.actionButton}
        onClick={handleSaveProfile}
        disabled={saving}
      >
        {saving ? "Saving..." : "Save Changes"}
      </button>

      <button
        style={styles.backButton}
        onClick={() => {
          setEditing(false);
          setError("");
        }}
        disabled={saving}
      >
        Cancel
      </button>
    </div>
  </div>
)}

          <div style={styles.divider}></div>

          {/* Personal Information */}
          <section>
            <h3 style={styles.sectionTitle}>Personal Information</h3>

            <div style={styles.infoGrid}>
              <div style={styles.infoBox}>
                <span style={styles.label}>Full Name</span>
                <span style={styles.value}>
                  {profile?.name || "Not available"}
                </span>
              </div>

              <div style={styles.infoBox}>
                <span style={styles.label}>Email Address</span>
                <span style={styles.value}>
                  {profile?.email || "Not available"}
                </span>
              </div>

              <div style={styles.infoBox}>
                <span style={styles.label}>User ID</span>
                <span style={styles.value}>
                  {profile?.id ?? "Not available"}
                </span>
              </div>

              <div style={styles.infoBox}>
                <span style={styles.label}>Google Account</span>
                <span style={styles.value}>
                  {profile?.oauthGoogle ? "Connected" : "Not Connected"}
                </span>
              </div>

              <div style={styles.infoBox}>
                <span style={styles.label}>Account Created</span>
                <span style={styles.value}>
                  {profile?.createdAt
                    ? new Date(profile.createdAt).toLocaleDateString()
                    : "Not available"}
                </span>
              </div>
            </div>
          </section>



          {/* Travel Preferences */}
{/* Travel Preferences */}
<section style={styles.actionsSection}>
  <div
    style={{
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      gap: "15px",
      marginBottom: "20px",
    }}
  >
    <h3 style={{ ...styles.sectionTitle, margin: 0 }}>
      Travel Preferences
    </h3>

    <button
      style={styles.actionButton}
      onClick={() => {
        setEditingPreferences(!editingPreferences);
        setPreferenceMessage("");
      }}
    >
      {editingPreferences ? "Cancel" : "Edit Preferences"}
    </button>
  </div>

  {preferenceMessage && (
    <p
      style={{
        marginBottom: "18px",
        color: preferenceMessage.includes("successfully")
          ? "#16a34a"
          : "#dc2626",
        fontWeight: "600",
      }}
    >
      {preferenceMessage}
    </p>
  )}

  {preferencesLoading ? (
    <p style={{ color: "#6b7280" }}>
      Loading travel preferences...
    </p>
  ) : editingPreferences ? (

    <div style={styles.preferencesForm}>

      {/* Travel Type */}
      <div style={styles.formGroup}>
        <label style={styles.formLabel}>
          Preferred Travel Type
        </label>

        <select
          value={travelType}
          onChange={(e) => setTravelType(e.target.value)}
          style={styles.select}
        >
          <option value="">Select travel type</option>
          <option value="Adventure">Adventure</option>
          <option value="Relaxation">Relaxation</option>
          <option value="Family">Family</option>
          <option value="Solo">Solo</option>
          <option value="Business">Business</option>
          <option value="Cultural">Cultural</option>
        </select>
      </div>

      {/* Preferred Destination */}
      <div style={styles.formGroup}>
        <label style={styles.formLabel}>
          Preferred Destination
        </label>

        <select
          value={preferredDestinationId}
          onChange={(e) =>
            setPreferredDestinationId(e.target.value)
          }
          style={styles.select}
        >
          <option value="">
            Select preferred destination
          </option>

          {destinations.map((destination) => (
            <option
              key={destination.id}
              value={destination.id}
            >
              {destination.name}
            </option>
          ))}
        </select>
      </div>

      {/* Favourite Destination */}
      <div style={styles.formGroup}>
        <label style={styles.formLabel}>
          Favourite Destination
        </label>

        <select
          value={favouriteDestinationId}
          onChange={(e) =>
            setFavouriteDestinationId(e.target.value)
          }
          style={styles.select}
        >
          <option value="">
            Select favourite destination
          </option>

          {destinations.map((destination) => (
            <option
              key={destination.id}
              value={destination.id}
            >
              {destination.name}
            </option>
          ))}
        </select>
      </div>

      <button
        style={{
          ...styles.actionButton,
          opacity: savingPreferences ? 0.7 : 1,
        }}
        onClick={savePreferences}
        disabled={savingPreferences}
      >
        {savingPreferences
          ? "Saving..."
          : "Save Preferences"}
      </button>

    </div>

  ) : (

    <div style={styles.infoGrid}>

      <div style={styles.infoBox}>
        <span style={styles.label}>
          Preferred Travel Type
        </span>

        <span style={styles.value}>
          {preferences?.preferredTravelType ||
            "Not selected"}
        </span>
      </div>

      <div style={styles.infoBox}>
        <span style={styles.label}>
          Preferred Destination
        </span>

        <span style={styles.value}>
          {preferences?.preferredDestination?.name ||
            "Not selected"}
        </span>
      </div>

      <div style={styles.infoBox}>
        <span style={styles.label}>
          Favourite Destination
        </span>

        <span style={styles.value}>
          {preferences?.favouriteDestination?.name ||
            "Not selected"}
        </span>
      </div>

    </div>
  )}
</section>


{/* Travel History */}
<section style={styles.actionsSection}>
  <h3 style={styles.sectionTitle}>Travel History</h3>

  {tripsLoading ? (
    <p style={{ color: "#6b7280" }}>
      Loading travel history...
    </p>
  ) : trips.length === 0 ? (
    <div style={styles.infoBox}>
      <span style={styles.value}>
        No trips found.
      </span>

      <span style={styles.label}>
        Your previous and upcoming trips will appear here.
      </span>
    </div>
  ) : (
    <div style={styles.historyList}>

      {trips.map((trip) => (
        <div
          key={trip.id}
          style={styles.historyCard}
        >

          <div style={styles.historyHeader}>

            <div>
              <h4 style={styles.historyTitle}>
                {trip.destination?.name || "Unknown Destination"}
              </h4>

              <p style={styles.historyDates}>
                {trip.startDate || "N/A"} → {trip.endDate || "N/A"}
              </p>
            </div>

            <span style={styles.statusBadge}>
              {trip.status || "N/A"}
            </span>

          </div>

          <div style={styles.historyDetails}>

            <div>
              <span style={styles.label}>
                Travelers </span>

              <span style={styles.value}>
                {trip.travelers ??    "N/A"}
              </span>
            </div>

            <div>
              <span style={styles.label}>
                Budget </span>

              <span style={styles.value}>
                {trip.budget !== undefined &&
                trip.budget !== null
                  ? `₹${trip.budget}`
                  : "N/A"}
              </span>
            </div>

          </div>

        </div>
      ))}

    </div>
  )}
</section>

          {/* Account Actions */}
          <section style={styles.actionsSection}>
            <h3 style={styles.sectionTitle}>Account</h3>

            <div style={styles.actions}>
              <button
                style={styles.actionButton}
                onClick={() => navigate("/dashboard")}
              >
                View Dashboard
              </button>

              <button
  style={styles.actionButton}
  onClick={() => setEditing(true)}
>
  Edit Profile
</button>
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    width: "100%",
    background: "#f5f7fb",
    color: "#111827",
  },

  header: {
    width: "100%",
    padding: "24px 5%",
    background: "#ffffff",
    borderBottom: "1px solid #e5e7eb",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
  },

  logo: {
    margin: 0,
    fontSize: "32px",
    fontWeight: "700",
    color: "#111827",
  },

  subtitle: {
    margin: "4px 0 0",
    color: "#6b7280",
    fontSize: "15px",
  },

  headerButtons: {
    display: "flex",
    gap: "12px",
    alignItems: "center",
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

  logoutButton: {
    border: "none",
    background: "#ef4444",
    color: "#ffffff",
    padding: "11px 20px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  container: {
    width: "100%",
    maxWidth: "1000px",
    margin: "0 auto",
    padding: "45px 25px",
  },

  profileCard: {
    background: "#ffffff",
    borderRadius: "18px",
    padding: "40px",
    boxShadow: "0 8px 30px rgba(0, 0, 0, 0.08)",
  },

  avatar: {
    width: "90px",
    height: "90px",
    borderRadius: "50%",
    background: "#111827",
    color: "#ffffff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "36px",
    fontWeight: "700",
    margin: "0 auto 18px",
  },

  name: {
    margin: 0,
    textAlign: "center",
    fontSize: "28px",
    fontWeight: "700",
  },

  email: {
    marginTop: "7px",
    textAlign: "center",
    color: "#6b7280",
  },

  divider: {
    height: "1px",
    background: "#e5e7eb",
    margin: "35px 0",
  },

  sectionTitle: {
    margin: "0 0 20px",
    fontSize: "20px",
    fontWeight: "700",
    color: "#111827",
  },

  infoGrid: {
    display: "grid",
    gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
    gap: "16px",
  },

  infoBox: {
    padding: "18px",
    border: "1px solid #e5e7eb",
    borderRadius: "12px",
    background: "#fafafa",
    display: "flex",
    flexDirection: "column",
    gap: "7px",
  },


  historyList: {
  display: "flex",
  flexDirection: "column",
  gap: "16px",
},

historyCard: {
  padding: "20px",
  border: "1px solid #e5e7eb",
  borderRadius: "14px",
  background: "#fafafa",
},

historyHeader: {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "flex-start",
  gap: "15px",
  marginBottom: "18px",
},

historyTitle: {
  margin: 0,
  fontSize: "19px",
  fontWeight: "700",
  color: "#111827",
},

historyDates: {
  margin: "6px 0 0",
  color: "#6b7280",
  fontSize: "14px",
},

statusBadge: {
  padding: "6px 12px",
  borderRadius: "20px",
  background: "#e5e7eb",
  color: "#374151",
  fontSize: "12px",
  fontWeight: "700",
  textTransform: "capitalize",
},

historyDetails: {
  display: "grid",
  gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
  gap: "15px",
},

  label: {
    fontSize: "13px",
    color: "#6b7280",
    fontWeight: "600",
  },

  value: {
    fontSize: "16px",
    color: "#111827",
    fontWeight: "600",
    wordBreak: "break-word",
  },

  actionsSection: {
    marginTop: "35px",
  },

  actions: {
    display: "flex",
    gap: "12px",
    flexWrap: "wrap",
  },

  actionButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "12px 20px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  center: {
    minHeight: "100vh",
    width: "100%",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    flexDirection: "column",
    gap: "15px",
  },

  button: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding: "10px 18px",
    borderRadius: "8px",
    cursor: "pointer",
  },


  preferencesForm: {
  display: "flex",
  flexDirection: "column",
  gap: "18px",
  padding: "20px",
  border: "1px solid #e5e7eb",
  borderRadius: "14px",
  background: "#fafafa",
},

formGroup: {
  display: "flex",
  flexDirection: "column",
  gap: "8px",
},

formLabel: {
  fontSize: "14px",
  fontWeight: "600",
  color: "#374151",
},

select: {
  width: "100%",
  padding: "12px 14px",
  border: "1px solid #d1d5db",
  borderRadius: "8px",
  background: "#ffffff",
  color: "#111827",
  fontSize: "15px",
  outline: "none",
  boxSizing: "border-box",
},


};

export default Profile;