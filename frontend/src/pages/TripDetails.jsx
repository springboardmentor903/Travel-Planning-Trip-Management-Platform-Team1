import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";

import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
} from "chart.js";

import { Doughnut } from "react-chartjs-2";

ChartJS.register(
  ArcElement,
  Tooltip,
  Legend
);

function TripDetails() {

  const { id } = useParams();
  const navigate = useNavigate();

  // =====================================================
  // TRIP
  // =====================================================

  const [trip, setTrip] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // =====================================================
  // ITINERARY
  // =====================================================

  const [itineraries, setItineraries] = useState([]);
  const [loadingItinerary, setLoadingItinerary] = useState(true);

  // =====================================================
  // BUDGET
  // =====================================================

  const [budget, setBudget] = useState(null);
  const [loadingBudget, setLoadingBudget] = useState(true);

  const [showBudgetModal, setShowBudgetModal] = useState(false);
  const [savingBudget, setSavingBudget] = useState(false);

  const [budgetForm, setBudgetForm] = useState({
    totalBudget: "",
    accommodationBudget: "",
    foodBudget: "",
    transportBudget: "",
    activityBudget: "",
    miscellaneousBudget: "",
  });

  // =====================================================
  // EXPENSES
  // =====================================================

  const [expenses, setExpenses] = useState([]);
  const [loadingExpenses, setLoadingExpenses] = useState(true);

  const [totalExpenses, setTotalExpenses] = useState(0);
  const [remainingBudget, setRemainingBudget] = useState(0);
  const [categorySummary, setCategorySummary] = useState([]);

  const [showExpenseModal, setShowExpenseModal] = useState(false);
  const [savingExpense, setSavingExpense] = useState(false);

  const [editingExpense, setEditingExpense] = useState(null);
  const [deletingExpense, setDeletingExpense] = useState(null);

  const [expenseForm, setExpenseForm] = useState({
    category: "",
    amount: "",
    expenseDate: "",
    receiptLink: "",
  });

  // =====================================================
  // ITINERARY MODAL
  // =====================================================

  const [showModal, setShowModal] = useState(false);
  const [savingItinerary, setSavingItinerary] = useState(false);

  const [editingItinerary, setEditingItinerary] = useState(null);
  const [deletingItinerary, setDeletingItinerary] = useState(null);

  const [formData, setFormData] = useState({
    dayNumber: "",
    date: "",
    title: "",
    description: "",
  });

  // =====================================================
  // AUTH CONFIG
  // =====================================================

  const getAuthConfig = () => {

    const token = localStorage.getItem("token");

    return {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    };
  };

  // =====================================================
  // AUTH ERROR HANDLER
  // =====================================================

  const handleAuthError = (err) => {

    if (err.response?.status === 401) {

      localStorage.removeItem("token");
      localStorage.removeItem("userId");
      localStorage.removeItem("userName");
      localStorage.removeItem("userEmail");

      alert("Your session has expired. Please login again.");

      navigate("/login");

      return true;
    }

    return false;
  };

  // =====================================================
  // INITIAL LOAD
  // =====================================================

  useEffect(() => {

    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login");
      return;
    }

    fetchTrip();
    fetchItineraries();
    fetchBudget();
    fetchExpenses();

  }, [id]);

  // =====================================================
  // FETCH TRIP
  // =====================================================

  const fetchTrip = async () => {

    try {

      setLoading(true);
      setError("");

      const response = await axios.get(
        `http://localhost:8080/api/trips/${id}`,
        getAuthConfig()
      );

      console.log("TRIP DETAILS:", response.data);

      setTrip(response.data);

    } catch (err) {

      console.error("Error fetching trip:", err);

      if (handleAuthError(err)) return;

      setError("Unable to load trip details.");

    } finally {

      setLoading(false);

    }
  };

  // =====================================================
  // FETCH ITINERARIES
  // =====================================================

  const fetchItineraries = async () => {

    try {

      setLoadingItinerary(true);

      const response = await axios.get(
        `http://localhost:8080/api/trips/${id}/itineraries`,
        getAuthConfig()
      );

      console.log("ITINERARIES:", response.data);

      if (Array.isArray(response.data)) {
        setItineraries(response.data);
      } else {
        setItineraries([]);
      }

    } catch (err) {

      console.error("Error fetching itineraries:", err);

      if (handleAuthError(err)) return;

      setItineraries([]);

    } finally {

      setLoadingItinerary(false);

    }
  };

  // =====================================================
  // FETCH BUDGET
  // =====================================================

  const fetchBudget = async () => {

    try {

      setLoadingBudget(true);

      const response = await axios.get(
        `http://localhost:8080/api/budgets/trip/${id}`,
        getAuthConfig()
      );

      console.log("BUDGET:", response.data);

      setBudget(response.data);

    } catch (err) {

      console.error("Error fetching budget:", err);

      // 404 means budget does not exist yet
      if (err.response?.status === 404) {
        setBudget(null);
        return;
      }

      if (handleAuthError(err)) return;

      setBudget(null);

    } finally {

      setLoadingBudget(false);

    }
  };

  // =====================================================
  // FETCH EXPENSES
  // =====================================================

  const fetchExpenses = async () => {

    try {

      setLoadingExpenses(true);

      // -----------------------------
      // EXPENSE LIST
      // -----------------------------

      const expenseResponse = await axios.get(
        `http://localhost:8080/api/expenses/trip/${id}`,
        getAuthConfig()
      );

      console.log(
        "EXPENSES:",
        expenseResponse.data
      );

      if (Array.isArray(expenseResponse.data)) {
        setExpenses(expenseResponse.data);
      } else {
        setExpenses([]);
      }

      // -----------------------------
      // TOTAL
      // -----------------------------

      try {

        const totalResponse = await axios.get(
          `http://localhost:8080/api/expenses/trip/${id}/total`,
          getAuthConfig()
        );

        setTotalExpenses(
          Number(totalResponse.data || 0)
        );

      } catch (err) {

        console.error(
          "Error fetching total expenses:",
          err
        );

        if (handleAuthError(err)) return;

      }

      // -----------------------------
      // CATEGORY SUMMARY
      // -----------------------------

      try {

        const categoryResponse = await axios.get(
          `http://localhost:8080/api/expenses/trip/${id}/category-summary`,
          getAuthConfig()
        );

        console.log(
          "CATEGORY SUMMARY:",
          categoryResponse.data
        );

        if (Array.isArray(categoryResponse.data)) {
          setCategorySummary(
            categoryResponse.data
          );
        } else {
          setCategorySummary([]);
        }

      } catch (err) {

        console.error(
          "Error fetching category summary:",
          err
        );

        if (handleAuthError(err)) return;

        setCategorySummary([]);

      }

      // -----------------------------
      // REMAINING BUDGET
      // -----------------------------

      try {

        const remainingResponse = await axios.get(
          `http://localhost:8080/api/expenses/trip/${id}/remaining-budget`,
          getAuthConfig()
        );

        console.log(
          "REMAINING BUDGET:",
          remainingResponse.data
        );

        setRemainingBudget(
          Number(remainingResponse.data || 0)
        );

      } catch (err) {

        console.error(
          "Error fetching remaining budget:",
          err
        );

        if (handleAuthError(err)) return;

      }

    } catch (err) {

      console.error(
        "Error fetching expenses:",
        err
      );

      if (handleAuthError(err)) return;

      setExpenses([]);

    } finally {

      setLoadingExpenses(false);

    }
  };

  // =====================================================
  // REFRESH EXPENSE DATA
  // =====================================================

  const refreshExpenseData = async () => {

    await fetchExpenses();

  };

  // =====================================================
  // FORMAT DATE
  // =====================================================

  const formatDate = (date) => {

    if (!date) {
      return "Not available";
    }

    return new Date(date).toLocaleDateString(
      "en-IN",
      {
        year: "numeric",
        month: "long",
        day: "numeric",
      }
    );
  };

  // =====================================================
  // FORMAT MONEY
  // =====================================================

  const formatMoney = (amount) => {

    return `₹${Number(amount || 0).toLocaleString(
      "en-IN",
      {
        maximumFractionDigits: 2,
      }
    )}`;

  };

  // =====================================================
  // BUDGET FORM CHANGE
  // =====================================================

  const handleBudgetChange = (e) => {

    const { name, value } = e.target;

    setBudgetForm((prev) => ({
      ...prev,
      [name]: value,
    }));

  };

  // =====================================================
  // OPEN ADD/EDIT BUDGET
  // =====================================================

  const openBudgetModal = () => {

    if (budget) {

      setBudgetForm({
        totalBudget:
          budget.totalBudget ?? "",

        accommodationBudget:
          budget.accommodationBudget ?? "",

        foodBudget:
          budget.foodBudget ?? "",

        transportBudget:
          budget.transportBudget ?? "",

        activityBudget:
          budget.activityBudget ?? "",

        miscellaneousBudget:
          budget.miscellaneousBudget ?? "",
      });

    } else {

      setBudgetForm({
        totalBudget:
          trip?.budget ?? "",

        accommodationBudget: "",
        foodBudget: "",
        transportBudget: "",
        activityBudget: "",
        miscellaneousBudget: "",
      });

    }

    setShowBudgetModal(true);

  };

  // =====================================================
  // CLOSE BUDGET MODAL
  // =====================================================

  const closeBudgetModal = () => {

    if (savingBudget) return;

    setShowBudgetModal(false);

  };

  // =====================================================
  // SAVE BUDGET
  // =====================================================

  const handleSaveBudget = async (e) => {

    e.preventDefault();

    if (
      !budgetForm.totalBudget ||
      Number(budgetForm.totalBudget) <= 0
    ) {

      alert(
        "Please enter a valid total budget."
      );

      return;
    }

    try {

      setSavingBudget(true);

      const payload = {

        tripId: Number(id),

        totalBudget:
          Number(budgetForm.totalBudget),

        accommodationBudget:
          budgetForm.accommodationBudget
            ? Number(
                budgetForm.accommodationBudget
              )
            : null,

        foodBudget:
          budgetForm.foodBudget
            ? Number(
                budgetForm.foodBudget
              )
            : null,

        transportBudget:
          budgetForm.transportBudget
            ? Number(
                budgetForm.transportBudget
              )
            : null,

        activityBudget:
          budgetForm.activityBudget
            ? Number(
                budgetForm.activityBudget
              )
            : null,

        miscellaneousBudget:
          budgetForm.miscellaneousBudget
            ? Number(
                budgetForm.miscellaneousBudget
              )
            : null,
      };

      console.log(
        "BUDGET PAYLOAD:",
        payload
      );

      if (budget) {

        // UPDATE
        await axios.put(
          `http://localhost:8080/api/budgets/trip/${id}`,
          payload,
          getAuthConfig()
        );

        alert(
          "Budget updated successfully! ✅"
        );

      } else {

        // CREATE
        await axios.post(
          "http://localhost:8080/api/budgets",
          payload,
          getAuthConfig()
        );

        alert(
          "Budget created successfully! 🎉"
        );

      }

      setShowBudgetModal(false);

      await fetchBudget();

      await fetchExpenses();

    } catch (err) {

      console.error(
        "Error saving budget:",
        err
      );

      if (handleAuthError(err)) return;

      alert(
        err.response?.data?.message ||
        "Unable to save budget."
      );

    } finally {

      setSavingBudget(false);

    }
  };

  // =====================================================
  // EXPENSE FORM CHANGE
  // =====================================================

  const handleExpenseChange = (e) => {

    const { name, value } = e.target;

    setExpenseForm((prev) => ({
      ...prev,
      [name]: value,
    }));

  };

  // =====================================================
  // OPEN ADD EXPENSE
  // =====================================================

  const openAddExpenseModal = () => {

    if (!budget) {

      alert(
        "Please create a budget before adding an expense."
      );

      return;
    }

    const userId =
      localStorage.getItem("userId") || trip?.user?.id || trip?.userId;

    if (!userId) {

      alert(
        "User information not found. Please logout and login again."
      );

      return;
    }

    localStorage.setItem("userId", userId);

    setEditingExpense(null);

    setExpenseForm({
      category: "",
      amount: "",
      expenseDate:
        new Date().toISOString().split("T")[0],
      receiptLink: "",
    });

    setShowExpenseModal(true);

  };

  // =====================================================
  // OPEN EDIT EXPENSE
  // =====================================================

  const openEditExpenseModal = (expense) => {

    setEditingExpense(expense);

    setExpenseForm({

      category:
        expense.category ?? "",

      amount:
        expense.amount ?? "",

      expenseDate:
        expense.expenseDate
          ? expense.expenseDate.substring(0, 10)
          : "",

      receiptLink:
        expense.receiptLink ?? "",
    });

    setShowExpenseModal(true);

  };

  // =====================================================
  // CLOSE EXPENSE MODAL
  // =====================================================

  const closeExpenseModal = () => {

    if (savingExpense) return;

    setShowExpenseModal(false);
    setEditingExpense(null);

    setExpenseForm({
      category: "",
      amount: "",
      expenseDate: "",
      receiptLink: "",
    });

  };

  // =====================================================
  // SAVE EXPENSE
  // =====================================================

  const handleSaveExpense = async (e) => {

    e.preventDefault();

    if (!budget) {

      alert(
        "Please create a budget first."
      );

      return;
    }

    if (!expenseForm.category) {

      alert(
        "Please select an expense category."
      );

      return;
    }

    if (
      !expenseForm.amount ||
      Number(expenseForm.amount) <= 0
    ) {

      alert(
        "Please enter a valid expense amount."
      );

      return;
    }

    if (!expenseForm.expenseDate) {

      alert(
        "Please select expense date."
      );

      return;
    }

    const userId =
      localStorage.getItem("userId") || trip?.user?.id || trip?.userId;

    if (!userId) {

      alert(
        "User ID not found. Please logout and login again."
      );

      return;
    }

    localStorage.setItem("userId", userId);

    try {

      setSavingExpense(true);

      const payload = {

        tripId: Number(id),

        budgetId:
          Number(budget.id),

        payerId:
          Number(userId),

        category:
          expenseForm.category,

        amount:
          Number(expenseForm.amount),

        expenseDate:
          expenseForm.expenseDate,

        receiptLink:
          expenseForm.receiptLink.trim() ||
          null,
      };

      console.log(
        "EXPENSE PAYLOAD:",
        payload
      );

      if (editingExpense) {

        await axios.put(
          `http://localhost:8080/api/expenses/${editingExpense.id}/trip/${id}`,
          payload,
          getAuthConfig()
        );

        alert(
          "Expense updated successfully! ✅"
        );

      } else {

        await axios.post(
          "http://localhost:8080/api/expenses",
          payload,
          getAuthConfig()
        );

        alert(
          "Expense added successfully! 🎉"
        );

      }

      closeExpenseModal();

      await refreshExpenseData();

    } catch (err) {

      console.error(
        "Error saving expense:",
        err
      );

      if (handleAuthError(err)) return;

      console.error(
        "Expense response:",
        err.response?.data
      );

      alert(
        err.response?.data?.message ||
        "Unable to save expense."
      );

    } finally {

      setSavingExpense(false);

    }
  };

  // =====================================================
  // DELETE EXPENSE
  // =====================================================

  const handleDeleteExpense = async (
    expenseId
  ) => {

    const confirmed =
      window.confirm(
        "Are you sure you want to delete this expense?"
      );

    if (!confirmed) return;

    try {

      setDeletingExpense(expenseId);

      await axios.delete(
        `http://localhost:8080/api/expenses/${expenseId}/trip/${id}`,
        getAuthConfig()
      );

      alert(
        "Expense deleted successfully! 🗑️"
      );

      await refreshExpenseData();

    } catch (err) {

      console.error(
        "Error deleting expense:",
        err
      );

      if (handleAuthError(err)) return;

      alert(
        err.response?.data?.message ||
        "Unable to delete expense."
      );

    } finally {

      setDeletingExpense(null);

    }
  };

  // =====================================================
  // ITINERARY FORM CHANGE
  // =====================================================

  const handleInputChange = (e) => {

    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

  };

  // =====================================================
  // OPEN ADD ITINERARY
  // =====================================================

  const openAddModal = () => {

    const nextDay =
      itineraries.length > 0
        ? Math.max(
            ...itineraries.map(
              (item) =>
                Number(item.dayNumber) || 0
            )
          ) + 1
        : 1;

    setEditingItinerary(null);

    setFormData({
      dayNumber: nextDay,
      date: "",
      title: "",
      description: "",
    });

    setShowModal(true);

  };

  // =====================================================
  // OPEN EDIT ITINERARY
  // =====================================================

  const openEditModal = (item) => {

    setEditingItinerary(item);

    setFormData({

      dayNumber:
        item.dayNumber ?? "",

      date:
        item.date
          ? item.date.substring(0, 10)
          : "",

      title:
        item.title ?? "",

      description:
        item.description ?? "",
    });

    setShowModal(true);

  };

  // =====================================================
  // CLOSE ITINERARY MODAL
  // =====================================================

  const closeModal = () => {

    if (savingItinerary) return;

    setShowModal(false);
    setEditingItinerary(null);

    setFormData({
      dayNumber: "",
      date: "",
      title: "",
      description: "",
    });

  };

  // =====================================================
  // ADD ITINERARY
  // =====================================================

  const handleAddItinerary = async (e) => {

    e.preventDefault();

    if (!formData.dayNumber) {

      alert(
        "Please enter day number."
      );

      return;
    }

    if (!formData.title.trim()) {

      alert(
        "Please enter activity title."
      );

      return;
    }

    try {

      setSavingItinerary(true);

      const payload = {

        dayNumber:
          Number(formData.dayNumber),

        date:
          formData.date || null,

        title:
          formData.title.trim(),

        description:
          formData.description.trim(),
      };

      await axios.post(
        `http://localhost:8080/api/trips/${id}/itineraries`,
        payload,
        getAuthConfig()
      );

      alert(
        "Activity added successfully! 🎉"
      );

      closeModal();

      await fetchItineraries();

    } catch (err) {

      console.error(
        "Error adding itinerary:",
        err
      );

      if (handleAuthError(err)) return;

      alert(
        err.response?.data?.message ||
        "Unable to add activity."
      );

    } finally {

      setSavingItinerary(false);

    }
  };

  // =====================================================
  // UPDATE ITINERARY
  // =====================================================

  const handleUpdateItinerary = async (e) => {

    e.preventDefault();

    if (!editingItinerary) {

      alert(
        "No activity selected."
      );

      return;
    }

    if (!formData.dayNumber) {

      alert(
        "Please enter day number."
      );

      return;
    }

    if (!formData.title.trim()) {

      alert(
        "Please enter activity title."
      );

      return;
    }

    try {

      setSavingItinerary(true);

      const payload = {

        dayNumber:
          Number(formData.dayNumber),

        date:
          formData.date || null,

        title:
          formData.title.trim(),

        description:
          formData.description.trim(),
      };

      await axios.put(
        `http://localhost:8080/api/trips/${id}/itineraries/${editingItinerary.id}`,
        payload,
        getAuthConfig()
      );

      alert(
        "Activity updated successfully! ✅"
      );

      closeModal();

      await fetchItineraries();

    } catch (err) {

      console.error(
        "Error updating itinerary:",
        err
      );

      if (handleAuthError(err)) return;

      alert(
        err.response?.data?.message ||
        "Unable to update activity."
      );

    } finally {

      setSavingItinerary(false);

    }
  };

  // =====================================================
  // DELETE ITINERARY
  // =====================================================

  const handleDeleteItinerary = async (
    itineraryId
  ) => {

    const confirmed =
      window.confirm(
        "Are you sure you want to delete this activity?"
      );

    if (!confirmed) return;

    try {

      setDeletingItinerary(
        itineraryId
      );

      await axios.delete(
        `http://localhost:8080/api/trips/${id}/itineraries/${itineraryId}`,
        getAuthConfig()
      );

      alert(
        "Activity deleted successfully! 🗑️"
      );

      await fetchItineraries();

    } catch (err) {

      console.error(
        "Error deleting itinerary:",
        err
      );

      if (handleAuthError(err)) return;

      alert(
        "Unable to delete activity."
      );

    } finally {

      setDeletingItinerary(null);

    }
  };

  // =====================================================
  // DELETE TRIP
  // =====================================================

  const handleDelete = async () => {

    const confirmed =
      window.confirm(
        "Are you sure you want to delete this trip?"
      );

    if (!confirmed) return;

    try {

      await axios.delete(
        `http://localhost:8080/api/trips/${id}`,
        getAuthConfig()
      );

      alert(
        "Trip deleted successfully."
      );

      navigate("/trips");

    } catch (err) {

      console.error(
        "Error deleting trip:",
        err
      );

      if (handleAuthError(err)) return;

      alert(
        "Unable to delete trip."
      );

    }
  };

  // =====================================================
  // CHART DATA & COLORS
  // =====================================================

  const getCategoryColor = (categoryName, index) => {
    const name = (categoryName || "").toLowerCase().trim();
    if (name.includes("hotel") || name.includes("accommodat")) return "#6366F1";
    if (name.includes("food") || name.includes("dining") || name.includes("eat") || name.includes("restaur")) return "#10B981";
    if (name.includes("transport") || name.includes("travel") || name.includes("cab") || name.includes("taxi") || name.includes("flight") || name.includes("bus")) return "#F59E0B";
    if (name.includes("entertain") || name.includes("activity") || name.includes("fun") || name.includes("ticket")) return "#EC4899";
    if (name.includes("shop") || name.includes("buy")) return "#06B6D4";
    if (name.includes("misc") || name.includes("other")) return "#8B5CF6";

    const fallbackColors = [
      "#6366F1",
      "#10B981",
      "#F59E0B",
      "#EC4899",
      "#06B6D4",
      "#8B5CF6",
      "#F97316",
      "#3B82F6",
      "#14B8A6",
      "#E11D48",
    ];

    return fallbackColors[index % fallbackColors.length];
  };

  const categoryColors = categorySummary.map((item, index) =>
    getCategoryColor(item[0], index)
  );

  const chartData = {
    labels: categorySummary.map((item) => item[0]),
    datasets: [
      {
        data: categorySummary.map((item) => Number(item[1])),
        backgroundColor: categoryColors,
        hoverBackgroundColor: categoryColors,
        borderColor: "#ffffff",
        borderWidth: 2,
        hoverOffset: 6,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: "bottom",
        labels: {
          padding: 16,
          usePointStyle: true,
          font: {
            size: 13,
            weight: "600",
          },
        },
      },
      tooltip: {
        callbacks: {
          label: function (context) {
            return ` ${context.label}: ${formatMoney(context.raw)}`;
          },
        },
      },
    },
  };

  // =====================================================
  // LOADING
  // =====================================================

  if (loading) {

    return (

      <div style={styles.center}>

        <div style={styles.loadingIcon}>
          ✈️
        </div>

        <h2>
          Loading trip details...
        </h2>

        <p style={styles.loadingText}>
          Please wait a moment.
        </p>

      </div>

    );

  }

  // =====================================================
  // ERROR
  // =====================================================

  if (error) {

    return (

      <div style={styles.center}>

        <div style={styles.errorIcon}>
          ⚠️
        </div>

        <p style={styles.error}>
          {error}
        </p>

        <button
          style={styles.primaryButton}
          onClick={() =>
            navigate("/trips")
          }
        >
          ← Back to My Trips
        </button>

      </div>

    );

  }

  // =====================================================
  // TRIP NOT FOUND
  // =====================================================

  if (!trip) {

    return (

      <div style={styles.center}>

        <div style={styles.errorIcon}>
          🔍
        </div>

        <h2>
          Trip not found
        </h2>

        <button
          style={styles.primaryButton}
          onClick={() =>
            navigate("/trips")
          }
        >
          ← Back to My Trips
        </button>

      </div>

    );

  }

  // =====================================================
  // DESTINATION
  // =====================================================

  const destinationName =
    trip.destination?.name ||
    trip.destination?.destinationName ||
    "Unknown Destination";

  // =====================================================
  // RETURN UI
  // =====================================================

  return (

    <div style={styles.page}>

      {/* =================================================
          HEADER
      ================================================= */}

      <header style={styles.header}>

        <div>

          <h1 style={styles.logo}>
            TripNest
          </h1>

          <p style={styles.subtitle}>
            Trip Details
          </p>

        </div>

        <div style={styles.headerButtons}>

          <button
            style={styles.secondaryButton}
            onClick={() =>
              navigate("/trips")
            }
          >
            🧳 My Trips
          </button>

          <button
            style={styles.secondaryButton}
            onClick={() =>
              navigate("/dashboard")
            }
          >
            🏠 Dashboard
          </button>

        </div>

      </header>


      {/* =================================================
          MAIN
      ================================================= */}

      <main style={styles.container}>

        {/* =================================================
            TOP BAR
        ================================================= */}

        <div style={styles.topBar}>

          <div>

            <h2 style={styles.title}>
              {destinationName}
            </h2>

            <p style={styles.subtitleText}>
              Complete information about your trip
            </p>

          </div>

          <span style={styles.status}>
            {trip.status || "PLANNED"}
          </span>

        </div>


        {/* =================================================
            TRIP INFORMATION
        ================================================= */}

        <div style={styles.card}>

          <div style={styles.destinationHeader}>

            <div style={styles.destinationIcon}>
              ✈️
            </div>

            <div>

              <h2 style={styles.destinationTitle}>
                {destinationName}
              </h2>

              <p style={styles.tripId}>
                Trip ID: #{trip.id}
              </p>

            </div>

          </div>


          <div style={styles.divider} />


          <h3 style={styles.sectionTitle}>
            Trip Information
          </h3>


          <div style={styles.infoGrid}>

            <div style={styles.infoBox}>

              <span style={styles.label}>
                📍 Destination
              </span>

              <strong style={styles.value}>
                {destinationName}
              </strong>

            </div>


            <div style={styles.infoBox}>

              <span style={styles.label}>
                📅 Start Date
              </span>

              <strong style={styles.value}>
                {formatDate(
                  trip.startDate
                )}
              </strong>

            </div>


            <div style={styles.infoBox}>

              <span style={styles.label}>
                📅 End Date
              </span>

              <strong style={styles.value}>
                {formatDate(
                  trip.endDate
                )}
              </strong>

            </div>


            <div style={styles.infoBox}>

              <span style={styles.label}>
                👥 Travelers
              </span>

              <strong style={styles.value}>
                {trip.travelers ??
                  "Not available"}
              </strong>

            </div>


            <div style={styles.infoBox}>

              <span style={styles.label}>
                💰 Trip Budget
              </span>

              <strong style={styles.value}>
                {trip.budget !== null &&
                trip.budget !== undefined
                  ? formatMoney(
                      trip.budget
                    )
                  : "Not available"}
              </strong>

            </div>


            <div style={styles.infoBox}>

              <span style={styles.label}>
                📌 Status
              </span>

              <strong style={styles.value}>
                {trip.status ||
                  "PLANNED"}
              </strong>

            </div>

          </div>


          <div style={styles.actions}>

            <button
              style={styles.editButton}
              onClick={() =>
                navigate(
                  `/trips/${trip.id}/edit`
                )
              }
            >
              ✏️ Edit Trip
            </button>


            <button
              style={styles.deleteButton}
              onClick={handleDelete}
            >
              🗑️ Delete Trip
            </button>

          </div>

        </div>


        {/* =================================================
            BUDGET SECTION
        ================================================= */}

        <div style={styles.card}>

          <div style={styles.sectionHeader}>

            <div>

              <h3 style={styles.sectionTitle}>
                💰 Trip Budget
              </h3>

              <p style={styles.sectionDescription}>
                Manage your planned trip budget
              </p>

            </div>

            <button
              style={styles.darkButton}
              onClick={
                openBudgetModal
              }
            >
              {budget
                ? "✏️ Edit Budget"
                : "＋ Create Budget"}
            </button>

          </div>


          {loadingBudget ? (

            <div style={styles.loadingBox}>
              ⏳ Loading budget...
            </div>

          ) : !budget ? (

            <div style={styles.emptyBox}>

              <div style={styles.emptyIcon}>
                💰
              </div>

              <h3>
                No budget created yet
              </h3>

              <p>
                Create a budget to start
                tracking your expenses.
              </p>

              <button
                style={styles.darkButton}
                onClick={
                  openBudgetModal
                }
              >
                ＋ Create Budget
              </button>

            </div>

          ) : (

            <>

              <div style={styles.budgetCards}>

                <div
                  style={{
                    ...styles.moneyCard,
                    background:
                      "#eef2ff",
                  }}
                >

                  <span style={styles.moneyLabel}>
                    Total Budget
                  </span>

                  <strong
                    style={styles.moneyValue}
                  >
                    {formatMoney(
                      budget.totalBudget
                    )}
                  </strong>

                </div>


                <div
                  style={{
                    ...styles.moneyCard,
                    background:
                      "#ecfdf5",
                  }}
                >

                  <span style={styles.moneyLabel}>
                    Total Expenses
                  </span>

                  <strong
                    style={{
                      ...styles.moneyValue,
                      color: "#047857",
                    }}
                  >
                    {formatMoney(
                      totalExpenses
                    )}
                  </strong>

                </div>


                <div
                  style={{
                    ...styles.moneyCard,
                    background:
                      remainingBudget >= 0
                        ? "#eff6ff"
                        : "#fef2f2",
                  }}
                >

                  <span style={styles.moneyLabel}>
                    Remaining Budget
                  </span>

                  <strong
                    style={{
                      ...styles.moneyValue,
                      color:
                        remainingBudget >= 0
                          ? "#2563eb"
                          : "#dc2626",
                    }}
                  >
                    {formatMoney(
                      remainingBudget
                    )}
                  </strong>

                </div>

              </div>


              {/* CATEGORY BUDGET */}

              <h4 style={styles.subHeading}>
                Budget Distribution
              </h4>

              <div style={styles.budgetGrid}>

                <BudgetItem
                  icon="🏨"
                  label="Accommodation"
                  value={
                    budget.accommodationBudget
                  }
                />

                <BudgetItem
                  icon="🍔"
                  label="Food"
                  value={
                    budget.foodBudget
                  }
                />

                <BudgetItem
                  icon="🚗"
                  label="Transportation"
                  value={
                    budget.transportBudget
                  }
                />

                <BudgetItem
                  icon="🎯"
                  label="Activities"
                  value={
                    budget.activityBudget
                  }
                />

                <BudgetItem
                  icon="🛍️"
                  label="Miscellaneous"
                  value={
                    budget.miscellaneousBudget
                  }
                />

              </div>

            </>

          )}

        </div>


        {/* =================================================
            EXPENSE SECTION
        ================================================= */}

        <div style={styles.card}>

          <div style={styles.sectionHeader}>

            <div>

              <h3 style={styles.sectionTitle}>
                💸 Expenses
              </h3>

              <p style={styles.sectionDescription}>
                Track where your trip money is going
              </p>

            </div>

            <button
              style={styles.darkButton}
              onClick={
                openAddExpenseModal
              }
            >
              ＋ Add Expense
            </button>

          </div>


          {loadingExpenses ? (

            <div style={styles.loadingBox}>
              ⏳ Loading expenses...
            </div>

          ) : (

            <>

              {/* EXPENSE SUMMARY */}

              <div style={styles.expenseSummary}>

                <div style={styles.summaryBox}>

                  <span>
                    Total Expenses
                  </span>

                  <strong>
                    {formatMoney(
                      totalExpenses
                    )}
                  </strong>

                </div>


                <div style={styles.summaryBox}>

                  <span>
                    Remaining
                  </span>

                  <strong
                    style={{
                      color:
                        remainingBudget >= 0
                          ? "#16a34a"
                          : "#dc2626",
                    }}
                  >
                    {formatMoney(
                      remainingBudget
                    )}
                  </strong>

                </div>


                <div style={styles.summaryBox}>

                  <span>
                    Number of Expenses
                  </span>

                  <strong>
                    {expenses.length}
                  </strong>

                </div>

              </div>


              {/* EXPENSE LIST + CHART */}

              <div style={styles.expenseLayout}>

                {/* EXPENSE LIST */}

                <div>

                  <h4 style={styles.subHeading}>
                    Expense History
                  </h4>

                  {expenses.length === 0 ? (

                    <div style={styles.emptyBox}>

                      <div style={styles.emptyIcon}>
                        💸
                      </div>

                      <h3>
                        No expenses yet
                      </h3>

                      <p>
                        Add your first expense
                        to start tracking.
                      </p>

                      <button
                        style={styles.darkButton}
                        onClick={
                          openAddExpenseModal
                        }
                      >
                        ＋ Add Expense
                      </button>

                    </div>

                  ) : (

                    <div style={styles.expenseList}>

                      {expenses.map(
                        (expense) => (

                          <div
                            key={
                              expense.id
                            }
                            style={
                              styles.expenseItem
                            }
                          >

                            <div
                              style={
                                styles.expenseIcon
                              }
                            >
                              {getCategoryIcon(
                                expense.category
                              )}
                            </div>


                            <div
                              style={
                                styles.expenseMain
                              }
                            >

                              <div
                                style={
                                  styles.expenseTop
                                }
                              >

                                <div>

                                  <strong
                                    style={
                                      styles.expenseCategory
                                    }
                                  >
                                    {
                                      expense.category
                                    }
                                  </strong>

                                  <p
                                    style={
                                      styles.expenseDate
                                    }
                                  >
                                    📅{" "}
                                    {formatDate(
                                      expense.expenseDate
                                    )}
                                  </p>

                                </div>

                                <strong
                                  style={
                                    styles.expenseAmount
                                  }
                                >
                                  {formatMoney(
                                    expense.amount
                                  )}
                                </strong>

                              </div>


                              {expense.receiptLink && (

                                <a
                                  href={
                                    expense.receiptLink
                                  }
                                  target="_blank"
                                  rel="noreferrer"
                                  style={
                                    styles.receiptLink
                                  }
                                >
                                  🔗 View Receipt
                                </a>

                              )}


                              <div
                                style={
                                  styles.expenseActions
                                }
                              >

                                <button
                                  style={
                                    styles.smallEditButton
                                  }
                                  onClick={() =>
                                    openEditExpenseModal(
                                      expense
                                    )
                                  }
                                >
                                  ✏️ Edit
                                </button>


                                <button
                                  style={
                                    styles.smallDeleteButton
                                  }
                                  onClick={() =>
                                    handleDeleteExpense(
                                      expense.id
                                    )
                                  }
                                  disabled={
                                    deletingExpense ===
                                    expense.id
                                  }
                                >
                                  {deletingExpense ===
                                  expense.id
                                    ? "Deleting..."
                                    : "🗑️ Delete"}
                                </button>

                              </div>

                            </div>

                          </div>

                        )
                      )}

                    </div>

                  )}

                </div>


                {/* CHART */}

                <div>

                  <h4 style={styles.subHeading}>
                    Spending by Category
                  </h4>

                  <div
                    style={
                      styles.chartContainer
                    }
                  >

                    {categorySummary.length ===
                    0 ? (

                      <div
                        style={
                          styles.chartEmpty
                        }
                      >
                        📊
                        <p>
                          No category data yet.
                        </p>
                      </div>

                    ) : (

                      <Doughnut
                        data={chartData}
                        options={
                          chartOptions
                        }
                      />

                    )}

                  </div>

                </div>

              </div>

            </>

          )}

        </div>


        {/* =================================================
            ITINERARY
        ================================================= */}

        <div style={styles.card}>

          <div
            style={
              styles.itineraryHeader
            }
          >

            <div
              style={
                styles.itineraryTitleRow
              }
            >

              <div
                style={
                  styles.itineraryMainIcon
                }
              >
                🗓️
              </div>

              <div>

                <h3
                  style={
                    styles.itineraryTitle
                  }
                >
                  Itinerary
                </h3>

                <p
                  style={
                    styles.itinerarySubtitle
                  }
                >
                  Your planned activities and journey schedule
                </p>

              </div>

            </div>


            <button
              style={
                styles.addItineraryButton
              }
              onClick={
                openAddModal
              }
            >
              ＋ Add Activity
            </button>

          </div>


          {loadingItinerary ? (

            <div
              style={
                styles.itineraryEmpty
              }
            >

              <div
                style={
                  styles.emptyItineraryIcon
                }
              >
                ⏳
              </div>

              <h4>
                Loading itinerary...
              </h4>

              <p>
                Please wait while we load your travel plans.
              </p>

            </div>

          ) : itineraries.length ===
            0 ? (

            <div
              style={
                styles.itineraryEmpty
              }
            >

              <div
                style={
                  styles.emptyItineraryIcon
                }
              >
                🗓️
              </div>

              <h4>
                No itinerary added yet
              </h4>

              <p>
                Start planning your trip by adding your first activity.
              </p>

              <button
                style={
                  styles.addItineraryButton
                }
                onClick={
                  openAddModal
                }
              >
                ＋ Add Activity
              </button>

            </div>

          ) : (

            <div
              style={
                styles.itineraryTimeline
              }
            >

              {itineraries.map(
                (item, index) => (

                  <div
                    style={
                      styles.itineraryItem
                    }
                    key={
                      item.id ||
                      index
                    }
                  >

                    <div
                      style={
                        styles.timeline
                      }
                    >

                      <div
                        style={
                          styles.timelineDot
                        }
                      >
                        {item.dayNumber ||
                          index + 1}
                      </div>

                      {index !==
                        itineraries.length -
                          1 && (

                        <div
                          style={
                            styles.timelineLine
                          }
                        />

                      )}

                    </div>


                    <div
                      style={
                        styles.dayCard
                      }
                    >

                      <div
                        style={
                          styles.dayHeader
                        }
                      >

                        <div>

                          <span
                            style={
                              styles.dayLabel
                            }
                          >
                            DAY{" "}
                            {item.dayNumber ||
                              index + 1}
                          </span>

                          <h4
                            style={
                              styles.dayTitle
                            }
                          >
                            {item.title ||
                              "Untitled Activity"}
                          </h4>

                        </div>


                        {item.date && (

                          <span
                            style={
                              styles.dateBadge
                            }
                          >
                            📅{" "}
                            {formatDate(
                              item.date
                            )}
                          </span>

                        )}

                      </div>


                      {item.description && (

                        <p
                          style={
                            styles.activityDescription
                          }
                        >
                          {item.description}
                        </p>

                      )}


                      <div
                        style={
                          styles.activityActions
                        }
                      >

                        <button
                          style={
                            styles.editActivityButton
                          }
                          onClick={() =>
                            openEditModal(
                              item
                            )
                          }
                          disabled={
                            savingItinerary
                          }
                        >
                          ✏️ Edit
                        </button>


                        <button
                          style={
                            styles.deleteActivityButton
                          }
                          onClick={() =>
                            handleDeleteItinerary(
                              item.id
                            )
                          }
                          disabled={
                            deletingItinerary ===
                            item.id
                          }
                        >
                          {deletingItinerary ===
                          item.id
                            ? "Deleting..."
                            : "🗑️ Delete"}
                        </button>

                      </div>

                    </div>

                  </div>

                )
              )}

            </div>

          )}

        </div>


        {/* =================================================
            INFO
        ================================================= */}

        <div style={styles.infoBanner}>

          <div
            style={
              styles.infoBannerIcon
            }
          >
            💡
          </div>

          <div>

            <h4
              style={
                styles.infoBannerTitle
              }
            >
              Plan your perfect journey
            </h4>

            <p
              style={
                styles.infoBannerText
              }
            >
              Add activities and track your expenses to keep your trip organized.
            </p>

          </div>

        </div>

      </main>


      {/* =================================================
          BUDGET MODAL
      ================================================= */}

      {showBudgetModal && (

        <div
          style={
            styles.modalOverlay
          }
        >

          <div
            style={
              styles.modal
            }
          >

            <div
              style={
                styles.modalHeader
              }
            >

              <div>

                <h2
                  style={
                    styles.modalTitle
                  }
                >
                  {budget
                    ? "Edit Budget"
                    : "Create Budget"}
                </h2>

                <p
                  style={
                    styles.modalSubtitle
                  }
                >
                  Set the budget for your trip.
                </p>

              </div>


              <button
                style={
                  styles.closeButton
                }
                onClick={
                  closeBudgetModal
                }
              >
                ×
              </button>

            </div>


            <form
              onSubmit={
                handleSaveBudget
              }
            >

              <div
                style={
                  styles.formGroup
                }
              >

                <label
                  style={
                    styles.formLabel
                  }
                >
                  Total Budget *
                </label>

                <input
                  type="number"
                  name="totalBudget"
                  min="1"
                  step="0.01"
                  value={
                    budgetForm.totalBudget
                  }
                  onChange={
                    handleBudgetChange
                  }
                  style={
                    styles.input
                  }
                  placeholder="e.g. 50000"
                  required
                />

              </div>


              <div
                style={
                  styles.formRow
                }
              >

                <BudgetInput
                  name="accommodationBudget"
                  label="🏨 Accommodation"
                  value={
                    budgetForm.accommodationBudget
                  }
                  onChange={
                    handleBudgetChange
                  }
                />

                <BudgetInput
                  name="foodBudget"
                  label="🍔 Food"
                  value={
                    budgetForm.foodBudget
                  }
                  onChange={
                    handleBudgetChange
                  }
                />

                <BudgetInput
                  name="transportBudget"
                  label="🚗 Transport"
                  value={
                    budgetForm.transportBudget
                  }
                  onChange={
                    handleBudgetChange
                  }
                />

                <BudgetInput
                  name="activityBudget"
                  label="🎯 Activities"
                  value={
                    budgetForm.activityBudget
                  }
                  onChange={
                    handleBudgetChange
                  }
                />

                <BudgetInput
                  name="miscellaneousBudget"
                  label="🛍️ Miscellaneous"
                  value={
                    budgetForm.miscellaneousBudget
                  }
                  onChange={
                    handleBudgetChange
                  }
                />

              </div>


              <div
                style={
                  styles.modalActions
                }
              >

                <button
                  type="button"
                  style={
                    styles.cancelButton
                  }
                  onClick={
                    closeBudgetModal
                  }
                >
                  Cancel
                </button>


                <button
                  type="submit"
                  style={
                    styles.saveButton
                  }
                  disabled={
                    savingBudget
                  }
                >
                  {savingBudget
                    ? "Saving..."
                    : budget
                    ? "✓ Update Budget"
                    : "💾 Save Budget"}
                </button>

              </div>

            </form>

          </div>

        </div>

      )}


      {/* =================================================
          EXPENSE MODAL
      ================================================= */}

      {showExpenseModal && (

        <div
          style={
            styles.modalOverlay
          }
        >

          <div
            style={
              styles.modal
            }
          >

            <div
              style={
                styles.modalHeader
              }
            >

              <div>

                <h2
                  style={
                    styles.modalTitle
                  }
                >
                  {editingExpense
                    ? "Edit Expense"
                    : "Add Expense"}
                </h2>

                <p
                  style={
                    styles.modalSubtitle
                  }
                >
                  Record your trip spending.
                </p>

              </div>


              <button
                style={
                  styles.closeButton
                }
                onClick={
                  closeExpenseModal
                }
                disabled={
                  savingExpense
                }
              >
                ×
              </button>

            </div>


            <form
              onSubmit={
                handleSaveExpense
              }
            >

              <div
                style={
                  styles.formGroup
                }
              >

                <label
                  style={
                    styles.formLabel
                  }
                >
                  Category *
                </label>

                <select
                  name="category"
                  value={
                    expenseForm.category
                  }
                  onChange={
                    handleExpenseChange
                  }
                  style={
                    styles.input
                  }
                  required
                >

                  <option value="">
                    Select category
                  </option>

                  <option value="Transportation">
                    🚗 Transportation
                  </option>

                  <option value="Hotel">
                    🏨 Hotel
                  </option>

                  <option value="Food">
                    🍔 Food
                  </option>

                  <option value="Shopping">
                    🛍️ Shopping
                  </option>

                  <option value="Entertainment">
                    🎬 Entertainment
                  </option>

                  <option value="Miscellaneous">
                    📦 Miscellaneous
                  </option>

                </select>

              </div>


              <div
                style={
                  styles.formRow
                }
              >

                <div
                  style={
                    styles.formGroup
                  }
                >

                  <label
                    style={
                      styles.formLabel
                    }
                  >
                    Amount *
                  </label>

                  <input
                    type="number"
                    name="amount"
                    min="0.01"
                    step="0.01"
                    value={
                      expenseForm.amount
                    }
                    onChange={
                      handleExpenseChange
                    }
                    style={
                      styles.input
                    }
                    placeholder="e.g. 1500"
                    required
                  />

                </div>


                <div
                  style={
                    styles.formGroup
                  }
                >

                  <label
                    style={
                      styles.formLabel
                    }
                  >
                    Date *
                  </label>

                  <input
                    type="date"
                    name="expenseDate"
                    value={
                      expenseForm.expenseDate
                    }
                    onChange={
                      handleExpenseChange
                    }
                    style={
                      styles.input
                    }
                    required
                  />

                </div>

              </div>


              <div
                style={
                  styles.formGroup
                }
              >

                <label
                  style={
                    styles.formLabel
                  }
                >
                  Receipt Link
                </label>

                <input
                  type="url"
                  name="receiptLink"
                  value={
                    expenseForm.receiptLink
                  }
                  onChange={
                    handleExpenseChange
                  }
                  style={
                    styles.input
                  }
                  placeholder="https://example.com/receipt"
                />

              </div>


              <div
                style={
                  styles.expenseFormInfo
                }
              >
                💡 Payer:{" "}
                {localStorage.getItem(
                  "userName"
                ) || "Logged-in user"}
              </div>


              <div
                style={
                  styles.modalActions
                }
              >

                <button
                  type="button"
                  style={
                    styles.cancelButton
                  }
                  onClick={
                    closeExpenseModal
                  }
                  disabled={
                    savingExpense
                  }
                >
                  Cancel
                </button>


                <button
                  type="submit"
                  style={
                    styles.saveButton
                  }
                  disabled={
                    savingExpense
                  }
                >
                  {savingExpense
                    ? editingExpense
                      ? "Updating..."
                      : "Saving..."
                    : editingExpense
                    ? "✓ Update Expense"
                    : "💾 Save Expense"}
                </button>

              </div>

            </form>

          </div>

        </div>

      )}


      {/* =================================================
          ITINERARY MODAL
      ================================================= */}

      {showModal && (

        <div
          style={
            styles.modalOverlay
          }
        >

          <div
            style={
              styles.modal
            }
          >

            <div
              style={
                styles.modalHeader
              }
            >

              <div>

                <h2
                  style={
                    styles.modalTitle
                  }
                >
                  {editingItinerary
                    ? "Edit Activity"
                    : "Add Activity"}
                </h2>

                <p
                  style={
                    styles.modalSubtitle
                  }
                >
                  {editingItinerary
                    ? "Update your itinerary activity."
                    : "Add a new activity to your trip itinerary."}
                </p>

              </div>


              <button
                style={
                  styles.closeButton
                }
                onClick={
                  closeModal
                }
                disabled={
                  savingItinerary
                }
              >
                ×
              </button>

            </div>


            <form
              onSubmit={
                editingItinerary
                  ? handleUpdateItinerary
                  : handleAddItinerary
              }
            >

              <div
                style={
                  styles.formRow
                }
              >

                <div
                  style={
                    styles.formGroup
                  }
                >

                  <label
                    style={
                      styles.formLabel
                    }
                  >
                    Day Number *
                  </label>

                  <input
                    type="number"
                    name="dayNumber"
                    min="1"
                    value={
                      formData.dayNumber
                    }
                    onChange={
                      handleInputChange
                    }
                    style={
                      styles.input
                    }
                    placeholder="e.g. 1"
                    required
                  />

                </div>


                <div
                  style={
                    styles.formGroup
                  }
                >

                  <label
                    style={
                      styles.formLabel
                    }
                  >
                    Date
                  </label>

                  <input
                    type="date"
                    name="date"
                    value={
                      formData.date
                    }
                    onChange={
                      handleInputChange
                    }
                    style={
                      styles.input
                    }
                  />

                </div>

              </div>


              <div
                style={
                  styles.formGroup
                }
              >

                <label
                  style={
                    styles.formLabel
                  }
                >
                  Activity Title *
                </label>

                <input
                  type="text"
                  name="title"
                  value={
                    formData.title
                  }
                  onChange={
                    handleInputChange
                  }
                  style={
                    styles.input
                  }
                  placeholder="e.g. Visit Taj Mahal"
                  maxLength="100"
                  required
                />

              </div>


              <div
                style={
                  styles.formGroup
                }
              >

                <label
                  style={
                    styles.formLabel
                  }
                >
                  Description
                </label>

                <textarea
                  name="description"
                  value={
                    formData.description
                  }
                  onChange={
                    handleInputChange
                  }
                  style={
                    styles.textarea
                  }
                  placeholder="Describe your activity..."
                  rows="4"
                  maxLength="1000"
                />

                <small
                  style={
                    styles.characterCount
                  }
                >
                  {
                    formData.description
                      .length
                  }
                  /1000
                </small>

              </div>


              <div
                style={
                  styles.modalActions
                }
              >

                <button
                  type="button"
                  style={
                    styles.cancelButton
                  }
                  onClick={
                    closeModal
                  }
                  disabled={
                    savingItinerary
                  }
                >
                  Cancel
                </button>


                <button
                  type="submit"
                  style={
                    styles.saveButton
                  }
                  disabled={
                    savingItinerary
                  }
                >
                  {savingItinerary
                    ? editingItinerary
                      ? "Updating..."
                      : "Saving..."
                    : editingItinerary
                    ? "✓ Update Activity"
                    : "💾 Save Activity"}
                </button>

              </div>

            </form>

          </div>

        </div>

      )}

    </div>

  );

}


