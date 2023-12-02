window.addEventListener("load", (event) => {
  console.log("page is fully loaded");

  document.getElementById('imgBtn').addEventListener('click', function () {
    document.getElementById('imgFileInput').click();
  });

  document.getElementById('imgFileInput').addEventListener("change", function (e) {
    console.log("change detected");

    var reader = new FileReader();
    reader.onload = function (e) {
      // get loaded data and render thumbnail.
      document.getElementById("imgBtn").src = e.target.result;
    };

    // read the image file as a data URL.
    reader.readAsDataURL(this.files[0]);
  });

  const checkboxes = document.getElementsByClassName('participateCheckbox');
  for (let i = 0; i < checkboxes.length; i++) {
    checkboxes[i].addEventListener('change', function () {
      const amount = document.getElementById('amount').value;
      onChangeAmount(amount);
      validateCheckboxes();
    });
  }

  // Submit Button
  document.getElementById("expenseForm").addEventListener('submit', (e) => {
    e.preventDefault();
      console.log('Saving expense');
      const b = document.getElementById("btn-save");

      const formData = new FormData();
      if (document.getElementById("imgFileInput").files[0] !== undefined) {
        formData.append('imageFile', document.getElementById("imgFileInput").files[0]);
      }
      formData.append('name', document.getElementById("name").value);
      formData.append('desc', document.getElementById("desc").value);
      formData.append('dateString', document.getElementById("dateString").value);
      formData.append('amount', document.getElementById("amount").value);
      formData.append('paidById', document.getElementById("paidById").value);
      formData.append('participateIds', Array.from(document.querySelectorAll('input[name="participateIds"]:checked')).map(cb => cb.value));
      formData.append('typeId', document.getElementById("typeId").value);

      go(b.getAttribute('formaction'), 'POST', formData, {})
        .then(d => {
          console.log("Expense: success", d);
          createToastNotification(`expense success`, `Expense changed successfully`);
          if (d.action === "redirect") {
            console.log("Redirecting to ", d.redirect);
            window.location.replace(d.redirect);
          }
        })
        .catch(e => {
          console.log("Error creating expense", e);
          createToastNotification(`error-expense-creation`, JSON.parse(e.text).message, true);
        })
  });
});


 // Delete expense Button
let b = document.getElementById("confirmBtn");
if (b != null) {
  b.onclick = (e) => {
    e.preventDefault();

    console.log('Deleting expense');

    go(b.getAttribute('formaction'), 'POST', {})
      .then(d => {
        console.log("Expense delete: success", d);
        if (d.action === "redirect") {
          console.log("Redirecting to ", d.redirect);
          window.location.replace(d.redirect);
        }
      })
      .catch(e => {
        console.log("Error deleting expense", e);
        createToastNotification(`error-expense-deletion`, JSON.parse(e.text).message, true);
      })
  };
} 

document.getElementById('amount').addEventListener('input', function () {
  const value = this.value.replace(/[^\d.]/g, ''); // remove any non-digit or non-decimal point characters
  const decimalIndex = value.indexOf('.');
  if (decimalIndex !== -1) {
    const decimalPlaces = value.length - decimalIndex - 1;
    if (decimalPlaces > 2) {
      this.value = value.substring(0, decimalIndex + 3); // truncate to 2 decimal places
      return;
    }
  }
  this.value = value;
  this.setCustomValidity("");
  if (value === "") {
    this.setCustomValidity("Please fill out this field");
  }
  else if (value <= 0) {
    this.setCustomValidity("Amount must be larger than 0");
  } else {
    onChangeAmount(value);
  }
});


/* Changes value pero user */
function onChangeAmount(amount) {
  console.log(`onChangeAmount(${amount}) called`);

  if (amount === "") {
    return;
  }

  const checkboxes = document.getElementsByClassName('participateCheckbox');
  const numChecked = document.querySelectorAll('input:checked').length;
  const values = document.getElementsByClassName('amountPerMember');

  for (let i = 0; i < checkboxes.length; i++) {
    if (checkboxes[i].checked) {
      values[i].innerHTML = (Math.round(amount / numChecked * 100) / 100).toFixed(2);
    }
    else {
      values[i].innerHTML = '';
    }
  }
}

// PARA QUE LA FECHA DE UN EXPENSE NO SEA SUPERIOR A LA ACTUAL
// Obtiene el elemento input de fecha
const fechaInput = document.getElementById('dateString');
// Obtiene la fecha actual en formato ISO (YYYY-MM-DD)
const fechaActual = new Date().toISOString().split('T')[0];
// Establece la fecha máxima en el input de fecha
fechaInput.max = fechaActual;

// Validate at least 1 participant
function validateCheckboxes() {
  const checkboxes = document.querySelectorAll('input[name="participateIds"]:checked');

  const firstCheckbox = document.querySelector('input[name="participateIds"]');
  firstCheckbox.setCustomValidity("");
  if (checkboxes.length === 0) {
    firstCheckbox.setCustomValidity("Please select a participant");
    firstCheckbox.reportValidity();
    return false;
  }
  firstCheckbox.reportValidity();
  return true;
}

// Validate image size
