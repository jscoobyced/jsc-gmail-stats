function getFormattedDate(id, label) {
  const value = document.getElementById(id).value;
  if (value.replaceAll(" ", "") === "") {
    showError("Please enter a valid date for " + label + ".");
    return "";
  }
  const dateValue = new Date(value);
  const offset = dateValue.getTimezoneOffset();
  const formattedDate = new Date(dateValue.getTime() - offset * 60 * 1000)
    .toISOString()
    .split("T")[0];
  return formattedDate;
}

function _showMessage(id, message) {
  document.getElementById(id).innerHTML = message;
}

function showError(message) {
  _showMessage("error", message);
}

function showMessage(message) {
  _showMessage("message", message);
}

function clearMessages() {
  showError("");
  showMessage("");
}

function processCountResponse(data) {
  const result = data.result;
  if (!result.success) {
    switch (result.errorCode) {
      case 1:
        // Label not found
        showError(result.message);
        break;
      case 2:
        // User not registered
        showError(result.message);
        showMessage("Click the 'Register' button to register.");
        document.getElementById("register").disabled = false;
        break;
      case 3:
        // Missing field
        showError(result.message);
        break;
      case 6:
        // Incorrect password
        showError(result.message);
        break;
      default:
        showError(result.message);
    }
    return;
  }

  const emailCount = data.emailCount.emailCount;
  const dateFrom = data.emailCount.dateFrom;
  const dateTo = data.emailCount.dateTo;
  showMessage(
    "You have " +
      emailCount +
      " emails between " +
      dateFrom +
      " and " +
      dateTo +
      "."
  );
}

function getEmail() {
  return document
    .getElementById("email")
    .value.replaceAll(".", "")
    .replaceAll("_", "")
    .replaceAll("+", "")
    .replaceAll("@", "")
    .replaceAll("-", "");
}

function countEmails() {
  clearMessages();
  const email = getEmail();
  if (email === "") {
    showError("Please enter an email.");
    return;
  }

  const password = document.getElementById("password").value;
  if (password === "") {
    showError("Please enter a password.");
    return;
  }

  const dateFrom = getFormattedDate("date-from", "Start Date");
  if (dateFrom === "") return;
  const dateTo = getFormattedDate("date-to", "End Date");
  if (dateTo === "") return;

  const label = "SENT";

  const url = "/count";
  const payload = {
    email,
    password,
    label,
    dateFrom,
    dateTo,
  };
  fetch(url, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })
    .then((response) => response.json())
    .then((data) => {
      processCountResponse(data);
    })
    .catch(function (error) {
      console.log(error);
    });
}

function processLoginResponse(data) {
  if (!data.success) {
    switch (data.errorCode) {
      case 3:
        // Missing field
        showError(data.message);
        break;
      default:
        showError(data.message);
    }
    return;
  }

  const authorizationUrl = data.message;
  const textLink =
    "You can register your Gmail account <a href='" +
    authorizationUrl +
    "' target='_blank'>here</a>.";
  showMessage(textLink);
}

function register() {
  clearMessages();
  const email = getEmail();
  if (email === "") {
    showError("Please enter an email.");
    return;
  }

  const password = document.getElementById("password").value;
  if (password === "") {
    showError("Please enter a password.");
    return;
  }

  const url = "/login";
  const payload = {
    email,
    password,
  };
  fetch(url, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })
    .then((response) => response.json())
    .then((data) => {
      processLoginResponse(data);
    })
    .catch(function (error) {
      console.log(error);
    });
}

function processUnregisterResponse(data) {
  if (!data.success) {
    showError(data.message);
    return;
  }
  showMessage("You have successfully unregistered.");
}

function unregister() {
  clearMessages();
  const email = getEmail();
  if (email === "") {
    showError("Please enter an email.");
    return;
  }

  const url = "/unregister";
  const payload = {
    email,
  };
  fetch(url, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  })
    .then((response) => response.json())
    .then((data) => {
      processUnregisterResponse(data);
    })
    .catch(function (error) {
      console.log(error);
    });
}