// =====================================================
// BUDGET INPUT COMPONENT
// =====================================================

function BudgetInput({
  name,
  label,
  value,
  onChange,
}) {

  return (

    <div
      style={
        styles.formGroup
      }
    >

      <label
        style={
          styles.formLabel
        }
      >
        {label}
      </label>

      <input
        type="number"
        name={name}
        min="0"
        step="0.01"
        value={value}
        onChange={onChange}
        style={styles.input}
        placeholder="0"
      />

    </div>

  );

}


// =====================================================
// BUDGET ITEM
// =====================================================

function BudgetItem({
  icon,
  label,
  value,
}) {

  return (

    <div
      style={
        styles.budgetItem
      }
    >

      <span style={styles.budgetItemIcon}>
        {icon}
      </span>

      <div>

        <span
          style={
            styles.budgetItemLabel
          }
        >
          {label}
        </span>

        <strong
          style={
            styles.budgetItemValue
          }
        >
          {value
            ? `₹${Number(value).toLocaleString(
                "en-IN"
              )}`
            : "₹0"}
        </strong>

      </div>

    </div>

  );

}


// =====================================================
// CATEGORY ICON
// =====================================================

function getCategoryIcon(category) {

  switch (category) {

    case "Transportation":
      return "🚗";

    case "Hotel":
      return "🏨";

    case "Food":
      return "🍔";

    case "Shopping":
      return "🛍️";

    case "Entertainment":
      return "🎬";

    case "Miscellaneous":
      return "📦";

    default:
      return "💸";

  }

}


// =====================================================
// STYLES
// =====================================================

const styles = {

  page: {
    minHeight: "100vh",
    background: "#f5f7fb",
    color: "#111827",
  },

  header: {
    background: "#ffffff",
    borderBottom:
      "1px solid #e5e7eb",
    padding: "20px 5%",
    display: "flex",
    justifyContent:
      "space-between",
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
    flexWrap: "wrap",
  },

  secondaryButton: {
    border:
      "1px solid #d1d5db",
    background: "#ffffff",
    color: "#111827",
    padding:
      "10px 17px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  container: {
    width: "90%",
    maxWidth: "1100px",
    margin: "0 auto",
    padding:
      "40px 0 60px",
  },

  topBar: {
    display: "flex",
    justifyContent:
      "space-between",
    alignItems: "center",
    gap: "20px",
    marginBottom: "25px",
    flexWrap: "wrap",
  },

  title: {
    margin: 0,
    fontSize: "32px",
  },

  subtitleText: {
    marginTop: "7px",
    color: "#6b7280",
  },

  status: {
    background: "#e5e7eb",
    padding:
      "9px 16px",
    borderRadius: "20px",
    fontSize: "13px",
    fontWeight: "700",
  },

  card: {
    background: "#ffffff",
    border:
      "1px solid #e5e7eb",
    borderRadius: "16px",
    padding: "30px",
    marginBottom: "22px",
    boxShadow:
      "0 5px 20px rgba(0,0,0,0.05)",
  },

  destinationHeader: {
    display: "flex",
    alignItems: "center",
    gap: "18px",
  },

  destinationIcon: {
    width: "65px",
    height: "65px",
    borderRadius: "14px",
    background: "#eef2ff",
    display: "flex",
    alignItems: "center",
    justifyContent:
      "center",
    fontSize: "32px",
  },

  destinationTitle: {
    margin: 0,
    fontSize: "25px",
  },

  tripId: {
    margin:
      "6px 0 0",
    color: "#6b7280",
    fontSize: "14px",
  },

  divider: {
    height: "1px",
    background: "#e5e7eb",
    margin:
      "28px 0",
  },

  sectionTitle: {
    margin:
      "0 0 8px",
    fontSize: "21px",
  },

  sectionDescription: {
    margin: 0,
    color: "#6b7280",
    fontSize: "14px",
  },

  sectionHeader: {
    display: "flex",
    justifyContent:
      "space-between",
    alignItems: "center",
    gap: "20px",
    marginBottom: "25px",
    flexWrap: "wrap",
  },

  infoGrid: {
    display: "grid",
    gridTemplateColumns:
      "repeat(auto-fit, minmax(230px, 1fr))",
    gap: "15px",
  },

  infoBox: {
    background: "#f9fafb",
    border:
      "1px solid #e5e7eb",
    borderRadius: "12px",
    padding: "18px",
    display: "flex",
    flexDirection: "column",
    gap: "8px",
  },

  label: {
    color: "#6b7280",
    fontSize: "14px",
  },

  value: {
    fontSize: "17px",
  },

  actions: {
    display: "flex",
    gap: "12px",
    marginTop: "30px",
    flexWrap: "wrap",
  },

  editButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding:
      "12px 20px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  deleteButton: {
    border: "none",
    background: "#ef4444",
    color: "#ffffff",
    padding:
      "12px 20px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

  darkButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding:
      "11px 18px",
    borderRadius: "9px",
    cursor: "pointer",
    fontWeight: "600",
  },

  loadingBox: {
    textAlign: "center",
    padding: "40px",
    color: "#6b7280",
  },

  emptyBox: {
    textAlign: "center",
    padding:
      "35px 20px",
    background: "#f9fafb",
    border:
      "1px dashed #d1d5db",
    borderRadius: "14px",
  },

  emptyIcon: {
    fontSize: "42px",
    marginBottom: "10px",
  },

  budgetCards: {
    display: "grid",
    gridTemplateColumns:
      "repeat(auto-fit, minmax(220px, 1fr))",
    gap: "15px",
  },

  moneyCard: {
    padding: "22px",
    borderRadius: "14px",
    display: "flex",
    flexDirection: "column",
    gap: "8px",
  },

  moneyLabel: {
    color: "#6b7280",
    fontSize: "14px",
  },

  moneyValue: {
    fontSize: "25px",
    color: "#111827",
  },

  subHeading: {
    margin:
      "28px 0 15px",
    fontSize: "17px",
  },

  budgetGrid: {
    display: "grid",
    gridTemplateColumns:
      "repeat(auto-fit, minmax(200px, 1fr))",
    gap: "12px",
  },

  budgetItem: {
    display: "flex",
    alignItems: "center",
    gap: "12px",
    background: "#f9fafb",
    border:
      "1px solid #e5e7eb",
    borderRadius: "12px",
    padding: "14px",
  },

  budgetItemIcon: {
    fontSize: "24px",
  },

  budgetItemLabel: {
    display: "block",
    color: "#6b7280",
    fontSize: "13px",
  },

  budgetItemValue: {
    display: "block",
    marginTop: "4px",
    fontSize: "16px",
  },

  expenseSummary: {
    display: "grid",
    gridTemplateColumns:
      "repeat(auto-fit, minmax(180px, 1fr))",
    gap: "12px",
    marginBottom: "25px",
  },

  summaryBox: {
    background: "#f9fafb",
    border:
      "1px solid #e5e7eb",
    borderRadius: "12px",
    padding: "16px",
    display: "flex",
    flexDirection: "column",
    gap: "7px",
  },

  expenseLayout: {
    display: "grid",
    gridTemplateColumns:
      "1.5fr 1fr",
    gap: "30px",
  },

  expenseList: {
    display: "flex",
    flexDirection: "column",
    gap: "12px",
  },

  expenseItem: {
    display: "flex",
    gap: "14px",
    background: "#f9fafb",
    border:
      "1px solid #e5e7eb",
    borderRadius: "12px",
    padding: "16px",
  },

  expenseIcon: {
    width: "45px",
    height: "45px",
    borderRadius: "10px",
    background: "#eef2ff",
    display: "flex",
    alignItems: "center",
    justifyContent:
      "center",
    fontSize: "22px",
    flexShrink: 0,
  },

  expenseMain: {
    flex: 1,
    minWidth: 0,
  },

  expenseTop: {
    display: "flex",
    justifyContent:
      "space-between",
    gap: "15px",
  },

  expenseCategory: {
    fontSize: "16px",
  },

  expenseDate: {
    margin:
      "5px 0 0",
    color: "#6b7280",
    fontSize: "12px",
  },

  expenseAmount: {
    fontSize: "18px",
    whiteSpace: "nowrap",
  },

  receiptLink: {
    display: "inline-block",
    marginTop: "8px",
    fontSize: "13px",
    color: "#2563eb",
    textDecoration:
      "none",
  },

  expenseActions: {
    display: "flex",
    gap: "8px",
    marginTop: "12px",
    paddingTop: "10px",
    borderTop:
      "1px solid #e5e7eb",
  },

  smallEditButton: {
    border:
      "1px solid #d1d5db",
    background: "#111827",
    color: "#ffffff",
    padding:
      "7px 11px",
    borderRadius: "7px",
    cursor: "pointer",
    fontSize: "12px",
    fontWeight: "600",
  },

  smallDeleteButton: {
    border:
      "1px solid #fecaca",
    background: "#ef4444",
    color: "#ffffff",
    padding:
      "7px 11px",
    borderRadius: "7px",
    cursor: "pointer",
    fontSize: "12px",
    fontWeight: "600",
  },

  chartContainer: {
    height: "330px",
    background: "#f9fafb",
    border:
      "1px solid #e5e7eb",
    borderRadius: "14px",
    padding: "20px",
  },

  chartEmpty: {
    height: "100%",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    justifyContent:
      "center",
    color: "#9ca3af",
    fontSize: "35px",
  },

  expenseFormInfo: {
    background: "#eef2ff",
    border:
      "1px solid #c7d2fe",
    borderRadius: "9px",
    padding: "12px",
    marginBottom: "15px",
    fontSize: "13px",
    color: "#3730a3",
  },

  itineraryHeader: {
    display: "flex",
    justifyContent:
      "space-between",
    alignItems: "center",
    gap: "20px",
    marginBottom: "30px",
    flexWrap: "wrap",
  },

  itineraryTitleRow: {
    display: "flex",
    alignItems: "center",
    gap: "14px",
  },

  itineraryMainIcon: {
    width: "48px",
    height: "48px",
    borderRadius: "12px",
    background: "#eef2ff",
    display: "flex",
    alignItems: "center",
    justifyContent:
      "center",
    fontSize: "24px",
  },

  itineraryTitle: {
    margin: 0,
    fontSize: "22px",
  },

  itinerarySubtitle: {
    margin:
      "5px 0 0",
    color: "#6b7280",
    fontSize: "14px",
  },

  addItineraryButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding:
      "11px 18px",
    borderRadius: "9px",
    cursor: "pointer",
    fontWeight: "600",
  },

  itineraryEmpty: {
    textAlign: "center",
    padding:
      "45px 20px",
    background: "#f9fafb",
    border:
      "1px dashed #d1d5db",
    borderRadius: "14px",
  },

  emptyItineraryIcon: {
    fontSize: "45px",
    marginBottom: "10px",
  },

  itineraryTimeline: {
    display: "flex",
    flexDirection: "column",
  },

  itineraryItem: {
    display: "flex",
    gap: "18px",
  },

  timeline: {
    width: "35px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },

  timelineDot: {
    width: "35px",
    height: "35px",
    borderRadius: "50%",
    background: "#111827",
    color: "#ffffff",
    display: "flex",
    alignItems: "center",
    justifyContent:
      "center",
    fontWeight: "700",
    fontSize: "14px",
    flexShrink: 0,
  },

  timelineLine: {
    width: "2px",
    flex: 1,
    background: "#d1d5db",
    marginTop: "6px",
    marginBottom: "6px",
  },

  dayCard: {
    flex: 1,
    background: "#f9fafb",
    border:
      "1px solid #e5e7eb",
    borderRadius: "14px",
    padding: "20px",
    marginBottom: "20px",
  },

  dayHeader: {
    display: "flex",
    justifyContent:
      "space-between",
    alignItems: "flex-start",
    gap: "15px",
    flexWrap: "wrap",
  },

  dayLabel: {
    fontSize: "12px",
    fontWeight: "800",
    color: "#6b7280",
    letterSpacing: "1px",
  },

  dayTitle: {
    margin:
      "5px 0 0",
    fontSize: "19px",
  },

  dateBadge: {
    background: "#eef2ff",
    color: "#3730a3",
    padding:
      "7px 11px",
    borderRadius: "20px",
    fontSize: "12px",
    fontWeight: "700",
  },

  activityDescription: {
    margin:
      "15px 0",
    color: "#4b5563",
    lineHeight: "1.6",
    fontSize: "14px",
  },

  activityActions: {
    display: "flex",
    gap: "8px",
    marginTop: "18px",
    paddingTop: "15px",
    borderTop:
      "1px solid #e5e7eb",
  },

  editActivityButton: {
    border:
      "1px solid #d1d5db",
    color: "#ffffff",
    background: "#111827",
    padding:
      "8px 13px",
    borderRadius: "7px",
    cursor: "pointer",
    fontSize: "13px",
    fontWeight: "600",
  },

  deleteActivityButton: {
    border:
      "1px solid #fecaca",
    background: "#ef4444",
    color: "#ffffff",
    padding:
      "8px 13px",
    borderRadius: "7px",
    cursor: "pointer",
    fontSize: "13px",
    fontWeight: "600",
  },

  infoBanner: {
    background: "#eef2ff",
    border:
      "1px solid #c7d2fe",
    borderRadius: "14px",
    padding:
      "18px 20px",
    display: "flex",
    alignItems: "center",
    gap: "15px",
    marginTop: "5px",
  },

  infoBannerIcon: {
    fontSize: "28px",
  },

  infoBannerTitle: {
    margin: 0,
    fontSize: "16px",
  },

  infoBannerText: {
    margin:
      "5px 0 0",
    color: "#4b5563",
    fontSize: "14px",
  },

  modalOverlay: {
    position: "fixed",
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background:
      "rgba(15, 23, 42, 0.65)",
    display: "flex",
    alignItems: "center",
    justifyContent:
      "center",
    padding: "20px",
    zIndex: 9999,
  },

  modal: {
    width: "100%",
    maxWidth: "600px",
    maxHeight: "90vh",
    overflowY: "auto",
    background: "#ffffff",
    borderRadius: "18px",
    padding: "28px",
    boxShadow:
      "0 25px 60px rgba(0,0,0,0.25)",
  },

  modalHeader: {
    display: "flex",
    justifyContent:
      "space-between",
    alignItems:
      "flex-start",
    gap: "15px",
    marginBottom: "25px",
  },

  modalTitle: {
    margin: 0,
    fontSize: "24px",
  },

  modalSubtitle: {
    margin:
      "6px 0 0",
    color: "#6b7280",
    fontSize: "14px",
  },

  closeButton: {
    border: "none",
    background: "#f3f4f6",
    color: "#374151",
    width: "36px",
    height: "36px",
    borderRadius: "50%",
    fontSize: "24px",
    cursor: "pointer",
    lineHeight: "1",
  },

  formRow: {
    display: "grid",
    gridTemplateColumns:
      "repeat(2, 1fr)",
    gap: "15px",
  },

  formGroup: {
    marginBottom: "18px",
  },

  formLabel: {
    display: "block",
    marginBottom: "7px",
    fontSize: "14px",
    fontWeight: "600",
    color: "#374151",
  },

  input: {
    width: "100%",
    boxSizing: "border-box",
    border:
      "1px solid #d1d5db",
    borderRadius: "9px",
    padding:
      "12px 13px",
    fontSize: "14px",
    outline: "none",
    background: "#ffffff",
  },

  textarea: {
    width: "100%",
    boxSizing: "border-box",
    border:
      "1px solid #d1d5db",
    borderRadius: "9px",
    padding:
      "12px 13px",
    fontSize: "14px",
    outline: "none",
    resize: "vertical",
    fontFamily: "inherit",
  },

  characterCount: {
    display: "block",
    textAlign: "right",
    marginTop: "5px",
    color: "#9ca3af",
    fontSize: "12px",
  },

  modalActions: {
    display: "flex",
    justifyContent:
      "flex-end",
    gap: "10px",
    marginTop: "10px",
  },

  cancelButton: {
    border:
      "1px solid #d1d5db",
    background: "#ffffff",
    color: "#374151",
    padding:
      "11px 18px",
    borderRadius: "9px",
    cursor: "pointer",
    fontWeight: "600",
  },

  saveButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding:
      "11px 20px",
    borderRadius: "9px",
    cursor: "pointer",
    fontWeight: "600",
  },

  center: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent:
      "center",
    flexDirection: "column",
    gap: "10px",
    padding: "20px",
  },

  loadingIcon: {
    fontSize: "45px",
  },

  loadingText: {
    color: "#6b7280",
  },

  errorIcon: {
    fontSize: "45px",
  },

  error: {
    color: "#b91c1c",
    marginBottom: "15px",
    textAlign: "center",
  },

  primaryButton: {
    border: "none",
    background: "#111827",
    color: "#ffffff",
    padding:
      "11px 18px",
    borderRadius: "8px",
    cursor: "pointer",
    fontWeight: "600",
  },

};

export default TripDetails;